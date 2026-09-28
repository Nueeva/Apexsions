package com.apexsions.quests.integration;

import com.apexsions.quests.ApexsionsQuests;
import com.apexsions.quests.model.PlayerQuestProgress;
import com.apexsions.quests.model.PlayerStreakData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final ApexsionsQuests plugin;

    public PlaceholderAPIExpansion(ApexsionsQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "apexsionsquests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nueeva";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerStreakData streak = plugin.getStreakService().getStreakData(player.getUniqueId());
        switch (params.toLowerCase()) {
            case "streak" -> {
                return String.valueOf(streak != null ? streak.getCurrentStreak() : 0);
            }
            case "highest_streak" -> {
                return String.valueOf(streak != null ? streak.getHighestStreak() : 0);
            }
            case "freeze_tokens" -> {
                return String.valueOf(streak != null ? streak.getStreakFreezeCount() : 0);
            }
            case "today_claimed" -> {
                if (streak == null) return "Tidak";
                int today = LocalDate.now().getDayOfMonth();
                return streak.isDayClaimed(today) ? "Ya" : "Tidak";
            }
            case "daily_completed" -> {
                List<PlayerQuestProgress> list = plugin.getQuestManager().getPlayerQuests(player.getUniqueId());
                long count = list.stream().filter(PlayerQuestProgress::isCompleted).count();
                return String.valueOf(count);
            }
            case "daily_total" -> {
                List<PlayerQuestProgress> list = plugin.getQuestManager().getPlayerQuests(player.getUniqueId());
                return String.valueOf(list.size());
            }
            case "passive_multiplier" -> {
                double boost = plugin.getStreakService().getPassiveBonusMultiplier(player.getUniqueId()) * 100;
                return String.format("%.0f%%", boost);
            }
        }

        return null;
    }
}
