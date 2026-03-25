package com.dex.dex_insights_mini.model;

import java.time.Instant;

public class Incident {
    private String incidentId;
    private String storeId;
    private Instant timestamp;
    private String severity;
    private String category;
    private String description;
    private String status;

    // Default constructor
    public Incident() {
    }

    // All args constructor
    public Incident(String incidentId, String storeId, Instant timestamp, String severity,
                   String category, String description, String status) {
        this.incidentId = incidentId;
        this.storeId = storeId;
        this.timestamp = timestamp;
        this.severity = severity;
        this.category = category;
        this.description = description;
        this.status = status;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}