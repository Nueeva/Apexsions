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
 * Handles XP awarded from mining ores and natural rock resources.
 */
public class MiningXpHandler implements XpSourceHandler {

    private final ApexsionsCorePlugin plugin;
    private final BlockPlacementTracker blockTracker;
    private final Map<Material, Long> customAmounts = new HashMap<>();

    public MiningXpHandler(ApexsionsCorePlugin plugin, BlockPlacementTracker blockTracker) {
        this.plugin = plugin;
        this.blockTracker = blockTracker;
        loadConfig();
    }

    private void loadConfig() {
        customAmounts.clear();
        ConfigurationSection section = plugin.getXpConfig().getConfigurationSection("sources.mining.custom-amounts");
        if (section != null) {
            for (String matName : section.getKeys(false)) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    customAmounts.put(mat, section.getLong(matName, 1L));
                }
            }
        }
    }

    @Override
    public XpSource getSource() {
        return XpSource.MINING;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getXpConfig().getBoolean("sources.mining.enabled", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;

        Block block = event.getBlock();
        Material mat = block.getType();

        // Check if natural block
        boolean naturalOnly = plugin.getXpConfig().getBoolean("sources.mining.natural-blocks-only", true);
        if (naturalOnly && blockTracker.isPlayerPlaced(block)) {
            return;
        }

        long amount = 0;
        if (customAmounts.containsKey(mat)) {
            amount = customAmounts.get(mat);
        } else if (isGeneralMiningBlock(mat)) {
            amount = plugin.getXpConfig().getLong("sources.mining.default", 1L);
        }

        if (amount > 0) {
            Player player = event.getPlayer();
            plugin.getLevelManager().addXp(player.getUniqueId(), amount, XpSource.MINING);
        }
    }

    private boolean isGeneralMiningBlock(Material mat) {
        return mat == Material.STONE || mat == Material.DEEPSLATE ||
                mat == Material.ANDESITE || mat == Material.DIORITE ||
                mat == Material.GRANITE || mat == Material.TUFF ||
                mat == Material.CALCITE || mat == Material.NETHERRACK ||
                mat == Material.BASALT || mat == Material.BLACKSTONE ||
                mat == Material.END_STONE;
    }
}
