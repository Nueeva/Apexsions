package com.apexsions.fishing.service;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.FishingZone;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing all WorldEdit-defined Fishing Zones and their persistence in zones.yml.
 */
public class FishingZoneManager {

    private final ApexsionsFishing plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, FishingZone> zones = new ConcurrentHashMap<>();

    public FishingZoneManager(ApexsionsFishing plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "zones.yml");
        reload();
    }

    public synchronized void reload() {
        zones.clear();
        if (!file.exists()) {
            plugin.saveResource("zones.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection sec = config.getConfigurationSection("zones");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection zSec = sec.getConfigurationSection(key);
                if (zSec != null) {
                    Map<String, Object> values = zSec.getValues(false);
                    FishingZone zone = FishingZone.deserialize(key, values);
                    if (zone != null) {
                        zones.put(key.toLowerCase(), zone);
                    }
                }
            }
        }
        plugin.getLogger().info("FishingZoneManager: " + zones.size() + " area mancing berhasil dimuat dari zones.yml!");
    }

    public synchronized void save() {
        if (config == null) {
            config = new YamlConfiguration();
        }
        config.set("zones", null); // clear existing
        for (FishingZone zone : zones.values()) {
            config.createSection("zones." + zone.getName(), zone.serialize());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan zones.yml: " + e.getMessage());
        }
    }

    public boolean createZone(
            @NotNull String name,
            @NotNull String displayName,
            @NotNull String worldName,
            int x1, int y1, int z1,
            int x2, int y2, int z2,
            boolean afkAllowed,
            double rateMultiplier,
            double rarityMultiplier,
            double weightMultiplier,
            double xpMultiplier
    ) {
        String cleanName = name.toLowerCase().trim();
        FishingZone zone = new FishingZone(
                cleanName, displayName, worldName,
                x1, y1, z1, x2, y2, z2,
                afkAllowed, rateMultiplier, rarityMultiplier, weightMultiplier, xpMultiplier
        );
        zones.put(cleanName, zone);
        save();
        return true;
    }

    public boolean deleteZone(@NotNull String name) {
        String cleanName = name.toLowerCase().trim();
        FishingZone removed = zones.remove(cleanName);
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    @Nullable
    public FishingZone getZone(@Nullable String name) {
        if (name == null) return null;
        return zones.get(name.toLowerCase().trim());
    }

    public Collection<FishingZone> getAllZones() {
        return Collections.unmodifiableCollection(zones.values());
    }

    @Nullable
    public FishingZone getZoneAt(@Nullable Location location) {
        if (location == null) return null;
        for (FishingZone zone : zones.values()) {
            if (zone.contains(location)) {
                return zone;
            }
        }
        return null;
    }

    public boolean isInZone(@Nullable Location location) {
        return getZoneAt(location) != null;
    }

    public boolean isAfkAllowedAt(@Nullable Location location) {
        boolean requireZone = plugin.getConfig().getBoolean("settings.afk-fishing.require-zone", false);
        FishingZone zone = getZoneAt(location);
        if (requireZone) {
            return zone != null && zone.isAfkAllowed();
        }
        if (zone != null) {
            return zone.isAfkAllowed();
        }
        return true;
    }
}
