package com.apex.battlepass.shop.refresh;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.shop.ItemRarity;
import com.apex.battlepass.shop.ShopCategory;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class RarityChanceService {

    private final ApexsionsBattlepass plugin;
    private final Map<ShopCategory, Map<ItemRarity, Double>> categoryChances = new EnumMap<>(ShopCategory.class);
    private final Random random = new Random();

    public RarityChanceService(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        loadChances();
    }

    public void loadChances() {
        categoryChances.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("shop.rarity-chances");
        for (ShopCategory cat : ShopCategory.values()) {
            Map<ItemRarity, Double> chances = new EnumMap<>(ItemRarity.class);
            String path = cat.name().toLowerCase();
            ConfigurationSection catSec = sec != null ? sec.getConfigurationSection(path) : null;

            if (catSec != null) {
                for (ItemRarity rarity : ItemRarity.values()) {
                    double val = catSec.getDouble(rarity.name().toLowerCase(), getDefaultChance(cat, rarity));
                    chances.put(rarity, val);
                }
            } else {
                for (ItemRarity rarity : ItemRarity.values()) {
                    chances.put(rarity, getDefaultChance(cat, rarity));
                }
            }
            normalizeIfInvalid(chances);
            categoryChances.put(cat, chances);
        }
    }

    private double getDefaultChance(ShopCategory cat, ItemRarity rarity) {
        return switch (cat) {
            case DAILY -> switch (rarity) {
                case COMMON -> 45.0;
                case UNCOMMON -> 30.0;
                case RARE -> 15.0;
                case EPIC -> 7.0;
                case LEGENDARY -> 2.5;
                case MYTHIC -> 0.5;
            };
            case WEEKLY -> switch (rarity) {
                case COMMON -> 25.0;
                case UNCOMMON -> 30.0;
                case RARE -> 25.0;
                case EPIC -> 12.0;
                case LEGENDARY -> 6.0;
                case MYTHIC -> 2.0;
            };
            case MONTHLY -> switch (rarity) {
                case COMMON -> 10.0;
                case UNCOMMON -> 20.0;
                case RARE -> 30.0;
                case EPIC -> 22.0;
                case LEGENDARY -> 13.0;
                case MYTHIC -> 5.0;
            };
        };
    }

    private void normalizeIfInvalid(Map<ItemRarity, Double> chances) {
        double sum = chances.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 100.0) > 0.01 && sum > 0) {
            for (Map.Entry<ItemRarity, Double> e : chances.entrySet()) {
                e.setValue(Math.round((e.getValue() / sum) * 1000.0) / 10.0);
            }
        }
    }

    public Map<ItemRarity, Double> getChances(ShopCategory category) {
        return categoryChances.computeIfAbsent(category, k -> new EnumMap<>(ItemRarity.class));
    }

    public double getChance(ShopCategory category, ItemRarity rarity) {
        return getChances(category).getOrDefault(rarity, 0.0);
    }

    public void setChance(ShopCategory category, ItemRarity rarity, double percentage) {
        getChances(category).put(rarity, percentage);
    }

    public boolean validateChances(ShopCategory category) {
        double sum = getChances(category).values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.abs(sum - 100.0) < 0.1;
    }

    public void saveChances() {
        for (ShopCategory cat : ShopCategory.values()) {
            Map<ItemRarity, Double> chances = getChances(cat);
            for (Map.Entry<ItemRarity, Double> e : chances.entrySet()) {
                plugin.getConfig().set("shop.rarity-chances." + cat.name().toLowerCase() + "." + e.getKey().name().toLowerCase(), e.getValue());
            }
        }
        plugin.saveConfig();
    }

    public ItemRarity rollRarity(ShopCategory category) {
        Map<ItemRarity, Double> chances = getChances(category);
        double roll = random.nextDouble() * 100.0;
        double current = 0.0;
        for (Map.Entry<ItemRarity, Double> e : chances.entrySet()) {
            current += e.getValue();
            if (roll <= current) {
                return e.getKey();
            }
        }
        return ItemRarity.COMMON;
    }
}
