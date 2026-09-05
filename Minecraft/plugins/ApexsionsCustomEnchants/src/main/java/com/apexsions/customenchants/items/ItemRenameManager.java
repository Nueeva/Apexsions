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
        activeSessions.put(player.getUniqueId(), new RenameSession(player, prompt, onInput, onCancel));
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
        player.sendMessage(mm.deserialize("<gold><bold>🏷 INPUT NAMA DI CHAT</bold></gold>"));
        player.sendMessage(mm.deserialize("<gray>" + prompt + "</gray>"));
        player.sendMessage(mm.deserialize("<yellow>Mendukung kode warna <aqua>&a&l</aqua> atau tag MiniMessage <aqua><gold><bold></aqua>.</yellow>"));
        player.sendMessage(mm.deserialize("<gray>Ketik <red><bold>cancel</bold></red> untuk membatalkan.</gray>"));
        player.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gradient>"));
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
