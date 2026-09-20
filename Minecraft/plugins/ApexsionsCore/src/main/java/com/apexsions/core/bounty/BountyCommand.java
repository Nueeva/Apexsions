package com.apexsions.core.bounty;

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
 * Command handling for /bounty, /bounties, /buronan, /headhunt.
 * Uses chat cards with clickable run_command buttons so Bedrock players can use it.
 */
public class BountyCommand implements CommandExecutor, TabCompleter {

    private final BountyManager manager;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BountyCommand(@NotNull BountyManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain dalam game yang dapat menggunakan perintah bounty.");
            return true;
        }

        if (!player.hasPermission("apexsions.bounty.use") && !player.hasPermission("apexsions.bounty.admin")) {
            player.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk menggunakan sistem bounty.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sendList(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> {
                if (args.length < 3) {
                    player.sendMessage(mm.deserialize("<gray>Gunakan: <yellow>/bounty add <pemain> <jumlah></yellow></gray>"));
                    return true;
                }
                Double amount = parseAmount(args[2]);
                if (amount == null || amount <= 0) {
                    player.sendMessage(mm.deserialize("<red>Jumlah bounty tidak valid.</red>"));
                    return true;
                }
                OfflinePlayer target = resolve(args[1]);
                if (target == null) {
                    player.sendMessage(mm.deserialize("<red>Pemain <yellow>" + args[1] + "</yellow> tidak ditemukan.</red>"));
                    return true;
                }
                manager.placeBounty(player, target, amount);
            }
            case "check" -> {
                if (args.length < 2) {
                    player.sendMessage(mm.deserialize("<gray>Gunakan: <yellow>/bounty check <pemain></yellow></gray>"));
                    return true;
                }
                OfflinePlayer target = resolve(args[1]);
                if (target == null) {
                    player.sendMessage(mm.deserialize("<red>Pemain <yellow>" + args[1] + "</yellow> tidak ditemukan.</red>"));
                    return true;
                }
                sendCheck(player, target);
            }
            case "top" -> sendList(player);
            case "hunters" -> manager.sendHunterLeaderboard(player);
            case "admin" -> handleAdmin(player, args);
            default -> sendList(player);
        }
        return true;
    }

    private void handleAdmin(Player player, String[] args) {
        if (!hasAdmin(player)) {
            player.sendMessage(mm.deserialize("<red>Fitur admin bounty hanya untuk Staff & Admin.</red>"));
            return;
        }
        if (args.length >= 3 && args[1].equalsIgnoreCase("clear")) {
            OfflinePlayer target = resolve(args[2]);
            if (target == null) {
                player.sendMessage(mm.deserialize("<red>Pemain <yellow>" + args[2] + "</yellow> tidak ditemukan.</red>"));
                return;
            }
            if (manager.clearBounty(target.getUniqueId())) {
                player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>BOUNTY DIBERSIHKAN!</bold></gradient> <gray>Seluruh kontribusi untuk <yellow>"
                        + args[2] + "</yellow> telah dikembalikan.</gray>"));
            } else {
                player.sendMessage(mm.deserialize("<red>Tidak ada bounty aktif untuk <yellow>" + args[2] + "</yellow>.</red>"));
            }
            return;
        }
        if (args.length >= 4 && args[1].equalsIgnoreCase("set")) {
            OfflinePlayer target = resolve(args[2]);
            Double amount = parseAmount(args[3]);
            if (target == null) {
                player.sendMessage(mm.deserialize("<red>Pemain <yellow>" + args[2] + "</yellow> tidak ditemukan.</red>"));
                return;
            }
            if (amount == null || amount <= 0) {
                player.sendMessage(mm.deserialize("<red>Jumlah bounty tidak valid.</red>"));
                return;
            }
            manager.setAdminBounty(target, amount);
            player.sendMessage(mm.deserialize("<gradient:#2ecc71:#27ae60><bold>BOUNTY ADMIN DIPASANG!</bold></gradient> <gray>Total untuk <yellow>"
                    + args[2] + "</yellow> ditambah <gold>" + manager.format(amount) + "</gold>.</gray>"));
            return;
        }
        player.sendMessage(mm.deserialize("<gray>Gunakan: <yellow>/bounty admin clear <pemain></yellow> atau <yellow>/bounty admin set <pemain> <jumlah></yellow></gray>"));
    }

    private void sendList(Player player) {
        List<TargetBounty> list = manager.getTopTargets(manager.getListLimit());
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════════</dark_gray>"));
        player.sendMessage(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>☠ PAPAN BURONAN AKTIF</bold></gradient>"));
        if (list.isEmpty()) {
            player.sendMessage(mm.deserialize("<gray>Belum ada bounty aktif. Pasang dengan <yellow>/bounty add <pemain> <jumlah></yellow>.</gray>"));
        } else {
            int rank = 1;
            for (TargetBounty tb : list) {
                Component line = mm.deserialize("<yellow>#" + rank + "</yellow> <white>" + tb.getTargetName()
                        + "</white> <dark_gray>|</dark_gray> <gold>" + manager.format(tb.total())
                        + "</gold> <gray>(" + tb.size() + " pemburu)</gray>");
                line = line.hoverEvent(HoverEvent.showText(mm.deserialize("<yellow>Klik untuk lihat rincian bounty "
                        + tb.getTargetName() + "</yellow>")))
                        .clickEvent(ClickEvent.runCommand("/bounty check " + tb.getTargetName()));
                player.sendMessage(line);
                rank++;
            }
        }
        player.sendMessage(mm.deserialize("<gray>Gunakan <yellow>/bounty add <pemain> <jumlah></yellow> untuk memasang hadiah kepala.</gray>"));
        player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════════</dark_gray>"));
        player.sendMessage(Component.empty());
    }

    private void sendCheck(Player player, OfflinePlayer target) {
        TargetBounty tb = manager.getBounty(target.getUniqueId());
        String name = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════════</dark_gray>"));
        player.sendMessage(mm.deserialize("<gradient:#e74c3c:#c0392b><bold>☠ BOUNTY: " + name + "</bold></gradient>"));
        if (tb == null || tb.total() <= 0) {
            player.sendMessage(mm.deserialize("<gray>Tidak ada bounty aktif untuk pemain ini.</gray>"));
        } else {
            player.sendMessage(mm.deserialize("<gray>Total hadiah:</gray> <gold><bold>" + manager.format(tb.total()) + "</bold></gold>"));
            for (TargetBounty.Contributor c : tb.contributors()) {
                player.sendMessage(mm.deserialize("<gray>  • </gray><white>" + c.name() + "</white> <dark_gray>-</dark_gray> <gold>" + manager.format(c.amount()) + "</gold>"));
            }
        }
        player.sendMessage(mm.deserialize("<dark_gray>══════════════════════════════════</dark_gray>"));
        player.sendMessage(Component.empty());
    }

    private boolean hasAdmin(Player player) {
        return player.hasPermission("apexsions.bounty.admin")
                || player.hasPermission("apexsions.admin")
                || player.isOp();
    }

    @Nullable
    private OfflinePlayer resolve(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore() || offline.isOnline()) {
            return offline;
        }
        return null;
    }

    @Nullable
    private Double parseAmount(String raw) {
        try {
            return Double.parseDouble(raw.replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        boolean isAdmin = hasAdmin(player);

        if (args.length == 1) {
            completions.add("list");
            completions.add("add");
            completions.add("check");
            completions.add("top");
            completions.add("hunters");
            if (isAdmin) {
                completions.add("admin");
            }
            return filterPrefix(completions, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("check"))) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                completions.add(online.getName());
            }
            return filterPrefix(completions, args[1]);
        }

        if (args.length == 2 && isAdmin && args[0].equalsIgnoreCase("admin")) {
            completions.add("clear");
            completions.add("set");
            return filterPrefix(completions, args[1]);
        }

        if (args.length == 3 && isAdmin && args[0].equalsIgnoreCase("admin")) {
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
