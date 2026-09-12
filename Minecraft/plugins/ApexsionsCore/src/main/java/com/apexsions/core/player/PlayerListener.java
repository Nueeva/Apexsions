package com.apexsions.core.player;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Optional;

/**
 * Listens for player connection events to manage cached profiles and first-join guidance.
 */
public class PlayerListener implements Listener {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerListener(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        // Asynchronously load or create player data before player spawns in world
        try {
            plugin.getPlayerDataService().loadOrCreate(event.getUniqueId(), event.getName()).join();
        } catch (Exception e) {
            plugin.getLogger().warning("Error pre-loading data for " + event.getName() + ": " + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. Ensure player display name is clean text without stale prefixes
        player.displayName(net.kyori.adventure.text.Component.text(player.getName()));
        player.customName(net.kyori.adventure.text.Component.text(player.getName()));

        // 2. Handle LuckPerms Rank default / owner assignment
        if (plugin.getLuckPermsHook().isAvailable() && plugin.getLuckPermsHook().getRankProvisioner() != null) {
            plugin.getLuckPermsHook().getRankProvisioner().handlePlayerJoin(player);
        }

        // 3. Reconcile Level progression in case player has accumulated XP
        plugin.getPlayerDataService().getCached(player.getUniqueId()).ifPresent(data -> {
            if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player)) {
                if (data.hasRegion()) {
                    plugin.getPlayerDataService().updateRegion(player.getUniqueId(), null);
                }
            }
            plugin.getLevelManager().reconcileLevel(data, player);
        });

        // 4. Synchronize rank nametag and scoreboard team immediately and after async rank provisioning
        if (plugin.getRankAnimationManager() != null) {
            plugin.getRankAnimationManager().updatePlayerNameplate(player);
            plugin.getRankAnimationManager().setupScoreboardForNewPlayer(player);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getRankAnimationManager() != null) {
                plugin.getRankAnimationManager().updatePlayerNameplate(player);
                plugin.getRankAnimationManager().setupScoreboardForNewPlayer(player);
            }
        }, 10L);
        
        // 5. Synchronize player stats with Web Platform after authentication / rank init
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getWebBridgeService() != null) {
                plugin.getWebBridgeService().syncPlayerAsync(player);
            }
        }, 40L);

        // 5. First-join guidance
        if (!player.hasPlayedBefore()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    boolean isStaff = plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isConclaveStaff(player);
                    if (isStaff) {
                        player.sendMessage(miniMessage.deserialize("<gradient:#00f2fe:#4facfe><bold>✦ THE AETHERIAL CONCLAVE ✦</bold></gradient> <dark_gray>➔</dark_gray> <aqua>Salam transenden dari Aetherion, Yang Mulia Pengawas Semesta. Anda adalah entitas dimensi atas penjaga keseimbangan Apexsions.</aqua>"));
                        return;
                    }

                    net.kyori.adventure.title.Title.Times times = net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(500),
                            java.time.Duration.ofMillis(4000),
                            java.time.Duration.ofMillis(1000)
                    );
                    net.kyori.adventure.title.Title welcomeTitle = net.kyori.adventure.title.Title.title(
                            miniMessage.deserialize("<gradient:#f1c40f:#e67e22><bold>APEXSIONS</bold></gradient>"),
                            miniMessage.deserialize("<yellow>Selamat Datang, <white>" + player.getName() + "</white>!</yellow>"),
                            times
                    );
                    player.showTitle(welcomeTitle);

                    player.sendMessage(miniMessage.deserialize("<gradient:#f1c40f:#e67e22><bold>KINGDOM</bold></gradient> <dark_gray>➔</dark_gray> <yellow>Pilih salah satu dari 3 kerajaan: <aqua>Zenithar</aqua>, <gold>Solterra</gold>, atau <green>Sylvamoor</green>.</yellow>"));
                    player.sendMessage(miniMessage.deserialize("<gradient:#f1c40f:#e67e22><bold>KINGDOM</bold></gradient> <dark_gray>➔</dark_gray> <gray>Bicara dengan Guide NPC atau ketik <gold><bold><click:run_command:'/kingdom choose'><hover:show_text:'<yellow>Klik untuk membuka Menu Pemilihan Kerajaan</yellow>'>[PILIH KERAJAAN]</click></bold></gold> untuk bersumpah setia!</gray>"));
                }
            }, 30L); // 1.5 seconds delay after initial spawn
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataService().getCached(player.getUniqueId()).ifPresentOrElse(data -> {
            if (data.getActiveTitle() == null || !data.hasRegion()) {
                player.displayName(net.kyori.adventure.text.Component.text(player.getName()));
                player.customName(net.kyori.adventure.text.Component.text(player.getName()));
            }
        }, () -> {
            player.displayName(net.kyori.adventure.text.Component.text(player.getName()));
            player.customName(net.kyori.adventure.text.Component.text(player.getName()));
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 1. Sanitize display name & custom name upon respawn
        player.displayName(net.kyori.adventure.text.Component.text(player.getName()));
        player.customName(net.kyori.adventure.text.Component.text(player.getName()));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getRankAnimationManager() != null) {
                plugin.getRankAnimationManager().updatePlayerNameplate(player);
            }
        }, 2L);

        if (!plugin.getConfigManager().isRespawnAtKingdom()) {
            return;
        }

        boolean overrideBed = plugin.getConfigManager().isOverrideBedSpawn();
        if ((event.isBedSpawn() || event.isAnchorSpawn()) && !overrideBed) {
            return;
        }

        plugin.getPlayerDataService().getCached(player.getUniqueId()).ifPresent(data -> {
            if (data.hasRegion()) {
                Optional<Region> regionOpt = plugin.getRegionManager().getRegion(data.getRegionId());
                if (regionOpt.isPresent()) {
                    Optional<Location> spawnLoc = regionOpt.get().getBukkitSpawnLocation();
                    if (spawnLoc.isPresent()) {
                        event.setRespawnLocation(spawnLoc.get());
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.getWebBridgeService() != null) {
            plugin.getWebBridgeService().syncPlayerAsync(event.getPlayer());
        }
        plugin.getPlayerDataService().flush(event.getPlayer().getUniqueId());
    }
}
