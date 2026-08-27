package com.yourserver.apexsionschat.command;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import net.kyori.adventure.text.Component;
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
import java.util.Collections;
import java.util.List;

public class ApexsionsChatAdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ApexsionsChatAdminCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionschat.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to use ApexsionsChat admin commands.</red>"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload":
                plugin.getConfigManager().loadAll();
                plugin.getChannelManager().registerDefaultChannels();
                plugin.getGameManager().startScheduler();
                plugin.getAnnouncementManager().startScheduler();
                sender.sendMessage(miniMessage.deserialize("<green>✔ ApexsionsChat modular configurations and schedulers reloaded successfully!</green>"));
                break;

            case "game":
                if (args.length > 1 && args[1].equalsIgnoreCase("start")) {
                    plugin.getGameManager().startRandomGame();
                    sender.sendMessage(miniMessage.deserialize("<green>✔ Force-started a chat game!</green>"));
                } else {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /apexsionschat game start</red>"));
                }
                break;

            case "announce":
                plugin.getAnnouncementManager().broadcastNext();
                sender.sendMessage(miniMessage.deserialize("<green>✔ Triggered immediate announcement broadcast!</green>"));
                break;

            case "mute":
            case "lock":
                boolean newState = !plugin.getModerationEngine().isGlobalMuted();
                plugin.getModerationEngine().setGlobalMuted(newState);
                if (newState) {
                    Bukkit.broadcast(miniMessage.deserialize("<red><bold>🔒 GLOBAL CHAT HAS BEEN MUTED BY STAFF 🔒</bold></red>"));
                } else {
                    Bukkit.broadcast(miniMessage.deserialize("<green><bold>🔓 GLOBAL CHAT HAS BEEN UNMUTED 🔓</bold></green>"));
                }
                break;

            case "clear":
                Component blank = Component.empty();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission("apexsionschat.admin")) {
                        for (int i = 0; i < 100; i++) {
                            p.sendMessage(blank);
                        }
                    }
                }
                Bukkit.broadcast(miniMessage.deserialize("<yellow>🧹 Chat has been cleared by a staff member.</yellow>"));
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold><bold>═════════ ApexsionsChat Admin ═════════</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/apexsionschat reload</yellow> <gray>- Reload all configuration files</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/apexsionschat game start</yellow> <gray>- Force-start a chat game</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/apexsionschat announce</yellow> <gray>- Broadcast next announcement</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/apexsionschat mute</yellow> <gray>- Toggle server-wide chat mute/lock</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/apexsionschat clear</yellow> <gray>- Clear chat history for all players</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold><bold>═══════════════════════════════════════</bold></gold>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionschat.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return Arrays.asList("reload", "game", "announce", "mute", "clear");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("game")) {
            return Collections.singletonList("start");
        }
        return Collections.emptyList();
    }
}
