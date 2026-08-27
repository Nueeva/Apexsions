package com.apex.economy.command;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.gui.PayMenu;
import com.apex.economy.util.NumberFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PayCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsEconomy plugin;

    public PayCommand(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Â§cPerintah ini hanya dapat digunakan oleh player!");
            return true;
        }

        if (args.length == 0) {
            new PayMenu(plugin, player, null).open();
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("Â§cPenggunaan: /pay <player> <amount> [rupiah|diamond]");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("Â§cPemain " + targetName + " tidak ditemukan atau sedang offline!");
            return true;
        }

        double amount;
        try {
            amount = NumberFormatUtil.parse(args[1]);
        } catch (Exception e) {
            player.sendMessage("Â§cJumlah tidak valid! Gunakan angka biasa atau singkatan seperti 10k, 1.5jt, 2m.");
            return true;
        }

        String currId = (args.length >= 3) ? args[2].toLowerCase() : "rupiah";
        Currency currency = plugin.getCurrencyRegistry().get(currId);
        if (currency == null) {
            player.sendMessage("Â§cMata uang " + currId + " tidak dikenali! Pilihan: rupiah, diamond.");
            return true;
        }

        plugin.getPayService().transfer(player, target.getUniqueId(), target.getName(), currency, amount);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName())) list.add(p.getName());
            }
            return list;
        }
        if (args.length == 2) {
            return List.of("1000", "5000", "10k", "50k", "100k", "1jt");
        }
        if (args.length == 3) {
            return List.of("rupiah", "diamond");
        }
        return List.of();
    }
}
