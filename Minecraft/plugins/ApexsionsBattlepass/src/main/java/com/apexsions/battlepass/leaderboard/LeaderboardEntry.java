package com.apexsions.battlepass.leaderboard;

import java.util.UUID;

public class LeaderboardEntry {

    private final UUID uuid;
    private final String playerName;
    private final int level;
    private final int xp;
    private final int currency;
    private final boolean online;
    private final int rank;

    public LeaderboardEntry(UUID uuid, String playerName, int level, int xp, int currency, boolean online, int rank) {
        this.uuid = uuid;
        this.playerName = playerName != null ? playerName : "Unknown";
        this.level = level;
        this.xp = xp;
        this.currency = currency;
        this.online = online;
        this.rank = rank;
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getCurrency() { return currency; }
    public boolean isOnline() { return online; }
    public int getRank() { return rank; }
}
