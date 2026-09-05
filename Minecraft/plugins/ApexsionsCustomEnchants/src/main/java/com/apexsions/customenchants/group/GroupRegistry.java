package com.apexsions.customenchants.group;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Registry storing and managing the 7 enchantment tiers.
 */
public class GroupRegistry {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final Map<String, EnchantmentGroup> groups = new LinkedHashMap<>();
    private File groupsFile;
    private FileConfiguration groupsConfig;

    public GroupRegistry(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        groups.clear();
        groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        if (!groupsFile.exists()) {
            plugin.saveResource("groups.yml", false);
        }
        groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);

        ConfigurationSection sec = groupsConfig.getConfigurationSection("groups");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            String dName = sec.getString(key + ".display-name", key);
            String color = sec.getString(key + ".color", "#ffffff");
            String iconStr = sec.getString(key + ".icon", "BOOK");
            Material mat = Material.matchMaterial(iconStr);
            if (mat == null) mat = Material.BOOK;

            String currency = sec.getString(key + ".currency", "rupiah");
            double cost = sec.getDouble(key + ".cost", 10000.0);
            boolean enabled = sec.getBoolean(key + ".enabled", true);
            boolean comingSoon = sec.getBoolean(key + ".coming-soon", false);

            EnchantmentGroup group = new EnchantmentGroup(key, dName, color, mat, currency, cost, enabled, comingSoon);
            groups.put(key.toUpperCase(), group);
        }

        plugin.getLogger().info("Loaded " + groups.size() + " enchantment groups/tiers.");
    }

    public void saveGroup(EnchantmentGroup group) {
        if (group == null) return;
        groups.put(group.getId().toUpperCase(), group);

        String path = "groups." + group.getId().toUpperCase();
        groupsConfig.set(path + ".display-name", group.getDisplayName());
        groupsConfig.set(path + ".color", group.getColor());
        groupsConfig.set(path + ".icon", group.getIcon().name());
        groupsConfig.set(path + ".currency", group.getCurrency());
        groupsConfig.set(path + ".cost", group.getCost());
        groupsConfig.set(path + ".enabled", group.isEnabled());
        groupsConfig.set(path + ".coming-soon", group.isComingSoon());

        try {
            groupsConfig.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save groups.yml: " + e.getMessage());
        }
    }

    public EnchantmentGroup getGroup(String id) {
        if (id == null) return null;
        return groups.get(id.toUpperCase().trim());
    }

    public Collection<EnchantmentGroup> getAllGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }
}
