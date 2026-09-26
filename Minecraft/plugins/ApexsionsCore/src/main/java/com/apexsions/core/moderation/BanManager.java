package com.apexsions.core.moderation;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Authoritative Centralized Ban & Moderation Engine for Apexsions.
 * Synchronizes in-memory caches, SQLite/PostgreSQL database, Paper BanList, and Web Admin.
 */
public class BanManager {

    private final ApexsionsCorePlugin plugin;
    private final BanRepository repository;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<UUID, BanRecord> activeBansByUuid = new ConcurrentHashMap<>();
    private final Map<String, BanRecord> activeBansByIp = new ConcurrentHashMap<>();

    public BanManager(ApexsionsCorePlugin plugin, BanRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        loadActiveBans();
    }

    public void loadActiveBans() {
        repository.loadActiveBans().thenAccept(bans -> {
            activeBansByUuid.clear();
            activeBansByIp.clear();
            int count = 0;
            for (BanRecord ban : bans) {
                if (ban.isActive()) {
                    activeBansByUuid.put(ban.getPlayerUuid(), ban);
                    if (ban.getIpAddress() != null && !ban.getIpAddress().isBlank()) {
                        activeBansByIp.put(ban.getIpAddress(), ban);
                    }
                    count++;
                }
            }
            plugin.getLogger().info("[BanManager] Loaded " + count + " active ban records into memory.");
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "[BanManager] Failed to load active bans: " + ex.getMessage(), ex);
            return null;
        });
    }

    /**
     * Check if a player UUID is currently banned.
     */
    public boolean isBanned(UUID playerUuid) {
        if (playerUuid == null) return false;
        BanRecord record = activeBansByUuid.get(playerUuid);
        if (record == null) return false;
        if (record.isExpired()) {
            unbanInternal(record, "System (Expired)", "Masa hukuman telah habis.");
            return false;
        }
        return record.isActive();
    }

    /**
     * Check if an IP address is currently banned.
     */
    public boolean isIpBanned(String ip) {
        if (ip == null || ip.isBlank()) return false;
        BanRecord record = activeBansByIp.get(ip);
        if (record == null) return false;
        if (record.isExpired()) {
            unbanInternal(record, "System (Expired)", "Masa hukuman telah habis.");
            return false;
        }
        return record.isActive();
    }

    public BanRecord getActiveBan(UUID playerUuid) {
        if (playerUuid == null) return null;
        BanRecord record = activeBansByUuid.get(playerUuid);
        if (record != null && record.isExpired()) {
            unbanInternal(record, "System (Expired)", "Masa hukuman telah habis.");
            return null;
        }
        return record;
    }

    public BanRecord getActiveIpBan(String ip) {
        if (ip == null || ip.isBlank()) return null;
        BanRecord record = activeBansByIp.get(ip);
        if (record != null && record.isExpired()) {
            unbanInternal(record, "System (Expired)", "Masa hukuman telah habis.");
            return null;
        }
        return record;
    }

    /**
     * Execute a player ban (permanent or temporary, NAME or IP).
     */
    public CompletableFuture<BanRecord> ban(UUID playerUuid, String playerName, String ipAddress,
                                            String bannedBy, String reason, Duration duration,
                                            BanRecord.BanType banType) {
        Instant now = Instant.now();
        Instant expiresAt = duration != null ? now.plus(duration) : null;
        UUID banId = UUID.randomUUID();

        BanRecord record = new BanRecord(banId, playerUuid, playerName, ipAddress,
                bannedBy, reason, banType, now, expiresAt, true, null, null, null);

        // 1. In-memory update
        activeBansByUuid.put(playerUuid, record);
        if (ipAddress != null && !ipAddress.isBlank()) {
            activeBansByIp.put(ipAddress, record);
        }

        // 2. Synchronize Paper native BanList
        syncToNativeBanList(record);

        // 3. Kick player immediately if online
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player online = Bukkit.getPlayer(playerUuid);
            if (online != null && online.isOnline()) {
                online.kick(formatBanScreen(record));
            }
        });

        // 4. Staff announcement
        broadcastStaffBan(record);

        // 5. Persist to DB and sync WebBridge
        return repository.insertBan(record).thenApply(v -> {
            if (plugin.getWebBridgeService() != null) {
                Long durationSeconds = duration != null ? duration.getSeconds() : null;
                plugin.getWebBridgeService().syncPunishmentAsync(
                        playerUuid.toString(),
                        playerName,
                        "BAN",
                        reason,
                        bannedBy,
                        durationSeconds
                );
            }
            return record;
        });
    }

    /**
     * Pardon / Unban a player by identifier (UUID, username, or IP).
     */
    public CompletableFuture<Boolean> unban(String targetIdentifier, String unbannedBy, String unbanReason) {
        // Find existing active ban
        BanRecord target = null;
        try {
            UUID targetUuid = UUID.fromString(targetIdentifier);
            target = activeBansByUuid.get(targetUuid);
        } catch (IllegalArgumentException ignored) {}

        if (target == null) {
            for (BanRecord r : activeBansByUuid.values()) {
                if (r.getPlayerName().equalsIgnoreCase(targetIdentifier)) {
                    target = r;
                    break;
                }
            }
        }

        if (target == null) {
            target = activeBansByIp.get(targetIdentifier);
        }

        if (target == null) {
            // Also attempt to pardon native Bukkit banlist directly as fallback
            pardonNativeBanList(targetIdentifier);
            return CompletableFuture.completedFuture(false);
        }

        BanRecord banToPardon = target;
        unbanInternal(banToPardon, unbannedBy, unbanReason);

        // Notify staff
        Component staffNotice = mm.deserialize("<gray>[<gold>Apexsions Security</gold>] Sanksi ban untuk <yellow>" +
                banToPardon.getPlayerName() + "</yellow> dicabut oleh <gold>" + unbannedBy + "</gold>. Alasan: <white>" + unbanReason + "</white></gray>");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("apexsions.staff") || p.isOp()) {
                p.sendMessage(staffNotice);
            }
        }
        plugin.getLogger().info("[BanManager] Ban for " + banToPardon.getPlayerName() + " pardoned by " + unbannedBy + ": " + unbanReason);

        return CompletableFuture.completedFuture(true);
    }

    private void unbanInternal(BanRecord record, String unbannedBy, String unbanReason) {
        record.deactivate(unbannedBy, unbanReason);
        activeBansByUuid.remove(record.getPlayerUuid());
        if (record.getIpAddress() != null) {
            activeBansByIp.remove(record.getIpAddress());
        }

        pardonNativeBanList(record.getPlayerName());
        if (record.getIpAddress() != null) {
            pardonNativeBanList(record.getIpAddress());
        }

        repository.updateUnban(record.getId(), unbannedBy, unbanReason);
    }

    private void syncToNativeBanList(BanRecord record) {
        try {
            Date expDate = record.getExpiresAt() != null ? Date.from(record.getExpiresAt()) : null;
            if (record.getBanType() == BanRecord.BanType.IP && record.getIpAddress() != null) {
                Bukkit.getBanList(BanList.Type.IP).addBan(record.getIpAddress(), record.getReason(), expDate, record.getBannedBy());
            } else {
                Bukkit.getBanList(BanList.Type.NAME).addBan(record.getPlayerName(), record.getReason(), expDate, record.getBannedBy());
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "[BanManager] Unable to sync to native Bukkit BanList: " + t.getMessage());
        }
    }

    private void pardonNativeBanList(String target) {
        try {
            Bukkit.getBanList(BanList.Type.NAME).pardon(target);
        } catch (Throwable ignored) {}
        try {
            Bukkit.getBanList(BanList.Type.IP).pardon(target);
        } catch (Throwable ignored) {}
    }

    private void broadcastStaffBan(BanRecord record) {
        String durStr = record.isPermanent() ? "PERMANEN" : record.getTimeRemainingFormatted();
        Component msg = mm.deserialize("<red>[Apexsions Security]</red> <yellow>" + record.getPlayerName() +
                "</yellow> telah di-ban oleh <gold>" + record.getBannedBy() + "</gold> (<aqua>" + durStr +
                "</aqua>). Alasan: <white>" + record.getReason() + "</white>");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("apexsions.staff") || p.isOp()) {
                p.sendMessage(msg);
            }
        }
        plugin.getLogger().info("[BanManager] Player " + record.getPlayerName() + " banned by " + record.getBannedBy() + " (" + durStr + "): " + record.getReason());
    }

    /**
     * Render the cinematic disconnection screen for banned players.
     */
    public Component formatBanScreen(BanRecord record) {
        String durStr = record.isPermanent() ? "<red><bold>PERMANEN</bold></red>" : "<aqua>" + record.getTimeRemainingFormatted() + "</aqua>";
        String typeLabel = record.getBanType() == BanRecord.BanType.IP ? " (IP BAN)" : "";

        String raw = """
                <dark_red><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></dark_red>
                <gold><bold>A P E X S I O N S</bold></gold> <gray>— The Peak Civilizations</gray>
                <red><bold>AKSES MASUK REALM DITOLAK%s</bold></red>
                
                <gray>Pemain Terhukum:</gray> <yellow>%s</yellow>
                <gray>Otoritas Penegak:</gray> <gold>%s</gold>
                <gray>Alasan Sanksi:</gray> <white>%s</white>
                <gray>Durasi Sanksi:</gray> %s
                
                <gray>Pusat Regulasi & Pengajuan Banding Resmi:</gray>
                <gold><underlined>https://web.apexsions.com/rules</underlined></gold>
                <dark_red><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></dark_red>
                """.formatted(typeLabel, record.getPlayerName(), record.getBannedBy(), record.getReason(), durStr);

        return mm.deserialize(raw.trim());
    }

    public Map<UUID, BanRecord> getActiveBans() {
        return Collections.unmodifiableMap(activeBansByUuid);
    }
}
