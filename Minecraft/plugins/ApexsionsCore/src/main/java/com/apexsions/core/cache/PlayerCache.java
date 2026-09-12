package com.apexsions.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.apexsions.core.player.PlayerData;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * High-performance in-memory cache for active PlayerData using Caffeine.
 */
public class PlayerCache {

    private final Cache<UUID, PlayerData> cache;

    public PlayerCache() {
        try {
            com.github.benmanes.caffeine.cache.RemovalCause.values();
        } catch (Throwable ignored) {
        }
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public Optional<PlayerData> get(UUID uuid) {
        return Optional.ofNullable(cache.getIfPresent(uuid));
    }

    public void put(UUID uuid, PlayerData data) {
        cache.put(uuid, data);
    }

    public void invalidate(UUID uuid) {
        try {
            cache.invalidate(uuid);
        } catch (Throwable t) {
            try {
                cache.asMap().remove(uuid);
            } catch (Throwable ignored) {
            }
        }
    }

    public Collection<PlayerData> getAllCached() {
        return cache.asMap().values();
    }

    public void clear() {
        try {
            cache.invalidateAll();
        } catch (Throwable t) {
            try {
                cache.asMap().clear();
            } catch (Throwable ignored) {
            }
        }
    }
}
