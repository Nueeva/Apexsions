package com.apexsions.quests.listener;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.QuestObjectiveType;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

public class QuestEventListener implements Listener {

    private final ApexsionsQuests plugin;

    public QuestEventListener(ApexsionsQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();
        BlockData data = event.getBlock().getBlockData();

        // Check if fully grown crop
        if (data instanceof Ageable ageable) {
            if (ageable.getAge() >= ageable.getMaximumAge()) {
                plugin.getQuestManager().onObjectiveProgress(player, QuestObjectiveType.HARVEST_CROPS, mat.name(), 1);
            }
        }

        plugin.getQuestManager().onObjectiveProgress(player, QuestObjectiveType.BREAK_BLOCK, mat.name(), 1);
        plugin.getQuestManager().onObjectiveProgress(player, QuestObjectiveType.MINE_BLOCK, mat.name(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();
        plugin.getQuestManager().onObjectiveProgress(player, QuestObjectiveType.PLACE_BLOCK, mat.name(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (event.getEntity() instanceof Player victim) {
            plugin.getQuestManager().onObjectiveProgress(killer, QuestObjectiveType.KILL_PLAYER, victim.getName(), 1);
        } else {
            String mobType = event.getEntityType().name();
            plugin.getQuestManager().onObjectiveProgress(killer, QuestObjectiveType.KILL_ENTITY, mobType, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            plugin.getQuestManager().onObjectiveProgress(event.getPlayer(), QuestObjectiveType.FISH, "FISH", 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (event.getRecipe() != null && event.getRecipe().getResult() != null) {
                Material mat = event.getRecipe().getResult().getType();
                int amt = event.getRecipe().getResult().getAmount();
                plugin.getQuestManager().onObjectiveProgress(player, QuestObjectiveType.CRAFT_ITEM, mat.name(), amt);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getItemType();
        int count = event.getItemAmount();
        plugin.getQuestManager().onObjectiveProgress(player, QuestObjectiveType.SMELT_ITEM, mat.name(), count);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getHarvestedBlock().getType();
        plugin.getQuestManager().onObjectiveProgress(player, QuestObjectiveType.HARVEST_CROPS, mat.name(), 1);
    }
}
