package com.apexsions.media.creator.model;

import java.util.List;

public record VideoValidationResult(
        boolean success,
        String errorMessage,
        Platform platform,
        String videoId,
        String videoUrl,
        String authorOrChannelId,
        String authorName,
        String title,
        long views,
        long likes,
        long publishedAt,
        boolean hasRequiredHashtag,
        List<String> foundHashtags
) {
    public static VideoValidationResult failure(String errorMessage) {
        return new VideoValidationResult(false, errorMessage, null, null, null, null, null, null, 0, 0, 0, false, List.of());
    }

    public static VideoValidationResult success(Platform platform, String videoId, String videoUrl,
                                                String authorOrChannelId, String authorName, String title,
                                                long views, long likes, long publishedAt,
                                                boolean hasRequiredHashtag, List<String> foundHashtags) {
        return new VideoValidationResult(true, null, platform, videoId, videoUrl, authorOrChannelId, authorName, title, views, likes, publishedAt, hasRequiredHashtag, foundHashtags);
    }
}
