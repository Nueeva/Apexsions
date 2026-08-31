package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
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
 * Handles player region & kingdom interactions (/region, /kingdom, /k, /level).
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
            sender.sendMessage(miniMessage.deserialize("<red>Only players can use kingdom commands.</red>"));
            return true;
        }

        // If command is /level, open Level Rewards / Profile GUI directly
        if (label.equalsIgnoreCase("level") || label.equalsIgnoreCase("lvl") || label.equalsIgnoreCase("profile")) {
            handleKingdomInfo(player);
            return true;
        }

        if (label.equalsIgnoreCase("rewards") || label.equalsIgnoreCase("reward") || label.equalsIgnoreCase("claim")) {
            handleKingdomRewards(player);
            return true;
        }

        if (label.equalsIgnoreCase("xp") || label.equalsIgnoreCase("guide") || label.equalsIgnoreCase("xpguide") || label.equalsIgnoreCase("exp")) {
            handleKingdomXpGuide(player);
            return true;
        }

        if (args.length == 0) {
            handleKingdomTeleport(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "choose":
            case "select":
            case "join":
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

            case "top":
            case "leaderboard":
            case "ranking":
                plugin.getKingdomTopGUI().open(player);
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

            case "setking":
                handleSetKing(sender, args);
                break;

            default:
                sender.sendMessage(miniMessage.deserialize("<gold><bold>Apexsions Kingdom Commands:</bold></gold>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/kingdom</yellow> <gray>- Teleport to your kingdom spawn</gray>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/kingdom rtp</yellow> <gray>- Random teleport strictly inside your kingdom</gray>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/kingdom choose</yellow> <gray>- Open kingdom selection GUI</gray>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/kingdom info</yellow> <gray>- Open your interactive Kingdom Profile & Level GUI</gray>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/kingdom top</yellow> <gray>- View the Hall of Fame & Kingdom Leaderboards</gray>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/kingdom rewards</yellow> <gray>- View & claim Level 1–100 progression rewards</gray>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/kingdom xp</yellow> <gray>- Open 13 XP gameplay sources and guide GUI</gray>"));
                sender.sendMessage(miniMessage.deserialize("<yellow>/level</yellow> <gray>- Quick shortcut to your character profile & level progress</gray>"));
                if (sender.hasPermission("apexsionscore.admin")) {
                    sender.sendMessage(miniMessage.deserialize("<gold>/kingdom setking <kingdom> <player></gold> <gray>- Angkat Raja baru kerajaan</gray>"));
                }
                break;
        }

        return true;
    }

    private void handleSetKing(CommandSender sender, String[] args) {
        if (!sender.hasPermission("apexsionscore.admin") && !sender.isOp()) {
            sender.sendMessage(miniMessage.deserialize("<red>Anda tidak memiliki izin untuk mengangkat Raja kerajaan!</red>"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(miniMessage.deserialize("<red>Penggunaan: <yellow>/kingdom setking <kingdom> <player></yellow></red>"));
            return;
        }

        String kingdomInput = args[1].toUpperCase();
        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(kingdomInput);
        if (regionOpt.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Kerajaan <yellow>" + kingdomInput + "</yellow> tidak ditemukan! Pilih: ZENITHAR, SOLTERRA, atau SYLVAMOOR.</red>"));
            return;
        }

        String targetName = args[2];
        plugin.getConfigManager().setKingdomKing(kingdomInput, targetName);

        Player targetPlayer = org.bukkit.Bukkit.getPlayer(targetName);
        if (targetPlayer != null) {
            plugin.getPlayerDataService().updateRegion(targetPlayer.getUniqueId(), regionOpt.get().getId());
            targetPlayer.showTitle(net.kyori.adventure.title.Title.title(
                    miniMessage.deserialize("<gradient:#f1c40f:#e67e22><bold>👑 PENOBATAN RAJA 👑</bold></gradient>"),
                    miniMessage.deserialize("<yellow>Kamu resmi menjadi Raja Tertinggi </yellow>" + regionOpt.get().getDisplayName()),
                    net.kyori.adventure.title.Title.Times.times(java.time.Duration.ofMillis(300), java.time.Duration.ofMillis(3000), java.time.Duration.ofMillis(800))
            ));
        }

        sender.sendMessage(miniMessage.deserialize("<green>✓ Berhasil mengangkat <yellow><bold>" + targetName + "</bold></yellow> sebagai Raja resmi <gold>" + regionOpt.get().getDisplayName() + "</gold>!</green>"));

        // Clean Modern Proclamation
        org.bukkit.Bukkit.broadcast(miniMessage.deserialize(
                "<gradient:#f1c40f:#e67e22><bold>👑 PENOBATAN RAJA</bold></gradient> <dark_gray>➔</dark_gray> <white><bold>"
                + targetName + "</bold></white> <gray>resmi dinobatkan sebagai Raja Tertinggi </gray>" + regionOpt.get().getDisplayName() + "<gray>!</gray>"
        ));

        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void handleKingdomTeleport(Player player) {
        if (!player.hasPermission("apexsionscore.command.region") && !player.hasPermission("kingdomcore.command.kingdom")) {
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
        if (!player.hasPermission("apexsionscore.command.region") && !player.hasPermission("kingdomcore.command.kingdom.choose")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to choose a kingdom.</red>"));
            return;
        }

        plugin.getRegionSelectionGUI().open(player);
    }

    private void handleKingdomInfo(Player player) {
        if (!player.hasPermission("apexsionscore.command.level") && !player.hasPermission("kingdomcore.command.level")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to view your kingdom profile.</red>"));
            return;
        }

        plugin.getKingdomProfileGUI().open(player);
    }

    private void handleKingdomRewards(Player player) {
        if (!player.hasPermission("apexsionscore.command.level") && !player.hasPermission("kingdomcore.command.level")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to view level rewards.</red>"));
            return;
        }

        plugin.getLevelRewardsGUI().open(player, 1);
    }

    private void handleKingdomXpGuide(Player player) {
        if (!player.hasPermission("apexsionscore.command.level") && !player.hasPermission("kingdomcore.command.level")) {
            player.sendMessage(miniMessage.deserialize("<red>You do not have permission to view XP guide.</red>"));
            return;
        }

        plugin.getXpGuideGUI().open(player);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("choose", "info", "top", "profile", "rewards", "claim", "xp", "guide", "level", "rtp", "wild", "wilderness"));
            if (sender.hasPermission("apexsionscore.admin")) {
                list.add("setking");
            }
            List<String> result = new ArrayList<>();
            for (String s : list) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(s);
                }
            }
            return result;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setking")) {
            return Arrays.asList("ZENITHAR", "SOLTERRA", "SYLVAMOOR");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setking")) {
            return null; // suggest online players
        }
        return Collections.emptyList();
    }
}
