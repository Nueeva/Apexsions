package com.apexsions.core.level;

import com.apexsions.core.config.ConfigManager;

/**
 * Calculates XP curves and level thresholds for KingdomCore progression (Levels 1 to 100).
 */
public class LevelFormula {

    private final ConfigManager configManager;
    private final Double customBase;
    private final Double customExponent;
    private final Integer customMaxLevel;

    public LevelFormula(ConfigManager configManager) {
        this.configManager = configManager;
        this.customBase = null;
        this.customExponent = null;
        this.customMaxLevel = null;
    }

    public LevelFormula(double base, double exponent, int maxLevel) {
        this.configManager = null;
        this.customBase = base;
        this.customExponent = exponent;
        this.customMaxLevel = maxLevel;
    }

    private double getBase() {
        return customBase != null ? customBase : configManager.getFormulaBase();
    }

    private double getExponent() {
        return customExponent != null ? customExponent : configManager.getFormulaExponent();
    }

    private int getMaxLevel() {
        return customMaxLevel != null ? customMaxLevel : configManager.getLevelMax();
    }

    /**
     * Calculates XP required to progress from the given level to the next level.
     * Formula: base * (level ^ exponent)
     */
    public long getRequiredXpForNextLevel(int level) {
        if (level < 1) level = 1;
        if (level >= getMaxLevel()) {
            return Long.MAX_VALUE; // Cap reached
        }

        double base = getBase();
        double exponent = getExponent();

        return Math.max(10L, Math.round(base * Math.pow(level, exponent)));
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
