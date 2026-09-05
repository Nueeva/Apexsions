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

    private String getPdcString(PersistentDataContainer pdc, String keyName) {
        if (pdc == null) return null;
        NamespacedKey k1 = new NamespacedKey("apexsions", keyName);
        if (pdc.has(k1, PersistentDataType.STRING)) return pdc.get(k1, PersistentDataType.STRING);
        NamespacedKey k2 = new NamespacedKey("apexsionscore", keyName);
        if (pdc.has(k2, PersistentDataType.STRING)) return pdc.get(k2, PersistentDataType.STRING);
        return null;
    }

    private void parseStatsInto(String raw, Map<KitStatType, Double> target) {
        if (raw == null || raw.isBlank()) return;
        for (String p : raw.split(";")) {
            String[] kv = p.split(":");
            if (kv.length == 2) {
                try {
                    KitStatType st = KitStatType.valueOf(kv[0].trim());
                    double val = Double.parseDouble(kv[1].trim());
                    target.put(st, val);
                } catch (Exception ignored) {}
            }
        }
    }

    public void checkPlayerArmor(Player player) {
        if (player == null || !player.isOnline()) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        Map<String, SetPieceData> detectedSets = new HashMap<>();

        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;
            ItemMeta meta = piece.getItemMeta();
            if (meta == null) continue;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            String setId = getPdcString(pdc, "set_id");
            if (setId == null) continue;

            String sName = getPdcString(pdc, "set_name");
            final String finalSName = sName != null ? sName : setId;

            int req = 4;
            NamespacedKey kReq1 = new NamespacedKey("apexsions", "set_req");
            NamespacedKey kReq2 = new NamespacedKey("apexsionscore", "set_req");
            if (pdc.has(kReq1, PersistentDataType.INTEGER)) {
                req = pdc.get(kReq1, PersistentDataType.INTEGER);
            } else if (pdc.has(kReq2, PersistentDataType.INTEGER)) {
                req = pdc.get(kReq2, PersistentDataType.INTEGER);
            }
            final int finalReq = req;

            SetPieceData data = detectedSets.computeIfAbsent(setId, k -> new SetPieceData(setId, finalSName, finalReq));
            data.incrementPieces();

            // 1. Check set2_stats
            String raw2 = getPdcString(pdc, "set2_stats");
            if (raw2 != null && !raw2.isBlank()) {
                parseStatsInto(raw2, data.set2Stats);
            }

            // 2. Check set4_stats
            String raw4 = getPdcString(pdc, "set4_stats");
            if (raw4 != null && !raw4.isBlank()) {
                parseStatsInto(raw4, data.set4Stats);
            }

            // 3. Fallback to legacy set_stats
            String rawLegacy = getPdcString(pdc, "set_stats");
            if (rawLegacy != null && !rawLegacy.isBlank()) {
                parseStatsInto(rawLegacy, data.legacyStats);
            }

            // 4. Fallback to legacy single stat
            if (data.set2Stats.isEmpty() && data.set4Stats.isEmpty() && data.legacyStats.isEmpty()) {
                String sTypeStr = getPdcString(pdc, "set_type");
                if (sTypeStr == null) sTypeStr = "DAMAGE_REDUCTION";
                double val = 15.0;
                NamespacedKey kVal1 = new NamespacedKey("apexsions", "set_val");
                NamespacedKey kVal2 = new NamespacedKey("apexsionscore", "set_val");
                if (pdc.has(kVal1, PersistentDataType.DOUBLE)) {
                    val = pdc.get(kVal1, PersistentDataType.DOUBLE);
                } else if (pdc.has(kVal2, PersistentDataType.DOUBLE)) {
                    val = pdc.get(kVal2, PersistentDataType.DOUBLE);
                }
                try {
                    KitStatType st = KitStatType.valueOf(sTypeStr.toUpperCase());
                    data.legacyStats.put(st, val);
                } catch (Exception ignored) {}
            }
        }

        // Determine if any set qualifies: Support 2-piece, 4-piece, or BOTH simultaneously!
        ActiveBonus qualifiedBonus = null;
        for (SetPieceData data : detectedSets.values()) {
            Map<KitStatType, Double> combined = new HashMap<>();

            // 2-piece activation: active if >= 2 pieces equipped
            if (data.pieces >= 2 && !data.set2Stats.isEmpty()) {
                for (Map.Entry<KitStatType, Double> e : data.set2Stats.entrySet()) {
                    combined.merge(e.getKey(), e.getValue(), Double::sum);
                }
            }

            // 4-piece activation: active if >= 4 pieces equipped (stacks with 2-piece if both set!)
            if (data.pieces >= 4 && !data.set4Stats.isEmpty()) {
                for (Map.Entry<KitStatType, Double> e : data.set4Stats.entrySet()) {
                    combined.merge(e.getKey(), e.getValue(), Double::sum);
                }
            }

            // Fallback to legacy set_stats if neither tiered stat is defined
            if (combined.isEmpty() && data.pieces >= data.requiredPieces && !data.legacyStats.isEmpty()) {
                combined.putAll(data.legacyStats);
            }

            if (!combined.isEmpty()) {
                if (qualifiedBonus == null || data.pieces > qualifiedBonus.piecesEquipped()) {
                    qualifiedBonus = new ActiveBonus(data.setId, data.setName, combined, data.pieces);
                }
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
                    String toolSetId = getPdcString(hPdc, "set_id");
                    String customToolStats = getPdcString(hPdc, "tool_stats");
                    // If custom tool_stats are present, ToolSetBonusListener handles them. Only apply legacy +25% if no custom tool_stats exist.
                    if (toolSetId != null && toolSetId.equalsIgnoreCase(atkBonus.setId()) && (customToolStats == null || customToolStats.isBlank())) {
                        mult += 0.25; // +25% extra attack for Legacy Tool Set Bonus!
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
        private final Map<KitStatType, Double> set2Stats = new HashMap<>();
        private final Map<KitStatType, Double> set4Stats = new HashMap<>();
        private final Map<KitStatType, Double> legacyStats = new HashMap<>();
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
