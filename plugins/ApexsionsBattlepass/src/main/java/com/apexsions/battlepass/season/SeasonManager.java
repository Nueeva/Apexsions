package com.apexsions.battlepass.season;

import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.util.TimeUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class SeasonManager {

    private final ApexsionsBattlepass plugin;
    private Season currentSeason;
    private ZoneId zoneId;

    public SeasonManager(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        loadSeasonConfig();
    }

    public void loadSeasonConfig() {
        String tz = plugin.getConfig().getString("timezone", "Asia/Jakarta");
        try {
            this.zoneId = ZoneId.of(tz);
        } catch (Exception e) {
            this.zoneId = ZoneId.systemDefault();
        }

        File file = new File(plugin.getDataFolder(), "seasons.yml");
        if (!file.exists()) {
            plugin.saveResource("seasons.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        int id = config.getInt("season.id", 1);
        String name = config.getString("season.name", "Season 1");
        String startStr = config.getString("season.start-date", "2026-08-01 00:00:00");
        String endStr = config.getString("season.end-date", "2026-10-31 23:59:59");

        ZonedDateTime start = TimeUtil.parseDateTime(startStr, zoneId);
        ZonedDateTime end = TimeUtil.parseDateTime(endStr, zoneId);

        // If end is before or equal to start, default to start + 3 months
        if (!end.isAfter(start)) {
            end = start.plusMonths(3).minusSeconds(1);
        }

        this.currentSeason = new Season(id, name, start, end);
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public SeasonState getSeasonState() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if (now.isBefore(currentSeason.getStartDate())) {
            return SeasonState.NOT_STARTED;
        } else if (!now.isAfter(currentSeason.getEndDate())) {
            return SeasonState.ACTIVE;
        } else if (!now.isAfter(currentSeason.getTransitionEndDate())) {
            return SeasonState.TRANSITION;
        } else {
            return SeasonState.ENDED;
        }
    }

    public boolean isActive() {
        return getSeasonState() == SeasonState.ACTIVE;
    }

    public boolean isTransition() {
        return getSeasonState() == SeasonState.TRANSITION;
    }

    public String getTimeLeftFormatted() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        SeasonState state = getSeasonState();

        if (state == SeasonState.NOT_STARTED) {
            Duration d = Duration.between(now, currentSeason.getStartDate());
            return "Starts in: " + TimeUtil.formatDaysHours(d);
        } else if (state == SeasonState.ACTIVE) {
            Duration d = Duration.between(now, currentSeason.getEndDate());
            return TimeUtil.formatDaysHours(d);
        } else if (state == SeasonState.TRANSITION) {
            Duration d = Duration.between(now, currentSeason.getTransitionEndDate());
            return "Transition ends in: " + TimeUtil.formatDaysHours(d);
        } else {
            return "Season Ended";
        }
    }

    public String getSeasonDateRangeFormatted() {
        if (currentSeason == null) return "";
        return currentSeason.getStartDate().format(TimeUtil.SHORT_DATE_FORMATTER) + " - " + currentSeason.getEndDate().format(TimeUtil.SHORT_DATE_FORMATTER);
    }

    /**
     * Calculate current active month index (1, 2, or 3) relative to the Season Start Date.
     */
    public int getCurrentMonthNumber() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if (now.isBefore(currentSeason.getStartDate())) return 1;

        // Calculate months between start of season and now
        long monthsPassed = ChronoUnit.MONTHS.between(currentSeason.getStartDate().toLocalDate().withDayOfMonth(1), now.toLocalDate().withDayOfMonth(1));
        int monthNum = (int) (monthsPassed % 3) + 1;
        return Math.max(1, Math.min(3, monthNum));
    }
}
