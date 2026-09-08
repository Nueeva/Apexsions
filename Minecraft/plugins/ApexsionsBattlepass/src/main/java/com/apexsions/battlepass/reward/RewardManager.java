package com.apexsions.battlepass.reward;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.api.event.BattlePassRewardClaimEvent;
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
import java.util.logging.Level;

public class RewardManager {

    private final ApexsionsBattlepass plugin;
    private final Map<Integer, Integer> levelRequiredXp = new HashMap<>();
    private final Map<Integer, Map<String, List<RewardItem>>> levelRewards = new HashMap<>();
    private final Set<String> specialPreviewLevels = new HashSet<>();
    private int maxLevel;
    private int defaultRequiredXp;

    public RewardManager(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        loadRewards();
    }

    public void loadRewards() {
        levelRequiredXp.clear();
        levelRewards.clear();
        specialPreviewLevels.clear();

        this.maxLevel = plugin.getConfig().getInt("battlepass.max-level", 100);
        this.defaultRequiredXp = plugin.getConfig().getInt("battlepass.default-required-xp", 1000);

        File targetFile = new File(plugin.getDataFolder(), "rewards/rewards.yml");
        File legacyFile = new File(plugin.getDataFolder(), "rewards.yml");
        File file;
        if (!targetFile.exists() && legacyFile.exists()) {
            file = legacyFile;
        } else {
            file = targetFile;
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try {
                    plugin.saveResource("rewards/rewards.yml", false);
                } catch (Exception e) {
                    try {
                        plugin.saveResource("rewards.yml", false);
                    } catch (Exception ignored) {}
                }
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection levelsSec = config.getConfigurationSection("levels");

        if (levelsSec != null) {
            for (String lvlKey : levelsSec.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(lvlKey);
                    int xp = levelsSec.getInt(lvlKey + ".required-xp", defaultRequiredXp);
                    levelRequiredXp.put(lvl, xp);

                    ConfigurationSection rewardsSec = levelsSec.getConfigurationSection(lvlKey + ".rewards");
                    if (rewardsSec != null) {
                        Map<String, List<RewardItem>> passMap = new HashMap<>();

                        for (String passKey : rewardsSec.getKeys(false)) {
                            List<Map<?, ?>> list = rewardsSec.getMapList(passKey);
                            List<RewardItem> items = new ArrayList<>();

                            for (Map<?, ?> map : list) {
                                Object typeObj = map.get("type");
                                String typeStr = typeObj != null ? typeObj.toString().toUpperCase() : "ITEM";
                                RewardType type;
                                try {
                                    type = RewardType.valueOf(typeStr);
                                } catch (Exception e) {
                                    type = RewardType.ITEM;
                                }

                                Object matObj = map.get("material");
                                String matStr = matObj != null ? matObj.toString() : null;
                                Material mat = matStr != null ? Material.matchMaterial(matStr) : null;

                                int amount = 1;
                                if (map.containsKey("amount")) {
                                    try {
                                        amount = Integer.parseInt(String.valueOf(map.get("amount")));
                                    } catch (NumberFormatException ignored) {}
                                }

                                String name = map.containsKey("name") ? String.valueOf(map.get("name")) : null;

                                List<String> commands = new ArrayList<>();
                                if (map.containsKey("commands")) {
                                    Object cmds = map.get("commands");
                                    if (cmds instanceof List<?>) {
                                        for (Object o : (List<?>) cmds) {
                                            commands.add(String.valueOf(o));
                                        }
                                    }
                                }

                                String perm = map.containsKey("permission") ? String.valueOf(map.get("permission")) : null;
                                String itemData = map.containsKey("item-data") ? String.valueOf(map.get("item-data")) : null;
                                String currencyId = map.containsKey("currency-id") ? String.valueOf(map.get("currency-id")) : null;

                                // Normalize MONEY / CURRENCY defaults
                                if (type == RewardType.MONEY || (name != null && name.toLowerCase().contains("rp"))) {
                                    if (currencyId == null || currencyId.isBlank() || currencyId.equalsIgnoreCase("battle_coins")) {
                                        currencyId = "rupiah";
                                    }
                                    if (mat == null) mat = Material.GOLD_INGOT;
                                } else if (type == RewardType.CURRENCY) {
                                    if (currencyId == null || currencyId.isBlank()) {
                                        currencyId = "battle_coins";
                                    }
                                    if (mat == null) {
                                        mat = "diamond".equalsIgnoreCase(currencyId) ? Material.DIAMOND : Material.SUNFLOWER;
                                    }
                                } else {
                                    if (mat == null) mat = Material.CHEST;
                                }

                                boolean specialPreview = (map.containsKey("special-preview") && Boolean.parseBoolean(String.valueOf(map.get("special-preview"))))
                                        || (map.containsKey("previewable") && Boolean.parseBoolean(String.valueOf(map.get("previewable"))));
                                String normKey = com.apexsions.battlepass.pass.PassManager.normalizePassId(passKey);
                                if (specialPreview) {
                                    specialPreviewLevels.add(lvl + ":" + normKey);
                                }

                                items.add(new RewardItem(type, mat, amount, name, commands, perm, itemData, currencyId, specialPreview));
                            }
                            String normPass = com.apexsions.battlepass.pass.PassManager.normalizePassId(passKey);
                            passMap.put(normPass, items);
                        }
                        levelRewards.put(lvl, passMap);
                    }

                    if (levelsSec.contains(lvlKey + ".special-preview")) {
                        ConfigurationSection spSec = levelsSec.getConfigurationSection(lvlKey + ".special-preview");
                        if (spSec != null) {
                            for (String pKey : spSec.getKeys(false)) {
                                if (spSec.getBoolean(pKey)) {
                                    specialPreviewLevels.add(lvl + ":" + com.apexsions.battlepass.pass.PassManager.normalizePassId(pKey));
                                }
                            }
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public void saveRewards() {
        File file1 = new File(plugin.getDataFolder(), "rewards/rewards.yml");
        File file2 = new File(plugin.getDataFolder(), "rewards.yml");
        FileConfiguration config = new YamlConfiguration();

        for (int lvl = 1; lvl <= maxLevel; lvl++) {
            String path = "levels." + lvl;
            config.set(path + ".required-xp", getRequiredXp(lvl));

            Map<String, List<RewardItem>> passMap = levelRewards.get(lvl);
            if (passMap != null) {
                for (var entry : passMap.entrySet()) {
                    String passKey = entry.getKey();
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (RewardItem ri : entry.getValue()) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("type", ri.getType().name());
                        map.put("material", ri.getMaterial().name());
                        map.put("amount", ri.getAmount());
                        if (ri.getName() != null) map.put("name", ri.getName());
                        if (!ri.getCommands().isEmpty()) map.put("commands", ri.getCommands());
                        if (ri.getPermission() != null) map.put("permission", ri.getPermission());
                        if (ri.getItemData() != null) map.put("item-data", ri.getItemData());
                        if (ri.getCurrencyId() != null) map.put("currency-id", ri.getCurrencyId());
                        map.put("special-preview", ri.isSpecialPreview());
                        list.add(map);
                    }
                    config.set(path + ".rewards." + passKey, list);
                }
            }
        }

        for (String spKey : specialPreviewLevels) {
            String[] parts = spKey.split(":");
            if (parts.length == 2) {
                config.set("levels." + parts[0] + ".special-preview." + parts[1], true);
            }
        }

        try {
            if (file1.getParentFile() != null && !file1.getParentFile().exists()) {
                file1.getParentFile().mkdirs();
            }
            config.save(file1);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save rewards/rewards.yml", e);
        }

        try {
            if (file2.exists() || !file1.exists()) {
                config.save(file2);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save rewards.yml", e);
        }
    }

    public int getRequiredXp(int level) {
        return levelRequiredXp.getOrDefault(level, defaultRequiredXp);
    }

    public void setRequiredXp(int level, int xp) {
        levelRequiredXp.put(level, xp);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public List<RewardItem> getRewards(int level, String passId) {
        Map<String, List<RewardItem>> map = levelRewards.get(level);
        if (map == null) return List.of();
        if (passId == null) return List.of();

        String norm = com.apexsions.battlepass.pass.PassManager.normalizePassId(passId);

        // 1. Direct match with normalized passId (respects empty list if admin cleared rewards)
        if (map.containsKey(norm)) {
            return map.get(norm);
        }

        // 2. Direct match with original passId
        String lower = passId.toLowerCase();
        if (map.containsKey(lower)) {
            return map.get(lower);
        }

        // 3. Fallbacks for legacy mappings ONLY if canonical key is not present
        if (norm.equals("citizen")) {
            if (map.containsKey("free")) return map.get("free");
        } else if (norm.equals("sio")) {
            if (map.containsKey("premium")) return map.get("premium");
            if (map.containsKey("premium-plus")) return map.get("premium-plus");
        } else if (norm.equals("exsio")) {
            if (map.containsKey("ultimate")) return map.get("ultimate");
            if (map.containsKey("vip")) return map.get("vip");
            if (map.containsKey("premium-plus")) return map.get("premium-plus");
        }

        return List.of();
    }

    public boolean isSpecialPreview(int level, String passId) {
        if (level % 50 == 0) return true;
        if (passId == null) return false;
        String norm = com.apexsions.battlepass.pass.PassManager.normalizePassId(passId);
        if (specialPreviewLevels.contains(level + ":" + norm)) {
            return true;
        }
        return getRewards(level, passId).stream().anyMatch(RewardItem::isSpecialPreview);
    }

    public void setSpecialPreview(int level, String passId, boolean special) {
        if (passId == null) return;
        String norm = com.apexsions.battlepass.pass.PassManager.normalizePassId(passId);
        String key = level + ":" + norm;
        if (special) {
            specialPreviewLevels.add(key);
        } else {
            specialPreviewLevels.remove(key);
        }
        List<RewardItem> current = getRewards(level, passId);
        if (!current.isEmpty()) {
            List<RewardItem> updated = new ArrayList<>();
            for (RewardItem ri : current) {
                updated.add(ri.withSpecialPreview(special));
            }
            setRewards(level, passId, updated);
        } else {
            saveRewards();
        }
    }

    public void setRewards(int level, String passId, List<RewardItem> rewards) {
        String norm = com.apexsions.battlepass.pass.PassManager.normalizePassId(passId);
        Map<String, List<RewardItem>> map = levelRewards.computeIfAbsent(level, k -> new HashMap<>());
        map.put(norm, new ArrayList<>(rewards));

        // Purge legacy alias keys from memory so they never resurrect
        if (norm.equals("citizen")) {
            map.remove("free");
        } else if (norm.equals("sio")) {
            map.remove("premium");
            map.remove("premium-plus");
        } else if (norm.equals("exsio")) {
            map.remove("ultimate");
            map.remove("vip");
            map.remove("premium-plus");
        }
        saveRewards();
    }

    public void addReward(int level, String passId, RewardItem reward) {
        boolean special = isSpecialPreview(level, passId);
        if (special && !reward.isSpecialPreview()) {
            reward = reward.withSpecialPreview(true);
        }
        List<RewardItem> current = new ArrayList<>(getRewards(level, passId));
        current.add(reward);
        setRewards(level, passId, current);
    }

    public void updateReward(int level, String passId, int index, RewardItem reward) {
        List<RewardItem> current = new ArrayList<>(getRewards(level, passId));
        if (index >= 0 && index < current.size()) {
            current.set(index, reward);
            setRewards(level, passId, current);
        }
    }

    public void removeReward(int level, String passId, int index) {
        List<RewardItem> current = new ArrayList<>(getRewards(level, passId));
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            setRewards(level, passId, current);
        }
    }

    public boolean claimReward(Player player, int level, String passId) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return false;

        passId = passId.toLowerCase();

        // 1. Validate Level
        if (data.getLevel() < level) {
            player.sendMessage(plugin.getMessage("reward-level-not-reached").replace("%level%", String.valueOf(level)));
            return false;
        }

        // 2. Validate Pass Ownership (supports pass hierarchy inheritance)
        if (!plugin.getPassManager().canAccessRewardTier(data.getPasses(), passId)) {
            player.sendMessage(plugin.getMessage("reward-pass-locked").replace("%pass%", passId.toUpperCase()));
            return false;
        }

        // 3. Validate Claim Status
        if (data.isRewardClaimed(level, passId)) {
            player.sendMessage(plugin.getMessage("reward-already-claimed").replace("%level%", String.valueOf(level)).replace("%pass%", passId.toUpperCase()));
            return false;
        }

        List<RewardItem> rewards = getRewards(level, passId);
        if (rewards.isEmpty()) {
            data.setRewardClaimed(level, passId);
            return true;
        }

        // 4. Distribute Rewards
        for (RewardItem item : rewards) {
            giveReward(player, data, item);
        }

        data.setRewardClaimed(level, passId);

        // 5. Trigger Event
        BattlePassRewardClaimEvent event = new BattlePassRewardClaimEvent(player, level, passId, rewards);
        Bukkit.getPluginManager().callEvent(event);

        player.sendMessage(plugin.getMessage("reward-claimed")
                .replace("%level%", String.valueOf(level))
                .replace("%pass%", passId.toUpperCase()));

        return true;
    }

    private void giveReward(Player player, PlayerData data, RewardItem reward) {
        switch (reward.getType()) {
            case ITEM -> {
                ItemStack is = reward.toItemStack();
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(is);
                if (!overflow.isEmpty()) {
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
            case COMMAND -> {
                for (String cmd : reward.getCommands()) {
                    String formatted = cmd.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
                }
            }
            case MONEY -> {
                try {
                    com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", reward.getAmount());
                } catch (Throwable t) {
                    if (plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
                        plugin.getVaultHook().deposit(player, reward.getAmount());
                    }
                }
            }
            case CURRENCY -> {
                String cId = reward.getCurrencyId();
                if (cId == null || cId.equalsIgnoreCase("battle_coins") || cId.equalsIgnoreCase("battlecoins") || cId.equalsIgnoreCase("coins")) {
                    plugin.getCurrencyService().addCurrency(player.getUniqueId(), reward.getAmount());
                } else if (cId.equalsIgnoreCase("rupiah")) {
                    try {
                        com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", reward.getAmount());
                    } catch (Throwable t) {
                        if (plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
                            plugin.getVaultHook().deposit(player, reward.getAmount());
                        }
                    }
                } else {
                    // Integration with ApexsionsEconomy if present
                    try {
                        com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), cId, reward.getAmount());
                    } catch (Throwable t) {
                        // Fallback to battle coins if economy not hooked
                        plugin.getCurrencyService().addCurrency(player.getUniqueId(), reward.getAmount());
                    }
                }
            }
            case PERMISSION -> {
                if (reward.getPermission() != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set " + reward.getPermission());
                }
            }
        }
    }
}

