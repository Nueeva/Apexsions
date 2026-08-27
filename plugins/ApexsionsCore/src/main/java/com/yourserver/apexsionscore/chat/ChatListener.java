package com.yourserver.apexsionscore.chat;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listens to Paper AsyncChatEvent and renders chat with KingdomCore format.
 */
public class ChatListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final ChatFormatter formatter;

    public ChatListener(ApexsionsCorePlugin plugin, ChatFormatter formatter) {
        this.plugin = plugin;
        this.formatter = formatter;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getConfigManager().isChatEnabled()) {
            return;
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> formatter.format(source, message));
    }
}
