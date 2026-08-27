package com.yourserver.apexsionscore.player;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
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

        // 1. Handle LuckPerms Rank default / owner assignment
        if (plugin.getLuckPermsHook().isAvailable() && plugin.getLuckPermsHook().getRankProvisioner() != null) {
            plugin.getLuckPermsHook().getRankProvisioner().handlePlayerJoin(player);
        }
        
        // 2. First-join guidance
        if (!player.hasPlayedBefore()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(miniMessage.deserialize("<gold><bold>════════════════════════════════════════════════</bold></gold>"));
                    player.sendMessage(miniMessage.deserialize("<yellow><bold>Welcome to Apexsions,</bold> <white>" + player.getName() + "</white>!</yellow>"));
                    player.sendMessage(miniMessage.deserialize("<gray>Discover the 3 great realms: <aqua>Zenithar</aqua>, <gold>Solterra</gold>, and <green>Sylvamoor</green>.</gray>"));
                    player.sendMessage(miniMessage.deserialize("<gray>When ready, interact with a Guide NPC or click <gold><bold><click:run_command:'/kingdom choose'><hover:show_text:'<yellow>Click to open Kingdom Selection Menu</yellow>'>[CHOOSE KINGDOM]</click></bold></gold> to pledge allegiance!</gray>"));
                    player.sendMessage(miniMessage.deserialize("<gold><bold>════════════════════════════════════════════════</bold></gold>"));
                }
            }, 30L); // 1.5 seconds delay after initial spawn
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataService().flush(event.getPlayer().getUniqueId());
    }
}
