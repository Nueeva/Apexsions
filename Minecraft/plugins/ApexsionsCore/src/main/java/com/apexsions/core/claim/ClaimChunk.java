package com.apexsions.core.claim;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a claimed 16x16 chunk of land owned by a player,
 * including bank balance, progressive tax upkeep, flags, and citizen roles.
 */
public class ClaimChunk {

    private static final Gson GSON = new Gson();
    private static final Type MAP_STRING_STRING_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final Type MAP_STRING_ROLE_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private final UUID id;
    private final UUID ownerId;
    private String ownerName;
    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private final Set<UUID> trustedPlayers;
    private final Map<UUID, ClaimRole> memberRoles;
    private final Map<String, String> flags;

    private double bankBalance;
    private double dailyUpkeep;
    private ClaimStatus status;
    private long gracePeriodUntil;
    private long lastTaxCollectedAt;
    private String kingdomId;
    private final long createdAt;

    public ClaimChunk(UUID id, UUID ownerId, String ownerName, String world, int chunkX, int chunkZ,
                      Set<UUID> trustedPlayers, long createdAt) {
        this(id, ownerId, ownerName, world, chunkX, chunkZ, trustedPlayers, null, null,
                0.0, 100.0, ClaimStatus.ACTIVE, 0L, 0L, null, createdAt);
    }

    public ClaimChunk(UUID id, UUID ownerId, String ownerName, String world, int chunkX, int chunkZ,
                      Set<UUID> trustedPlayers, Map<UUID, ClaimRole> memberRoles, Map<String, String> flags,
                      double bankBalance, double dailyUpkeep, ClaimStatus status,
                      long gracePeriodUntil, long lastTaxCollectedAt, String kingdomId, long createdAt) {
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

        this.memberRoles = new ConcurrentHashMap<>();
        if (memberRoles != null) {
            this.memberRoles.putAll(memberRoles);
        } else {
            // Seed builder roles for existing trusted players
            for (UUID u : this.trustedPlayers) {
                this.memberRoles.put(u, ClaimRole.BUILDER);
            }
        }

        this.flags = new ConcurrentHashMap<>();
        // Default safe flags
        this.flags.put("pvp", "false");
        this.flags.put("mob_spawn", "false");
        this.flags.put("fire_spread", "false");
        this.flags.put("explosions", "false");
        if (flags != null) {
            this.flags.putAll(flags);
        }

        this.bankBalance = Math.max(0.0, bankBalance);
        this.dailyUpkeep = dailyUpkeep > 0 ? dailyUpkeep : 100.0;
        this.status = status != null ? status : ClaimStatus.ACTIVE;
        this.gracePeriodUntil = gracePeriodUntil;
        this.lastTaxCollectedAt = lastTaxCollectedAt > 0 ? lastTaxCollectedAt : System.currentTimeMillis();
        this.kingdomId = kingdomId;
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

    public Map<UUID, ClaimRole> getMemberRoles() {
        return Collections.unmodifiableMap(memberRoles);
    }

    public boolean isOwner(UUID uuid) {
        return ownerId.equals(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return isOwner(uuid) || trustedPlayers.contains(uuid) || memberRoles.containsKey(uuid);
    }

    public ClaimRole getRole(UUID uuid) {
        if (uuid == null) return ClaimRole.VISITOR;
        if (isOwner(uuid)) return ClaimRole.OWNER;
        ClaimRole role = memberRoles.get(uuid);
        if (role != null) return role;
        if (trustedPlayers.contains(uuid)) return ClaimRole.BUILDER;
        return ClaimRole.VISITOR;
    }

    public void setRole(UUID uuid, ClaimRole role) {
        if (uuid == null || isOwner(uuid)) return;
        if (role == null || role == ClaimRole.VISITOR) {
            memberRoles.remove(uuid);
            trustedPlayers.remove(uuid);
        } else {
            memberRoles.put(uuid, role);
            trustedPlayers.add(uuid);
        }
    }

    public boolean addTrust(UUID uuid) {
        if (uuid == null || isOwner(uuid)) return false;
        setRole(uuid, ClaimRole.BUILDER);
        return true;
    }

    public boolean removeTrust(UUID uuid) {
        if (uuid == null) return false;
        boolean removed = trustedPlayers.remove(uuid) | (memberRoles.remove(uuid) != null);
        return removed;
    }

    // --- Financial & Tax Management ---

    public double getBankBalance() {
        return bankBalance;
    }

    public synchronized void setBankBalance(double bankBalance) {
        this.bankBalance = Math.max(0.0, bankBalance);
    }

    public synchronized void deposit(double amount) {
        if (amount <= 0) return;
        this.bankBalance += amount;
        if (this.status == ClaimStatus.GRACE_PERIOD && this.bankBalance >= this.dailyUpkeep) {
            this.status = ClaimStatus.ACTIVE;
            this.gracePeriodUntil = 0;
        }
    }

    public synchronized boolean deduct(double amount) {
        if (amount <= 0) return true;
        if (this.bankBalance >= amount) {
            this.bankBalance -= amount;
            return true;
        }
        return false;
    }

    public double getDailyUpkeep() {
        return dailyUpkeep;
    }

    public void setDailyUpkeep(double dailyUpkeep) {
        this.dailyUpkeep = Math.max(0.0, dailyUpkeep);
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status != null ? status : ClaimStatus.ACTIVE;
    }

    public long getGracePeriodUntil() {
        return gracePeriodUntil;
    }

    public void setGracePeriodUntil(long gracePeriodUntil) {
        this.gracePeriodUntil = gracePeriodUntil;
    }

    public boolean isInGracePeriod() {
        return status == ClaimStatus.GRACE_PERIOD && System.currentTimeMillis() < gracePeriodUntil;
    }

    public boolean isGracePeriodExpired() {
        return status == ClaimStatus.GRACE_PERIOD && System.currentTimeMillis() >= gracePeriodUntil;
    }

    public double getDaysRemaining() {
        if (dailyUpkeep <= 0) return 999.0;
        return bankBalance / dailyUpkeep;
    }

    public long getLastTaxCollectedAt() {
        return lastTaxCollectedAt;
    }

    public void setLastTaxCollectedAt(long lastTaxCollectedAt) {
        this.lastTaxCollectedAt = lastTaxCollectedAt;
    }

    public String getKingdomId() {
        return kingdomId;
    }

    public void setKingdomId(String kingdomId) {
        this.kingdomId = kingdomId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // --- Flags Management ---

    public Map<String, String> getFlags() {
        return Collections.unmodifiableMap(flags);
    }

    public String getFlag(String key, String def) {
        return flags.getOrDefault(key.toLowerCase(), def);
    }

    public boolean getBooleanFlag(String key, boolean def) {
        String val = flags.get(key.toLowerCase());
        if (val == null) return def;
        return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("1");
    }

    public void setFlag(String key, String value) {
        if (key == null) return;
        if (value == null) {
            flags.remove(key.toLowerCase());
        } else {
            flags.put(key.toLowerCase(), value);
        }
    }

    public String getName() {
        return flags.get("name");
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            flags.remove("name");
        } else {
            flags.put("name", name.trim());
        }
    }

    public boolean isOutpost() {
        return getBooleanFlag("is_outpost", false);
    }

    public void setOutpost(boolean outpost) {
        if (outpost) {
            setFlag("is_outpost", "true");
        } else {
            setFlag("is_outpost", null);
        }
    }

    // --- Serialization Helpers ---

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

    public String getFlagsSerialized() {
        return GSON.toJson(flags);
    }

    public static Map<String, String> deserializeFlags(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.equals("{}")) {
            return new ConcurrentHashMap<>();
        }
        try {
            Map<String, String> map = GSON.fromJson(raw, MAP_STRING_STRING_TYPE);
            return map != null ? new ConcurrentHashMap<>(map) : new ConcurrentHashMap<>();
        } catch (Exception e) {
            return new ConcurrentHashMap<>();
        }
    }

    public String getRolesSerialized() {
        Map<String, String> rawMap = new HashMap<>();
        for (Map.Entry<UUID, ClaimRole> entry : memberRoles.entrySet()) {
            rawMap.put(entry.getKey().toString(), entry.getValue().name());
        }
        return GSON.toJson(rawMap);
    }

    public static Map<UUID, ClaimRole> deserializeRoles(String raw) {
        Map<UUID, ClaimRole> map = new ConcurrentHashMap<>();
        if (raw == null || raw.trim().isEmpty() || raw.equals("{}")) {
            return map;
        }
        try {
            Map<String, String> rawMap = GSON.fromJson(raw, MAP_STRING_ROLE_TYPE);
            if (rawMap != null) {
                for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                    try {
                        map.put(UUID.fromString(entry.getKey()), ClaimRole.fromString(entry.getValue()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }
}
