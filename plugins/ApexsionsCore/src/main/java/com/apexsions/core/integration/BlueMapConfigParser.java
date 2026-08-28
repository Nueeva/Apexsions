package com.apexsions.core.integration;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.region.Region;
import com.apexsions.core.region.TerritoryPolygon;

import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses BlueMap map configurations (such as world.conf) to extract kingdom boundaries,
 * spawn positions, labels, and polygon coordinates.
 */
public class BlueMapConfigParser {

    private final ApexsionsCorePlugin plugin;

    public BlueMapConfigParser(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean parseAndApply() {
        File confFile = findWorldConfigFile();
        if (confFile == null || !confFile.exists()) {
            plugin.getLogger().warning("BlueMap world.conf not found. Skipping BlueMap marker import.");
            return false;
        }

        try {
            plugin.getLogger().info("Parsing BlueMap kingdom markers from " + confFile.getPath() + "...");
            String content = Files.readString(confFile.toPath());
            
            // Clean comments from HOCON/JSON
            content = content.replaceAll("(?m)^\\s*#.*$", "");
            content = content.replaceAll("(?m)^\\s*//.*$", "");

            // Extract world name if defined
            String mapWorldName = "world";
            Matcher worldMatcher = Pattern.compile("world\\s*:\\s*\"([^\"]+)\"").matcher(content);
            if (worldMatcher.find()) {
                mapWorldName = worldMatcher.group(1);
            }

            int matchedCount = 0;

            // Regex pattern to extract each marker block: "markerId": { ... }
            Pattern markerPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{([^\\{\\}]*(?:\\{[^\\{\\}]*\\}[^\\{\\}]*)*)\\}");
            Matcher markerMatcher = markerPattern.matcher(content);

            while (markerMatcher.find()) {
                String markerKey = markerMatcher.group(1);
                String blockContent = markerMatcher.group(2);

                if (!blockContent.contains("\"shape\"") && !blockContent.contains("shape")) {
                    continue;
                }

                // Determine kingdom key
                String kingdomKey = resolveKingdomKey(markerKey, blockContent);
                if (kingdomKey == null) {
                    continue;
                }

                Optional<Region> regionOpt = plugin.getRegionManager().getRegion(kingdomKey);
                if (regionOpt.isEmpty()) {
                    continue;
                }

                Region region = regionOpt.get();

                // 1. Parse position (spawn)
                Matcher posMatcher = Pattern.compile("\"position\"\\s*:\\s*\\{\\s*\"x\"\\s*:\\s*([-\\d.]+)\\s*,\\s*\"y\"\\s*:\\s*([-\\d.]+)\\s*,\\s*\"z\"\\s*:\\s*([-\\d.]+)\\s*\\}").matcher(blockContent);
                if (posMatcher.find()) {
                    double px = Double.parseDouble(posMatcher.group(1));
                    double py = Double.parseDouble(posMatcher.group(2));
                    double pz = Double.parseDouble(posMatcher.group(3));

                    region.setWorldName(mapWorldName);
                    region.setSpawnX(px);
                    region.setSpawnY(py);
                    region.setSpawnZ(pz);
                }

                // 2. Parse shape-min-y and shape-max-y
                double minY = -64.0;
                double maxY = 1000.0;
                Matcher minYMatcher = Pattern.compile("\"shape-min-y\"\\s*:\\s*([-\\d.]+)").matcher(blockContent);
                if (minYMatcher.find()) {
                    minY = Double.parseDouble(minYMatcher.group(1));
                }
                Matcher maxYMatcher = Pattern.compile("\"shape-max-y\"\\s*:\\s*([-\\d.]+)").matcher(blockContent);
                if (maxYMatcher.find()) {
                    maxY = Double.parseDouble(maxYMatcher.group(1));
                }

                // 3. Parse shape vertices
                List<TerritoryPolygon.Point2D> points = new ArrayList<>();
                Matcher pointMatcher = Pattern.compile("\\{\\s*\"x\"\\s*:\\s*([-\\d.]+)\\s*,\\s*\"z\"\\s*:\\s*([-\\d.]+)\\s*\\}").matcher(blockContent);
                while (pointMatcher.find()) {
                    double x = Double.parseDouble(pointMatcher.group(1));
                    double z = Double.parseDouble(pointMatcher.group(2));
                    points.add(new TerritoryPolygon.Point2D(x, z));
                }

                if (!points.isEmpty()) {
                    TerritoryPolygon polygon = new TerritoryPolygon(points, minY, maxY);
                    region.setPolygon(polygon);
                }

                // 4. Parse colors
                Matcher lineColMatcher = Pattern.compile("\"line-color\"\\s*:\\s*\\{\\s*\"r\"\\s*:\\s*(\\d+)\\s*,\\s*\"g\"\\s*:\\s*(\\d+)\\s*,\\s*\"b\"\\s*:\\s*(\\d+)").matcher(blockContent);
                if (lineColMatcher.find()) {
                    int r = Integer.parseInt(lineColMatcher.group(1));
                    int g = Integer.parseInt(lineColMatcher.group(2));
                    int b = Integer.parseInt(lineColMatcher.group(3));
                    region.setLineColor(new Color(r, g, b));
                }

                plugin.getRegionManager().registerRegion(region);
                matchedCount++;
                plugin.getLogger().info("Mapped BlueMap kingdom marker '" + markerKey + "' -> " + kingdomKey + 
                        " (Vertices: " + points.size() + ", Spawn: " + region.getSpawnX() + ", " + region.getSpawnY() + ", " + region.getSpawnZ() + ")");
            }

            plugin.getLogger().info("Successfully loaded " + matchedCount + " kingdom boundaries from BlueMap config.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed parsing BlueMap world.conf: " + e.getMessage());
            return false;
        }
    }

    private String resolveKingdomKey(String markerKey, String blockContent) {
        String lower = (markerKey + " " + blockContent).toLowerCase(Locale.ROOT);
        if (lower.contains("zenithar") || markerKey.equalsIgnoreCase("raja2")) {
            return "ZENITHAR";
        }
        if (lower.contains("solterra") || markerKey.equalsIgnoreCase("raja1")) {
            return "SOLTERRA";
        }
        if (lower.contains("sylvamoor") || markerKey.equalsIgnoreCase("raja3")) {
            return "SYLVAMOOR";
        }
        return null;
    }

    private File findWorldConfigFile() {
        // 1. Check configured custom path
        String customPath = plugin.getConfig().getString("bluemap.config-path", "world.conf");
        File customFile = new File(customPath);
        if (customFile.exists()) return customFile;

        File inPluginData = new File(plugin.getDataFolder(), customPath);
        if (inPluginData.exists()) return inPluginData;

        // 2. Check BlueMap default map folder
        File blueMapFolder = new File(plugin.getDataFolder().getParentFile(), "BlueMap/maps/world.conf");
        if (blueMapFolder.exists()) return blueMapFolder;

        // 3. Check server root
        File rootFile = new File("world.conf");
        if (rootFile.exists()) return rootFile;

        return null;
    }
}
