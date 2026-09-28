package com.apexsions.jobs.listener;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.JobType;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class JobActivityListener implements Listener {

    private final ApexsionsJobs plugin;
    private static final String SPAWNER_META = "apx_spawner_mob";

    public JobActivityListener(ApexsionsJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        // 1. Record placed block for Anti-Abuse
        if (plugin.getConfig().getBoolean("anti-abuse.placed-blocks-protection", true)) {
            plugin.getPlacedBlockTracker().addPlaced(event.getBlock());
        }

        // 2. Builder Job reward
        plugin.getJobService().handleAction(player, JobType.BUILDER, mat.name(), 1.0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();
        BlockData data = event.getBlock().getBlockData();

        // 1. Anti-Abuse Check: Was this block placed by a player?
        boolean isPlaced = false;
        if (plugin.getConfig().getBoolean("anti-abuse.placed-blocks-protection", true)) {
            if (plugin.getPlacedBlockTracker().isPlaced(event.getBlock())) {
                isPlaced = true;
                plugin.getPlacedBlockTracker().removePlaced(event.getBlock());
            }
        }

        if (isPlaced) {
            // Player-placed blocks NEVER grant mining or chopping rewards
            return;
        }

        // 2. Miner Job
        plugin.getJobService().handleAction(player, JobType.MINER, mat.name(), 1.0);

        // 3. Lumberjack Job
        if (mat.name().endsWith("_LOG") || mat.name().endsWith("_STEM")) {
            plugin.getJobService().handleAction(player, JobType.LUMBERJACK, mat.name(), 1.0);
        }

        // 4. Farmer Job
        if (data instanceof Ageable ageable) {
            if (ageable.getAge() >= ageable.getMaximumAge()) {
                plugin.getJobService().handleAction(player, JobType.FARMER, mat.name(), 1.0);
            }
        } else if (mat == Material.SUGAR_CANE || mat == Material.CACTUS || mat == Material.MELON || mat == Material.PUMPKIN) {
            plugin.getJobService().handleAction(player, JobType.FARMER, mat.name(), 1.0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        entity.setMetadata(SPAWNER_META, new FixedMetadataValue(plugin, true));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        double multiplier = 1.0;
        if (event.getEntity().hasMetadata(SPAWNER_META)) {
            multiplier = plugin.getConfig().getDouble("anti-abuse.spawner-mob-multiplier", 0.1);
        }

        String entityName = event.getEntityType().name();
        plugin.getJobService().handleAction(killer, JobType.HUNTER, entityName, multiplier);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Player player = event.getPlayer();
            Entity caught = event.getCaught();
            String fishType = "COD";
            if (caught instanceof Item item) {
                fishType = item.getItemStack().getType().name();
            }
            plugin.getJobService().handleAction(player, JobType.FISHERMAN, fishType, 1.0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (event.getRecipe() != null && event.getRecipe().getResult() != null) {
                Material mat = event.getRecipe().getResult().getType();
                plugin.getJobService().handleAction(player, JobType.CRAFTER, mat.name(), 1.0);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getItemType();
        plugin.getJobService().handleAction(player, JobType.CRAFTER, mat.name(), 1.0);
    }
}
