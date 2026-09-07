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
    private final Long customQuadraticA;
    private final Long customQuadraticB;
    private final String customType;
    private final Integer customMaxLevel;

    public LevelFormula(ConfigManager configManager) {
        this.configManager = configManager;
        this.customBase = null;
        this.customExponent = null;
        this.customMultiplier = null;
        this.customQuadraticA = null;
        this.customQuadraticB = null;
        this.customType = null;
        this.customMaxLevel = null;
    }

    /**
     * Quadratic formula constructor: EXP = (a * L^2) + (b * L).
     * Default server formula: a = 510, b = -10 -> EXP = (510 * L^2) - (10 * L).
     */
    public LevelFormula(String type, long a, long b, int maxLevel) {
        this.configManager = null;
        this.customBase = null;
        this.customExponent = null;
        this.customMultiplier = null;
        this.customQuadraticA = a;
        this.customQuadraticB = b;
        this.customType = type != null ? type : "QUADRATIC";
        this.customMaxLevel = maxLevel;
    }

    /**
     * Legacy constructor for exponent-based progression.
     */
    public LevelFormula(double base, double exponent, int maxLevel) {
        this.configManager = null;
        this.customBase = base;
        this.customExponent = exponent;
        this.customMultiplier = 1.1;
        this.customQuadraticA = null;
        this.customQuadraticB = null;
        this.customType = "EXPONENT";
        this.customMaxLevel = maxLevel;
    }

    /**
     * Constructor allowing explicit formula type ("MULTIPLIER" or "EXPONENT").
     */
    public LevelFormula(String type, double base, double factor, int maxLevel) {
        this.configManager = null;
        this.customBase = base;
        this.customQuadraticA = null;
        this.customQuadraticB = null;
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

    public static LevelFormula quadratic(long a, long b, int maxLevel) {
        return new LevelFormula("QUADRATIC", a, b, maxLevel);
    }

    public static LevelFormula defaultFormula(int maxLevel) {
        return new LevelFormula("QUADRATIC", 510L, -10L, maxLevel);
    }

    public static LevelFormula multiplier(double base, double multiplier, int maxLevel) {
        return new LevelFormula("MULTIPLIER", base, multiplier, maxLevel);
    }

    private String getType() {
        if (customType != null) return customType;
        return configManager != null ? configManager.getFormulaType() : "QUADRATIC";
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

    private long getQuadraticA() {
        if (customQuadraticA != null) return customQuadraticA;
        return configManager != null ? configManager.getFormulaQuadraticA() : 510L;
    }

    private long getQuadraticB() {
        if (customQuadraticB != null) return customQuadraticB;
        return configManager != null ? configManager.getFormulaQuadraticB() : -10L;
    }

    private int getMaxLevel() {
        if (customMaxLevel != null) return customMaxLevel;
        return configManager != null ? configManager.getLevelMax() : 100;
    }

    /**
     * Calculates XP required to progress from the given level to the next level.
     * QUADRATIC (default): (a * L^2) + (b * L) -> Default: (510 * L^2) - (10 * L)
     * MULTIPLIER: base * (multiplier ^ (level - 1))
     * EXPONENT: base * (level ^ exponent)
     */
    public long getRequiredXpForNextLevel(int level) {
        if (level < 1) level = 1;
        if (level >= getMaxLevel()) {
            return Long.MAX_VALUE; // Cap reached
        }

        String type = getType();

        if ("EXPONENT".equalsIgnoreCase(type)) {
            double base = getBase();
            double exponent = getExponent();
            return Math.max(10L, Math.round(base * Math.pow(level, exponent)));
        } else if ("MULTIPLIER".equalsIgnoreCase(type)) {
            double base = getBase();
            double multiplier = getMultiplier();
            return Math.max(10L, Math.round(base * Math.pow(multiplier, level - 1)));
        } else {
            // QUADRATIC (default): (a * L^2) + (b * L)
            // Rumus resmi Apexsions: EXP = (510 * L^2) - (10 * L)
            long a = getQuadraticA();
            long b = getQuadraticB();
            long l = level;
            long xp = (a * l * l) + (b * l);
            return Math.max(10L, xp);
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
