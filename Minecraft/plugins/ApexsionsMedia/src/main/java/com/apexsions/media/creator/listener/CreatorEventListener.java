package com.apexsions.media.creator.listener;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.gui.CreatorHubGUI;
import com.apexsions.media.creator.gui.CreatorTiersGUI;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CreatorEventListener implements Listener {

    private final ApexsionsMediaPlugin plugin;

    public CreatorEventListener(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        if (plugin.getChatInputSessionManager().hasActiveSession(player.getUniqueId())) {
            e.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(e.message());
            plugin.getChatInputSessionManager().handleChatInput(player, message);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof CreatorHubGUI hub) {
            hub.handleClick(e);
        } else if (e.getInventory().getHolder() instanceof CreatorTiersGUI tiers) {
            tiers.handleClick(e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.getChatInputSessionManager().cancelSession(e.getPlayer().getUniqueId());
    }
}
