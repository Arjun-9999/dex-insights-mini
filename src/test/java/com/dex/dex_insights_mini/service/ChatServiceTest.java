package com.dex.dex_insights_mini.service;

import com.dex.dex_insights_mini.dto.ChatRequest;
import com.dex.dex_insights_mini.dto.ChatResponse;
import com.dex.dex_insights_mini.exception.InvalidRequestException;
import com.dex.dex_insights_mini.exception.StoreNotFoundException;
import com.dex.dex_insights_mini.model.Incident;
import com.dex.dex_insights_mini.model.Store;
import com.dex.dex_insights_mini.model.StoreAddress;
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
    void answerQuestion_throwsBadRequestWhenStoreIdMissing() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("How is the store?");

        assertThatThrownBy(() -> chatService.answerQuestion(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("storeId is required");
    }

    @Test
    void answerQuestion_includesOnlyLast24HourContextAndCitations() {
        ChatRequest request = new ChatRequest();
        request.setQuestion("How is S1 doing?");
        request.setStoreId("S1");

        ChatResponse response = chatService.answerQuestion(request);

        assertThat(response.getAnswer()).contains("Store S1 (Austin, TX) is ONLINE with 2 offline pumps.");
        assertThat(response.getAnswer()).contains("In the last 24h, 1 transactions totaling $10.50.");
        assertThat(response.getAnswer()).contains("Recent incidents: 1.");

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

    private static Store store(String id, String brand, String status, int offlinePumps, String city, String state) {
        StoreAddress address = new StoreAddress();
        address.setCity(city);
        address.setState(state);

        Store store = new Store();
        store.setStoreId(id);
        store.setBrand(brand);
        store.setStatus(status);
        store.setOfflinePumps(offlinePumps);
        store.setStoreAddress(address);
        store.setLastUpdatedTime(Instant.now());
        return store;
    }

    private static Transaction transaction(String id, String storeId, String amount, Instant endTime) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(id);
        transaction.setStoreId(storeId);
        transaction.setTransactionAmnt(amount);
        transaction.setVolume("5.0");
        transaction.setTransactionEndTime(endTime);
        transaction.setTransactionStartTime(endTime.minusSeconds(120));
        return transaction;
    }

    private static Incident incident(String id, String storeId, String severity, Instant timestamp) {
        Incident incident = new Incident();
        incident.setIncidentId(id);
        incident.setStoreId(storeId);
        incident.setSeverity(severity);
        incident.setTimestamp(timestamp);
        return incident;
    }
}
