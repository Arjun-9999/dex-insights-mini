package com.dex.dex_insights_mini.service;

import com.dex.dex_insights_mini.dto.ChatRequest;
import com.dex.dex_insights_mini.dto.ChatResponse;
import com.dex.dex_insights_mini.exception.InvalidRequestException;
import com.dex.dex_insights_mini.exception.StoreNotFoundException;
import com.dex.dex_insights_mini.model.*;
import com.dex.dex_insights_mini.repository.JsonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final List<Store> stores;
    private final List<Transaction> transactions;
    private final List<Incident> incidents;

    public ChatService(JsonRepository repo) {
        this.stores = repo.loadStores();
        this.transactions = repo.loadTransactions();
        this.incidents = repo.loadIncidents();
    }

    public ChatResponse answerQuestion(ChatRequest request) {
        String storeId = request.getStoreId();

        if (storeId == null || storeId.isBlank()) {
            throw new InvalidRequestException("storeId is required for chat context");
        }

        log.info("event=chat_answer storeId={} questionLength={}",
                storeId,
                request.getQuestion() == null ? 0 : request.getQuestion().length());
        Store store = stores.stream()
                .filter(s -> s.getStoreId().equals(storeId))
                .findFirst().orElse(null);

        if (store == null) {
            throw new StoreNotFoundException(storeId);
        }

        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        List<Transaction> recentTx = transactions.stream()
                .filter(t -> t.getStoreId().equals(storeId))
                .filter(t -> t.getTransactionEndTime().isAfter(cutoff))
                .toList();

        List<Incident> recentIncidents = incidents.stream()
                .filter(i -> i.getStoreId().equals(storeId))
                .filter(i -> i.getTimestamp().isAfter(cutoff))
                .toList();

        String answer = String.format(
                "Store %s (%s, %s) is %s with %d offline pumps. " +
                        "In the last 24h, %d transactions totaling $%.2f. " +
                        "Recent incidents: %d.",
                store.getStoreId(),
                store.getStoreAddress().getCity(),
                store.getStoreAddress().getState(),
                store.getStatus(),
                store.getOfflinePumps(),
                recentTx.size(),
                recentTx.stream().mapToDouble(t -> Double.parseDouble(t.getTransactionAmnt())).sum(),
                recentIncidents.size()
        );

        List<Map<String, String>> citations = new ArrayList<>();
        citations.add(Map.of("type", "store", "storeId", store.getStoreId(), "lastUpdatedTime", store.getLastUpdatedTime().toString()));
        recentTx.forEach(t -> citations.add(Map.of("type", "transaction", "transactionId", t.getTransactionId(), "transactionEndTime", t.getTransactionEndTime().toString())));
        recentIncidents.forEach(i -> citations.add(Map.of("type", "incident", "incidentId", i.getIncidentId(), "timestamp", i.getTimestamp().toString())));

        return new ChatResponse(answer, citations, "Store status, transactions, and incidents retrieved");
    }
}