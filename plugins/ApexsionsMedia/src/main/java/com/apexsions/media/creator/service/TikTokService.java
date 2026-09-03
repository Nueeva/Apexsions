package com.apexsions.media.creator.service;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.model.Platform;
import com.apexsions.media.creator.model.VideoValidationResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TikTokService {

    private final ApexsionsMediaPlugin plugin;
    private final HttpClient httpClient;

    private static final Pattern TIKTOK_URL_PATTERN = Pattern.compile(
            "https?://(?:www\\.|vt\\.|vm\\.|m\\.)?tiktok\\.com/(?:@([a-zA-Z0-9_.-]+)/video/([0-9]+)|([a-zA-Z0-9_-]+))"
    );

    public TikTokService(ApexsionsMediaPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isTikTokUrl(String input) {
        if (input == null || input.isBlank()) return false;
        return input.toLowerCase().contains("tiktok.com");
    }

    public CompletableFuture<VideoValidationResult> validateVideo(String videoUrl, List<String> requiredHashtags) {
        if (!isTikTokUrl(videoUrl)) {
            return CompletableFuture.completedFuture(VideoValidationResult.failure("URL bukan tautan video TikTok yang valid! Contoh: https://vt.tiktok.com/xxx atau https://tiktok.com/@user/video/xxx"));
        }

        String encodedUrl = URLEncoder.encode(videoUrl.trim(), StandardCharsets.UTF_8);
        String apiUrl = "https://www.tikwm.com/api/?url=" + encodedUrl;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return VideoValidationResult.failure("Gagal menghubungi server pemeriksa TikTok (HTTP " + response.statusCode() + ").");
                    }

                    try {
                        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                        int code = root.has("code") ? root.get("code").getAsInt() : -1;
                        if (code != 0 || !root.has("data")) {
                            String msg = root.has("msg") ? root.get("msg").getAsString() : "Video tidak ditemukan atau bersifat privat.";
                            return VideoValidationResult.failure("TikTok Error: " + msg);
                        }

                        JsonObject data = root.getAsJsonObject("data");
                        String videoId = data.has("id") ? data.get("id").getAsString() : "";
                        String title = data.has("title") ? data.get("title").getAsString() : "";
                        long playCount = data.has("play_count") ? data.get("play_count").getAsLong() : 0L;
                        long diggCount = data.has("digg_count") ? data.get("digg_count").getAsLong() : 0L;
                        long createTime = data.has("create_time") ? data.get("create_time").getAsLong() * 1000L : System.currentTimeMillis();

                        String authorUniqueId = "";
                        String authorNickname = "";
                        if (data.has("author") && data.get("author").isJsonObject()) {
                            JsonObject author = data.getAsJsonObject("author");
                            authorUniqueId = author.has("unique_id") ? author.get("unique_id").getAsString() : "";
                            authorNickname = author.has("nickname") ? author.get("nickname").getAsString() : "";
                        }

                        List<String> foundHashtags = new ArrayList<>();
                        String fullText = title.toLowerCase();

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

                        return VideoValidationResult.success(
                                Platform.TIKTOK,
                                videoId,
                                videoUrl.trim(),
                                authorUniqueId,
                                authorNickname,
                                title,
                                playCount,
                                diggCount,
                                createTime,
                                hasHashtag,
                                foundHashtags
                        );
                    } catch (Exception e) {
                        return VideoValidationResult.failure("Gagal memproses respons metadata TikTok: " + e.getMessage());
                    }
                })
                .exceptionally(ex -> VideoValidationResult.failure("Gagal menghubungi server TikTok: " + ex.getMessage()));
    }
}
