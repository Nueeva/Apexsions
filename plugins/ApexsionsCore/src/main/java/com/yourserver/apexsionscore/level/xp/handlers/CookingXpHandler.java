package com.yourserver.apexsionscore.level.xp.handlers;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.level.xp.XpSource;
import com.yourserver.apexsionscore.level.xp.XpSourceHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles XP awarded from cooking, smelting, and blasting items with item-specific amounts.
 */
public class CookingXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final Map<Material, Long> customAmounts = new HashMap<>();

    public CookingXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        customAmounts.clear();
        ConfigurationSection section = plugin.getXpConfig().getConfigurationSection("sources.cooking.custom-amounts");
        if (section != null) {
            for (String matName : section.getKeys(false)) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    customAmounts.put(mat, section.getLong(matName, 2L));
                }
            }
        }
    }

    @Override
    public XpSource getSource() {
        return XpSource.COOKING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.cooking.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        int count = event.getItemAmount();
        Material mat = event.getItemType();

        long perItem = customAmounts.getOrDefault(mat, plugin.getXpConfig().getLong("sources.cooking.default", 2L));
        long total = perItem * count;

        if (total > 0) {
            plugin.getLevelManager().addXp(player.getUniqueId(), total, XpSource.COOKING);
        }
    }
}
