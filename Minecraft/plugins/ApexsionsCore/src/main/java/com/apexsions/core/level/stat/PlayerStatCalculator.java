package com.apexsions.core.level.stat;

/**
 * Pure mathematical service for calculating player progression statistics based on level.
 * Implements soft-cap diminishing returns to ensure long-term server combat balance.
 */
public final class PlayerStatCalculator {

    private PlayerStatCalculator() {
        // Utility class
    }

    /**
     * Calculates complete progression stats for a given level.
     *
     * @param level Player level (1 to 100).
     * @return Immutable {@link PlayerProgressionStats}.
     */
    public static PlayerProgressionStats calculate(int level) {
        int safeLevel = Math.clamp(level, 1, 100);

        double healthBonus = calculateHealthBonus(safeLevel);
        double attackBonus = calculateAttackBonus(safeLevel);
        double pveDamageMultiplier = calculatePveDamageMultiplier(safeLevel);
        double pveResistance = calculatePveResistance(safeLevel);

        return new PlayerProgressionStats(safeLevel, healthBonus, attackBonus, pveDamageMultiplier, pveResistance);
    }

    /**
     * Calculates bonus max health (HP).
     * <p>
     * - Lv 1-25:  +0.20 HP/lv (+5.0 HP at Lv 25)<br>
     * - Lv 26-50: +0.15 HP/lv (+3.75 HP at Lv 50 -> total 8.75 HP)<br>
     * - Lv 51-75: +0.10 HP/lv (+2.50 HP at Lv 75 -> total 11.25 HP)<br>
     * - Lv 76-100: +0.03 HP/lv (+0.75 HP at Lv 100 -> total 12.0 HP)
     */
    public static double calculateHealthBonus(int level) {
        if (level <= 0) return 0.0;
        if (level <= 25) {
            return roundTwoDecimals(level * 0.20);
        } else if (level <= 50) {
            return roundTwoDecimals(5.0 + (level - 25) * 0.15);
        } else if (level <= 75) {
            return roundTwoDecimals(8.75 + (level - 50) * 0.10);
        } else {
            double bonus = 11.25 + (Math.min(level, 100) - 75) * 0.03;
            return roundTwoDecimals(Math.min(12.0, bonus));
        }
    }

    /**
     * Calculates bonus attack damage.
     * <p>
     * - Lv 1-30:  +0.03 Attack/lv (+0.90 Attack at Lv 30)<br>
     * - Lv 31-60: +0.02 Attack/lv (+0.60 Attack at Lv 60 -> total 1.50 Attack)<br>
     * - Lv 61-100: +0.01 Attack/lv (+0.40 Attack at Lv 100 -> total 1.90 Attack)
     */
    public static double calculateAttackBonus(int level) {
        if (level <= 0) return 0.0;
        if (level <= 30) {
            return roundTwoDecimals(level * 0.03);
        } else if (level <= 60) {
            return roundTwoDecimals(0.90 + (level - 30) * 0.02);
        } else {
            double bonus = 1.50 + (Math.min(level, 100) - 60) * 0.01;
            return roundTwoDecimals(Math.min(1.90, bonus));
        }
    }

    /**
     * Calculates bonus damage percentage against monsters (PvE only).
     * <p>
     * - Lv 1-20:  +0.50%/lv (+10.0% at Lv 20)<br>
     * - Lv 21-50: +0.30%/lv (+9.0% at Lv 50 -> total 19.0%)<br>
     * - Lv 51-75: +0.20%/lv (+5.0% at Lv 75 -> total 24.0%)<br>
     * - Lv 76-100: +0.10%/lv (+2.5% at Lv 100 -> total 26.5%)
     */
    public static double calculatePveDamageMultiplier(int level) {
        if (level <= 0) return 0.0;
        if (level <= 20) {
            return roundFourDecimals(level * 0.005);
        } else if (level <= 50) {
            return roundFourDecimals(0.10 + (level - 20) * 0.003);
        } else if (level <= 75) {
            return roundFourDecimals(0.19 + (level - 50) * 0.002);
        } else {
            double bonus = 0.24 + (Math.min(level, 100) - 75) * 0.001;
            return roundFourDecimals(Math.min(0.265, bonus));
        }
    }

    /**
     * Calculates natural damage reduction against incoming monster attacks (PvE only).
     * <p>
     * - Lv < 25:   0.0%<br>
     * - Lv 25-49:  2.5%<br>
     * - Lv 50-74:  5.0%<br>
     * - Lv 75-99:  7.5%<br>
     * - Lv 100:   10.0%
     */
    public static double calculatePveResistance(int level) {
        if (level < 25) {
            return 0.0;
        } else if (level < 50) {
            return 0.025;
        } else if (level < 75) {
            return 0.050;
        } else if (level < 100) {
            return 0.075;
        } else {
            return 0.100;
        }
    }

    private static double roundTwoDecimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private static double roundFourDecimals(double val) {
        return Math.round(val * 10000.0) / 10000.0;
    }
}
