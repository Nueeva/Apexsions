package com.apex.battlepass.pass;

import org.bukkit.Material;
import java.util.List;

public class PassTier {

    private final String id;
    private final String displayName;
    private final String permission;
    private final boolean defaultOwned;
    private final Material icon;
    private final List<String> lore;
    private final int priority;
    private final List<String> rewardAccess;

    public PassTier(String id, String displayName, String permission, boolean defaultOwned, Material icon, List<String> lore, int priority, List<String> rewardAccess) {
        this.id = id.toLowerCase();
        this.displayName = displayName != null ? displayName : id;
        this.permission = permission != null ? permission : "apexsionsbattlepass.pass." + this.id;
        this.defaultOwned = defaultOwned;
        this.icon = icon != null ? icon : Material.PAPER;
        this.lore = lore != null ? lore : List.of();
        this.priority = priority;
        this.rewardAccess = rewardAccess != null && !rewardAccess.isEmpty() ? rewardAccess : List.of(this.id);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isDefaultOwned() {
        return defaultOwned;
    }

    public Material getIcon() {
        return icon;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getRewardAccess() {
        return rewardAccess;
    }
}
