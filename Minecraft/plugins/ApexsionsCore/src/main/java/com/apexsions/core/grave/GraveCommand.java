package com.apexsions.core.grave;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
 * Command handling for /grave, /tombstone, /nisan.
 */
public class GraveCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final GraveManager manager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GraveCommand(@NotNull ApexsionsCorePlugin plugin, @NotNull GraveManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain dalam game yang dapat menggunakan perintah nisan.");
            return true;
        }

        if (!player.hasPermission("apexsions.grave.use") && !player.hasPermission("apexsions.grave.admin")) {
            player.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk menggunakan sistem nisan.</red>"));
            return true;
        }

        if (!manager.isEnabled()) {
            player.sendMessage(mm.deserialize("<red>❌ Sistem nisan saat ini dinonaktifkan.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("open")) {
            GraveRecord nearest = manager.findNearest(player, manager.getInteractDistance());
            if (nearest == null) {
                GraveRecord own = manager.getActiveGrave(player.getUniqueId());
                if (own != null) {
                    player.sendMessage(mm.deserialize("<yellow>⚠ Anda tidak berada di dekat nisan Anda. Gunakan <gold>/grave list</gold> untuk melihat lokasinya.</yellow>"));
                } else {
                    player.sendMessage(mm.deserialize("<gray>Tidak ada nisan aktif milik Anda.</gray>"));
                }
                return true;
            }
            manager.collect(player, nearest, true);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("list")) {
            sendList(player, player);
            return true;
        }

        if (sub.equals("compass")) {
            manager.pointCompassToGrave(player);
            return true;
        }

        if (sub.equals("admin")) {
            handleAdmin(player, args);
            return true;
        }

        if (!hasAdmin(player)) {
            player.sendMessage(mm.deserialize("<red>❌ Anda hanya dapat melihat nisan diri sendiri.</red>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        sendList(player, target);
        return true;
    }

    private void handleAdmin(Player player, String[] args) {
        if (!hasAdmin(player)) {
            player.sendMessage(mm.deserialize("<red>❌ Fitur admin nisan hanya untuk Staff & Admin.</red>"));
            return;
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            if (manager.forceRelease(target.getUniqueId())) {
                player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>✔ NISAN DILEPAS!</bold></gradient> <gray>Nisan milik <yellow>" + args[2] + "</yellow> telah dibuka paksa.</gray>"));
            } else {
                player.sendMessage(mm.deserialize("<red>❌ Tidak ada nisan aktif untuk <yellow>" + args[2] + "</yellow>.</red>"));
            }
            return;
        }
        if (args.length >= 2) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            sendList(player, target);
            return;
        }
        player.sendMessage(mm.deserialize("<gray>Gunakan: <yellow>/grave admin remove <pemain></yellow></gray>"));
    }

    private void sendList(Player viewer, OfflinePlayer target) {
        List<GraveRecord> graves = manager.getGraves(target.getUniqueId());
        String name = target.getName() != null ? target.getName() : target.getUniqueId().toString();

        Component header = mm.deserialize("<dark_gray>══════════════════════════════════════════════</dark_gray>");
        viewer.sendMessage(Component.empty());
        viewer.sendMessage(header);
        viewer.sendMessage(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>☠ DAFTAR NISAN: " + name + "</bold></gradient>"));

        if (graves.isEmpty()) {
            viewer.sendMessage(mm.deserialize("<gray>Tidak ada nisan aktif.</gray>"));
            viewer.sendMessage(header);
            return;
        }

        for (GraveRecord grave : graves) {
            viewer.sendMessage(mm.deserialize("<gray>Dunia:</gray> <yellow>" + grave.getWorldName() + "</yellow> <dark_gray>|</dark_gray> <gold>X: "
                    + (int) grave.getX() + ", Y: " + (int) grave.getY() + ", Z: " + (int) grave.getZ() + "</gold>"));
            viewer.sendMessage(mm.deserialize("<gray>Sisa waktu:</gray> <aqua>" + grave.remainingFormatted() + "</aqua>"));

            Component btnTake = mm.deserialize("<green><bold>[AMBIL]</bold></green>")
                    .clickEvent(ClickEvent.runCommand("/grave open"))
                    .hoverEvent(HoverEvent.showText(mm.deserialize("<green>Klik untuk membuka nisan terdekat (harus berada di dekatnya)</green>")));
            Component btnCompass = mm.deserialize("<gold><bold>[KOMPAS]</bold></gold>")
                    .clickEvent(ClickEvent.runCommand("/grave compass"))
                    .hoverEvent(HoverEvent.showText(mm.deserialize("<yellow>Arahkan kompas ke nisan</yellow>")));

            viewer.sendMessage(Component.text(" ").append(btnTake).append(Component.text("   ")).append(btnCompass));
        }
        viewer.sendMessage(header);
        viewer.sendMessage(Component.empty());
    }

    private boolean hasAdmin(Player player) {
        return player.hasPermission("apexsions.grave.admin")
                || player.hasPermission("apexsions.admin")
                || player.isOp();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        boolean isAdmin = hasAdmin(player);

        if (args.length == 1) {
            completions.add("open");
            completions.add("list");
            completions.add("compass");
            if (isAdmin) {
                completions.add("admin");
                for (Player online : Bukkit.getOnlinePlayers()) {
                    completions.add(online.getName());
                }
            }
            return filterPrefix(completions, args[0]);
        }

        if (args.length == 2 && isAdmin && args[0].equalsIgnoreCase("admin")) {
            completions.add("remove");
            for (Player online : Bukkit.getOnlinePlayers()) {
                completions.add(online.getName());
            }
            return filterPrefix(completions, args[1]);
        }

        if (args.length == 3 && isAdmin && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("remove")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                completions.add(online.getName());
            }
            return filterPrefix(completions, args[2]);
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