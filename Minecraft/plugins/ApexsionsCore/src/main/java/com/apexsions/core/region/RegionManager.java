package com.apexsions.core.region;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.database.RegionRepository;
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
            ensureSionsRegion();
        }).join();
    }

    /**
     * Ensures Kerajaan Sions is registered with its full territory polygon even when
     * hidden from BlueMap web configurations.
     */
    public void ensureSionsRegion() {
        Region sions = regionsByKey.get("SIONS");
        if (sions == null) {
            UUID sionsId = UUID.nameUUIDFromBytes("apexsions:region:sions".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            sions = new Region(
                    sionsId,
                    "SIONS",
                    "Kerajaan Sions",
                    "world",
                    -6119.0,
                    92.0,
                    -3457.0,
                    0.0f,
                    0.0f,
                    true
            );
            registerRegion(sions);
        }
        if (sions.getPolygon() == null) {
            List<TerritoryPolygon.Point2D> points = List.of(
                    new TerritoryPolygon.Point2D(-5785, -3730),
                    new TerritoryPolygon.Point2D(-5960, -3791),
                    new TerritoryPolygon.Point2D(-6239, -3735),
                    new TerritoryPolygon.Point2D(-6365, -3530),
                    new TerritoryPolygon.Point2D(-6320, -3330),
                    new TerritoryPolygon.Point2D(-6211, -3269),
                    new TerritoryPolygon.Point2D(-6090, -3235),
                    new TerritoryPolygon.Point2D(-5981, -3243),
                    new TerritoryPolygon.Point2D(-5875, -3275),
                    new TerritoryPolygon.Point2D(-5804, -3368),
                    new TerritoryPolygon.Point2D(-5725, -3526)
            );
            sions.setPolygon(new TerritoryPolygon(points, -64.0, 1000.0));
            sions.setLineColor(new java.awt.Color(128, 0, 128));
            sions.setFillColor(new java.awt.Color(128, 0, 128));
            plugin.getLogger().info("Bound permanent secret territory polygon to Kerajaan Sions.");
        }
    }

    public Optional<Region> getRegion(UUID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(regionsById.get(id));
    }

    public Optional<Region> getRegionById(UUID id) {
        return getRegion(id);
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
