package com.apexsions.core.sions;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.TerritoryPolygon;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Manages the Lore-Friendly Temporal Reconstruction Engine for Kerajaan Sions.
 * Features:
 * - Persistent Baseline Snapshotting (/sions set) saved to disk
 * - Real-time tracking of breaks, places, and explosions
 * - Proximity countdown broadcasts (10m, 5m, 1m, 10s) to players within Sions
 * - Ancient Key validation and management for protected containers
 */
public class SionsTemporalService {

    private static final int BASELINE_MAGIC = 0x53494F4E; // "SION"
    private static final int BASELINE_VERSION = 1;

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final NamespacedKey keySionsPdc;

    // Active session modifications: Location -> Pristine BlockData
    private final Map<Location, BlockData> originalBlocks = new ConcurrentHashMap<>();

    // Persistent Ground-Truth Baseline: BlockCoord -> BlockData
    public record BlockCoord(int x, int y, int z) implements Serializable {}
    private final Map<BlockCoord, BlockData> baselineBlocks = new ConcurrentHashMap<>();

    // Admin bypass set (permanent builders)
    private final Set<UUID> bypassPlayers = ConcurrentHashMap.newKeySet();

    private BukkitTask countdownTask;
    private long nextResetTimeMs = 0L;
    private int intervalMinutes = 60;
    private File baselineFile;

    public SionsTemporalService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.keySionsPdc = new NamespacedKey(plugin, "sions_key");
        this.baselineFile = new File(plugin.getDataFolder(), "sions_baseline.dat");
    }

    public void start() {
        if (!isEnabled()) {
            plugin.getLogger().info("Sions Temporal Reconstruction is disabled in config.");
            return;
        }

        this.intervalMinutes = Math.max(1, plugin.getConfig().getInt("sions-temporal.interval-minutes", 60));
        this.nextResetTimeMs = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);

        // Load baseline from disk if present
        loadBaseline();

        if (countdownTask != null) {
            countdownTask.cancel();
        }

        // Ticking countdown task every 1 second (20 ticks)
        this.countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, this::onCountdownTick, 20L, 20L);

        plugin.getLogger().info("Sions Temporal Reconstruction Engine active. Reset interval: " + intervalMinutes + " minutes. Baseline blocks loaded: " + baselineBlocks.size());
    }

    public void stop() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    public void onDisable() {
        stop();
        if (plugin.getConfig().getBoolean("sions-temporal.restore-on-shutdown", true) && (!originalBlocks.isEmpty() || !baselineBlocks.isEmpty())) {
            int count = restoreAll(false);
            plugin.getLogger().info("Sions Temporal Engine: Cleaned up and restored " + count + " blocks on shutdown.");
        }
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("sions-temporal.enabled", true);
    }

    public boolean isPreventItemDrops() {
        return plugin.getConfig().getBoolean("sions-temporal.prevent-item-drops", true);
    }

    public boolean isContainerLockEnabled() {
        return plugin.getConfig().getBoolean("sions-temporal.container-lock.enabled", true);
    }

    public boolean isConsumeKeyOnUse() {
        return plugin.getConfig().getBoolean("sions-temporal.container-lock.consume-key", false);
    }

    public boolean isInSions(Location location) {
        if (location == null || location.getWorld() == null) return false;
        Optional<Region> sionsOpt = plugin.getRegionManager().getRegion("SIONS");
        return sionsOpt.map(region -> region.containsLocation(location)).orElse(false);
    }

    public boolean isBypassing(Player player) {
        if (player == null) return false;
        return bypassPlayers.contains(player.getUniqueId());
    }

    public boolean toggleBypass(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        if (bypassPlayers.contains(uuid)) {
            bypassPlayers.remove(uuid);
            return false;
        } else {
            bypassPlayers.add(uuid);
            return true;
        }
    }

    /**
     * Ticking loop handling proximity countdown broadcasts and triggering reset.
     */
    private void onCountdownTick() {
        long remainingSec = getRemainingSeconds();

        // Warning alerts at 10m (600s), 5m (300s), 1m (60s), and countdown 10..1s
        if (remainingSec == 600 || remainingSec == 300 || remainingSec == 60 || (remainingSec <= 10 && remainingSec > 0)) {
            broadcastCountdownAlert(remainingSec);
        }

        if (remainingSec <= 0) {
            restoreAll(true);
        }
    }

    private void broadcastCountdownAlert(long sec) {
        String timeStr;
        if (sec >= 60) {
            long min = sec / 60;
            timeStr = min + " Menit";
        } else {
            timeStr = sec + " Detik";
        }

        String chatMsg = "<dark_purple>✦ [Kerajaan Sions]</dark_purple> <yellow>Segel waktu kuno bergetar! Wilayah ini akan mengalami pemulihan temporal dalam <aqua><bold>" + timeStr + "</bold></aqua>.</yellow>";
        String actionMsg = "<gradient:#8e44ad:#d4af37>⏳ Pemulihan Temporal Sions: <yellow><bold>" + timeStr + "</bold></yellow></gradient>";

        boolean serverWide = plugin.getConfig().getBoolean("sions-temporal.broadcast-server", false);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (serverWide || isInSions(p.getLocation())) {
                if (sec == 600 || sec == 300 || sec == 60 || sec == 10) {
                    p.sendMessage(miniMessage.deserialize(chatMsg));
                }
                p.sendActionBar(miniMessage.deserialize(actionMsg));

                if (sec <= 5) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.4f);
                } else if (sec == 10 || sec == 60) {
                    p.playSound(p.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.2f);
                }
            }
        }
    }

    /**
     * Records a block's pristine state before modification.
     */
    public void recordBlockState(Location loc, BlockData originalState) {
        if (!isEnabled() || loc == null || originalState == null) return;
        if (!isInSions(loc)) return;

        Location blockLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        originalBlocks.putIfAbsent(blockLoc, originalState.clone());
    }

    public void recordAirPlacement(Location loc) {
        if (!isEnabled() || loc == null) return;
        if (!isInSions(loc)) return;

        Location blockLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        originalBlocks.putIfAbsent(blockLoc, Bukkit.createBlockData(Material.AIR));
    }

    /**
     * Restores all modified blocks in Kerajaan Sions back to their original states.
     */
    public int restoreAll(boolean announce) {
        int restoredCount = 0;

        // 1. Restore active session modifications
        if (!originalBlocks.isEmpty()) {
            for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
                Location loc = entry.getKey();
                BlockData originalData = entry.getValue();

                if (loc.getWorld() != null) {
                    Block block = loc.getBlock();
                    block.setBlockData(originalData, false);
                    if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                        loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0.5, 0.5, 0.5), 3, 0.2, 0.2, 0.2, 0.02);
                    }
                    restoredCount++;
                }
            }
            originalBlocks.clear();
        }

        // 2. If persistent baseline exists, verify against baseline for any discrepancies
        if (!baselineBlocks.isEmpty()) {
            Optional<Region> sionsOpt = plugin.getRegionManager().getRegion("SIONS");
            if (sionsOpt.isPresent()) {
                Region region = sionsOpt.get();
                World world = Bukkit.getWorld(region.getWorldName());
                if (world != null) {
                    for (Map.Entry<BlockCoord, BlockData> entry : baselineBlocks.entrySet()) {
                        BlockCoord c = entry.getKey();
                        BlockData expected = entry.getValue();

                        // Only inspect if chunk is loaded to prevent server lag spikes
                        if (world.isChunkLoaded(c.x() >> 4, c.z() >> 4)) {
                            Block block = world.getBlockAt(c.x(), c.y(), c.z());
                            if (!block.getBlockData().matches(expected)) {
                                block.setBlockData(expected, false);
                                world.spawnParticle(Particle.PORTAL, block.getLocation().add(0.5, 0.5, 0.5), 2, 0.1, 0.1, 0.1, 0.01);
                                restoredCount++;
                            }
                        }
                    }
                }
            }
        }

        // 3. Broadcast Lore Reconstruction Announcement
        if (announce && restoredCount > 0) {
            String message = "<dark_purple>✦ [SIONS] </dark_purple><light_purple><bold>REKONSTRUKSI TEMPORAL KERAJAAN SIONS!</bold></light_purple><newline>" +
                    "<gray>Waktu temporal telah berputar kembali. <gold>" + restoredCount + "</gold> struktur kuno telah menyusun kembali dirinya ke wujud semula!</gray>";

            boolean serverWide = plugin.getConfig().getBoolean("sions-temporal.broadcast-server", false);
            if (serverWide) {
                Bukkit.broadcast(miniMessage.deserialize(message));
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isInSions(player.getLocation())) {
                        player.sendMessage(miniMessage.deserialize(message));
                        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 0.8f);
                        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0f, 1.2f);
                    }
                }
            }
        }

        this.nextResetTimeMs = System.currentTimeMillis() + (intervalMinutes * 60L * 1000L);
        return restoredCount;
    }

    /**
     * Captures current world blocks within Sions territory between minY and maxY as the authoritative baseline.
     */
    public void saveBaseline(int minY, int maxY, Consumer<Integer> callback) {
        Optional<Region> sionsOpt = plugin.getRegionManager().getRegion("SIONS");
        if (sionsOpt.isEmpty()) {
            callback.accept(0);
            return;
        }

        Region sions = sionsOpt.get();
        World world = Bukkit.getWorld(sions.getWorldName());
        if (world == null) {
            callback.accept(0);
            return;
        }

        TerritoryPolygon polygon = sions.getPolygon();
        if (polygon == null || polygon.getVertices().isEmpty()) {
            callback.accept(0);
            return;
        }

        // Bounding box of the polygon
        final int fMinX = (int) Math.floor(polygon.getMinX());
        final int fMaxX = (int) Math.ceil(polygon.getMaxX());
        final int fMinZ = (int) Math.floor(polygon.getMinZ());
        final int fMaxZ = (int) Math.ceil(polygon.getMaxZ());
        final double midY = (polygon.getMinY() + polygon.getMaxY()) / 2.0;

        // Perform snapshot synchronously to capture exact block data, then serialize asynchronously
        Map<BlockCoord, BlockData> scanned = new HashMap<>();

        for (int x = fMinX; x <= fMaxX; x++) {
            for (int z = fMinZ; z <= fMaxZ; z++) {
                if (polygon.contains(x, midY, z)) {
                    for (int y = minY; y <= maxY; y++) {
                        Block b = world.getBlockAt(x, y, z);
                        if (b.getType() != Material.AIR) {
                            scanned.put(new BlockCoord(x, y, z), b.getBlockData().clone());
                        }
                    }
                }
            }
        }

        // Update in-memory baseline
        baselineBlocks.clear();
        baselineBlocks.putAll(scanned);
        originalBlocks.clear(); // Clear pending relative diffs since baseline was just saved

        // Save to GZIP data file asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(baselineFile))))) {
                out.writeInt(BASELINE_MAGIC);
                out.writeInt(BASELINE_VERSION);
                out.writeUTF(world.getName());
                out.writeInt(minY);
                out.writeInt(maxY);
                out.writeInt(scanned.size());

                for (Map.Entry<BlockCoord, BlockData> entry : scanned.entrySet()) {
                    BlockCoord c = entry.getKey();
                    out.writeInt(c.x());
                    out.writeInt(c.y());
                    out.writeInt(c.z());
                    out.writeUTF(entry.getValue().getAsString());
                }

                out.flush();
                plugin.getLogger().info("Successfully saved Sions baseline to disk (" + scanned.size() + " blocks).");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save Sions baseline file: " + e.getMessage());
            }

            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(scanned.size()));
        });
    }

    /**
     * Loads the baseline data from disk.
     */
    public void loadBaseline() {
        if (!baselineFile.exists()) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(baselineFile))))) {
            int magic = in.readInt();
            if (magic != BASELINE_MAGIC) {
                plugin.getLogger().warning("Invalid Sions baseline file format.");
                return;
            }

            int version = in.readInt();
            String worldName = in.readUTF();
            int minY = in.readInt();
            int maxY = in.readInt();
            int count = in.readInt();

            baselineBlocks.clear();
            for (int i = 0; i < count; i++) {
                int x = in.readInt();
                int y = in.readInt();
                int z = in.readInt();
                String dataStr = in.readUTF();
                try {
                    BlockData data = Bukkit.createBlockData(dataStr);
                    baselineBlocks.put(new BlockCoord(x, y, z), data);
                } catch (IllegalArgumentException ignored) {}
            }

            plugin.getLogger().info("Loaded Sions persistent baseline: " + baselineBlocks.size() + " blocks from disk.");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not read Sions baseline file: " + e.getMessage());
        }
    }

    public boolean hasBaseline() {
        return !baselineBlocks.isEmpty();
    }

    public int getBaselineBlockCount() {
        return baselineBlocks.size();
    }

    public int getModifiedBlockCount() {
        return originalBlocks.size();
    }

    public long getRemainingSeconds() {
        return Math.max(0L, (nextResetTimeMs - System.currentTimeMillis()) / 1000L);
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    // =========================================================================
    // Sions Ancient Key Management & Validation
    // =========================================================================

    /**
     * Creates an authentic Sions Ancient Key item stack.
     */
    public ItemStack createKeyItem(int amount) {
        FileConfiguration config = plugin.getConfig();
        String matName = config.getString("sions-temporal.container-lock.key.material", "TRIPWIRE_HOOK");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.TRIPWIRE_HOOK;

        ItemStack key = new ItemStack(mat, Math.max(1, amount));
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            String nameFormat = config.getString("sions-temporal.container-lock.key.name", "<gradient:#8e44ad:#d4af37><bold>Sions Ancient Key</bold></gradient>");
            meta.displayName(miniMessage.deserialize(nameFormat));

            List<String> loreLines = config.getStringList("sions-temporal.container-lock.key.lore");
            if (loreLines != null && !loreLines.isEmpty()) {
                List<net.kyori.adventure.text.Component> compLore = new ArrayList<>();
                for (String line : loreLines) {
                    compLore.add(miniMessage.deserialize(line));
                }
                meta.lore(compLore);
            }

            meta.getPersistentDataContainer().set(keySionsPdc, PersistentDataType.BOOLEAN, true);
            key.setItemMeta(meta);
        }
        return key;
    }

    /**
     * Checks whether an item stack qualifies as the Sions Ancient Key.
     */
    public boolean isSionsKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        // 1. Check persistent PDC tag
        if (meta.getPersistentDataContainer().has(keySionsPdc, PersistentDataType.BOOLEAN)) {
            return true;
        }

        // 2. Check Display Name matching config
        String configName = plugin.getConfig().getString("sions-temporal.container-lock.key.name", "Sions Ancient Key");
        net.kyori.adventure.text.Component display = meta.displayName();
        if (display != null) {
            String serialized = miniMessage.serialize(display);
            if (serialized.equalsIgnoreCase(configName) || serialized.contains("Sions Ancient Key") || meta.getDisplayName().contains("Sions Ancient Key")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a player possesses at least one Sions Key in their inventory.
     */
    public boolean hasSionsKey(Player player) {
        if (player == null) return false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isSionsKey(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Consumes 1 Sions Key from player's inventory if enabled.
     */
    public boolean consumeSionsKey(Player player) {
        if (player == null) return false;
        if (!isConsumeKeyOnUse()) return true;

        for (ItemStack item : player.getInventory().getContents()) {
            if (isSionsKey(item)) {
                int amount = item.getAmount();
                if (amount > 1) {
                    item.setAmount(amount - 1);
                } else {
                    player.getInventory().removeItem(item);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the item currently held in player's main hand as the official key item in config.yml.
     */
    public boolean setKeyItemFromHand(Player player) {
        if (player == null) return false;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            return false;
        }

        FileConfiguration config = plugin.getConfig();
        config.set("sions-temporal.container-lock.key.material", hand.getType().name());

        ItemMeta meta = hand.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            config.set("sions-temporal.container-lock.key.name", miniMessage.serialize(meta.displayName()));
        }

        if (meta != null && meta.hasLore() && meta.lore() != null) {
            List<String> rawLore = new ArrayList<>();
            for (net.kyori.adventure.text.Component c : meta.lore()) {
                rawLore.add(miniMessage.serialize(c));
            }
            config.set("sions-temporal.container-lock.key.lore", rawLore);
        }

        plugin.saveConfig();
        return true;
    }
}
