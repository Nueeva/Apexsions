package com.apexsions.core.claim;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Service managing land claims, access control, quota checks, and boundary visualizations.
 */
public class ClaimManager {

    private final ApexsionsCorePlugin plugin;
    private final ClaimRepository repository;
    private final Map<String, ClaimChunk> claims = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<String, Integer> claimLimits = new HashMap<>();
    private final Set<String> disabledWorlds = new HashSet<>();
    private boolean protectBlocks = true;
    private boolean protectContainers = true;
    private boolean protectDoorsGates = true;
    private boolean protectRedstone = true;
    private boolean protectPassiveEntities = true;
    private boolean preventExplosions = true;
    private boolean preventFluidPlacing = true;
    private boolean visualizerEnabled = true;
    private int visualizerDurationSeconds = 8;
    private Color boundaryColor = Color.fromRGB(212, 175, 55); // Gold

    public ClaimManager(ApexsionsCorePlugin plugin, ClaimRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        loadConfig();
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "claims.yml");
        if (!configFile.exists()) {
            plugin.saveResource("claims.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        claimLimits.clear();
        if (config.isConfigurationSection("claim-limits")) {
            for (String rank : config.getConfigurationSection("claim-limits").getKeys(false)) {
                claimLimits.put(rank.toLowerCase(), config.getInt("claim-limits." + rank, 4));
            }
        } else {
            claimLimits.put("wanderer", 4);
            claimLimits.put("sions", 32);
        }

        disabledWorlds.clear();
        for (String w : config.getStringList("disabled-worlds")) {
            disabledWorlds.add(w.toLowerCase());
        }

        protectBlocks = config.getBoolean("protection.protect-blocks", true);
        protectContainers = config.getBoolean("protection.protect-containers", true);
        protectDoorsGates = config.getBoolean("protection.protect-doors-gates", true);
        protectRedstone = config.getBoolean("protection.protect-redstone-levers", true);
        protectPassiveEntities = config.getBoolean("protection.protect-passive-entities", true);
        preventExplosions = config.getBoolean("protection.prevent-explosions", true);
        preventFluidPlacing = config.getBoolean("protection.prevent-fluid-placing", true);

        visualizerEnabled = config.getBoolean("visualizer.enabled", true);
        visualizerDurationSeconds = config.getInt("visualizer.duration-seconds", 8);
    }

    public void loadClaims() {
        repository.loadAllClaims().thenAccept(list -> {
            claims.clear();
            for (ClaimChunk c : list) {
                claims.put(c.getChunkKey(), c);
            }
            plugin.getLogger().info("Loaded " + claims.size() + " sovereign land claims from database.");
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Could not load claims into memory", ex);
            return null;
        });
    }

    public Optional<ClaimChunk> getClaimAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return Optional.empty();
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        return getClaimAt(loc.getWorld().getName(), chunkX, chunkZ);
    }

    public Optional<ClaimChunk> getClaimAt(String world, int chunkX, int chunkZ) {
        return Optional.ofNullable(claims.get(ClaimChunk.buildChunkKey(world, chunkX, chunkZ)));
    }

    public List<ClaimChunk> getClaimsByOwner(UUID ownerId) {
        List<ClaimChunk> list = new ArrayList<>();
        for (ClaimChunk c : claims.values()) {
            if (c.isOwner(ownerId)) {
                list.add(c);
            }
        }
        return list;
    }

    public int getClaimCount(UUID ownerId) {
        int count = 0;
        for (ClaimChunk c : claims.values()) {
            if (c.isOwner(ownerId)) {
                count++;
            }
        }
        return count;
    }

    public int getMaxClaims(Player player) {
        if (player.isOp() || player.hasPermission("apexsions.admin.claim.unlimited") || player.hasPermission("apexsions.admin")) {
            return 9999;
        }

        // Check permission-based custom limits (e.g. apexsions.claim.limit.50)
        int highestPermLimit = 0;
        for (var perm : player.getEffectivePermissions()) {
            String pName = perm.getPermission().toLowerCase();
            if (pName.startsWith("apexsions.claim.limit.")) {
                try {
                    int val = Integer.parseInt(pName.substring("apexsions.claim.limit.".length()));
                    if (val > highestPermLimit) highestPermLimit = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (highestPermLimit > 0) {
            return highestPermLimit;
        }

        // Fallback to LuckPerms primary group or rank mapping
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
            String rankKey = plugin.getLuckPermsHook().getPlayerRankKey(player);
            if (rankKey != null && claimLimits.containsKey(rankKey.toLowerCase())) {
                return claimLimits.get(rankKey.toLowerCase());
            }
        }

        return claimLimits.getOrDefault("wanderer", 4);
    }

    public boolean canBuild(Player player, Location loc) {
        if (player == null || loc == null) return false;
        if (player.isOp() || player.hasPermission("apexsions.admin.bypass.claim") || player.hasPermission("apexsions.admin")) {
            return true;
        }

        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();
            return claim.isTrusted(player.getUniqueId());
        }

        // If not claimed by player, check Kingdom sovereign territory
        if (plugin.getRegionManager() != null) {
            Optional<Region> regionOpt = plugin.getRegionManager().getRegionAt(loc);
            if (regionOpt.isPresent()) {
                Region region = regionOpt.get();
                if (region.isPlayable()) {
                    Optional<PlayerData> pData = plugin.getPlayerDataService().getCached(player.getUniqueId());
                    UUID playerKingdom = pData.map(PlayerData::getRegionId).orElse(null);
                    if (playerKingdom != null && playerKingdom.equals(region.getId())) {
                        return true; // Member of this kingdom building in kingdom wilderness
                    } else {
                        // Outsider/Foreign kingdom member
                        if (plugin.getWarManager() != null && plugin.getWarManager().isWarActive()) {
                            return true; // War time allows interaction
                        }
                        return false; // Sovereign kingdom blocks foreign griefing
                    }
                }
            }
        }

        return true; // Neutral wilderness
    }

    public boolean canInteract(Player player, Location loc, Material mat) {
        if (player == null || loc == null) return false;
        if (player.isOp() || player.hasPermission("apexsions.admin.bypass.claim") || player.hasPermission("apexsions.admin")) {
            return true;
        }

        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();
            if (claim.isTrusted(player.getUniqueId())) {
                return true;
            }

            // Check if material is container or locked mechanism
            if (isContainer(mat) || isRestrictedMechanism(mat)) {
                return false;
            }
            return false;
        }

        // Territory check for sovereign chests
        if (isContainer(mat) && plugin.getRegionManager() != null) {
            Optional<Region> regionOpt = plugin.getRegionManager().getRegionAt(loc);
            if (regionOpt.isPresent() && regionOpt.get().isPlayable()) {
                Optional<PlayerData> pData = plugin.getPlayerDataService().getCached(player.getUniqueId());
                UUID playerKingdom = pData.map(PlayerData::getRegionId).orElse(null);
                if (playerKingdom == null || !playerKingdom.equals(regionOpt.get().getId())) {
                    if (plugin.getWarManager() == null || !plugin.getWarManager().isWarActive()) {
                        return false; // Cannot loot chests in foreign kingdom without war
                    }
                }
            }
        }

        return true;
    }

    public boolean isContainer(Material mat) {
        if (mat == null) return false;
        return switch (mat) {
            case CHEST, TRAPPED_CHEST, BARREL, SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX,
                 MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, LIME_SHULKER_BOX,
                 PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX,
                 PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX,
                 RED_SHULKER_BOX, BLACK_SHULKER_BOX, HOPPER, FURNACE, BLAST_FURNACE, SMOKER,
                 DISPENSER, DROPPER, BREWING_STAND, CHISELED_BOOKSHELF, CRAFTER -> true;
            default -> false;
        };
    }

    public boolean isRestrictedMechanism(Material mat) {
        if (mat == null) return false;
        String name = mat.name();
        return name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR") || name.endsWith("_GATE")
                || name.endsWith("_BUTTON") || mat == Material.LEVER || mat == Material.REPEATER
                || mat == Material.COMPARATOR;
    }

    public ClaimResult claimCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        String worldName = chunk.getWorld().getName();

        if (disabledWorlds.contains(worldName.toLowerCase())) {
            return new ClaimResult(false, "<red>✖ Wilayah dunia ini dilindungi dan tidak dapat diklaim!</red>");
        }

        String key = ClaimChunk.buildChunkKey(worldName, chunk.getX(), chunk.getZ());
        if (claims.containsKey(key)) {
            ClaimChunk existing = claims.get(key);
            if (existing.isOwner(player.getUniqueId())) {
                return new ClaimResult(false, "<yellow>⚠ Anda sudah memiliki tanah ini!</yellow>");
            } else {
                return new ClaimResult(false, "<red>✖ Tanah ini sudah diklaim oleh <gold>" + existing.getOwnerName() + "</gold>!</red>");
            }
        }

        int currentClaims = getClaimCount(player.getUniqueId());
        int maxClaims = getMaxClaims(player);
        if (currentClaims >= maxClaims) {
            return new ClaimResult(false, "<red>✖ Kuota klaim Anda sudah penuh (<gold>" + currentClaims + "/" + maxClaims + "</gold> chunks)! Tingkatkan kasta atau rank untuk mendapatkan lebih banyak tanah.</red>");
        }

        ClaimChunk newClaim = new ClaimChunk(UUID.randomUUID(), player.getUniqueId(), player.getName(),
                worldName, chunk.getX(), chunk.getZ(), null, System.currentTimeMillis());

        claims.put(key, newClaim);
        repository.saveClaim(newClaim);
        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        showChunkBoundary(player, chunk);
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.7f, 1.2f);

        return new ClaimResult(true, "<green>✔ Berhasil mengklaim tanah di chunk <gold>[" + chunk.getX() + ", " + chunk.getZ() + "]</gold>! Total tanah Anda: <gold>" + (currentClaims + 1) + "/" + maxClaims + "</gold> chunks.</green>");
    }

    public ClaimResult unclaimCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        String key = ClaimChunk.buildChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());

        ClaimChunk claim = claims.get(key);
        if (claim == null) {
            return new ClaimResult(false, "<yellow>⚠ Tanah di chunk ini tidak diklaim oleh siapapun.</yellow>");
        }

        if (!claim.isOwner(player.getUniqueId()) && !player.isOp() && !player.hasPermission("apexsions.admin")) {
            return new ClaimResult(false, "<red>✖ Anda bukan pemilik tanah ini! Dimiliki oleh <gold>" + claim.getOwnerName() + "</gold>.</red>");
        }

        claims.remove(key);
        repository.deleteClaim(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, 1.0f);
        return new ClaimResult(true, "<gold>✔ Berhasil melepas klaim tanah pada chunk [" + chunk.getX() + ", " + chunk.getZ() + "].</gold>");
    }

    public ClaimResult unclaimAll(Player player) {
        List<ClaimChunk> list = getClaimsByOwner(player.getUniqueId());
        if (list.isEmpty()) {
            return new ClaimResult(false, "<yellow>⚠ Anda belum memiliki klaim tanah.</yellow>");
        }

        for (ClaimChunk c : list) {
            claims.remove(c.getChunkKey());
        }
        repository.deleteClaimsByOwner(player.getUniqueId());
        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 0.8f);
        return new ClaimResult(true, "<gold>✔ Berhasil melepas seluruh <yellow>" + list.size() + "</yellow> klaim tanah Anda.</gold>");
    }

    public boolean forceUnclaimChunk(String world, int chunkX, int chunkZ) {
        String key = ClaimChunk.buildChunkKey(world, chunkX, chunkZ);
        ClaimChunk removed = claims.remove(key);
        if (removed != null) {
            repository.deleteClaim(world, chunkX, chunkZ);
            if (plugin.getWebBridgeService() != null) {
                plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
            }
            return true;
        }
        return false;
    }

    public int forceUnclaimAll(UUID ownerUuid) {
        List<ClaimChunk> list = getClaimsByOwner(ownerUuid);
        if (list.isEmpty()) return 0;
        for (ClaimChunk c : list) {
            claims.remove(c.getChunkKey());
        }
        repository.deleteClaimsByOwner(ownerUuid);
        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }
        return list.size();
    }

    public Collection<ClaimChunk> getAllClaims() {
        return Collections.unmodifiableCollection(claims.values());
    }

    public ClaimResult trustPlayer(Player owner, UUID targetId, String targetName) {
        List<ClaimChunk> ownerClaims = getClaimsByOwner(owner.getUniqueId());
        if (ownerClaims.isEmpty()) {
            return new ClaimResult(false, "<yellow>⚠ Anda belum memiliki klaim tanah apapun untuk menambahkan izin trust.</yellow>");
        }

        boolean addedAny = false;
        for (ClaimChunk c : ownerClaims) {
            if (c.addTrust(targetId)) {
                addedAny = true;
                repository.updateTrustedPlayers(c);
            }
        }

        if (addedAny) {
            return new ClaimResult(true, "<green>✔ Pemain <gold>" + targetName + "</gold> kini dipercaya (trusted) di seluruh wilayah klaim Anda.</green>");
        } else {
            return new ClaimResult(false, "<yellow>⚠ Pemain " + targetName + " sudah berstatus trusted di wilayah Anda.</yellow>");
        }
    }

    public ClaimResult untrustPlayer(Player owner, UUID targetId, String targetName) {
        List<ClaimChunk> ownerClaims = getClaimsByOwner(owner.getUniqueId());
        if (ownerClaims.isEmpty()) {
            return new ClaimResult(false, "<yellow>⚠ Anda belum memiliki klaim tanah.</yellow>");
        }

        boolean removedAny = false;
        for (ClaimChunk c : ownerClaims) {
            if (c.removeTrust(targetId)) {
                removedAny = true;
                repository.updateTrustedPlayers(c);
            }
        }

        if (removedAny) {
            return new ClaimResult(true, "<gold>✔ Izin trust pemain <yellow>" + targetName + "</yellow> telah dicabut dari seluruh wilayah klaim Anda.</gold>");
        } else {
            return new ClaimResult(false, "<yellow>⚠ Pemain " + targetName + " tidak terdaftar di daftar trusted Anda.</yellow>");
        }
    }

    public void showChunkBoundary(Player player, Chunk chunk) {
        if (!visualizerEnabled || player == null || !player.isOnline()) return;

        World world = chunk.getWorld();
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;
        int maxX = minX + 16;
        int maxZ = minZ + 16;
        double playerY = player.getLocation().getY();

        Particle.DustOptions dust = new Particle.DustOptions(boundaryColor, 1.2f);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticksLeft = visualizerDurationSeconds * 2; // runs every 10 ticks (0.5s)

            @Override
            public void run() {
                if (!player.isOnline() || ticksLeft <= 0) {
                    return;
                }
                ticksLeft--;

                for (int x = minX; x <= maxX; x += 2) {
                    player.spawnParticle(Particle.DUST, x, playerY + 0.5, minZ, 1, 0, 0, 0, 0, dust);
                    player.spawnParticle(Particle.DUST, x, playerY + 0.5, maxZ, 1, 0, 0, 0, 0, dust);
                    player.spawnParticle(Particle.DUST, x, playerY + 1.5, minZ, 1, 0, 0, 0, 0, dust);
                    player.spawnParticle(Particle.DUST, x, playerY + 1.5, maxZ, 1, 0, 0, 0, 0, dust);
                }
                for (int z = minZ; z <= maxZ; z += 2) {
                    player.spawnParticle(Particle.DUST, minX, playerY + 0.5, z, 1, 0, 0, 0, 0, dust);
                    player.spawnParticle(Particle.DUST, maxX, playerY + 0.5, z, 1, 0, 0, 0, 0, dust);
                    player.spawnParticle(Particle.DUST, minX, playerY + 1.5, z, 1, 0, 0, 0, 0, dust);
                    player.spawnParticle(Particle.DUST, maxX, playerY + 1.5, z, 1, 0, 0, 0, 0, dust);
                }
            }
        }, 0L, 10L);

        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, visualizerDurationSeconds * 20L);
    }

    public boolean isProtectBlocks() {
        return protectBlocks;
    }

    public boolean isProtectContainers() {
        return protectContainers;
    }

    public boolean isProtectDoorsGates() {
        return protectDoorsGates;
    }

    public boolean isProtectRedstone() {
        return protectRedstone;
    }

    public boolean isProtectPassiveEntities() {
        return protectPassiveEntities;
    }

    public boolean isPreventExplosions() {
        return preventExplosions;
    }

    public boolean isPreventFluidPlacing() {
        return preventFluidPlacing;
    }

    public record ClaimResult(boolean success, String message) {}
}
