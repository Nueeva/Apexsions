package com.apexsions.economy.command;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.util.NumberFormatUtil;
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
            sender.sendMessage("§cAnda tidak memiliki izin untuk menggunakan perintah ini.");
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage("§a[ApexsionsEconomy] Konfigurasi dan pengaturan mata uang berhasil di-reload!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cPenggunaan: /ecoadmin <give|take|set|reload> [player] [amount] [rupiah|diamond]");
            return true;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.getUniqueId() == null) {
            sender.sendMessage("§cPemain " + targetName + " tidak valid.");
            return true;
        }

        double amount;
        try {
            amount = NumberFormatUtil.parse(args[2]);
        } catch (Exception e) {
            sender.sendMessage("§cJumlah tidak valid!");
            return true;
        }

        String currId = (args.length >= 4) ? args[3].toLowerCase() : "rupiah";
        Currency currency = plugin.getCurrencyRegistry().get(currId);
        if (currency == null) {
            sender.sendMessage("§cMata uang " + currId + " tidak dikenali!");
            return true;
        }

        switch (action) {
            case "give", "add" -> {
                plugin.getCurrencyService().addBalance(target.getUniqueId(), currency.getId(), amount);
                sender.sendMessage("§aBerhasil memberikan §e" + NumberFormatUtil.format(amount, currency) + " §akepada §e" + targetName);
            }
            case "take", "remove" -> {
                plugin.getCurrencyService().removeBalance(target.getUniqueId(), currency.getId(), amount);
                sender.sendMessage("§cBerhasil mengurangi §e" + NumberFormatUtil.format(amount, currency) + " §cdari §e" + targetName);
            }
            case "set" -> {
                plugin.getCurrencyService().setBalance(target.getUniqueId(), currency.getId(), amount);
                sender.sendMessage("§aBerhasil menyetel saldo " + currency.getDisplayName() + " §e" + targetName + " §amenjadi §e" + NumberFormatUtil.format(amount, currency));
            }
            default -> sender.sendMessage("§cAksi tidak valid! Pilihan: give, take, set, reload.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("give", "take", "set", "reload");
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            List<String> list = new ArrayList<>();
            for (var p : Bukkit.getOnlinePlayers()) list.add(p.getName());
            return list;
        }
        if (args.length == 3 && !args[0].equalsIgnoreCase("reload")) {
            return List.of("1000", "10k", "100k", "1jt", "10jt", "1m");
        }
        if (args.length == 4 && !args[0].equalsIgnoreCase("reload")) {
            return List.of("rupiah", "diamond");
        }
        return List.of();
    }
}
