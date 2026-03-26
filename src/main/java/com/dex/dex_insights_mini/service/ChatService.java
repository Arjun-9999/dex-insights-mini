package com.dex.dex_insights_mini.service;

import com.dex.dex_insights_mini.dto.ChatRequest;
import com.dex.dex_insights_mini.dto.ChatResponse;
import com.dex.dex_insights_mini.exception.StoreNotFoundException;
import com.dex.dex_insights_mini.model.*;
import com.dex.dex_insights_mini.repository.JsonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final double LOW_TANK_THRESHOLD_RATIO = 0.20d;
    private final List<Store> stores;
    private final List<Transaction> transactions;
    private final List<Incident> incidents;

    public ChatService(JsonRepository repo) {
        this.stores = repo.loadStores();
        this.transactions = repo.loadTransactions();
        this.incidents = repo.loadIncidents();
    }

    public ChatResponse answerQuestion(ChatRequest request) {
        String selectedStoreId = request.getStoreId();
        String question = request.getQuestion() == null ? "" : request.getQuestion().toLowerCase().trim();

        log.info("event=chat_answer storeId={} question={}", selectedStoreId, question);

        // Extract store ID mentioned in question (e.g., "show incidents for store 10001")
        String mentionedStoreId = extractStoreIdFromQuestion(question);
        String contextStoreId = mentionedStoreId != null ? mentionedStoreId : selectedStoreId;

        // No store context → portfolio-level answer
        if (contextStoreId == null || contextStoreId.isBlank()) {
            return answerPortfolioQuestion(question);
        }

        Store store = stores.stream()
                .filter(s -> s.getStoreId().equals(contextStoreId))
                .findFirst().orElse(null);

        if (store == null) {
            throw new StoreNotFoundException(contextStoreId);
        }

        // Greeting
        if (isGreeting(question)) {
            return new ChatResponse(
                    String.format("Hello! I can answer questions about Store %s (%s). Try asking about pumps, incidents, transactions or status.",
                            store.getStoreId(), store.getBrand()),
                    List.of(buildStoreCitationMap(store)),
                    "Greeting detected"
            );
        }

        List<Transaction> recentTx = retrieveRelevantTransactions(contextStoreId, question, 50);
        List<Incident> recentIncidents = retrieveRelevantIncidents(contextStoreId, question, 50);

        List<Map<String, String>> citations = new ArrayList<>();
        citations.add(Map.of("type", "store", "storeId", store.getStoreId(), "lastUpdatedTime", store.getLastUpdatedTime().toString()));

        // Pump-related question
        if (question.contains("pump") || question.contains("offline")) {
            List<Incident> pumpIncidents = retrieveRelevantIncidents(contextStoreId, "pump offline", 5);
            String answer = String.format(
                    "Store %s (%s, %s) has %d total pumps — %d active and %d offline.",
                    store.getStoreId(), store.getStoreAddress().getCity(), store.getStoreAddress().getState(),
                    store.getTotalPumps(), store.getActivePumps(), store.getOfflinePumps()
            );
            return groundAnswerWithRAG(store, List.of(), pumpIncidents, answer);
        }

        // Incident-related question
        if (question.contains("incident") || question.contains("issue") || question.contains("problem") || question.contains("alert")) {
            List<Incident> relevantIncidents = retrieveRelevantIncidents(contextStoreId, question, 5);
            String answer = String.format(
                    "Store %s had %d incident(s) in the last 24 hours.",
                    store.getStoreId(), relevantIncidents.size()
            );
            return groundAnswerWithRAG(store, List.of(), relevantIncidents, answer);
        }

        // Transaction/revenue-related question
        if (question.contains("transaction") || question.contains("revenue") || question.contains("sales") || question.contains("amount")) {
            List<Transaction> relevantTransactions = retrieveRelevantTransactions(contextStoreId, question, 10);
            double totalRevenue = relevantTransactions.stream().mapToDouble(t -> Double.parseDouble(t.getTransactionAmnt())).sum();
            String answer = String.format(
                    "Store %s had %d transaction(s) in the last 24 hours totaling $%.2f.",
                    store.getStoreId(), relevantTransactions.size(), totalRevenue
            );
            return groundAnswerWithRAG(store, relevantTransactions, List.of(), answer);
        }

        // Status-related question
        if (question.contains("status") || question.contains("online") || question.contains("active") || question.contains("health")) {
            String answer = String.format(
                    "Store %s (%s, %s) is currently %s.",
                    store.getStoreId(), store.getStoreAddress().getCity(),
                    store.getStoreAddress().getState(), store.getStatus()
            );
            List<Transaction> statusTx = retrieveRelevantTransactions(contextStoreId, question, 5);
            List<Incident> statusIncidents = retrieveRelevantIncidents(contextStoreId, question, 5);
            return groundAnswerWithRAG(store, statusTx, statusIncidents, answer);
        }

        // Tank-related question
        if (question.contains("tank") || question.contains("fuel") || question.contains("level") || question.contains("capacity")) {
            int tankCount = store.getTanks() == null ? 0 : store.getTanks().size();
            String answer = String.format(
                    "Store %s has %d fuel tank(s).",
                    store.getStoreId(), tankCount
            );
            List<Map<String, String>> tankCitations = new ArrayList<>();
            tankCitations.add(buildStoreCitationMap(store));
            if (store.getTanks() != null) {
                store.getTanks().forEach(t -> {
                    Map<String, String> c = new HashMap<>();
                    c.put("type", "tank");
                    c.put("storeId", store.getStoreId());
                    c.put("gradeName", t.getGradeName() != null ? t.getGradeName() : "");
                    c.put("capacityGallons", String.valueOf(t.getCapacityGallons()));
                    c.put("levelGallons", String.valueOf(t.getLevelGallons()));
                    c.put("ullageGallons", String.valueOf(t.getUllageGallons()));
                    c.put("lastUpdatedTime", t.getLastUpdatedTime() != null ? t.getLastUpdatedTime().toString() : "");
                    tankCitations.add(c);
                });
            }
            return new ChatResponse(answer, tankCitations, "Tank data retrieved – " + tankCount + " tank(s)");
        }

        // Explicit or implied overview question for selected/mentioned store
        if (mentionedStoreId != null || isStoreOverviewQuestion(question)) {
            recentTx.forEach(t -> citations.add(Map.of("type", "transaction", "transactionId", t.getTransactionId(), "transactionEndTime", t.getTransactionEndTime().toString())));
            recentIncidents.forEach(i -> citations.add(Map.of("type", "incident", "incidentId", i.getIncidentId(), "severity", i.getSeverity(), "timestamp", i.getTimestamp().toString())));

            String answer = String.format(
                    "Store %s (%s, %s) is %s with %d offline pumps. In the last 24h: %d transactions ($%.2f), %d incident(s).",
                    store.getStoreId(),
                    store.getStoreAddress().getCity(),
                    store.getStoreAddress().getState(),
                    store.getStatus(),
                    store.getOfflinePumps(),
                    recentTx.size(),
                    recentTx.stream().mapToDouble(t -> Double.parseDouble(t.getTransactionAmnt())).sum(),
                    recentIncidents.size()
            );
            return new ChatResponse(answer, citations, "Store summary retrieved (all available data)");
        }

        // Unrecognized question → guide user
        return new ChatResponse(
                String.format("Sorry, I didn't understand that question for Store %s (%s). " +
                        "You can ask me about: pumps (offline/active), incidents, transactions, revenue, status, or tank levels.",
                        store.getStoreId(), store.getBrand()),
                citations,
                "Unrecognized question"
        );
    }

    /**
     * Extract store ID from question text (e.g., "show incidents for store 10001" → "10001")
     */
    private String extractStoreIdFromQuestion(String question) {
        // Match patterns like "store 10001", "store10001", "for 10001", etc.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:store\\s+|for\\s+|in\\s+)(\\d{5})");
        java.util.regex.Matcher matcher = pattern.matcher(question);
        
        if (matcher.find()) {
            String extractedId = matcher.group(1);
            boolean storeExists = stores.stream().anyMatch(s -> s.getStoreId().equals(extractedId));
            if (!storeExists) {
                throw new StoreNotFoundException(extractedId);
            }
            return extractedId;
        }
        
        return null;
    }

    private boolean isGreeting(String question) {
        return question.matches("^(hi|hello|hey|howdy|greetings|good morning|good afternoon|good evening)[.!?]?$");
    }

    private boolean isStoreOverviewQuestion(String question) {
        return question.contains("how is")
                || question.contains("how's")
                || question.contains("doing")
                || question.contains("tell me about")
                || question.contains("about this store")
                || question.contains("store summary")
                || question.contains("store overview");
    }

    // ============ RAG (Retrieval-Augmented Generation) Methods ============

    /**
     * Retrieve relevant stores based on question keywords.
     * Ranks by relevance score.
     */
    private List<Store> retrieveRelevantStores(String question, int limit) {
        return stores.stream()
                .map(store -> {
                    int relevanceScore = calculateStoreRelevance(store, question);
                    return new AbstractMap.SimpleEntry<>(store, relevanceScore);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calculate relevance score for a store based on question keywords.
     */
    private int calculateStoreRelevance(Store store, String question) {
        int score = 0;

        // Boost if question mentions offline/status and store has offline pumps
        if ((question.contains("offline") || question.contains("problem")) && store.getOfflinePumps() > 0) {
            score += 10;
        }

        // Boost if question mentions incidents and store has recent incidents
        if (question.contains("incident") || question.contains("alert")) {
            score += 5;
        }

        // Boost if question mentions location/city
        if (store.getStoreAddress() != null && store.getStoreAddress().getCity() != null) {
            String city = store.getStoreAddress().getCity().toLowerCase();
            if (question.contains(city)) {
                score += 8;
            }
        }

        // Boost if question mentions store brand
        if (store.getBrand() != null && question.contains(store.getBrand().toLowerCase())) {
            score += 7;
        }

        return score;
    }

    /**
     * Retrieve and rank relevant transactions based on question.
     */
    private List<Transaction> retrieveRelevantTransactions(String storeId, String question, int limit) {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));

        return transactions.stream()
                .filter(t -> t.getStoreId().equals(storeId))
                .filter(t -> t.getTransactionEndTime().isAfter(cutoff))
                .sorted(Comparator.comparing(Transaction::getTransactionEndTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve and rank relevant incidents based on question.
     */
    private List<Incident> retrieveRelevantIncidents(String storeId, String question, int limit) {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        
        return incidents.stream()
                .filter(i -> i.getStoreId().equals(storeId))
                .filter(i -> i.getTimestamp().isAfter(cutoff))
                // Rank by severity if question asks about severity/high-priority
                .sorted((a, b) -> {
                    if (question.contains("critical") || question.contains("high")) {
                        return b.getSeverity().compareTo(a.getSeverity());
                    }
                    return b.getTimestamp().compareTo(a.getTimestamp());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Build a single store citation map with full operational fields.
     */
    private Map<String, String> buildStoreCitationMap(Store store) {
        Map<String, String> c = new HashMap<>();
        c.put("type", "store");
        c.put("storeId", store.getStoreId());
        c.put("brand", store.getBrand() != null ? store.getBrand() : "");
        c.put("status", store.getStatus() != null ? store.getStatus() : "");
        c.put("location", store.getStoreAddress() != null
                ? store.getStoreAddress().getCity() + ", " + store.getStoreAddress().getState()
                : "Unknown");
        c.put("totalPumps", String.valueOf(store.getTotalPumps()));
        c.put("activePumps", String.valueOf(store.getActivePumps()));
        c.put("offlinePumps", String.valueOf(store.getOfflinePumps()));
        c.put("hyperCare", store.isHyperCare() ? "Yes" : "No");
        c.put("lastUpdatedTime", store.getLastUpdatedTime().toString());
        return c;
    }

    /**
     * Build grounded citations from retrieved data.
     */
    private List<Map<String, String>> buildGroundedCitations(Store store,
                                                               List<Transaction> transactions,
                                                               List<Incident> incidents) {
        List<Map<String, String>> citations = new ArrayList<>();

        // Store citation (primary context) – include operational fields
        Map<String, String> storeCitation = new HashMap<>();
        storeCitation.put("type", "store");
        storeCitation.put("storeId", store.getStoreId());
        storeCitation.put("brand", store.getBrand() != null ? store.getBrand() : "");
        storeCitation.put("status", store.getStatus() != null ? store.getStatus() : "");
        storeCitation.put("location", store.getStoreAddress() != null
                ? store.getStoreAddress().getCity() + ", " + store.getStoreAddress().getState()
                : "Unknown");
        storeCitation.put("totalPumps", String.valueOf(store.getTotalPumps()));
        storeCitation.put("activePumps", String.valueOf(store.getActivePumps()));
        storeCitation.put("offlinePumps", String.valueOf(store.getOfflinePumps()));
        storeCitation.put("hyperCare", store.isHyperCare() ? "Yes" : "No");
        storeCitation.put("lastUpdatedTime", store.getLastUpdatedTime().toString());
        citations.add(storeCitation);

        // Transaction citations – include fuel grade, volume, dispenser
        transactions.forEach(t -> {
            Map<String, String> c = new HashMap<>();
            c.put("type", "transaction");
            c.put("transactionId", t.getTransactionId());
            c.put("storeId", t.getStoreId());
            c.put("amount", "$" + t.getTransactionAmnt());
            c.put("gradeName", t.getGradeName() != null ? t.getGradeName() : "");
            c.put("volume", t.getVolume() != null ? t.getVolume() + " gal" : "");
            c.put("dispenserId", String.valueOf(t.getDispenserId()));
            c.put("timestamp", t.getTransactionEndTime().toString());
            citations.add(c);
        });

        // Incident citations – include category, description, status
        incidents.forEach(i -> {
            Map<String, String> c = new HashMap<>();
            c.put("type", "incident");
            c.put("incidentId", i.getIncidentId());
            c.put("storeId", i.getStoreId());
            c.put("severity", i.getSeverity() != null ? i.getSeverity() : "");
            c.put("category", i.getCategory() != null ? i.getCategory() : "");
            c.put("description", i.getDescription() != null ? i.getDescription() : "");
            c.put("status", i.getStatus() != null ? i.getStatus() : "");
            c.put("timestamp", i.getTimestamp().toString());
            citations.add(c);
        });

        return citations;
    }

    /**
     * Ground answer with evidence from retrieved and ranked data.
     */
    private ChatResponse groundAnswerWithRAG(Store store, List<Transaction> relevantTransactions,
                                              List<Incident> relevantIncidents, String answerTemplate) {
        List<Map<String, String>> citations = buildGroundedCitations(store, relevantTransactions, relevantIncidents);

        String groundingNote = "Evidence from " + (relevantTransactions.size() + relevantIncidents.size()) + 
                               " data points in last 24h (ranked by relevance)";

        return new ChatResponse(answerTemplate, citations, groundingNote);
    }

    private ChatResponse answerPortfolioQuestion(String question) {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));

        List<Transaction> recentTx = transactions.stream()
                .filter(t -> t.getTransactionEndTime().isAfter(cutoff))
                .toList();

        List<Incident> recentIncidents = incidents.stream()
                .filter(i -> i.getTimestamp().isAfter(cutoff))
                .toList();

        long storesWithOffline = stores.stream().filter(s -> s.getOfflinePumps() > 0).count();
        int totalOfflinePumps = stores.stream().mapToInt(Store::getOfflinePumps).sum();
        double totalRevenue24h = recentTx.stream().mapToDouble(t -> Double.parseDouble(t.getTransactionAmnt())).sum();

        List<Store> topOfflineStores = retrieveRelevantStores(question, 3);
        if (topOfflineStores.isEmpty()) {
            topOfflineStores = stores.stream()
                    .sorted((a, b) -> Integer.compare(b.getOfflinePumps(), a.getOfflinePumps()))
                    .limit(3)
                    .toList();
        }

        List<Map<String, String>> citations = new ArrayList<>();

        // Greeting
        if (isGreeting(question)) {
            topOfflineStores.forEach(s -> citations.add(buildStoreCitationMap(s)));
            return new ChatResponse(
                    "Hello! I can answer portfolio-wide questions like pump status, incidents, transactions, or revenue. Select a store for specific details.",
                    citations,
                    "Greeting detected"
            );
        }

        // Pump-related
        if (question.contains("pump") || question.contains("offline")) {
            topOfflineStores.forEach(s -> citations.add(buildStoreCitationMap(s)));
            String answer = String.format(
                    "Across %d stores, %d have offline pumps (%d pumps offline in total). Top stores by offline pumps: %s.",
                    stores.size(), storesWithOffline, totalOfflinePumps,
                    topOfflineStores.stream().map(s -> s.getStoreId() + "(" + s.getOfflinePumps() + ")").reduce((a, b) -> a + ", " + b).orElse("none")
            );
            return new ChatResponse(answer, citations, "Portfolio pump data retrieved");
        }

        // Tank/runout-risk related
        if (question.contains("tank")
                || question.contains("fuel")
                || question.contains("runout")
                || question.contains("risk")
                || (question.contains("low") && question.contains("level"))) {

            List<Map<String, Object>> atRiskTanks = stores.stream()
                    .filter(s -> s.getTanks() != null)
                    .flatMap(s -> s.getTanks().stream()
                            .filter(t -> t.getCapacityGallons() > 0)
                            .map(t -> Map.<String, Object>of(
                                    "store", s,
                                    "tank", t,
                                    "ratio", (double) t.getLevelGallons() / t.getCapacityGallons()
                            )))
                    .filter(entry -> (double) entry.get("ratio") < LOW_TANK_THRESHOLD_RATIO)
                    .sorted(Comparator.comparingDouble(entry -> (double) entry.get("ratio")))
                    .toList();

            if (atRiskTanks.isEmpty()) {
                return new ChatResponse(
                        "No immediate runout risk detected. I found no tanks below 20% level across the portfolio.",
                        citations,
                        "Portfolio tank-level scan completed"
                );
            }

            atRiskTanks.stream().limit(5).forEach(entry -> {
                Store store = (Store) entry.get("store");
                Tank tank = (Tank) entry.get("tank");
                double ratio = (double) entry.get("ratio");

                citations.add(buildStoreCitationMap(store));

                Map<String, String> tankCitation = new HashMap<>();
                tankCitation.put("type", "tank");
                tankCitation.put("storeId", store.getStoreId());
                tankCitation.put("gradeName", tank.getGradeName() != null ? tank.getGradeName() : "");
                tankCitation.put("capacityGallons", String.valueOf(tank.getCapacityGallons()));
                tankCitation.put("levelGallons", String.valueOf(tank.getLevelGallons()));
                tankCitation.put("ullageGallons", String.valueOf(tank.getUllageGallons()));
                tankCitation.put("riskPct", String.format("%.1f", ratio * 100));
                tankCitation.put("lastUpdatedTime", tank.getLastUpdatedTime() != null ? tank.getLastUpdatedTime().toString() : "");
                citations.add(tankCitation);
            });

            long affectedStoreCount = atRiskTanks.stream()
                    .map(entry -> ((Store) entry.get("store")).getStoreId())
                    .distinct()
                    .count();

            String topRisks = atRiskTanks.stream()
                    .limit(3)
                    .map(entry -> {
                        Store store = (Store) entry.get("store");
                        Tank tank = (Tank) entry.get("tank");
                        double ratio = (double) entry.get("ratio");
                        String grade = tank.getGradeName() == null ? "tank" : tank.getGradeName();
                        return store.getStoreId() + " " + grade + " (" + String.format("%.1f", ratio * 100) + "%)";
                    })
                    .collect(Collectors.joining(", "));

            String answer = String.format(
                    "Yes. %d store(s) show runout risk with at least one tank below 20%%. Most at-risk tanks: %s.",
                    affectedStoreCount,
                    topRisks
            );

            return new ChatResponse(answer, citations, "Portfolio tank-risk analysis retrieved");
        }

        // Incident-related
        if (question.contains("incident") || question.contains("issue") || question.contains("problem") || question.contains("alert")) {
            recentIncidents.stream().limit(5).forEach(i -> {
                Map<String, String> c = new HashMap<>();
                c.put("type", "incident");
                c.put("incidentId", i.getIncidentId());
                c.put("storeId", i.getStoreId());
                c.put("severity", i.getSeverity() != null ? i.getSeverity() : "");
                c.put("timestamp", i.getTimestamp() != null ? i.getTimestamp().toString() : "");
                citations.add(c);
            });
            Map<String, Long> bySeverity = recentIncidents.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Incident::getSeverity, java.util.stream.Collectors.counting()));
            String answer = String.format(
                    "In the last 24 hours, %d incident(s) across all stores. By severity: %s.",
                    recentIncidents.size(), bySeverity
            );
            return new ChatResponse(answer, citations, "Portfolio incident data retrieved");
        }

        // Transaction/revenue-related
        if (question.contains("transaction") || question.contains("revenue") || question.contains("sales") || question.contains("amount")) {
            recentTx.stream().limit(5).forEach(t -> {
                Map<String, String> c = new HashMap<>();
                c.put("type", "transaction");
                c.put("transactionId", t.getTransactionId());
                c.put("storeId", t.getStoreId());
                c.put("amount", t.getTransactionAmnt() != null ? t.getTransactionAmnt() : "");
                c.put("timestamp", t.getTransactionEndTime() != null ? t.getTransactionEndTime().toString() : "");
                citations.add(c);
            });
            String answer = String.format(
                    "In the last 24 hours, %d transaction(s) totaling $%.2f across all stores.",
                    recentTx.size(), totalRevenue24h
            );
            return new ChatResponse(answer, citations, "Portfolio transaction data retrieved");
        }

        // Status-related
        if (question.contains("status") || question.contains("online") || question.contains("health") || question.contains("summary") || question.contains("overview")) {
            long onlineCount = stores.stream().filter(s -> "ONLINE".equalsIgnoreCase(s.getStatus())).count();
            long offlineCount = stores.stream().filter(s -> "OFFLINE".equalsIgnoreCase(s.getStatus())).count();
            String answer = String.format(
                    "Across %d stores: %d online, %d offline. %d stores have offline pumps (%d total). In 24h: %d transactions ($%.2f revenue), %d incidents.",
                    stores.size(), onlineCount, offlineCount, storesWithOffline, totalOfflinePumps,
                    recentTx.size(), totalRevenue24h, recentIncidents.size()
            );
            topOfflineStores.forEach(s -> citations.add(buildStoreCitationMap(s)));
            recentTx.stream().limit(3).forEach(t -> {
                Map<String, String> c = new HashMap<>();
                c.put("type", "transaction");
                c.put("transactionId", t.getTransactionId());
                c.put("storeId", t.getStoreId());
                c.put("timestamp", t.getTransactionEndTime() != null ? t.getTransactionEndTime().toString() : "");
                citations.add(c);
            });
            recentIncidents.stream().limit(3).forEach(i -> {
                Map<String, String> c = new HashMap<>();
                c.put("type", "incident");
                c.put("incidentId", i.getIncidentId());
                c.put("storeId", i.getStoreId());
                c.put("timestamp", i.getTimestamp() != null ? i.getTimestamp().toString() : "");
                citations.add(c);
            });
            return new ChatResponse(answer, citations, "Portfolio-wide status retrieved");
        }

        // Unrecognized question → guide user
        return new ChatResponse(
                "Sorry, I didn't understand that question. You can ask me about: " +
                        "offline pumps, incidents, transactions, revenue, or overall store status.",
                citations,
                "Unrecognized question"
        );
    }
}

