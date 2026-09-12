package com.apexsions.core.vanish;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * High-performance Vanish System for ApexsionsCore.
 * Features:
 * - Completely hides vanished players from non-staff (world and TAB list)
 * - Keeps player 100% solid/visible to themselves (zero translucent potion effects)
 * - Disables collisions & hitboxes (setCollidable false)
 * - Fake leave/join broadcasts with server rank formatting
 * - Silent toggle flag (-s)
 * - Actionbar persistence reminder
 * - Persistent vanish state across reboots/reconnects
 */
public class VanishManager {

    private final ApexsionsCorePlugin plugin;
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final File dataFile;
    private BukkitTask reminderTask;

    public VanishManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "vanished-players.yml");
        loadData();
        startReminderTask();
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public boolean isVanished(Player player) {
        return player != null && isVanished(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    public int getVanishedCount() {
        return (int) vanishedPlayers.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .count();
    }

    /**
     * Toggles vanish mode for a player.
     */
    public boolean toggleVanish(Player player, boolean silent) {
        boolean newState = !isVanished(player);
        setVanished(player, newState, silent);
        return newState;
    }

    /**
     * Sets vanish state for a player.
     */
    public void setVanished(Player player, boolean vanish, boolean silent) {
        if (player == null || !player.isOnline()) return;
        UUID uuid = player.getUniqueId();

        if (vanish) {
            vanishedPlayers.add(uuid);
            player.setCollidable(false);
            player.setSleepingIgnored(true);
            player.setAllowFlight(true);
            player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            player.setMetadata("vanish", new FixedMetadataValue(plugin, true));
            notifyTabVanish(player, true);

            // Hide from normal players, ensure visible to staff
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer.getUniqueId().equals(uuid)) {
                    continue; // Self visibility: character remains 100% visible to self!
                }
                if (viewer.hasPermission("apexsions.vanish.see")) {
                    viewer.showPlayer(plugin, player);
                } else {
                    viewer.hidePlayer(plugin, player);
                }
            }

            // Fake quit broadcast
            if (!silent) {
                broadcastFakeQuit(player);
            }

            // Notify staff
            Component staffNotif = mm.deserialize(
                    "<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Vanish</bold></gradient>]</dark_gray> <yellow>"
                            + player.getName() + "</yellow> <gray>sekarang <red><bold>VANISHED</bold></red> (Tak terlihat oleh pemain biasa).</gray>"
            );
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("apexsions.vanish.see") && !staff.getUniqueId().equals(uuid)) {
                    staff.sendMessage(staffNotif);
                }
            }

            // Player confirmation
            player.sendMessage(mm.deserialize(
                    "<gradient:#f1c40f:#e67e22><bold>👻 MODE VANISH DIAKTIFKAN</bold></gradient>\n" +
                            "<gray>• Status: <red><bold>Tak terlihat</bold></red> oleh pemain biasa.\n" +
                            "• Karaktermu tetap terlihat normal di layarmu sendiri.\n" +
                            "• Hitbox & tabrakan fisik: <yellow>Dinonaktifkan</yellow>.\n" +
                            "• Nama di daftar TAB: <red>Disembunyikan</red>.\n" +
                            (silent ? "• Mode: <italic><yellow>Senyap (-s, tanpa fake quit broadcast)</yellow></italic>" : "• Pesan fake leave telah disiarkan ke server.") + "</gray>"
            ));
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.8f, 1.4f);

        } else {
            vanishedPlayers.remove(uuid);
            player.setCollidable(true);
            player.setSleepingIgnored(false);
            player.removeMetadata("vanished", plugin);
            player.removeMetadata("vanish", plugin);
            notifyTabVanish(player, false);

            // Re-show to all players
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.showPlayer(plugin, player);
            }

            // Fake join broadcast
            if (!silent) {
                broadcastFakeJoin(player);
            }

            // Notify staff
            Component staffNotif = mm.deserialize(
                    "<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Vanish</bold></gradient>]</dark_gray> <yellow>"
                            + player.getName() + "</yellow> <gray>sekarang <green><bold>TERLIHAT KEMBALI</bold></green> (Unvanished).</gray>"
            );
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("apexsions.vanish.see") && !staff.getUniqueId().equals(uuid)) {
                    staff.sendMessage(staffNotif);
                }
            }

            // Player confirmation
            player.sendMessage(mm.deserialize(
                    "<gradient:#f1c40f:#e67e22><bold>👁️ MODE VANISH DINONAKTIFKAN</bold></gradient>\n" +
                            "<gray>• Status: <green><bold>Terlihat</bold></green> oleh seluruh pemain.\n" +
                            "• Hitbox & tabrakan fisik: <green>Diaktifkan kembali</green>.\n" +
                            "• Nama di daftar TAB: <green>Ditampilkan kembali</green>.\n" +
                            (silent ? "• Mode: <italic><yellow>Senyap (-s, tanpa fake join broadcast)</yellow></italic>" : "• Pesan fake join telah disiarkan ke server.") + "</gray>"
            ));
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 0.8f, 1.4f);
        }

        saveData();
    }

    /**
     * Broadcasts a fake quit message identical to server format.
     */
    public void broadcastFakeQuit(Player player) {
        String rank = resolveRankDisplay(player);
        String msg = "<dark_gray>[</dark_gray><red><bold>-</bold></red><dark_gray>]</dark_gray> "
                + rank + " <white><bold>" + player.getName() + "</bold></white> <gray>meninggalkan server</gray>";
        Bukkit.broadcast(mm.deserialize(msg));
    }

    /**
     * Broadcasts a fake join message identical to server format.
     */
    public void broadcastFakeJoin(Player player) {
        String rank = resolveRankDisplay(player);
        String msg = "<dark_gray>[</dark_gray><green><bold>+</bold></green><dark_gray>]</dark_gray> "
                + rank + " <white><bold>" + player.getName() + "</bold></white> <gray>bergabung ke server</gray>";
        Bukkit.broadcast(mm.deserialize(msg));
    }

    private String resolveRankDisplay(Player player) {
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
            try {
                return plugin.getLuckPermsHook().getPlayerRank(player);
            } catch (Throwable ignored) {}
        }
        return "<gray>[Wanderer]</gray>";
    }

    /**
     * Handle player join: enforce vanish visibility and hide vanished players from newcomers.
     */
    public void handlePlayerJoin(Player joiner) {
        UUID uuid = joiner.getUniqueId();
        boolean isVanishedJoin = isVanished(uuid);

        if (isVanishedJoin) {
            joiner.setCollidable(false);
            joiner.setSleepingIgnored(true);
            joiner.setAllowFlight(true);
            joiner.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            joiner.setMetadata("vanish", new FixedMetadataValue(plugin, true));
            notifyTabVanish(joiner, true);

            // Hide the vanished joiner from everyone except staff
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (viewer.getUniqueId().equals(uuid)) continue;
                if (!viewer.hasPermission("apexsions.vanish.see")) {
                    viewer.hidePlayer(plugin, joiner);
                } else {
                    viewer.showPlayer(plugin, joiner);
                }
            }

            // Remind the staff member
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (joiner.isOnline()) {
                    joiner.sendMessage(mm.deserialize(
                            "<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Apexsions</bold></gradient>]</dark_gray> " +
                                    "<yellow>Kamu login dalam status <red><bold>VANISH</bold></red>. Tidak ada pesan join yang disiarkan ke pemain lain.</yellow>"
                    ));
                    joiner.playSound(joiner.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.7f, 1.2f);
                }
            }, 10L);
        }

        // Hide other vanished players from this new player if they don't have see permission
        boolean canSee = joiner.hasPermission("apexsions.vanish.see");
        for (UUID vUuid : vanishedPlayers) {
            if (vUuid.equals(uuid)) continue;
            Player vPlayer = Bukkit.getPlayer(vUuid);
            if (vPlayer != null && vPlayer.isOnline()) {
                if (!canSee) {
                    joiner.hidePlayer(plugin, vPlayer);
                } else {
                    joiner.showPlayer(plugin, vPlayer);
                }
            }
        }
    }

    public void handleWorldChange(Player player) {
        if (!isVanished(player)) return;
        UUID uuid = player.getUniqueId();

        // Re-enforce hidePlayer across world transfers
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.getUniqueId().equals(uuid)) continue;
            if (!viewer.hasPermission("apexsions.vanish.see")) {
                viewer.hidePlayer(plugin, player);
            } else {
                viewer.showPlayer(plugin, player);
            }
        }
        player.setCollidable(false);
    }

    private void startReminderTask() {
        this.reminderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (vanishedPlayers.isEmpty()) return;
            Component bar = mm.deserialize(
                    "<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Apexsions</bold></gradient>]</dark_gray> " +
                            "<red><bold>👻 ANDA SEDANG VANISH</bold></red> <gray>(Tak terlihat oleh pemain biasa)</gray>"
            );
            for (UUID uuid : vanishedPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendActionBar(bar);
                }
            }
        }, 40L, 40L);
    }

    public void shutdown() {
        if (reminderTask != null && !reminderTask.isCancelled()) {
            reminderTask.cancel();
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removeMetadata("vanished", plugin);
            p.removeMetadata("vanish", plugin);
        }
        saveData();
    }

    private void loadData() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        List<String> list = config.getStringList("vanished-uuids");
        for (String s : list) {
            try {
                vanishedPlayers.add(UUID.fromString(s));
            } catch (Exception ignored) {}
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isVanished(p.getUniqueId())) {
                p.setMetadata("vanished", new FixedMetadataValue(plugin, true));
                p.setMetadata("vanish", new FixedMetadataValue(plugin, true));
                notifyTabVanish(p, true);
            }
        }
    }

    private void notifyTabVanish(Player player, boolean vanished) {
        try {
            Class<?> tabApiClass = Class.forName("me.neznamy.tab.api.TabAPI");
            Object tabApi = tabApiClass.getMethod("getInstance").invoke(null);
            if (tabApi != null) {
                Object tabPlayer = tabApiClass.getMethod("getPlayer", UUID.class).invoke(tabApi, player.getUniqueId());
                if (tabPlayer != null) {
                    tabPlayer.getClass().getMethod("setVanished", boolean.class).invoke(tabPlayer, vanished);
                }
            }
        } catch (Throwable ignored) {
            // TAB plugin not loaded or differing version; Bukkit metadata handles vanish automatically
        }
    }

    private void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("vanished-uuids", vanishedPlayers.stream().map(UUID::toString).collect(Collectors.toList()));
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Gagal menyimpan data vanished players: " + e.getMessage());
        }
    }
}
