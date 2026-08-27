package com.yourserver.apexsionscore.command;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.level.xp.XpSource;
import com.yourserver.apexsionscore.player.PlayerData;
import com.yourserver.apexsionscore.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handles administrative management commands for KingdomCore (/kingdomcore, /kc).
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AdminCommand(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionscore.admin") && !sender.hasPermission("apexionscore.admin") && !sender.hasPermission("kingdomcore.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to use ApexsionsCore admin commands.</red>"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload":
                plugin.getConfigManager().load();
                plugin.getRegionManager().loadRegions();
                if (plugin.getRewardManager() != null) {
                    plugin.getRewardManager().loadRewards();
                }
                if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().getRankProvisioner() != null) {
                    plugin.getLuckPermsHook().getRankProvisioner().provisionRanksAsync();
                }
                if (plugin.getBlueMapHook() != null) {
                    plugin.getBlueMapHook().getConfigParser().parseAndApply();
                }
                sender.sendMessage(miniMessage.deserialize("<green>ApexsionsCore modular configs, LuckPerms ranks, BlueMap markers, and Level Rewards reloaded successfully!</green>"));
                break;

            case "setlevel":
                if (args.length < 3) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac setlevel <player> <level></red>"));
                    return true;
                }
                handleSetLevel(sender, args[1], args[2]);
                break;

            case "addxp":
                if (args.length < 3) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /kc addxp <player> <amount></red>"));
                    return true;
                }
                handleAddXp(sender, args[1], args[2]);
                break;

            case "setregion":
            case "setkingdom":
                if (args.length < 3) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /kc setkingdom <player> <kingdomKey></red>"));
                    return true;
                }
                handleSetRegion(sender, args[1], args[2]);
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /kc info <player></red>"));
                    return true;
                }
                handleInfo(sender, args[1]);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleSetLevel(CommandSender sender, String targetName, String levelStr) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player '" + targetName + "' not found or offline.</red>"));
            return;
        }

        try {
            int level = Integer.parseInt(levelStr);
            plugin.getLevelManager().setLevel(target.getUniqueId(), level);
            sender.sendMessage(miniMessage.deserialize("<green>Set level of <white>" + target.getName() + "</white> to <yellow>" + level + "</yellow>.</green>"));
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid level number.</red>"));
        }
    }

    private void handleAddXp(CommandSender sender, String targetName, String amountStr) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player '" + targetName + "' not found or offline.</red>"));
            return;
        }

        try {
            long amount = Long.parseLong(amountStr);
            if (amount <= 0) {
                sender.sendMessage(miniMessage.deserialize("<red>XP amount must be greater than 0.</red>"));
                return;
            }
            plugin.getLevelManager().addXp(target.getUniqueId(), amount, XpSource.COMMAND);
            sender.sendMessage(miniMessage.deserialize("<green>Added <yellow>" + amount + " XP</yellow> to <white>" + target.getName() + "</white>.</green>"));
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid XP amount number.</red>"));
        }
    }

    private void handleSetRegion(CommandSender sender, String targetName, String regionKey) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player '" + targetName + "' not found or offline.</red>"));
            return;
        }

        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(regionKey);
        if (regionOpt.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Region '" + regionKey + "' does not exist.</red>"));
            return;
        }

        Region region = regionOpt.get();
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(target.getUniqueId());
        if (dataOpt.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Target player data is not loaded.</red>"));
            return;
        }

        PlayerData data = dataOpt.get();
        data.setRegionId(region.getId());
        plugin.getPlayerDataService().save(data);

        sender.sendMessage(miniMessage.deserialize("<green>Set region of <white>" + target.getName() + "</white> to <yellow>" + region.getDisplayName() + "</yellow>.</green>"));
        target.sendMessage(miniMessage.deserialize("<gold>An administrator set your region to <yellow>" + region.getDisplayName() + "</yellow>.</gold>"));
    }

    private void handleInfo(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player '" + targetName + "' not found or offline.</red>"));
            return;
        }

        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(target.getUniqueId());
        if (dataOpt.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Player profile not found in cache.</red>"));
            return;
        }

        PlayerData data = dataOpt.get();
        String regionDisplay = "None";
        if (data.hasRegion()) {
            Optional<Region> regionOpt = plugin.getRegionManager().getRegion(data.getRegionId());
            if (regionOpt.isPresent()) {
                regionDisplay = regionOpt.get().getDisplayName();
            }
        }

        String title = plugin.getLevelManager().getLevelTitle(target.getUniqueId());
        long reqXp = plugin.getLevelManager().getRequiredXpForNextLevel(data.getLevel());
        String reqXpStr = reqXp == Long.MAX_VALUE ? "MAX" : String.valueOf(reqXp);

        sender.sendMessage(miniMessage.deserialize("<gold><bold>═════════ Admin Player Info ═════════</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>Player:</yellow> <white>" + target.getName() + " (" + target.getUniqueId() + ")</white>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>Kingdom:</yellow> <white>" + regionDisplay + "</white>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>Level:</yellow> <gold>" + data.getLevel() + "</gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>Title:</yellow> <gold>" + title + "</gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>XP:</yellow> <green>" + data.getXp() + "</green> <gray>/ " + reqXpStr + "</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold><bold>═════════════════════════════════════</bold></gold>"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold><bold>═════════ ApexsionsCore Admin ═════════</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac reload</yellow> <gray>- Reload plugin configuration</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setlevel <player> <level></yellow> <gray>- Set player level</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac addxp <player> <amount></yellow> <gray>- Add XP to player</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setkingdom <player> <kingdom></yellow> <gray>- Change player kingdom</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac info <player></yellow> <gray>- View player progression info</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold><bold>═════════════════════════════════════</bold></gold>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("apexsionscore.admin") && !sender.hasPermission("apexionscore.admin") && !sender.hasPermission("kingdomcore.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subs = Arrays.asList("reload", "setlevel", "addxp", "setkingdom", "setregion", "info");
            String prefix = args[0].toLowerCase();
            List<String> list = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(prefix)) list.add(s);
            }
            return list;
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    players.add(p.getName());
                }
            }
            return players;
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("setkingdom") || args[0].equalsIgnoreCase("setregion"))) {
            List<String> regions = new ArrayList<>();
            for (Region r : plugin.getRegionManager().getRegions()) {
                if (r.getKey().toLowerCase().startsWith(args[2].toLowerCase())) {
                    regions.add(r.getKey());
                }
            }
            return regions;
        }

        return Collections.emptyList();
    }
}
