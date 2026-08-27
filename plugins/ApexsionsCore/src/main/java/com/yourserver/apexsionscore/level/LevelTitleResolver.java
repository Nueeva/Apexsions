package com.yourserver.apexsionscore.level;

import com.yourserver.apexsionscore.ApexsionsCorePlugin;
import com.yourserver.apexsionscore.region.Region;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

/**
 * Resolves level title strings dynamically based on Region and Level tier.
 */
public class LevelTitleResolver {

    private final ApexsionsCorePlugin plugin;

    public LevelTitleResolver(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public String resolveTitle(Region region, int level) {
        level = Math.clamp(level, 1, 100);
        String regionKey = region != null ? region.getKey().toUpperCase(Locale.ROOT) : "default";

        String title = lookupTitle(regionKey, level);
        if (title != null && !title.isEmpty()) {
            return title;
        }

        // Fallback to default table
        title = lookupTitle("default", level);
        if (title != null && !title.isEmpty()) {
            return title;
        }

        return "Citizen";
    }

    private String lookupTitle(String groupKey, int level) {
        ConfigurationSection section = plugin.getConfigManager().getTitlesConfig().getConfigurationSection("level-titles." + groupKey);
        if (section == null) {
            return null;
        }

        for (String rangeKey : section.getKeys(false)) {
            String[] parts = rangeKey.split("-");
            if (parts.length == 2) {
                try {
                    int min = Integer.parseInt(parts[0].trim());
                    int max = Integer.parseInt(parts[1].trim());
                    if (level >= min && level <= max) {
                        return section.getString(rangeKey);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }
}
