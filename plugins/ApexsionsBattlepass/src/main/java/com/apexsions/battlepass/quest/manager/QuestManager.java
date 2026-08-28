package com.apexsions.battlepass.quest.manager;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.api.event.BattlePassQuestCompleteEvent;
import com.apexsions.battlepass.player.PlayerData;
import com.apexsions.battlepass.quest.model.Quest;
import com.apexsions.battlepass.quest.model.QuestCategory;
import com.apexsions.battlepass.quest.model.QuestObjectiveType;
import com.apexsions.battlepass.quest.model.QuestStatus;
import com.apexsions.battlepass.quest.service.QuestPeriodService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class QuestManager {

    private final ApexsionsBattlepass plugin;
    private final QuestPeriodService periodService;
    private final Map<String, Quest> dailyQuests = new LinkedHashMap<>();
    private final Map<Integer, Map<String, Quest>> weeklyQuests = new LinkedHashMap<>();
    private final Map<Integer, Map<String, Quest>> monthlyQuests = new LinkedHashMap<>();

    public QuestManager(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        this.periodService = new QuestPeriodService(plugin);
        loadQuests();
    }

    public void loadQuests() {
        dailyQuests.clear();
        weeklyQuests.clear();
        monthlyQuests.clear();

        // 1. Load Daily Quests
        File dailyFile = new File(plugin.getDataFolder(), "quests/daily.yml");
        if (!dailyFile.exists()) plugin.saveResource("quests/daily.yml", false);
        FileConfiguration dailyConfig = YamlConfiguration.loadConfiguration(dailyFile);
        ConfigurationSection dailySec = dailyConfig.getConfigurationSection("quests");
        if (dailySec != null) {
            for (String key : dailySec.getKeys(false)) {
                Quest q = parseQuest(key, dailySec.getConfigurationSection(key), QuestCategory.DAILY, 0);
                if (q != null) dailyQuests.put(key, q);
            }
        }

        // 2. Load Weekly Quests
        File weeklyFile = new File(plugin.getDataFolder(), "quests/weekly.yml");
        if (!weeklyFile.exists()) plugin.saveResource("quests/weekly.yml", false);
        FileConfiguration weeklyConfig = YamlConfiguration.loadConfiguration(weeklyFile);
        ConfigurationSection weeksSec = weeklyConfig.getConfigurationSection("weeks");
        if (weeksSec != null) {
            for (String weekStr : weeksSec.getKeys(false)) {
                try {
                    int weekIndex = Integer.parseInt(weekStr);
                    ConfigurationSection wSec = weeksSec.getConfigurationSection(weekStr);
                    Map<String, Quest> map = new LinkedHashMap<>();
                    if (wSec != null) {
                        for (String key : wSec.getKeys(false)) {
                            Quest q = parseQuest(key, wSec.getConfigurationSection(key), QuestCategory.WEEKLY, weekIndex);
                            if (q != null) map.put(key, q);
                        }
                    }
                    weeklyQuests.put(weekIndex, map);
                } catch (NumberFormatException ignored) {}
            }
        }

        // 3. Load Monthly Quests
        File monthlyFile = new File(plugin.getDataFolder(), "quests/monthly.yml");
        if (!monthlyFile.exists()) plugin.saveResource("quests/monthly.yml", false);
        FileConfiguration monthlyConfig = YamlConfiguration.loadConfiguration(monthlyFile);
        ConfigurationSection monthsSec = monthlyConfig.getConfigurationSection("months");
        if (monthsSec != null) {
            for (String monthStr : monthsSec.getKeys(false)) {
                try {
                    int monthIndex = Integer.parseInt(monthStr);
                    ConfigurationSection mSec = monthsSec.getConfigurationSection(monthStr);
                    Map<String, Quest> map = new LinkedHashMap<>();
                    if (mSec != null) {
                        for (String key : mSec.getKeys(false)) {
                            Quest q = parseQuest(key, mSec.getConfigurationSection(key), QuestCategory.MONTHLY, monthIndex);
                            if (q != null) map.put(key, q);
                        }
                    }
                    monthlyQuests.put(monthIndex, map);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private Quest parseQuest(String id, ConfigurationSection sec, QuestCategory category, int periodIndex) {
        if (sec == null) return null;
        String name = sec.getString("name", id);
        String desc = sec.getString("description", "");
        String typeStr = sec.getString("type", "KILL_ENTITY").toUpperCase();
        QuestObjectiveType type;
        try {
            type = QuestObjectiveType.valueOf(typeStr);
        } catch (Exception e) {
            type = QuestObjectiveType.KILL_ENTITY;
        }

        String entityStr = sec.getString("target-entity");
        EntityType entityType = null;
        if (entityStr != null) {
            try { entityType = EntityType.valueOf(entityStr.toUpperCase()); } catch (Exception ignored) {}
        }

        String blockStr = sec.getString("target-block");
        Material blockMat = null;
        if (blockStr != null) {
            blockMat = Material.matchMaterial(blockStr);
        }

        String itemStr = sec.getString("target-item");
        Material itemMat = null;
        if (itemStr != null) {
            itemMat = Material.matchMaterial(itemStr);
        }

        int targetAmount = sec.getInt("target-amount", 1);
        int rewardXp = sec.getInt("reward-xp", 100);
        int rewardCoins = sec.getInt("reward-coins", 10);

        return new Quest(id, name, desc, category, type, periodIndex, entityType, blockMat, itemMat, targetAmount, rewardXp, rewardCoins);
    }

    public QuestPeriodService getPeriodService() {
        return periodService;
    }

    public Map<String, Quest> getDailyQuests() {
        return dailyQuests;
    }

    public Map<String, Quest> getActiveDailyQuests() {
        if (dailyQuests.size() <= 10) {
            return dailyQuests;
        }

        // Deterministic daily selection based on date epoch day
        java.time.ZoneId zone = plugin.getSeasonManager().getZoneId();
        long epochDay = java.time.LocalDate.now(zone).toEpochDay();
        Random random = new Random(epochDay * 31L + 17L);

        List<Quest> allList = new ArrayList<>(dailyQuests.values());
        Collections.shuffle(allList, random);

        Map<String, Quest> selected = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(10, allList.size()); i++) {
            Quest q = allList.get(i);
            selected.put(q.getId(), q);
        }
        return selected;
    }

    public Map<Integer, Map<String, Quest>> getWeeklyQuests() {
        return weeklyQuests;
    }

    public Map<Integer, Map<String, Quest>> getMonthlyQuests() {
        return monthlyQuests;
    }

    public QuestStatus getQuestStatus(Player player, Quest quest) {
        if (quest == null) return QuestStatus.LOCKED;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data != null && data.isQuestCompleted(quest.getId())) {
            return QuestStatus.COMPLETED;
        }

        if (quest.getCategory() == QuestCategory.DAILY) {
            if (!plugin.getSeasonManager().isActive()) return QuestStatus.TRANSITION;
            return getActiveDailyQuests().containsKey(quest.getId()) ? QuestStatus.ACTIVE : QuestStatus.LOCKED;
        } else if (quest.getCategory() == QuestCategory.WEEKLY) {
            return periodService.getWeeklyPeriodStatus(quest.getPeriodIndex());
        } else if (quest.getCategory() == QuestCategory.MONTHLY) {
            return periodService.getMonthlyPeriodStatus(quest.getPeriodIndex());
        }
        return QuestStatus.LOCKED;
    }


    public void incrementProgress(Player player, QuestObjectiveType type, Object target, int amount) {
        if (!plugin.getSeasonManager().isActive()) return; // Locked outside active season

        PlayerData data = plugin.getPlayerManager().getPlayerData(player);
        if (data == null) return;

        List<Quest> activeQuests = getActiveQuestsForPlayer(player, type);
        for (Quest quest : activeQuests) {
            if (data.isQuestCompleted(quest.getId())) continue;

            // Strict Backend Locking Check
            if (!periodService.isQuestActive(quest)) continue;

            // Match Targets
            if (quest.getType() == QuestObjectiveType.KILL_ENTITY && target instanceof EntityType et) {
                if (quest.getTargetEntity() != null && quest.getTargetEntity() != et) continue;
            } else if ((quest.getType() == QuestObjectiveType.BREAK_BLOCK || quest.getType() == QuestObjectiveType.PLACE_BLOCK || quest.getType() == QuestObjectiveType.MINE_BLOCK) && target instanceof Material mat) {
                if (quest.getTargetBlock() != null && quest.getTargetBlock() != mat) continue;
            } else if (quest.getType() == QuestObjectiveType.CRAFT_ITEM && target instanceof Material mat) {
                if (quest.getTargetItem() != null && quest.getTargetItem() != mat) continue;
            }

            int current = data.getQuestProgress(quest.getId());
            int newProgress = current + amount;
            data.setQuestProgress(quest.getId(), newProgress);

            if (newProgress >= quest.getTargetAmount()) {
                data.setQuestCompleted(quest.getId(), true);

                // Add XP through centralized XP service
                plugin.getXpService().addXp(player, quest.getRewardXp());

                // Add Coins
                if (quest.getRewardCoins() > 0) {
                    plugin.getCurrencyService().addCurrency(player.getUniqueId(), quest.getRewardCoins());
                }

                // Trigger Quest Complete Event
                BattlePassQuestCompleteEvent event = new BattlePassQuestCompleteEvent(player, quest);
                Bukkit.getPluginManager().callEvent(event);

                player.sendMessage(plugin.getMessage("quest-completed")
                        .replace("%quest%", quest.getName())
                        .replace("%xp%", String.valueOf(quest.getRewardXp()))
                        .replace("%coins%", String.valueOf(quest.getRewardCoins())));
            }
        }
    }

    public void saveQuests() {
        // 1. Save Daily
        File dailyFile = new File(plugin.getDataFolder(), "quests/daily.yml");
        FileConfiguration dailyConfig = new YamlConfiguration();
        for (Quest q : dailyQuests.values()) {
            writeQuestToConfig(dailyConfig, "quests." + q.getId(), q);
        }
        try { dailyConfig.save(dailyFile); } catch (Exception ignored) {}

        // 2. Save Weekly
        File weeklyFile = new File(plugin.getDataFolder(), "quests/weekly.yml");
        FileConfiguration weeklyConfig = new YamlConfiguration();
        for (var entry : weeklyQuests.entrySet()) {
            int week = entry.getKey();
            for (Quest q : entry.getValue().values()) {
                writeQuestToConfig(weeklyConfig, "weeks." + week + "." + q.getId(), q);
            }
        }
        try { weeklyConfig.save(weeklyFile); } catch (Exception ignored) {}

        // 3. Save Monthly
        File monthlyFile = new File(plugin.getDataFolder(), "quests/monthly.yml");
        FileConfiguration monthlyConfig = new YamlConfiguration();
        for (var entry : monthlyQuests.entrySet()) {
            int month = entry.getKey();
            for (Quest q : entry.getValue().values()) {
                writeQuestToConfig(monthlyConfig, "months." + month + "." + q.getId(), q);
            }
        }
        try { monthlyConfig.save(monthlyFile); } catch (Exception ignored) {}
    }

    private void writeQuestToConfig(FileConfiguration config, String path, Quest q) {
        config.set(path + ".name", q.getName());
        config.set(path + ".description", q.getDescription());
        config.set(path + ".type", q.getType().name());
        if (q.getTargetEntity() != null) config.set(path + ".target-entity", q.getTargetEntity().name());
        if (q.getTargetBlock() != null) config.set(path + ".target-block", q.getTargetBlock().name());
        if (q.getTargetItem() != null) config.set(path + ".target-item", q.getTargetItem().name());
        config.set(path + ".target-amount", q.getTargetAmount());
        config.set(path + ".reward-xp", q.getRewardXp());
        config.set(path + ".reward-coins", q.getRewardCoins());
    }

    public void addOrUpdateQuest(Quest quest) {
        if (quest == null) return;
        if (quest.getCategory() == QuestCategory.DAILY) {
            dailyQuests.put(quest.getId(), quest);
        } else if (quest.getCategory() == QuestCategory.WEEKLY) {
            weeklyQuests.computeIfAbsent(quest.getPeriodIndex(), k -> new LinkedHashMap<>()).put(quest.getId(), quest);
        } else if (quest.getCategory() == QuestCategory.MONTHLY) {
            monthlyQuests.computeIfAbsent(quest.getPeriodIndex(), k -> new LinkedHashMap<>()).put(quest.getId(), quest);
        }
        saveQuests();
    }

    public void deleteQuest(QuestCategory category, int periodIndex, String questId) {
        if (category == QuestCategory.DAILY) {
            dailyQuests.remove(questId);
        } else if (category == QuestCategory.WEEKLY) {
            Map<String, Quest> map = weeklyQuests.get(periodIndex);
            if (map != null) map.remove(questId);
        } else if (category == QuestCategory.MONTHLY) {
            Map<String, Quest> map = monthlyQuests.get(periodIndex);
            if (map != null) map.remove(questId);
        }
        saveQuests();
    }

    private List<Quest> getActiveQuestsForPlayer(Player player, QuestObjectiveType type) {
        List<Quest> list = new ArrayList<>();

        // Daily (only today's 10 active daily quests)
        Map<String, Quest> activeDaily = getActiveDailyQuests();
        for (Quest q : activeDaily.values()) {
            if (q.getType() == type && periodService.isQuestActive(q)) {
                list.add(q);
            }
        }

        // Weekly
        int currentWeek = periodService.getCurrentWeekNumber();
        Map<String, Quest> wMap = weeklyQuests.get(currentWeek);
        if (wMap != null) {
            for (Quest q : wMap.values()) {
                if (q.getType() == type && periodService.isQuestActive(q)) {
                    list.add(q);
                }
            }
        }

        // Monthly
        int currentMonth = periodService.getCurrentMonthNumber();
        Map<String, Quest> mMap = monthlyQuests.get(currentMonth);
        if (mMap != null) {
            for (Quest q : mMap.values()) {
                if (q.getType() == type && periodService.isQuestActive(q)) {
                    list.add(q);
                }
            }
        }

        return list;
    }

}
