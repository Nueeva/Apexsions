package com.apexsions.fishing.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerFishingStats {

    private final UUID uuid;
    private long totalFishCaught;
    private double totalWeightCaught;
    private double heaviestFishWeight;
    private String heaviestFishName;
    private int secretCatches;
    private final Map<String, Double> personalBestPerSpecies = new HashMap<>();
    private int virtualBait = 0;

    public PlayerFishingStats(UUID uuid) {
        this.uuid = uuid;
        this.totalFishCaught = 0;
        this.totalWeightCaught = 0.0;
        this.heaviestFishWeight = 0.0;
        this.heaviestFishName = "-";
        this.secretCatches = 0;
        this.virtualBait = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getPlayerUuid() {
        return uuid;
    }

    public long getTotalFishCaught() {
        return totalFishCaught;
    }

    public void setTotalFishCaught(long totalFishCaught) {
        this.totalFishCaught = totalFishCaught;
    }

    public double getTotalWeightCaught() {
        return totalWeightCaught;
    }

    public void setTotalWeightCaught(double totalWeightCaught) {
        this.totalWeightCaught = totalWeightCaught;
    }

    public double getHeaviestFishWeight() {
        return heaviestFishWeight;
    }

    public void setHeaviestFishWeight(double heaviestFishWeight) {
        this.heaviestFishWeight = heaviestFishWeight;
    }

    public String getHeaviestFishName() {
        return heaviestFishName != null ? heaviestFishName : "-";
    }

    public void setHeaviestFishName(String heaviestFishName) {
        this.heaviestFishName = heaviestFishName;
    }

    public int getSecretCatches() {
        return secretCatches;
    }

    public void setSecretCatches(int secretCatches) {
        this.secretCatches = secretCatches;
    }

    public Map<String, Double> getPersonalBestPerSpecies() {
        return personalBestPerSpecies;
    }

    public void recordCatch(String speciesId, String speciesName, double weight, boolean isSecret) {
        totalFishCaught++;
        totalWeightCaught += weight;
        if (isSecret) {
            secretCatches++;
        }

        if (weight > heaviestFishWeight) {
            heaviestFishWeight = weight;
            heaviestFishName = speciesName;
        }

        double curPb = personalBestPerSpecies.getOrDefault(speciesId.toLowerCase(), 0.0);
        if (weight > curPb) {
            personalBestPerSpecies.put(speciesId.toLowerCase(), weight);
        }
    }

    public boolean hasDiscoveredSpecies(String speciesId) {
        return personalBestPerSpecies.containsKey(speciesId.toLowerCase());
    }

    public double getPersonalBest(String speciesId) {
        return personalBestPerSpecies.getOrDefault(speciesId.toLowerCase(), 0.0);
    }

    public int getVirtualBait() {
        return virtualBait;
    }

    public void setVirtualBait(int virtualBait) {
        this.virtualBait = Math.max(0, virtualBait);
    }

    public void addVirtualBait(int amount) {
        this.virtualBait = Math.max(0, this.virtualBait + amount);
    }

    public boolean consumeVirtualBait() {
        if (this.virtualBait > 0) {
            this.virtualBait--;
            return true;
        }
        return false;
    }
}
