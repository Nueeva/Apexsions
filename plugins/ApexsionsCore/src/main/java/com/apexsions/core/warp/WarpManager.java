package com.apexsions.core.warp;

import com.apexsions.core.ApexsionsCorePlugin;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {

    private final ApexsionsCorePlugin plugin;
    private final Map<String, Warp> warps = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private HikariDataSource dataSource;

    public WarpManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        initDatabase();
        loadAllWarps();
    }

    private void initDatabase() {
        this.dataSource = plugin.getDatabaseManager().getDataSource();
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS core_warps (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x REAL NOT NULL,
                    y REAL NOT NULL,
                    z REAL NOT NULL,
                    yaw REAL NOT NULL,
                    pitch REAL NOT NULL,
                    permission TEXT,
                    delay_seconds INTEGER NOT NULL DEFAULT 3,
                    hidden INTEGER NOT NULL DEFAULT 0,
                    description TEXT
                );
            """);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize core_warps table: " + e.getMessage());
        }
    }

    public void loadAllWarps() {
        warps.clear();
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM core_warps");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                Material icon = Material.matchMaterial(rs.getString("icon"));
                if (icon == null) icon = Material.COMPASS;
                String world = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");
                String perm = rs.getString("permission");
                int delay = rs.getInt("delay_seconds");
                boolean hidden = rs.getInt("hidden") == 1;
                String desc = rs.getString("description");

                Warp warp = new Warp(id, name, category, icon, world, x, y, z, yaw, pitch, perm, delay, hidden, desc);
                warps.put(id.toLowerCase(), warp);
            }
            plugin.getLogger().info("Loaded " + warps.size() + " warps from database.");
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading core warps: " + e.getMessage());
        }
    }

    public CompletableFuture<Boolean> setWarp(String id, String name, String category, Material icon,
                                             Location loc, String permission, int delay, String description) {
        Warp warp = new Warp(id, name, category, icon,
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(), permission, delay, false, description);

        warps.put(id.toLowerCase(), warp);
        return saveWarp(warp);
    }

    public CompletableFuture<Boolean> saveWarp(Warp warp) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                     INSERT INTO core_warps (id, name, category, icon, world, x, y, z, yaw, pitch, permission, delay_seconds, hidden, description)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     ON CONFLICT(id) DO UPDATE SET
                         name=excluded.name, category=excluded.category, icon=excluded.icon,
                         world=excluded.world, x=excluded.x, y=excluded.y, z=excluded.z,
                         yaw=excluded.yaw, pitch=excluded.pitch, permission=excluded.permission,
                         delay_seconds=excluded.delay_seconds, hidden=excluded.hidden, description=excluded.description
                 """)) {
                ps.setString(1, warp.getId());
                ps.setString(2, warp.getName());
                ps.setString(3, warp.getCategory());
                ps.setString(4, warp.getIcon().name());
                ps.setString(5, warp.getWorldName());
                ps.setDouble(6, warp.getX());
                ps.setDouble(7, warp.getY());
                ps.setDouble(8, warp.getZ());
                ps.setFloat(9, warp.getYaw());
                ps.setFloat(10, warp.getPitch());
                ps.setString(11, warp.getPermission());
                ps.setInt(12, warp.getDelaySeconds());
                ps.setInt(13, warp.isHidden() ? 1 : 0);
                ps.setString(14, warp.getDescription());
                ps.executeUpdate();
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save warp " + warp.getId() + ": " + e.getMessage());
                return false;
            }
        });
    }

    public boolean deleteWarp(String id) {
        Warp removed = warps.remove(id.toLowerCase());
        if (removed != null) {
            CompletableFuture.runAsync(() -> {
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM core_warps WHERE id = ?")) {
                    ps.setString(1, id.toLowerCase());
                    ps.executeUpdate();
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to delete warp " + id + ": " + e.getMessage());
                }
            });
            return true;
        }
        return false;
    }

    public Warp getWarp(String id) {
        if (id == null) return null;
        return warps.get(id.toLowerCase());
    }

    public Collection<Warp> getWarps() {
        return Collections.unmodifiableCollection(warps.values());
    }

    public List<String> getCategories() {
        Set<String> cats = new LinkedHashSet<>();
        cats.add("ALL");
        for (Warp w : warps.values()) {
            if (w.getCategory() != null && !w.getCategory().isBlank()) {
                cats.add(w.getCategory().toUpperCase());
            }
        }
        return new ArrayList<>(cats);
    }

    public void teleportPlayer(Player player, Warp warp) {
        if (player == null || !player.isOnline() || warp == null) return;

        // Check permission
        if (!warp.canAccess(player)) {
            player.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk teleportasi ke warp <yellow>" + warp.getName() + "</yellow>!</red>"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Check Combat Tag
        if (plugin.getCombatTagManager() != null && plugin.getCombatTagManager().isInCombat(player)) {
            player.sendMessage(mm.deserialize("<red>✖ Teleportasi dibatalkan karena kamu sedang dalam pertempuran (Combat Tag)!</red>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // Check Kingdom War Active
        if (plugin.getKingdomWarManager() != null && plugin.getKingdomWarManager().isWarActiveFor(player)) {
            player.sendMessage(mm.deserialize("<red>✖ Teleportasi dibatalkan karena kerajaanmu sedang dalam status War Aktif!</red>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        Location target = warp.getLocation();
        if (target == null || target.getWorld() == null) {
            player.sendMessage(mm.deserialize("<red>Dunia tujuan warp '<yellow>" + warp.getWorldName() + "</yellow>' tidak tersedia.</red>"));
            return;
        }

        cancelPendingTeleport(player.getUniqueId());

        int delay = warp.getDelaySeconds();
        boolean bypassDelay = player.hasPermission("apexsionscore.warp.bypass") || player.hasPermission("kingdomcore.admin");

        if (delay <= 0 || bypassDelay) {
            executeTeleport(player, warp, target);
            return;
        }

        // Start countdown
        Location startLoc = player.getLocation().clone();
        player.sendMessage(mm.deserialize("<yellow>⏳ Mempersiapkan teleportasi ke <gold>" + warp.getName() + "</gold> dalam <white>" + delay + "</white> detik. <red>Jangan bergerak!</red></yellow>"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int remaining = delay;
            double angle = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelPendingTeleport(player.getUniqueId());
                    return;
                }

                // Check movement
                if (player.getLocation().distanceSquared(startLoc) > 0.15) {
                    player.sendMessage(mm.deserialize("<red>✖ Teleportasi dibatalkan karena kamu bergerak!</red>"));
                    player.sendActionBar(mm.deserialize("<red><bold>✖ TELEPORTASI DIBATALKAN</bold></red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    cancelPendingTeleport(player.getUniqueId());
                    return;
                }

                // Particle effects during warmup
                Location pLoc = player.getLocation().clone();
                for (int i = 0; i < 4; i++) {
                    angle += Math.PI / 8;
                    double xOff = Math.cos(angle) * 0.8;
                    double zOff = Math.sin(angle) * 0.8;
                    pLoc.getWorld().spawnParticle(Particle.PORTAL, pLoc.clone().add(xOff, (angle % 2.0), zOff), 1, 0, 0, 0, 0);
                    pLoc.getWorld().spawnParticle(Particle.ENCHANT, pLoc.clone().add(-xOff, (angle % 2.0), -zOff), 1, 0, 0, 0, 0);
                }

                remaining--;
                if (remaining > 0) {
                    player.sendActionBar(mm.deserialize("<gradient:#f39c12:#f1c40f><bold>✦ TELEPORTASI KE " + warp.getName().toUpperCase() + " DALAM " + remaining + " DETIK... ✦</bold></gradient>"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.4f);
                } else {
                    cancelPendingTeleport(player.getUniqueId());
                    executeTeleport(player, warp, target);
                }
            }
        }, 20L, 20L);

        pendingTeleports.put(player.getUniqueId(), task);
    }

    public Location getSafeLandingLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return loc;
        World world = loc.getWorld();
        int blockX = loc.getBlockX();
        int blockY = loc.getBlockY();
        int blockZ = loc.getBlockZ();

        org.bukkit.block.Block feet = world.getBlockAt(blockX, blockY, blockZ);
        org.bukkit.block.Block head = world.getBlockAt(blockX, blockY + 1, blockZ);
        org.bukkit.block.Block ground = world.getBlockAt(blockX, blockY - 1, blockZ);

        if (feet.getType() == Material.LAVA || head.getType() == Material.LAVA || ground.getType() == Material.LAVA || ground.getType() == Material.VOID_AIR) {
            org.bukkit.block.Block highest = world.getHighestBlockAt(blockX, blockZ);
            Location safe = highest.getLocation().add(0.5, 1.0, 0.5);
            safe.setYaw(loc.getYaw());
            safe.setPitch(loc.getPitch());
            return safe;
        }

        if (feet.getType().isSolid() || head.getType().isSolid()) {
            for (int y = blockY; y < world.getMaxHeight() - 2; y++) {
                org.bukkit.block.Block b1 = world.getBlockAt(blockX, y, blockZ);
                org.bukkit.block.Block b2 = world.getBlockAt(blockX, y + 1, blockZ);
                org.bukkit.block.Block bBelow = world.getBlockAt(blockX, y - 1, blockZ);
                if (!b1.getType().isSolid() && !b2.getType().isSolid() && bBelow.getType().isSolid()) {
                    Location safe = b1.getLocation().add(0.5, 0.0, 0.5);
                    safe.setYaw(loc.getYaw());
                    safe.setPitch(loc.getPitch());
                    return safe;
                }
            }
        }
        return loc;
    }

    private void executeTeleport(Player player, Warp warp, Location target) {
        Location safeTarget = getSafeLandingLocation(target);
        player.teleportAsync(safeTarget).thenAccept(success -> {
            if (success) {
                player.sendActionBar(mm.deserialize("<green><bold>✔ BERHASIL TELEPORTASI KE " + warp.getName().toUpperCase() + "!</bold></green>"));
                player.sendMessage(mm.deserialize("<green>✔ Berhasil teleportasi ke <yellow>" + warp.getName() + "</yellow>!</green>"));
                player.playSound(safeTarget, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.1f);
                player.playSound(safeTarget, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.8f, 1.3f);
                safeTarget.getWorld().spawnParticle(Particle.PORTAL, safeTarget.clone().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.1);
                safeTarget.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, safeTarget.clone().add(0, 1, 0), 15, 0.4, 0.6, 0.4, 0.2);
            } else {
                player.sendMessage(mm.deserialize("<red>Gagal melakukan teleportasi ke warp tujuan.</red>"));
            }
        });
    }

    public void cancelPendingTeleport(UUID uuid) {
        BukkitTask task = pendingTeleports.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}
