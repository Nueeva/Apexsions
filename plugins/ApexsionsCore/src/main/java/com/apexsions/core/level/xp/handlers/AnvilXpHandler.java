package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handles XP awarded from repairing, combining, and renaming items in Anvils.
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
            // Result slot is index 2
            if (event.getRawSlot() == 2 && event.getWhoClicked() instanceof Player player) {
                ItemStack result = anvilInv.getItem(2);
                if (result != null && result.getType() != Material.AIR) {
                    long amount = plugin.getXpConfig().getLong("sources.anvil.default", 5L);
                    plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.ANVIL);
                }
            }
        }
    }
}
