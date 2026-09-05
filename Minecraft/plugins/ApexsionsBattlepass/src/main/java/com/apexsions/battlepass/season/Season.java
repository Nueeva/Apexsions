package com.apexsions.battlepass.season;

import java.time.ZonedDateTime;

public class Season {

    private final int id;
    private final String name;
    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;

    public Season(int id, String name, ZonedDateTime startDate, ZonedDateTime endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public ZonedDateTime getTransitionEndDate() {
        return endDate.plusDays(1); // 1-day transition period
    }
}
