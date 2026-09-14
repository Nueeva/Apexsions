package com.apexsions.fishing.listener;

import com.apexsions.fishing.ApexsionsFishing;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RodLevelRestrictionListener implements Listener {

    private final ApexsionsFishing plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, Long> messageCooldown = new ConcurrentHashMap<>();

    public RodLevelRestrictionListener(ApexsionsFishing plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FISHING_ROD) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission("apexsions.fishing.bypasslevel")) {
            return;
        }

        int reqLevel = plugin.getRodManager().getMinLevel(item);
        if (reqLevel <= 0) return;

        int playerLevel = plugin.getPlayerCoreLevel(player);
        if (playerLevel < reqLevel) {
            event.setCancelled(true);
            sendFeedback(player, reqLevel);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission("apexsions.fishing.bypasslevel")) {
            return;
        }

        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() != Material.FISHING_ROD) {
            rod = player.getInventory().getItemInOffHand();
        }

        int reqLevel = plugin.getRodManager().getMinLevel(rod);
        if (reqLevel <= 0) return;

        int playerLevel = plugin.getPlayerCoreLevel(player);
        if (playerLevel < reqLevel) {
            event.setCancelled(true);
            sendFeedback(player, reqLevel);
        }
    }

    private void sendFeedback(Player player, int reqLevel) {
        long now = System.currentTimeMillis();
        long last = messageCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - last > 1500) {
            messageCooldown.put(player.getUniqueId(), now);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(mm.deserialize("<red><bold>LEVEL TIDAK MENCUKUPI!</bold></red> <gray>Anda membutuhkan minimal <yellow>Level " + reqLevel + "</yellow> untuk menggunakan pancingan ini!</gray>"));
        }
    }
}
