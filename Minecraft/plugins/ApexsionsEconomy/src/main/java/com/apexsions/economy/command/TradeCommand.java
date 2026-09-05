package com.apexsions.economy.command;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.trade.gui.TradePlayerSelectMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TradeCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsEconomy plugin;

    public TradeCommand(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPerintah ini hanya dapat dijalankan oleh pemain in-game!");
            return true;
        }

        if (!player.hasPermission("apexsionseconomy.trade")) {
            player.sendMessage("§cAnda tidak memiliki izin untuk menggunakan sistem trade!");
            return true;
        }

        if (args.length == 0) {
            // Open GUI selector
            new TradePlayerSelectMenu(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("accept") || sub.equals("terima") || sub.equals("setuju")) {
            String targetName = args.length > 1 ? args[1] : null;
            plugin.getTradeManager().acceptRequest(player, targetName);
            return true;
        }

        if (sub.equals("deny") || sub.equals("tolak") || sub.equals("reject")) {
            String targetName = args.length > 1 ? args[1] : null;
            plugin.getTradeManager().denyRequest(player, targetName);
            return true;
        }

        if (sub.equals("toggle")) {
            plugin.getTradeManager().toggleTrade(player);
            return true;
        }

        if (sub.equals("on") || sub.equals("enable") || sub.equals("aktif")) {
            plugin.getTradeManager().setTradeEnabled(player.getUniqueId(), true);
            player.sendMessage("§a[✔] Fitur trade Anda sekarang §eAKTIF§a. Pemain lain dapat mengirimkan permintaan trade.");
            return true;
        }

        if (sub.equals("off") || sub.equals("disable") || sub.equals("nonaktif")) {
            plugin.getTradeManager().setTradeEnabled(player.getUniqueId(), false);
            player.sendMessage("§c[✖] Fitur trade Anda sekarang §eNONAKTIF§c. Pemain lain tidak dapat mengirimkan permintaan trade ke Anda.");
            return true;
        }

        if (sub.equals("cancel") || sub.equals("batal")) {
            var session = plugin.getTradeManager().getActiveSession(player);
            if (session != null) {
                session.cancelTrade(player, "Dibatalkan via perintah /trade cancel");
            } else {
                player.sendMessage("§cAnda tidak sedang berada dalam sesi trade.");
            }
            return true;
        }


        // Direct request to player: /trade <playerName>
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPemain §e" + targetName + " §ctidak ditemukan atau sedang offline!");
            return true;
        }

        plugin.getTradeManager().sendRequest(player, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String query = args[0].toLowerCase();
            List<String> subs = List.of("accept", "deny", "toggle", "on", "off", "cancel");
            for (String sub : subs) {
                if (sub.startsWith(query)) completions.add(sub);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (sender instanceof Player self && p.getUniqueId().equals(self.getUniqueId())) continue;
                if (p.getName().toLowerCase().startsWith(query)) completions.add(p.getName());
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny"))) {
            String query = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (sender instanceof Player self && p.getUniqueId().equals(self.getUniqueId())) continue;
                if (p.getName().toLowerCase().startsWith(query)) completions.add(p.getName());
            }
        }
        return completions;
    }
}

