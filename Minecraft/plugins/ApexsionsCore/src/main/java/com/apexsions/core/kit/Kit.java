package com.apexsions.core.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a server kit in ApexsionsCore.
 */
public class Kit {

    private final String id;
    private String displayName;
    private String requiredRank;
    private long cooldownSeconds;
    private Material displayIcon;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private List<ItemStack> extraItems;
    private KitArmorSetBonus setBonus;

    public Kit(String id, String displayName, String requiredRank, long cooldownSeconds, Material displayIcon) {
        this.id = id.toLowerCase().trim();
        this.displayName = displayName;
        this.requiredRank = requiredRank != null ? requiredRank.toLowerCase().trim() : "wanderer";
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
        this.displayIcon = displayIcon != null ? displayIcon : Material.CHEST;
        this.extraItems = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRequiredRank() {
        return requiredRank;
    }

    public void setRequiredRank(String requiredRank) {
        this.requiredRank = requiredRank != null ? requiredRank.toLowerCase().trim() : "wanderer";
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
    }

    public Material getDisplayIcon() {
        return displayIcon;
    }

    public void setDisplayIcon(Material displayIcon) {
        this.displayIcon = displayIcon != null ? displayIcon : Material.CHEST;
    }

    public ItemStack getHelmet() {
        return helmet != null ? helmet.clone() : null;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet != null ? helmet.clone() : null;
    }

    public ItemStack getChestplate() {
        return chestplate != null ? chestplate.clone() : null;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate != null ? chestplate.clone() : null;
    }

    public ItemStack getLeggings() {
        return leggings != null ? leggings.clone() : null;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings != null ? leggings.clone() : null;
    }

    public ItemStack getBoots() {
        return boots != null ? boots.clone() : null;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots != null ? boots.clone() : null;
    }

    public List<ItemStack> getExtraItems() {
        List<ItemStack> cloneList = new ArrayList<>();
        for (ItemStack item : extraItems) {
            if (item != null) {
                cloneList.add(item.clone());
            }
        }
        return cloneList;
    }

    public void setExtraItems(List<ItemStack> extraItems) {
        this.extraItems = extraItems != null ? new ArrayList<>(extraItems) : new ArrayList<>();
    }

    public KitArmorSetBonus getSetBonus() {
        return setBonus;
    }

    public void setSetBonus(KitArmorSetBonus setBonus) {
        this.setBonus = setBonus;
    }

    public boolean hasArmor() {
        return helmet != null || chestplate != null || leggings != null || boots != null;
    }

    public int getArmorPieceCount() {
        int count = 0;
        if (helmet != null) count++;
        if (chestplate != null) count++;
        if (leggings != null) count++;
        if (boots != null) count++;
        return count;
    }
}
