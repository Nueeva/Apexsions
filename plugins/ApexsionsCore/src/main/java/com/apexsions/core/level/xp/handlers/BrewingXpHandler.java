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
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handles XP awarded from taking completed potions from brewing stands.
 */
public class BrewingXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;

    public BrewingXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public XpSource getSource() {
        return XpSource.BREWING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.brewing.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;

        if (event.getInventory() instanceof BrewerInventory) {
            int slot = event.getRawSlot();
            // Potion bottle slots in brewing stand: 0, 1, 2
            if (slot >= 0 && slot <= 2 && event.getWhoClicked() instanceof Player player) {
                ItemStack current = event.getCurrentItem();
                if (current != null && (current.getType() == Material.POTION ||
                        current.getType() == Material.SPLASH_POTION ||
                        current.getType() == Material.LINGERING_POTION)) {

                    long amount = plugin.getXpConfig().getLong("sources.brewing.default", 5L);
                    plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.BREWING);
                }
            }
        }
    }
}
