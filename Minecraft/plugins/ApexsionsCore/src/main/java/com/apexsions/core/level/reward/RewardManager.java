package com.apexsions.core.level.reward;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages level progression rewards (Levels 2–100), items, and milestone rewards.
 */
public class RewardManager {

    private final ApexsionsCorePlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<Integer, Reward> rewards = new ConcurrentHashMap<>();

    public RewardManager(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadRewards() {
        rewards.clear();
        FileConfiguration config = plugin.getConfigManager().getRewardsConfig();
        ConfigurationSection rewardsSec = config.getConfigurationSection("rewards");

        // Read default template
        String defIcon = config.getString("default-reward.icon", "GOLD_NUGGET");
        String defName = config.getString("default-reward.display-name", "<yellow>Level %level% Bounty</yellow>");
        List<String> defLore = config.getStringList("default-reward.lore");
        List<String> defCommands = config.getStringList("default-reward.commands");

        int minLvl = plugin.getConfigManager().getLevelMin();
        int maxLvl = plugin.getConfigManager().getLevelMax();

        for (int lvl = minLvl + 1; lvl <= maxLvl; lvl++) {
            boolean isMilestone = (lvl % 10 == 1 && lvl > 1) || (lvl == maxLvl);
            String path = String.valueOf(lvl);

            if (rewardsSec != null && rewardsSec.contains(path)) {
                ConfigurationSection sec = rewardsSec.getConfigurationSection(path);
                boolean ms = sec.getBoolean("is-milestone", isMilestone);
                String name = sec.getString("display-name", "<yellow>Level " + lvl + " Reward</yellow>");
                String icon = sec.getString("icon", ms ? "ENDER_CHEST" : "CHEST");
                List<String> lore = sec.getStringList("lore");
                List<String> rawCommands = sec.getStringList("commands");
                String broadcast = sec.getString("broadcast");
                String sound = sec.getString("sound", ms ? "UI_TOAST_CHALLENGE_COMPLETE" : "ENTITY_PLAYER_LEVELUP");

                List<ItemStack> items = new ArrayList<>();
                List<?> rawItems = sec.getList("items");
                if (rawItems != null) {
                    for (Object obj : rawItems) {
                        if (obj instanceof ItemStack is) {
                            items.add(is);
                        }
                    }
                }

                // If commands contain item gives, convert to physical item & don't execute via console command
                List<String> filteredCommands = new ArrayList<>();
                for (String cmd : rawCommands) {
                    if (isGiveCommand(cmd)) {
                        ItemStack parsed = parseGiveCommand(cmd);
                        if (parsed != null) {
                            items.add(parsed);
                        }
                    } else {
                        filteredCommands.add(cmd);
                    }
                }

                rewards.put(lvl, new Reward(lvl, ms, name, icon, lore, filteredCommands, items, broadcast, sound));
            } else {
                // Generate standard reward
                String name = defName.replace("%level%", String.valueOf(lvl));
                List<String> lore = new ArrayList<>();
                for (String l : defLore) {
                    lore.add(l.replace("%level%", String.valueOf(lvl)));
                }
                List<String> cmds = new ArrayList<>();
                List<ItemStack> defaultItems = new ArrayList<>();
                for (String c : defCommands) {
                    String resolved = c.replace("%level%", String.valueOf(lvl));
                    if (isGiveCommand(resolved)) {
                        ItemStack parsed = parseGiveCommand(resolved);
                        if (parsed != null) defaultItems.add(parsed);
                    } else {
                        cmds.add(resolved);
                    }
                }

                rewards.put(lvl, new Reward(lvl, isMilestone, name, isMilestone ? "ENDER_CHEST" : "CHEST", lore, cmds, defaultItems, null, isMilestone ? "UI_TOAST_CHALLENGE_COMPLETE" : "ENTITY_PLAYER_LEVELUP"));
            }
        }

        plugin.getLogger().info("Loaded " + rewards.size() + " progression level rewards.");
    }

    public Optional<Reward> getReward(int level) {
        return Optional.ofNullable(rewards.get(level));
    }

    public Map<Integer, Reward> getAllRewards() {
        return Collections.unmodifiableMap(rewards);
    }

    public int getUnclaimedCount(PlayerData data) {
        if (data == null) return 0;
        int count = 0;
        int max = data.getLevel();
        for (int lvl = 2; lvl <= max; lvl++) {
            if (!data.isRewardClaimed(lvl) && rewards.containsKey(lvl)) {
                count++;
            }
        }
        return count;
    }

    public boolean claimReward(Player player, int level) {
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty()) return false;

        PlayerData data = dataOpt.get();
        if (data.getLevel() < level) {
            player.sendMessage(miniMessage.deserialize("<red>Kamu harus mencapai Level " + level + " untuk mengambil hadiah ini!</red>"));
            return false;
        }

        if (data.isRewardClaimed(level)) {
            player.sendMessage(miniMessage.deserialize("<red>Kamu sudah pernah mengklaim hadiah untuk Level " + level + ".</red>"));
            return false;
        }

        Reward reward = rewards.get(level);
        if (reward == null) return false;

        // Mark claimed
        data.setRewardClaimed(level);
        plugin.getPlayerDataService().save(data);

        // Resolve kingdom placeholder
        String kingdomName = "None";
        if (data.getRegionId() != null) {
            Optional<Region> reg = plugin.getRegionManager().getRegion(data.getRegionId());
            if (reg.isPresent()) kingdomName = reg.get().getDisplayName();
        }

        // Give physical items
        for (ItemStack item : reward.getItems()) {
            if (item != null && !item.getType().isAir()) {
                Map<Integer, ItemStack> overflow = player.getInventory().addItem(item.clone());
                for (ItemStack drop : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        // Execute console commands (excluding item give commands)
        for (String cmd : reward.getCommands()) {
            if (isGiveCommand(cmd)) continue;
            String parsedCmd = cmd.replace("%player%", player.getName())
                    .replace("%level%", String.valueOf(level))
                    .replace("%kingdom%", kingdomName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
        }

        // Sound
        try {
            Sound sound = Sound.valueOf(reward.getSound());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception ignored) {}

        // Broadcast if milestone
        if (reward.getBroadcast() != null && !reward.getBroadcast().isEmpty()) {
            String bcast = reward.getBroadcast().replace("%player%", player.getName())
                    .replace("%level%", String.valueOf(level))
                    .replace("%kingdom%", kingdomName);
            Bukkit.broadcast(miniMessage.deserialize(bcast));
        }

        player.sendMessage(miniMessage.deserialize("<green>✔ Berhasil mengklaim hadiah untuk <gold>Level " + level + "</gold>!</green>"));
        return true;
    }

    public int claimAllAvailable(Player player) {
        Optional<PlayerData> dataOpt = plugin.getPlayerDataService().getCached(player.getUniqueId());
        if (dataOpt.isEmpty()) return 0;

        PlayerData data = dataOpt.get();
        int claimedCount = 0;
        int max = data.getLevel();

        for (int lvl = 2; lvl <= max; lvl++) {
            if (!data.isRewardClaimed(lvl) && rewards.containsKey(lvl)) {
                if (claimReward(player, lvl)) {
                    claimedCount++;
                }
            }
        }
        return claimedCount;
    }

    public synchronized void saveRewardItems(int level, List<ItemStack> items) {
        File file = new File(plugin.getDataFolder(), "progression/rewards.yml");
        if (!file.exists()) {
            file = new File(plugin.getDataFolder(), "rewards.yml");
        }
        FileConfiguration config = plugin.getConfigManager().getRewardsConfig();

        config.set("rewards." + level + ".items", items);
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save reward items for level " + level + ": " + e.getMessage());
        }
        loadRewards();
    }

    public synchronized void addItemToReward(int level, ItemStack item) {
        Reward current = rewards.get(level);
        List<ItemStack> list = new ArrayList<>();
        if (current != null) {
            list.addAll(current.getItems());
        }
        list.add(item.clone());
        saveRewardItems(level, list);
    }

    public synchronized void removeItemFromReward(int level, int index) {
        Reward current = rewards.get(level);
        if (current == null) return;
        List<ItemStack> list = new ArrayList<>(current.getItems());
        if (index >= 0 && index < list.size()) {
            list.remove(index);
            saveRewardItems(level, list);
        }
    }

    private boolean isGiveCommand(String cmd) {
        if (cmd == null) return false;
        String lower = cmd.trim().toLowerCase();
        return lower.startsWith("give %player% ") || lower.startsWith("minecraft:give %player% ")
                || lower.startsWith("give ") || lower.startsWith("minecraft:give ");
    }

    private ItemStack parseGiveCommand(String cmd) {
        if (cmd == null) return null;
        String[] parts = cmd.trim().split("\\s+");
        // format: give %player% <material> [amount] or minecraft:give %player% <material> [amount]
        int matIndex = -1;
        int amtIndex = -1;
        if (parts.length >= 3 && (parts[0].equalsIgnoreCase("give") || parts[0].equalsIgnoreCase("minecraft:give"))) {
            if (parts[1].equalsIgnoreCase("%player%") || parts[1].startsWith("@")) {
                matIndex = 2;
                amtIndex = 3;
            } else {
                matIndex = 1;
                amtIndex = 2;
            }
        }
        if (matIndex != -1 && matIndex < parts.length) {
            String matName = parts[matIndex].replace("minecraft:", "").toUpperCase();
            int amount = 1;
            if (amtIndex != -1 && amtIndex < parts.length) {
                try {
                    amount = Integer.parseInt(parts[amtIndex]);
                } catch (NumberFormatException ignored) {}
            }
            Material mat = Material.matchMaterial(matName);
            if (mat != null && !mat.isAir()) {
                return new ItemStack(mat, Math.max(1, amount));
            }
        }
        return null;
    }
}
