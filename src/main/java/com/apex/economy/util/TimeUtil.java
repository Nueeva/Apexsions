package com.apex.economy.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy");

    public static ZonedDateTime parseDateTime(String dateStr, ZoneId zoneId) {
        if (dateStr == null || dateStr.isBlank()) {
            return ZonedDateTime.now(zoneId);
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DATE_FORMATTER);
            return localDateTime.atZone(zoneId);
        } catch (DateTimeParseException e) {
            return ZonedDateTime.now(zoneId);
        }
    }

    public static String formatDuration(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return "0s";
        }
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static String formatDaysHours(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return "0h 0m";
        }
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            return days + "d " + hours + "h";
        } else {
            return hours + "h " + minutes + "m";
        }
    }

    public static String formatHoursMinutes(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            return "00h 00m";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02dh %02dm", hours, minutes);
    }
}
