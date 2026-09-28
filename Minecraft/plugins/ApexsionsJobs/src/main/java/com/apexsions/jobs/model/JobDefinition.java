package com.apexsions.jobs.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobDefinition {
    private final String id;
    private final JobType type;
    private final String name;
    private final String title;
    private final String description;
    private final String iconMaterial;
    private final int maxLevel;
    private final double basePay;
    private final double baseExp;
    private final Map<String, JobRewardItem> rewards = new HashMap<>();

    public JobDefinition(String id, JobType type, String name, String title, String description,
                         String iconMaterial, int maxLevel, double basePay, double baseExp,
                         List<JobRewardItem> rewardItems) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.title = title;
        this.description = description;
        this.iconMaterial = iconMaterial;
        this.maxLevel = maxLevel;
        this.basePay = basePay;
        this.baseExp = baseExp;
        if (rewardItems != null) {
            for (JobRewardItem r : rewardItems) {
                this.rewards.put(r.getTarget().toUpperCase(), r);
            }
        }
    }

    public String getId() { return id; }
    public JobType getType() { return type; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIconMaterial() { return iconMaterial; }
    public int getMaxLevel() { return maxLevel; }
    public double getBasePay() { return basePay; }
    public double getBaseExp() { return baseExp; }

    public JobRewardItem getReward(String target) {
        if (target == null) return null;
        return rewards.get(target.toUpperCase());
    }

    public Map<String, JobRewardItem> getRewards() {
        return rewards;
    }

    public boolean hasReward(String target) {
        if (target == null) return false;
        return rewards.containsKey(target.toUpperCase());
    }
}
