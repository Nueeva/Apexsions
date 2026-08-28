package com.apex.economy.leaderboard;

import java.util.UUID;

public class EconomyLeaderboardEntry {

    private final int rank;
    private final UUID uuid;
    private final String currencyId;
    private final double balance;

    public EconomyLeaderboardEntry(int rank, UUID uuid, String currencyId, double balance) {
        this.rank = rank;
        this.uuid = uuid;
        this.currencyId = currencyId;
        this.balance = balance;
    }

    public int getRank() { return rank; }
    public UUID getUuid() { return uuid; }
    public String getCurrencyId() { return currencyId; }
    public double getBalance() { return balance; }
}
