package com.dex.dex_insights_mini.exception;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(String storeId) {
        super("Store not found: " + storeId);
    }
}

