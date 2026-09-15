package com.apexsions.core.moderation;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable/mutable representation of a player ban record in the Apexsions ecosystem.
 */
public class BanRecord {

    public enum BanType {
        NAME,
        IP
    }

    private final UUID id;
    private final UUID playerUuid;
    private final String playerName;
    private final String ipAddress;
    private final String bannedBy;
    private final String reason;
    private final BanType banType;
    private final Instant bannedAt;
    private final Instant expiresAt; // null means permanent
    private boolean active;
    private String unbannedBy;
    private String unbanReason;
    private Instant unbannedAt;

    public BanRecord(UUID id, UUID playerUuid, String playerName, String ipAddress,
                     String bannedBy, String reason, BanType banType,
                     Instant bannedAt, Instant expiresAt, boolean active,
                     String unbannedBy, String unbanReason, Instant unbannedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.ipAddress = ipAddress;
        this.bannedBy = bannedBy != null ? bannedBy : "System";
        this.reason = reason != null ? reason : "Melanggar Ketentuan Apexsions.";
        this.banType = banType != null ? banType : BanType.NAME;
        this.bannedAt = bannedAt != null ? bannedAt : Instant.now();
        this.expiresAt = expiresAt;
        this.active = active;
        this.unbannedBy = unbannedBy;
        this.unbanReason = unbanReason;
        this.unbannedAt = unbannedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public String getReason() {
        return reason;
    }

    public BanType getBanType() {
        return banType;
    }

    public Instant getBannedAt() {
        return bannedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        if (!active) return false;
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    public boolean isPermanent() {
        return expiresAt == null;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public String getUnbannedBy() {
        return unbannedBy;
    }

    public String getUnbanReason() {
        return unbanReason;
    }

    public Instant getUnbannedAt() {
        return unbannedAt;
    }

    public void deactivate(String unbannedBy, String unbanReason) {
        this.active = false;
        this.unbannedBy = unbannedBy;
        this.unbanReason = unbanReason;
        this.unbannedAt = Instant.now();
    }

    public String getTimeRemainingFormatted() {
        if (isPermanent()) {
            return "PERMANEN";
        }
        if (isExpired()) {
            return "KEDALUWARSA";
        }
        Duration rem = Duration.between(Instant.now(), expiresAt);
        long days = rem.toDays();
        long hours = rem.toHoursPart();
        long minutes = rem.toMinutesPart();
        long seconds = rem.toSecondsPart();

        if (days > 0) {
            return String.format("%d hari %d jam", days, hours);
        } else if (hours > 0) {
            return String.format("%d jam %d menit", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d menit %d detik", minutes, seconds);
        } else {
            return String.format("%d detik", seconds);
        }
    }
}
