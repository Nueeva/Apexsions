package com.yourserver.apexsionschat.command;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.model.Report;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReportCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Long> reportCooldowns = new ConcurrentHashMap<>();

    public ReportCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only in-game players can submit reports.</red>"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /report <player> <reason></red>"));
            return true;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(miniMessage.deserialize("<red>✖ You cannot report yourself!</red>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        UUID targetUuid;
        String finalTargetName;

        if (target != null) {
            targetUuid = target.getUniqueId();
            finalTargetName = target.getName();
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
            if (offline.hasPlayedBefore() || offline.isOnline()) {
                targetUuid = offline.getUniqueId();
                finalTargetName = offline.getName() != null ? offline.getName() : targetName;
            } else {
                player.sendMessage(miniMessage.deserialize("<red>✖ Player '" + targetName + "' could not be found.</red>"));
                return true;
            }
        }

        // Rate limit check
        int cdSec = plugin.getConfigManager().getReportsConfig().getInt("reports.cooldown-seconds", 60);
        long last = reportCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if ((System.currentTimeMillis() - last) < (cdSec * 1000L)) {
            long remaining = (cdSec * 1000L - (System.currentTimeMillis() - last)) / 1000;
            player.sendMessage(miniMessage.deserialize("<red>✖ Please wait " + remaining + "s before submitting another report.</red>"));
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        Report report = new Report(
                player.getUniqueId(),
                player.getName(),
                targetUuid,
                finalTargetName,
                reason,
                player.getWorld().getName()
        );

        plugin.getReportRepository().createReportAsync(report).thenAccept(reportId -> {
            if (reportId > 0) {
                reportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                player.sendMessage(miniMessage.deserialize("<green>✔ Your report against <yellow>" + finalTargetName + "</yellow> (Report #" + reportId + ") has been submitted to online staff.</green>"));

                // Notify online staff
                notifyStaff(report);
            } else {
                player.sendMessage(miniMessage.deserialize("<red>✖ An error occurred while saving your report. Please contact an admin.</red>"));
            }
        });

        return true;
    }

    private void notifyStaff(Report report) {
        String staffPerm = plugin.getConfigManager().getReportsConfig().getString("reports.staff-permission", "apexsionschat.staff.reports");
        Component staffAlert = miniMessage.deserialize(
                "<gold>🛡️ <red><bold>[NEW REPORT #" + report.getReportId() + "]</bold></red> <yellow>" +
                report.getReporterName() + "</yellow> reported <red>" + report.getReportedName() + "</red>: <white>" +
                report.getReason() + "</white> <yellow><underlined>[Click to View]</underlined></yellow></gold>"
        ).clickEvent(ClickEvent.runCommand("/reports")).hoverEvent(HoverEvent.showText(miniMessage.deserialize("<yellow>Click to open Reports GUI</yellow>")));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(staffPerm)) {
                p.sendMessage(staffAlert);
                if (plugin.getConfigManager().getMainConfig().getBoolean("sounds.report-alert.enabled", true)) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                }
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName()) && p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        if (args.length == 2) {
            List<String> reasons = plugin.getConfigManager().getReportsConfig().getStringList("reports.standard-reasons");
            List<String> list = new ArrayList<>();
            for (String r : reasons) {
                if (r.toLowerCase().startsWith(args[1].toLowerCase())) {
                    list.add(r);
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
