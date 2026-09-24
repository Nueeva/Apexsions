package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.DeathCoordinateManager;
import com.apexsions.core.player.DeathRecord;
import com.apexsions.core.util.PlayerResolver;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command handling for /deathcoords, /lastdeath, /kor, /cor.
 */
public class DeathCoordsCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final DeathCoordinateManager manager;
    private final MiniMessage mm;

    public DeathCoordsCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getDeathCoordinateManager();
        this.mm = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain dalam game yang dapat menggunakan perintah koordinat kematian.");
            return true;
        }

        // 1. /deathcoords (no arguments) -> View own death coords
        if (args.length == 0) {
            manager.sendSelfDeathNotification(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        // 2. /deathcoords compass [player]
        if (sub.equals("compass")) {
            if (args.length == 1) {
                manager.pointCompassToDeath(player, null);
                return true;
            }

            // Staff setting compass to someone else's death
            if (!hasStaffPermission(player)) {
                player.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk mengarahkan kompas ke koordinat pemain lain.</red>"));
                return true;
            }

            OfflinePlayer target = PlayerResolver.resolveOffline(args[1]);
            if (target == null || target.getUniqueId() == null) {
                player.sendMessage(mm.deserialize("<red>❌ Pemain <yellow>" + args[1] + "</yellow> tidak ditemukan.</red>"));
                return true;
            }
            DeathRecord record = manager.getLatestDeathRecord(target.getUniqueId());
            if (record == null) {
                player.sendMessage(mm.deserialize("<red>❌ Tidak ada data kematian yang terekam untuk pemain <yellow>" + args[1] + "</yellow>.</red>"));
                return true;
            }

            manager.pointCompassToDeath(player, record);
            return true;
        }

        // 3. /deathcoords tp <player> (Admin / Staff Teleport)
        if (sub.equals("tp")) {
            if (!hasStaffPermission(player)) {
                player.sendMessage(mm.deserialize("<red>❌ Fitur teleportasi titik kematian hanya dapat diakses oleh Staff & Admin.</red>"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(mm.deserialize("<red>Gunakan: <yellow>/deathcoords tp <pemain></yellow></red>"));
                return true;
            }

            OfflinePlayer target = PlayerResolver.resolveOffline(args[1]);
            if (target == null || target.getUniqueId() == null) {
                player.sendMessage(mm.deserialize("<red>❌ Pemain <yellow>" + args[1] + "</yellow> tidak ditemukan.</red>"));
                return true;
            }
            DeathRecord record = manager.getLatestDeathRecord(target.getUniqueId());
            if (record == null) {
                player.sendMessage(mm.deserialize("<red>❌ Tidak ada data kematian yang terekam untuk pemain <yellow>" + args[1] + "</yellow>.</red>"));
                return true;
            }

            Location loc = record.toLocation();
            if (loc == null || loc.getWorld() == null) {
                player.sendMessage(mm.deserialize("<red>❌ Dunia tempat kematian (" + record.worldName() + ") tidak dapat dimuat.</red>"));
                return true;
            }

            player.teleportAsync(loc).thenAccept(success -> {
                if (success) {
                    player.sendMessage(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>🚀 TELEPORT ADMIN!</bold></gradient> <gray>Tiba di lokasi kematian <yellow>" + (target.getName() != null ? target.getName() : args[1]) + "</yellow> (<gold>X: " + (int) record.x() + ", Y: " + (int) record.y() + ", Z: " + (int) record.z() + "</gold>).</gray>"));
                } else {
                    player.sendMessage(mm.deserialize("<red>❌ Gagal melakukan teleportasi ke lokasi tujuan.</red>"));
                }
            });
            return true;
        }

        // 4. /deathcoords <player> -> Staff inspecting another player's death
        if (!hasStaffPermission(player)) {
            player.sendMessage(mm.deserialize("<red>❌ Anda hanya dapat memeriksa koordinat kematian diri sendiri.</red>"));
            return true;
        }

        OfflinePlayer target = PlayerResolver.resolveOffline(args[0]);
        if (target == null || target.getUniqueId() == null) {
            player.sendMessage(mm.deserialize("<red>❌ Pemain <yellow>" + args[0] + "</yellow> tidak ditemukan.</red>"));
            return true;
        }
        manager.sendAdminDeathNotification(player, target);
        return true;
    }

    private boolean hasStaffPermission(Player player) {
        return player.hasPermission("apexsions.admin")
                || player.hasPermission("apexsions.staff")
                || player.hasPermission("apexsions.core.deathcoords.others")
                || player.isOp();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        boolean isStaff = hasStaffPermission(player);

        if (args.length == 1) {
            completions.add("compass");
            if (isStaff) {
                completions.add("tp");
                completions.addAll(PlayerResolver.completePlayerNames(player, args[0]));
            }
            return filterPrefix(completions, args[0]);
        }

        if (args.length == 2 && isStaff && (args[0].equalsIgnoreCase("compass") || args[0].equalsIgnoreCase("tp"))) {
            return PlayerResolver.completePlayerNames(player, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                filtered.add(s);
            }
        }
        return filtered;
    }
}
