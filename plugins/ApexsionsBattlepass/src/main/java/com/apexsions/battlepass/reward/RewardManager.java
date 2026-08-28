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
    private int maxLevel;
    private int defaultRequiredXp;

    public RewardManager(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        loadRewards();
    }

    public void loadRewards() {
        levelRequiredXp.clear();
        levelRewards.clear();

        this.maxLevel = plugin.getConfig().getInt("battlepass.max-level", 100);
        this.defaultRequiredXp = plugin.getConfig().getInt("battlepass.default-required-xp", 1000);

        File file = new File(plugin.getDataFolder(), "rewards.yml");
        if (!file.exists()) {
            plugin.saveResource("rewards.yml", false);
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
                                String matStr = matObj != null ? matObj.toString() : "DIAMOND";
                                Material mat = Material.matchMaterial(matStr);

                                int amount = 1;
                                if (map.containsKey("amount")) {
                                    amount = Integer.parseInt(String.valueOf(map.get("amount")));
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
                                String currencyId = map.containsKey("currency-id") ? String.valueOf(map.get("currency-id")) : "battle_coins";

                                items.add(new RewardItem(type, mat, amount, name, commands, perm, itemData, currencyId));
                            }
                            passMap.put(passKey.toLowerCase(), items);
                        }
                        levelRewards.put(lvl, passMap);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public void saveRewards() {
        File file = new File(plugin.getDataFolder(), "rewards.yml");
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
                        list.add(map);
                    }
                    config.set(path + ".rewards." + passKey, list);
                }
            }
        }

        try {
            config.save(file);
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
        return map.getOrDefault(passId.toLowerCase(), List.of());
    }

    public void setRewards(int level, String passId, List<RewardItem> rewards) {
        levelRewards.computeIfAbsent(level, k -> new HashMap<>()).put(passId.toLowerCase(), new ArrayList<>(rewards));
        saveRewards();
    }

    public void addReward(int level, String passId, RewardItem reward) {
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
                if (plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
                    plugin.getVaultHook().deposit(player, reward.getAmount());
                }
            }
            case CURRENCY -> {
                String cId = reward.getCurrencyId();
                if (cId == null || cId.equalsIgnoreCase("battle_coins") || cId.equalsIgnoreCase("battlecoins")) {
                    plugin.getCurrencyService().addCurrency(player.getUniqueId(), reward.getAmount());
                } else {
                    // Integration with ApexsionsEconomy if present
                    try {
                        com.apexsions.economy.api.ApexsionsEconomyAPI.deposit(player.getUniqueId(), cId, reward.getAmount());
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

