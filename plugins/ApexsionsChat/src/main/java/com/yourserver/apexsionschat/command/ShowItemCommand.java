package com.yourserver.apexsionschat.command;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.channel.ChatChannel;
import com.yourserver.apexsionschat.gui.ItemShowcaseGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ShowItemCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsChatPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ShowItemCommand(ApexsionsChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Only in-game players can showcase or view items.</red>"));
            return true;
        }

        // 1. Inspect existing showcase snapshot by ID: /showitem <id>
        if (args.length > 0) {
            String showcaseId = args[0];
            Optional<ItemStack> itemOpt = plugin.getItemShowcaseService().getShowcaseItem(showcaseId);
            if (itemOpt.isPresent()) {
                new ItemShowcaseGUI(plugin, itemOpt.get()).open(player);
            } else {
                player.sendMessage(miniMessage.deserialize("<red>✖ Showcase item has expired or no longer exists.</red>"));
            }
            return true;
        }

        // 2. Showcase currently held main hand item
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.AIR) {
            player.sendMessage(miniMessage.deserialize("<red>✖ You must be holding an item in your main hand to showcase it!</red>"));
            return true;
        }

        ChatChannel channel = plugin.getChannelManager().getPlayerChannel(player);
        if (!channel.canSpeak(player)) {
            player.sendMessage(miniMessage.deserialize("<red>✖ You do not have permission to speak in the " + channel.getName() + " channel.</red>"));
            return true;
        }

        // Format showcase message directly through channel pipeline
        Component formattedShowcase = plugin.getChatFormatter().format(player, channel, "[item]");

        // Broadcast to authorized channel recipients
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (channel.canReceive(recipient, player)) {
                recipient.sendMessage(formattedShowcase);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
