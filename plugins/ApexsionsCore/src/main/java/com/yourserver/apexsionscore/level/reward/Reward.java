package com.yourserver.apexsionscore.level.reward;

import java.util.Collections;
import java.util.List;

/**
 * Domain model representing a level reward (regular or special milestone reward).
 */
public class Reward {

    private final int level;
    private final boolean milestone;
    private final String displayName;
    private final String icon;
    private final List<String> lore;
    private final List<String> commands;
    private final String broadcast;
    private final String sound;

    public Reward(int level, boolean milestone, String displayName, String icon,
                  List<String> lore, List<String> commands, String broadcast, String sound) {
        this.level = level;
        this.milestone = milestone;
        this.displayName = displayName != null ? displayName : ("<yellow>Level " + level + " Reward</yellow>");
        this.icon = icon != null ? icon : (milestone ? "ENDER_CHEST" : "GOLD_NUGGET");
        this.lore = lore != null ? Collections.unmodifiableList(lore) : Collections.emptyList();
        this.commands = commands != null ? Collections.unmodifiableList(commands) : Collections.emptyList();
        this.broadcast = broadcast;
        this.sound = sound != null ? sound : (milestone ? "UI_TOAST_CHALLENGE_COMPLETE" : "ENTITY_PLAYER_LEVELUP");
    }

    public int getLevel() {
        return level;
    }

    public boolean isMilestone() {
        return milestone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public String getSound() {
        return sound;
    }
}
