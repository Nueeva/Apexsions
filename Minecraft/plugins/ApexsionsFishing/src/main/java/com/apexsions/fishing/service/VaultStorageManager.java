package com.apexsions.fishing.service;

import com.apexsions.fishing.ApexsionsFishing;
import com.apexsions.fishing.model.PlayerFishingStats;
import com.apexsions.fishing.model.PlayerVaultData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VaultStorageManager {

    private final ApexsionsFishing plugin;
    private final File dataFolder;

    private final Map<UUID, PlayerVaultData> vaultCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerFishingStats> statsCache = new ConcurrentHashMap<>();

    public VaultStorageManager(ApexsionsFishing plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public @NotNull PlayerVaultData getVaultData(@NotNull UUID uuid) {
        return vaultCache.computeIfAbsent(uuid, this::loadVaultFromDisk);
    }

    public @NotNull PlayerFishingStats getStats(@NotNull UUID uuid) {
        return statsCache.computeIfAbsent(uuid, this::loadStatsFromDisk);
    }

    private PlayerVaultData loadVaultFromDisk(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        int defaultPages = plugin.getConfig().getInt("settings.vault.default-unlocked-pages", 1);
        PlayerVaultData data = new PlayerVaultData(uuid, defaultPages);

        if (!file.exists()) {
            return data;
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int unlocked = cfg.getInt("vault.unlocked-pages", defaultPages);
        data.setUnlockedPages(unlocked);

        ConfigurationSection pagesSec = cfg.getConfigurationSection("vault.pages");
        if (pagesSec != null) {
            for (String pKey : pagesSec.getKeys(false)) {
                try {
                    int pNum = Integer.parseInt(pKey);
                    ItemStack[] items = new ItemStack[PlayerVaultData.SLOTS_PER_PAGE];

                    // Priority 1: ConfigurationSection by slot index (e.g. 0: ItemStack, 5: ItemStack)
                    if (pagesSec.isConfigurationSection(pKey)) {
                        ConfigurationSection pageSec = pagesSec.getConfigurationSection(pKey);
                        if (pageSec != null) {
                            for (String sKey : pageSec.getKeys(false)) {
                                try {
                                    int slot = Integer.parseInt(sKey);
                                    if (slot >= 0 && slot < PlayerVaultData.SLOTS_PER_PAGE) {
                                        items[slot] = pageSec.getItemStack(sKey);
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    } else if (pagesSec.isList(pKey)) {
                        // Priority 2: Fallback to list
                        List<?> list = pagesSec.getList(pKey);
                        if (list != null) {
                            for (int i = 0; i < Math.min(list.size(), PlayerVaultData.SLOTS_PER_PAGE); i++) {
                                Object obj = list.get(i);
                                if (obj instanceof ItemStack is) {
                                    items[i] = is;
                                }
                            }
                        }
                    }
                    data.setPage(pNum, items);
                } catch (NumberFormatException ignored) {}
            }
        }
        return data;
    }

    private PlayerFishingStats loadStatsFromDisk(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        PlayerFishingStats stats = new PlayerFishingStats(uuid);

        if (!file.exists()) {
            return stats;
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        stats.setTotalFishCaught(cfg.getLong("stats.total-caught", 0));
        stats.setTotalWeightCaught(cfg.getDouble("stats.total-weight", 0.0));
        stats.setHeaviestFishWeight(cfg.getDouble("stats.heaviest-weight", 0.0));
        stats.setHeaviestFishName(cfg.getString("stats.heaviest-name", "-"));
        stats.setSecretCatches(cfg.getInt("stats.secret-catches", 0));

        ConfigurationSection pbSec = cfg.getConfigurationSection("stats.personal-bests");
        if (pbSec != null) {
            for (String spKey : pbSec.getKeys(false)) {
                double pb = pbSec.getDouble(spKey, 0.0);
                stats.getPersonalBestPerSpecies().put(spKey.toLowerCase(), pb);
            }
        }
        return stats;
    }

    public void savePlayerData(@NotNull UUID uuid) {
        PlayerVaultData vault = vaultCache.get(uuid);
        PlayerFishingStats stats = statsCache.get(uuid);
        if (vault == null && stats == null) return;

        File file = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration cfg = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

        if (vault != null) {
            cfg.set("vault.unlocked-pages", vault.getUnlockedPages());
            for (Map.Entry<Integer, ItemStack[]> entry : vault.getAllPages().entrySet()) {
                int pageNum = entry.getKey();
                ItemStack[] items = entry.getValue();
                String pagePath = "vault.pages." + pageNum;
                cfg.set(pagePath, null); // Clear old data for page

                for (int slot = 0; slot < items.length; slot++) {
                    ItemStack is = items[slot];
                    if (is != null && !is.getType().isAir()) {
                        cfg.set(pagePath + "." + slot, is);
                    }
                }
            }
        }

        if (stats != null) {
            cfg.set("stats.total-caught", stats.getTotalFishCaught());
            cfg.set("stats.total-weight", stats.getTotalWeightCaught());
            cfg.set("stats.heaviest-weight", stats.getHeaviestFishWeight());
            cfg.set("stats.heaviest-name", stats.getHeaviestFishName());
            cfg.set("stats.secret-catches", stats.getSecretCatches());
            for (Map.Entry<String, Double> pb : stats.getPersonalBestPerSpecies().entrySet()) {
                cfg.set("stats.personal-bests." + pb.getKey(), pb.getValue());
            }
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan data memancing untuk UUID " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID uuid : vaultCache.keySet()) {
            savePlayerData(uuid);
        }
    }

    public boolean depositToVault(Player player, ItemStack item) {
        PlayerVaultData data = getVaultData(player.getUniqueId());
        int unlocked = data.getUnlockedPages();

        for (int p = 1; p <= unlocked; p++) {
            ItemStack[] items = data.getPage(p);
            for (int slot = 0; slot < items.length; slot++) {
                if (items[slot] == null || items[slot].getType().isAir()) {
                    items[slot] = item.clone();
                    data.setPage(p, items);
                    return true;
                }
            }
        }
        return false;
    }

    // Leaderboard queries
    public List<PlayerFishingStats> getTopHeaviestCatch(int limit) {
        ensureAllDataLoaded();
        List<PlayerFishingStats> list = new ArrayList<>(statsCache.values());
        list.removeIf(stats -> isLeaderboardExempt(stats.getPlayerUuid()));
        list.sort((a, b) -> Double.compare(b.getHeaviestFishWeight(), a.getHeaviestFishWeight()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<PlayerFishingStats> getTopTotalWeight(int limit) {
        ensureAllDataLoaded();
        List<PlayerFishingStats> list = new ArrayList<>(statsCache.values());
        list.removeIf(stats -> isLeaderboardExempt(stats.getPlayerUuid()));
        list.sort((a, b) -> Double.compare(b.getTotalWeightCaught(), a.getTotalWeightCaught()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<PlayerFishingStats> getTopTotalFish(int limit) {
        ensureAllDataLoaded();
        List<PlayerFishingStats> list = new ArrayList<>(statsCache.values());
        list.removeIf(stats -> isLeaderboardExempt(stats.getPlayerUuid()));
        list.sort((a, b) -> Long.compare(b.getTotalFishCaught(), a.getTotalFishCaught()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    private void ensureAllDataLoaded() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName().replace(".yml", "");
                try {
                    UUID uuid = UUID.fromString(name);
                    getStats(uuid);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    /**
     * Checks if a player is server staff, admin, OP, or exempt from public leaderboards.
     */
    public boolean isLeaderboardExempt(UUID uuid) {
        if (uuid == null) return false;

        // 1. Authoritative check via ApexsionsCore API
        if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsCore")) {
            try {
                if (com.apexsions.core.api.ApexsionsCoreProvider.isAvailable()) {
                    return com.apexsions.core.api.ApexsionsCoreProvider.get().isLeaderboardExempt(uuid);
                }
            } catch (Throwable ignored) {}
        }

        // 2. Bukkit OP
        org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (op != null && op.isOp()) {
            return true;
        }

        // 3. Operators list check (covers offline OPs in ops.json)
        try {
            for (org.bukkit.OfflinePlayer operator : Bukkit.getOperators()) {
                if (uuid.equals(operator.getUniqueId())) return true;
                if (op != null && op.getName() != null && op.getName().equalsIgnoreCase(operator.getName())) return true;
            }
        } catch (Throwable ignored) {}

        // 4. Online player permissions
        org.bukkit.entity.Player onlineP = Bukkit.getPlayer(uuid);
        if (onlineP != null && (onlineP.isOp() 
                || onlineP.hasPermission("apexsions.admin") 
                || onlineP.hasPermission("apexsions.staff") 
                || onlineP.hasPermission("apexsionscore.admin")
                || onlineP.hasPermission("apexsions.conclave")
                || onlineP.hasPermission("apexsions.leaderboard.exempt"))) {
            return true;
        }

        // 5. Name-based match for server founders and staff
        if (op != null && op.getName() != null) {
            String name = op.getName().toLowerCase(java.util.Locale.ROOT).replaceAll("^[.*_]+", "");
            if (name.contains("nueeva") || name.contains("nuevaid") || name.contains("rifqi") 
                    || name.contains("friell") || name.contains("favian") || name.contains("fanerf") 
                    || name.contains("kazrienvall")) {
                return true;
            }
        }

        return false;
    }
}
