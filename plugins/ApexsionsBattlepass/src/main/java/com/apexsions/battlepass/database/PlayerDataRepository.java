package com.apexsions.battlepass.database;

import com.apexsions.battlepass.player.PlayerData;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataRepository {

    void init();

    CompletableFuture<PlayerData> loadPlayerData(UUID uuid, int currentSeasonId);

    CompletableFuture<Void> savePlayerData(PlayerData playerData);

    CompletableFuture<List<PlayerData>> loadAllPlayerData(int currentSeasonId);

    void close();
}

