package com.apexsions.battlepass.player.listener;

import com.apexsions.battlepass.ApexsionsBattlepass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final ApexsionsBattlepass plugin;

    public PlayerConnectionListener(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int currentSeasonId = plugin.getSeasonManager().getCurrentSeason().getId();
        plugin.getPlayerManager().loadPlayerData(event.getPlayer(), currentSeasonId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerManager().unloadPlayerData(event.getPlayer().getUniqueId());
    }
}
