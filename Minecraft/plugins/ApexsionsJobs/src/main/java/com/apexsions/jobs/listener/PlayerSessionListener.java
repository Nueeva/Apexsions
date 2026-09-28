package com.apexsions.jobs.listener;

import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.PlayerJobData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerSessionListener implements Listener {

    private final ApexsionsJobs plugin;

    public PlayerSessionListener(ApexsionsJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getRepository().loadPlayerJobs(uuid).thenAccept(jobs -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                plugin.getJobService().setPlayerJobs(uuid, jobs);
            });
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Map<String, PlayerJobData> map = plugin.getJobService().getPlayerJobs(uuid);
        if (map != null) {
            for (PlayerJobData data : map.values()) {
                plugin.getRepository().savePlayerJob(data);
            }
            plugin.getJobService().removePlayerJobs(uuid);
        }
    }
}
