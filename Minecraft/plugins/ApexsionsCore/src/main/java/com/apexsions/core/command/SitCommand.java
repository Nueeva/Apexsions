package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.pose.PoseManager;
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
import java.util.Collections;
import java.util.List;

public class SitCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final PoseManager poseManager;
    private final MiniMessage mm;

    public SitCommand(ApexsionsCorePlugin plugin, PoseManager poseManager) {
        this.plugin = plugin;
        this.poseManager = poseManager;
        this.mm = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Admin force sit another player
        if (args.length >= 1 && sender.hasPermission("apexsions.pose.admin")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(mm.deserialize("<red>❌ Pemain <yellow>" + args[0] + "</yellow> tidak ditemukan atau sedang offline.</red>"));
                return true;
            }
            if (poseManager.isSitting(target)) {
                poseManager.standUp(target, true);
                sender.sendMessage(mm.deserialize("<green>✔ <yellow>" + target.getName() + "</yellow> telah diberdirikan.</green>"));
            } else {
                poseManager.sit(target, null, false);
                sender.sendMessage(mm.deserialize("<green>✔ <yellow>" + target.getName() + "</yellow> telah didudukkan.</green>"));
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>❌ Perintah ini hanya dapat dijalankan oleh pemain.</red>"));
            return true;
        }

        if (!player.hasPermission("apexsions.pose.sit")) {
            player.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk menggunakan perintah ini.</red>"));
            return true;
        }

        if (poseManager.isSitting(player)) {
            poseManager.standUp(player, true);
        } else {
            poseManager.sit(player, null, false);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("apexsions.pose.admin")) {
            List<String> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
