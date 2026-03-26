package com.dex.dex_insights_mini.service;

import com.dex.dex_insights_mini.exception.StoreNotFoundException;
import com.dex.dex_insights_mini.repository.JsonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.dex.dex_insights_mini.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class StoreService {
    private static final Logger log = LoggerFactory.getLogger(StoreService.class);
    private final List<Store> stores;

    public StoreService(JsonRepository repo) {
        this.stores = repo.loadStores();
    }

    public Page<Store> getStores(String brand, String status, boolean sortByOfflinePumps, Pageable pageable) {
        log.info("event=load_stores brand={} status={} sortByOfflinePumps={} page={} size={}",
                brand, status, sortByOfflinePumps, pageable.getPageNumber(), pageable.getPageSize());

        String brandFilter = brand == null ? null : brand.trim();
        String statusFilter = status == null ? null : status.trim();

        Stream<Store> stream = stores.stream();
        if (brandFilter != null && !brandFilter.isBlank()) {
            stream = stream.filter(s -> s.getBrand() != null && s.getBrand().equalsIgnoreCase(brandFilter));
        }
        if (statusFilter != null && !statusFilter.isBlank()) {
            stream = stream.filter(s -> s.getStatus() != null && s.getStatus().equalsIgnoreCase(statusFilter));
        }
        if (sortByOfflinePumps) stream = stream.sorted(Comparator.comparingInt(Store::getOfflinePumps).reversed());
        List<Store> filtered = stream.toList();
        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Store> pageContent = filtered.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    public Store getStoreById(String storeId) {
        return stores.stream()
                .filter(s -> s.getStoreId().equals(storeId))
                .findFirst()
                .orElseThrow(() -> new StoreNotFoundException(storeId));
    }
}