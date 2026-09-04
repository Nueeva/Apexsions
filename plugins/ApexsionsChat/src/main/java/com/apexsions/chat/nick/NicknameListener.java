package com.apexsions.chat.nick;

import com.apexsions.chat.ApexsionsChatPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Event listener handling player join/quit lifecycle for nicknames and token voucher redemption.
 */
public class NicknameListener implements Listener {

    private final ApexsionsChatPlugin plugin;

    public NicknameListener(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getNicknameService().loadPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getNicknameService().unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        if (plugin.getNicknameService().isTokenItem(item)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            plugin.getNicknameService().redeemTokenItem(player, item);
        }
    }
}
