package com.apexsions.jobs.service;

import com.apexsions.core.api.ApexsionsCoreProvider;
import com.apexsions.economy.api.ApexsionsEconomyProvider;
import com.apexsions.jobs.ApexsionsJobs;
import com.apexsions.jobs.model.JobDefinition;
import com.apexsions.jobs.model.JobRewardItem;
import com.apexsions.jobs.model.JobType;
import com.apexsions.jobs.model.PlayerJobData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JobService {

    private final ApexsionsJobs plugin;
    private final Map<String, JobDefinition> definitions = new HashMap<>();
    private final Map<UUID, Map<String, PlayerJobData>> playerJobsCache = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public JobService(ApexsionsJobs plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        definitions.clear();
        ConfigurationSection jobsSec = plugin.getConfig().getConfigurationSection("jobs");
        if (jobsSec == null) return;

        for (String id : jobsSec.getKeys(false)) {
            try {
                String name = jobsSec.getString(id + ".name", id);
                String title = jobsSec.getString(id + ".title", name);
                String desc = jobsSec.getString(id + ".desc", "");
                String icon = jobsSec.getString(id + ".icon", "IRON_PICKAXE");
                int maxLevel = jobsSec.getInt(id + ".max-level", 100);
                double basePay = jobsSec.getDouble(id + ".base-pay", 10.0);
                double baseExp = jobsSec.getDouble(id + ".base-exp", 5.0);

                JobType type = JobType.valueOf(id.toUpperCase());

                List<JobRewardItem> items = new ArrayList<>();
                List<Map<?, ?>> rawItems = jobsSec.getMapList(id + ".items");
                for (Map<?, ?> m : rawItems) {
                    String target = String.valueOf(m.get("target"));
                    double pay = m.containsKey("pay") ? Double.parseDouble(String.valueOf(m.get("pay"))) : basePay;
                    double exp = m.containsKey("exp") ? Double.parseDouble(String.valueOf(m.get("exp"))) : baseExp;
                    items.add(new JobRewardItem(target, pay, exp));
                }

                definitions.put(id.toLowerCase(), new JobDefinition(id.toLowerCase(), type, name, title, desc, icon, maxLevel, basePay, baseExp, items));
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal memuat profesi job " + id + ": " + e.getMessage());
            }
        }
    }

    public Map<String, JobDefinition> getDefinitions() {
        return definitions;
    }

    public JobDefinition getDefinition(String id) {
        if (id == null) return null;
        return definitions.get(id.toLowerCase());
    }

    public Map<String, PlayerJobData> getPlayerJobs(UUID uuid) {
        return playerJobsCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
    }

    public void setPlayerJobs(UUID uuid, Map<String, PlayerJobData> jobs) {
        playerJobsCache.put(uuid, jobs != null ? jobs : new ConcurrentHashMap<>());
    }

    public void removePlayerJobs(UUID uuid) {
        playerJobsCache.remove(uuid);
    }

    public PlayerJobData getActiveJobData(Player player) {
        Map<String, PlayerJobData> map = getPlayerJobs(player.getUniqueId());
        for (PlayerJobData data : map.values()) {
            if (data.isActive()) return data;
        }
        return null;
    }

    public boolean joinJob(Player player, String jobId) {
        JobDefinition def = getDefinition(jobId);
        if (def == null) return false;

        Map<String, PlayerJobData> map = getPlayerJobs(player.getUniqueId());
        PlayerJobData existing = map.get(jobId.toLowerCase());

        // Deactivate other jobs (Single primary job active at a time)
        for (PlayerJobData other : map.values()) {
            other.setActive(false);
            plugin.getRepository().savePlayerJob(other);
        }

        if (existing == null) {
            existing = new PlayerJobData(player.getUniqueId(), def.getId(), 1, 0.0, 0.0, java.time.LocalDate.now().toString(), true);
            map.put(def.getId(), existing);
        } else {
            existing.setActive(true);
        }

        plugin.getRepository().savePlayerJob(existing);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.sendMessage(mm.deserialize(plugin.getMsg("jobs.joined").replace("%job%", def.getName())));
        return true;
    }

    public boolean leaveJob(Player player, String jobId) {
        Map<String, PlayerJobData> map = getPlayerJobs(player.getUniqueId());
        PlayerJobData data = map.get(jobId.toLowerCase());
        if (data != null && data.isActive()) {
            data.setActive(false);
            plugin.getRepository().savePlayerJob(data);
            JobDefinition def = getDefinition(jobId);
            String jobName = def != null ? def.getName() : jobId;
            player.sendMessage(mm.deserialize(plugin.getMsg("jobs.left").replace("%job%", jobName)));
            return true;
        }
        return false;
    }

    public void handleAction(Player player, JobType jobType, String targetName, double multiplier) {
        if (player == null || !player.isOnline()) return;

        PlayerJobData data = getActiveJobData(player);
        if (data == null) return;

        JobDefinition def = getDefinition(data.getJobId());
        if (def == null || def.getType() != jobType) return;

        JobRewardItem reward = def.getReward(targetName);
        if (reward == null) return;

        // Daily cap reset check
        data.resetDailyIfNeeded();

        double baseCap = plugin.getConfig().getDouble("anti-abuse.daily-base-cap", 150000.0);
        double capPerLvl = plugin.getConfig().getDouble("anti-abuse.daily-cap-per-level", 10000.0);
        double dailyCap = baseCap + (data.getLevel() * capPerLvl);

        if (data.getDailyEarnings() >= dailyCap) {
            return;
        }

        // Wage calculation
        double basePay = reward.getPay() * multiplier;
        double levelBonus = 1.0 + (data.getLevel() * 0.05);
        double streakBonus = 1.0; // Hook to ApexsionsQuests if present

        double grossPay = basePay * levelBonus * streakBonus;

        // Cap check
        if (data.getDailyEarnings() + grossPay > dailyCap) {
            grossPay = Math.max(0.0, dailyCap - data.getDailyEarnings());
        }

        if (grossPay <= 0) return;

        // Kingdom Tax calculation
        boolean taxEnabled = plugin.getConfig().getBoolean("kingdom-tax.enabled", true);
        double taxRate = plugin.getConfig().getDouble("kingdom-tax.tax-rate", 0.05);
        double taxAmount = 0.0;
        double netPay = grossPay;

        if (taxEnabled && ApexsionsCoreProvider.isAvailable()) {
            String kingdom = ApexsionsCoreProvider.get().getPlayerRegionKey(player.getUniqueId());
            if (kingdom != null && !kingdom.equalsIgnoreCase("none")) {
                taxAmount = grossPay * taxRate;
                netPay = grossPay - taxAmount;
                // Deposit tax to kingdom treasury
                if (ApexsionsEconomyProvider.isAvailable()) {
                    ApexsionsEconomyProvider.get().depositKingdomTreasury(kingdom, "rupiah", taxAmount);
                }
            }
        }

        // Deposit net pay to player
        if (ApexsionsEconomyProvider.isAvailable()) {
            ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", netPay);
        }

        // EXP Gain & Level Up
        double expGain = reward.getExp() * multiplier;
        data.setExp(data.getExp() + expGain);
        data.addDailyEarnings(grossPay);

        boolean levelUp = data.checkLevelUp(def.getMaxLevel());
        if (levelUp) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            String lvlMsg = plugin.getMsg("jobs.level-up")
                    .replace("%job%", def.getName())
                    .replace("%level%", String.valueOf(data.getLevel()));
            player.sendMessage(mm.deserialize(lvlMsg));
        }

        // Save
        plugin.getRepository().savePlayerJob(data);

        // Actionbar notification
        String actionMsg = plugin.getMsg("jobs.action-earned")
                .replace("%job%", def.getName())
                .replace("%money%", String.format("%,.0f", netPay))
                .replace("%exp%", String.format("%.0f", expGain));
        player.sendActionBar(mm.deserialize(actionMsg));
    }
}
