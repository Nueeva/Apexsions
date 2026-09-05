package com.apexsions.core.kingdom;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages active Paper attribute modifiers and periodic kingdom traits:
 * - Sylvamoor: Max Health +2.0, Luck +0.12, Altitude Sickness at Y > 110 (Hunger & Weakness)
 * - Solterra: Max Health -2.0, Mining Speed +10%
 * - Zenithar: Speed +0.05, Luck +0.07
 */
public class KingdomBuffManager {

    private final ApexsionsCorePlugin plugin;
    private final NamespacedKey keyHealth;
    private final NamespacedKey keySpeed;
    private final NamespacedKey keyLuck;
    private final NamespacedKey keyMining;
    private final Attribute miningAttribute;
    private BukkitTask heartbeatTask;

    public KingdomBuffManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.keyHealth = new NamespacedKey(plugin, "kingdom_health_mod");
        this.keySpeed = new NamespacedKey(plugin, "kingdom_speed_mod");
        this.keyLuck = new NamespacedKey(plugin, "kingdom_luck_mod");
        this.keyMining = new NamespacedKey(plugin, "kingdom_mining_mod");
        this.miningAttribute = resolveMiningAttribute();
    }

    private Attribute resolveMiningAttribute() {
        for (String name : new String[]{"PLAYER_BLOCK_BREAK_SPEED", "BLOCK_BREAK_SPEED", "MINING_EFFICIENCY"}) {
            try {
                return Attribute.valueOf(name);
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    public void start() {
        // Heartbeat every 40 ticks (2 seconds)
        this.heartbeatTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickHeartbeat, 40L, 40L);
    }

    public void stop() {
        if (heartbeatTask != null && !heartbeatTask.isCancelled()) {
            heartbeatTask.cancel();
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            removeBuffs(p);
        }
    }

    public void applyBuffs(Player player) {
        if (player == null || !player.isOnline()) return;

        String kingdom = getPlayerKingdomKey(player.getUniqueId());
        removeBuffs(player);

        if (kingdom.equalsIgnoreCase("NONE")) {
            return;
        }

        switch (kingdom) {
            case "SYLVAMOOR" -> {
                // Darah +2 HP (+1 Heart)
                addModifier(player, Attribute.MAX_HEALTH, keyHealth, 2.0, AttributeModifier.Operation.ADD_NUMBER);
                // Luck +12%
                addModifier(player, Attribute.LUCK, keyLuck, 0.12, AttributeModifier.Operation.ADD_NUMBER);
                // Mining speed -10% if attribute available
                if (miningAttribute != null) {
                    addModifier(player, miningAttribute, keyMining, -0.10, AttributeModifier.Operation.ADD_SCALAR);
                }
            }
            case "SOLTERRA" -> {
                // Darah -2 HP (-1 Heart)
                addModifier(player, Attribute.MAX_HEALTH, keyHealth, -2.0, AttributeModifier.Operation.ADD_NUMBER);
                // Mining speed +10% if attribute available
                if (miningAttribute != null) {
                    addModifier(player, miningAttribute, keyMining, 0.10, AttributeModifier.Operation.ADD_SCALAR);
                }
            }
            case "ZENITHAR" -> {
                // Speed +5%
                addModifier(player, Attribute.MOVEMENT_SPEED, keySpeed, 0.05, AttributeModifier.Operation.ADD_SCALAR);
                // Luck +7%
                addModifier(player, Attribute.LUCK, keyLuck, 0.07, AttributeModifier.Operation.ADD_NUMBER);
            }
        }
    }

    public void removeBuffs(Player player) {
        if (player == null) return;
        removeModifier(player, Attribute.MAX_HEALTH, keyHealth);
        removeModifier(player, Attribute.MOVEMENT_SPEED, keySpeed);
        removeModifier(player, Attribute.LUCK, keyLuck);
        if (miningAttribute != null) {
            removeModifier(player, miningAttribute, keyMining);
        }
    }

    private void addModifier(Player player, Attribute attribute, NamespacedKey key, double amount, AttributeModifier.Operation operation) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        for (AttributeModifier mod : inst.getModifiers()) {
            if (mod.getKey().equals(key)) {
                return;
            }
        }
        inst.addModifier(new AttributeModifier(key, amount, operation));
    }

    private void removeModifier(Player player, Attribute attribute, NamespacedKey key) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        for (AttributeModifier mod : new ArrayList<>(inst.getModifiers())) {
            if (mod.getKey().equals(key)) {
                inst.removeModifier(mod);
            }
        }
    }

    private void tickHeartbeat() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String kingdom = getPlayerKingdomKey(player.getUniqueId());
            if (kingdom.equalsIgnoreCase("SYLVAMOOR")) {
                // Debuff: Altitude Sickness di Ketinggian Y > 110 (Hunger & Weakness Ringan)
                if (player.getLocation().getY() > 110.0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 50, 0, true, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 50, 0, true, false, true));
                }
            } else if (kingdom.equalsIgnoreCase("SOLTERRA")) {
                // Fallback subtle Haste for mining speed if attribute not available
                if (miningAttribute == null) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 50, 0, true, false, false));
                }
            }
        }
    }

    public String getPlayerKingdomKey(UUID uuid) {
        if (uuid == null) return "NONE";
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(uuid);
        if (dataOpt.isPresent() && dataOpt.get().hasRegion()) {
            return plugin.getRegionManager().getRegion(dataOpt.get().getRegionId())
                    .map(r -> r.getKey().toUpperCase(Locale.ROOT))
                    .orElse("NONE");
        }
        return "NONE";
    }
}
