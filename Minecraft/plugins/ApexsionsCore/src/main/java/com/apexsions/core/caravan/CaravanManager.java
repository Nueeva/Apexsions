package com.apexsions.core.caravan;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.integration.EconomyBridge;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * The Wandering Black Market Caravan: a roaming money-sink NPC that appears at
 * a secret wilderness coordinate for a limited window on configured days.
 *
 * Deliberately transient: nothing is persisted, so a restart simply re-rolls
 * the next spawn. Purchases are economy transactions with optional command
 * rewards (titles/cosmetics) executed on the main thread.
 */
public class CaravanManager {

    private static final Set<Material> UNSAFE_GROUND = Set.of(
            Material.WATER, Material.LAVA, Material.BUBBLE_COLUMN,
            Material.MAGMA_BLOCK, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.CACTUS, Material.SWEET_BERRY_BUSH, Material.POWDER_SNOW,
            Material.FIRE, Material.SOUL_FIRE, Material.COBWEB,
            Material.SEAGRASS, Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT,
            Material.ICE, Material.PACKED_ICE, Material.FROSTED_ICE, Material.BLUE_ICE,
            Material.POINTED_DRIPSTONE, Material.LILY_PAD, Material.BARRIER
    );

    private final ApexsionsCorePlugin plugin;
    private final EconomyBridge economy;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Random random = new Random();

    private final List<CaravanOffer> offers = new ArrayList<>();

    private BukkitTask schedulerTask;
    private boolean isSpawning = false;
    @Nullable
    private Location activeLocation;
    @Nullable
    private Long activeUntil;
    @Nullable
    private Long nextAnnounceAt;

    public CaravanManager(@NotNull ApexsionsCorePlugin plugin, @NotNull EconomyBridge economy) {
        this.plugin = plugin;
        this.economy = economy;
        reload();
    }

    // --- Configuration ---

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("caravan.enabled", true);
    }

    public boolean broadcastAnnouncements() {
        return plugin.getConfig().getBoolean("caravan.broadcast-announcements", true);
    }

    public int getAnnounceIntervalMinutes() {
        return Math.max(1, plugin.getConfig().getInt("caravan.announce-interval-minutes", 15));
    }

    public long getDurationMinutes() {
        return Math.max(1L, plugin.getConfig().getLong("caravan.duration-minutes", 120L));
    }

    public int getGapMinutes() {
        return Math.max(1, plugin.getConfig().getInt("caravan.gap-minutes", 15));
    }

    public int getZoneRadius() {
        return Math.max(32, plugin.getConfig().getInt("caravan.zone-radius", 3000));
    }

    public List<DayOfWeek> getActiveDays() {
        List<String> raw = plugin.getConfig().getStringList("caravan.active-days");
        List<DayOfWeek> days = new ArrayList<>();
        if (raw != null) {
            for (String s : raw) {
                if (s == null) continue;
                try {
                    days.add(DayOfWeek.valueOf(s.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Invalid caravan.active-days entry: " + s);
                }
            }
        }
        return days;
    }

    public List<CaravanOffer> getOffers() {
        return Collections.unmodifiableList(offers);
    }

    public void reload() {
        offers.clear();
        ConfigurationSection offerSection = plugin.getConfig().getConfigurationSection("caravan.offers");
        if (offerSection == null) {
            return;
        }
        for (String key : offerSection.getKeys(false)) {
            ConfigurationSection s = offerSection.getConfigurationSection(key);
            if (s == null) continue;
            Material icon = Material.matchMaterial(s.getString("icon", "CHEST"));
            if (icon == null) icon = Material.CHEST;
            Material material = s.contains("material") ? Material.matchMaterial(s.getString("material")) : null;
            offers.add(new CaravanOffer(
                    key,
                    icon,
                    s.getString("display-name"),
                    s.getString("price-currency", EconomyBridge.DEFAULT_CURRENCY),
                    s.getDouble("price", 10000.0),
                    material,
                    Math.max(1, s.getInt("amount", 1)),
                    s.getString("command")
            ));
        }
        if (!offers.isEmpty()) {
            plugin.getLogger().info("Loaded " + offers.size() + " caravan offer(s).");
        }
    }

    @Nullable
    public CaravanOffer getOffer(@NotNull String id) {
        for (CaravanOffer offer : offers) {
            if (offer.id().equalsIgnoreCase(id)) {
                return offer;
            }
        }
        return null;
    }

    // --- Lifecycle ---

    public void start() {
        if (!isEnabled()) {
            return;
        }
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }
        long interval = Math.max(100L, plugin.getConfig().getLong("caravan.check-interval-ticks", 200L));
        schedulerTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
            schedulerTask = null;
        }
        despawn();
    }

    private void tick() {
        if (!isEnabled()) {
            return;
        }
        if (activeLocation != null) {
            if (activeUntil != null && System.currentTimeMillis() >= activeUntil) {
                announce("<gradient:#9b59b6:#e74c3c><bold>KAFILAH PASAR GELAP</bold></gradient> <gray>telah beranjak pergi. Sampai jumpa akhir pekan depan!</gray>");
                despawn();
                return;
            }
            if (nextAnnounceAt != null && System.currentTimeMillis() >= nextAnnounceAt) {
                announceLocation();
                // Advance the schedule, otherwise this fires on every tick.
                nextAnnounceAt = System.currentTimeMillis() + getAnnounceIntervalMinutes() * 60_000L;
            }
            return;
        }
        trySpawn();
    }

    private void trySpawn() {
        if (!isActiveDay()) {
            return;
        }
        if (findExisting() != null || activeLocation != null || isSpawning) {
            return; // admin-placed caravan still standing or search already running
        }
        spawnRandomWilderness(null);
    }

    /**
     * Finds a safe wilderness land location and spawns the caravan NPC.
     */
    public void spawnRandomWilderness(@Nullable Player adminNotifier) {
        if (isSpawning) {
            if (adminNotifier != null) {
                adminNotifier.sendMessage(mm.deserialize("<yellow>Pencarian lokasi kafilah sedang berlangsung...</yellow>"));
            }
            return;
        }
        String worldName = plugin.getConfig().getString("caravan.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            if (adminNotifier != null) {
                adminNotifier.sendMessage(mm.deserialize("<red>Dunia '" + worldName + "' belum dimuat di server.</red>"));
            }
            plugin.getLogger().warning("Caravan world '" + worldName + "' not loaded.");
            return;
        }

        isSpawning = true;
        findSafeLocationAsync(world, 0, 50, loc -> {
            isSpawning = false;
            if (loc == null) {
                plugin.getLogger().warning("Kafilah Pasar Gelap: Gagal menemukan daratan aman setelah 50 percobaan.");
                if (adminNotifier != null) {
                    adminNotifier.sendMessage(mm.deserialize("<red>Gagal menemukan daratan aman untuk kafilah setelah 50 percobaan.</red>"));
                }
                return;
            }

            if (!spawn(loc)) {
                if (adminNotifier != null) {
                    adminNotifier.sendMessage(mm.deserialize("<red>Gagal memunculkan entity kafilah di lokasi tujuan.</red>"));
                }
                return;
            }

            activeUntil = System.currentTimeMillis() + getDurationMinutes() * 60_000L;
            nextAnnounceAt = System.currentTimeMillis() + getAnnounceIntervalMinutes() * 60_000L;
            announce("<gradient:#9b59b6:#e74c3c><bold>KAFILAH PASAR GELAP MUNCUL!</bold></gradient> <gray>Pedagang misterius berkelana di dunia liar dengan barang langka.</gray>");
            announceLocation();
            if (adminNotifier != null) {
                adminNotifier.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>KAFILAH BERHASIL DIPINDAHKAN!</bold></gradient> <gray>Lokasi daratan baru: <gold>X: "
                        + (int) loc.getX() + ", Y: " + (int) loc.getY() + ", Z: " + (int) loc.getZ() + "</gold></gray>"));
            }
        });
    }

    private void findSafeLocationAsync(@NotNull World world, int attempt, int maxAttempts, @NotNull Consumer<Location> callback) {
        if (attempt >= maxAttempts) {
            callback.accept(null);
            return;
        }

        int radius = getZoneRadius();
        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;

        // Hindari area spawn utama
        if (Math.hypot(x, z) < 150) {
            findSafeLocationAsync(world, attempt + 1, maxAttempts, callback);
            return;
        }

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        world.getChunkAtAsync(chunkX, chunkZ).thenAccept(chunk -> Bukkit.getScheduler().runTask(plugin, () -> {
            // 1. Cek Bioma: Dilarang keras bioma laut, sungai, dan void
            Biome biome = world.getBiome(x, 64, z);
            String biomeName = biome.name().toUpperCase(Locale.ROOT);
            if (biomeName.contains("OCEAN") || biomeName.contains("RIVER") || biomeName.contains("VOID")) {
                findSafeLocationAsync(world, attempt + 1, maxAttempts, callback);
                return;
            }

            // 2. Ketinggian permukaan
            int highestY = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            if (highestY < world.getMinHeight() + 5 || highestY > world.getMaxHeight() - 5) {
                findSafeLocationAsync(world, attempt + 1, maxAttempts, callback);
                return;
            }

            Block ground = world.getBlockAt(x, highestY, z);
            Block feet = world.getBlockAt(x, highestY + 1, z);
            Block head = world.getBlockAt(x, highestY + 2, z);

            // 3. Validasi blok daratan kokoh & aman (bukan air, lava, daun, api, es terapung)
            if (!isSafeGround(ground) || !isSafePassThrough(feet) || !isSafePassThrough(head)) {
                findSafeLocationAsync(world, attempt + 1, maxAttempts, callback);
                return;
            }

            // 4. Cek blok sekeliling untuk memastikan daratan luas & bukan tepian tebing cairan
            if (!isSurroundingSafe(world, x, highestY, z)) {
                findSafeLocationAsync(world, attempt + 1, maxAttempts, callback);
                return;
            }

            callback.accept(new Location(world, x + 0.5, highestY + 1.0, z + 0.5));
        })).exceptionally(ex -> {
            Bukkit.getScheduler().runTask(plugin, () -> findSafeLocationAsync(world, attempt + 1, maxAttempts, callback));
            return null;
        });
    }

    private boolean isSafeGround(@Nullable Block block) {
        if (block == null || block.isEmpty() || block.isLiquid()) {
            return false;
        }
        Material mat = block.getType();
        if (UNSAFE_GROUND.contains(mat)) {
            return false;
        }
        if (mat.name().contains("LEAVES") || mat.name().contains("WATER") || mat.name().contains("LAVA")) {
            return false;
        }
        return mat.isSolid();
    }

    private boolean isSafePassThrough(@Nullable Block block) {
        if (block == null) {
            return false;
        }
        if (block.isLiquid() || block.getType() == Material.WATER || block.getType() == Material.LAVA) {
            return false;
        }
        Material mat = block.getType();
        if (mat == Material.FIRE || mat == Material.SOUL_FIRE || mat == Material.CAMPFIRE
                || mat == Material.SOUL_CAMPFIRE || mat == Material.SWEET_BERRY_BUSH
                || mat == Material.CACTUS || mat == Material.POWDER_SNOW) {
            return false;
        }
        return block.isEmpty() || block.isPassable();
    }

    private boolean isSurroundingSafe(@NotNull World world, int cx, int y, int cz) {
        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : deltas) {
            Block adjGround = world.getBlockAt(cx + d[0], y, cz + d[1]);
            if (adjGround.isLiquid() || adjGround.getType() == Material.WATER || adjGround.getType() == Material.LAVA) {
                return false;
            }
        }
        return true;
    }

    private boolean isActiveDay() {
        List<DayOfWeek> days = getActiveDays();
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        return days.isEmpty() || days.contains(today);
    }

    private void announceLocation() {
        if (activeLocation == null) {
            return;
        }
        announce("<gray>Petunjuk koordinat: <gold>X: " + (int) activeLocation.getX()
                + ", Y: " + (int) activeLocation.getY() + ", Z: " + (int) activeLocation.getZ() + "</gold> <dark_gray>|</dark_gray> <yellow>Dunia: "
                + activeLocation.getWorld().getName() + "</yellow>");
    }

    private void announce(@NotNull String miniMessage) {
        if (broadcastAnnouncements()) {
            Bukkit.broadcast(mm.deserialize(miniMessage));
        }
    }

    // --- Placement ---

    /**
     * Spawns a villager caravan at the location. Returns false when unavailable.
     */
    public boolean spawn(@NotNull Location loc) {
        despawn();
        World world = loc.getWorld();
        if (world == null) {
            return false;
        }
        try {
            org.bukkit.entity.Villager villager = world.spawn(loc, org.bukkit.entity.Villager.class, v -> {
                v.setAI(false);
                v.setInvulnerable(true);
                v.setPersistent(false);
                v.setRemoveWhenFarAway(false);
                v.customName(mm.deserialize(plugin.getConfig().getString("caravan.display-name",
                        "<gradient:#9b59b6:#e74c3c><bold>Kafilah Pasar Gelap</bold></gradient>")));
                v.setCustomNameVisible(true);
                v.setProfession(org.bukkit.entity.Villager.Profession.CLERIC);
                v.setVillagerType(org.bukkit.entity.Villager.Type.SWAMP);
                v.getPersistentDataContainer().set(caravanKey(),
                        org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            });
            activeLocation = villager.getLocation();
            return true;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed spawning caravan", t);
            return false;
        }
    }

    public void despawn() {
        org.bukkit.entity.Entity existing = findExisting();
        if (existing != null) {
            existing.remove();
        } else if (activeLocation != null && activeLocation.getWorld() != null) {
            World w = activeLocation.getWorld();
            int cx = activeLocation.getBlockX() >> 4;
            int cz = activeLocation.getBlockZ() >> 4;
            if (w.isChunkLoaded(cx, cz)) {
                for (org.bukkit.entity.Entity e : w.getChunkAt(cx, cz).getEntities()) {
                    if (isCaravanNpc(e)) {
                        e.remove();
                    }
                }
            }
        }
        activeLocation = null;
        activeUntil = null;
        nextAnnounceAt = null;
        isSpawning = false;
    }

    @Nullable
    private org.bukkit.entity.Entity findExisting() {
        String worldName = plugin.getConfig().getString("caravan.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof org.bukkit.entity.Villager villager
                    && villager.getPersistentDataContainer().has(caravanKey(), org.bukkit.persistence.PersistentDataType.BYTE)) {
                return entity;
            }
        }
        return null;
    }

    private org.bukkit.NamespacedKey caravanKey() {
        return new org.bukkit.NamespacedKey(plugin, "caravan_npc");
    }

    /** True when the entity is the caravan merchant NPC. */
    public boolean isCaravanNpc(@Nullable org.bukkit.entity.Entity entity) {
        return entity instanceof org.bukkit.entity.Villager villager
                && villager.getPersistentDataContainer().has(caravanKey(), org.bukkit.persistence.PersistentDataType.BYTE);
    }

    public boolean isActive() {
        return getActiveLocation() != null;
    }

    @Nullable
    public Location getActiveLocation() {
        if (activeLocation != null) {
            return activeLocation;
        }
        org.bukkit.entity.Entity existing = findExisting();
        if (existing != null) {
            activeLocation = existing.getLocation();
            return activeLocation;
        }
        return null;
    }

    @Nullable
    public String getRemainingFormatted() {
        if (activeUntil == null) {
            return null;
        }
        long remaining = Math.max(0, activeUntil - System.currentTimeMillis());
        long min = remaining / 60_000L;
        if (min >= 60) {
            return (min / 60) + " jam " + (min % 60) + " menit";
        }
        return min + " menit";
    }

    /** Time until the configured active window opens, for status displays. */
    @NotNull
    public String getScheduleDescription() {
        List<DayOfWeek> days = getActiveDays();
        if (days.isEmpty()) {
            return "setiap hari";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            if (i > 0) sb.append(", ");
            String name = days.get(i).name().toLowerCase(Locale.ROOT);
            sb.append(Character.toUpperCase(name.charAt(0))).append(name.substring(1));
        }
        return sb.toString();
    }

    // --- Purchases ---

    /**
     * Executes a purchase. Currency is withdrawn first; the reward is granted
     * only after a successful withdrawal. Returns a user-facing result message.
     */
    @NotNull
    public String purchase(@NotNull Player player, @NotNull CaravanOffer offer) {
        if (!economy.isAvailable()) {
            return "<red>Ekonomi tidak tersedia saat ini.</red>";
        }
        String currency = offer.priceCurrency();
        if (!economy.has(player.getUniqueId(), currency, offer.price())) {
            return "<red>Saldo Anda tidak cukup. Harga: <gold>" + economy.format(offer.price(), currency) + "</gold>.</red>";
        }
        if (!economy.withdraw(player.getUniqueId(), currency, offer.price())) {
            return "<red>Transaksi gagal: penarikan saldo ditolak.</red>";
        }

        if (offer.isCommandOffer()) {
            String cmd = offer.command().replace("%player%", player.getName());
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            return "<gradient:#2ecc71:#27ae60><bold>PEMBELIAN BERHASIL!</bold></gradient> <gray>Anda membeli</gray> <yellow>"
                    + displayName(offer) + "</yellow><gray>.</gray>";
        }

        if (offer.material() != null) {
            org.bukkit.inventory.ItemStack reward = new org.bukkit.inventory.ItemStack(offer.material(), offer.amount());
            java.util.Map<Integer, org.bukkit.inventory.ItemStack> leftover = player.getInventory().addItem(reward);
            for (org.bukkit.inventory.ItemStack rest : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), rest);
            }
            return "<gradient:#2ecc71:#27ae60><bold>PEMBELIAN BERHASIL!</bold></gradient> <gray>Anda menerima <yellow>"
                    + offer.amount() + "x " + displayName(offer) + "</yellow>.</gray>";
        }

        // Nothing to grant (config error): refund to avoid stealing money.
        economy.deposit(player.getUniqueId(), currency, offer.price());
        return "<red>Penawaran ini salah konfigurasi dan saldo Anda dikembalikan.</red>";
    }

    public double getBalance(@NotNull Player player) {
        return economy.getBalance(player.getUniqueId(), EconomyBridge.DEFAULT_CURRENCY);
    }

    @NotNull
    public String format(double amount, @NotNull String currencyId) {
        return economy.format(amount, currencyId);
    }

    @NotNull
    public String displayName(@NotNull CaravanOffer offer) {
        if (offer.displayName() != null && !offer.displayName().isBlank()) {
            return offer.displayName();
        }
        if (offer.material() != null) {
            String n = offer.material().name().toLowerCase(Locale.ROOT).replace('_', ' ');
            return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
        return offer.id();
    }

    /** Active day helper used by /caravan status. */
    @NotNull
    public String describeWindow() {
        long until = activeUntil != null ? activeUntil : 0L;
        if (until <= 0) {
            return "tidak aktif";
        }
        ZonedDateTime zdt = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(until), ZoneId.systemDefault());
        return "berakhir " + zdt.truncatedTo(ChronoUnit.MINUTES).toLocalTime();
    }
}
