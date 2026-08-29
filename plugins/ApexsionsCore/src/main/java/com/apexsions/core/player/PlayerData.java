package com.apexsions.core.player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory and persistent domain model representing a player's KingdomCore profile,
 * including level rewards claiming state.
 */
public class PlayerData {

    private final UUID uuid;
    private String username;
    private int level;
    private long xp;
    private UUID regionId;
    private String activeTitle;
    private String activeAura;
    private String activeTrail;
    private String activeKillEffect;
    private final Set<String> unlockedTitles = ConcurrentHashMap.newKeySet();
    private final Set<String> unlockedCosmetics = ConcurrentHashMap.newKeySet();
    private final Set<Integer> claimedRewards = ConcurrentHashMap.newKeySet();
    private final Instant createdAt;
    private Instant updatedAt;

    public PlayerData(UUID uuid, String username, int level, long xp, UUID regionId, Instant createdAt, Instant updatedAt) {
        this.uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.level = Math.clamp(level, 1, 100);
        this.xp = Math.max(0, xp);
        this.regionId = regionId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static PlayerData createDefault(UUID uuid, String username) {
        return new PlayerData(uuid, username, 1, 0L, null, Instant.now(), Instant.now());
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.updatedAt = Instant.now();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.clamp(level, 1, 100);
        this.updatedAt = Instant.now();
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = Math.max(0, xp);
        this.updatedAt = Instant.now();
    }

    public void addXp(long amount) {
        this.xp = Math.max(0, this.xp + amount);
        this.updatedAt = Instant.now();
    }

    public UUID getRegionId() {
        return regionId;
    }

    public void setRegionId(UUID regionId) {
        this.regionId = regionId;
        this.updatedAt = Instant.now();
    }

    public boolean hasRegion() {
        return regionId != null;
    }

    public String getActiveTitle() {
        return activeTitle;
    }

    public void setActiveTitle(String activeTitle) {
        this.activeTitle = activeTitle;
        this.updatedAt = Instant.now();
    }

    public String getActiveAura() {
        return activeAura;
    }

    public void setActiveAura(String activeAura) {
        this.activeAura = activeAura;
        this.updatedAt = Instant.now();
    }

    public String getActiveTrail() {
        return activeTrail;
    }

    public void setActiveTrail(String activeTrail) {
        this.activeTrail = activeTrail;
        this.updatedAt = Instant.now();
    }

    public String getActiveKillEffect() {
        return activeKillEffect;
    }

    public void setActiveKillEffect(String activeKillEffect) {
        this.activeKillEffect = activeKillEffect;
        this.updatedAt = Instant.now();
    }

    public boolean hasUnlockedTitle(String titleId) {
        return titleId != null && unlockedTitles.contains(titleId.toLowerCase(Locale.ROOT));
    }

    public void unlockTitle(String titleId) {
        if (titleId != null) {
            unlockedTitles.add(titleId.toLowerCase(Locale.ROOT));
            this.updatedAt = Instant.now();
        }
    }

    public Set<String> getUnlockedTitles() {
        return Collections.unmodifiableSet(unlockedTitles);
    }

    public boolean hasUnlockedCosmetic(String cosmeticId) {
        return cosmeticId != null && unlockedCosmetics.contains(cosmeticId.toLowerCase(Locale.ROOT));
    }

    public void unlockCosmetic(String cosmeticId) {
        if (cosmeticId != null) {
            unlockedCosmetics.add(cosmeticId.toLowerCase(Locale.ROOT));
            this.updatedAt = Instant.now();
        }
    }

    public Set<String> getUnlockedCosmetics() {
        return Collections.unmodifiableSet(unlockedCosmetics);
    }

    public boolean isRewardClaimed(int level) {
        return claimedRewards.contains(level);
    }

    public void setRewardClaimed(int level) {
        claimedRewards.add(level);
        this.updatedAt = Instant.now();
    }

    public Set<Integer> getClaimedRewards() {
        return Collections.unmodifiableSet(claimedRewards);
    }

    public String getClaimedRewardsString() {
        if (claimedRewards.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int lvl : claimedRewards) {
            if (sb.length() > 0) sb.append(",");
            sb.append(lvl);
        }
        return sb.toString();
    }

    public void setClaimedRewardsFromString(String str) {
        claimedRewards.clear();
        if (str == null || str.trim().isEmpty()) return;
        for (String part : str.split(",")) {
            try {
                claimedRewards.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {}
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markUpdated() {
        this.updatedAt = Instant.now();
    }
}
