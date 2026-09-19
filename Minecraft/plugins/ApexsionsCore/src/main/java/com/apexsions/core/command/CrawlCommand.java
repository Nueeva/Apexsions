package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.pose.PlayerPoseType;
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

public class CrawlCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final PoseManager poseManager;
    private final MiniMessage mm;

    public CrawlCommand(ApexsionsCorePlugin plugin, PoseManager poseManager) {
        this.plugin = plugin;
        this.poseManager = poseManager;
        this.mm = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Admin force crawl another player
        if (args.length >= 1 && sender.hasPermission("apexsions.pose.admin")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(mm.deserialize("<red>❌ Pemain <yellow>" + args[0] + "</yellow> tidak ditemukan atau sedang offline.</red>"));
                return true;
            }
            if (poseManager.getActivePose(target) == PlayerPoseType.CRAWLING) {
                poseManager.standUp(target, false);
                sender.sendMessage(mm.deserialize("<green>✔ <yellow>" + target.getName() + "</yellow> telah diberdirikan dari merangkak.</green>"));
            } else {
                poseManager.crawl(target);
                sender.sendMessage(mm.deserialize("<green>✔ <yellow>" + target.getName() + "</yellow> telah dibuat merangkak.</green>"));
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>❌ Perintah ini hanya dapat dijalankan oleh pemain.</red>"));
            return true;
        }

        if (!player.hasPermission("apexsions.pose.crawl")) {
            player.sendMessage(mm.deserialize("<red>❌ Anda tidak memiliki izin untuk menggunakan perintah ini.</red>"));
            return true;
        }

        poseManager.crawl(player);
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
