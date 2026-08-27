package com.apex.economy.command;

import com.apex.economy.ApexsionsEconomy;
import com.apex.economy.currency.Currency;
import com.apex.economy.gui.EconomyMainMenu;
import com.apex.economy.util.NumberFormatUtil;
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
            sender.sendMessage("Â§cPerintah ini hanya dapat dijalankan oleh player!");
            return true;
        }

        if (args.length == 0) {
            new EconomyMainMenu(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("bal") || sub.equals("balance") || sub.equals("info")) {
            player.sendMessage("Â§8Â§m----------------------------------------");
            player.sendMessage("Â§aÂ§lDOMPET & INFORMASI SALDO ANDA:");
            for (Currency c : plugin.getCurrencyRegistry().getAll()) {
                double b = plugin.getCurrencyService().getBalance(player.getUniqueId(), c.getId());
                player.sendMessage(" Â§8- Â§f" + c.getDisplayName() + ": Â§eÂ§l" + NumberFormatUtil.format(b, c) + " Â§7(" + NumberFormatUtil.formatFull(b, c) + ")");
            }
            player.sendMessage("Â§8Â§m----------------------------------------");
            return true;
        }

        new EconomyMainMenu(plugin, player).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("menu", "bal", "info");
        }
        return List.of();
    }
}
