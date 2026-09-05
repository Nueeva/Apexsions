package com.apexsions.customenchants.listener;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.tools.ToolStatType;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime listener managing Tool and Weapon Set Bonus synergies.
 * Bonuses strictly only activate when the player wears armor with the matching set ID.
 */
public class ToolSetBonusListener implements Listener {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final NamespacedKey keySetId;
    private final NamespacedKey keyToolBonus;
    private final NamespacedKey keyToolStats;

    private final NamespacedKey keyReachMod;
    private final NamespacedKey keySpeedMod;

    public ToolSetBonusListener(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.keySetId = new NamespacedKey("apexsions", "set_id");
        this.keyToolBonus = new NamespacedKey("apexsions", "tool_bonus");
        this.keyToolStats = new NamespacedKey("apexsions", "tool_stats");

        this.keyReachMod = new NamespacedKey(plugin, "tool_reach_bonus");
        this.keySpeedMod = new NamespacedKey(plugin, "tool_speed_bonus");
    }

    /**
     * Checks if the player is currently wearing at least 2 pieces of armor with the matching setId.
     */
    public boolean isWearingMatchingArmorSet(Player player, String toolSetId) {
        if (toolSetId == null || toolSetId.isBlank()) return false;
        ItemStack[] armor = player.getInventory().getArmorContents();
        int matchingPieces = 0;

        for (ItemStack piece : armor) {
            if (piece == null || piece.getType().isAir()) continue;
            ItemMeta meta = piece.getItemMeta();
            if (meta == null) continue;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(keySetId, PersistentDataType.STRING)) {
                String aSetId = pdc.get(keySetId, PersistentDataType.STRING);
                if (toolSetId.equalsIgnoreCase(aSetId)) {
                    matchingPieces++;
                }
            }
        }

        // At least 2 pieces of the set must be worn for tool synergy to activate
        return matchingPieces >= 2;
    }

    private Map<ToolStatType, Double> getToolStats(ItemStack item) {
        Map<ToolStatType, Double> map = new HashMap<>();
        if (item == null || item.getType().isAir()) return map;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return map;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (pdc.has(keyToolStats, PersistentDataType.STRING)) {
            String raw = pdc.get(keyToolStats, PersistentDataType.STRING);
            if (raw != null && !raw.isBlank()) {
                for (String p : raw.split(";")) {
                    String[] kv = p.split(":");
                    if (kv.length == 2) {
                        try {
                            ToolStatType st = ToolStatType.valueOf(kv[0].trim());
                            double val = Double.parseDouble(kv[1].trim());
                            map.put(st, val);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return map;
    }

    private String getToolSetId(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(keySetId, PersistentDataType.STRING);
    }

    // 1. Weapon Combat Boost
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWeaponAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        String setId = getToolSetId(weapon);
        if (setId == null) return;

        if (!isWearingMatchingArmorSet(player, setId)) return;

        Map<ToolStatType, Double> stats = getToolStats(weapon);
        if (stats.isEmpty()) return;

        double damageMult = 1.0;

        // Weapon Damage Boost
        if (stats.containsKey(ToolStatType.WEAPON_DAMAGE_BOOST)) {
            double boost = stats.get(ToolStatType.WEAPON_DAMAGE_BOOST);
            damageMult += (boost / 100.0);
        }

        // Critical Damage Boost (when falling/crit)
        if (stats.containsKey(ToolStatType.CRITICAL_DAMAGE_BOOST) && player.getFallDistance() > 0.0f) {
            double critBoost = stats.get(ToolStatType.CRITICAL_DAMAGE_BOOST);
            damageMult += (critBoost / 100.0);
        }

        if (damageMult > 1.0) {
            event.setDamage(event.getDamage() * damageMult);
            player.getWorld().spawnParticle(Particle.CRIT, event.getEntity().getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    // 2. Durability Preservation (Unbreakable while set active)
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        String setId = getToolSetId(item);
        if (setId == null) return;

        if (!isWearingMatchingArmorSet(player, setId)) return;

        Map<ToolStatType, Double> stats = getToolStats(item);
        if (stats.containsKey(ToolStatType.UNBREAKABLE_SET) && stats.get(ToolStatType.UNBREAKABLE_SET) > 0.0) {
            event.setCancelled(true);
        }
    }

    // 3. Block Break & Experience Multiplier & Mining Fatigue Immunity
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        String setId = getToolSetId(tool);
        if (setId == null) return;

        if (!isWearingMatchingArmorSet(player, setId)) return;

        Map<ToolStatType, Double> stats = getToolStats(tool);
        if (stats.isEmpty()) return;

        // Mining Fatigue Immunity
        if (stats.containsKey(ToolStatType.FATIGUE_IMMUNITY) && stats.get(ToolStatType.FATIGUE_IMMUNITY) > 0.0) {
            if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            }
        }

        // EXP Multiplier
        if (stats.containsKey(ToolStatType.EXP_MULTIPLIER) && event.getExpToDrop() > 0) {
            double boost = stats.get(ToolStatType.EXP_MULTIPLIER);
            int newExp = (int) Math.round(event.getExpToDrop() * (1.0 + (boost / 100.0)));
            event.setExpToDrop(newExp);
        }
    }

    // 4. Fishing EXP Boost
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        String setId = getToolSetId(rod);
        if (setId == null) return;

        if (!isWearingMatchingArmorSet(player, setId)) return;

        Map<ToolStatType, Double> stats = getToolStats(rod);
        if (stats.containsKey(ToolStatType.EXP_MULTIPLIER) && event.getExpToDrop() > 0) {
            double boost = stats.get(ToolStatType.EXP_MULTIPLIER);
            event.setExpToDrop((int) Math.round(event.getExpToDrop() * (1.0 + (boost / 100.0))));
        }
    }

    // 5. Reach & Attack Speed Attributes update when holding item
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        updateToolAttributes(player, newItem);
    }

    public void updateToolAttributes(Player player, ItemStack item) {
        AttributeInstance reachInst = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        AttributeInstance entityReachInst = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        AttributeInstance speedInst = player.getAttribute(Attribute.ATTACK_SPEED);

        // Remove previous modifiers
        if (reachInst != null) reachInst.removeModifier(keyReachMod);
        if (entityReachInst != null) entityReachInst.removeModifier(keyReachMod);
        if (speedInst != null) speedInst.removeModifier(keySpeedMod);

        if (item == null || item.getType().isAir()) return;
        String setId = getToolSetId(item);
        if (setId == null || !isWearingMatchingArmorSet(player, setId)) return;

        Map<ToolStatType, Double> stats = getToolStats(item);
        if (stats.isEmpty()) return;

        // Block reach
        if (stats.containsKey(ToolStatType.MINING_REACH_BOOST) && reachInst != null) {
            double blocks = stats.get(ToolStatType.MINING_REACH_BOOST);
            reachInst.addTransientModifier(new AttributeModifier(keyReachMod, blocks, AttributeModifier.Operation.ADD_NUMBER));
        }

        // Entity attack reach
        if (stats.containsKey(ToolStatType.ATTACK_REACH_BOOST) && entityReachInst != null) {
            double blocks = stats.get(ToolStatType.ATTACK_REACH_BOOST);
            entityReachInst.addTransientModifier(new AttributeModifier(keyReachMod, blocks, AttributeModifier.Operation.ADD_NUMBER));
        }

        // Attack speed
        if (stats.containsKey(ToolStatType.ATTACK_SPEED_BOOST) && speedInst != null) {
            double boost = stats.get(ToolStatType.ATTACK_SPEED_BOOST);
            speedInst.addTransientModifier(new AttributeModifier(keySpeedMod, boost / 100.0, AttributeModifier.Operation.ADD_SCALAR));
        }
    }
}
