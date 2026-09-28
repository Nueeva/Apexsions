package com.apexsions.quests.model;

import java.util.UUID;

public class PlayerQuestProgress {
    private final String id;
    private final UUID playerUuid;
    private final String questId;
    private final String questDate;
    private int currentProgress;
    private final int targetProgress;
    private boolean completed;
    private boolean claimed;

    public PlayerQuestProgress(String id, UUID playerUuid, String questId, String questDate,
                               int currentProgress, int targetProgress, boolean completed, boolean claimed) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.questId = questId;
        this.questDate = questDate;
        this.currentProgress = currentProgress;
        this.targetProgress = targetProgress;
        this.completed = completed;
        this.claimed = claimed;
    }

    public String getId() { return id; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getQuestId() { return questId; }
    public String getQuestDate() { return questDate; }
    public int getCurrentProgress() { return currentProgress; }
    public int getTargetProgress() { return targetProgress; }
    public boolean isCompleted() { return completed; }
    public boolean isClaimed() { return claimed; }

    public void addProgress(int amount) {
        this.currentProgress = Math.min(targetProgress, this.currentProgress + amount);
        if (this.currentProgress >= targetProgress) {
            this.completed = true;
        }
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }
}
