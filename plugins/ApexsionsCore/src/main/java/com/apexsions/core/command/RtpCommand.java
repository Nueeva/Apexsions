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

        plugin.getKingdomRtpService().executeRtp(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
