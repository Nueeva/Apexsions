package com.apexsions.fishing.gui.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Universal Input GUI for ApexsionsFishing.
 * Integrates Native Dialog GUI (NightCore / Paper / Bungee) with a seamless, clean chat prompt fallback.
 */
public class FishingInputGUI {

    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final ConcurrentHashMap<UUID, ChatFallbackSession> pendingChatSessions = new ConcurrentHashMap<>();
    private static volatile boolean listenerRegistered = false;

    public static void open(Plugin plugin, Player player, String title, String prompt, String defaultText,
                            Consumer<String> onInput, Runnable onCancel) {
        if (player == null || !player.isOnline()) return;

        // Try Native Dialog GUI first
        if (NativeDialogAdapter.isSupported()) {
            if (NativeDialogAdapter.showInput(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        // Fallback to chat prompt
        openChatFallback(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    public static void openNumeric(Plugin plugin, Player player, String title, String prompt, int defaultValue,
                                   int min, int max, Consumer<Integer> onNumber, Runnable onCancel) {
        open(plugin, player, title, prompt + " <gray>(" + min + " - " + max + ")</gray>", String.valueOf(defaultValue), input -> {
            try {
                int val = Integer.parseInt(input.trim());
                if (val < min || val > max) {
                    player.sendMessage(mm.deserialize("<red>Nilai angka harus berada di antara " + min + " dan " + max + "!</red>"));
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage(mm.deserialize("<red>Input '" + input + "' bukan angka bilangan bulat yang valid!</red>"));
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }

    public static void openDouble(Plugin plugin, Player player, String title, String prompt, double defaultValue,
                                  double min, double max, Consumer<Double> onNumber, Runnable onCancel) {
        open(plugin, player, title, prompt + " <gray>(" + min + " - " + max + ")</gray>", String.valueOf(defaultValue), input -> {
            try {
                String sanitized = input.trim().replace(",", ".");
                double val = Double.parseDouble(sanitized);
                if (val < min || val > max) {
                    player.sendMessage(mm.deserialize("<red>Nilai desimal harus berada di antara " + min + " dan " + max + "!</red>"));
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage(mm.deserialize("<red>Input '" + input + "' bukan angka desimal yang valid!</red>"));
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }

    private static void openChatFallback(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                         Consumer<String> onInput, Runnable onCancel) {
        ensureListener(plugin);
        pendingChatSessions.put(player.getUniqueId(), new ChatFallbackSession(player.getUniqueId(), onInput, onCancel));
        player.closeInventory();

        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize("<gradient:#00c6ff:#0072ff><bold>════════════════════════════════════════</bold></gradient>"));
        player.sendMessage(mm.deserialize(" <yellow><bold>" + title + "</bold></yellow>"));
        player.sendMessage(mm.deserialize(" <gray>" + prompt + "</gray>"));
        if (defaultText != null && !defaultText.isBlank()) {
            player.sendMessage(mm.deserialize(" <dark_gray>Nilai saat ini: " + defaultText + "</dark_gray>"));
        }
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize(" <white>Ketik jawaban Anda di chat.</white> <gray>Atau ketik <red><bold>cancel</bold></red> untuk membatalkan.</gray>"));
        player.sendMessage(mm.deserialize("<gradient:#00c6ff:#0072ff><bold>════════════════════════════════════════</bold></gradient>"));
        player.sendMessage(Component.empty());
    }

    private static synchronized void ensureListener(Plugin plugin) {
        if (listenerRegistered) return;
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onChat(AsyncPlayerChatEvent event) {
                Player p = event.getPlayer();
                ChatFallbackSession session = pendingChatSessions.remove(p.getUniqueId());
                if (session != null) {
                    event.setCancelled(true);
                    String msg = event.getMessage().trim();
                    if (msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase("batal")) {
                        p.sendMessage(mm.deserialize("<gray>Aksi pengubahan nilai dibatalkan.</gray>"));
                        if (session.onCancel != null) {
                            Bukkit.getScheduler().runTask(plugin, session.onCancel);
                        }
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> session.onInput.accept(msg));
                    }
                }
            }
        }, plugin);
        listenerRegistered = true;
    }

    private record ChatFallbackSession(UUID uuid, Consumer<String> onInput, Runnable onCancel) {}
}
