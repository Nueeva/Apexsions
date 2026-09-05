package com.apexsions.core.admin;

import com.apexsions.core.ApexsionsCorePlugin;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Thread-safe asynchronous chat input manager for admin GUI custom value inputs.
 */
public class AdminChatInputManager implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, InputSession> activeSessions = new ConcurrentHashMap<>();

    public AdminChatInputManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void startSession(Player admin, String promptMessage, Consumer<String> onInput, Runnable onCancel) {
        // Cancel existing session if any
        cancelSession(admin.getUniqueId(), false);

        admin.closeInventory();
        admin.sendMessage(mm.deserialize("<gold><bold>════════════════ [ APEXSIONS INPUT ] ════════════════</bold></gold>"));
        admin.sendMessage(mm.deserialize("<yellow>" + promptMessage + "</yellow>"));
        admin.sendMessage(mm.deserialize("<gray>Ketik nilaimu di chat sekarang. Ketik <red><bold>cancel</bold></red> untuk membatalkan.</gray>"));
        admin.sendMessage(mm.deserialize("<dark_gray>Sesi akan kedaluwarsa secara otomatis dalam 30 detik.</dark_gray>"));
        admin.sendMessage(mm.deserialize("<gold><bold>═════════════════════════════════════════════════════</bold></gold>"));
        admin.playSound(admin.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeSessions.containsKey(admin.getUniqueId())) {
                activeSessions.remove(admin.getUniqueId());
                if (admin.isOnline()) {
                    admin.sendMessage(mm.deserialize("<red>⏱ Sesi input chat telah kedaluwarsa (timeout 30 detik).</red>"));
                    admin.playSound(admin.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    if (onCancel != null) onCancel.run();
                }
            }
        }, 30 * 20L); // 30 seconds

        activeSessions.put(admin.getUniqueId(), new InputSession(onInput, onCancel, timeoutTask));
    }

    public void cancelSession(UUID uuid, boolean notify) {
        InputSession session = activeSessions.remove(uuid);
        if (session != null) {
            session.timeoutTask.cancel();
            if (notify) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage(mm.deserialize("<yellow>✖ Sesi input dibatalkan.</yellow>"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                }
            }
            if (session.onCancel != null) {
                Bukkit.getScheduler().runTask(plugin, session.onCancel);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        InputSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        activeSessions.remove(player.getUniqueId());
        session.timeoutTask.cancel();

        String rawText = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (rawText.equalsIgnoreCase("cancel") || rawText.equalsIgnoreCase("batal")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(mm.deserialize("<yellow>✖ Input dibatalkan oleh admin.</yellow>"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                if (session.onCancel != null) session.onCancel.run();
            });
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                session.onInput.accept(rawText);
            } catch (Exception e) {
                player.sendMessage(mm.deserialize("<red>Terjadi kesalahan saat memproses input: " + e.getMessage() + "</red>"));
                if (session.onCancel != null) session.onCancel.run();
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelSession(event.getPlayer().getUniqueId(), false);
    }

    private record InputSession(Consumer<String> onInput, Runnable onCancel, BukkitTask timeoutTask) {}
}
