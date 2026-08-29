package com.apexsions.shop.category;

import com.apexsions.shop.ApexsionsShop;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class ShopItemRegistry {

    private final ApexsionsShop plugin;
    private final Map<String, ShopItem> itemById = new LinkedHashMap<>();
    private final Map<Material, ShopItem> itemByMaterial = new EnumMap<>(Material.class);
    private final Map<ShopCategory, List<ShopItem>> itemsByCategory = new EnumMap<>(ShopCategory.class);

    public ShopItemRegistry(ApexsionsShop plugin) {
        this.plugin = plugin;
        for (ShopCategory cat : ShopCategory.values()) {
            itemsByCategory.put(cat, new ArrayList<>());
        }
    }

    public void load() {
        itemById.clear();
        itemByMaterial.clear();
        for (ShopCategory cat : ShopCategory.values()) {
            itemsByCategory.get(cat).clear();
        }

        File categoriesDir = new File(plugin.getDataFolder(), "categories");
        if (!categoriesDir.exists()) {
            categoriesDir.mkdirs();
        }

        for (ShopCategory category : ShopCategory.values()) {
            String fileName = category.getId() + ".yml";
            File categoryFile = new File(categoriesDir, fileName);

            if (!categoryFile.exists()) {
                plugin.saveResource("categories/" + fileName, false);
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(categoryFile);

            // Merge defaults from jar if needed
            InputStream defStream = plugin.getResource("categories/" + fileName);
            if (defStream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8)));
            }

            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            if (itemsSection == null) continue;

            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection sec = itemsSection.getConfigurationSection(key);
                if (sec == null) continue;

                String matName = sec.getString("material");
                if (matName == null) continue;

                Material mat = Material.matchMaterial(matName);
                if (mat == null) {
                    plugin.getLogger().warning("Unknown material '" + matName + "' in " + fileName);
                    continue;
                }

                boolean buyEnabled = sec.getBoolean("buy-enabled", sec.getDouble("buy-price", 1.0) > 0);
                double buyPrice = Math.max(1.0, sec.getDouble("buy-price", 20.0));
                double sellPrice = sec.getDouble("sell-price", buyPrice * 0.20);
                String displayName = sec.getString("display-name", "<white>" + mat.name() + "</white>");

                ShopItem shopItem = new ShopItem(key.toLowerCase(), mat, category, buyPrice, sellPrice, buyEnabled, displayName);
                itemById.put(shopItem.getId(), shopItem);
                itemByMaterial.put(mat, shopItem);
                itemsByCategory.get(category).add(shopItem);
            }
        }

        plugin.getLogger().info("Loaded " + itemById.size() + " shop items across " + ShopCategory.values().length + " categories.");
    }

    public ShopItem getItem(String id) {
        if (id == null) return null;
        return itemById.get(id.toLowerCase());
    }

    public ShopItem getItem(Material material) {
        if (material == null) return null;
        return itemByMaterial.get(material);
    }

    public List<ShopItem> getItemsByCategory(ShopCategory category) {
        return Collections.unmodifiableList(itemsByCategory.getOrDefault(category, Collections.emptyList()));
    }

    public Collection<ShopItem> getAllItems() {
        return Collections.unmodifiableCollection(itemById.values());
    }
}
