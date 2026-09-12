package com.apexsions.core.combat;

/**
 * Normalizer utility to ensure fair PvP gameplay in Kingdom Wars.
 * Prevents high-level stat inflation from trivializing player-versus-player combat.
 */
public final class SmartCombatNormalizer {

    /**
     * Maximum bonus raw attack damage allowed to take effect during PvP combat.
     * Any excess attack bonus granted by level progression is deducted from PvP strikes.
     */
    public static final double PVP_MAX_ATTACK_BONUS = 0.80;

    /**
     * Effective maximum health threshold for PvP damage calculations (Vanilla 20.0 + 4.0 bonus).
     */
    public static final double PVP_MAX_EFFECTIVE_HEALTH = 24.0;

    private SmartCombatNormalizer() {
        // Utility class
    }

    /**
     * Calculates any excess attack damage above the allowed PvP cap.
     *
     * @param levelAttackBonus The player's raw level attack bonus.
     * @return Excess damage to deduct from PvP attack (0.0 if within cap).
     */
    public static double getPvPExcessAttack(double levelAttackBonus) {
        return Math.max(0.0, levelAttackBonus - PVP_MAX_ATTACK_BONUS);
    }

    /**
     * Computes the proportional damage scaling factor for high-health defenders in PvP.
     * Ensures that a strike deals the same percentage of effective health against a high-level
     * player as it would against a normalized 24.0 HP player, eliminating stat-stomping without
     * visually flickering hearts.
     *
     * @param actualMaxHealth The defender's actual max health.
     * @return Proportional multiplier (>= 1.0).
     */
    public static double getPvPHealthDamageScale(double actualMaxHealth) {
        if (actualMaxHealth > PVP_MAX_EFFECTIVE_HEALTH) {
            return actualMaxHealth / PVP_MAX_EFFECTIVE_HEALTH;
        }
        return 1.0;
    }
}
