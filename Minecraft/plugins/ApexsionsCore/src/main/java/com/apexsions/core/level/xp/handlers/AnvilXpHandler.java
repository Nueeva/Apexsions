package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles XP awarded from repairing, combining, and renaming items in Anvils.
 * Awards bonus XP for combining and upgrading enchantment levels (Vanilla I-V & Custom VI-XX+).
 */
public class AnvilXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;

    public AnvilXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public XpSource getSource() {
        return XpSource.ANVIL;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.anvil.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;

        if (event.getInventory() instanceof AnvilInventory anvilInv) {
            // Result slot in Anvil is index 2
            if (event.getRawSlot() == 2 && event.getWhoClicked() instanceof Player player) {
                ItemStack result = anvilInv.getItem(2);
                if (result == null || result.getType().isAir()) {
                    result = event.getCurrentItem();
                }
                if (result == null || result.getType().isAir()) return;

                ItemStack first = anvilInv.getItem(0);
                ItemStack second = anvilInv.getItem(1);

                long amount = calculateAnvilXp(first, second, result);
                if (amount > 0) {
                    plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.ANVIL);
                }
            }
        }
    }

    public long calculateAnvilXp(ItemStack first, ItemStack second, ItemStack result) {
        if (first == null || first.getType().isAir()) {
            return plugin.getXpConfig().getLong("sources.anvil.default", 10L);
        }

        // 1. Rename Only (Slot 1 is empty)
        if (second == null || second.getType().isAir()) {
            return plugin.getXpConfig().getLong("sources.anvil.rename", 5L);
        }

        // 2. Combine or Repair (Both Slot 0 and Slot 1 are present)
        Map<Enchantment, Integer> firstEnchants = extractEnchantments(first);
        Map<Enchantment, Integer> resultEnchants = extractEnchantments(result);

        boolean hasEnchantmentChange = false;
        long upgradeBonus = 0L;

        long vanillaUpgradePerLvl = plugin.getXpConfig().getLong("sources.anvil.upgrade-per-level", 30L);
        long customUpgradePerLvl = plugin.getXpConfig().getLong("sources.anvil.custom-upgrade-per-level", 60L);

        for (Map.Entry<Enchantment, Integer> entry : resultEnchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int newLvl = entry.getValue();
            int oldLvl = firstEnchants.getOrDefault(ench, 0);

            if (newLvl > oldLvl) {
                hasEnchantmentChange = true;
                if (newLvl <= 5) {
                    upgradeBonus += (long) (newLvl - oldLvl) * vanillaUpgradePerLvl;
                } else {
                    // Custom levels > 5
                    int vanillaPortion = Math.max(0, 5 - oldLvl);
                    int customPortion = newLvl - Math.max(5, oldLvl);
                    upgradeBonus += ((long) vanillaPortion * vanillaUpgradePerLvl) + ((long) customPortion * customUpgradePerLvl);
                }
            }
        }

        if (hasEnchantmentChange) {
            long combineBase = plugin.getXpConfig().getLong("sources.anvil.combine-base", 25L);
            return combineBase + upgradeBonus;
        }

        // Material Repair (Durability restore without new enchants)
        return plugin.getXpConfig().getLong("sources.anvil.repair", 25L);
    }

    private Map<Enchantment, Integer> extractEnchantments(ItemStack item) {
        Map<Enchantment, Integer> map = new HashMap<>(item.getEnchantments());
        if (item.getItemMeta() instanceof EnchantmentStorageMeta bookMeta) {
            map.putAll(bookMeta.getStoredEnchants());
        }
        return map;
    }
}
