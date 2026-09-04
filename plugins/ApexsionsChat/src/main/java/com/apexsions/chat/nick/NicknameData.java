package com.apexsions.chat.nick;

import java.util.UUID;

/**
 * Domain model representing a player's nickname state, selected color style, and token balance.
 */
public class NicknameData {

    private final UUID uuid;
    private String playerName;
    private String nicknameRaw;
    private String colorStyleId;
    private int tokens;
    private long updatedAt;

    public NicknameData(UUID uuid, String playerName, String nicknameRaw, String colorStyleId, int tokens, long updatedAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.nicknameRaw = nicknameRaw;
        this.colorStyleId = colorStyleId != null && !colorStyleId.isBlank() ? colorStyleId : "default";
        this.tokens = Math.max(0, tokens);
        this.updatedAt = updatedAt;
    }

    public static NicknameData createDefault(UUID uuid, String playerName) {
        return new NicknameData(uuid, playerName, null, "default", 0, System.currentTimeMillis());
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

    public String getNicknameRaw() {
        return nicknameRaw;
    }

    public void setNicknameRaw(String nicknameRaw) {
        this.nicknameRaw = nicknameRaw;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean hasNickname() {
        return nicknameRaw != null && !nicknameRaw.isBlank();
    }

    public String getColorStyleId() {
        return colorStyleId;
    }

    public void setColorStyleId(String colorStyleId) {
        this.colorStyleId = colorStyleId != null && !colorStyleId.isBlank() ? colorStyleId : "default";
        this.updatedAt = System.currentTimeMillis();
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = Math.max(0, tokens);
        this.updatedAt = System.currentTimeMillis();
    }

    public void addTokens(int amount) {
        if (amount > 0) {
            this.tokens += amount;
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public boolean consumeToken() {
        if (this.tokens > 0) {
            this.tokens--;
            this.updatedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
