package com.yourserver.apexsionscore.command;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the /lobby command.
 */
public class LobbyCommand implements CommandExecutor {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LobbyCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>This command can only be executed by players or NPCs dispatching as player.</red>"));
            return true;
        }

        if (!player.hasPermission("kingdomcore.command.lobby")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to use /lobby.</red>"));
            return true;
        }

        Location lobbyLoc = plugin.getConfigManager().getLobbyLocation();
        if (lobbyLoc == null || lobbyLoc.getWorld() == null) {
            player.sendMessage(miniMessage.deserialize("<red>Lobby spawn location is not set or lobby world is not loaded.</red>"));
            return true;
        }

        player.sendMessage(miniMessage.deserialize("<gold>Teleporting to the lobby...</gold>"));
        player.teleportAsync(lobbyLoc).thenAccept(success -> {
            if (success) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(miniMessage.deserialize("<green>Welcome to the lobby!</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Failed to teleport to lobby.</red>"));
            }
        });

        return true;
    }
}
