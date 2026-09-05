package com.apexsions.core.kit;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * High-performance combat & attribute listener enforcing Armor Set Bonuses & Tool Set Bonuses.
 * Supports multiple concurrent stat bonuses per set.
 */
public class KitArmorSetListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Cache of active bonuses per player for instant zero-allocation combat queries
    private final Map<UUID, ActiveBonus> activeBonuses = new ConcurrentHashMap<>();

    private final NamespacedKey keyHealthMod;
    private final NamespacedKey keySpeedMod;

    public record ActiveBonus(
            String setId,
            String setName,
            Map<KitStatType, Double> stats,
            int piecesEquipped
    ) {
        public double getStat(KitStatType type) {
            return stats.getOrDefault(type, 0.0);
        }

        public boolean hasStat(KitStatType type) {
            return stats.containsKey(type);
        }
    }

    public KitArmorSetListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.keyHealthMod = new NamespacedKey(plugin, "armor_set_health");
        this.keySpeedMod = new NamespacedKey(plugin, "armor_set_speed");

        // Periodic 20-tick scan of equipped armor for all online players
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::scanAllOnlinePlayers, 20L, 20L);
    }

    public void scanAllOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            checkPlayerArmor(player);
        }
    }

    public void checkPlayerArmor(Player player) {
        if (player == null || !player.isOnline()) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        Map<String, SetPieceData> detectedSets = new HashMap<>();

        NamespacedKey kSetId = plugin.getKitManager().getKeySetId();
        NamespacedKey kSetName = plugin.getKitManager().getKeySetName();
        NamespacedKey kSetType = plugin.getKitManager().getKeySetType();
        NamespacedKey kSetVal = plugin.getKitManager().getKeySetVal();
        NamespacedKey kSetReq = plugin.getKitManager().getKeySetReq();
        NamespacedKey kSetStats = plugin.getKitManager().getKeySetStats();

        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;
            ItemMeta meta = piece.getItemMeta();
            if (meta == null) continue;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(kSetId, PersistentDataType.STRING)) {
                String setId = pdc.get(kSetId, PersistentDataType.STRING);
                if (setId == null) continue;

                String sName = pdc.getOrDefault(kSetName, PersistentDataType.STRING, setId);
                int req = pdc.getOrDefault(kSetReq, PersistentDataType.INTEGER, 4);

                SetPieceData data = detectedSets.computeIfAbsent(setId, k -> new SetPieceData(setId, sName, req));
                data.incrementPieces();

                // Parse multi-stat string if present
                if (pdc.has(kSetStats, PersistentDataType.STRING)) {
                    String raw = pdc.get(kSetStats, PersistentDataType.STRING);
                    if (raw != null) {
                        for (String p : raw.split(";")) {
                            String[] kv = p.split(":");
                            if (kv.length == 2) {
                                try {
                                    KitStatType st = KitStatType.valueOf(kv[0].trim());
                                    double val = Double.parseDouble(kv[1].trim());
                                    data.stats.put(st, val);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }

                // Fallback to legacy single stat
                if (data.stats.isEmpty()) {
                    String sTypeStr = pdc.getOrDefault(kSetType, PersistentDataType.STRING, "DAMAGE_REDUCTION");
                    double val = pdc.getOrDefault(kSetVal, PersistentDataType.DOUBLE, 15.0);
                    try {
                        KitStatType st = KitStatType.valueOf(sTypeStr.toUpperCase());
                        data.stats.put(st, val);
                    } catch (Exception ignored) {}
                }
            }
        }

        // Determine if any set qualifies
        ActiveBonus qualifiedBonus = null;
        for (SetPieceData data : detectedSets.values()) {
            if (data.pieces >= data.requiredPieces) {
                qualifiedBonus = new ActiveBonus(data.setId, data.setName, data.stats, data.pieces);
                break;
            }
        }

        ActiveBonus prevBonus = activeBonuses.get(player.getUniqueId());

        if (qualifiedBonus != null) {
            activeBonuses.put(player.getUniqueId(), qualifiedBonus);
            applyAttributeBonuses(player, qualifiedBonus);

            if (prevBonus == null || !prevBonus.setId().equalsIgnoreCase(qualifiedBonus.setId())) {
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.4f);
                player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>✦ ARMOR SET BONUS AKTIF! ✦</bold></gradient>"));
                player.sendMessage(mm.deserialize("<gray>Set:</gray> <gold>" + qualifiedBonus.setName() + "</gold> <dark_gray>(" + qualifiedBonus.piecesEquipped() + " Pieces)</dark_gray>"));
                for (Map.Entry<KitStatType, Double> e : qualifiedBonus.stats().entrySet()) {
                    player.sendMessage(mm.deserialize("<gray>Efek:</gray> <yellow>" + e.getKey().formatValue(e.getValue()) + " " + e.getKey().getDisplayName() + "</yellow>"));
                }
            }
        } else {
            if (prevBonus != null) {
                activeBonuses.remove(player.getUniqueId());
                removeAttributeBonuses(player);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.0f);
                player.sendMessage(mm.deserialize("<red>✦ Armor Set Bonus (" + prevBonus.setName() + ") non-aktif karena keping armor dilepas. ✦</red>"));
            }
        }
    }

    private void applyAttributeBonuses(Player player, ActiveBonus bonus) {
        if (bonus.hasStat(KitStatType.EXTRA_MAX_HEALTH)) {
            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                boolean hasMod = false;
                for (AttributeModifier mod : attr.getModifiers()) {
                    if (mod.getKey().equals(keyHealthMod)) {
                        hasMod = true;
                        break;
                    }
                }
                if (!hasMod) {
                    AttributeModifier mod = new AttributeModifier(keyHealthMod, bonus.getStat(KitStatType.EXTRA_MAX_HEALTH), AttributeModifier.Operation.ADD_NUMBER);
                    attr.addModifier(mod);
                }
            }
        }

        if (bonus.hasStat(KitStatType.MOVEMENT_SPEED_BOOST)) {
            AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
            if (attr != null) {
                boolean hasMod = false;
                for (AttributeModifier mod : attr.getModifiers()) {
                    if (mod.getKey().equals(keySpeedMod)) {
                        hasMod = true;
                        break;
                    }
                }
                if (!hasMod) {
                    AttributeModifier mod = new AttributeModifier(keySpeedMod, bonus.getStat(KitStatType.MOVEMENT_SPEED_BOOST) / 100.0, AttributeModifier.Operation.ADD_SCALAR);
                    attr.addModifier(mod);
                }
            }
        }
    }

    private void removeAttributeBonuses(Player player) {
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null) {
            for (AttributeModifier mod : new ArrayList<>(healthAttr.getModifiers())) {
                if (mod.getKey().equals(keyHealthMod)) {
                    healthAttr.removeModifier(mod);
                }
            }
        }

        AttributeInstance speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            for (AttributeModifier mod : new ArrayList<>(speedAttr.getModifiers())) {
                if (mod.getKey().equals(keySpeedMod)) {
                    speedAttr.removeModifier(mod);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // 1. Attacker Stat Bonuses (ATTACK_DAMAGE_BOOST, CRITICAL_DAMAGE_BOOST, and TOOL SET BONUS)
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker != null) {
            ActiveBonus atkBonus = activeBonuses.get(attacker.getUniqueId());
            if (atkBonus != null) {
                double mult = 1.0;

                if (atkBonus.hasStat(KitStatType.ATTACK_DAMAGE_BOOST)) {
                    mult += (atkBonus.getStat(KitStatType.ATTACK_DAMAGE_BOOST) / 100.0);
                }

                if (atkBonus.hasStat(KitStatType.CRITICAL_DAMAGE_BOOST) && attacker.getFallDistance() > 0.0f) {
                    mult += (atkBonus.getStat(KitStatType.CRITICAL_DAMAGE_BOOST) / 100.0);
                    attacker.getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
                }

                // TOOL SET BONUS: Check if held item in main hand shares the same set_id!
                ItemStack held = attacker.getInventory().getItemInMainHand();
                if (held != null && !held.getType().isAir() && held.hasItemMeta()) {
                    PersistentDataContainer hPdc = held.getItemMeta().getPersistentDataContainer();
                    String toolSetId = hPdc.get(plugin.getKitManager().getKeySetId(), PersistentDataType.STRING);
                    if (toolSetId != null && toolSetId.equalsIgnoreCase(atkBonus.setId())) {
                        mult += 0.25; // +25% extra attack for Tool Set Bonus!
                        attacker.getWorld().spawnParticle(Particle.ENCHANTED_HIT, event.getEntity().getLocation().add(0, 1, 0), 12, 0.3, 0.3, 0.3, 0.1);
                    }
                }

                if (mult > 1.0) {
                    event.setDamage(event.getDamage() * mult);
                }
            }
        }

        // 2. Defender Stat Bonuses (DODGE_CHANCE, DAMAGE_REDUCTION)
        if (event.getEntity() instanceof Player defender) {
            ActiveBonus defBonus = activeBonuses.get(defender.getUniqueId());
            if (defBonus != null) {
                // Dodge Chance check
                if (defBonus.hasStat(KitStatType.DODGE_CHANCE)) {
                    double roll = ThreadLocalRandom.current().nextDouble() * 100.0;
                    if (roll < defBonus.getStat(KitStatType.DODGE_CHANCE)) {
                        event.setCancelled(true);
                        defender.playSound(defender.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0f, 1.5f);
                        defender.playSound(defender.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, 1.2f);
                        defender.getWorld().spawnParticle(Particle.POOF, defender.getLocation().add(0, 1, 0), 10, 0.2, 0.4, 0.2, 0.05);
                        defender.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>💨 DODGE!</bold> Kamu berhasil menghindari serangan musuh!</gradient>"));
                        if (attacker != null) {
                            attacker.sendMessage(mm.deserialize("<gray>Musuh berhasil menghindari seranganmu!</gray>"));
                        }
                        return;
                    }
                }

                // Damage Reduction check
                if (defBonus.hasStat(KitStatType.DAMAGE_REDUCTION)) {
                    double reduction = Math.abs(defBonus.getStat(KitStatType.DAMAGE_REDUCTION)) / 100.0;
                    double newDamage = Math.max(0.5, event.getDamage() * (1.0 - reduction));
                    event.setDamage(newDamage);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        activeBonuses.remove(player.getUniqueId());
        removeAttributeBonuses(player);
    }

    public ActiveBonus getActiveBonus(UUID uuid) {
        return activeBonuses.get(uuid);
    }

    private static class SetPieceData {
        private final String setId;
        private final String setName;
        private final Map<KitStatType, Double> stats = new HashMap<>();
        private final int requiredPieces;
        private int pieces = 0;

        public SetPieceData(String setId, String setName, int requiredPieces) {
            this.setId = setId;
            this.setName = setName;
            this.requiredPieces = requiredPieces;
        }

        public void incrementPieces() {
            this.pieces++;
        }
    }
}
