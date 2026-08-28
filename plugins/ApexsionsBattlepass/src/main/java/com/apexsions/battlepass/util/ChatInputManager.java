package com.apexsions.battlepass.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {

    private final Plugin plugin;
    private final Map<UUID, ChatInputSession> activeSessions = new ConcurrentHashMap<>();

    public static class ChatInputSession {
        private final Player player;
        private final String prompt;
        private final Consumer<String> onInput;
        private final Runnable onCancel;
        private final long expiresAt;

        public ChatInputSession(Player player, String prompt, Consumer<String> onInput, Runnable onCancel, long timeoutSeconds) {
            this.player = player;
            this.prompt = prompt;
            this.onInput = onInput;
            this.onCancel = onCancel;
            this.expiresAt = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public ChatInputManager(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Cleanup task every 10 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            activeSessions.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    if (p != null && p.isOnline()) {
                        p.sendMessage("§c[!] Sesi input chat telah kedaluwarsa.");
                    }
                    if (entry.getValue().onCancel != null) {
                        entry.getValue().onCancel.run();
                    }
                    return true;
                }
                return false;
            });
        }, 200L, 200L);
    }

    public void startInput(Player player, String prompt, Consumer<String> onInput, Runnable onCancel) {
        startInput(player, prompt, onInput, onCancel, 60);
    }

    public void startInput(Player player, String prompt, Consumer<String> onInput, Runnable onCancel, long timeoutSeconds) {
        player.closeInventory();
        activeSessions.put(player.getUniqueId(), new ChatInputSession(player, prompt, onInput, onCancel, timeoutSeconds));
        player.sendMessage("§8=======================================");
        player.sendMessage("§6§lINPUT REQUIRED:");
        player.sendMessage("§e" + prompt);
        player.sendMessage("§7Ketik jawaban Anda di chat, atau ketik §c'batal' §7untuk membatalkan.");
        player.sendMessage("§8=======================================");
    }

    public void startNumericInput(Player player, String prompt, Consumer<Integer> onNumber, Runnable onCancel, int min, int max) {
        startInput(player, prompt, input -> {
            try {
                int val = Integer.parseInt(input.trim());
                if (val < min || val > max) {
                    player.sendMessage("§cAngka harus berada di antara " + min + " dan " + max + ".");
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInput tidak valid! Harap masukkan angka yang benar.");
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }

    public void startDoubleInput(Player player, String prompt, Consumer<Double> onNumber, Runnable onCancel, double min, double max) {
        startInput(player, prompt, input -> {
            try {
                double val = Double.parseDouble(input.trim().replace(',', '.'));
                if (val < min || val > max) {
                    player.sendMessage("§cAngka harus berada di antara " + min + " dan " + max + ".");
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInput tidak valid! Harap masukkan angka yang benar.");
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatInputSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        String message = event.getMessage().trim();

        if (session.isExpired()) {
            player.sendMessage("§c[!] Sesi input chat telah kedaluwarsa.");
            if (session.onCancel != null) {
                Bukkit.getScheduler().runTask(plugin, session.onCancel);
            }
            return;
        }

        if (message.equalsIgnoreCase("batal") || message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("exit")) {
            player.sendMessage("§e[!] Sesi input dibatalkan.");
            if (session.onCancel != null) {
                Bukkit.getScheduler().runTask(plugin, session.onCancel);
            }
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                session.onInput.accept(message);
            } catch (Exception ex) {
                player.sendMessage("§cTerjadi kesalahan saat memproses input: " + ex.getMessage());
                if (session.onCancel != null) session.onCancel.run();
            }
        });
    }
}
