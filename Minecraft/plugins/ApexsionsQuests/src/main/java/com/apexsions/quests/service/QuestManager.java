package com.apexsions.quests.service;

import com.apexsions.core.api.ApexsionsCoreAPI;
import com.apexsions.core.level.xp.XpSource;
import com.apexsions.economy.api.ApexsionsEconomyAPI;
import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.api.event.DailyQuestCompleteEvent;
import com.apexsions.quests.model.PlayerQuestProgress;
import com.apexsions.quests.model.PlayerStreakData;
import com.apexsions.quests.model.Quest;
import com.apexsions.quests.model.QuestObjectiveType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final ApexsionsQuests plugin;
    private final Map<String, Quest> questPool = new HashMap<>();
    private final Map<UUID, List<PlayerQuestProgress>> activeQuests = new ConcurrentHashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    public QuestManager(ApexsionsQuests plugin) {
        this.plugin = plugin;
        loadQuestsFromConfig();
    }

    public void loadQuestsFromConfig() {
        questPool.clear();
        ConfigurationSection poolSec = plugin.getConfig().getConfigurationSection("quests.pool");
        if (poolSec == null) return;

        for (String id : poolSec.getKeys(false)) {
            try {
                String typeStr = poolSec.getString(id + ".type", "BREAK_BLOCK");
                QuestObjectiveType type = QuestObjectiveType.valueOf(typeStr.toUpperCase());
                String targetStr = poolSec.getString(id + ".target", "ANY");
                List<String> targets = Arrays.stream(targetStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

                int amount = poolSec.getInt(id + ".amount", 10);
                String title = poolSec.getString(id + ".title", id);
                String desc = poolSec.getString(id + ".desc", "");

                double money = poolSec.getDouble(id + ".reward.money", 0.0);
                int coreXp = poolSec.getInt(id + ".reward.core-xp", 0);
                int passXp = poolSec.getInt(id + ".reward.pass-xp", 0);
                String cmd = poolSec.getString(id + ".reward.command", null);

                Quest q = new Quest(id, type, targets, amount, title, desc, money, coreXp, passXp, cmd);
                questPool.put(id, q);
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal memuat quest " + id + ": " + e.getMessage());
            }
        }
    }

    public Quest getQuest(String id) {
        return questPool.get(id);
    }

    public List<PlayerQuestProgress> getPlayerQuests(UUID uuid) {
        return activeQuests.computeIfAbsent(uuid, k -> new ArrayList<>());
    }

    public void setPlayerQuests(UUID uuid, List<PlayerQuestProgress> quests) {
        activeQuests.put(uuid, quests != null ? quests : new ArrayList<>());
    }

    public void removePlayerQuests(UUID uuid) {
        activeQuests.remove(uuid);
    }

    public void ensureDailyQuestsAssigned(Player player) {
        UUID uuid = player.getUniqueId();
        String today = LocalDate.now().toString();

        plugin.getRepository().loadDailyQuests(uuid, today).thenAccept(loaded -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (loaded != null && !loaded.isEmpty()) {
                    activeQuests.put(uuid, loaded);
                } else {
                    // Generate new random quests for today
                    List<PlayerQuestProgress> generated = generateRandomQuests(player, today);
                    activeQuests.put(uuid, generated);
                    for (PlayerQuestProgress p : generated) {
                        plugin.getRepository().saveDailyQuest(p);
                    }
                }
            });
        });
    }

    private List<PlayerQuestProgress> generateRandomQuests(Player player, String date) {
        int amountNeeded = plugin.getConfig().getInt("quests.daily-quests-amount", 4);
        List<Quest> all = new ArrayList<>(questPool.values());
        Collections.shuffle(all);

        List<PlayerQuestProgress> result = new ArrayList<>();
        int count = Math.min(amountNeeded, all.size());
        for (int i = 0; i < count; i++) {
            Quest q = all.get(i);
            String id = player.getUniqueId() + "_" + date + "_" + q.getId();
            result.add(new PlayerQuestProgress(id, player.getUniqueId(), q.getId(), date, 0, q.getTargetAmount(), false, false));
        }
        return result;
    }

    public boolean rerollQuest(Player player, String questIdToReplace) {
        UUID uuid = player.getUniqueId();
        PlayerStreakData streakData = plugin.getStreakService().getStreakData(uuid);
        if (streakData == null) return false;

        String today = LocalDate.now().toString();
        if (!today.equals(streakData.getLastRerollDate())) {
            streakData.setRerollsUsedToday(0);
            streakData.setLastRerollDate(today);
        }

        int maxRerolls = plugin.getConfig().getInt("quests.daily-rerolls-allowed", 1);
        if (streakData.getRerollsUsedToday() >= maxRerolls) {
            player.sendMessage(mm.deserialize(plugin.getMsg("quests.reroll-limit")));
            return false;
        }

        List<PlayerQuestProgress> list = getPlayerQuests(uuid);
        PlayerQuestProgress target = null;
        for (PlayerQuestProgress p : list) {
            if (p.getQuestId().equalsIgnoreCase(questIdToReplace)) {
                target = p;
                break;
            }
        }
        if (target == null || target.isCompleted()) return false;

        // Pick a quest not currently in list
        Set<String> currentIds = new HashSet<>();
        for (PlayerQuestProgress p : list) currentIds.add(p.getQuestId());

        List<Quest> available = new ArrayList<>();
        for (Quest q : questPool.values()) {
            if (!currentIds.contains(q.getId())) {
                available.add(q);
            }
        }

        if (available.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>Tidak ada quest pengganti yang tersedia saat ini.</red>"));
            return false;
        }

        Collections.shuffle(available);
        Quest newQuest = available.get(0);

        list.remove(target);
        String newId = uuid + "_" + today + "_" + newQuest.getId();
        PlayerQuestProgress newProg = new PlayerQuestProgress(newId, uuid, newQuest.getId(), today, 0, newQuest.getTargetAmount(), false, false);
        list.add(newProg);

        streakData.setRerollsUsedToday(streakData.getRerollsUsedToday() + 1);
        plugin.getRepository().saveStreakData(streakData);
        plugin.getRepository().saveDailyQuest(newProg);

        player.sendMessage(mm.deserialize(plugin.getMsg("quests.reroll-success")));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
        return true;
    }

    public void onObjectiveProgress(Player player, QuestObjectiveType type, String targetName, int amount) {
        if (player == null || !player.isOnline()) return;
        List<PlayerQuestProgress> list = getPlayerQuests(player.getUniqueId());
        if (list == null || list.isEmpty()) return;

        for (PlayerQuestProgress prog : list) {
            if (prog.isCompleted()) continue;
            Quest q = getQuest(prog.getQuestId());
            if (q == null || q.getType() != type) continue;
            if (!q.matchesTarget(targetName)) continue;

            prog.addProgress(amount);
            plugin.getRepository().saveDailyQuest(prog);

            if (prog.isCompleted()) {
                // Quest newly completed!
                DailyQuestCompleteEvent event = new DailyQuestCompleteEvent(player, q);
                Bukkit.getPluginManager().callEvent(event);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                String msg = plugin.getMsg("quests.completed").replace("%title%", q.getTitle());
                player.sendMessage(mm.deserialize(msg));
            }
        }
    }

    public boolean claimReward(Player player, String questId) {
        List<PlayerQuestProgress> list = getPlayerQuests(player.getUniqueId());
        if (list == null) return false;

        PlayerQuestProgress target = null;
        for (PlayerQuestProgress p : list) {
            if (p.getQuestId().equalsIgnoreCase(questId)) {
                target = p;
                break;
            }
        }

        if (target == null || !target.isCompleted() || target.isClaimed()) {
            return false;
        }

        Quest q = getQuest(target.getQuestId());
        if (q == null) return false;

        target.setClaimed(true);
        plugin.getRepository().saveDailyQuest(target);

        // 1. Money
        if (q.getRewardMoney() > 0) {
            try {
                if (com.apexsions.economy.api.ApexsionsEconomyProvider.isAvailable()) {
                    com.apexsions.economy.api.ApexsionsEconomyProvider.get().deposit(player.getUniqueId(), "rupiah", q.getRewardMoney());
                }
            } catch (Throwable ignored) {}
        }

        // 2. Core XP
        if (q.getRewardCoreXp() > 0) {
            try {
                if (com.apexsions.core.api.ApexsionsCoreProvider.isAvailable()) {
                    com.apexsions.core.api.ApexsionsCoreProvider.get().addXp(player.getUniqueId(), (long) q.getRewardCoreXp(), com.apexsions.core.level.xp.XpSource.BATTLEPASS_QUEST);
                }
            } catch (Throwable ignored) {}
        }

        // 3. Battlepass XP
        if (q.getRewardPassXp() > 0) {
            try {
                if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                    com.apexsions.battlepass.ApexsionsBattlepass.getInstance()
                            .getXpService().addXp(player, q.getRewardPassXp());
                }
            } catch (Throwable ignored) {}
        }

        // 4. Custom Command if any
        if (q.getRewardCommand() != null && !q.getRewardCommand().isBlank()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), q.getRewardCommand().replace("%player%", player.getName()));
        }

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        String msg = plugin.getMsg("quests.claimed")
                .replace("%money%", String.format("%,.0f", q.getRewardMoney()))
                .replace("%core_xp%", String.valueOf(q.getRewardCoreXp()))
                .replace("%pass_xp%", String.valueOf(q.getRewardPassXp()));
        player.sendMessage(mm.deserialize(msg));
        return true;
    }
}
