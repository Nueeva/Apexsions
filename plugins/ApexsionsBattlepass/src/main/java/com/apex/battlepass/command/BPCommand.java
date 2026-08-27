package com.apex.battlepass.command;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.gui.main.MainMenu;
import com.apex.battlepass.player.PlayerData;
import com.apex.battlepass.quest.gui.QuestMainMenu;
import com.apex.battlepass.reward.gui.RewardsMenu;
import com.apex.battlepass.shop.gui.DailyShopMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BPCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsBattlepass plugin;

    public BPCommand(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("apexsionsbattlepass.use") && !player.hasPermission("apexsionsbattlepass.open")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("menu")) {
            new MainMenu(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "quests", "quest" -> {
                new QuestMainMenu(plugin, player, null).open();
                return true;
            }
            case "rewards", "reward" -> {
                new RewardsMenu(plugin, player, null).open();
                return true;
            }
            case "shop", "store" -> {
                new DailyShopMenu(plugin, player, null).open();
                return true;
            }
            case "info", "stats" -> {
                PlayerData data = plugin.getPlayerManager().getPlayerData(player);
                if (data != null) {
                    int reqXp = plugin.getRewardManager().getRequiredXp(data.getLevel());
                    player.sendMessage("§8=======================================");
                    player.sendMessage("§6§lBATTLE PASS STATISTIK - §e" + player.getName());
                    player.sendMessage("§7Season: §e" + plugin.getSeasonManager().getCurrentSeason().getName());
                    player.sendMessage("§7Level: §e" + data.getLevel() + " §8/ §f" + plugin.getRewardManager().getMaxLevel());
                    player.sendMessage("§7XP: §a" + data.getXp() + " §8/ §f" + reqXp);
                    player.sendMessage("§7Battle Coins: §e" + plugin.getCurrencyService().format(data.getCurrency()));
                    player.sendMessage("§7Pass Aktif: §b" + String.join(", ", data.getPasses()).toUpperCase());
                    player.sendMessage("§7Sisa Waktu Season: §a" + plugin.getSeasonManager().getTimeLeftFormatted());
                    player.sendMessage("§8=======================================");
                }
                return true;
            }
            case "help" -> {
                player.sendMessage("§8=======================================");
                player.sendMessage("§6§lAPEXSIONS BATTLEPASS §8- §eBantuan Player");
                player.sendMessage("§e/bp §7- Buka Menu Utama BattlePass GUI");
                player.sendMessage("§e/bp info §7- Lihat statistik BattlePass Anda");
                player.sendMessage("§e/bp quests §7- Buka Menu Quests Harian & Mingguan");
                player.sendMessage("§e/bp rewards §7- Buka Menu Klaim Hadiah Level");
                player.sendMessage("§e/bp shop §7- Buka BattlePass Shop");
                player.sendMessage("§8=======================================");
                return true;
            }
        }

        player.sendMessage(plugin.getMessage("invalid-args"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("open");
            list.add("info");
            list.add("quests");
            list.add("rewards");
            list.add("shop");
            list.add("help");
            return list;
        }
        return List.of();
    }
}
