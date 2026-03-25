package com.dex.dex_insights_mini.service;
import com.dex.dex_insights_mini.model.*;
import com.dex.dex_insights_mini.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InsightsService {
    private final List<Store> stores;
    private final List<Transaction> transactions;
    private final List<Incident> incidents;

    public InsightsService(JsonRepository repo) {
        this.stores = repo.loadStores();
        this.transactions = repo.loadTransactions();
        this.incidents = repo.loadIncidents();
    }

    public Map<String, Object> generateOverview() {
        Map<String, Object> result = new HashMap<>();

        // Top offline stores
        List<Store> topOffline = stores.stream()
                .sorted(Comparator.comparingInt(Store::getOfflinePumps).reversed())
                .limit(3).toList();
        result.put("topOfflineStores", topOffline);

        // Stores with low tank levels (<20%)
        List<Store> lowTanks = stores.stream()
                .filter(s -> s.getTanks().stream()
                        .anyMatch(t -> (double) t.getLevelGallons() / t.getCapacityGallons() < 0.2))
                .toList();
        result.put("lowTankLevelStores", lowTanks);

        // Incident counts by severity
        Map<String, Long> incidentCounts = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getSeverity, Collectors.counting()));
        result.put("incidentCountsBySeverity", incidentCounts);

        // Transaction volume by store
        Map<String, Double> volumeByStore = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getStoreId,
                        Collectors.summingDouble(t -> Double.parseDouble(t.getVolume()))));
        result.put("transactionVolumeByStore", volumeByStore);

        // Revenue by store
        Map<String, Double> revenueByStore = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getStoreId,
                        Collectors.summingDouble(t -> Double.parseDouble(t.getTransactionAmnt()))));
        result.put("revenueByStore", revenueByStore);

        return result;
    }
}