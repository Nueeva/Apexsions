package com.apexsions.core.command;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
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
 * Handles administrative management commands for ApexsionsCore (/apexsionscore, /ac, /kadmin).
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

            case "war":
                handleWar(sender, args);
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
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac addxp <player> <amount></red>"));
                    return true;
                }
                handleAddXp(sender, args[1], args[2]);
                break;

            case "setregion":
            case "setkingdom":
                if (args.length < 3) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac setkingdom <player> <kingdomKey></red>"));
                    return true;
                }
                handleSetRegion(sender, args[1], args[2]);
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac info <player></red>"));
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

    private void handleWar(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<yellow>War Commands:</yellow>"));
            sender.sendMessage(miniMessage.deserialize("<gold>/ac war start <Kingdom1> <Kingdom2> [durasi_menit]</gold>"));
            sender.sendMessage(miniMessage.deserialize("<gold>/ac war stop</gold>"));
            sender.sendMessage(miniMessage.deserialize("<gold>/ac war status</gold>"));
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("start")) {
            if (args.length < 4) {
                sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac war start <Kingdom1> <Kingdom2> [durasi_menit]</red>"));
                return;
            }

            Optional<Region> k1 = plugin.getRegionManager().getRegion(args[2]);
            Optional<Region> k2 = plugin.getRegionManager().getRegion(args[3]);

            if (k1.isEmpty() || k2.isEmpty()) {
                sender.sendMessage(miniMessage.deserialize("<red>Salah satu nama kerajaan tidak valid!</red>"));
                return;
            }

            long duration = 30L;
            if (args.length >= 5) {
                try {
                    duration = Long.parseLong(args[4]);
                } catch (NumberFormatException ignored) {}
            }

            plugin.getWarManager().startWar(k1.get(), k2.get(), duration);
            sender.sendMessage(miniMessage.deserialize("<green>Perang berhasil dideklarasikan antara <yellow>" + k1.get().getKey() + "</yellow> dan <yellow>" + k2.get().getKey() + "</yellow> selama " + duration + " menit!</green>"));
        } else if (action.equals("stop") || action.equals("end")) {
            if (!plugin.getWarManager().isWarActive()) {
                sender.sendMessage(miniMessage.deserialize("<yellow>Tidak ada perang kerajaan yang sedang aktif saat ini.</yellow>"));
                return;
            }
            plugin.getWarManager().stopWar();
            sender.sendMessage(miniMessage.deserialize("<green>Perang kerajaan berhasil dihentikan secara paksa oleh admin.</green>"));
        } else if (action.equals("status")) {
            if (!plugin.getWarManager().isWarActive()) {
                sender.sendMessage(miniMessage.deserialize("<green>Status: <yellow>Semua kerajaan dalam keadaan DAMAI.</yellow></green>"));
            } else {
                long rem = plugin.getWarManager().getRemainingSeconds();
                sender.sendMessage(miniMessage.deserialize("<red>Status: <bold>PERANG AKTIF!</bold></red>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Kerajaan: <yellow>" + plugin.getWarManager().getKingdom1().map(Region::getKey).orElse("?") + "</yellow> vs <yellow>" + plugin.getWarManager().getKingdom2().map(Region::getKey).orElse("?") + "</yellow></gray>"));
                sender.sendMessage(miniMessage.deserialize("<gray>Sisa Waktu: <white>" + (rem / 60) + "m " + (rem % 60) + "s</white></gray>"));
            }
        }
    }

    private void handleSetLevel(CommandSender sender, String playerName, String levelStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found or offline.</red>"));
            return;
        }

        try {
            int newLevel = Integer.parseInt(levelStr);
            if (newLevel < 1 || newLevel > 100) {
                sender.sendMessage(miniMessage.deserialize("<red>Level must be between 1 and 100.</red>"));
                return;
            }

            plugin.getPlayerDataService().getCached(target.getUniqueId()).ifPresent(data -> {
                data.setLevel(newLevel);
                long reqXp = plugin.getLevelFormula().getXpForLevel(newLevel);
                data.setXp(reqXp);
                plugin.getPlayerDataService().save(data);
                sender.sendMessage(miniMessage.deserialize("<green>Set level of " + target.getName() + " to " + newLevel + ".</green>"));
                target.sendMessage(miniMessage.deserialize("<green>Your level has been set to " + newLevel + " by an administrator.</green>"));
            });
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid level number.</red>"));
        }
    }

    private void handleAddXp(CommandSender sender, String playerName, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found or offline.</red>"));
            return;
        }

        try {
            long amount = Long.parseLong(amountStr);
            if (amount <= 0) {
                sender.sendMessage(miniMessage.deserialize("<red>Amount must be greater than 0.</red>"));
                return;
            }

            plugin.getXpService().awardXp(target.getUniqueId(), amount, XpSource.ADMIN);
            sender.sendMessage(miniMessage.deserialize("<green>Added " + amount + " XP to " + target.getName() + ".</green>"));
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid XP amount.</red>"));
        }
    }

    private void handleSetRegion(CommandSender sender, String playerName, String regionKey) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found or offline.</red>"));
            return;
        }

        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(regionKey);
        if (regionOpt.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Kingdom '" + regionKey + "' does not exist.</red>"));
            return;
        }

        Region region = regionOpt.get();
        plugin.getPlayerDataService().getCached(target.getUniqueId()).ifPresent(data -> {
            data.setRegionId(region.getId());
            plugin.getPlayerDataService().save(data);
            sender.sendMessage(miniMessage.deserialize("<green>Set kingdom of " + target.getName() + " to " + region.getKey() + ".</green>"));
            target.sendMessage(miniMessage.deserialize("<green>Your allegiance has been transferred to " + region.getDisplayName() + " by an administrator.</green>"));
        });
    }

    private void handleInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found or offline.</red>"));
            return;
        }

        plugin.getPlayerDataService().getCached(target.getUniqueId()).ifPresentOrElse(data -> {
            sender.sendMessage(miniMessage.deserialize("<gold>--- Player Info: " + target.getName() + " ---</gold>"));
            sender.sendMessage(miniMessage.deserialize("<gray>Level: <yellow>" + data.getLevel() + "</yellow></gray>"));
            sender.sendMessage(miniMessage.deserialize("<gray>XP: <yellow>" + data.getXp() + "</yellow></gray>"));
            String regionName = data.getRegionId() != null ?
                    plugin.getRegionManager().getRegion(data.getRegionId()).map(Region::getKey).orElse("Unknown") : "None";
            sender.sendMessage(miniMessage.deserialize("<gray>Kingdom: <yellow>" + regionName + "</yellow></gray>"));
            sender.sendMessage(miniMessage.deserialize("<gray>Claimed Rewards: <yellow>" + data.getClaimedRewards().size() + "</yellow></gray>"));
        }, () -> sender.sendMessage(miniMessage.deserialize("<red>No cached data found for player.</red>")));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold><bold>=== ApexsionsCore Admin Commands ===</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac reload</yellow> <gray>- Reload all modular configs & markers</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac war <start|stop|status></yellow> <gray>- Manage kingdom wars</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setlevel <player> <level></yellow> <gray>- Set player level (1-100)</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac addxp <player> <amount></yellow> <gray>- Grant progression XP</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setkingdom <player> <kingdomKey></yellow> <gray>- Transfer player kingdom</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac info <player></yellow> <gray>- Inspect player progression data</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = Arrays.asList("reload", "war", "setlevel", "addxp", "setkingdom", "info");
            return filter(list, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("war")) {
            return filter(Arrays.asList("start", "stop", "status"), args[1]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("setlevel") || args[0].equalsIgnoreCase("addxp") || args[0].equalsIgnoreCase("setkingdom") || args[0].equalsIgnoreCase("info"))) {
            return null; // Player names
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("war") && args[1].equalsIgnoreCase("start")) {
            return filter(new ArrayList<>(plugin.getRegionManager().getRegions().stream().map(Region::getKey).toList()), args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("war") && args[1].equalsIgnoreCase("start")) {
            return filter(new ArrayList<>(plugin.getRegionManager().getRegions().stream().map(Region::getKey).toList()), args[3]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setkingdom")) {
            return filter(new ArrayList<>(plugin.getRegionManager().getRegions().stream().map(Region::getKey).toList()), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }
}
