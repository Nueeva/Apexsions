package com.apexsions.customenchants.group;

import org.bukkit.Material;

/**
 * Model representing a custom enchantment tier/group (Simple, Unique, Elite, Ultimate, Legendary, Fabled, Heroic).
 */
public class EnchantmentGroup {

    private final String id;
    private String displayName;
    private String color;
    private Material icon;
    private String currency; // "rupiah" or "diamond"
    private double cost;
    private boolean enabled;
    private boolean comingSoon;

    public EnchantmentGroup(String id, String displayName, String color, Material icon, String currency, double cost, boolean enabled, boolean comingSoon) {
        this.id = id.toUpperCase().trim();
        this.displayName = displayName;
        this.color = color;
        this.icon = icon != null ? icon : Material.BOOK;
        this.currency = currency != null ? currency.toLowerCase().trim() : "rupiah";
        this.cost = cost;
        this.enabled = enabled;
        this.comingSoon = comingSoon;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency != null ? currency.toLowerCase().trim() : "rupiah";
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isComingSoon() {
        return comingSoon;
    }

    public void setComingSoon(boolean comingSoon) {
        this.comingSoon = comingSoon;
    }

    public String getFormattedCost() {
        if (currency.equalsIgnoreCase("diamond")) {
            return (long) cost + " Diamond";
        } else {
            return "Rp " + String.format("%,d", (long) cost).replace(',', '.');
        }
    }
}
