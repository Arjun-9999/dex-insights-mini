package com.dex.dex_insights_mini.service;

import com.dex.dex_insights_mini.dto.ChatRequest;
import com.dex.dex_insights_mini.dto.ChatResponse;
import com.dex.dex_insights_mini.exception.StoreNotFoundException;
import com.dex.dex_insights_mini.model.Incident;
import com.dex.dex_insights_mini.model.Store;
import com.dex.dex_insights_mini.model.StoreAddress;
import com.dex.dex_insights_mini.model.Tank;
import com.dex.dex_insights_mini.model.Transaction;
import com.dex.dex_insights_mini.repository.JsonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private JsonRepository jsonRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        Store targetStore = store("S1", "Shell", "ONLINE", 2, "Austin", "TX");
        Store otherStore = store("S2", "BP", "OFFLINE", 1, "Dallas", "TX");

        when(jsonRepository.loadStores()).thenReturn(List.of(targetStore, otherStore));

        Instant now = Instant.now();
        when(jsonRepository.loadTransactions()).thenReturn(List.of(
                transaction("T1", "S1", "10.50", now.minusSeconds(3600)),
                transaction("T2", "S1", "99.00", now.minusSeconds(60L * 60 * 48)),
                transaction("T3", "S2", "20.00", now.minusSeconds(1200))
        ));

        when(jsonRepository.loadIncidents()).thenReturn(List.of(
                incident("I1", "S1", "HIGH", now.minusSeconds(1800)),
                incident("I2", "S1", "LOW", now.minusSeconds(60L * 60 * 72)),
                incident("I3", "S2", "MEDIUM", now.minusSeconds(900))
        ));

        chatService = new ChatService(jsonRepository);
    }

    @Test
    void answerQuestion_throwsNotFoundWhenStoreMissing() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("How is the store?");
        request.setStoreId("UNKNOWN");

        assertThatThrownBy(() -> chatService.answerQuestion(request))
                .isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void answerQuestion_throwsNotFoundWhenInlineStoreIdMissing() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("show incidents for store 99999");

        assertThatThrownBy(() -> chatService.answerQuestion(request))
                .isInstanceOf(StoreNotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void answerQuestion_withoutStoreId_returnsPortfolioSummary() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("What is the overall status?");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("Across 2 stores");
        assertThat(response.getRetrievedContextSummary()).contains("Portfolio-wide");
        assertThat(response.getCitations())
                .extracting(c -> c.get("type"))
                .contains("store", "transaction", "incident");
    }

    @Test
    void answerQuestion_portfolioIncidentQuestion_returnsIncidentRecordCitations() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("Any incidents in last 24h?");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("incident(s) across all stores");
        assertThat(response.getCitations())
                .extracting(c -> c.get("type"))
                .contains("incident");

        Map<String, String> incidentCitation = response.getCitations().stream()
                .filter(c -> "incident".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(incidentCitation.get("storeId")).isNotBlank();
        assertThat(incidentCitation.get("incidentId")).isNotBlank();
        assertThat(incidentCitation.get("timestamp")).isNotBlank();
    }

    @Test
    void answerQuestion_portfolioRunoutRiskQuestion_returnsTankRiskSummaryAndCitations() {
        Store riskyStore = store("10001", "7-Eleven", "ONLINE", 1, "Austin", "TX");
        riskyStore.setTanks(List.of(
                tank("Regular", 10000, 1200, 8800),
                tank("Premium", 9000, 1800, 7200)
        ));

        Store healthyStore = store("10002", "Shell", "ONLINE", 0, "Dallas", "TX");
        healthyStore.setTanks(List.of(
                tank("Regular", 10000, 6200, 3800)
        ));

        when(jsonRepository.loadStores()).thenReturn(List.of(riskyStore, healthyStore));
        when(jsonRepository.loadTransactions()).thenReturn(List.of());
        when(jsonRepository.loadIncidents()).thenReturn(List.of());
        chatService = new ChatService(jsonRepository);

        ChatRequest request = new ChatRequest();
        request.setQuestion("Any stores with low tank levels that look like runout risk");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("runout risk");
        assertThat(response.getRetrievedContextSummary()).contains("tank-risk");
        assertThat(response.getCitations())
                .extracting(c -> c.get("type"))
                .contains("store", "tank");

        Map<String, String> tankCitation = response.getCitations().stream()
                .filter(c -> "tank".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(tankCitation.get("storeId")).isEqualTo("10001");
        assertThat(tankCitation.get("riskPct")).isNotBlank();
    }

    @Test
    void answerQuestion_keepsSelectedStoreWhenQuestionHasBareStoreNumber() {
        Store selectedStore = store("10001", "7-Eleven", "ONLINE", 1, "Austin", "TX");
        Store mentionedButNotExplicit = store("10002", "Shell", "OFFLINE", 4, "Dallas", "TX");

        when(jsonRepository.loadStores()).thenReturn(List.of(selectedStore, mentionedButNotExplicit));
        when(jsonRepository.loadTransactions()).thenReturn(List.of());
        when(jsonRepository.loadIncidents()).thenReturn(List.of());
        chatService = new ChatService(jsonRepository);

        ChatRequest request = new ChatRequest();
        request.setStoreId("10001");
        request.setQuestion("10002 offline");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("Store 10001");
        Map<String, String> storeCitation = response.getCitations().stream()
                .filter(c -> "store".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(storeCitation.get("storeId")).isEqualTo("10001");
    }

    @Test
    void answerQuestion_prefersExplicitInlineStoreReferenceOverSelectedStore() {
        Store selectedStore = store("10001", "7-Eleven", "ONLINE", 1, "Austin", "TX");
        Store explicitInlineStore = store("10002", "Shell", "OFFLINE", 4, "Dallas", "TX");

        when(jsonRepository.loadStores()).thenReturn(List.of(selectedStore, explicitInlineStore));
        when(jsonRepository.loadTransactions()).thenReturn(List.of());
        when(jsonRepository.loadIncidents()).thenReturn(List.of());
        chatService = new ChatService(jsonRepository);

        ChatRequest request = new ChatRequest();
        request.setStoreId("10001");
        request.setQuestion("store 10002 offline");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("Store 10002");
        Map<String, String> storeCitation = response.getCitations().stream()
                .filter(c -> "store".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(storeCitation.get("storeId")).isEqualTo("10002");
    }

    @Test
    void answerQuestion_includesOnlyLast24HourContextAndCitations() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("How is S1 doing?");
        request.setStoreId("S1");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("Store S1 (Austin, TX) is ONLINE with 2 offline pumps.");
        assertThat(response.getAnswer()).contains("In the last 24h: 1 transactions ($10.50), 1 incident(s).");

        assertThat(response.getCitations()).hasSize(3);
        assertThat(response.getCitations())
                .extracting(c -> c.get("type"))
                .containsExactlyInAnyOrder("store", "transaction", "incident");

        Map<String, String> storeCitation = response.getCitations().stream()
                .filter(c -> "store".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(storeCitation.get("storeId")).isEqualTo("S1");
    }

    @Test
    void answerQuestion_pumpQuestion_returnsEnrichedStoreAndIncidentCitations() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("How many pumps are offline?");
        request.setStoreId("S1");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("Store S1").contains("total pumps");
        assertThat(response.getCitations())
                .extracting(c -> c.get("type"))
                .contains("store", "incident");

        Map<String, String> storeCitation = response.getCitations().stream()
                .filter(c -> "store".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(storeCitation.get("offlinePumps")).isEqualTo("2");
        assertThat(storeCitation.get("activePumps")).isEqualTo("6");

        Map<String, String> incidentCitation = response.getCitations().stream()
                .filter(c -> "incident".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(incidentCitation.get("category")).isEqualTo("PUMP_FAULT");
        assertThat(incidentCitation.get("description")).isEqualTo("Pump 3 offline");
        assertThat(incidentCitation.get("status")).isEqualTo("OPEN");
    }

    @Test
    void answerQuestion_statusQuestion_returnsSupportingTransactionAndIncidentEvidence() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("What is the current status?");
        request.setStoreId("S1");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getRetrievedContextSummary()).contains("Evidence from 2 data points");
        assertThat(response.getCitations())
                .extracting(c -> c.get("type"))
                .contains("store", "transaction", "incident");

        Map<String, String> transactionCitation = response.getCitations().stream()
                .filter(c -> "transaction".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(transactionCitation.get("gradeName")).isEqualTo("Regular");
        assertThat(transactionCitation.get("volume")).isEqualTo("5.0 gal");
        assertThat(transactionCitation.get("dispenserId")).isEqualTo("3");
    }

    @Test
    void answerQuestion_tankQuestion_returnsPerTankCitations() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("Show tank levels");
        request.setStoreId("S1");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("2 fuel tank(s)");
        assertThat(response.getCitations()).hasSize(3);
        assertThat(response.getCitations())
                .extracting(c -> c.get("type"))
                .containsExactlyInAnyOrder("store", "tank", "tank");

        Map<String, String> tankCitation = response.getCitations().stream()
                .filter(c -> "tank".equals(c.get("type")))
                .findFirst()
                .orElseThrow();
        assertThat(tankCitation.get("gradeName")).isNotBlank();
        assertThat(tankCitation.get("capacityGallons")).isNotBlank();
        assertThat(tankCitation.get("levelGallons")).isNotBlank();
    }

    private static Store store(String id, String brand, String status, int offlinePumps, String city, String state) {
        StoreAddress address = new StoreAddress();
        address.setCity(city);
        address.setState(state);

        Store store = new Store();
        store.setStoreId(id);
        store.setBrand(brand);
        store.setStatus(status);
        store.setTotalPumps(8);
        store.setActivePumps(6);
        store.setOfflinePumps(offlinePumps);
        store.setHyperCare(true);
        store.setStoreAddress(address);
        store.setLastUpdatedTime(Instant.now());
        store.setTanks(List.of(
                tank("Regular", 10000, 4200, 5800),
                tank("Premium", 9000, 3100, 5900)
        ));
        return store;
    }

    private static Transaction transaction(String id, String storeId, String amount, Instant endTime) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(id);
        transaction.setStoreId(storeId);
        transaction.setTransactionAmnt(amount);
        transaction.setGradeName("Regular");
        transaction.setVolume("5.0");
        transaction.setDispenserId(3);
        transaction.setTransactionEndTime(endTime);
        transaction.setTransactionStartTime(endTime.minusSeconds(120));
        return transaction;
    }

    private static Incident incident(String id, String storeId, String severity, Instant timestamp) {
        Incident incident = new Incident();
        incident.setIncidentId(id);
        incident.setStoreId(storeId);
        incident.setSeverity(severity);
        incident.setCategory("PUMP_FAULT");
        incident.setDescription("Pump 3 offline");
        incident.setStatus("OPEN");
        incident.setTimestamp(timestamp);
        return incident;
    }

    private static Tank tank(String grade, int capacity, int level, int ullage) {
        Tank tank = new Tank();
        tank.setGradeName(grade);
        tank.setCapacityGallons(capacity);
        tank.setLevelGallons(level);
        tank.setUllageGallons(ullage);
        tank.setLastUpdatedTime(Instant.now());
        return tank;
    }
}
