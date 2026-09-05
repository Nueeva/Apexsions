package com.apexsions.battlepass.command;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.admin.gui.AdminMainMenu;
import com.apexsions.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ABPCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsBattlepass plugin;

    public ABPCommand(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Backend Security Check: Only players with apexsionsbattlepass.admin or console can use /abp
        if (!sender.hasPermission("apexsionsbattlepass.admin")) {
            sender.sendMessage(plugin.getMessage("admin-no-permission"));
            return true;
        }

        // Running /abp without args or with 'menu' / 'gui' opens Admin GUI Panel directly
        if (args.length == 0 || args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage("player-only"));
                return true;
            }
            new AdminMainMenu(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "season" -> {
                sender.sendMessage("§8=======================================");
                sender.sendMessage("§6§lBATTLEPASS SEASON - §e" + plugin.getSeasonManager().getCurrentSeason().getName());
                sender.sendMessage("§7Status: §e" + plugin.getSeasonManager().getSeasonState());
                sender.sendMessage("§7Periode: §f" + plugin.getSeasonManager().getSeasonDateRangeFormatted());
                sender.sendMessage("§7Sisa Waktu: §a" + plugin.getSeasonManager().getTimeLeftFormatted());
                sender.sendMessage("§7Week Aktif: §fWeek " + plugin.getQuestManager().getPeriodService().getCurrentWeekNumber());
                sender.sendMessage("§7Month Aktif: §fMonth " + plugin.getQuestManager().getPeriodService().getCurrentMonthNumber());
                sender.sendMessage("§8=======================================");
                return true;
            }
            case "reload" -> {
                plugin.reloadAllConfigurations();
                sender.sendMessage(plugin.getMessage("plugin-reloaded"));
                return true;
            }
            case "givepass" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cPenggunaan: /abp givepass <player> <tier>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("player-not-found").replace("%player%", args[1]));
                    return true;
                }
                String passType = args[2].toLowerCase();
                PlayerData data = plugin.getPlayerManager().getPlayerData(target);
                if (data != null) {
                    data.addPass(passType);
                    sender.sendMessage("§aBerhasil memberikan pass §e" + passType.toUpperCase() + " §akepada §e" + target.getName());
                    target.sendMessage("§aSelamat! Anda telah mendapatkan §e" + passType.toUpperCase() + " Pass§a!");
                }
                return true;
            }
            case "setlevel" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cPenggunaan: /abp setlevel <player> <level>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("player-not-found").replace("%player%", args[1]));
                    return true;
                }
                try {
                    int lvl = Integer.parseInt(args[2]);
                    PlayerData data = plugin.getPlayerManager().getPlayerData(target);
                    if (data != null) {
                        data.setLevel(lvl);
                        sender.sendMessage("§aBerhasil mengatur level §e" + target.getName() + " §amenjadi §e" + lvl);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cLevel harus berupa angka.");
                }
                return true;
            }
            case "addxp" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cPenggunaan: /abp addxp <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("player-not-found").replace("%player%", args[1]));
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getXpService().addXp(target, amount);
                    sender.sendMessage("§aBerhasil menambahkan §e" + amount + " XP §akepada §e" + target.getName());
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cJumlah XP harus berupa angka.");
                }
                return true;
            }
            case "currency" -> {
                if (args.length < 4) {
                    sender.sendMessage("§cPenggunaan: /abp currency <add|remove|set> <player> <amount>");
                    return true;
                }
                String action = args[1].toLowerCase();
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("player-not-found").replace("%player%", args[2]));
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[3]);
                    String currName = plugin.getCurrencyService().getCurrencyName();
                    if (action.equalsIgnoreCase("add")) {
                        plugin.getCurrencyService().addCurrency(target.getUniqueId(), amount);
                        sender.sendMessage(plugin.getMessage("currency-added").replace("%amount%", String.valueOf(amount)).replace("%currency%", currName).replace("%player%", target.getName()));
                    } else if (action.equalsIgnoreCase("remove")) {
                        plugin.getCurrencyService().removeCurrency(target.getUniqueId(), amount);
                        sender.sendMessage(plugin.getMessage("currency-removed").replace("%amount%", String.valueOf(amount)).replace("%currency%", currName).replace("%player%", target.getName()));
                    } else if (action.equalsIgnoreCase("set")) {
                        plugin.getCurrencyService().setCurrency(target.getUniqueId(), amount);
                        sender.sendMessage(plugin.getMessage("currency-set").replace("%amount%", String.valueOf(amount)).replace("%currency%", currName).replace("%player%", target.getName()));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cJumlah currency harus berupa angka.");
                }
                return true;
            }
            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cPenggunaan: /abp reset <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("player-not-found").replace("%player%", args[1]));
                    return true;
                }
                PlayerData data = plugin.getPlayerManager().getPlayerData(target);
                if (data != null) {
                    data.resetProgressForNewSeason(plugin.getSeasonManager().getCurrentSeason().getId());
                    plugin.getRepository().savePlayerData(data);
                    sender.sendMessage("§aBerhasil mereset data Battle Pass §e" + target.getName());
                }
                return true;
            }
            case "resetrefresh" -> {
                if (args.length > 1 && !args[1].equalsIgnoreCase("all")) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(plugin.getMessage("player-not-found").replace("%player%", args[1]));
                        return true;
                    }
                    PlayerData data = plugin.getPlayerManager().getPlayerData(target);
                    if (data != null) {
                        plugin.getShopRefreshService().resetPlayerDailyRefreshCount(data);
                        sender.sendMessage(plugin.getMessage("shop-refresh-reset-player").replace("%player%", target.getName()));
                    }
                } else {
                    plugin.getShopRefreshService().resetAllDailyRefreshCounts();
                    sender.sendMessage(plugin.getMessage("shop-refresh-reset-all"));
                }
                return true;
            }
            case "help" -> {
                sender.sendMessage("§8=======================================");
                sender.sendMessage("§4§lAPEXSIONS BATTLEPASS §8- §cAdmin Commands");
                sender.sendMessage("§e/abp §7- Buka Admin BattlePass Panel GUI");
                sender.sendMessage("§e/abp reload §7- Reload seluruh konfigurasi");
                sender.sendMessage("§e/abp season §7- Lihat status season saat ini");
                sender.sendMessage("§e/abp givepass <player> <tier> §7- Berikan pass tier");
                sender.sendMessage("§e/abp setlevel <player> <lvl> §7- Atur level pemain");
                sender.sendMessage("§e/abp addxp <player> <amount> §7- Tambahkan XP ke pemain");
                sender.sendMessage("§e/abp currency <add|remove|set> <player> <amount> §7- Kelola coins pemain");
                sender.sendMessage("§e/abp reset <player> §7- Reset progres pemain");
                sender.sendMessage("§e/abp resetrefresh [all|player] §7- Reset counter refresh harian");
                sender.sendMessage("§8=======================================");
                return true;
            }
        }

        sender.sendMessage(plugin.getMessage("invalid-args"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("apexsionsbattlepass.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("menu");
            completions.add("season");
            completions.add("reload");
            completions.add("givepass");
            completions.add("setlevel");
            completions.add("addxp");
            completions.add("currency");
            completions.add("reset");
            completions.add("resetrefresh");
            completions.add("help");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("currency")) {
                completions.add("add");
                completions.add("remove");
                completions.add("set");
            } else if (args[0].equalsIgnoreCase("resetrefresh")) {
                completions.add("all");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            } else if (List.of("givepass", "setlevel", "addxp", "reset").contains(args[0].toLowerCase())) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("givepass")) {
                completions.addAll(plugin.getPassManager().getPasses().keySet());
            } else if (args[0].equalsIgnoreCase("currency")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}

