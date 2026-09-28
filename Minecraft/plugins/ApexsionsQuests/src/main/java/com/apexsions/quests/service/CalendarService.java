package com.apexsions.quests.service;

import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.api.event.DailyRewardClaimEvent;
import com.apexsions.quests.model.PlayerStreakData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CalendarService {

    public static class DayReward {
        private final int day;
        private final double money;
        private final int coreXp;
        private final int passXp;
        private final String crateKey;
        private final String description;

        public DayReward(int day, double money, int coreXp, int passXp, String crateKey, String description) {
            this.day = day;
            this.money = money;
            this.coreXp = coreXp;
            this.passXp = passXp;
            this.crateKey = crateKey;
            this.description = description != null ? description : "";
        }

        public int getDay() { return day; }
        public double getMoney() { return money; }
        public int getCoreXp() { return coreXp; }
        public int getPassXp() { return passXp; }
        public String getCrateKey() { return crateKey; }
        public String getDescription() { return description; }
    }

    private final ApexsionsQuests plugin;
    private final Map<Integer, DayReward> rewards = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public CalendarService(ApexsionsQuests plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        rewards.clear();
        ConfigurationSection calSec = plugin.getConfig().getConfigurationSection("calendar");
        if (calSec == null) return;

        // Load 4 Weeks (Days 1 to 28)
        for (int w = 1; w <= 4; w++) {
            ConfigurationSection wSec = calSec.getConfigurationSection("week-" + w + ".rewards");
            if (wSec != null) {
                for (String key : wSec.getKeys(false)) {
                    try {
                        int day = Integer.parseInt(key);
                        double money = wSec.getDouble(key + ".money", 0.0);
                        int coreXp = wSec.getInt(key + ".core-xp", 0);
                        int passXp = wSec.getInt(key + ".pass-xp", 0);
                        String crateKey = wSec.getString(key + ".crate-key", null);
                        String desc = wSec.getString(key + ".desc", "");
                        rewards.put(day, new DayReward(day, money, coreXp, passXp, crateKey, desc));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Load Finale Days (Days 29, 30, 31)
        ConfigurationSection finSec = calSec.getConfigurationSection("finale");
        if (finSec != null) {
            for (String key : finSec.getKeys(false)) {
                try {
                    int day = Integer.parseInt(key);
                    double money = finSec.getDouble(key + ".money", 0.0);
                    int coreXp = finSec.getInt(key + ".core-xp", 0);
                    int passXp = finSec.getInt(key + ".pass-xp", 0);
                    String crateKey = finSec.getString(key + ".crate-key", null);
                    String desc = finSec.getString(key + ".desc", "");
                    rewards.put(day, new DayReward(day, money, coreXp, passXp, crateKey, desc));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public DayReward getReward(int day) {
        return rewards.get(day);
    }

    public boolean isTodayClaimed(PlayerStreakData data) {
        int today = LocalDate.now().getDayOfMonth();
        return data.isDayClaimed(today);
    }

    public boolean claimToday(Player player) {
        if (player == null || !player.isOnline()) return false;
        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        if (data == null) return false;

        int today = LocalDate.now().getDayOfMonth();
        if (data.isDayClaimed(today)) {
            player.sendMessage(mm.deserialize(plugin.getMsg("daily.already-claimed")));
            return false;
        }

        DayReward reward = getReward(today);
        if (reward == null) {
            reward = new DayReward(today, 50.0, 5, 60, null, "Hadiah Harian Standar");
        }

        // Mark as claimed
        data.setDayClaimed(today);
        data.setLastClaimDate(LocalDate.now().toString());

        // Increment or verify streak
        plugin.getStreakService().handleDailyLoginStreak(player, data);

        // Dispatch rewards
        dispatchReward(player, reward);

        // Save
        plugin.getRepository().saveStreakData(data);

        // Fire event
        DailyRewardClaimEvent event = new DailyRewardClaimEvent(player, today, reward.getMoney(), reward.getCoreXp(), reward.getPassXp());
        Bukkit.getPluginManager().callEvent(event);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
        String msg = plugin.getMsg("daily.claimed-success")
                .replace("%day%", String.valueOf(today))
                .replace("%reward%", reward.getDescription());
        player.sendMessage(mm.deserialize(msg));
        return true;
    }

    public boolean catchUpClaim(Player player, int missedDay) {
        if (player == null || !player.isOnline()) return false;
        int today = LocalDate.now().getDayOfMonth();
        // Catch-up is available during Finale Days (29, 30, 31)
        if (today < 29) {
            player.sendMessage(mm.deserialize("<red>Fitur Catch-up hanya dapat digunakan pada Hari Spesial Finale (Hari 29-31)!</red>"));
            return false;
        }

        PlayerStreakData data = plugin.getStreakService().getStreakData(player.getUniqueId());
        if (data == null || data.isDayClaimed(missedDay)) return false;

        double cost = plugin.getConfig().getDouble("calendar.catch-up-cost", 5000.0);
        try {
            if (com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
                double bal = com.apexsions.economy.api.ApexsionsEconomyProvider.get().getBalance(player.getUniqueId(), "rupiah");
                if (bal < cost) {
                    String noMoney = plugin.getMsg("daily.catch-up-no-money").replace("%cost%", String.format("%,.0f", cost));
                    player.sendMessage(mm.deserialize(noMoney));
                    return false;
                }
                com.apexsions.economy.api.ApexsionsEconomyProvider.get().withdraw(player.getUniqueId(), "rupiah", cost);
            }
        } catch (Throwable t) {
            // If economy fails
        }

        DayReward reward = getReward(missedDay);
        if (reward == null) {
            reward = new DayReward(missedDay, 50.0, 5, 60, null, "Hadiah Harian");
        }

        data.setDayClaimed(missedDay);
        dispatchReward(player, reward);
        plugin.getRepository().saveStreakData(data);

        String success = plugin.getMsg("daily.catch-up-success")
                .replace("%day%", String.valueOf(missedDay))
                .replace("%cost%", String.format("%,.0f", cost));
        player.sendMessage(mm.deserialize(success));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        return true;
    }

    public void dispatchReward(Player player, DayReward reward) {
        // 1. Money Reward (ApexsionsEconomy)
        if (reward.getMoney() > 0) {
            try {
                if (com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
                    com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", reward.getMoney());
                }
            } catch (Throwable ignored) {}
        }

        // 2. Core Player XP (ApexsionsCore)
        if (reward.getCoreXp() > 0) {
            try {
                if (com.apexsions.core.api.ApexsionsCoreProvider.isAvailable()) {
                    com.apexsions.core.api.ApexsionsCoreProvider.get().addXp(player.getUniqueId(), (long) reward.getCoreXp(), com.apexsions.core.level.xp.XpSource.BATTLEPASS_QUEST);
                }
            } catch (Throwable ignored) {}
        }

        // 3. BattlePass XP (ApexsionsBattlepass Hook)
        if (reward.getPassXp() > 0) {
            try {
                if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                    com.apexsions.battlepass.ApexsionsBattlepass.getInstance()
                            .getXpService().addXp(player, reward.getPassXp());
                }
            } catch (Throwable ignored) {}
        }

        // 4. Crate Key Reward
        if (reward.getCrateKey() != null && !reward.getCrateKey().isBlank()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crates givekey " + player.getName() + " " + reward.getCrateKey() + " 1");
        }
    }
}
