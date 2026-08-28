package com.apex.battlepass.quest.service;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.quest.model.Quest;
import com.apex.battlepass.quest.model.QuestCategory;
import com.apex.battlepass.quest.model.QuestStatus;
import com.apex.battlepass.season.SeasonState;
import com.apex.battlepass.util.TimeUtil;

import java.time.*;

public class QuestPeriodService {

    public static final int SPECIAL_WEEK_INDEX = 5;

    private final ApexsionsBattlepass plugin;

    public QuestPeriodService(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
    }

    /**
     * Get current active week in the current calendar month:
     * 1 = Week 1 (Days 1..7)
     * 2 = Week 2 (Days 8..14)
     * 3 = Week 3 (Days 15..21)
     * 4 = Week 4 (Days 22..28)
     * 5 = Special Week (Days 29..End of Month)
     */
    public int getCurrentWeekNumber() {
        ZoneId zone = plugin.getSeasonManager().getZoneId();
        ZonedDateTime now = ZonedDateTime.now(zone);
        int dayOfMonth = now.getDayOfMonth();

        if (dayOfMonth <= 7) return 1;
        if (dayOfMonth <= 14) return 2;
        if (dayOfMonth <= 21) return 3;
        if (dayOfMonth <= 28) return 4;
        return SPECIAL_WEEK_INDEX; // Special Week
    }

    /**
     * Check if current calendar month has days for Special Week (e.g. 29, 30, 31 days).
     */
    public boolean monthHasSpecialWeek() {
        ZoneId zone = plugin.getSeasonManager().getZoneId();
        ZonedDateTime now = ZonedDateTime.now(zone);
        int lengthOfMonth = now.toLocalDate().lengthOfMonth();
        return lengthOfMonth > 28;
    }

    public int getSpecialWeekDayCount() {
        ZoneId zone = plugin.getSeasonManager().getZoneId();
        ZonedDateTime now = ZonedDateTime.now(zone);
        int lengthOfMonth = now.toLocalDate().lengthOfMonth();
        return Math.max(0, lengthOfMonth - 28);
    }

    /**
     * Get current Month in the 3-month Season cycle (1, 2, or 3).
     */
    public int getCurrentMonthNumber() {
        return plugin.getSeasonManager().getCurrentMonthNumber();
    }

    public QuestStatus getWeeklyPeriodStatus(int week) {
        SeasonState state = plugin.getSeasonManager().getSeasonState();
        if (state == SeasonState.TRANSITION || state == SeasonState.ENDED) {
            return QuestStatus.TRANSITION;
        }
        if (state == SeasonState.NOT_STARTED) {
            return QuestStatus.UPCOMING;
        }

        int currentWeek = getCurrentWeekNumber();
        if (week == currentWeek) {
            return QuestStatus.ACTIVE;
        } else if (week < currentWeek) {
            return QuestStatus.EXPIRED;
        } else {
            return QuestStatus.UPCOMING;
        }
    }

    public QuestStatus getMonthlyPeriodStatus(int month) {
        SeasonState state = plugin.getSeasonManager().getSeasonState();
        if (state == SeasonState.TRANSITION || state == SeasonState.ENDED) {
            return QuestStatus.TRANSITION;
        }
        if (state == SeasonState.NOT_STARTED) {
            return QuestStatus.UPCOMING;
        }

        int currentMonth = getCurrentMonthNumber();
        if (month == currentMonth) {
            return QuestStatus.ACTIVE;
        } else if (month < currentMonth) {
            return QuestStatus.EXPIRED;
        } else {
            return QuestStatus.UPCOMING;
        }
    }

    public boolean isQuestActive(Quest quest) {
        if (!plugin.getSeasonManager().isActive()) return false;
        if (quest == null) return false;

        if (quest.getCategory() == QuestCategory.DAILY) {
            return true;
        } else if (quest.getCategory() == QuestCategory.WEEKLY) {
            return getWeeklyPeriodStatus(quest.getPeriodIndex()) == QuestStatus.ACTIVE;
        } else if (quest.getCategory() == QuestCategory.MONTHLY) {
            return getMonthlyPeriodStatus(quest.getPeriodIndex()) == QuestStatus.ACTIVE;
        }
        return false;
    }

    /**
     * Returns real-time countdown for Daily Quests (e.g. "12h 34m").
     */
    public String getDailyResetTimeLeft() {
        ZoneId zone = plugin.getSeasonManager().getZoneId();
        ZonedDateTime now = ZonedDateTime.now(zone);
        String resetTimeStr = plugin.getConfig().getString("resets.daily-reset-time", "00:00");
        LocalTime resetTime;
        try {
            resetTime = LocalTime.parse(resetTimeStr);
        } catch (Exception e) {
            resetTime = LocalTime.MIDNIGHT;
        }

        ZonedDateTime nextReset = now.toLocalDate().atTime(resetTime).atZone(zone);
        if (!now.isBefore(nextReset)) {
            nextReset = nextReset.plusDays(1);
        }

        Duration duration = Duration.between(now, nextReset);
        return TimeUtil.formatHoursMinutes(duration);
    }

    /**
     * Returns real-time countdown for current active Weekly Quest / Special Week (e.g. "4d 8h").
     */
    public String getWeeklyTimeLeft() {
        ZoneId zone = plugin.getSeasonManager().getZoneId();
        ZonedDateTime now = ZonedDateTime.now(zone);
        int currentWeek = getCurrentWeekNumber();

        ZonedDateTime weekEnd;
        if (currentWeek == 1) {
            weekEnd = now.withDayOfMonth(7).toLocalDate().atTime(LocalTime.MAX).atZone(zone);
        } else if (currentWeek == 2) {
            weekEnd = now.withDayOfMonth(14).toLocalDate().atTime(LocalTime.MAX).atZone(zone);
        } else if (currentWeek == 3) {
            weekEnd = now.withDayOfMonth(21).toLocalDate().atTime(LocalTime.MAX).atZone(zone);
        } else if (currentWeek == 4) {
            weekEnd = now.withDayOfMonth(28).toLocalDate().atTime(LocalTime.MAX).atZone(zone);
        } else {
            // Special Week ends on last day of current month
            int lastDay = now.toLocalDate().lengthOfMonth();
            weekEnd = now.withDayOfMonth(lastDay).toLocalDate().atTime(LocalTime.MAX).atZone(zone);
        }

        Duration duration = Duration.between(now, weekEnd);
        return TimeUtil.formatDaysHours(duration);
    }

    /**
     * Returns real-time countdown for current Monthly Quest (e.g. "18d 6h").
     */
    public String getMonthlyTimeLeft() {
        ZoneId zone = plugin.getSeasonManager().getZoneId();
        ZonedDateTime now = ZonedDateTime.now(zone);
        int lastDay = now.toLocalDate().lengthOfMonth();
        ZonedDateTime monthEnd = now.withDayOfMonth(lastDay).toLocalDate().atTime(LocalTime.MAX).atZone(zone);

        Duration duration = Duration.between(now, monthEnd);
        return TimeUtil.formatDaysHours(duration);
    }

    /**
     * Returns real-time countdown for Season (e.g. "42d 13h").
     */
    public String getSeasonTimeLeft() {
        return plugin.getSeasonManager().getTimeLeftFormatted();
    }
}
