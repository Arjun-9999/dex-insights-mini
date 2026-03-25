package com.dex.dex_insights_mini.service;

import com.dex.dex_insights_mini.model.Incident;
import com.dex.dex_insights_mini.model.Store;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    @Mock
    private JsonRepository jsonRepository;

    private InsightsService insightsService;

    @BeforeEach
    void setUp() {
        when(jsonRepository.loadStores()).thenReturn(List.of(
                store("S1", 9, tank(1000, 150)),
                store("S2", 2, tank(1000, 700)),
                store("S3", 5, tank(1000, 800))
        ));

        when(jsonRepository.loadTransactions()).thenReturn(List.of(
                transaction("S1", "10.0", "25.5"),
                transaction("S1", "12.5", "30.0"),
                transaction("S2", "20.0", "50.0")
        ));

        when(jsonRepository.loadIncidents()).thenReturn(List.of(
                incident("HIGH"),
                incident("HIGH"),
                incident("LOW")
        ));

        insightsService = new InsightsService(jsonRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateOverview_returnsSortedStoresAndAggregates() {
        Map<String, Object> overview = insightsService.generateOverview();

        List<Store> topOfflineStores = (List<Store>) overview.get("topOfflineStores");
        assertThat(topOfflineStores).extracting(Store::getStoreId).containsExactly("S1", "S3", "S2");

        List<Store> lowTankLevelStores = (List<Store>) overview.get("lowTankLevelStores");
        assertThat(lowTankLevelStores).extracting(Store::getStoreId).containsExactly("S1");

        Map<String, Long> incidentCounts = (Map<String, Long>) overview.get("incidentCountsBySeverity");
        assertThat(incidentCounts).containsEntry("HIGH", 2L).containsEntry("LOW", 1L);

        Map<String, Double> volumeByStore = (Map<String, Double>) overview.get("transactionVolumeByStore");
        assertThat(volumeByStore.get("S1")).isEqualTo(22.5);
        assertThat(volumeByStore.get("S2")).isEqualTo(20.0);

        Map<String, Double> revenueByStore = (Map<String, Double>) overview.get("revenueByStore");
        assertThat(revenueByStore.get("S1")).isEqualTo(55.5);
        assertThat(revenueByStore.get("S2")).isEqualTo(50.0);
    }

    private static Store store(String id, int offlinePumps, Tank tank) {
        Store store = new Store();
        store.setStoreId(id);
        store.setOfflinePumps(offlinePumps);
        store.setTanks(List.of(tank));
        return store;
    }

    private static Tank tank(int capacity, int level) {
        Tank tank = new Tank();
        tank.setCapacityGallons(capacity);
        tank.setLevelGallons(level);
        tank.setLastUpdatedTime(Instant.now());
        return tank;
    }

    private static Transaction transaction(String storeId, String volume, String amount) {
        Transaction transaction = new Transaction();
        transaction.setStoreId(storeId);
        transaction.setVolume(volume);
        transaction.setTransactionAmnt(amount);
        return transaction;
    }

    private static Incident incident(String severity) {
        Incident incident = new Incident();
        incident.setSeverity(severity);
        incident.setIncidentId(severity + "-1");
        return incident;
    }
}
