package com.apexsions.core.caravan;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
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
 * Player and admin control for the Wandering Black Market Caravan.
 */
public class CaravanCommand implements CommandExecutor, TabCompleter {

    private final CaravanManager manager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CaravanCommand(@NotNull CaravanManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain dalam game yang dapat menggunakan perintah kafilah.");
            return true;
        }
        if (!player.hasPermission("apexsions.caravan.use") && !player.hasPermission("apexsions.caravan.admin")) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk mengakses kafilah.</red>"));
            return true;
        }

        if (args.length == 0) {
            if (!manager.isActive()) {
                player.sendMessage(mm.deserialize("<gray>Kafilah pasar gelap sedang tidak aktif. Hari aktif: <yellow>"
                        + manager.getScheduleDescription() + "</yellow>.</gray>"));
                return true;
            }
            new CaravanGUI(manager, player).open(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "status" -> sendStatus(player);
            case "open", "shop" -> {
                if (!manager.isActive()) {
                    player.sendMessage(mm.deserialize("<red>Kafilah sedang tidak aktif.</red>"));
                    return true;
                }
                new CaravanGUI(manager, player).open(player);
            }
            case "admin" -> handleAdmin(player, args);
            default -> sendStatus(player);
        }
        return true;
    }

    private void sendStatus(Player player) {
        player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════</dark_gray>"));
        player.sendMessage(mm.deserialize("<gradient:#9b59b6:#e74c3c><bold>KAFILAH PASAR GELAP</bold></gradient>"));
        if (manager.isActive()) {
            Location loc = manager.getActiveLocation();
            player.sendMessage(mm.deserialize("<gray>Status: <green>AKTIF</green></gray>"));
            if (loc != null) {
                player.sendMessage(mm.deserialize("<gray>Lokasi: <gold>X: " + (int) loc.getX() + ", Y: " + (int) loc.getY()
                        + ", Z: " + (int) loc.getZ() + "</gold> <dark_gray>|</dark_gray> <yellow>" + loc.getWorld().getName() + "</yellow></gray>"));
            }
            player.sendMessage(mm.deserialize("<gray>Sisa waktu: <aqua>" + manager.describeWindow() + "</aqua></gray>"));
        } else {
            player.sendMessage(mm.deserialize("<gray>Status: <red>TIDAK AKTIF</red></gray>"));
            player.sendMessage(mm.deserialize("<gray>Hari aktif: <yellow>" + manager.getScheduleDescription() + "</yellow></gray>"));
        }
        player.sendMessage(mm.deserialize("<gray>Jumlah penawaran: <white>" + manager.getOffers().size() + "</white></gray>"));
        player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════</dark_gray>"));
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("apexsions.caravan.admin") && !player.hasPermission("apexsions.admin") && !player.isOp()) {
            player.sendMessage(mm.deserialize("<red>Fitur admin kafilah hanya untuk Staff.</red>"));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<gray>Gunakan: <yellow>/caravan admin spawn|reroll|tp|despawn|reload</yellow></gray>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "spawn" -> {
                if (manager.spawn(player.getLocation())) {
                    player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>KAFILAH DIPANGGIL!</bold></gradient> <gray>Pedagang muncul di lokasi Anda.</gray>"));
                } else {
                    player.sendMessage(mm.deserialize("<red>Gagal memunculkan kafilah.</red>"));
                }
            }
            case "reroll", "respawn" -> {
                player.sendMessage(mm.deserialize("<yellow>Mencari daratan liar yang aman untuk kafilah...</yellow>"));
                manager.spawnRandomWilderness(player);
            }
            case "tp", "teleport" -> {
                if (!manager.isActive() || manager.getActiveLocation() == null) {
                    player.sendMessage(mm.deserialize("<red>Kafilah sedang tidak aktif atau belum memiliki lokasi.</red>"));
                    return;
                }
                player.teleportAsync(manager.getActiveLocation().clone().add(0, 0.5, 0)).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(mm.deserialize("<green>Teleportasi ke kafilah pasar gelap berhasil.</green>"));
                    } else {
                        player.sendMessage(mm.deserialize("<red>Gagal melakukan teleportasi ke kafilah.</red>"));
                    }
                });
            }
            case "despawn" -> {
                manager.despawn();
                player.sendMessage(mm.deserialize("<yellow>Kafilah telah dihilangkan.</yellow>"));
            }
            case "reload" -> {
                manager.reload();
                player.sendMessage(mm.deserialize("<green>Konfigurasi kafilah dimuat ulang.</green>"));
            }
            default -> player.sendMessage(mm.deserialize("<gray>Gunakan: <yellow>/caravan admin spawn|reroll|tp|despawn|reload</yellow></gray>"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        List<String> completions = new ArrayList<>();
        boolean isAdmin = player.hasPermission("apexsions.caravan.admin") || player.hasPermission("apexsions.admin") || player.isOp();

        if (args.length == 1) {
            completions.add("status");
            completions.add("open");
            if (isAdmin) {
                completions.add("admin");
            }
            return filterPrefix(completions, args[0]);
        }
        if (args.length == 2 && isAdmin && args[0].equalsIgnoreCase("admin")) {
            completions.add("spawn");
            completions.add("reroll");
            completions.add("tp");
            completions.add("despawn");
            completions.add("reload");
            return filterPrefix(completions, args[1]);
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