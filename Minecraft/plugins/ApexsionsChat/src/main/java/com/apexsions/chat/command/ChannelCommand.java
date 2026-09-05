package com.apexsions.chat.command;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.channel.ChatChannel;
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

public class ChannelCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChannelCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only in-game players can switch or speak in chat channels.</red>"));
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        // Direct channel speak commands: /g, /global, /kingdomchat, /staffchat
        if (cmdName.equals("global") || label.equalsIgnoreCase("g")) {
            return handleDirectChannel(player, "global", args);
        }
        if (cmdName.equals("kingdomchat") || label.equalsIgnoreCase("kchat") || label.equalsIgnoreCase("kc")) {
            return handleDirectChannel(player, "kingdom", args);
        }
        if (cmdName.equals("staffchat") || label.equalsIgnoreCase("sc")) {
            return handleDirectChannel(player, "staff", args);
        }

        // Generic /channel <channelName>
        if (args.length == 0 || args[0].equalsIgnoreCase("settings") || args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
            player.openInventory(new com.apexsions.chat.gui.ChatSettingsGUI(plugin, player).getInventory());
            return true;
        }

        if (args[0].equalsIgnoreCase("profile") && args.length > 1) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                player.openInventory(new com.apexsions.chat.gui.SocialProfileGUI(plugin, player, target).getInventory());
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Pemain target tidak ditemukan atau sedang offline.</red>"));
            }
            return true;
        }

        String targetChannelId = args[0].toLowerCase();
        boolean success = plugin.getChannelManager().setPlayerChannel(player, targetChannelId);
        if (success) {
            ChatChannel channel = plugin.getChannelManager().getPlayerChannel(player);
            player.sendMessage(miniMessage.deserialize("<green>✔ Switched active channel to <yellow>" + channel.getName() + "</yellow>.</green>"));
        } else {
            player.sendMessage(miniMessage.deserialize("<red>✖ Could not switch to channel '" + args[0] + "'. Check name or realm assignment.</red>"));
        }

        return true;
    }

    private boolean handleDirectChannel(Player player, String channelId, String[] args) {
        if (args.length == 0) {
            boolean success = plugin.getChannelManager().setPlayerChannel(player, channelId);
            if (success) {
                ChatChannel channel = plugin.getChannelManager().getPlayerChannel(player);
                player.sendMessage(miniMessage.deserialize("<green>✔ Switched active channel to <yellow>" + channel.getName() + "</yellow>.</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<red>✖ Could not switch to " + channelId + " channel. Check permissions or realm.</red>"));
            }
            return true;
        }

        // Send direct message in channel
        plugin.getChannelManager().getChannel(channelId).ifPresent(channel -> {
            if (!channel.canSpeak(player)) {
                player.sendMessage(miniMessage.deserialize("<red>You cannot speak in the " + channel.getName() + " channel.</red>"));
                return;
            }
            String message = String.join(" ", args);
            player.chat(message);
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (ChatChannel ch : plugin.getChannelManager().getChannels()) {
                if (sender instanceof Player p && ch.canSpeak(p)) {
                    if (ch.getId().toLowerCase().startsWith(args[0].toLowerCase())) {
                        list.add(ch.getId());
                    }
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
