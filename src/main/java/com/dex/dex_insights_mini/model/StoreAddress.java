package com.dex.dex_insights_mini.model;

public class StoreAddress {
    private String state;
    private String city;

    // Default constructor
    public StoreAddress() {
    }

    // All args constructor
    public StoreAddress(String state, String city) {
        this.state = state;
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
