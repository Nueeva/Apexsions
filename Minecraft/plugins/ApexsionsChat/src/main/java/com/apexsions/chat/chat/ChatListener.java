package com.apexsions.chat.chat;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.channel.ChatChannel;
import com.apexsions.chat.moderation.ModerationResult;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatListener(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        // 1. Check if message is a valid answer for an active chat game
        if (plugin.getGameManager() != null && plugin.getGameManager().checkAnswer(player, rawMessage)) {
            // Player answered the chat game correctly!
            event.setCancelled(true);
            return;
        }

        // 2. Resolve target channel
        ChatChannel channel = plugin.getChannelManager().getPlayerChannel(player);
        if (!channel.canSpeak(player)) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to speak in the " + channel.getName() + " channel.</red>"));
            event.setCancelled(true);
            return;
        }

        // 3. Moderation Pipeline (Spam, Ads, Profanity, Hate Speech)
        ModerationResult modResult = plugin.getModerationEngine().process(player, rawMessage, channel.getId());
        if (modResult.isBlocked()) {
            player.sendMessage(miniMessage.deserialize("<red>✖ Message blocked: <dark_red>" + modResult.getReason() + "</dark_red></red>"));
            event.setCancelled(true);
            return;
        }

        String finalMessage = modResult.getMessage() != null ? modResult.getMessage() : rawMessage;

        // 4. Format chat message with rich components, domain metadata, badges, and mentions
        Component formattedComponent = plugin.getChatFormatter().format(player, channel, finalMessage);

        // 5. Apply channel recipient filtering
        event.viewers().removeIf(audience -> {
            if (audience instanceof Player recipient) {
                return !channel.canReceive(recipient, player);
            }
            return false;
        });

        // 6. Set custom renderer
        event.renderer((source, sourceDisplayName, message, viewer) -> formattedComponent);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 1. Luxury Join Message with MiniMessage
        if (plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.enabled", true)) {
            String rank = "<gray>[Wanderer]</gray>";
            if (plugin.getApexsionsCoreHook() != null && plugin.getApexsionsCoreHook().isAvailable()) {
                var prof = plugin.getApexsionsCoreHook().getPlayerChatProfile(uuid);
                if (prof != null) rank = prof.rank();
            } else if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
                rank = plugin.getLuckPermsHook().getPlayerRank(player);
            }
            event.joinMessage(miniMessage.deserialize(
                    "<dark_gray>[</dark_gray><green><bold>+</bold></green><dark_gray>]</dark_gray> " + rank + " <white><bold>" + player.getName() + "</bold></white> <gray>bergabung ke server</gray>"
            ));
        }

        // 2. Check unread offline mail asynchronously
        plugin.getMailRepository().countUnreadMailAsync(uuid).thenAccept(unreadCount -> {
            if (unreadCount > 0 && player.isOnline()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendMessage(miniMessage.deserialize(
                                "<gold>📬 You have <yellow><bold>" + unreadCount + "</bold></yellow> unread offline message(s)! Type <yellow><underlined>/mail</underlined></yellow> to view.</gold>"
                        ));
                        try {
                            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                        } catch (Exception ignored) {}
                    }
                }, 40L); // 2 seconds after join
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.enabled", true)) {
            String rank = "<gray>[Wanderer]</gray>";
            if (plugin.getApexsionsCoreHook() != null && plugin.getApexsionsCoreHook().isAvailable()) {
                var prof = plugin.getApexsionsCoreHook().getPlayerChatProfile(uuid);
                if (prof != null) rank = prof.rank();
            } else if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
                rank = plugin.getLuckPermsHook().getPlayerRank(player);
            }
            event.quitMessage(miniMessage.deserialize(
                    "<dark_gray>[</dark_gray><red><bold>-</bold></red><dark_gray>]</dark_gray> " + rank + " <white><bold>" + player.getName() + "</bold></white> <gray>meninggalkan server</gray>"
            ));
        }
    }
}
