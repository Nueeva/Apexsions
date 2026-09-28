package com.apexsions.jobs.model;

import java.time.LocalDate;
import java.util.UUID;

public class PlayerJobData {
    private final UUID uuid;
    private final String jobId;
    private int level;
    private double exp;
    private double dailyEarnings;
    private String lastEarningDate;
    private boolean active;

    public PlayerJobData(UUID uuid, String jobId, int level, double exp,
                         double dailyEarnings, String lastEarningDate, boolean active) {
        this.uuid = uuid;
        this.jobId = jobId;
        this.level = Math.max(1, level);
        this.exp = exp;
        this.dailyEarnings = dailyEarnings;
        this.lastEarningDate = lastEarningDate != null ? lastEarningDate : LocalDate.now().toString();
        this.active = active;
    }

    public UUID getUuid() { return uuid; }
    public String getJobId() { return jobId; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public double getExp() { return exp; }
    public void setExp(double exp) { this.exp = exp; }
    public double getDailyEarnings() { return dailyEarnings; }
    public void setDailyEarnings(double dailyEarnings) { this.dailyEarnings = dailyEarnings; }
    public String getLastEarningDate() { return lastEarningDate; }
    public void setLastEarningDate(String lastEarningDate) { this.lastEarningDate = lastEarningDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getRequiredExp() {
        return 100.0 * level * 1.25;
    }

    public boolean checkLevelUp(int maxLevel) {
        boolean leveledUp = false;
        double req = getRequiredExp();
        while (this.exp >= req && this.level < maxLevel) {
            this.exp -= req;
            this.level++;
            leveledUp = true;
            req = getRequiredExp();
        }
        return leveledUp;
    }

    public void addDailyEarnings(double amount) {
        this.dailyEarnings += amount;
    }

    public void resetDailyIfNeeded() {
        String today = LocalDate.now().toString();
        if (!today.equals(this.lastEarningDate)) {
            this.dailyEarnings = 0.0;
            this.lastEarningDate = today;
        }
    }
}
