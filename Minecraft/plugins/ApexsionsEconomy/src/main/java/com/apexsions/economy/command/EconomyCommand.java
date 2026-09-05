package com.apexsions.economy.command;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.currency.Currency;
import com.apexsions.economy.gui.EconomyMainMenu;
import com.apexsions.economy.util.NumberFormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EconomyCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsEconomy plugin;

    public EconomyCommand(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPerintah ini hanya dapat dijalankan oleh player!");
            return true;
        }

        if (args.length == 0) {
            new EconomyMainMenu(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("bal") || sub.equals("balance") || sub.equals("info")) {
            player.sendMessage("§8§m----------------------------------------");
            player.sendMessage("§a§lDOMPET & INFORMASI SALDO ANDA:");
            for (Currency c : plugin.getCurrencyRegistry().getAll()) {
                double b = plugin.getCurrencyService().getBalance(player.getUniqueId(), c.getId());
                player.sendMessage(" §8- §f" + c.getDisplayName() + ": §e§l" + NumberFormatUtil.format(b, c) + " §7(" + NumberFormatUtil.formatFull(b, c) + ")");
            }
            player.sendMessage("§8§m----------------------------------------");
            return true;
        }

        if (sub.equals("top") || sub.equals("leaderboard") || sub.equals("baltop") || sub.equals("balancetop")) {
            new com.apexsions.economy.gui.EconomyLeaderboardMenu(plugin, player, "rupiah", null).open();
            return true;
        }

        if (sub.equals("deposit") || sub.equals("bank") || sub.equals("deposito")) {
            new com.apexsions.economy.gui.BankDepositMenu(plugin, player, null).open();
            return true;
        }

        new EconomyMainMenu(plugin, player).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("menu", "bal", "info", "top", "deposit", "bank");
        }
        return List.of();
    }
}
