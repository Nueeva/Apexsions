package com.apexsions.battlepass.player;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.database.PlayerDataRepository;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final ApexsionsBattlepass plugin;
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    public PlayerManager(ApexsionsBattlepass plugin, PlayerDataRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (uuid == null) return null;
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) {
            int currentSeasonId = (plugin.getSeasonManager() != null && plugin.getSeasonManager().getCurrentSeason() != null)
                    ? plugin.getSeasonManager().getCurrentSeason().getId() : 1;
            try {
                data = repository.loadPlayerData(uuid, currentSeasonId).get(1, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception ignored) {}

            if (data == null) {
                data = new PlayerData(uuid, currentSeasonId);
            }
            playerDataCache.put(uuid, data);
        }
        return data;
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void loadPlayerData(Player player, int currentSeasonId) {
        repository.loadPlayerData(player.getUniqueId(), currentSeasonId).thenAccept(data -> {
            playerDataCache.put(player.getUniqueId(), data);
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getXpService().checkLevelUp(player, data);
                }
            });
        });
    }

    public void unloadPlayerData(UUID uuid) {
        PlayerData data = playerDataCache.remove(uuid);
        if (data != null) {
            repository.savePlayerData(data);
        }
    }

    public void saveAllPlayerData() {
        for (PlayerData data : playerDataCache.values()) {
            repository.savePlayerData(data);
        }
    }

    public Map<UUID, PlayerData> getPlayerDataCache() {
        return playerDataCache;
    }
}
