package com.apexsions.crates.crate;

import com.apexsions.crates.milestone.Milestone;
import com.apexsions.crates.reward.Reward;
import com.apexsions.crates.reward.RewardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

import java.util.*;

public class Crate {

    private final String id;
    private final String name;
    private final Material blockMaterial;
    private final CrateAnimationType animationType;
    private final String requiredKeyId;
    private final Map<String, Reward> rewards;
    private final Map<Integer, Milestone> milestones;
    private final boolean hologramEnabled;
    private final List<String> hologramLines;
    private final int cooldownSeconds;

    public Crate(String id, String name, Material blockMaterial, CrateAnimationType animationType,
                 String requiredKeyId, Map<String, Reward> rewards, Map<Integer, Milestone> milestones,
                 boolean hologramEnabled, List<String> hologramLines, int cooldownSeconds) {
        this.id = id;
        this.name = name != null ? name : "Peti " + id;
        this.blockMaterial = blockMaterial != null ? blockMaterial : Material.CHEST;
        this.animationType = animationType != null ? animationType : CrateAnimationType.ROULETTE;
        this.requiredKeyId = requiredKeyId;
        this.rewards = rewards != null ? rewards : new LinkedHashMap<>();
        this.milestones = milestones != null ? milestones : new TreeMap<>();
        this.hologramEnabled = hologramEnabled;
        this.hologramLines = hologramLines != null ? hologramLines : new ArrayList<>();
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(name);
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public CrateAnimationType getAnimationType() {
        return animationType;
    }

    public String getRequiredKeyId() {
        return requiredKeyId;
    }

    public Collection<Reward> getRewards() {
        return Collections.unmodifiableCollection(rewards.values());
    }

    public Reward getReward(String rewardId) {
        if (rewardId == null) return null;
        return rewards.get(rewardId.toLowerCase());
    }

    public Map<Integer, Milestone> getMilestones() {
        return Collections.unmodifiableMap(milestones);
    }

    public Milestone getMilestone(int openCount) {
        return milestones.get(openCount);
    }

    public boolean isHologramEnabled() {
        return hologramEnabled;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public Reward rollReward() {
        return RewardManager.rollReward(rewards.values());
    }

    public double getRewardChance(Reward reward) {
        return RewardManager.calculateChance(reward, rewards.values());
    }
}
