package com.apexsions.core.claim;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Enterprise service managing land claims, progressive taxation, grace periods,
 * flag controls, citizen roles, and kingdom integration.
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

    // Progressive Tax Configuration
    private boolean taxEnabled = true;
    private double baseTaxPerChunk = 100.0;
    private double progressiveMultiplier = 0.15;
    private int checkIntervalMinutes = 30;
    private int taxPeriodHours = 24;
    private int gracePeriodHours = 72;
    private double kingdomTreasurySplit = 0.50;
    private boolean unclaimOnGraceExpire = true;
    private boolean exemptUpperDimension = true;

    // Siege War Configuration
    private boolean siegeWarEnabled = true;
    private boolean allowHostileRaid = true;
    private boolean requireOwnerOnline = true;

    private BukkitTask taxCollectorTask;

    // Player position cache to detect entering/leaving claims
    private final Map<UUID, String> lastPlayerChunkKey = new ConcurrentHashMap<>();

    public ClaimManager(ApexsionsCorePlugin plugin, ClaimRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        loadConfig();
        startTaxCollectorScheduler();
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

        // Tax settings
        taxEnabled = config.getBoolean("tax.enabled", true);
        baseTaxPerChunk = config.getDouble("tax.base-tax-per-chunk", 100.0);
        progressiveMultiplier = config.getDouble("tax.progressive-multiplier", 0.15);
        checkIntervalMinutes = config.getInt("tax.check-interval-minutes", 30);
        taxPeriodHours = config.getInt("tax.tax-period-hours", 24);
        gracePeriodHours = config.getInt("tax.grace-period-hours", 72);
        kingdomTreasurySplit = config.getDouble("tax.kingdom-treasury-split", 0.50);
        unclaimOnGraceExpire = config.getBoolean("tax.unclaim-on-grace-expire", true);
        exemptUpperDimension = config.getBoolean("tax.exempt-upper-dimension", true);

        // Siege War settings
        siegeWarEnabled = config.getBoolean("siege-war.enabled", true);
        allowHostileRaid = config.getBoolean("siege-war.allow-hostile-raid", true);
        requireOwnerOnline = config.getBoolean("siege-war.require-owner-online", true);

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

    public void startTaxCollectorScheduler() {
        if (taxCollectorTask != null && !taxCollectorTask.isCancelled()) {
            taxCollectorTask.cancel();
        }
        if (!taxEnabled) return;

        long intervalTicks = checkIntervalMinutes * 60L * 20L;
        taxCollectorTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::processTaxCollectionCycle, 20L * 60L, intervalTicks);
    }

    public void stopScheduler() {
        if (taxCollectorTask != null && !taxCollectorTask.isCancelled()) {
            taxCollectorTask.cancel();
        }
    }

    public void loadClaims() {
        repository.loadAllClaims().thenAccept(list -> {
            claims.clear();
            for (ClaimChunk c : list) {
                claims.put(c.getChunkKey(), c);
            }
            plugin.getLogger().info("Loaded " + claims.size() + " sovereign land claims with tax & roles from database.");
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
        if (player.isOp() || player.hasPermission("apexsions.admin.claim.unlimited") || player.hasPermission("apexsions.claim.unlimited") || player.hasPermission("apexsions.admin")) {
            return Integer.MAX_VALUE;
        }

        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
            if (plugin.getLuckPermsHook().isStaffOrAdmin(player.getUniqueId())) {
                return Integer.MAX_VALUE;
            }
            String rankKey = plugin.getLuckPermsHook().getPlayerRankKey(player);
            if (rankKey != null && claimLimits.containsKey(rankKey.toLowerCase())) {
                int limit = claimLimits.get(rankKey.toLowerCase());
                return limit <= -1 ? Integer.MAX_VALUE : limit;
            }
        }

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

        int def = claimLimits.getOrDefault("wanderer", 4);
        return def <= -1 ? Integer.MAX_VALUE : def;
    }

    /**
     * Checks if a territory owner is exempt from daily upkeep tax (Upper Dimension / Staff).
     */
    public boolean isTaxExempt(UUID ownerId) {
        if (!exemptUpperDimension || ownerId == null) return false;
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isStaffOrAdmin(ownerId)) {
            return true;
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer(ownerId);
        return op != null && op.isOp();
    }

    // --- Progressive Tax & Upkeep Calculation ---

    public double calculateChunkDailyTax(UUID ownerId) {
        if (isTaxExempt(ownerId)) return 0.0;
        int totalClaims = Math.max(1, getClaimCount(ownerId));
        return baseTaxPerChunk * (1.0 + (totalClaims - 1) * progressiveMultiplier);
    }

    public double calculateTotalDailyTax(UUID ownerId) {
        if (isTaxExempt(ownerId)) return 0.0;
        int totalClaims = getClaimCount(ownerId);
        if (totalClaims <= 0) return 0.0;
        double perChunk = calculateChunkDailyTax(ownerId);
        return perChunk * totalClaims;
    }

    public void processTaxCollectionCycle() {
        if (!taxEnabled) return;
        long now = System.currentTimeMillis();
        long periodMs = taxPeriodHours * 3600L * 1000L;

        // Group claims by owner to calculate bulk tax accurately
        Map<UUID, List<ClaimChunk>> ownerClaims = new HashMap<>();
        for (ClaimChunk c : claims.values()) {
            ownerClaims.computeIfAbsent(c.getOwnerId(), k -> new ArrayList<>()).add(c);
        }

        for (Map.Entry<UUID, List<ClaimChunk>> entry : ownerClaims.entrySet()) {
            UUID ownerId = entry.getKey();
            List<ClaimChunk> chunks = entry.getValue();
            if (chunks.isEmpty()) continue;

            if (isTaxExempt(ownerId)) {
                for (ClaimChunk claim : chunks) {
                    claim.setDailyUpkeep(0.0);
                    claim.setStatus(ClaimStatus.ACTIVE);
                    claim.setGracePeriodUntil(0L);
                }
                continue;
            }

            double chunkRate = calculateChunkDailyTax(ownerId);

            for (ClaimChunk claim : chunks) {
                claim.setDailyUpkeep(chunkRate);

                // Check if tax collection interval is due
                if (now - claim.getLastTaxCollectedAt() >= periodMs) {
                    if (claim.deduct(chunkRate)) {
                        // Successfully collected
                        claim.setStatus(ClaimStatus.ACTIVE);
                        claim.setGracePeriodUntil(0);
                        claim.setLastTaxCollectedAt(now);
                        repository.updateClaimFinancials(claim);

                        // Split with Kingdom Treasury
                        if (claim.getKingdomId() != null && !claim.getKingdomId().isBlank() && kingdomTreasurySplit > 0) {
                            double treasuryShare = chunkRate * kingdomTreasurySplit;
                            depositKingdomTreasury(claim.getKingdomId(), treasuryShare);
                        }
                    } else {
                        // Failed to collect tax - Enter or progress grace period
                        if (claim.getStatus() != ClaimStatus.GRACE_PERIOD) {
                            claim.setStatus(ClaimStatus.GRACE_PERIOD);
                            claim.setGracePeriodUntil(now + (gracePeriodHours * 3600L * 1000L));
                            repository.updateClaimFinancials(claim);

                            // Notify online owner
                            Player owner = Bukkit.getPlayer(ownerId);
                            if (owner != null && owner.isOnline()) {
                                owner.sendMessage(mm.deserialize("<red><b>⚠ [PAJAK WILAYAH]</b> Saldo brankas klaim Anda di <gold>[" + claim.getChunkX() + ", " + claim.getChunkZ() + "]</gold> habis! Masa tenggang <b>72 jam</b> dimulai sebelum tanah disita.</red>"));
                                owner.playSound(owner.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 0.6f);
                            }
                        } else if (claim.isGracePeriodExpired()) {
                            // Grace period expired!
                            if (unclaimOnGraceExpire) {
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    forceUnclaimChunk(claim.getWorld(), claim.getChunkX(), claim.getChunkZ());
                                    Player owner = Bukkit.getPlayer(ownerId);
                                    if (owner != null && owner.isOnline()) {
                                        owner.sendMessage(mm.deserialize("<dark_red><b>✖ [PENYITAAN TANAH]</b> Masa tenggang klaim Anda di <gold>[" + claim.getChunkX() + ", " + claim.getChunkZ() + "]</gold> telah berakhir! Tanah telah disita dan kembali menjadi alam liar.</dark_red>"));
                                        owner.playSound(owner.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.7f, 1.0f);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }
    }

    private void depositKingdomTreasury(String kingdomKey, double amount) {
        if (kingdomKey == null || kingdomKey.isBlank() || amount <= 0) return;
        if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
            try {
                Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
                Method isAvail = providerClass.getMethod("isAvailable");
                if ((Boolean) isAvail.invoke(null)) {
                    Method getMethod = providerClass.getMethod("get");
                    Object api = getMethod.invoke(null);
                    Method deposit = api.getClass().getMethod("depositKingdomTreasury", String.class, String.class, double.class);
                    deposit.invoke(api, kingdomKey, "rupiah", amount);
                }
            } catch (Throwable ignored) {}
        }
    }

    // --- Financial Operations (Deposit & Withdraw) ---

    public ClaimResult depositBank(Player player, double amount) {
        if (amount <= 0) {
            return new ClaimResult(false, "<red>✖ Jumlah nominal deposit harus lebih besar dari 0!</red>");
        }

        List<ClaimChunk> playerClaims = getClaimsByOwner(player.getUniqueId());
        if (playerClaims.isEmpty()) {
            return new ClaimResult(false, "<yellow>⚠ Anda belum memiliki klaim tanah apapun.</yellow>");
        }

        if (plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
            double balance = plugin.getVaultHook().getBalance(player);
            if (balance < amount) {
                return new ClaimResult(false, "<red>✖ Saldo dompet Anda tidak cukup! Memiliki: <gold>Rp" + String.format("%,.0f", balance) + "</gold>.</red>");
            }
            plugin.getVaultHook().withdraw(player, amount);
        }

        // Distribute deposit evenly across all owned chunks
        double perChunk = amount / playerClaims.size();
        for (ClaimChunk c : playerClaims) {
            c.deposit(perChunk);
            repository.updateClaimFinancials(c);
        }

        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
        return new ClaimResult(true, "<green>✔ Berhasil menyetor <gold>Rp" + String.format("%,.0f", amount) + "</gold> ke Brankas Wilayah! Terbagi ke <yellow>" + playerClaims.size() + "</yellow> chunk tanah Anda.</green>");
    }

    public ClaimResult withdrawBank(Player player, double amount) {
        if (amount <= 0) {
            return new ClaimResult(false, "<red>✖ Jumlah penarikan harus lebih besar dari 0!</red>");
        }

        List<ClaimChunk> playerClaims = getClaimsByOwner(player.getUniqueId());
        if (playerClaims.isEmpty()) {
            return new ClaimResult(false, "<yellow>⚠ Anda tidak memiliki klaim tanah.</yellow>");
        }

        double totalVaulted = 0.0;
        for (ClaimChunk c : playerClaims) {
            totalVaulted += c.getBankBalance();
        }

        if (totalVaulted < amount) {
            return new ClaimResult(false, "<red>✖ Total saldo brankas klaim Anda tidak mencukupi! Tersedia: <gold>Rp" + String.format("%,.0f", totalVaulted) + "</gold>.</red>");
        }

        double perChunk = amount / playerClaims.size();
        for (ClaimChunk c : playerClaims) {
            c.deduct(perChunk);
            repository.updateClaimFinancials(c);
        }

        if (plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
            plugin.getVaultHook().deposit(player, amount);
        }

        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
        return new ClaimResult(true, "<green>✔ Berhasil menarik <gold>Rp" + String.format("%,.0f", amount) + "</gold> dari Brankas Wilayah ke dompet Anda.</green>");
    }

    // --- Access Control & Granular Permissions ---

    public boolean canBuild(Player player, Location loc) {
        if (player == null || loc == null) return false;
        if (player.isOp() || player.hasPermission("apexsions.admin.bypass.claim") || player.hasPermission("apexsions.admin")
                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player))) {
            return true;
        }

        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();

            // Check active Siege War condition
            if (isSiegeRaidAllowed(player, claim)) {
                return true;
            }

            ClaimRole role = claim.getRole(player.getUniqueId());
            return role.isAtLeast(ClaimRole.BUILDER);
        }

        // Sovereign kingdom check
        if (plugin.getRegionManager() != null) {
            Optional<Region> regionOpt = plugin.getRegionManager().getRegionAt(loc);
            if (regionOpt.isPresent() && regionOpt.get().isPlayable()) {
                Optional<PlayerData> pData = plugin.getPlayerDataService().getCached(player.getUniqueId());
                UUID playerKingdom = pData.map(PlayerData::getRegionId).orElse(null);
                if (playerKingdom != null && playerKingdom.equals(regionOpt.get().getId())) {
                    return true;
                } else {
                    if (plugin.getWarManager() != null && plugin.getWarManager().isWarActive()) {
                        return true;
                    }
                    return false;
                }
            }
        }

        return true;
    }

    public boolean canInteract(Player player, Location loc, Material mat) {
        if (player == null || loc == null) return false;
        if (player.isOp() || player.hasPermission("apexsions.admin.bypass.claim") || player.hasPermission("apexsions.admin")
                || (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player))) {
            return true;
        }

        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();

            if (isSiegeRaidAllowed(player, claim)) {
                return true;
            }

            ClaimRole role = claim.getRole(player.getUniqueId());
            if (role.isAtLeast(ClaimRole.BUILDER)) {
                return true;
            }

            // Container requires at least BUILDER
            if (isContainer(mat)) {
                return false;
            }

            // Door / Gate / Button allows VISITOR unless restricted
            if (isRestrictedMechanism(mat) && !protectDoorsGates) {
                return true;
            }
            return false;
        }

        if (isContainer(mat) && plugin.getRegionManager() != null) {
            Optional<Region> regionOpt = plugin.getRegionManager().getRegionAt(loc);
            if (regionOpt.isPresent() && regionOpt.get().isPlayable()) {
                Optional<PlayerData> pData = plugin.getPlayerDataService().getCached(player.getUniqueId());
                UUID playerKingdom = pData.map(PlayerData::getRegionId).orElse(null);
                if (playerKingdom == null || !playerKingdom.equals(regionOpt.get().getId())) {
                    if (plugin.getWarManager() == null || !plugin.getWarManager().isWarActive()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isPvpAllowed(Location loc) {
        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();
            return claim.getBooleanFlag("pvp", false);
        }
        return true;
    }

    public boolean isMobSpawnAllowed(Location loc) {
        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();
            return claim.getBooleanFlag("mob_spawn", false);
        }
        return true;
    }

    public boolean isFireSpreadAllowed(Location loc) {
        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();
            return claim.getBooleanFlag("fire_spread", false);
        }
        return true;
    }

    public boolean isExplosionsAllowed(Location loc) {
        Optional<ClaimChunk> claimOpt = getClaimAt(loc);
        if (claimOpt.isPresent()) {
            ClaimChunk claim = claimOpt.get();
            return claim.getBooleanFlag("explosions", false);
        }
        return !preventExplosions;
    }

    private boolean isSiegeRaidAllowed(Player attacker, ClaimChunk claim) {
        if (!siegeWarEnabled || !allowHostileRaid) return false;
        if (plugin.getWarManager() == null || !plugin.getWarManager().isWarActive()) return false;

        // Check if owner or any claim member is online
        if (requireOwnerOnline) {
            Player owner = Bukkit.getPlayer(claim.getOwnerId());
            boolean anyoneOnline = (owner != null && owner.isOnline());
            if (!anyoneOnline) {
                for (UUID memberId : claim.getMemberRoles().keySet()) {
                    Player m = Bukkit.getPlayer(memberId);
                    if (m != null && m.isOnline()) {
                        anyoneOnline = true;
                        break;
                    }
                }
            }
            if (!anyoneOnline) return false;
        }

        // Check kingdom hostility
        if (claim.getKingdomId() == null || claim.getKingdomId().isBlank()) return false;
        Optional<PlayerData> aData = plugin.getPlayerDataService().getCached(attacker.getUniqueId());
        if (aData.isEmpty() || aData.get().getRegionId() == null) return false;

        Optional<Region> aRegion = plugin.getRegionManager().getRegionById(aData.get().getRegionId());
        Optional<Region> cRegion = plugin.getRegionManager().getRegion(claim.getKingdomId());

        if (aRegion.isPresent() && cRegion.isPresent()) {
            return plugin.getWarManager().isWarActiveBetween(aRegion.get(), cRegion.get());
        }
        return false;
    }

    // --- Boundary Navigation & Titles ---

    public void handlePlayerMove(Player player, Location from, Location to) {
        if (from.getBlockX() >> 4 == to.getBlockX() >> 4 && from.getBlockZ() >> 4 == to.getBlockZ() >> 4) {
            return;
        }

        String toKey = ClaimChunk.buildChunkKey(to.getWorld().getName(), to.getBlockX() >> 4, to.getBlockZ() >> 4);
        String fromKey = lastPlayerChunkKey.put(player.getUniqueId(), toKey);

        Optional<ClaimChunk> toClaimOpt = getClaimAt(to);
        Optional<ClaimChunk> fromClaimOpt = Optional.ofNullable(fromKey != null ? claims.get(fromKey) : null);

        // Entering new territory
        if (toClaimOpt.isPresent()) {
            ClaimChunk toClaim = toClaimOpt.get();
            if (fromClaimOpt.isEmpty() || !fromClaimOpt.get().getOwnerId().equals(toClaim.getOwnerId())) {
                sendTerritoryGreeting(player, toClaim);
            }
        } else if (fromClaimOpt.isPresent()) {
            // Leaving territory
            ClaimChunk fromClaim = fromClaimOpt.get();
            sendTerritoryFarewell(player, fromClaim);
        }
    }

    private void sendTerritoryGreeting(Player player, ClaimChunk claim) {
        String greeting = claim.getFlag("greeting", null);
        String statusNote = claim.isInGracePeriod() ? " <red>[MENUNGGAK PAJAK]</red>" : "";

        if (greeting != null && !greeting.isBlank()) {
            player.sendActionBar(mm.deserialize(greeting + statusNote));
        } else {
            String ownerDisplay = claim.isOwner(player.getUniqueId()) ? "<green>Wilayah Anda Sendiri</green>" : "<gold>Wilayah " + claim.getOwnerName() + "</gold>";
            player.sendActionBar(mm.deserialize("<gray>Memasuki</gray> " + ownerDisplay + statusNote));
        }

        // Warn owner if their own claim is delinquent
        if (claim.isOwner(player.getUniqueId()) && claim.isInGracePeriod()) {
            player.sendMessage(mm.deserialize("<red><b>⚠ [PAJAK TERHUTANG]</b> Wilayah ini berada dalam <b>Masa Tenggang</b>! Segera setor via <yellow>/claim deposit</yellow>.</red>"));
        }
    }

    private void sendTerritoryFarewell(Player player, ClaimChunk claim) {
        String farewell = claim.getFlag("farewell", null);
        if (farewell != null && !farewell.isBlank()) {
            player.sendActionBar(mm.deserialize(farewell));
        } else {
            player.sendActionBar(mm.deserialize("<gray>Meninggalkan wilayah <gold>" + claim.getOwnerName() + "</gold> ➔ <green>Alam Liar</green></gray>"));
        }
    }

    // --- Claim & Unclaim Core ---

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
        if (maxClaims != Integer.MAX_VALUE && currentClaims >= maxClaims) {
            return new ClaimResult(false, "<red>✖ Kuota klaim Anda sudah penuh (<gold>" + currentClaims + "/" + maxClaims + "</gold> chunks)! Tingkatkan kasta atau rank untuk mendapatkan lebih banyak tanah.</red>");
        }

        // Determine player's Kingdom
        String kingdomKey = null;
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player)) {
            kingdomKey = "AETHERION";
        } else if (plugin.getRegionManager() != null) {
            Optional<PlayerData> pData = plugin.getPlayerDataService().getCached(player.getUniqueId());
            if (pData.isPresent() && pData.get().getRegionId() != null) {
                Optional<Region> reg = plugin.getRegionManager().getRegionById(pData.get().getRegionId());
                if (reg.isPresent()) {
                    kingdomKey = reg.get().getKey();
                }
            }
        }

        double initialRate = calculateChunkDailyTax(player.getUniqueId());
        ClaimChunk newClaim = new ClaimChunk(UUID.randomUUID(), player.getUniqueId(), player.getName(),
                worldName, chunk.getX(), chunk.getZ(), null, null, null,
                0.0, initialRate, ClaimStatus.ACTIVE, 0L, System.currentTimeMillis(), kingdomKey, System.currentTimeMillis());

        claims.put(key, newClaim);
        repository.saveClaim(newClaim);
        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        showChunkBoundary(player, chunk);
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.7f, 1.2f);

        String maxStr = maxClaims == Integer.MAX_VALUE ? "∞" : String.valueOf(maxClaims);
        String taxNotice = initialRate > 0
                ? "Pajak harian: <gold>Rp" + String.format("%,.0f", initialRate) + "/hari</gold>. Setor saldo via <yellow>/claim deposit</yellow>."
                : "<aqua>Wilayah Dewan Aetherion (Upper Dimension — Bebas Pajak Upkeep).</aqua>";

        return new ClaimResult(true, "<green>✔ Berhasil mengklaim tanah di chunk <gold>[" + chunk.getX() + ", " + chunk.getZ() + "]</gold>! Kuota terpakai: <gold>" + (currentClaims + 1) + "/" + maxStr + "</gold>. " + taxNotice + "</green>");
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

    // --- Role & Flag Commands ---

    public ClaimResult setRole(Player owner, UUID targetId, String targetName, ClaimRole role) {
        List<ClaimChunk> ownerClaims = getClaimsByOwner(owner.getUniqueId());
        if (ownerClaims.isEmpty()) {
            return new ClaimResult(false, "<yellow>⚠ Anda belum memiliki wilayah klaim.</yellow>");
        }

        for (ClaimChunk c : ownerClaims) {
            c.setRole(targetId, role);
            repository.updateClaimRoles(c);
        }

        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        if (role == null || role == ClaimRole.VISITOR) {
            return new ClaimResult(true, "<gold>✔ Izin warga <yellow>" + targetName + "</yellow> telah dicabut dari seluruh wilayah klaim Anda.</gold>");
        } else {
            return new ClaimResult(true, "<green>✔ Pemain <gold>" + targetName + "</gold> kini memiliki peran " + role.getBadge() + " di seluruh wilayah klaim Anda.</green>");
        }
    }

    public ClaimResult setFlag(Player player, String flagKey, String value) {
        Optional<ClaimChunk> claimOpt = getClaimAt(player.getLocation());
        if (claimOpt.isEmpty()) {
            return new ClaimResult(false, "<yellow>⚠ Berdirilah di dalam petak klaim Anda untuk mengubah pengaturan flag!</yellow>");
        }
        ClaimChunk claim = claimOpt.get();
        if (!claim.isOwner(player.getUniqueId()) && !claim.getRole(player.getUniqueId()).isAtLeast(ClaimRole.MANAGER) && !player.isOp()) {
            return new ClaimResult(false, "<red>✖ Anda memerlukan peran minimal <gradient:#00c6ff:#0072ff>Pengelola (Manager)</gradient> untuk mengubah flag wilayah!</red>");
        }

        claim.setFlag(flagKey, value);
        repository.updateClaimFlags(claim);

        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncClaimsAsync(getAllClaims());
        }

        return new ClaimResult(true, "<green>✔ Flag <gold>" + flagKey + "</gold> wilayah ini telah diatur ke: <yellow>" + value + "</yellow>.</green>");
    }

    public ClaimResult trustPlayer(Player owner, UUID targetId, String targetName) {
        return setRole(owner, targetId, targetName, ClaimRole.BUILDER);
    }

    public ClaimResult untrustPlayer(Player owner, UUID targetId, String targetName) {
        return setRole(owner, targetId, targetName, ClaimRole.VISITOR);
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
            int ticksLeft = visualizerDurationSeconds * 2;

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

    public boolean isProtectBlocks() { return protectBlocks; }
    public boolean isProtectContainers() { return protectContainers; }
    public boolean isProtectDoorsGates() { return protectDoorsGates; }
    public boolean isProtectRedstone() { return protectRedstone; }
    public boolean isProtectPassiveEntities() { return protectPassiveEntities; }
    public boolean isPreventExplosions() { return preventExplosions; }
    public boolean isPreventFluidPlacing() { return preventFluidPlacing; }

    public record ClaimResult(boolean success, String message) {}
}
