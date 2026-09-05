package com.apexsions.customenchants.presets;

import com.apexsions.customenchants.ApexsionsCustomEnchantsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages saving and loading custom item/armor set presets to presets.yml.
 */
public class PresetManager {

    private final ApexsionsCustomEnchantsPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public record Preset(
            String id,
            String displayName,
            long createdAt,
            List<ItemStack> armorPieces,
            List<ItemStack> toolPieces
    ) {}

    private final Map<String, Preset> presets = new LinkedHashMap<>();

    public PresetManager(ApexsionsCustomEnchantsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "presets.yml");
        loadPresets();
    }

    public void loadPresets() {
        presets.clear();
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create presets.yml: " + e.getMessage());
                return;
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("presets")) return;

        for (String key : config.getConfigurationSection("presets").getKeys(false)) {
            String path = "presets." + key;
            String name = config.getString(path + ".display-name", key);
            long time = config.getLong(path + ".created-at", System.currentTimeMillis());

            List<ItemStack> armor = new ArrayList<>();
            List<?> rawArmor = config.getList(path + ".armor");
            if (rawArmor != null) {
                for (Object o : rawArmor) {
                    if (o instanceof ItemStack is) armor.add(is);
                }
            }

            List<ItemStack> tools = new ArrayList<>();
            List<?> rawTools = config.getList(path + ".tools");
            if (rawTools != null) {
                for (Object o : rawTools) {
                    if (o instanceof ItemStack is) tools.add(is);
                }
            }

            presets.put(key.toLowerCase(), new Preset(key.toLowerCase(), name, time, armor, tools));
        }
    }

    public void savePreset(String id, String displayName, List<ItemStack> armor, List<ItemStack> tools) {
        String cleanId = id.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        if (cleanId.isBlank()) cleanId = "preset_" + System.currentTimeMillis();

        Preset p = new Preset(cleanId, displayName, System.currentTimeMillis(), armor, tools);
        presets.put(cleanId, p);

        String path = "presets." + cleanId;
        config.set(path + ".display-name", displayName);
        config.set(path + ".created-at", p.createdAt());
        config.set(path + ".armor", armor);
        config.set(path + ".tools", tools);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save presets.yml: " + e.getMessage());
        }
    }

    public boolean deletePreset(String id) {
        String cleanId = id.toLowerCase();
        if (presets.remove(cleanId) != null) {
            config.set("presets." + cleanId, null);
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save presets.yml: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    public Collection<Preset> getPresets() {
        return presets.values();
    }

    public Preset getPreset(String id) {
        return presets.get(id.toLowerCase());
    }
}
