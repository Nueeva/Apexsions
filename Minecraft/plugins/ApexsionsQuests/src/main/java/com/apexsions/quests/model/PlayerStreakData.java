package com.apexsions.quests.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerStreakData {
    private final UUID uuid;
    private int currentStreak;
    private int highestStreak;
    private String lastLoginDate;
    private String lastClaimDate;
    private long monthlyClaimedMask;
    private final Set<Integer> claimedMilestones;
    private int streakFreezeCount;
    private int rerollsUsedToday;
    private String lastRerollDate;

    public PlayerStreakData(UUID uuid, int currentStreak, int highestStreak, String lastLoginDate,
                            String lastClaimDate, long monthlyClaimedMask, Set<Integer> claimedMilestones,
                            int streakFreezeCount, int rerollsUsedToday, String lastRerollDate) {
        this.uuid = uuid;
        this.currentStreak = currentStreak;
        this.highestStreak = highestStreak;
        this.lastLoginDate = lastLoginDate != null ? lastLoginDate : "";
        this.lastClaimDate = lastClaimDate != null ? lastClaimDate : "";
        this.monthlyClaimedMask = monthlyClaimedMask;
        this.claimedMilestones = claimedMilestones != null ? claimedMilestones : new HashSet<>();
        this.streakFreezeCount = streakFreezeCount;
        this.rerollsUsedToday = rerollsUsedToday;
        this.lastRerollDate = lastRerollDate != null ? lastRerollDate : "";
    }

    public UUID getUuid() { return uuid; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
        if (currentStreak > this.highestStreak) {
            this.highestStreak = currentStreak;
        }
    }

    public int getHighestStreak() { return highestStreak; }
    public void setHighestStreak(int highestStreak) { this.highestStreak = highestStreak; }

    public String getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public String getLastClaimDate() { return lastClaimDate; }
    public void setLastClaimDate(String lastClaimDate) { this.lastClaimDate = lastClaimDate; }

    public long getMonthlyClaimedMask() { return monthlyClaimedMask; }
    public void setMonthlyClaimedMask(long monthlyClaimedMask) { this.monthlyClaimedMask = monthlyClaimedMask; }

    public boolean isDayClaimed(int day) {
        if (day < 1 || day > 31) return false;
        return (monthlyClaimedMask & (1L << (day - 1))) != 0;
    }

    public void setDayClaimed(int day) {
        if (day >= 1 && day <= 31) {
            this.monthlyClaimedMask |= (1L << (day - 1));
        }
    }

    public Set<Integer> getClaimedMilestones() { return claimedMilestones; }
    public boolean isMilestoneClaimed(int days) { return claimedMilestones.contains(days); }
    public void addClaimedMilestone(int days) { claimedMilestones.add(days); }

    public int getStreakFreezeCount() { return streakFreezeCount; }
    public void setStreakFreezeCount(int streakFreezeCount) { this.streakFreezeCount = streakFreezeCount; }
    public void addStreakFreeze(int amount) { this.streakFreezeCount += amount; }
    public boolean useStreakFreeze() {
        if (this.streakFreezeCount > 0) {
            this.streakFreezeCount--;
            return true;
        }
        return false;
    }

    public int getRerollsUsedToday() { return rerollsUsedToday; }
    public void setRerollsUsedToday(int rerollsUsedToday) { this.rerollsUsedToday = rerollsUsedToday; }

    public String getLastRerollDate() { return lastRerollDate; }
    public void setLastRerollDate(String lastRerollDate) { this.lastRerollDate = lastRerollDate; }
}
