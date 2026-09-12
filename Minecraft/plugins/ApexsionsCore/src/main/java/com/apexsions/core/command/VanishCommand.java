package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.vanish.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command executor and tab-completer for /vanish and /v.
 */
public class VanishCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final VanishManager vanishManager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public VanishCommand(ApexsionsCorePlugin plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.vanish")) {
            sender.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk menggunakan perintah vanish.</red>"));
            return true;
        }

        // Subcommand: /vanish list
        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            showVanishedList(sender);
            return true;
        }

        // Case 1: No args -> toggle self
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize("<red>Konsol harus menentukan pemain: /vanish <player> [-s]</red>"));
                return true;
            }
            vanishManager.toggleVanish(player, false);
            return true;
        }

        // Case 2: 1 arg: either "-s" for self, or target player
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("-s") || args[0].equalsIgnoreCase("silent")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Hanya pemain yang dapat toggle vanish diri sendiri.</red>"));
                    return true;
                }
                vanishManager.toggleVanish(player, true);
                return true;
            }

            // Target player
            if (!sender.hasPermission("apexsions.vanish.others")) {
                sender.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk mengubah status vanish pemain lain.</red>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(mm.deserialize("<red>Pemain target '" + args[0] + "' tidak ditemukan atau sedang offline.</red>"));
                return true;
            }

            boolean nowVanished = vanishManager.toggleVanish(target, false);
            sender.sendMessage(mm.deserialize(
                    "<green>✓ Berhasil mengubah mode vanish untuk <yellow>" + target.getName() + "</yellow> menjadi: "
                            + (nowVanished ? "<red><bold>VANISHED</bold></red>" : "<green><bold>VISIBLE</bold></green>") + ".</green>"
            ));
            return true;
        }

        // Case 3: 2 args: /vanish <target> -s
        if (args.length >= 2) {
            if (!sender.hasPermission("apexsions.vanish.others")) {
                sender.sendMessage(mm.deserialize("<red>Kamu tidak memiliki izin untuk mengubah status vanish pemain lain.</red>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(mm.deserialize("<red>Pemain target '" + args[0] + "' tidak ditemukan atau sedang offline.</red>"));
                return true;
            }

            boolean silent = args[1].equalsIgnoreCase("-s") || args[1].equalsIgnoreCase("silent");
            boolean nowVanished = vanishManager.toggleVanish(target, silent);
            sender.sendMessage(mm.deserialize(
                    "<green>✓ Berhasil mengubah mode vanish (Silent: " + silent + ") untuk <yellow>" + target.getName() + "</yellow> menjadi: "
                            + (nowVanished ? "<red><bold>VANISHED</bold></red>" : "<green><bold>VISIBLE</bold></green>") + ".</green>"
            ));
            return true;
        }

        return true;
    }

    private void showVanishedList(CommandSender sender) {
        Set<UUID> vanished = vanishManager.getVanishedPlayers();
        List<Player> onlineVanished = vanished.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .toList();

        if (onlineVanished.isEmpty()) {
            sender.sendMessage(mm.deserialize("<dark_gray>[<gradient:#f1c40f:#e67e22><bold>Vanish</bold></gradient>]</dark_gray> <gray>Tidak ada staf yang sedang dalam mode vanish saat ini.</gray>"));
            return;
        }

        sender.sendMessage(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👻 DAFTAR STAF DALAM MODE VANISH (" + onlineVanished.size() + "):</bold></gradient>"));
        for (Player p : onlineVanished) {
            sender.sendMessage(mm.deserialize("<dark_gray>•</dark_gray> <yellow>" + p.getName() + "</yellow> <gray>(" + p.getWorld().getName() + " " + p.getLocation().getBlockX() + ", " + p.getLocation().getBlockY() + ", " + p.getLocation().getBlockZ() + ")</gray>"));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.vanish")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("-s");
            completions.add("list");
            if (sender.hasPermission("apexsions.vanish.others")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && sender.hasPermission("apexsions.vanish.others")) {
            if ("-s".startsWith(args[1].toLowerCase())) {
                return List.of("-s");
            }
        }

        return Collections.emptyList();
    }
}
