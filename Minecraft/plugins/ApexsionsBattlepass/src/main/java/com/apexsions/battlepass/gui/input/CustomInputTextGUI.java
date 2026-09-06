package com.apexsions.battlepass.gui.input;

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
 * Custom Input Text GUI for Apexsions.
 * Uses the exact same Native Minecraft Dialog input screen as seen in ExcellentCrates.
 * Fully supports Minecraft 26.2, NightCore Dialog API, Paper Dialog API, Spigot/Bungee Dialog API,
 * and Bedrock Edition (Floodgate Modal Form).
 *
 * NEVER uses chest keypad slots or anvil inventories.
 */
public class CustomInputTextGUI {

    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final ConcurrentHashMap<UUID, ChatFallbackSession> pendingChatFallbacks = new ConcurrentHashMap<>();
    private static volatile boolean listenerRegistered = false;

    private final Plugin plugin;
    private final Player player;
    private final String title;
    private final String prompt;
    private final String defaultText;
    private final Consumer<String> onInput;
    private final Runnable onCancel;

    public CustomInputTextGUI(Plugin plugin, Player player, String title, String prompt, String defaultText,
                              Consumer<String> onInput, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.prompt = prompt;
        this.defaultText = defaultText;
        this.onInput = onInput;
        this.onCancel = onCancel;
    }

    public CustomInputTextGUI(Plugin plugin, Player player, String title, String prompt, String defaultText,
                              Consumer<String> onInput) {
        this(plugin, player, title, prompt, defaultText, onInput, null);
    }

    public void open() {
        open(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    public static void open(Plugin plugin, Player player, String title, String prompt, String defaultText,
                            Consumer<String> onInput, Runnable onCancel) {
        if (player == null || !player.isOnline()) return;

        // 1. Bedrock Edition (Geyser / Floodgate Modal Form)
        if (BedrockFormAdapter.isBedrockPlayer(player)) {
            if (BedrockFormAdapter.openInputForm(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        // 2. Java Edition 26.2 Dialogs (NightCore / Paper / Bungee)
        if (NativeDialogAdapter.isSupported()) {
            if (NativeDialogAdapter.showInput(plugin, player, title, prompt, defaultText, onInput, onCancel)) {
                return;
            }
        }

        // 3. Fallback: Clean Chat Prompt (No Anvil Inventory)
        openChatFallback(plugin, player, title, prompt, defaultText, onInput, onCancel);
    }

    public static void open(Plugin plugin, Player player, String title, String prompt, String defaultText,
                            Consumer<String> onInput) {
        open(plugin, player, title, prompt, defaultText, onInput, null);
    }

    public static void openNumeric(Plugin plugin, Player player, String title, String prompt, int defaultValue,
                                   int min, int max, Consumer<Integer> onNumber, Runnable onCancel) {
        open(plugin, player, title, prompt + " (" + min + " - " + max + ")", String.valueOf(defaultValue), input -> {
            try {
                int val = Integer.parseInt(input.trim());
                if (val < min || val > max) {
                    player.sendMessage(mm.deserialize("<red>Angka harus berada di antara " + min + " dan " + max + "!</red>"));
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage(mm.deserialize("<red>Input bukan angka yang valid!</red>"));
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }

    public static void openDouble(Plugin plugin, Player player, String title, String prompt, double defaultValue,
                                  double min, double max, Consumer<Double> onNumber, Runnable onCancel) {
        open(plugin, player, title, prompt, String.valueOf(defaultValue), input -> {
            try {
                double val = Double.parseDouble(input.trim().replace(',', '.'));
                if (val < min || val > max) {
                    player.sendMessage(mm.deserialize("<red>Nilai harus berada di antara " + min + " dan " + max + "!</red>"));
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onNumber.accept(val);
            } catch (NumberFormatException e) {
                player.sendMessage(mm.deserialize("<red>Input bukan angka desimal yang valid!</red>"));
                if (onCancel != null) onCancel.run();
            }
        }, onCancel);
    }

    private static void openChatFallback(Plugin plugin, Player player, String title, String prompt, String defaultText,
                                         Consumer<String> onInput, Runnable onCancel) {
        ensureListener(plugin);
        pendingChatFallbacks.put(player.getUniqueId(), new ChatFallbackSession(plugin, onInput, onCancel));

        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize("<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold>"));
        player.sendMessage(mm.deserialize("<yellow><b>" + (title != null ? title : "INPUT") + "</b></yellow>"));
        player.sendMessage(mm.deserialize("<gray>" + (prompt != null ? prompt : "Silakan ketik jawaban Anda di chat:") + "</gray>"));
        if (defaultText != null && !defaultText.isBlank()) {
            player.sendMessage(mm.deserialize("<dark_gray>Default: " + defaultText + "</dark_gray>"));
        }
        Component cancelBtn = mm.deserialize("<red><b>[BATALKAN INPUT]</b></red>")
                .clickEvent(ClickEvent.runCommand("/apexsionscancelinput"));
        player.sendMessage(cancelBtn);
        player.sendMessage(mm.deserialize("<gold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gold>"));
    }

    private static synchronized void ensureListener(Plugin plugin) {
        if (!listenerRegistered) {
            listenerRegistered = true;
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.LOWEST)
                public void onChat(AsyncPlayerChatEvent e) {
                    ChatFallbackSession session = pendingChatFallbacks.remove(e.getPlayer().getUniqueId());
                    if (session != null) {
                        e.setCancelled(true);
                        String msg = e.getMessage().trim();
                        if (msg.equalsIgnoreCase("batal") || msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase("/apexsionscancelinput")) {
                            if (session.onCancel != null) {
                                Bukkit.getScheduler().runTask(session.plugin, session.onCancel);
                            }
                            return;
                        }
                        Bukkit.getScheduler().runTask(session.plugin, () -> {
                            try {
                                session.onInput.accept(msg);
                            } catch (Throwable t) {
                                e.getPlayer().sendMessage("§cError: " + t.getMessage());
                            }
                        });
                    }
                }
            }, plugin);
        }
    }

    private record ChatFallbackSession(Plugin plugin, Consumer<String> onInput, Runnable onCancel) {}
}
