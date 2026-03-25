package com.dex.dex_insights_mini.model;

import java.time.Instant;

public class Tank {
    private String gradeName;
    private int capacityGallons;
    private int levelGallons;
    private int ullageGallons;
    private Instant lastUpdatedTime;

    // Default constructor
    public Tank() {
    }

    // All args constructor
    public Tank(String gradeName, int capacityGallons, int levelGallons,
                int ullageGallons, Instant lastUpdatedTime) {
        this.gradeName = gradeName;
        this.capacityGallons = capacityGallons;
        this.levelGallons = levelGallons;
        this.ullageGallons = ullageGallons;
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public int getCapacityGallons() {
        return capacityGallons;
    }

    public void setCapacityGallons(int capacityGallons) {
        this.capacityGallons = capacityGallons;
    }

    public int getLevelGallons() {
        return levelGallons;
    }

    public void setLevelGallons(int levelGallons) {
        this.levelGallons = levelGallons;
    }

    public int getUllageGallons() {
        return ullageGallons;
    }

    public void setUllageGallons(int ullageGallons) {
        this.ullageGallons = ullageGallons;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Instant lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
