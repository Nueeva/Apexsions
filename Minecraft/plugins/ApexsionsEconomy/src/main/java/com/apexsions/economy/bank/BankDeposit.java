package com.apexsions.economy.bank;

import java.util.UUID;

public class BankDeposit {
    private final String id;
    private final UUID uuid;
    private final String currencyId;
    private final double amount;
    private final double interestRate;
    private final double expectedReturn;
    private final long createdAt;
    private final long maturesAt;
    private boolean claimed;

    public BankDeposit(String id, UUID uuid, String currencyId, double amount, double interestRate, double expectedReturn, long createdAt, long maturesAt, boolean claimed) {
        this.id = id;
        this.uuid = uuid;
        this.currencyId = currencyId;
        this.amount = amount;
        this.interestRate = interestRate;
        this.expectedReturn = expectedReturn;
        this.createdAt = createdAt;
        this.maturesAt = maturesAt;
        this.claimed = claimed;
    }

    public String getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getCurrencyId() { return currencyId; }
    public double getAmount() { return amount; }
    public double getInterestRate() { return interestRate; }
    public double getExpectedReturn() { return expectedReturn; }
    public long getCreatedAt() { return createdAt; }
    public long getMaturesAt() { return maturesAt; }
    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public boolean isMatured() {
        return System.currentTimeMillis() >= maturesAt;
    }

    public String getTimeRemainingFormatted() {
        long diff = maturesAt - System.currentTimeMillis();
        if (diff <= 0) return "Siap Diklaim!";
        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
        return hours + "j " + minutes + "m";
    }
}
