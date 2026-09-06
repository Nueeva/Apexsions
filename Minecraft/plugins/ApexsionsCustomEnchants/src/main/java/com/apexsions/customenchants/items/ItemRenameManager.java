package com.apexsions.customenchants.items;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import com.apexsions.customenchants.gui.AdminItemCreatorGUI;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages in-chat text input sessions for item renaming, set naming, etc.
 */
public class ItemRenameManager implements Listener {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    public record RenameSession(
            Player player,
            String prompt,
            Consumer<String> onInput,
            Runnable onCancel
    ) {}

    private final Map<UUID, RenameSession> activeSessions = new ConcurrentHashMap<>();

    public ItemRenameManager(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
    }

    public void startSession(Player player, String prompt, Consumer<String> onInput, Runnable onCancel) {
        com.apexsions.customenchants.gui.input.EnchantsInputManager.openInput(
                plugin,
                player,
                "RENAME ITEM",
                prompt,
                "",
                plainText -> {
                    activeSessions.remove(player.getUniqueId());
                    String formatted = plainText;
                    if (plainText.contains("&")) {
                        Component c = legacySerializer.deserialize(plainText);
                        formatted = mm.serialize(c);
                    } else if (!plainText.contains("<") && !plainText.contains(">")) {
                        formatted = "<gold><bold>" + plainText + "</bold></gold>";
                    }
                    onInput.accept(formatted);
                },
                () -> {
                    activeSessions.remove(player.getUniqueId());
                    if (onCancel != null) onCancel.run();
                }
        );
    }

    public boolean hasActiveSession(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }

    public void cancelSession(UUID uuid) {
        RenameSession s = activeSessions.remove(uuid);
        if (s != null && s.onCancel != null) {
            Bukkit.getScheduler().runTask(plugin, s.onCancel);
        }
    }

    @org.bukkit.event.EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        RenameSession session = activeSessions.remove(event.getPlayer().getUniqueId());
        if (session != null && session.onCancel != null) {
            session.onCancel.run();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        RenameSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        String plainText = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (plainText.equalsIgnoreCase("cancel") || plainText.equalsIgnoreCase("batal")) {
            player.sendMessage(mm.deserialize("<red>Input nama dibatalkan.</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            if (session.onCancel != null) {
                Bukkit.getScheduler().runTask(plugin, session.onCancel);
            }
            return;
        }

        // Process color codes / formatting
        String formatted = plainText;
        if (plainText.contains("&")) {
            Component c = legacySerializer.deserialize(plainText);
            formatted = mm.serialize(c);
        } else if (!plainText.contains("<") && !plainText.contains(">")) {
            // Default color if no codes provided
            formatted = "<gold><bold>" + plainText + "</bold></gold>";
        }

        final String finalInput = formatted;
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                session.onInput.accept(finalInput);
            } catch (Exception e) {
                player.sendMessage(mm.deserialize("<red>Gagal memproses nama: " + e.getMessage() + "</red>"));
                if (session.onCancel != null) {
                    session.onCancel.run();
                }
            }
        });
    }
}
