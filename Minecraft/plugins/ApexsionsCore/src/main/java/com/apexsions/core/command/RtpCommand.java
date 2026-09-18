package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Command executor for /rtp, /wild, /wilderness, /krtp (Random Kingdom Teleport).
 */
public class RtpCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RtpCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat dijalankan oleh pemain.</red>"));
            return true;
        }

        if (args.length > 0 && ((plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player)) || player.hasPermission("apexsionscore.admin"))) {
            plugin.getKingdomRtpService().executeRtpTargeted(player, args[0]);
        } else {
            plugin.getKingdomRtpService().executeRtp(player);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player p && ((plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(p)) || p.hasPermission("apexsionscore.admin"))) {
            List<String> list = java.util.Arrays.asList("zenithar", "solterra", "sylvamoor", "wild");
            List<String> result = new java.util.ArrayList<>();
            for (String s : list) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
