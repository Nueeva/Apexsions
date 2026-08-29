package com.apexsions.core.war;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;
import java.util.UUID;

/**
 * Enforces territorial PvP rules:
 * Members of the same kingdom CANNOT damage each other when inside their kingdom claim.
 * Outside kingdom territory (wilderness / warzone / enemy territory), friendly fire is allowed.
 */
public class KingdomProtectionListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public KingdomProtectionListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) return;

        // Check if same kingdom
        Optional<PlayerData> victimDataOpt = plugin.getPlayerDataService().getCached(victim.getUniqueId());
        Optional<PlayerData> attackerDataOpt = plugin.getPlayerDataService().getCached(attacker.getUniqueId());

        if (victimDataOpt.isEmpty() || attackerDataOpt.isEmpty()) return;

        UUID victimKingdom = victimDataOpt.get().getRegionId();
        UUID attackerKingdom = attackerDataOpt.get().getRegionId();

        if (victimKingdom == null || attackerKingdom == null || !victimKingdom.equals(attackerKingdom)) {
            return; // Different kingdoms or no kingdom -> standard PvP rules apply
        }

        // Both are in the same kingdom. Check if victim is inside their kingdom claim
        Location victimLoc = victim.getLocation();
        Optional<Region> regionAtLoc = plugin.getRegionManager().getRegionAt(victimLoc);

        if (regionAtLoc.isPresent() && regionAtLoc.get().getId().equals(victimKingdom)) {
            // Inside own kingdom territory -> Set damage to 0 (Masih bisa memukul tapi 0 damage)!
            event.setDamage(0.0);
            attacker.sendActionBar(mm.deserialize("<red><bold>⚔ PERLINDUNGAN WILAYAH: </bold><gray>Damage sesama warga kerajaan di wilayah sendiri adalah 0!</gray></red>"));
            attacker.playSound(attacker.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.6f, 1.4f);
        }
        // If outside territory or different kingdoms -> full PvP damage is permitted
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ProjectileSource shooter = potion.getShooter();
        if (!(shooter instanceof Player attacker)) return;

        // Check if potion contains harmful effects
        boolean hasHarmful = false;
        for (PotionEffect effect : potion.getEffects()) {
            PotionEffectType type = effect.getType();
            if (type.equals(PotionEffectType.INSTANT_DAMAGE) ||
                type.equals(PotionEffectType.POISON) ||
                type.equals(PotionEffectType.SLOWNESS) ||
                type.equals(PotionEffectType.WEAKNESS) ||
                type.equals(PotionEffectType.WITHER) ||
                type.equals(PotionEffectType.MINING_FATIGUE) ||
                type.equals(PotionEffectType.BLINDNESS)) {
                hasHarmful = true;
                break;
            }
        }

        if (!hasHarmful) return;

        Optional<PlayerData> attackerDataOpt = plugin.getPlayerDataService().getCached(attacker.getUniqueId());
        if (attackerDataOpt.isEmpty() || attackerDataOpt.get().getRegionId() == null) return;
        UUID attackerKingdom = attackerDataOpt.get().getRegionId();

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player victim) || victim.getUniqueId().equals(attacker.getUniqueId())) continue;

            Optional<PlayerData> victimDataOpt = plugin.getPlayerDataService().getCached(victim.getUniqueId());
            if (victimDataOpt.isPresent() && attackerKingdom.equals(victimDataOpt.get().getRegionId())) {
                Optional<Region> regionAtLoc = plugin.getRegionManager().getRegionAt(victim.getLocation());
                if (regionAtLoc.isPresent() && regionAtLoc.get().getId().equals(attackerKingdom)) {
                    // Cancel harmful potion effect on same kingdom member inside claim
                    event.setIntensity(victim, 0.0);
                }
            }
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player p) {
            return p;
        }
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            return p;
        }
        if (damager instanceof Tameable pet && pet.getOwner() instanceof Player p) {
            return p;
        }
        return null;
    }
}
