package com.apexsions.core.pose;

public enum PlayerPoseType {
    NONE("Berdiri"),
    SITTING("Duduk"),
    LAYING("Tiduran"),
    CRAWLING("Merangkak"),
    BELLYFLOP("Tengkurap"),
    SPINNING("Berputar");

    private final String displayName;

    PlayerPoseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
