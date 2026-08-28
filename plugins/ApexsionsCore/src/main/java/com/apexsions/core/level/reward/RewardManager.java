package com.apexsions.core.level.reward;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages level progression rewards (Levels 2–100) and milestone rewards.
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
                String icon = sec.getString("icon", ms ? "ENDER_CHEST" : "GOLD_INGOT");
                List<String> lore = sec.getStringList("lore");
                List<String> commands = sec.getStringList("commands");
                String broadcast = sec.getString("broadcast");
                String sound = sec.getString("sound", ms ? "UI_TOAST_CHALLENGE_COMPLETE" : "ENTITY_PLAYER_LEVELUP");

                rewards.put(lvl, new Reward(lvl, ms, name, icon, lore, commands, broadcast, sound));
            } else {
                // Generate standard reward
                String name = defName.replace("%level%", String.valueOf(lvl));
                List<String> lore = new ArrayList<>();
                for (String l : defLore) {
                    lore.add(l.replace("%level%", String.valueOf(lvl)));
                }
                List<String> cmds = new ArrayList<>();
                for (String c : defCommands) {
                    cmds.add(c.replace("%level%", String.valueOf(lvl)));
                }

                rewards.put(lvl, new Reward(lvl, isMilestone, name, isMilestone ? "ENDER_CHEST" : defIcon, lore, cmds, null, isMilestone ? "UI_TOAST_CHALLENGE_COMPLETE" : "ENTITY_PLAYER_LEVELUP"));
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
            player.sendMessage(miniMessage.deserialize("<red>You must reach Level " + level + " to claim this reward!</red>"));
            return false;
        }

        if (data.isRewardClaimed(level)) {
            player.sendMessage(miniMessage.deserialize("<red>You have already claimed the reward for Level " + level + ".</red>"));
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

        // Execute console commands
        for (String cmd : reward.getCommands()) {
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

        player.sendMessage(miniMessage.deserialize("<green>✔ Successfully claimed reward for <gold>Level " + level + "</gold>!</green>"));
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
}
