package com.apex.battlepass.player;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.database.PlayerDataRepository;
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
        return playerDataCache.get(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void loadPlayerData(Player player, int currentSeasonId) {
        repository.loadPlayerData(player.getUniqueId(), currentSeasonId).thenAccept(data -> {
            playerDataCache.put(player.getUniqueId(), data);
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
