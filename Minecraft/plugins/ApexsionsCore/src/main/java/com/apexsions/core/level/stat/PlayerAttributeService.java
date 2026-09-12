package com.apexsions.core.level.stat;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.event.KingdomLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.ArrayList;

/**
 * Service managing native Paper AttributeModifiers for player level progression.
 * Applies persistent MAX_HEALTH and ATTACK_DAMAGE bonuses using safe NamespacedKeys.
 */
public class PlayerAttributeService implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final NamespacedKey keyHealth;
    private final NamespacedKey keyAttack;

    public PlayerAttributeService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.keyHealth = new NamespacedKey(plugin, "apex_level_health");
        this.keyAttack = new NamespacedKey(plugin, "apex_level_attack");
    }

    /**
     * Applies level progression attribute modifiers to the player.
     *
     * @param player Target player.
     * @param level  Player's progression level.
     */
    public void applyAttributes(Player player, int level) {
        if (player == null || !player.isOnline()) return;

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> applyAttributes(player, level));
            return;
        }

        PlayerProgressionStats stats = PlayerStatCalculator.calculate(level);

        // 1. Apply Max Health
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            removeModifier(healthAttr, keyHealth);
            if (stats.healthBonus() > 0) {
                AttributeModifier mod = new AttributeModifier(keyHealth, stats.healthBonus(), AttributeModifier.Operation.ADD_NUMBER);
                healthAttr.addModifier(mod);
            }
            // Clamp health if needed so it doesn't exceed new max health
            if (player.getHealth() > healthAttr.getValue()) {
                player.setHealth(healthAttr.getValue());
            }
        }

        // 2. Apply Attack Damage
        AttributeInstance attackAttr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackAttr != null) {
            removeModifier(attackAttr, keyAttack);
            if (stats.attackBonus() > 0) {
                AttributeModifier mod = new AttributeModifier(keyAttack, stats.attackBonus(), AttributeModifier.Operation.ADD_NUMBER);
                attackAttr.addModifier(mod);
            }
        }
    }

    /**
     * Removes all level progression attribute modifiers from the player.
     *
     * @param player Target player.
     */
    public void removeAttributes(Player player) {
        if (player == null) return;
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            removeModifier(healthAttr, keyHealth);
        }
        AttributeInstance attackAttr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackAttr != null) {
            removeModifier(attackAttr, keyAttack);
        }
    }

    private void removeModifier(AttributeInstance instance, NamespacedKey key) {
        for (AttributeModifier mod : new ArrayList<>(instance.getModifiers())) {
            if (mod.getKey().equals(key)) {
                instance.removeModifier(mod);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Delay slightly so PlayerData has finished loading from cache
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                int level = plugin.getLevelManager().getLevel(player.getUniqueId());
                applyAttributes(player, level);
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                int level = plugin.getLevelManager().getLevel(player.getUniqueId());
                applyAttributes(player, level);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKingdomLevelUp(KingdomLevelUpEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.isOnline()) {
            applyAttributes(player, event.getNewLevel());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up memory or transient state if necessary
    }
}
