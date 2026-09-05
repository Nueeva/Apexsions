package com.apexsions.core.level.xp.handlers;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.level.xp.XpSourceHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles XP awarded from fishing activities with specific amounts per fish and treasure caught.
 */
public class FishingXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final Map<Material, Long> customAmounts = new HashMap<>();

    public FishingXpHandler(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        customAmounts.clear();
        ConfigurationSection section = plugin.getXpConfig().getConfigurationSection("sources.fishing.custom-amounts");
        if (section != null) {
            for (String matName : section.getKeys(false)) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    customAmounts.put(mat, section.getLong(matName, 10L));
                }
            }
        }
    }

    @Override
    public XpSource getSource() {
        return XpSource.FISHING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.fishing.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (!isEnabled()) return;

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            long amount = plugin.getXpConfig().getLong("sources.fishing.default", 10L);

            if (event.getCaught() instanceof Item caughtItem) {
                ItemStack stack = caughtItem.getItemStack();
                Material caughtMat = stack.getType();
                if (customAmounts.containsKey(caughtMat)) {
                    amount = customAmounts.get(caughtMat);
                } else if (isTreasureItem(stack)) {
                    amount += plugin.getXpConfig().getLong("sources.fishing.treasure-bonus", 25L);
                }
            }

            if (amount > 0) {
                plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.FISHING);
            }
        }
    }

    private boolean isTreasureItem(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.contains("ENCHANTED_BOOK") || name.contains("BOW") ||
                name.contains("FISHING_ROD") || name.contains("NAME_TAG") ||
                name.contains("NAUTILUS_SHELL") || name.contains("SADDLE");
    }
}
