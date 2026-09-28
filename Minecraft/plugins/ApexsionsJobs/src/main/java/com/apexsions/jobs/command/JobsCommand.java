package com.apexsions.jobs.command;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.gui.dialog.JobsDialog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JobsCommand implements CommandExecutor {

    private final ApexsionsJobs plugin;

    public JobsCommand(ApexsionsJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Perintah ini hanya dapat dijalankan oleh pemain in-game.");
            return true;
        }

        JobsDialog.open(plugin, player);
        return true;
    }
}
