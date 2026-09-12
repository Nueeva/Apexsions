package com.apexsions.core.combat;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.stat.PlayerProgressionStats;
import com.apexsions.core.level.stat.PlayerStatCalculator;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Core combat listener for Apexsions RPG progression.
 * Applies diminishing PvE damage bonuses and resistances, while enforcing
 * strict smart normalization for PvP encounters in Kingdom Wars.
 */
public class PlayerCombatProgressionListener implements Listener {

    private final ApexsionsCorePlugin plugin;

    public PlayerCombatProgressionListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Early damage pipeline (Priority: NORMAL):
     * - PvE Outgoing: Boosts player damage against monsters by pveDamageMultiplier.
     * - PvE Incoming: Reduces monster damage against player by pveResistance.
     * - PvP Outgoing: Deducts any excess raw attack bonus exceeding the +0.80 PvP cap.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEarlyDamageCalculations(EntityDamageByEntityEvent event) {
        Player attacker = resolvePlayerAttacker(event.getDamager());
        boolean isVictimPlayer = event.getEntity() instanceof Player;

        // 1. Attacker is Player
        if (attacker != null) {
            int attackerLevel = plugin.getLevelManager().getLevel(attacker.getUniqueId());
            PlayerProgressionStats stats = PlayerStatCalculator.calculate(attackerLevel);

            if (!isVictimPlayer) {
                // PvE: Attacking monster/hostile entity
                if (isMonsterTarget(event.getEntity())) {
                    double pveBonus = stats.pveDamageMultiplier();
                    if (pveBonus > 0.0) {
                        event.setDamage(event.getDamage() * (1.0 + pveBonus));
                    }
                }
            } else {
                // PvP: Attacking another player
                // Deduct excess raw attack above the +0.80 PvP cap before kingdom/kit multipliers
                double excess = SmartCombatNormalizer.getPvPExcessAttack(stats.attackBonus());
                if (excess > 0.0) {
                    double adjusted = Math.max(0.5, event.getDamage() - excess);
                    event.setDamage(adjusted);
                }
            }
        }

        // 2. Defender is Player receiving damage from a Monster (PvE Incoming)
        if (isVictimPlayer && attacker == null) {
            Player defender = (Player) event.getEntity();
            if (isMonsterDamager(event.getDamager())) {
                int defLevel = plugin.getLevelManager().getLevel(defender.getUniqueId());
                PlayerProgressionStats defStats = PlayerStatCalculator.calculate(defLevel);
                double pveRes = defStats.pveResistance();
                if (pveRes > 0.0) {
                    event.setDamage(event.getDamage() * (1.0 - pveRes));
                }
            }
        }
    }

    /**
     * Final damage pipeline (Priority: HIGHEST):
     * Normalizes effective health pool in PvP so players with high max health
     * take proportionally scaled damage, ensuring high-level players cannot stomp
     * Kingdom Wars purely through raw HP inflation.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFinalPvPHealthNormalization(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) {
            return;
        }

        Player attacker = resolvePlayerAttacker(event.getDamager());
        if (attacker == null) {
            return; // Only apply in Player vs Player combat
        }

        AttributeInstance healthAttr = defender.getAttribute(Attribute.MAX_HEALTH);
        double actualMaxHealth = healthAttr != null ? healthAttr.getValue() : 20.0;

        double scaleFactor = SmartCombatNormalizer.getPvPHealthDamageScale(actualMaxHealth);
        if (scaleFactor > 1.0) {
            event.setDamage(event.getDamage() * scaleFactor);
        }
    }

    private Player resolvePlayerAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player p) {
            return p;
        }
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            return p;
        }
        return null;
    }

    private boolean isMonsterTarget(org.bukkit.entity.Entity target) {
        if (target instanceof Monster) return true;
        if (target instanceof org.bukkit.entity.Boss) return true;
        if (target instanceof org.bukkit.entity.Slime) return true;
        if (target instanceof org.bukkit.entity.Phantom) return true;
        if (target instanceof org.bukkit.entity.Shulker) return true;
        if (target instanceof org.bukkit.entity.Ghast) return true;
        // Check for MythicMobs or custom named entities
        return target.hasMetadata("MythicMob") || (target instanceof LivingEntity && !(target instanceof Player));
    }

    private boolean isMonsterDamager(org.bukkit.entity.Entity damager) {
        if (damager instanceof Monster) return true;
        if (damager instanceof org.bukkit.entity.Boss) return true;
        if (damager instanceof org.bukkit.entity.Slime) return true;
        if (damager instanceof org.bukkit.entity.Phantom) return true;
        if (damager instanceof org.bukkit.entity.Ghast) return true;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Monster) return true;
        return damager.hasMetadata("MythicMob");
    }
}
