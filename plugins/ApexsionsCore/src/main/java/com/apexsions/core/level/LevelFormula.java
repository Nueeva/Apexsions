package com.apexsions.core.level;

import com.apexsions.core.config.ConfigManager;

/**
 * Calculates XP curves and level thresholds for KingdomCore progression (Levels 1 to 100).
 */
public class LevelFormula {

    private final ConfigManager configManager;
    private final Double customBase;
    private final Double customExponent;
    private final Double customMultiplier;
    private final String customType;
    private final Integer customMaxLevel;

    public LevelFormula(ConfigManager configManager) {
        this.configManager = configManager;
        this.customBase = null;
        this.customExponent = null;
        this.customMultiplier = null;
        this.customType = null;
        this.customMaxLevel = null;
    }

    /**
     * Legacy constructor for exponent-based progression.
     */
    public LevelFormula(double base, double exponent, int maxLevel) {
        this.configManager = null;
        this.customBase = base;
        this.customExponent = exponent;
        this.customMultiplier = 1.1;
        this.customType = "EXPONENT";
        this.customMaxLevel = maxLevel;
    }

    /**
     * Constructor allowing explicit formula type ("MULTIPLIER" or "EXPONENT").
     */
    public LevelFormula(String type, double base, double factor, int maxLevel) {
        this.configManager = null;
        this.customBase = base;
        this.customType = type != null ? type : "MULTIPLIER";
        if ("EXPONENT".equalsIgnoreCase(type)) {
            this.customExponent = factor;
            this.customMultiplier = 1.1;
        } else {
            this.customExponent = 1.5;
            this.customMultiplier = factor;
        }
        this.customMaxLevel = maxLevel;
    }

    public static LevelFormula multiplier(double base, double multiplier, int maxLevel) {
        return new LevelFormula("MULTIPLIER", base, multiplier, maxLevel);
    }

    private String getType() {
        if (customType != null) return customType;
        return configManager != null ? configManager.getFormulaType() : "MULTIPLIER";
    }

    private double getBase() {
        if (customBase != null) return customBase;
        return configManager != null ? configManager.getFormulaBase() : 100.0;
    }

    private double getExponent() {
        if (customExponent != null) return customExponent;
        return configManager != null ? configManager.getFormulaExponent() : 1.5;
    }

    private double getMultiplier() {
        if (customMultiplier != null) return customMultiplier;
        return configManager != null ? configManager.getFormulaMultiplier() : 1.1;
    }

    private int getMaxLevel() {
        if (customMaxLevel != null) return customMaxLevel;
        return configManager != null ? configManager.getLevelMax() : 100;
    }

    /**
     * Calculates XP required to progress from the given level to the next level.
     * MULTIPLIER: base * (multiplier ^ (level - 1))
     * EXPONENT: base * (level ^ exponent)
     */
    public long getRequiredXpForNextLevel(int level) {
        if (level < 1) level = 1;
        if (level >= getMaxLevel()) {
            return Long.MAX_VALUE; // Cap reached
        }

        double base = getBase();
        String type = getType();

        if ("EXPONENT".equalsIgnoreCase(type)) {
            double exponent = getExponent();
            return Math.max(10L, Math.round(base * Math.pow(level, exponent)));
        } else {
            double multiplier = getMultiplier();
            return Math.max(10L, Math.round(base * Math.pow(multiplier, level - 1)));
        }
    }

    /**
     * Helper alias for getRequiredXpForNextLevel.
     */
    public long getXpForLevel(int level) {
        return getRequiredXpForNextLevel(level);
    }

    /**
     * Calculates the total cumulative XP required from Level 1 to reach the target level.
     */
    public long getTotalXpForLevel(int targetLevel) {
        long total = 0;
        for (int i = 1; i < targetLevel; i++) {
            total += getRequiredXpForNextLevel(i);
        }
        return total;
    }
}
