package com.apexsions.jobs.command;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.PlayerJobData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JobsAdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsJobs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public JobsAdminCommand(ApexsionsJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsions.admin.jobs")) {
            sender.sendMessage(mm.deserialize("<red>Anda tidak memiliki izin untuk menggunakan perintah ini.</red>"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(mm.deserialize("<gold><bold>APEXSIONS JOBS ADMIN</bold></gold>"));
            sender.sendMessage(mm.deserialize(" <yellow>/jadmin reload</yellow> <gray>- Reload konfigurasi</gray>"));
            sender.sendMessage(mm.deserialize(" <yellow>/jadmin setlevel <player> <job> <lvl></yellow> <gray>- Atur level pekerjaan</gray>"));
            sender.sendMessage(mm.deserialize(" <yellow>/jadmin resetdaily <player></yellow> <gray>- Reset batas gaji hari ini</gray>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getJobService().loadConfig();
                sender.sendMessage(mm.deserialize(plugin.getMsg("admin.reload")));
            }
            case "setlevel" -> {
                if (args.length < 4) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /jadmin setlevel <player> <job> <lvl></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain tidak ditemukan.</red>"));
                    return true;
                }
                String jobId = args[2].toLowerCase();
                try {
                    int lvl = Integer.parseInt(args[3]);
                    Map<String, PlayerJobData> map = plugin.getJobService().getPlayerJobs(target.getUniqueId());
                    PlayerJobData data = map.get(jobId);
                    if (data == null) {
                        data = new PlayerJobData(target.getUniqueId(), jobId, lvl, 0.0, 0.0, java.time.LocalDate.now().toString(), false);
                        map.put(jobId, data);
                    } else {
                        data.setLevel(lvl);
                    }
                    plugin.getRepository().savePlayerJob(data);
                    sender.sendMessage(mm.deserialize(plugin.getMsg("admin.setlevel")
                            .replace("%player%", target.getName())
                            .replace("%job%", jobId)
                            .replace("%level%", String.valueOf(lvl))));
                } catch (NumberFormatException e) {
                    sender.sendMessage(mm.deserialize("<red>Level harus berupa angka.</red>"));
                }
            }
            case "resetdaily" -> {
                if (args.length < 2) {
                    sender.sendMessage(mm.deserialize("<red>Gunakan: /jadmin resetdaily <player></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Pemain tidak ditemukan.</red>"));
                    return true;
                }
                Map<String, PlayerJobData> map = plugin.getJobService().getPlayerJobs(target.getUniqueId());
                for (PlayerJobData d : map.values()) {
                    d.setDailyEarnings(0.0);
                    plugin.getRepository().savePlayerJob(d);
                }
                sender.sendMessage(mm.deserialize(plugin.getMsg("admin.resetdaily").replace("%player%", target.getName())));
            }
            default -> sender.sendMessage(mm.deserialize("<red>Subcommand tidak dikenal. Ketik /jadmin untuk bantuan.</red>"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("reload", "setlevel", "resetdaily"), args[0]);
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return filter(names, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setlevel")) {
            return filter(new ArrayList<>(plugin.getJobService().getDefinitions().keySet()), args[2]);
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
