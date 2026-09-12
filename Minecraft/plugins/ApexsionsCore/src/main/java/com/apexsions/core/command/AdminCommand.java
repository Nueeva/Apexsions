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
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac setkingdom <player> <ZENITHAR|SOLTERRA|SYLVAMOOR></red>"));
                    return true;
                }
                handleSetRegion(sender, args[1], args[2]);
                break;

            case "resetregion":
            case "resetkingdom":
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac resetkingdom <player></red>"));
                    return true;
                }
                handleResetRegion(sender, args[1]);
                break;

            case "sync":
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac sync <player></red>"));
                    return true;
                }
                Player targetToSync = Bukkit.getPlayerExact(args[1]);
                if (targetToSync == null) {
                    sender.sendMessage(miniMessage.deserialize("<red>Player <yellow>" + args[1] + "</yellow> is not online.</red>"));
                    return true;
                }
                if (plugin.getWebBridgeService() != null) {
                    plugin.getWebBridgeService().syncPlayerAsync(targetToSync);
                    sender.sendMessage(miniMessage.deserialize("<green>Sync request dispatched for <yellow>" + targetToSync.getName() + "</yellow> to web portal.</green>"));
                } else {
                    sender.sendMessage(miniMessage.deserialize("<red>WebBridge service is unavailable.</red>"));
                }
                break;

            case "setlobby":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Only players can set the lobby location.</red>"));
                    return true;
                }
                plugin.getConfigManager().setLobbyLocation(player.getLocation());
                sender.sendMessage(miniMessage.deserialize("<green>Lobby location updated to world <yellow>" + player.getWorld().getName() + "</yellow> at (" + 
                        String.format("%.1f", player.getLocation().getX()) + ", " + 
                        String.format("%.1f", player.getLocation().getY()) + ", " + 
                        String.format("%.1f", player.getLocation().getZ()) + ")!</green>"));
                break;

            case "setspawn":
            case "setcapital":
            case "setkingdomspawn":
                if (!(sender instanceof Player pSpawn)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Only players can set kingdom capital spawn location.</red>"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac setspawn <ZENITHAR|SOLTERRA|SYLVAMOOR></red>"));
                    return true;
                }
                handleSetSpawn(pSpawn, args[1]);
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage(miniMessage.deserialize("<red>Usage: /ac info <player></red>"));
                    return true;
                }
                handleInfo(sender, args[1]);
                break;

            case "rewards":
            case "reward":
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(miniMessage.deserialize("<red>Perintah ini hanya dapat digunakan oleh pemain in-game.</red>"));
                    return true;
                }
                p.openInventory(new com.apexsions.core.gui.admin.AdminLevelRewardListGUI(plugin, p, 1).getInventory());
                break;

            case "cancelinput":
                if (sender instanceof Player pCancel) {
                    plugin.getAdminChatInputManager().cancelSession(pCancel.getUniqueId(), true);
                }
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

            String k1Input = args[2].toUpperCase(Locale.ROOT);
            String k2Input = args[3].toUpperCase(Locale.ROOT);
            if (k1Input.equals("SIONS") || k2Input.equals("SIONS")) {
                sender.sendMessage(miniMessage.deserialize("<red>SIONS adalah reruntuhan kuno (Terra Interdicta) dan tidak berpartisipasi dalam Perang Kerajaan!</red>"));
                return;
            }

            if (!plugin.getRegionManager().isPlayableKingdom(k1Input) || !plugin.getRegionManager().isPlayableKingdom(k2Input)) {
                sender.sendMessage(miniMessage.deserialize("<red>Perang hanya dapat dideklarasikan antar-kerajaan fana: ZENITHAR, SOLTERRA, SYLVAMOOR.</red>"));
                return;
            }

            Optional<Region> k1 = plugin.getRegionManager().getRegion(k1Input);
            Optional<Region> k2 = plugin.getRegionManager().getRegion(k2Input);

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
        int newLevel;
        try {
            newLevel = Integer.parseInt(levelStr);
            if (newLevel < 1 || newLevel > 100) {
                sender.sendMessage(miniMessage.deserialize("<red>Level must be between 1 and 100.</red>"));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid level number.</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target != null) {
            plugin.getPlayerDataService().getCached(target.getUniqueId()).ifPresent(data -> {
                data.setLevel(newLevel);
                long reqXp = plugin.getLevelFormula().getXpForLevel(newLevel);
                data.setXp(reqXp);
                plugin.getPlayerDataService().save(data);
                sender.sendMessage(miniMessage.deserialize("<green>Set level of " + target.getName() + " to " + newLevel + ".</green>"));
                target.sendMessage(miniMessage.deserialize("<green>Your level has been set to " + newLevel + " by an administrator.</green>"));
            });
            return;
        }

        // Support offline player via asynchronous persistence
        org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
        if (offline.getUniqueId() == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player '" + playerName + "' not found.</red>"));
            return;
        }

        String targetName = offline.getName() != null ? offline.getName() : playerName;
        plugin.getPlayerDataService().loadOrCreate(offline.getUniqueId(), targetName).thenAccept(data -> {
            data.setLevel(newLevel);
            long reqXp = plugin.getLevelFormula().getXpForLevel(newLevel);
            data.setXp(reqXp);
            plugin.getPlayerDataService().save(data);
            sender.sendMessage(miniMessage.deserialize("<green>Set level of " + targetName + " (offline) to " + newLevel + ".</green>"));
        }).exceptionally(ex -> {
            sender.sendMessage(miniMessage.deserialize("<red>Failed to set level for offline player: " + ex.getMessage() + "</red>"));
            return null;
        });
    }

    private void handleAddXp(CommandSender sender, String playerName, String amountStr) {
        long amount;
        try {
            amount = Long.parseLong(amountStr);
            if (amount <= 0) {
                sender.sendMessage(miniMessage.deserialize("<red>Amount must be greater than 0.</red>"));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid XP amount.</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target != null) {
            plugin.getXpService().awardXp(target.getUniqueId(), amount, XpSource.ADMIN);
            sender.sendMessage(miniMessage.deserialize("<green>Added " + amount + " XP to " + target.getName() + ".</green>"));
            return;
        }

        // Support offline player via asynchronous persistence
        org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(playerName);
        if (offline.getUniqueId() == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player '" + playerName + "' not found.</red>"));
            return;
        }

        String targetName = offline.getName() != null ? offline.getName() : playerName;
        plugin.getPlayerDataService().loadOrCreate(offline.getUniqueId(), targetName).thenAccept(data -> {
            long newXp = data.getXp() + amount;
            data.setXp(newXp);
            plugin.getLevelManager().reconcileLevel(data, null);
            plugin.getPlayerDataService().save(data);
            sender.sendMessage(miniMessage.deserialize("<green>Added " + amount + " XP to " + targetName + " (offline).</green>"));
        }).exceptionally(ex -> {
            sender.sendMessage(miniMessage.deserialize("<red>Failed to award XP to offline player: " + ex.getMessage() + "</red>"));
            return null;
        });
    }

    private void handleSetRegion(CommandSender sender, String playerName, String regionKey) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found or offline.</red>"));
            return;
        }

        String key = regionKey.toUpperCase(Locale.ROOT);
        if (key.equals("SIONS")) {
            sender.sendMessage(miniMessage.deserialize("<red>Kerajaan SIONS adalah reruntuhan terlarang kuno (Terra Interdicta), bukan faksi fana yang dapat dihuni! Pilih: ZENITHAR, SOLTERRA, atau SYLVAMOOR.</red>"));
            return;
        }

        if (!plugin.getRegionManager().isPlayableKingdom(key)) {
            sender.sendMessage(miniMessage.deserialize("<red>Kerajaan '" + regionKey + "' tidak valid! Pilihan resmi: ZENITHAR, SOLTERRA, SYLVAMOOR.</red>"));
            return;
        }

        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(key);
        if (regionOpt.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Kingdom '" + regionKey + "' does not exist in registry.</red>"));
            return;
        }

        Region region = regionOpt.get();
        plugin.getPlayerDataService().updateRegion(target.getUniqueId(), region.getId());
        sender.sendMessage(miniMessage.deserialize("<green>Set kingdom of " + target.getName() + " to " + region.getKey() + ".</green>"));
        target.sendMessage(miniMessage.deserialize("<green>Your allegiance has been transferred to " + region.getDisplayName() + " by an administrator.</green>"));
    }

    private void handleResetRegion(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(miniMessage.deserialize("<red>Player not found or offline.</red>"));
            return;
        }

        plugin.getPlayerDataService().updateRegion(target.getUniqueId(), null);
        sender.sendMessage(miniMessage.deserialize("<green>Reset kingdom allegiance of " + target.getName() + ".</green>"));
        target.sendMessage(miniMessage.deserialize("<yellow>Your kingdom allegiance has been reset by an administrator. You may choose again using <gold>/kingdom choose</gold>.</yellow>"));
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

    private void handleSetSpawn(Player player, String kingdomKey) {
        String key = kingdomKey.toUpperCase(Locale.ROOT);
        if (key.equals("SIONS")) {
            player.sendMessage(miniMessage.deserialize("<red>SIONS adalah reruntuhan kuno (Terra Interdicta) dan tidak memiliki ibukota fana! Titik spawn hanya untuk: ZENITHAR, SOLTERRA, SYLVAMOOR.</red>"));
            return;
        }

        if (!plugin.getRegionManager().isPlayableKingdom(key)) {
            player.sendMessage(miniMessage.deserialize("<red>Kerajaan <yellow>" + key + "</yellow> tidak valid! Pilihan resmi: ZENITHAR, SOLTERRA, SYLVAMOOR.</red>"));
            return;
        }

        Optional<Region> regionOpt = plugin.getRegionManager().getRegion(key);
        if (regionOpt.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>Kerajaan <yellow>" + key + "</yellow> tidak ditemukan! Pilihan: ZENITHAR, SOLTERRA, SYLVAMOOR.</red>"));
            return;
        }

        Region region = regionOpt.get();
        org.bukkit.Location loc = player.getLocation();
        region.setWorldName(loc.getWorld().getName());
        region.setSpawnX(loc.getX());
        region.setSpawnY(loc.getY());
        region.setSpawnZ(loc.getZ());
        region.setSpawnYaw(loc.getYaw());
        region.setSpawnPitch(loc.getPitch());

        // 1. Save to Database & active region registry
        plugin.getRegionManager().registerRegion(region);

        // 2. Update config kingdoms.yml
        String coordStr = String.format("%s (%d, %d, %d)", 
                loc.getWorld().getName(), 
                loc.getBlockX(), 
                loc.getBlockY(), 
                loc.getBlockZ());
        plugin.getConfigManager().setKingdomCapitalCoordinates(key, coordStr);

        player.playSound(loc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        player.sendMessage(miniMessage.deserialize("<green>✓ Titik spawn ibukota kerajaan <yellow><bold>" + region.getDisplayName() + "</bold></yellow> berhasil disetel ke world <aqua>" + loc.getWorld().getName() + "</aqua> pada koordinat <gold>(" + 
                String.format("%.1f", loc.getX()) + ", " + 
                String.format("%.1f", loc.getY()) + ", " + 
                String.format("%.1f", loc.getZ()) + ")</gold>!</green>"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold><bold>=== ApexsionsCore Admin Commands ===</bold></gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac reload</yellow> <gray>- Reload all modular configs & markers</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac war <start|stop|status></yellow> <gray>- Manage kingdom wars</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setlevel <player> <level></yellow> <gray>- Set player level (1-100)</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac addxp <player> <amount></yellow> <gray>- Grant progression XP</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setkingdom <player> <kingdomKey></yellow> <gray>- Transfer player kingdom (Zenithar, Solterra, Sylvamoor)</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac resetkingdom <player></yellow> <gray>- Reset player kingdom allegiance</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setlobby</yellow> <gray>- Set lobby spawn to your current location/world</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac setspawn <kingdom></yellow> <gray>- Set kingdom capital spawn to your current location</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/ac info <player></yellow> <gray>- Inspect player progression data</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = Arrays.asList("reload", "war", "setlevel", "addxp", "setkingdom", "resetkingdom", "setlobby", "setspawn", "info", "sync");
            return filter(list, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("war")) {
            return filter(Arrays.asList("start", "stop", "status"), args[1]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("setcapital") || args[0].equalsIgnoreCase("setkingdomspawn"))) {
            return filter(plugin.getRegionManager().getPlayableKingdomKeys(), args[1]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("setlevel") || args[0].equalsIgnoreCase("addxp") || args[0].equalsIgnoreCase("setkingdom") || args[0].equalsIgnoreCase("resetkingdom") || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("sync"))) {
            return null; // Player names
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("war") && args[1].equalsIgnoreCase("start")) {
            return filter(plugin.getRegionManager().getPlayableKingdomKeys(), args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("war") && args[1].equalsIgnoreCase("start")) {
            return filter(plugin.getRegionManager().getPlayableKingdomKeys(), args[3]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setkingdom")) {
            return filter(plugin.getRegionManager().getPlayableKingdomKeys(), args[2]);
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
