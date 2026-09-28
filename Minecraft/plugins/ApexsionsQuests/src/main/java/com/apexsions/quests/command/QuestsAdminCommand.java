package com.apexsions.quests.command;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.PlayerStreakData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestsAdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsQuests plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public QuestsAdminCommand(ApexsionsQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.admin.quests")) {
            sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk menggunakan perintah ini.</red>"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(mm.deserialize("<gold><bold>APEXSIONS QUESTS ADMIN</bold></gold>"));
            sender.sendMessage(mm.deserialize(" <yellow>/qadmin reload</yellow> <gray>- Reload konfigurasi</gray>"));
            sender.sendMessage(mm.deserialize(" <yellow>/qadmin setstreak <player> <days></yellow> <gray>- Atur streak login</gray>"));
            sender.sendMessage(mm.deserialize(" <yellow>/qadmin givefreeze <player> <amount></yellow> <gray>- Beri streak freeze token</gray>"));
            sender.sendMessage(mm.deserialize(" <yellow>/qadmin resetdaily <player></yellow> <gray>- Reset klaim hari ini</gray>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getCalendarService().loadConfig();
                plugin.getStreakService().loadConfig();
                plugin.getQuestManager().loadQuestsFromConfig();
                sender.sendMessage(mm.deserialize(plugin.getMsg("admin.reload")));
            }
            case "setstreak" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /qadmin setstreak <player> <days></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain tidak ditemukan atau sedang offline.</red>"));
                    return true;
                }
                try {
                    int days = Integer.parseInt(args[2]);
                    PlayerStreakData data = plugin.getStreakService().getStreakData(target.getUniqueId());
                    if (data != null) {
                        data.setCurrentStreak(days);
                        plugin.getRepository().saveStreakData(data);
                        sender.sendMessage(mm.deserialize(plugin.getMsg("admin.setstreak")
                                .replace("%player%", target.getName())
                                .replace("%days%", String.valueOf(days))));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(mm.deserialize("<red>Jumlah hari harus berupa angka valid.</red>"));
                }
            }
            case "givefreeze" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /qadmin givefreeze <player> <amount></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain tidak ditemukan atau sedang offline.</red>"));
                    return true;
                }
                try {
                    int amt = Integer.parseInt(args[2]);
                    PlayerStreakData data = plugin.getStreakService().getStreakData(target.getUniqueId());
                    if (data != null) {
                        data.addStreakFreeze(amt);
                        plugin.getRepository().saveStreakData(data);
                        sender.sendMessage(mm.deserialize(plugin.getMsg("admin.givefreeze")
                                .replace("%player%", target.getName())
                                .replace("%amount%", String.valueOf(amt))));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(mm.deserialize("<red>Jumlah harus berupa angka.</red>"));
                }
            }
            case "resetdaily" -> {
                if (args.length < 2) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /qadmin resetdaily <player></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain tidak ditemukan.</red>"));
                    return true;
                }
                PlayerStreakData data = plugin.getStreakService().getStreakData(target.getUniqueId());
                if (data != null) {
                    int today = LocalDate.now().getDayOfMonth();
                    // Clear the today bit
                    long mask = data.getMonthlyClaimedMask() & ~(1L << (today - 1));
                    data.setMonthlyClaimedMask(mask);
                    data.setLastClaimDate("");
                    plugin.getRepository().saveStreakData(data);
                    sender.sendMessage(mm.deserialize(plugin.getMsg("admin.resetdaily").replace("%player%", target.getName())));
                }
            }
            default -> sender.sendMessage(mm.deserialize("<red>Subcommand tidak dikenal. Ketik /qadmin untuk bantuan.</red>"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("reload", "setstreak", "givefreeze", "resetdaily"), args[0]);
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return filter(names, args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> list, String token) {
        List<String> out = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(token.toLowerCase())) out.add(s);
        }
        return out;
    }
}
