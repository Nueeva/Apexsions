package com.apex.battlepass.quest.model;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Quest {

    private final String id;
    private final String name;
    private final String description;
    private final QuestCategory category;
    private final QuestObjectiveType type;
    private final int periodIndex; // Week number (1..N) or Month number (1..N), 0 for daily
    private final EntityType targetEntity;
    private final Material targetBlock;
    private final Material targetItem;
    private final int targetAmount;
    private final int rewardXp;
    private final int rewardCoins;

    public Quest(String id, String name, String description, QuestCategory category, QuestObjectiveType type,
                 int periodIndex, EntityType targetEntity, Material targetBlock, Material targetItem,
                 int targetAmount, int rewardXp, int rewardCoins) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.type = type;
        this.periodIndex = periodIndex;
        this.targetEntity = targetEntity;
        this.targetBlock = targetBlock;
        this.targetItem = targetItem;
        this.targetAmount = targetAmount > 0 ? targetAmount : 1;
        this.rewardXp = rewardXp;
        this.rewardCoins = rewardCoins;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public QuestObjectiveType getType() {
        return type;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public EntityType getTargetEntity() {
        return targetEntity;
    }

    public Material getTargetBlock() {
        return targetBlock;
    }

    public Material getTargetItem() {
        return targetItem;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getRewardXp() {
        return rewardXp;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }
}
