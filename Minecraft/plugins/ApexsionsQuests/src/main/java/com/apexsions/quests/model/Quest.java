package com.apexsions.quests.model;

import java.util.List;

public class Quest {
    private final String id;
    private final QuestObjectiveType type;
    private final List<String> targets;
    private final int targetAmount;
    private final String title;
    private final String description;
    private final double rewardMoney;
    private final int rewardCoreXp;
    private final int rewardPassXp;
    private final String rewardCommand;

    public Quest(String id, QuestObjectiveType type, List<String> targets, int targetAmount,
                 String title, String description, double rewardMoney, int rewardCoreXp,
                 int rewardPassXp, String rewardCommand) {
        this.id = id;
        this.type = type;
        this.targets = targets;
        this.targetAmount = targetAmount;
        this.title = title;
        this.description = description;
        this.rewardMoney = rewardMoney;
        this.rewardCoreXp = rewardCoreXp;
        this.rewardPassXp = rewardPassXp;
        this.rewardCommand = rewardCommand;
    }

    public String getId() { return id; }
    public QuestObjectiveType getType() { return type; }
    public List<String> getTargets() { return targets; }
    public int getTargetAmount() { return targetAmount; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getRewardMoney() { return rewardMoney; }
    public int getRewardCoreXp() { return rewardCoreXp; }
    public int getRewardPassXp() { return rewardPassXp; }
    public String getRewardCommand() { return rewardCommand; }

    public boolean matchesTarget(String name) {
        if (targets == null || targets.isEmpty()) return true;
        if (targets.contains("ANY") || targets.contains("*")) return true;
        for (String t : targets) {
            if (t.equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}
