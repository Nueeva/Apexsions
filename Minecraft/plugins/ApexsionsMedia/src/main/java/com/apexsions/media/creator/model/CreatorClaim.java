package com.apexsions.media.creator.model;

import java.util.UUID;

public class CreatorClaim {

    private final String claimId;
    private final UUID uuid;
    private final Platform platform;
    private final String videoId;
    private final String videoUrl;
    private final long views;
    private final long likes;
    private final String tierId;
    private final long claimedAt;

    public CreatorClaim(String claimId, UUID uuid, Platform platform, String videoId,
                        String videoUrl, long views, long likes, String tierId, long claimedAt) {
        this.claimId = claimId;
        this.uuid = uuid;
        this.platform = platform;
        this.videoId = videoId;
        this.videoUrl = videoUrl;
        this.views = views;
        this.likes = likes;
        this.tierId = tierId;
        this.claimedAt = claimedAt;
    }

    public String getClaimId() {
        return claimId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public long getViews() {
        return views;
    }

    public long getLikes() {
        return likes;
    }

    public String getTierId() {
        return tierId;
    }

    public long getClaimedAt() {
        return claimedAt;
    }
}
