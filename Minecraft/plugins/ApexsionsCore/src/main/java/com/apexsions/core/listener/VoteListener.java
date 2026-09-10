package com.apexsions.core.listener;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Dynamic event listener for Votifier / NuVotifier.
 * Soft-depends on Votifier: if NuVotifier is installed, it dynamically registers
 * and delivers 3x Vote Keys + Rp 1.000, then syncs with WebBridge asynchronously.
 */
public class VoteListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public VoteListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register dynamic listener for NuVotifier / Votifier if available on runtime classpath.
     */
    public void registerIfAvailable() {
        try {
            Class<?> eventClass = Class.forName("com.vexsoftware.votifier.model.VotifierEvent");
            @SuppressWarnings("unchecked")
            Class<? extends Event> voteEventClass = (Class<? extends Event>) eventClass;

            EventExecutor executor = (listener, event) -> {
                if (eventClass.isInstance(event)) {
                    handleVoteEvent(event);
                }
            };

            Bukkit.getPluginManager().registerEvent(
                    voteEventClass,
                    this,
                    EventPriority.NORMAL,
                    executor,
                    plugin
            );
            plugin.getLogger().info("[VoteListener] NuVotifier integration activated: VotifierEvent registered.");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().fine("[VoteListener] NuVotifier not present on classpath. Inbound votes will use WebBridge queue/webhook.");
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "[VoteListener] Failed to hook VotifierEvent: " + t.getMessage());
        }
    }

    private void handleVoteEvent(Object event) {
        try {
            Method getVoteMethod = event.getClass().getMethod("getVote");
            Object vote = getVoteMethod.invoke(event);
            if (vote == null) return;

            Class<?> voteClass = vote.getClass();
            String username = (String) voteClass.getMethod("getUsername").invoke(vote);
            String serviceName = (String) voteClass.getMethod("getServiceName").invoke(vote);
            String address = (String) voteClass.getMethod("getAddress").invoke(vote);
            String timestamp = (String) voteClass.getMethod("getTimeStamp").invoke(vote);

            if (username == null || username.trim().isEmpty()) {
                return;
            }

            username = username.trim();
            plugin.getLogger().info("[VoteListener] Received Votifier vote for '" + username + "' from " + serviceName);

            // Execute auto-reward in-game on Bukkit main thread
            String targetUser = username;
            Bukkit.getScheduler().runTask(plugin, () -> {
                // 1. Give 3x Vote Crate Keys (handles offline players via keysOnHold)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crates key give " + targetUser + " vote 3");

                // 2. Give Rp 1.000 currency (handles offline players via database balance)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecoadmin give " + targetUser + " 1000 rupiah");

                // 3. In-game notification if online
                Player player = Bukkit.getPlayerExact(targetUser);
                if (player != null && player.isOnline()) {
                    Component alert = mm.deserialize(
                            "<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold>\n" +
                            "<yellow><bold>           APEXSIONS VOTE REWARD</bold></yellow>\n" +
                            "<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold>\n" +
                            "<white>Terima kasih telah mendukung kedaulatan Apexsions!</white>\n\n" +
                            "<gold><bold>+ 3x Vote Crate Keys</bold></gold>\n" +
                            "<green><bold>+ Rp 1.000 Saldo Peradaban</bold></green>\n\n" +
                            "<gray>Gunakan </gray><aqua><bold>/vote</bold></aqua><gray> untuk mendukung server kembali.</gray>\n" +
                            "<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold>"
                    );
                    player.sendMessage(alert);
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }

                // 4. Report to WebBridge database asynchronously
                if (plugin.getWebBridgeService() != null) {
                    plugin.getWebBridgeService().reportInboundVoteAsync(targetUser, serviceName, address, timestamp);
                }
            });

        } catch (Throwable t) {
            plugin.getLogger().warning("[VoteListener] Error processing Votifier event: " + t.getMessage());
        }
    }
}
