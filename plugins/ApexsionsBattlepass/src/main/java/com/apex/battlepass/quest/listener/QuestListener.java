package com.apex.battlepass.quest.listener;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.quest.model.QuestObjectiveType;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class QuestListener implements Listener {

    private final ApexsionsBattlepass plugin;

    public QuestListener(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (event.getEntity() instanceof Player) {
            plugin.getQuestManager().incrementProgress(killer, QuestObjectiveType.KILL_PLAYER, event.getEntityType(), 1);
        } else {
            plugin.getQuestManager().incrementProgress(killer, QuestObjectiveType.KILL_ENTITY, event.getEntityType(), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.BREAK_BLOCK, mat, 1);
        plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.MINE_BLOCK, mat, 1);

        // Check if fully grown crop harvest
        if (event.getBlock().getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() >= ageable.getMaximumAge()) {
                plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.HARVEST_CROPS, mat, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.PLACE_BLOCK, mat, 1);

        // Check crop planting
        if (mat.name().contains("SEEDS") || mat.name().contains("SAPLING") || mat == Material.CARROT || mat == Material.POTATO || mat == Material.SUGAR_CANE || mat == Material.NETHER_WART) {
            plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.PLANT_CROPS, mat, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (event.getRecipe() != null && event.getRecipe().getResult() != null) {
                Material mat = event.getRecipe().getResult().getType();
                int amount = event.getRecipe().getResult().getAmount();
                plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.CRAFT_ITEM, mat, Math.max(1, amount));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getItemType();
        int amount = event.getItemAmount();
        plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.SMELT_ITEM, mat, Math.max(1, amount));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.FISH, null, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player player) {
            plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.BREED_ANIMALS, event.getEntityType(), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.ENCHANT_ITEM, event.getItem().getType(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsumeFood(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.CONSUME_FOOD, event.getItem().getType(), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpChange(PlayerExpChangeEvent event) {
        if (event.getAmount() > 0) {
            Player player = event.getPlayer();
            plugin.getQuestManager().incrementProgress(player, QuestObjectiveType.EXP_GAIN, null, event.getAmount());
        }
    }
}
