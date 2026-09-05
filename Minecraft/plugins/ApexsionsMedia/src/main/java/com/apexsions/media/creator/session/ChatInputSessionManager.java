package com.apexsions.media.creator.session;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.model.Platform;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputSessionManager {

    private final ApexsionsMediaPlugin plugin;
    private final Map<UUID, InputSession> activeSessions = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public enum SessionType {
        SUBMIT_VIDEO,
        LINK_YOUTUBE,
        LINK_TIKTOK
    }

    public record InputSession(SessionType type, Platform platform, long expiry, Consumer<String> callback) {}

    public ChatInputSessionManager(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startSession(Player player, SessionType type, Platform platform, String promptText, Consumer<String> callback) {
        long expiry = System.currentTimeMillis() + 60_000L; // 60 seconds timeout
        activeSessions.put(player.getUniqueId(), new InputSession(type, platform, expiry, callback));

        player.closeInventory();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        player.sendMessage(mm.deserialize(
                "\n<gradient:#3498db:#2ecc71><bold>✦ SESI INPUT KREATOR ✦</bold></gradient>\n" +
                promptText + "\n" +
                "<gray>Ketik <red>cancel</red> di chat untuk membatalkan sesi ini (Batas waktu: 60 detik).</gray>\n"
        ));
    }

    public boolean hasActiveSession(UUID uuid) {
        InputSession session = activeSessions.get(uuid);
        if (session == null) return false;
        if (System.currentTimeMillis() > session.expiry()) {
            activeSessions.remove(uuid);
            return false;
        }
        return true;
    }

    public boolean handleChatInput(Player player, String message) {
        UUID uuid = player.getUniqueId();
        InputSession session = activeSessions.remove(uuid);
        if (session == null) return false;

        if (System.currentTimeMillis() > session.expiry()) {
            player.sendMessage(mm.deserialize("<red><b>[Creator]</b> Sesi input telah kedaluwarsa.</red>"));
            return true;
        }

        String trimmed = message.trim();
        if (trimmed.equalsIgnoreCase("cancel") || trimmed.equalsIgnoreCase("batal")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            player.sendMessage(mm.deserialize("<yellow><b>[Creator]</b> Sesi input dibatalkan.</yellow>"));
            return true;
        }

        try {
            session.callback().accept(trimmed);
        } catch (Exception e) {
            player.sendMessage(mm.deserialize("<red>Terjadi kesalahan saat memproses input: " + e.getMessage() + "</red>"));
        }
        return true;
    }

    public void cancelSession(UUID uuid) {
        activeSessions.remove(uuid);
    }
}
