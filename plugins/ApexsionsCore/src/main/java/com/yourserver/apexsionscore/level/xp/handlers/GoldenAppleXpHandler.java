package com.yourserver.apexsionscore.level.xp.handlers;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.level.xp.XpSource;
import com.yourserver.apexsionscore.level.xp.XpSourceHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles XP awarded from consuming Golden Apples and Enchanted Golden Apples.
 */
public class GoldenAppleXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;

    public GoldenAppleXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public XpSource getSource() {
        return XpSource.GOLDEN_APPLE;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.golden-apple.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled()) return;

        ItemStack item = event.getItem();
        Material mat = item.getType();

        if (mat == Material.GOLDEN_APPLE) {
            Player player = event.getPlayer();
            long amount = plugin.getXpConfig().getLong("sources.golden-apple.golden-apple", 10L);
            plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.GOLDEN_APPLE);
        } else if (mat == Material.ENCHANTED_GOLDEN_APPLE) {
            Player player = event.getPlayer();
            long amount = plugin.getXpConfig().getLong("sources.golden-apple.enchanted-golden-apple", 50L);
            plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.GOLDEN_APPLE);
        }
    }
}
