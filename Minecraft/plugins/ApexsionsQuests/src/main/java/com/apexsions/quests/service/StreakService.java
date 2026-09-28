package com.apexsions.quests.service;

import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.api.event.StreakMilestoneClaimEvent;
import com.apexsions.quests.model.PlayerStreakData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StreakService {

    public static class MilestoneReward {
        private final int days;
        private final double money;
        private final int coreXp;
        private final int passXp;
        private final String crateKey;
        private final int streakFreeze;
        private final String description;

        public MilestoneReward(int days, double money, int coreXp, int passXp, String crateKey, int streakFreeze, String description) {
            this.days = days;
            this.money = money;
            this.coreXp = coreXp;
            this.passXp = passXp;
            this.crateKey = crateKey;
            this.streakFreeze = streakFreeze;
            this.description = description != null ? description : "";
        }

        public int getDays() { return days; }
        public double getMoney() { return money; }
        public int getCoreXp() { return coreXp; }
        public int getPassXp() { return passXp; }
        public String getCrateKey() { return crateKey; }
        public int getStreakFreeze() { return streakFreeze; }
        public String getDescription() { return description; }
    }

    private final ApexsionsQuests plugin;
    private final Map<UUID, PlayerStreakData> cache = new ConcurrentHashMap<>();
    private final Map<Integer, MilestoneReward> milestones = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public StreakService(ApexsionsQuests plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        milestones.clear();
        ConfigurationSection msSec = plugin.getConfig().getConfigurationSection("streak-milestones");
        if (msSec == null) return;

        for (String key : msSec.getKeys(false)) {
            try {
                int days = Integer.parseInt(key);
                double money = msSec.getDouble(key + ".money", 0.0);
                int coreXp = msSec.getInt(key + ".core-xp", 0);
                int passXp = msSec.getInt(key + ".pass-xp", 0);
                String crateKey = msSec.getString(key + ".crate-key", null);
                int freeze = msSec.getInt(key + ".streak-freeze", 0);
                String desc = msSec.getString(key + ".desc", "");
                milestones.put(days, new MilestoneReward(days, money, coreXp, passXp, crateKey, freeze, desc));
            } catch (NumberFormatException ignored) {}
        }
    }

    public PlayerStreakData getStreakData(UUID uuid) {
        return cache.get(uuid);
    }

    public void setStreakData(UUID uuid, PlayerStreakData data) {
        if (data != null) cache.put(uuid, data);
    }

    public void removeStreakData(UUID uuid) {
        cache.remove(uuid);
    }

    public Map<Integer, MilestoneReward> getMilestones() {
        return milestones;
    }

    public void handleDailyLoginStreak(Player player, PlayerStreakData data) {
        LocalDate today = LocalDate.now();
        String lastClaimStr = data.getLastClaimDate();

        if (lastClaimStr == null || lastClaimStr.isBlank()) {
            data.setCurrentStreak(1);
        } else {
            try {
                LocalDate lastClaim = LocalDate.parse(lastClaimStr);
                long daysDiff = ChronoUnit.DAYS.between(lastClaim, today);

                if (daysDiff == 1) {
                    // Claimed yesterday, streak continues!
                    data.setCurrentStreak(data.getCurrentStreak() + 1);
                } else if (daysDiff > 1) {
                    // Missed days
                    if (data.useStreakFreeze()) {
                        data.setCurrentStreak(data.getCurrentStreak() + 1);
                        player.sendMessage(mm.deserialize(plugin.getMsg("daily.streak-frozen")));
                    } else {
                        data.setCurrentStreak(1);
                        player.sendMessage(mm.deserialize(plugin.getMsg("daily.streak-lost").replace("%hours%", "36")));
                    }
                }
            } catch (Throwable t) {
                data.setCurrentStreak(1);
            }
        }

        data.setLastLoginDate(today.toString());
    }

    public boolean claimMilestone(Player player, int milestoneDays) {
        if (player == null || !player.isOnline()) return false;
        PlayerStreakData data = getStreakData(player.getUniqueId());
        if (data == null) return false;

        if (data.getCurrentStreak() < milestoneDays) {
            player.sendMessage(mm.deserialize("<red>Streak Anda saat ini (" + data.getCurrentStreak() + " Hari) belum mencapai " + milestoneDays + " Hari!</red>"));
            return false;
        }

        if (data.isMilestoneClaimed(milestoneDays)) {
            player.sendMessage(mm.deserialize("<red>Anda sudah mengklaim hadiah Milestone " + milestoneDays + " Hari ini!</red>"));
            return false;
        }

        MilestoneReward reward = milestones.get(milestoneDays);
        if (reward == null) return false;

        data.addClaimedMilestone(milestoneDays);

        // 1. Money
        if (reward.getMoney() > 0) {
            try {
                if (com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
                    com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", reward.getMoney());
                }
            } catch (Throwable ignored) {}
        }

        // 2. Core XP
        if (reward.getCoreXp() > 0) {
            try {
                if (com.apexsions.core.api.ApexsionsCoreProvider.isAvailable()) {
                    com.apexsions.core.api.ApexsionsCoreProvider.get().addXp(player.getUniqueId(), (long) reward.getCoreXp(), com.apexsions.core.level.xp.XpSource.BATTLEPASS_QUEST);
                }
            } catch (Throwable ignored) {}
        }

        // 3. Battlepass XP
        if (reward.getPassXp() > 0) {
            try {
                if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                    com.apexsions.battlepass.ApexsionsBattlepass.getInstance()
                            .getXpService().addXp(player, reward.getPassXp());
                }
            } catch (Throwable ignored) {}
        }

        // 4. Streak Freeze Token
        if (reward.getStreakFreeze() > 0) {
            data.addStreakFreeze(reward.getStreakFreeze());
        }

        // 5. Crate Key
        if (reward.getCrateKey() != null && !reward.getCrateKey().isBlank()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crates givekey " + player.getName() + " " + reward.getCrateKey() + " 1");
        }

        plugin.getRepository().saveStreakData(data);

        StreakMilestoneClaimEvent event = new StreakMilestoneClaimEvent(player, milestoneDays, reward.getMoney(), reward.getCoreXp(), reward.getPassXp());
        Bukkit.getPluginManager().callEvent(event);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        String msg = plugin.getMsg("daily.milestone-claimed")
                .replace("%days%", String.valueOf(milestoneDays))
                .replace("%reward%", reward.getDescription());
        player.sendMessage(mm.deserialize(msg));
        return true;
    }

    public double getPassiveBonusMultiplier(UUID uuid) {
        PlayerStreakData data = getStreakData(uuid);
        if (data == null) return 0.0;
        int s = data.getCurrentStreak();
        if (s >= 28) return 0.15;
        if (s >= 14) return 0.10;
        if (s >= 7) return 0.05;
        return 0.0;
    }
}
