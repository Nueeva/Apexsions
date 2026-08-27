package com.yourserver.apexsionscore.level.xp;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.level.xp.antiabuse.BlockPlacementTracker;
import com.yourserver.apexsionscore.level.xp.antiabuse.MovementTracker;
import com.yourserver.apexsionscore.level.xp.antiabuse.PvpKillTracker;
import com.yourserver.apexsionscore.level.xp.handlers.*;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry holding all active XP source handlers and anti-abuse trackers.
 */
public class XpSourceRegistry {

    private final ApexsionsCorePlugin plugin;
    private final List<XpSourceHandler> handlers = new ArrayList<>();

    private BlockPlacementTracker blockTracker;
    private PvpKillTracker pvpTracker;
    private MovementTracker movementTracker;

    public XpSourceRegistry(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        // Trackers
        this.blockTracker = new BlockPlacementTracker(plugin.getConfigManager());
        this.pvpTracker = new PvpKillTracker(plugin.getConfigManager());
        this.movementTracker = new MovementTracker(plugin.getConfigManager());

        Bukkit.getPluginManager().registerEvents(blockTracker, plugin);

        // Handlers
        handlers.add(new MiningXpHandler(plugin, blockTracker));
        handlers.add(new WoodcuttingXpHandler(plugin, blockTracker));
        handlers.add(new FishingXpHandler(plugin));
        handlers.add(new MobKillXpHandler(plugin));
        handlers.add(new PlayerKillXpHandler(plugin, pvpTracker));
        handlers.add(new FarmingXpHandler(plugin));
        handlers.add(new AnvilXpHandler(plugin));
        handlers.add(new EnchantingXpHandler(plugin));
        handlers.add(new CookingXpHandler(plugin));
        handlers.add(new GoldenAppleXpHandler(plugin));
        handlers.add(new BrewingXpHandler(plugin));
        handlers.add(new PotionUseXpHandler(plugin));
        handlers.add(new ExplorationXpHandler(plugin, movementTracker));

        for (XpSourceHandler handler : handlers) {
            Bukkit.getPluginManager().registerEvents(handler, plugin);
        }

        plugin.getLogger().info("Registered " + handlers.size() + " progression XP handlers.");
    }

    public void unregisterAll() {
        for (XpSourceHandler handler : handlers) {
            HandlerList.unregisterAll(handler);
        }
        if (blockTracker != null) {
            HandlerList.unregisterAll(blockTracker);
        }
        handlers.clear();
    }

    public BlockPlacementTracker getBlockTracker() {
        return blockTracker;
    }

    public PvpKillTracker getPvpTracker() {
        return pvpTracker;
    }

    public MovementTracker getMovementTracker() {
        return movementTracker;
    }
}
