package com.dex.dex_insights_mini.model;

import java.time.Instant;

public class Transaction {
    private String transactionId;
    private String storeId;
    private String gradeName;
    private String transactionAmnt;
    private String volume;
    private int dispenserId;
    private Instant transactionStartTime;
    private Instant transactionEndTime;

    // Default constructor
    public Transaction() {
    }

    // All args constructor
    public Transaction(String transactionId, String storeId, String gradeName,
                      String transactionAmnt, String volume, int dispenserId,
                      Instant transactionStartTime, Instant transactionEndTime) {
        this.transactionId = transactionId;
        this.storeId = storeId;
        this.gradeName = gradeName;
        this.transactionAmnt = transactionAmnt;
        this.volume = volume;
        this.dispenserId = dispenserId;
        this.transactionStartTime = transactionStartTime;
        this.transactionEndTime = transactionEndTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getTransactionAmnt() {
        return transactionAmnt;
    }

    public void setTransactionAmnt(String transactionAmnt) {
        this.transactionAmnt = transactionAmnt;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public int getDispenserId() {
        return dispenserId;
    }

    public void setDispenserId(int dispenserId) {
        this.dispenserId = dispenserId;
    }

    public Instant getTransactionStartTime() {
        return transactionStartTime;
    }

    public void setTransactionStartTime(Instant transactionStartTime) {
        this.transactionStartTime = transactionStartTime;
    }

    public Instant getTransactionEndTime() {
        return transactionEndTime;
    }

    public void setTransactionEndTime(Instant transactionEndTime) {
        this.transactionEndTime = transactionEndTime;
    }
}
