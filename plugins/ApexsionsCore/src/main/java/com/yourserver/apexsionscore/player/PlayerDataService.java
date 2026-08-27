package com.yourserver.apexsionscore.player;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.cache.PlayerCache;
import com.yourserver.apexsionscore.database.PlayerRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service managing PlayerData lifecycles, caching, and persistence.
 */
public class PlayerDataService {

    private final ApexsionsCorePlugin plugin;
    private final PlayerRepository repository;
    private final PlayerCache cache;

    public PlayerDataService(ApexsionsCorePlugin plugin, PlayerRepository repository, PlayerCache cache) {
        this.plugin = plugin;
        this.repository = repository;
        this.cache = cache;
    }

    public Optional<PlayerData> getCached(UUID uuid) {
        return cache.get(uuid);
    }

    public CompletableFuture<PlayerData> loadOrCreate(UUID uuid, String username) {
        Optional<PlayerData> cached = cache.get(uuid);
        if (cached.isPresent()) {
            PlayerData data = cached.get();
            if (!data.getUsername().equalsIgnoreCase(username)) {
                data.setUsername(username);
                repository.save(data);
            }
            return CompletableFuture.completedFuture(data);
        }

        return repository.findByUuid(uuid).thenCompose(opt -> {
            if (opt.isPresent()) {
                PlayerData data = opt.get();
                if (!data.getUsername().equalsIgnoreCase(username)) {
                    data.setUsername(username);
                    repository.save(data);
                }
                cache.put(uuid, data);
                return CompletableFuture.completedFuture(data);
            } else {
                PlayerData newData = PlayerData.createDefault(uuid, username);
                cache.put(uuid, newData);
                return repository.save(newData).thenApply(v -> newData);
            }
        });
    }

    public CompletableFuture<Void> save(PlayerData data) {
        data.markUpdated();
        cache.put(data.getUuid(), data);
        return repository.save(data);
    }

    public CompletableFuture<Void> updateRegion(UUID playerUuid, UUID regionId) {
        cache.get(playerUuid).ifPresent(data -> data.setRegionId(regionId));
        return repository.updateRegion(playerUuid, regionId);
    }

    public void flush(UUID uuid) {
        cache.get(uuid).ifPresent(data -> {
            repository.save(data);
            cache.invalidate(uuid);
        });
    }

    public void saveAllCached() {
        for (PlayerData data : cache.getAllCached()) {
            repository.save(data).join();
        }
    }

    public void flushAll() {
        saveAllCached();
    }
}
