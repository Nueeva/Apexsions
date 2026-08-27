package com.apex.economy.command;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class AdminEconomyCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsEconomy plugin;

    public AdminEconomyCommand(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("apexsionseconomy.admin") && !sender.hasPermission("apexpassionseconomy.admin")) {
            sender.sendMessage("Â§cAnda tidak memiliki izin untuk menggunakan perintah ini.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Â§cPenggunaan: /ecoadmin <give|take|set> <player> <amount> [rupiah|diamond]");
            return true;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.getUniqueId() == null) {
            sender.sendMessage("Â§cPemain " + targetName + " tidak valid.");
            return true;
        }

        double amount;
        try {
            amount = NumberFormatUtil.parse(args[2]);
        } catch (Exception e) {
            sender.sendMessage("Â§cJumlah tidak valid!");
            return true;
        }

        String currId = (args.length >= 4) ? args[3].toLowerCase() : "rupiah";
        Currency currency = plugin.getCurrencyRegistry().get(currId);
        if (currency == null) {
            sender.sendMessage("Â§cMata uang " + currId + " tidak dikenali!");
            return true;
        }

        switch (action) {
            case "give", "add" -> {
                plugin.getCurrencyService().addBalance(target.getUniqueId(), currency.getId(), amount);
                sender.sendMessage("Â§aBerhasil memberikan Â§e" + NumberFormatUtil.format(amount, currency) + " Â§akepada Â§e" + targetName);
            }
            case "take", "remove" -> {
                plugin.getCurrencyService().removeBalance(target.getUniqueId(), currency.getId(), amount);
                sender.sendMessage("Â§cBerhasil mengurangi Â§e" + NumberFormatUtil.format(amount, currency) + " Â§cdari Â§e" + targetName);
            }
            case "set" -> {
                plugin.getCurrencyService().setBalance(target.getUniqueId(), currency.getId(), amount);
                sender.sendMessage("Â§aBerhasil menyetel saldo " + currency.getDisplayName() + " Â§e" + targetName + " Â§amenjadi Â§e" + NumberFormatUtil.format(amount, currency));
            }
            default -> sender.sendMessage("Â§cAksi tidak valid! Pilihan: give, take, set.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("give", "take", "set");
        }
        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            for (var p : Bukkit.getOnlinePlayers()) list.add(p.getName());
            return list;
        }
        if (args.length == 3) {
            return List.of("1000", "10k", "100k", "1jt", "10jt", "1m");
        }
        if (args.length == 4) {
            return List.of("rupiah", "diamond");
        }
        return List.of();
    }
}
