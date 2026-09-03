package com.apexsions.media.creator.model;

import java.util.UUID;

public class CreatorProfile {

    private final UUID uuid;
    private String playerName;
    private String youtubeChannelId;
    private String youtubeHandle;
    private String tiktokUsername;
    private String pendingPlatform;
    private String pendingIdentifier;
    private String pendingVerifyCode;
    private long pendingVerifyExpiry;
    private long createdAt;

    public CreatorProfile(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.createdAt = System.currentTimeMillis();
    }

    public CreatorProfile(UUID uuid, String playerName, String youtubeChannelId, String youtubeHandle,
                          String tiktokUsername, String pendingPlatform, String pendingIdentifier,
                          String pendingVerifyCode, long pendingVerifyExpiry, long createdAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.youtubeChannelId = youtubeChannelId;
        this.youtubeHandle = youtubeHandle;
        this.tiktokUsername = tiktokUsername;
        this.pendingPlatform = pendingPlatform;
        this.pendingIdentifier = pendingIdentifier;
        this.pendingVerifyCode = pendingVerifyCode;
        this.pendingVerifyExpiry = pendingVerifyExpiry;
        this.createdAt = createdAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getYoutubeChannelId() {
        return youtubeChannelId;
    }

    public void setYoutubeChannelId(String youtubeChannelId) {
        this.youtubeChannelId = youtubeChannelId;
    }

    public String getYoutubeHandle() {
        return youtubeHandle;
    }

    public void setYoutubeHandle(String youtubeHandle) {
        this.youtubeHandle = youtubeHandle;
    }

    public String getTiktokUsername() {
        return tiktokUsername;
    }

    public void setTiktokUsername(String tiktokUsername) {
        this.tiktokUsername = tiktokUsername;
    }

    public String getPendingPlatform() {
        return pendingPlatform;
    }

    public void setPendingPlatform(String pendingPlatform) {
        this.pendingPlatform = pendingPlatform;
    }

    public String getPendingIdentifier() {
        return pendingIdentifier;
    }

    public void setPendingIdentifier(String pendingIdentifier) {
        this.pendingIdentifier = pendingIdentifier;
    }

    public String getPendingVerifyCode() {
        return pendingVerifyCode;
    }

    public void setPendingVerifyCode(String pendingVerifyCode) {
        this.pendingVerifyCode = pendingVerifyCode;
    }

    public long getPendingVerifyExpiry() {
        return pendingVerifyExpiry;
    }

    public void setPendingVerifyExpiry(long pendingVerifyExpiry) {
        this.pendingVerifyExpiry = pendingVerifyExpiry;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isYouTubeLinked() {
        return youtubeChannelId != null && !youtubeChannelId.isBlank();
    }

    public boolean isTikTokLinked() {
        return tiktokUsername != null && !tiktokUsername.isBlank();
    }

    public boolean hasPendingVerification() {
        return pendingVerifyCode != null && !pendingVerifyCode.isBlank() && System.currentTimeMillis() < pendingVerifyExpiry;
    }

    public void clearPendingVerification() {
        this.pendingPlatform = null;
        this.pendingIdentifier = null;
        this.pendingVerifyCode = null;
        this.pendingVerifyExpiry = 0L;
    }
}
