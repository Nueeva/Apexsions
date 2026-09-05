package com.apexsions.core.anvil;

import com.apexsions.core.ApexsionsCorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.view.AnvilView;

/**
 * Handles anvil enhancements:
 * - Bypasses the "Too Expensive!" (level 40) lock.
 * - Supports unlimited or capped repair costs.
 * - Preserves custom enchantments (e.g. Sharpness 20, Protection 12) from being downgraded.
 */
public class AnvilListener implements Listener {

    private final ApexsionsCorePlugin plugin;

    public AnvilListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.getConfigManager().isAnvilRemoveTooExpensive()) return;

        if (event.getView() instanceof AnvilView view) {
            int maxCost = plugin.getConfigManager().getAnvilMaxRepairCost();
            view.setMaximumRepairCost(maxCost);

            if (plugin.getConfigManager().isAnvilBypassEnchantLimits()) {
                view.bypassEnchantmentLevelRestriction(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!plugin.getConfigManager().isAnvilRemoveTooExpensive()) return;

        AnvilView view = event.getView();
        AnvilInventory inv = event.getInventory();
        int maxCost = plugin.getConfigManager().getAnvilMaxRepairCost();

        view.setMaximumRepairCost(maxCost);
        inv.setMaximumRepairCost(maxCost);

        if (plugin.getConfigManager().isAnvilBypassEnchantLimits()) {
            view.bypassEnchantmentLevelRestriction(true);
        }

        int costCap = plugin.getConfigManager().getAnvilCostCap();
        if (costCap > 0 && view.getRepairCost() > costCap) {
            view.setRepairCost(costCap);
        }
    }
}
