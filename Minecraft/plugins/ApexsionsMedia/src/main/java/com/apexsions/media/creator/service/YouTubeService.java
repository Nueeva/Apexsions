package com.apexsions.media.creator.service;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.model.Platform;
import com.apexsions.media.creator.model.VideoValidationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeService {

    private final ApexsionsMediaPlugin plugin;
    private final HttpClient httpClient;

    private static final Pattern YT_URL_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?v=|shorts/|embed/|v/)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
    );

    public YouTubeService(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String extractVideoId(String input) {
        if (input == null || input.isBlank()) return null;
        input = input.trim();
        Matcher matcher = YT_URL_PATTERN.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (input.matches("^[a-zA-Z0-9_-]{11}$")) {
            return input;
        }
        return null;
    }

    public CompletableFuture<VideoValidationResult> validateVideo(String videoUrlOrId, List<String> requiredHashtags) {
        String videoId = extractVideoId(videoUrlOrId);
        if (videoId == null) {
            return CompletableFuture.completedFuture(VideoValidationResult.failure("Format URL YouTube tidak valid! Contoh: https://youtu.be/XXXX atau https://youtube.com/watch?v=XXXX"));
        }

        String apiKey = plugin.getConfig().getString("creator.youtube.api-key", "").trim();
        if (apiKey.isEmpty() || apiKey.equalsIgnoreCase("YOUR_GOOGLE_CLOUD_API_KEY")) {
            return CompletableFuture.completedFuture(VideoValidationResult.failure("YouTube API Key belum dikonfigurasi oleh administrator di config.yml!"));
        }

        String apiUrl = "https://www.googleapis.com/youtube/v3/videos?part=snippet,statistics&id=" + videoId + "&key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "ApexsionsMedia-CreatorSuite/1.0")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return VideoValidationResult.failure("Gagal menghubungi YouTube API (Status Code: " + response.statusCode() + "). Periksa API Key di config!");
                    }

                    try {
                        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                        JsonArray items = root.getAsJsonArray("items");
                        if (items == null || items.isEmpty()) {
                            return VideoValidationResult.failure("Video YouTube dengan ID " + videoId + " tidak ditemukan atau bersifat pribadi (Private/Unlisted).");
                        }

                        JsonObject item = items.get(0).getAsJsonObject();
                        JsonObject snippet = item.getAsJsonObject("snippet");
                        JsonObject statistics = item.getAsJsonObject("statistics");

                        String channelId = snippet.has("channelId") ? snippet.get("channelId").getAsString() : "";
                        String channelTitle = snippet.has("channelTitle") ? snippet.get("channelTitle").getAsString() : "";
                        String title = snippet.has("title") ? snippet.get("title").getAsString() : "";
                        String description = snippet.has("description") ? snippet.get("description").getAsString() : "";
                        String publishedAtStr = snippet.has("publishedAt") ? snippet.get("publishedAt").getAsString() : "";

                        long publishedAt = Instant.now().toEpochMilli();
                        try {
                            if (!publishedAtStr.isBlank()) {
                                publishedAt = Instant.parse(publishedAtStr).toEpochMilli();
                            }
                        } catch (Exception ignored) {}

                        long views = statistics.has("viewCount") ? statistics.get("viewCount").getAsLong() : 0L;
                        long likes = statistics.has("likeCount") ? statistics.get("likeCount").getAsLong() : 0L;

                        List<String> foundHashtags = new ArrayList<>();
                        String fullText = (title + " " + description).toLowerCase();

                        if (snippet.has("tags")) {
                            for (JsonElement t : snippet.getAsJsonArray("tags")) {
                                fullText += " " + t.getAsString().toLowerCase();
                            }
                        }

                        boolean hasHashtag = false;
                        if (requiredHashtags == null || requiredHashtags.isEmpty()) {
                            hasHashtag = true;
                        } else {
                            for (String tag : requiredHashtags) {
                                String cleanTag = tag.trim().toLowerCase();
                                if (fullText.contains(cleanTag) || fullText.contains(cleanTag.replace("#", ""))) {
                                    hasHashtag = true;
                                    foundHashtags.add(tag);
                                }
                            }
                        }

                        String canonicalUrl = "https://www.youtube.com/watch?v= " + videoId;
                        return VideoValidationResult.success(
                                Platform.YOUTUBE,
                                videoId,
                                canonicalUrl.trim(),
                                channelId,
                                channelTitle,
                                title,
                                views,
                                likes,
                                publishedAt,
                                hasHashtag,
                                foundHashtags
                        );
                    } catch (Exception e) {
                        return VideoValidationResult.failure("Terjadi kesalahan saat memproses data YouTube: " + e.getMessage());
                    }
                })
                .exceptionally(ex -> VideoValidationResult.failure("Gagal menghubungi server YouTube: " + ex.getMessage()));
    }

    public CompletableFuture<Boolean> verifyChannelOwnership(String channelIdOrHandle, String verifyCode) {
        String apiKey = plugin.getConfig().getString("creator.youtube.api-key", "").trim();
        if (apiKey.isEmpty() || apiKey.equalsIgnoreCase("YOUR_GOOGLE_CLOUD_API_KEY")) {
            return CompletableFuture.completedFuture(false);
        }

        String clean = channelIdOrHandle.trim();
        String url;
        if (clean.startsWith("@") || !clean.startsWith("UC")) {
            String handle = clean.startsWith("@") ? clean.substring(1) : clean;
            url = "https://www.googleapis.com/youtube/v3/channels?part=snippet&forHandle=" + handle + "&key=" + apiKey;
        } else {
            url = "https://www.googleapis.com/youtube/v3/channels?part=snippet&id=" + clean + "&key=" + apiKey;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "ApexsionsMedia-CreatorSuite/1.0")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) return false;
                    try {
                        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                        JsonArray items = root.getAsJsonArray("items");
                        if (items == null || items.isEmpty()) return false;

                        JsonObject snippet = items.get(0).getAsJsonObject().getAsJsonObject("snippet");
                        String description = snippet.has("description") ? snippet.get("description").getAsString() : "";
                        return description.contains(verifyCode);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .exceptionally(ex -> false);
    }
}
