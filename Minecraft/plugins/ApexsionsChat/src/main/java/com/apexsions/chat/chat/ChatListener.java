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

    private final java.util.Set<UUID> announcedJoins = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinPre(PlayerJoinEvent event) {
        // Nullify join message at LOWEST so other delayed-join plugins (like AuthMe) capture null
        if (plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.enabled", true)
                && plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.delay-until-login", true)
                && plugin.getAuthMeHook() != null && plugin.getAuthMeHook().isAvailable()
                && !plugin.getAuthMeHook().isAuthenticated(event.getPlayer())) {
            event.joinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.enabled", true)) {
            event.joinMessage(null);
            checkUnreadMail(player);
            return;
        }

        boolean delayUntilLogin = plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.delay-until-login", true);
        if (delayUntilLogin && plugin.getAuthMeHook() != null && plugin.getAuthMeHook().isAvailable()) {
            if (!plugin.getAuthMeHook().isAuthenticated(player)) {
                // Player has not yet authenticated via AuthMe; suppress join message until login
                event.joinMessage(null);
                return;
            }
        }

        // Already authenticated or AuthMe not active: announce immediately
        broadcastJoin(player, event);
    }

    public void broadcastJoin(Player player) {
        broadcastJoin(player, null);
    }

    public void broadcastJoin(Player player, PlayerJoinEvent event) {
        if (player == null || !player.isOnline()) return;
        UUID uuid = player.getUniqueId();

        if (!announcedJoins.add(uuid)) {
            return; // Already announced in this session
        }

        if (plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.enabled", true)) {
            String rank = "<gray>[Wanderer]</gray>";
            String kingdom = "Belum Memilih";
            if (plugin.getApexsionsCoreHook() != null && plugin.getApexsionsCoreHook().isAvailable()) {
                var prof = plugin.getApexsionsCoreHook().getPlayerChatProfile(uuid);
                if (prof != null) {
                    rank = prof.rank();
                    kingdom = prof.kingdomDisplayName();
                }
            } else if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
                rank = plugin.getLuckPermsHook().getPlayerRank(player);
            }

            String template = plugin.getConfigManager().getMainConfig().getString(
                    "join-quit-messages.join-format",
                    "<dark_gray>[</dark_gray><green><bold>+</bold></green><dark_gray>]</dark_gray> {rank} <white><bold>{player}</bold></white> <gray>bergabung ke server</gray>"
            );
            String formatted = template
                    .replace("{rank}", rank)
                    .replace("{player}", player.getName())
                    .replace("{kingdom}", kingdom);

            Component joinComp = miniMessage.deserialize(formatted);
            if (event != null) {
                event.joinMessage(joinComp);
            } else {
                Bukkit.broadcast(joinComp);
            }
        }

        checkUnreadMail(player);
    }

    private void checkUnreadMail(Player player) {
        UUID uuid = player.getUniqueId();
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
                }, 40L); // 2 seconds after join announcement
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean wasAnnounced = announcedJoins.remove(uuid);

        // If player never authenticated and delayed join is active, silence quit broadcast
        if (plugin.getAuthMeHook() != null && plugin.getAuthMeHook().isAvailable()) {
            if (!plugin.getAuthMeHook().isAuthenticated(player) && !wasAnnounced) {
                event.quitMessage(null);
                return;
            }
        }

        if (plugin.getConfigManager().getMainConfig().getBoolean("join-quit-messages.enabled", true)) {
            String rank = "<gray>[Wanderer]</gray>";
            String kingdom = "Belum Memilih";
            if (plugin.getApexsionsCoreHook() != null && plugin.getApexsionsCoreHook().isAvailable()) {
                var prof = plugin.getApexsionsCoreHook().getPlayerChatProfile(uuid);
                if (prof != null) {
                    rank = prof.rank();
                    kingdom = prof.kingdomDisplayName();
                }
            } else if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
                rank = plugin.getLuckPermsHook().getPlayerRank(player);
            }

            String template = plugin.getConfigManager().getMainConfig().getString(
                    "join-quit-messages.quit-format",
                    "<dark_gray>[</dark_gray><red><bold>-</bold></red><dark_gray>]</dark_gray> {rank} <white><bold>{player}</bold></white> <gray>meninggalkan server</gray>"
            );
            String formatted = template
                    .replace("{rank}", rank)
                    .replace("{player}", player.getName())
                    .replace("{kingdom}", kingdom);

            event.quitMessage(miniMessage.deserialize(formatted));
        }
    }
}
