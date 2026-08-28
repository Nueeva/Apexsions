package com.yourserver.apexsionscore.command;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.player.PlayerData;
import com.yourserver.apexsionscore.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handles /kingdom (aliases: /k, /kingdoms), /kingdom choose, /kingdom info, /kingdom rewards, and /kingdom xp.
 */
public class KingdomCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public KingdomCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>This command can only be executed by players.</red>"));
            return true;
        }

        String cmdLabel = label.toLowerCase();
        if (cmdLabel.equals("rewards") || cmdLabel.equals("reward") || cmdLabel.equals("claim")) {
            handleKingdomRewards(player);
            return true;
        }
        if (cmdLabel.equals("exp") || cmdLabel.equals("xp") || cmdLabel.equals("xpguide") || cmdLabel.equals("guide")) {
            handleKingdomXpGuide(player);
            return true;
        }
        if (cmdLabel.equals("level") || cmdLabel.equals("lvl") || cmdLabel.equals("profile") || cmdLabel.equals("stats")) {
            handleKingdomInfo(player);
            return true;
        }

        if (args.length == 0) {
            // Default: /kingdom -> Teleport to own kingdom
            handleKingdomTeleport(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "choose":
            case "select":
                handleKingdomChoose(player);
                break;

            case "info":
            case "profile":
            case "stats":
            case "gui":
            case "menu":
            case "level":
            case "lvl":
                handleKingdomInfo(player);
                break;

            case "rewards":
            case "reward":
            case "claim":
                handleKingdomRewards(player);
                break;

            case "xp":
            case "guide":
            case "xpguide":
            case "exp":
                handleKingdomXpGuide(player);
                break;

            case "rtp":
            case "wild":
            case "wilderness":
            case "krtp":
                plugin.getKingdomRtpService().executeRtp(player);
                break;

            default:
                player.sendMessage(miniMessage.deserialize("<gold><bold>Apexsions Kingdom Commands:</bold></gold>"));
                player.sendMessage(miniMessage.deserialize("<yellow>/kingdom</yellow> <gray>- Teleport to your kingdom spawn</gray>"));
                player.sendMessage(miniMessage.deserialize("<yellow>/kingdom rtp</yellow> <gray>- Random teleport strictly inside your kingdom</gray>"));
                player.sendMessage(miniMessage.deserialize("<yellow>/kingdom choose</yellow> <gray>- Open kingdom selection GUI</gray>"));
                player.sendMessage(miniMessage.deserialize("<yellow>/kingdom info</yellow> <gray>- Open your interactive Kingdom Profile & Level GUI</gray>"));
                player.sendMessage(miniMessage.deserialize("<yellow>/kingdom rewards</yellow> <gray>- View & claim Level 1–100 progression rewards</gray>"));
                player.sendMessage(miniMessage.deserialize("<yellow>/kingdom xp</yellow> <gray>- Open 13 XP gameplay sources and guide GUI</gray>"));
                player.sendMessage(miniMessage.deserialize("<yellow>/level</yellow> <gray>- Quick shortcut to your character profile & level progress</gray>"));
                break;
        }

        return true;
    }

    private void handleKingdomTeleport(Player player) {
        if (!player.hasPermission("kingdomcore.command.kingdom")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to teleport to your kingdom.</red>"));
            return;
        }

        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty() || !dataOpt.get().hasRegion()) {
            player.sendMessage(miniMessage.deserialize("<yellow>You have not pledged allegiance to a kingdom yet! Opening selection menu...</yellow>"));
            plugin.getRegionSelectionGUI().open(player);
            return;
        }

        UUID regionId = dataOpt.get().getRegionId();
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(regionId);
        if (regionOpt.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Your assigned kingdom could not be found.</red>"));
            return;
        }

        plugin.getRegionTeleportService().teleport(player, regionOpt.get());
    }

    private void handleKingdomChoose(Player player) {
        if (!player.hasPermission("kingdomcore.command.kingdom.choose")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to choose a kingdom.</red>"));
            return;
        }

        plugin.getRegionSelectionGUI().open(player);
    }

    private void handleKingdomInfo(Player player) {
        if (!player.hasPermission("kingdomcore.command.kingdom.info")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to view kingdom info.</red>"));
            return;
        }

        plugin.getKingdomProfileGUI().open(player);
    }

    private void handleKingdomRewards(Player player) {
        if (!player.hasPermission("kingdomcore.command.kingdom.info")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to view rewards.</red>"));
            return;
        }

        plugin.getLevelRewardsGUI().open(player, 1);
    }

    private void handleKingdomXpGuide(Player player) {
        plugin.getXpGuideGUI().open(player);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = Arrays.asList("choose", "info", "profile", "rewards", "claim", "xp", "guide", "level", "rtp", "wild", "wilderness");
            String prefix = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String s : list) {
                if (s.startsWith(prefix)) {
                    matches.add(s);
                }
            }
            return matches;
        }
        return Collections.emptyList();
    }
}
