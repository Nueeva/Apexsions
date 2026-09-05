package com.apexsions.media.creator.model;

public enum Platform {
    YOUTUBE("YouTube", "<red><bold>YouTube</bold></red>", "https://youtube.com/"),
    TIKTOK("TikTok", "<light_purple><bold>TikTok</bold></light_purple>", "https://tiktok.com/");

    private final String displayName;
    private final String miniMessageTag;
    private final String baseUrl;

    Platform(String displayName, String miniMessageTag, String baseUrl) {
        this.displayName = displayName;
        this.miniMessageTag = miniMessageTag;
        this.baseUrl = baseUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMiniMessageTag() {
        return miniMessageTag;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
