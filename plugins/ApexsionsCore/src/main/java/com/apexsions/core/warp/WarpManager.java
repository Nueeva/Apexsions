package com.apexsions.core.warp;

import com.apexsions.core.ApexsionsCorePlugin;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
        player.sendMessage(mm.deserialize("<yellow>⏳ Teleportasi ke <gold>" + warp.getName() + "</gold> dalam <white>" + delay + "</white> detik. Jangan bergerak!</yellow>"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int remaining = delay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelPendingTeleport(player.getUniqueId());
                    return;
                }

                // Check movement
                if (player.getLocation().distanceSquared(startLoc) > 0.1) {
                    player.sendMessage(mm.deserialize("<red>✖ Teleportasi dibatalkan karena kamu bergerak!</red>"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    cancelPendingTeleport(player.getUniqueId());
                    return;
                }

                remaining--;
                if (remaining > 0) {
                    player.sendActionBar(mm.deserialize("<yellow>Teleportasi dalam <gold>" + remaining + "s</gold>...</yellow>"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.5f);
                } else {
                    cancelPendingTeleport(player.getUniqueId());
                    executeTeleport(player, warp, target);
                }
            }
        }, 20L, 20L);

        pendingTeleports.put(player.getUniqueId(), task);
    }

    private void executeTeleport(Player player, Warp warp, Location target) {
        player.teleportAsync(target).thenAccept(success -> {
            if (success) {
                player.sendMessage(mm.deserialize("<green>✔ Berhasil teleportasi ke <yellow>" + warp.getName() + "</yellow>!</green>"));
                player.playSound(target, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                target.getWorld().spawnParticle(Particle.PORTAL, target.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            } else {
                player.sendMessage(mm.deserialize("<red>Gagal melakukan teleportasi.</red>"));
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
