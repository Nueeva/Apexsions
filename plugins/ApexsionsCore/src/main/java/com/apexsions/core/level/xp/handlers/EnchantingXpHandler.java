package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;

/**
 * Handles XP awarded from enchanting items in Enchantment Tables.
 */
public class EnchantingXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;

    public EnchantingXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public XpSource getSource() {
        return XpSource.ENCHANTING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.enchanting.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!isEnabled()) return;

        Player player = event.getEnchanter();
        long baseAmount = plugin.getXpConfig().getLong("sources.enchanting.default", 10L);
        boolean scale = plugin.getXpConfig().getBoolean("sources.enchanting.scale-with-level", true);

        long totalAmount = baseAmount;
        if (scale) {
            totalAmount += event.getExpLevelCost() * 2L;
        }

        plugin.getLevelManager().addXp(player.getUniqueId(), totalAmount, XpSource.ENCHANTING);
    }
}
