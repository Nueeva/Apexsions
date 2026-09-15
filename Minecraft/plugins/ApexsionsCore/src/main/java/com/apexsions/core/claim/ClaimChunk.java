package com.apexsions.core.claim;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a claimed 16x16 chunk of land owned by a player.
 */
public class ClaimChunk {

    private final UUID id;
    private final UUID ownerId;
    private String ownerName;
    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private final Set<UUID> trustedPlayers;
    private final long createdAt;

    public ClaimChunk(UUID id, UUID ownerId, String ownerName, String world, int chunkX, int chunkZ,
                      Set<UUID> trustedPlayers, long createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.ownerId = Objects.requireNonNull(ownerId, "Owner ID cannot be null");
        this.ownerName = ownerName != null ? ownerName : "Unknown";
        this.world = Objects.requireNonNull(world, "World cannot be null");
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.trustedPlayers = ConcurrentHashMap.newKeySet();
        if (trustedPlayers != null) {
            this.trustedPlayers.addAll(trustedPlayers);
        }
        this.createdAt = createdAt > 0 ? createdAt : System.currentTimeMillis();
    }

    public static String buildChunkKey(String world, int chunkX, int chunkZ) {
        return world.toLowerCase() + ":" + chunkX + ":" + chunkZ;
    }

    public String getChunkKey() {
        return buildChunkKey(world, chunkX, chunkZ);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public Set<UUID> getTrustedPlayers() {
        return Collections.unmodifiableSet(trustedPlayers);
    }

    public boolean isOwner(UUID uuid) {
        return ownerId.equals(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return isOwner(uuid) || trustedPlayers.contains(uuid);
    }

    public boolean addTrust(UUID uuid) {
        if (uuid == null || isOwner(uuid)) return false;
        return trustedPlayers.add(uuid);
    }

    public boolean removeTrust(UUID uuid) {
        return trustedPlayers.remove(uuid);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getTrustedPlayersSerialized() {
        if (trustedPlayers.isEmpty()) return "";
        StringJoiner sj = new StringJoiner(",");
        for (UUID u : trustedPlayers) {
            sj.add(u.toString());
        }
        return sj.toString();
    }

    public static Set<UUID> deserializeTrustedPlayers(String raw) {
        Set<UUID> set = new HashSet<>();
        if (raw == null || raw.trim().isEmpty()) return set;
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    set.add(UUID.fromString(trimmed));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return set;
    }
}
