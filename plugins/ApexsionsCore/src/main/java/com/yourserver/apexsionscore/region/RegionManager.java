package com.yourserver.apexsionscore.region;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.database.RegionRepository;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active Regions in the KingdomCore system and spatial territory queries.
 */
public class RegionManager {

    private final ApexsionsCorePlugin plugin;
    private final RegionRepository repository;
    private final Map<UUID, Region> regionsById = new ConcurrentHashMap<>();
    private final Map<String, Region> regionsByKey = new ConcurrentHashMap<>();

    public RegionManager(ApexsionsCorePlugin plugin, RegionRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void loadRegions() {
        repository.findAll().thenAccept(list -> {
            regionsById.clear();
            regionsByKey.clear();
            for (Region r : list) {
                if (r.isEnabled()) {
                    regionsById.put(r.getId(), r);
                    regionsByKey.put(r.getKey().toUpperCase(Locale.ROOT), r);
                }
            }
            plugin.getLogger().info("Loaded " + regionsById.size() + " active regions from database.");
        }).join();
    }

    public Optional<Region> getRegion(UUID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(regionsById.get(id));
    }

    public Optional<Region> getRegion(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(regionsByKey.get(key.toUpperCase(Locale.ROOT)));
    }

    public Optional<Region> getRegionAt(Location location) {
        if (location == null || location.getWorld() == null) {
            return Optional.empty();
        }

        for (Region r : regionsById.values()) {
            if (r.containsLocation(location)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    public Collection<Region> getRegions() {
        return Collections.unmodifiableCollection(regionsById.values());
    }

    public void registerRegion(Region region) {
        regionsById.put(region.getId(), region);
        regionsByKey.put(region.getKey().toUpperCase(Locale.ROOT), region);
        repository.save(region);
    }
}
