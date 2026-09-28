package com.apexsions.quests.command;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.gui.DailyCalendarGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DailyCommand implements CommandExecutor {

    private final ApexsionsQuests plugin;

    public DailyCommand(ApexsionsQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Perintah ini hanya dapat dijalankan oleh pemain in-game.");
            return true;
        }

        new DailyCalendarGUI(plugin, player).open();
        return true;
    }
}
