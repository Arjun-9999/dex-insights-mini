package com.dex.dex_insights_mini.repository;
import com.dex.dex_insights_mini.exception.DataLoadException;
import com.dex.dex_insights_mini.model.*;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;
import java.util.List;

@Repository
public class JsonRepository {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public List<Store> loadStores() {
        return loadJson("data/stores.json", new TypeReference<List<Store>>() {});
    }
    public List<Transaction> loadTransactions() {
        return loadJson("data/transactions.json", new TypeReference<List<Transaction>>() {});
    }
    public List<Incident> loadIncidents() {
        return loadJson("data/incidents.json", new TypeReference<List<Incident>>() {});
    }

    private <T> List<T> loadJson(String path, TypeReference<List<T>> type) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new DataLoadException("Resource not found: " + path);
            }
            return mapper.readValue(is, type);
        } catch (Exception e) {
            if (e instanceof DataLoadException dataLoadException) {
                throw dataLoadException;
            }
            throw new DataLoadException("Failed to load " + path, e);
        }
    }
}