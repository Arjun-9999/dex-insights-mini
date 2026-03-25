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
        Stream<Store> stream = stores.stream();
        if (brand != null) stream = stream.filter(s -> s.getBrand().equalsIgnoreCase(brand));
        if (status != null) stream = stream.filter(s -> s.getStatus().equalsIgnoreCase(status));
        if (sortByOfflinePumps) stream = stream.sorted(Comparator.comparingInt(Store::getOfflinePumps).reversed());
        List<Store> filtered = stream.toList();
        int start = (int) pageable.getOffset();
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