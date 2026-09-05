package com.apexsions.core.player;

import com.apexsions.core.ApexsionsCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
            plugin.getLevelManager().reconcileLevel(data, player);
        });
        
        // 3. First-join guidance
        if (!player.hasPlayedBefore()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    net.kyori.adventure.title.Title.Times times = net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ofMillis(500),
                            java.time.Duration.ofMillis(4000),
                            java.time.Duration.ofMillis(1000)
                    );
                    net.kyori.adventure.title.Title welcomeTitle = net.kyori.adventure.title.Title.title(
                            miniMessage.deserialize("<gradient:#f1c40f:#e67e22><bold>APEXSIONS KINGDOM</bold></gradient>"),
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataService().flush(event.getPlayer().getUniqueId());
    }
}
