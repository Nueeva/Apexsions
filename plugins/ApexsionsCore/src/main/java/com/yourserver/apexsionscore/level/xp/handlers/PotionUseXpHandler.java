package com.yourserver.apexsionscore.level.xp.handlers;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.level.xp.XpSource;
import com.yourserver.apexsionscore.level.xp.XpSourceHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles XP awarded from consuming or splashing potions.
 */
public class PotionUseXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;

    public PotionUseXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public XpSource getSource() {
        return XpSource.POTION_USE;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.potion-use.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrink(PlayerItemConsumeEvent event) {
        if (!isEnabled()) return;

        ItemStack item = event.getItem();
        if (item.getType() == Material.POTION) {
            Player player = event.getPlayer();
            long amount = plugin.getXpConfig().getLong("sources.potion-use.default", 3L);
            plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.POTION_USE);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSplash(PotionSplashEvent event) {
        if (!isEnabled()) return;

        if (event.getPotion().getShooter() instanceof Player player) {
            long amount = plugin.getXpConfig().getLong("sources.potion-use.default", 3L);
            plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.POTION_USE);
        }
    }
}
