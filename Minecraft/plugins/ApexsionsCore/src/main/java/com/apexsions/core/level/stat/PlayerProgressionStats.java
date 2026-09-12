package com.apexsions.core.level.stat;

/**
 * Immutable record representing the combat and attribute progression stats of a player based on their level.
 *
 * @param level               The player's progression level (1-100).
 * @param healthBonus         Bonus max health (HP) added via attribute modifier (diminishing curve, max 12.0 HP).
 * @param attackBonus         Bonus raw attack damage added via attribute modifier (diminishing curve, max 1.90 Attack).
 * @param pveDamageMultiplier Additional percentage damage dealt against monster entities (diminishing curve, max +26.5%).
 * @param pveResistance       Percentage damage reduction against incoming attacks from monster entities (max 10.0%).
 */
public record PlayerProgressionStats(
        int level,
        double healthBonus,
        double attackBonus,
        double pveDamageMultiplier,
        double pveResistance
) {
    /**
     * @return Total expected base max health (Vanilla 20.0 + healthBonus).
     */
    public double getTotalMaxHealth() {
        return 20.0 + healthBonus;
    }

    /**
     * @return Formatted string for UI/Scoreboard display.
     */
    public String getFormattedPveDamagePercent() {
        return String.format("+%.1f%%", pveDamageMultiplier * 100.0);
    }

    /**
     * @return Formatted string for UI/Scoreboard display.
     */
    public String getFormattedPveResistancePercent() {
        return String.format("%.1f%%", pveResistance * 100.0);
    }
}
