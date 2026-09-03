package com.apexsions.media.creator.model;

import java.util.List;

public class CreatorTier {

    private final String id;
    private final String displayName;
    private final long minViews;
    private final long minLikes;
    private final List<String> rewards;
    private final List<String> perksDescription;

    public CreatorTier(String id, String displayName, long minViews, long minLikes,
                       List<String> rewards, List<String> perksDescription) {
        this.id = id;
        this.displayName = displayName;
        this.minViews = minViews;
        this.minLikes = minLikes;
        this.rewards = rewards;
        this.perksDescription = perksDescription;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getMinViews() {
        return minViews;
    }

    public long getMinLikes() {
        return minLikes;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public List<String> getPerksDescription() {
        return perksDescription;
    }

    public boolean matches(long views, long likes) {
        return views >= minViews && likes >= minLikes;
    }
}
