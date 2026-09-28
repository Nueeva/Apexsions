package com.apexsions.jobs.service;

import com.apexsions.jobs.database.JobsRepository;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PlacedBlockTracker {

    private final JobsRepository repository;
    private final Set<String> memoryCache;

    public PlacedBlockTracker(JobsRepository repository) {
        this.repository = repository;
        // Keep up to 20,000 recently placed block keys in memory for instant O(1) checks
        int maxCapacity = 20000;
        Map<String, Boolean> lru = new LinkedHashMap<>(maxCapacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > maxCapacity;
            }
        };
        this.memoryCache = Collections.synchronizedSet(Collections.newSetFromMap(lru));
    }

    private String toKey(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    public void addPlaced(Block block) {
        if (block == null) return;
        String key = toKey(block);
        memoryCache.add(key);
        repository.recordPlacedBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public boolean isPlaced(Block block) {
        if (block == null) return false;
        String key = toKey(block);
        if (memoryCache.contains(key)) {
            return true;
        }
        return repository.isPlacedBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public void removePlaced(Block block) {
        if (block == null) return;
        String key = toKey(block);
        memoryCache.remove(key);
        repository.removePlacedBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }
}
