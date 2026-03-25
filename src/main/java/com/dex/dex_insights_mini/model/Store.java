package com.dex.dex_insights_mini.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class Store {
    @JsonProperty("STOREID")
    private String storeId;
    
    @JsonProperty("BRAND")
    private String brand;
    
    private String status;
    private int totalPumps;
    private int activePumps;
    private int offlinePumps;
    private boolean hyperCare;
    private Instant lastUpdatedTime;
    private StoreAddress storeAddress;
    private String latitude;
    private String longitude;
    private int anomalyCount;
    private List<Tank> tanks;

    // Default constructor
    public Store() {
    }

    // All args constructor
    public Store(String storeId, String brand, String status, int totalPumps, int activePumps,
                 int offlinePumps, boolean hyperCare, Instant lastUpdatedTime, 
                 StoreAddress storeAddress, String latitude, String longitude, 
                 int anomalyCount, List<Tank> tanks) {
        this.storeId = storeId;
        this.brand = brand;
        this.status = status;
        this.totalPumps = totalPumps;
        this.activePumps = activePumps;
        this.offlinePumps = offlinePumps;
        this.hyperCare = hyperCare;
        this.lastUpdatedTime = lastUpdatedTime;
        this.storeAddress = storeAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.anomalyCount = anomalyCount;
        this.tanks = tanks;
    }

    // Getters and setters
    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalPumps() {
        return totalPumps;
    }

    public void setTotalPumps(int totalPumps) {
        this.totalPumps = totalPumps;
    }

    public int getActivePumps() {
        return activePumps;
    }

    public void setActivePumps(int activePumps) {
        this.activePumps = activePumps;
    }

    public int getOfflinePumps() {
        return offlinePumps;
    }

    public void setOfflinePumps(int offlinePumps) {
        this.offlinePumps = offlinePumps;
    }

    public boolean isHyperCare() {
        return hyperCare;
    }

    public void setHyperCare(boolean hyperCare) {
        this.hyperCare = hyperCare;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Instant lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public StoreAddress getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(StoreAddress storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(int anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public List<Tank> getTanks() {
        return tanks;
    }

    public void setTanks(List<Tank> tanks) {
        this.tanks = tanks;
    }
}
