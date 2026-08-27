package com.yourserver.apexsionscore.level.xp.handlers;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.level.xp.XpSource;
import com.yourserver.apexsionscore.level.xp.XpSourceHandler;
import com.yourserver.apexsionscore.level.xp.antiabuse.BlockPlacementTracker;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles XP awarded from woodcutting natural logs.
 */
public class WoodcuttingXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final BlockPlacementTracker blockTracker;
    private final Map<Material, Long> customAmounts = new HashMap<>();

    public WoodcuttingXpHandler(ApexsionsCorePlugin plugin, BlockPlacementTracker blockTracker) {
        this.plugin = plugin;
        this.blockTracker = blockTracker;
        loadConfig();
    }

    private void loadConfig() {
        customAmounts.clear();
        ConfigurationSection section = plugin.getXpConfig().getConfigurationSection("sources.woodcutting.custom-amounts");
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
        return XpSource.WOODCUTTING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.woodcutting.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Block block = event.getBlock();
        Material mat = block.getType();

        if (!isLog(mat)) return;

        // Anti-abuse: check player placed
        if (blockTracker.isPlayerPlaced(block)) {
            return;
        }

        long amount = customAmounts.getOrDefault(mat, plugin.getXpConfig().getLong("sources.woodcutting.default", 2L));
        if (amount > 0) {
            Player player = event.getPlayer();
            plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.WOODCUTTING);
        }
    }

    private boolean isLog(Material mat) {
        String name = mat.name();
        return name.endsWith("_LOG") || name.endsWith("_WOOD") || name.endsWith("_STEM") || name.endsWith("_HYPHAE");
    }
}
