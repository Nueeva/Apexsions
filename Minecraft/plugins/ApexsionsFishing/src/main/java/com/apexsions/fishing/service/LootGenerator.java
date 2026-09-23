package com.apexsions.fishing.service;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.CatchType;
import com.apexsions.fishing.model.FishRarity;
import com.apexsions.fishing.model.FishingLootItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootGenerator {

    private final ApexsionsFishing plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public final NamespacedKey keyFishId;
    public final NamespacedKey keyFishWeight;
    public final NamespacedKey keyFishRarity;
    public final NamespacedKey keyFishPrice;
    public final NamespacedKey keyCatcherName;
    public final NamespacedKey keyCatchTime;
    public final NamespacedKey keyCatchType;

    private final List<FishingLootItem> fishTable = new ArrayList<>();
    private final List<FishingLootItem> junkTable = new ArrayList<>();
    private final List<FishingLootItem> treasureTable = new ArrayList<>();
    private final Map<String, FishingLootItem> allItemsById = new LinkedHashMap<>();

    private double cfgWeightFish = 55.0;
    private double cfgWeightJunk = 30.0;
    private double cfgWeightTreasure = 15.0;

    public LootGenerator(ApexsionsFishing plugin) {
        this.plugin = plugin;
        this.keyFishId = new NamespacedKey(plugin, "fish_id");
        this.keyFishWeight = new NamespacedKey(plugin, "fish_weight");
        this.keyFishRarity = new NamespacedKey(plugin, "fish_rarity");
        this.keyFishPrice = new NamespacedKey(plugin, "fish_price");
        this.keyCatcherName = new NamespacedKey(plugin, "catcher_name");
        this.keyCatchTime = new NamespacedKey(plugin, "catch_time");
        this.keyCatchType = new NamespacedKey(plugin, "catch_type");
        reload();
    }

    public void reload() {
        fishTable.clear();
        junkTable.clear();
        treasureTable.clear();
        allItemsById.clear();

        File lootFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!lootFile.exists()) {
            plugin.saveResource("loot.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(lootFile);

        // 1. Load Fishes
        ConfigurationSection fishSec = cfg.getConfigurationSection("loot.fishes");
        if (fishSec != null) {
            for (String key : fishSec.getKeys(false)) {
                String matStr = fishSec.getString(key + ".material", "COD");
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.COD;

                String name = fishSec.getString(key + ".display-name", "<white>" + key + "</white>");
                FishRarity rarity = FishRarity.fromString(fishSec.getString(key + ".rarity", "COMMON"));
                double weight = fishSec.getDouble(key + ".chance-weight", 10.0);
                double minW = fishSec.getDouble(key + ".min-weight", 1.0);
                double maxW = fishSec.getDouble(key + ".max-weight", 5.0);
                double price = fishSec.getDouble(key + ".base-price", 100.0);
                String desc = fishSec.getString(key + ".description", "");

                FishingLootItem item = new FishingLootItem(key, mat, name, rarity, CatchType.FISH, weight, minW, maxW, price, desc);
                fishTable.add(item);
                allItemsById.put(key.toLowerCase(), item);
            }
        }

        // 2. Load Junk
        ConfigurationSection junkSec = cfg.getConfigurationSection("loot.junk");
        if (junkSec != null) {
            for (String key : junkSec.getKeys(false)) {
                String matStr = junkSec.getString(key + ".material", "STRING");
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.STRING;

                String name = junkSec.getString(key + ".display-name", "<white>" + key + "</white>");
                FishRarity rarity = FishRarity.fromString(junkSec.getString(key + ".rarity", "COMMON"));
                double weight = junkSec.getDouble(key + ".chance-weight", 10.0);

                FishingLootItem item = new FishingLootItem(key, mat, name, rarity, CatchType.JUNK, weight, 0, 0, 0, "");
                junkTable.add(item);
                allItemsById.put(key.toLowerCase(), item);
            }
        }

        // 3. Load Treasure
        ConfigurationSection treasureSec = cfg.getConfigurationSection("loot.treasure");
        if (treasureSec != null) {
            for (String key : treasureSec.getKeys(false)) {
                String matStr = treasureSec.getString(key + ".material", "SADDLE");
                Material mat = Material.matchMaterial(matStr);
                if (mat == null) mat = Material.SADDLE;

                String name = treasureSec.getString(key + ".display-name", "<aqua>" + key + "</aqua>");
                FishRarity rarity = FishRarity.fromString(treasureSec.getString(key + ".rarity", "RARE"));
                double weight = treasureSec.getDouble(key + ".chance-weight", 5.0);

                FishingLootItem item = new FishingLootItem(key, mat, name, rarity, CatchType.TREASURE, weight, 0, 0, 0, "");
                treasureTable.add(item);
                allItemsById.put(key.toLowerCase(), item);
            }
        }

        // 4. Load Category Weights
        this.cfgWeightFish = cfg.getDouble("loot.category-weights.fish", 55.0);
        this.cfgWeightJunk = cfg.getDouble("loot.category-weights.junk", 30.0);
        this.cfgWeightTreasure = cfg.getDouble("loot.category-weights.treasure", 15.0);

        plugin.getLogger().info("LootGenerator dimuat: " + fishTable.size() + " spesies ikan, " + junkTable.size() + " sampah laut, " + treasureTable.size() + " harta karun. (Bobot Pool: Ikan=" + cfgWeightFish + ", Junk=" + cfgWeightJunk + ", Treasure=" + cfgWeightTreasure + ")");
    }

    public static class CatchResult {
        public final ItemStack item;
        public final FishingLootItem lootItem;
        public final double weightKg;
        public final boolean isSecret;
        public final boolean isFish;

        public CatchResult(ItemStack item, FishingLootItem lootItem, double weightKg, boolean isSecret, boolean isFish) {
            this.item = item;
            this.lootItem = lootItem;
            this.weightKg = weightKg;
            this.isSecret = isSecret;
            this.isFish = isFish;
        }
    }

    public CatchResult generateCatch(@NotNull Player player, @Nullable ItemStack rod) {
        double luckBonus = plugin.getRodManager().getLuckBonus(rod);
        double weightBonus = plugin.getRodManager().getWeightBonus(rod);

        // Calculate Category Pool based on configured weights
        double junkWeight = Math.max(5.0, cfgWeightJunk * (1.0 - Math.min(0.75, luckBonus)));
        double treasureWeight = cfgWeightTreasure * (1.0 + luckBonus * 1.5);
        double fishWeight = cfgWeightFish * (1.0 + luckBonus * 0.5);

        double totalCatWeight = junkWeight + fishWeight + treasureWeight;
        double roll = ThreadLocalRandom.current().nextDouble() * totalCatWeight;

        CatchType selectedType;
        if (roll < junkWeight && !junkTable.isEmpty()) {
            selectedType = CatchType.JUNK;
        } else if (roll < (junkWeight + treasureWeight) && !treasureTable.isEmpty()) {
            selectedType = CatchType.TREASURE;
        } else {
            selectedType = CatchType.FISH;
        }

        List<FishingLootItem> pool;
        if (selectedType == CatchType.JUNK) {
            pool = junkTable;
        } else if (selectedType == CatchType.TREASURE) {
            pool = treasureTable;
        } else {
            pool = fishTable;
        }

        if (pool.isEmpty()) {
            pool = fishTable;
            selectedType = CatchType.FISH;
        }

        // Weighted roll inside pool
        double poolTotal = 0.0;
        for (FishingLootItem item : pool) {
            double w = item.getChanceWeight();
            if (luckBonus > 0 && item.getRarity().getTierOrder() >= 3) {
                w *= (1.0 + luckBonus * 2.0); // Boost Rare/Epic/Legendary/Secret with luck!
            }
            poolTotal += w;
        }

        double itemRoll = ThreadLocalRandom.current().nextDouble() * poolTotal;
        FishingLootItem chosen = pool.get(0);
        double current = 0.0;
        for (FishingLootItem item : pool) {
            double w = item.getChanceWeight();
            if (luckBonus > 0 && item.getRarity().getTierOrder() >= 3) {
                w *= (1.0 + luckBonus * 2.0);
            }
            current += w;
            if (itemRoll <= current) {
                chosen = item;
                break;
            }
        }

        boolean isSecret = chosen.getRarity() == FishRarity.SECRET;
        boolean isFish = chosen.isFish();
        double finalWeight = 0.0;

        ItemStack stack = new ItemStack(chosen.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(chosen.getDisplayName()));
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(keyCatchType, PersistentDataType.STRING, chosen.getCatchType().name());
            pdc.set(keyFishRarity, PersistentDataType.STRING, chosen.getRarity().name());
            pdc.set(keyCatcherName, PersistentDataType.STRING, player.getName());
            pdc.set(keyCatchTime, PersistentDataType.LONG, System.currentTimeMillis());

            List<Component> lore = new ArrayList<>();

            if (isFish) {
                pdc.set(keyFishId, PersistentDataType.STRING, chosen.getId());

                // Gaussian random weight: mean is center of min/max, 95% inside range
                double minW = chosen.getMinWeight();
                double maxW = chosen.getMaxWeight();
                double mean = (minW + maxW) / 2.0;
                double stdDev = (maxW - minW) / 4.0;
                double gaussian = ThreadLocalRandom.current().nextGaussian();
                double generatedWeight = mean + (gaussian * stdDev);
                generatedWeight = Math.max(minW, Math.min(maxW * 1.5, generatedWeight)); // can slightly exceed max on critical high roll

                // Apply rod weight multiplier
                if (weightBonus > 0) {
                    generatedWeight *= (1.0 + weightBonus);
                }
                finalWeight = Math.round(generatedWeight * 100.0) / 100.0;
                pdc.set(keyFishWeight, PersistentDataType.DOUBLE, finalWeight);

                // Calculate price
                double weightRatio = Math.min(2.0, finalWeight / maxW);
                double price = chosen.getBasePrice() * chosen.getRarity().getPriceMultiplier() * (1.0 + weightRatio);
                long roundedPrice = Math.round(price);
                pdc.set(keyFishPrice, PersistentDataType.DOUBLE, (double) roundedPrice);

                // Rating Stars (1 to 5)
                int stars = 1 + (int) Math.min(4, Math.max(0, (finalWeight - minW) / (maxW - minW) * 4.0));
                String starStr = "★".repeat(stars) + "☆".repeat(5 - stars);

                lore.add(mm.deserialize("<dark_gray>" + chosen.getDescription() + "</dark_gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<yellow>✦ Tingkat:</yellow> ").append(chosen.getRarity().getFormattedComponent()));
                lore.add(mm.deserialize("<yellow>✦ Bobot:</yellow> <gold><bold>" + String.format("%.2f", finalWeight) + " kg</bold></gold> <dark_gray>(Max: " + String.format("%.1f", maxW) + " kg)</dark_gray>"));
                lore.add(mm.deserialize("<yellow>✦ Kualitas:</yellow> <green>" + starStr + "</green> <gray>(Grade " + (char)('A' + (5 - stars)) + ")</gray>"));
                lore.add(mm.deserialize("<yellow>✦ Pemancing:</yellow> <white>" + player.getName() + "</white>"));
                lore.add(mm.deserialize("<yellow>✦ Tanggal:</yellow> <dark_gray>" + dateFormat.format(new Date()) + "</dark_gray>"));
                lore.add(Component.empty());
                lore.add(mm.deserialize("<gold>Dapat dijual di Nelayan seharga <yellow><bold>Rp " + String.format("%,d", roundedPrice) + "</bold></yellow></gold>"));
            } else {
                // Junk or Treasure
                lore.add(mm.deserialize("<yellow>✦ Kategori:</yellow> <gray>" + (chosen.getCatchType() == CatchType.TREASURE ? "<aqua>Harta Karun Air</aqua>" : "<white>Sampah Perairan</white>") + "</gray>"));
                lore.add(mm.deserialize("<yellow>✦ Tingkat:</yellow> ").append(chosen.getRarity().getFormattedComponent()));
                lore.add(mm.deserialize("<yellow>✦ Pemancing:</yellow> <white>" + player.getName() + "</white>"));
                lore.add(mm.deserialize("<yellow>✦ Tanggal:</yellow> <dark_gray>" + dateFormat.format(new Date()) + "</dark_gray>"));
            }

            meta.lore(lore);
            stack.setItemMeta(meta);
        }

        return new CatchResult(stack, chosen, finalWeight, isSecret, isFish);
    }

    public List<FishingLootItem> getAllFishSpecies() {
        return Collections.unmodifiableList(fishTable);
    }

    public @Nullable FishingLootItem getFishById(String id) {
        if (id == null) return null;
        return allItemsById.get(id.toLowerCase());
    }

    public boolean isFishingItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        // Always allow fishing rods and raw fish/bait materials even without meta
        if (item.getType() == Material.FISHING_ROD || item.getType() == Material.COD
                || item.getType() == Material.SALMON || item.getType() == Material.TROPICAL_FISH
                || item.getType() == Material.PUFFERFISH) {
            return true;
        }
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(keyCatchType, PersistentDataType.STRING) || pdc.has(keyFishId, PersistentDataType.STRING);
    }

    public boolean isFish(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String catchType = pdc.get(keyCatchType, PersistentDataType.STRING);
        if ("FISH".equalsIgnoreCase(catchType)) return true;
        return pdc.has(keyFishId, PersistentDataType.STRING);
    }

    public double getFishPrice(@Nullable ItemStack item) {
        if (!isFish(item)) return 0.0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0.0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(keyFishPrice, PersistentDataType.DOUBLE, 0.0) * item.getAmount();
    }

    public double getFishWeight(@Nullable ItemStack item) {
        if (!isFish(item)) return 0.0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0.0;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(keyFishWeight, PersistentDataType.DOUBLE, 0.0);
    }

    public List<FishingLootItem> getFishTable() {
        return Collections.unmodifiableList(fishTable);
    }
}

