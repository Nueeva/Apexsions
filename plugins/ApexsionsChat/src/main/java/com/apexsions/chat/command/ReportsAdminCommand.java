package com.apexsions.chat.command;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.gui.ReportListGUI;
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

public class ReportsAdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ReportsAdminCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only in-game players can open the reports GUI.</red>"));
            return true;
        }

        if (!player.hasPermission("apexsionschat.staff.reports")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to view staff reports.</red>"));
            return true;
        }

        new ReportListGUI(plugin, 1).loadAndOpen(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
