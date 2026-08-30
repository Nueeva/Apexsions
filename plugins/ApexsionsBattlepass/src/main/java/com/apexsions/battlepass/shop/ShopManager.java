package com.apexsions.battlepass.shop;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ShopManager {

    private final ApexsionsBattlepass plugin;
    private final Map<ShopCategory, Map<String, ShopItem>> shopItems = new EnumMap<>(ShopCategory.class);

    public ShopManager(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        loadShop();
    }

    public void loadShop() {
        shopItems.clear();
        for (ShopCategory cat : ShopCategory.values()) {
            shopItems.put(cat, new LinkedHashMap<>());
        }

        loadCategoryFile(ShopCategory.DAILY, "shop/daily.yml");
        loadCategoryFile(ShopCategory.WEEKLY, "shop/weekly.yml");
        loadCategoryFile(ShopCategory.MONTHLY, "shop/monthly.yml");
    }

    private void loadCategoryFile(ShopCategory category, String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection itemsSec = config.getConfigurationSection("items");
        if (itemsSec == null) return;

        Map<String, ShopItem> map = shopItems.get(category);

        for (String key : itemsSec.getKeys(false)) {
            String name = itemsSec.getString(key + ".display-name", key);
            String matStr = itemsSec.getString(key + ".material", "CHEST");
            Material mat = Material.matchMaterial(matStr);
            int amount = itemsSec.getInt(key + ".amount", 1);
            String currencyType = itemsSec.getString(key + ".currency-type", "BATTLE_COINS");
            double price = itemsSec.getDouble(key + ".price", 100);
            int limit = itemsSec.getInt(key + ".purchase-limit", 0);
            List<String> commands = itemsSec.getStringList(key + ".commands");
            List<String> lore = itemsSec.getStringList(key + ".lore");
            String rarityStr = itemsSec.getString(key + ".rarity", "COMMON");
            ItemRarity rarity = ItemRarity.fromString(rarityStr);
            String categoryTag = itemsSec.getString(key + ".category", "General");
            int weight = itemsSec.getInt(key + ".weight", 10);
            String itemData = itemsSec.getString(key + ".item-data", null);

            ShopItem item = new ShopItem(key, name, mat, amount, currencyType, price, limit, commands, lore, rarity, categoryTag, weight, itemData);
            map.put(key, item);
        }
    }

    public void saveCategory(ShopCategory category) {
        String fileName = switch (category) {
            case DAILY -> "shop/daily.yml";
            case WEEKLY -> "shop/weekly.yml";
            case MONTHLY -> "shop/monthly.yml";
        };

        File file = new File(plugin.getDataFolder(), fileName);
        FileConfiguration config = new YamlConfiguration();
        Map<String, ShopItem> items = getShopItems(category);

        for (ShopItem it : items.values()) {
            String path = "items." + it.getId();
            config.set(path + ".display-name", it.getDisplayName());
            config.set(path + ".material", it.getMaterial().name());
            config.set(path + ".amount", it.getAmount());
            config.set(path + ".currency-type", it.getCurrencyType());
            config.set(path + ".price", it.getPrice());
            config.set(path + ".purchase-limit", it.getPurchaseLimit());
            if (!it.getCommands().isEmpty()) config.set(path + ".commands", it.getCommands());
            if (!it.getLore().isEmpty()) config.set(path + ".lore", it.getLore());
            config.set(path + ".rarity", it.getRarity().name());
            config.set(path + ".category", it.getCategoryTag());
            config.set(path + ".weight", it.getWeight());
            if (it.getItemData() != null) config.set(path + ".item-data", it.getItemData());
        }

        try {
            config.save(file);
        } catch (Exception ignored) {}
    }

    public void addOrUpdateShopItem(ShopCategory category, ShopItem item) {
        if (item == null) return;
        shopItems.computeIfAbsent(category, k -> new LinkedHashMap<>()).put(item.getId(), item);
        saveCategory(category);
    }

    public void deleteShopItem(ShopCategory category, String itemId) {
        Map<String, ShopItem> map = shopItems.get(category);
        if (map != null) {
            map.remove(itemId);
            saveCategory(category);
        }
    }

    public Map<String, ShopItem> getShopItems(ShopCategory category) {
        return shopItems.getOrDefault(category, Collections.emptyMap());
    }

    public ShopItem getShopItem(ShopCategory category, String itemId) {
        return getShopItems(category).get(itemId);
    }

    public ShopItem findShopItem(String itemId) {
        for (ShopCategory cat : ShopCategory.values()) {
            ShopItem item = getShopItem(cat, itemId);
            if (item != null) return item;
        }
        return null;
    }

    public Collection<ShopItem> getDisplayItems(Player player, ShopCategory category) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data != null) {
            List<String> rotationIds = data.getRotation(category);
            if (rotationIds != null && !rotationIds.isEmpty()) {
                List<ShopItem> list = new ArrayList<>();
                Map<String, ShopItem> pool = getShopItems(category);
                for (String id : rotationIds) {
                    if (list.size() >= 10) break; // Cap at 10 items
                    ShopItem it = pool.get(id);
                    if (it != null) {
                        list.add(it);
                    }
                }
                if (!list.isEmpty()) {
                    return list;
                }
            }
        }
        List<ShopItem> all = new ArrayList<>(getShopItems(category).values());
        if (all.size() > 10) {
            return all.subList(0, 10);
        }
        return all;
    }

    public List<String> generateNewRotation(ShopCategory category) {
        Map<String, ShopItem> pool = getShopItems(category);
        if (pool.isEmpty()) return List.of();

        List<ShopItem> allItems = new ArrayList<>(pool.values());
        int targetSlots = Math.min(10, allItems.size());

        Collections.shuffle(allItems);
        List<String> chosenIds = new ArrayList<>();
        for (int i = 0; i < targetSlots && i < allItems.size(); i++) {
            chosenIds.add(allItems.get(i).getId());
        }
        return chosenIds;
    }

    public boolean purchaseItem(Player player, ShopCategory category, String itemId) {
        if (!plugin.getSeasonManager().isActive()) {
            player.sendMessage(plugin.getMessage("shop-locked-transition"));
            return false;
        }

        ShopItem item = getShopItem(category, itemId);
        if (item == null) {
            item = findShopItem(itemId);
        }
        if (item == null) return false;

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return false;

        // Check Purchase Limit
        int bought = data.getShopPurchaseCount(itemId);
        if (item.getPurchaseLimit() > 0 && bought >= item.getPurchaseLimit()) {
            player.sendMessage(plugin.getMessage("shop-limit-reached"));
            return false;
        }

        String currType = item.getCurrencyType().toUpperCase();

        // Check Balance & Deduct
        if (currType.equals("BATTLE_COINS") || currType.equals("BATTLECOINS")) {
            if (data.getCurrency() < (int) item.getPrice()) {
                player.sendMessage(plugin.getMessage("shop-insufficient-coins").replace("%currency%", "Battle Coins"));
                return false;
            }
            plugin.getCurrencyService().removeCurrency(player.getUniqueId(), (int) item.getPrice());
        } else {
            // ApexsionsEconomy Currency (Rupiah)
            try {
                double bal = com.apexsions.economy.api.ApexsionsEconomyProvider.get().getBalance(player.getUniqueId(), item.getCurrencyType());
                if (bal < item.getPrice()) {
                    player.sendMessage("§cSaldo Rupiah Anda tidak mencukupi! Butuh §eRp." + String.format("%,.0f", item.getPrice()));
                    return false;
                }
                com.apexsions.economy.api.ApexsionsEconomyProvider.get().withdraw(player.getUniqueId(), item.getCurrencyType(), item.getPrice());
            } catch (Throwable t) {
                // Fallback to battle coins if economy plugin is absent
                if (data.getCurrency() < (int) item.getPrice()) {
                    player.sendMessage(plugin.getMessage("shop-insufficient-coins").replace("%currency%", "Battle Coins"));
                    return false;
                }
                plugin.getCurrencyService().removeCurrency(player.getUniqueId(), (int) item.getPrice());
            }
        }

        // Give Rewards (Item or Commands)
        if (item.getCommands().isEmpty()) {
            ItemStack is = item.toItemStack();
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(is);
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        } else {
            for (String cmd : item.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        }

        data.incrementShopPurchaseCount(itemId);

        String currDisplay;
        if ("rupiah".equalsIgnoreCase(item.getCurrencyType())) {
            currDisplay = "Rp." + String.format("%,.0f", item.getPrice());
        } else {
            currDisplay = (int) item.getPrice() + " Battle Coins";
        }
        player.sendMessage(plugin.getMessage("shop-item-bought")
                .replace("%item%", item.getDisplayName())
                .replace("%price%", currDisplay));

        return true;
    }
}


