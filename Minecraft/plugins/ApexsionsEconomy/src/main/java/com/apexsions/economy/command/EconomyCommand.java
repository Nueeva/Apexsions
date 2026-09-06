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
        if (args.length > 0) {
            String sub = args[0].toLowerCase();

            // Admin subcommands (accessible by Console or Players with admin permission)
            if (sub.equals("give") || sub.equals("add") || sub.equals("take") || sub.equals("remove") || sub.equals("set") || sub.equals("reload")) {
                if (!sender.hasPermission("apexsionseconomy.admin") && !sender.hasPermission("apexpassionseconomy.admin")) {
                    sender.sendMessage("§cAnda tidak memiliki izin untuk menggunakan perintah admin ekonomi.");
                    return true;
                }

                if (sub.equals("reload")) {
                    plugin.reload();
                    sender.sendMessage("§a[ApexsionsEconomy] Konfigurasi dan mata uang berhasil di-reload!");
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage("§cPenggunaan: /" + label + " " + sub + " <player> <amount> [rupiah|diamond]");
                    return true;
                }

                String targetName = args[1];
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                if (target.getUniqueId() == null) {
                    sender.sendMessage("§cPemain " + targetName + " tidak valid.");
                    return true;
                }

                double amount;
                String currId = "rupiah";

                if (args.length >= 4) {
                    if (plugin.getCurrencyRegistry().get(args[2].toLowerCase()) != null) {
                        currId = args[2].toLowerCase();
                        try {
                            amount = NumberFormatUtil.parse(args[3]);
                        } catch (Exception e) {
                            sender.sendMessage("§cJumlah tidak valid: " + args[3]);
                            return true;
                        }
                    } else {
                        currId = args[3].toLowerCase();
                        try {
                            amount = NumberFormatUtil.parse(args[2]);
                        } catch (Exception e) {
                            sender.sendMessage("§cJumlah tidak valid: " + args[2]);
                            return true;
                        }
                    }
                } else {
                    try {
                        amount = NumberFormatUtil.parse(args[2]);
                    } catch (Exception e) {
                        sender.sendMessage("§cJumlah tidak valid: " + args[2]);
                        return true;
                    }
                }

                Currency currency = plugin.getCurrencyRegistry().get(currId);
                if (currency == null) {
                    sender.sendMessage("§cMata uang " + currId + " tidak dikenali!");
                    return true;
                }

                switch (sub) {
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
                }
                return true;
            }
        }

        // GUI & Player-specific features require an in-game Player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPenggunaan Console: /" + label + " <give|take|set|reload> [player] [amount] [currency]");
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
            List<String> list = new ArrayList<>(List.of("menu", "bal", "info", "top", "deposit", "bank"));
            if (sender.hasPermission("apexsionseconomy.admin") || sender.hasPermission("apexpassionseconomy.admin")) {
                list.addAll(List.of("give", "take", "set", "reload"));
            }
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && List.of("give", "take", "set", "add", "remove").contains(args[0].toLowerCase())) {
            return null; // Bukkit handles online player list
        }
        if (args.length == 4 && List.of("give", "take", "set", "add", "remove").contains(args[0].toLowerCase())) {
            return plugin.getCurrencyRegistry().getAll().stream().map(Currency::getId).filter(id -> id.startsWith(args[3].toLowerCase())).toList();
        }
        return List.of();
    }
}
