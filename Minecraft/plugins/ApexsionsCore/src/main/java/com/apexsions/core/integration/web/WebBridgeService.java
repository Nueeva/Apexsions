package com.apexsions.core.integration.web;

import com.apexsions.core.ApexsionsCorePlugin;
import com.apexsions.core.player.PlayerData;
import com.apexsions.core.region.Region;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Service to sync server status, player progression/stats, and execute web delivery queue
 * with the Apexsions Web Platform.
 * Non-blocking asynchronous bridge daemon.
 */
public class WebBridgeService {

    private final ApexsionsCorePlugin plugin;
    private final HttpClient httpClient;
    private BukkitTask heartbeatTask;
    private BukkitTask deliveryTask;
    private BukkitTask playerSyncTask;

    private String apiUrl;
    private String apiKey;
    private boolean enabled;

    public WebBridgeService(ApexsionsCorePlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void start() {
        // Load settings with sensible fallbacks
        this.apiUrl = plugin.getConfig().getString("web-bridge.api-url", "http://127.0.0.1:8000/api/apexsions-bridge");
        this.apiKey = plugin.getConfig().getString("web-bridge.api-key", "apexsions_bridge_key_live_2026");
        this.enabled = plugin.getConfig().getBoolean("web-bridge.enabled", true);

        if (!enabled) {
            plugin.getLogger().info("[WebBridge] Web bridge synchronization is disabled in config.");
            return;
        }

        // Schedule async heartbeat every 30 seconds (600 ticks)
        this.heartbeatTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::sendHeartbeat, 60L, 600L);

        // Schedule async delivery queue polling every 10 seconds (200 ticks)
        this.deliveryTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::pollDeliveries, 100L, 200L);

        // Schedule periodic player synchronization for all online players every 30 seconds (600 ticks)
        this.playerSyncTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::syncAllOnlinePlayers, 120L, 600L);

        plugin.getLogger().info("[WebBridge] Web bridge daemon active. Heartbeat & Delivery polling scheduled to: " + apiUrl);
    }

    public void stop() {
        if (heartbeatTask != null && !heartbeatTask.isCancelled()) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
        if (deliveryTask != null && !deliveryTask.isCancelled()) {
            deliveryTask.cancel();
            deliveryTask = null;
        }
        if (playerSyncTask != null && !playerSyncTask.isCancelled()) {
            playerSyncTask.cancel();
            playerSyncTask = null;
        }
    }

    private void sendHeartbeat() {
        try {
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            int maxPlayers = Bukkit.getMaxPlayers();
            List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            double tps = 20.0;
            try {
                double[] tpsArr = Bukkit.getTPS();
                if (tpsArr != null && tpsArr.length > 0) {
                    tps = Math.min(20.0, Math.round(tpsArr[0] * 10.0) / 10.0);
                }
            } catch (Throwable ignored) {
            }
            String version = Bukkit.getMinecraftVersion();

            StringBuilder playersJson = new StringBuilder("[");
            for (int i = 0; i < playerNames.size(); i++) {
                playersJson.append("\"").append(escapeJson(playerNames.get(i))).append("\"");
                if (i < playerNames.size() - 1) {
                    playersJson.append(",");
                }
            }
            playersJson.append("]");

            String jsonPayload = String.format(
                    "{\"online_players\":%d,\"max_players\":%d,\"players\":%s,\"tps\":%.1f,\"version\":\"%s\"}",
                    onlinePlayers, maxPlayers, playersJson.toString(), tps, version
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/heartbeat"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("X-Apexsions-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            plugin.getLogger().log(Level.FINE, "[WebBridge] Heartbeat returned status: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.FINE, "[WebBridge] Unable to connect to web platform: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception ex) {
            plugin.getLogger().log(Level.FINE, "[WebBridge] Heartbeat generation error: " + ex.getMessage());
        }
    }

    /**
     * Poll pending command deliveries from the web platform.
     * Non-blocking HTTP GET followed by main-thread console execution.
     */
    private void pollDeliveries() {
        if (!enabled) return;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/deliveries/pending"))
                    .timeout(Duration.ofSeconds(6))
                    .header("Accept", "application/json")
                    .header("X-Apexsions-Key", apiKey)
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            return;
                        }

                        try {
                            JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                            if (!obj.has("deliveries") || !obj.get("deliveries").isJsonArray()) {
                                return;
                            }

                            JsonArray deliveries = obj.getAsJsonArray("deliveries");
                            if (deliveries.isEmpty()) {
                                return;
                            }

                            // Dispatch commands safely on the Bukkit Main Thread
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                for (JsonElement elem : deliveries) {
                                    if (!elem.isJsonObject()) continue;
                                    JsonObject del = elem.getAsJsonObject();

                                    int id = del.get("id").getAsInt();
                                    String cmd = del.get("command").getAsString();
                                    String username = del.has("player_username") && !del.get("player_username").isJsonNull()
                                            ? del.get("player_username").getAsString() : null;

                                    boolean success = false;
                                    String error = null;

                                    try {
                                        plugin.getLogger().info("[WebBridge] Executing delivery #" + id + ": " + cmd);
                                        success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                    } catch (Throwable t) {
                                        error = t.getMessage();
                                        plugin.getLogger().warning("[WebBridge] Error executing delivery #" + id + ": " + error);
                                    }

                                    // Report execution status back to web
                                    reportDeliveryStatus(id, success ? "DELIVERED" : "FAILED", error);

                                    // If command relates to an online player, re-sync their stats immediately
                                    if (username != null) {
                                        Player target = Bukkit.getPlayerExact(username);
                                        if (target != null && target.isOnline()) {
                                            syncPlayerAsync(target);
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.FINE, "[WebBridge] Failed to parse pending deliveries: " + e.getMessage());
                        }
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.FINE, "[WebBridge] Delivery poll error: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception ex) {
            plugin.getLogger().log(Level.FINE, "[WebBridge] Poll deliveries initiation error: " + ex.getMessage());
        }
    }

    /**
     * Send execution confirmation of a delivery item back to web platform.
     */
    private void reportDeliveryStatus(int deliveryId, String status, String errorMessage) {
        String errorJson = errorMessage != null ? "\"" + escapeJson(errorMessage) + "\"" : "null";
        String payload = String.format("{\"status\":\"%s\",\"error_message\":%s}", status, errorJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/deliveries/" + deliveryId + "/status"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Apexsions-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.FINE, "[WebBridge] Failed to report delivery status #" + deliveryId + ": " + ex.getMessage());
                    return null;
                });
    }

    /**
     * Periodically synchronize all currently online players' stats to the web platform.
     */
    public void syncAllOnlinePlayers() {
        if (!enabled) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != null && p.isOnline()) {
                syncPlayerAsync(p);
            }
        }
    }

    /**
     * Synchronize a player's complete in-game statistics (rank, level, xp, kingdom, titles, balances)
     * to the Apexsions Web Platform asynchronously.
     */
    public void syncPlayerAsync(Player player) {
        if (!enabled || player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        String username = player.getName();

        // 1. Resolve Rank & Display
        String rankKey = "wanderer";
        String rankDisplay = "Wanderer";
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isAvailable()) {
            rankKey = plugin.getLuckPermsHook().getPlayerRankKey(player);
            rankDisplay = plugin.getLuckPermsHook().getPlayerRankDisplayName(player);
        } else if (player.isOp()) {
            rankKey = "ancestor";
            rankDisplay = "Ancestor";
        }

        // 2. Resolve PlayerData (Level, XP, Titles, Kingdom)
        PlayerData data = plugin.getPlayerDataService().getCached(uuid).orElse(null);
        int level = data != null ? data.getLevel() : 1;
        long xp = data != null ? data.getXp() : 0;
        long reqXp = plugin.getLevelManager() != null ? plugin.getLevelManager().getRequiredXpForNextLevel(level) : 100;
        String levelTitle = plugin.getLevelManager() != null ? plugin.getLevelManager().getLevelTitle(uuid) : "Citizen";
        String activeTitle = (data != null && data.getActiveTitle() != null) ? data.getActiveTitle() : "";

        levelTitle = cleanMiniMessageTags(levelTitle);
        if (levelTitle.isBlank()) {
            levelTitle = "Citizen";
        }
        activeTitle = cleanMiniMessageTags(activeTitle);

        String kingdomKey = "NONE";
        String kingdomDisplay = "Belum Memilih";
        if (data != null && data.hasRegion()) {
            Optional<Region> regOpt = plugin.getRegionManager().getRegion(data.getRegionId());
            if (regOpt.isPresent()) {
                kingdomKey = regOpt.get().getKey();
                kingdomDisplay = regOpt.get().getDisplayName();
            }
        }

        // 3. Resolve Balances
        double balRupiah = 0.0;
        if (plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
            try {
                balRupiah = plugin.getVaultHook().getBalance(player);
            } catch (Throwable ignored) {}
        }
        double balDiamond = getDiamondBalance(uuid);

        // 4. Resolve Unlocked Titles Array
        StringBuilder titlesJson = new StringBuilder("[");
        if (data != null && !data.getUnlockedTitles().isEmpty()) {
            List<String> list = new ArrayList<>(data.getUnlockedTitles());
            for (int i = 0; i < list.size(); i++) {
                titlesJson.append("\"").append(escapeJson(list.get(i))).append("\"");
                if (i < list.size() - 1) {
                    titlesJson.append(",");
                }
            }
        }
        titlesJson.append("]");

        String jsonPayload = String.format(
                Locale.ROOT,
                "{\"player_uuid\":\"%s\",\"player_username\":\"%s\",\"rank\":\"%s\",\"rank_display\":\"%s\"," +
                "\"kingdom\":\"%s\",\"kingdom_display\":\"%s\",\"level\":%d,\"xp\":%d,\"required_xp\":%d," +
                "\"level_title\":\"%s\",\"active_title\":\"%s\",\"balance_rupiah\":%.2f,\"balance_diamond\":%.2f,\"unlocked_titles\":%s}",
                escapeJson(uuid.toString()),
                escapeJson(username),
                escapeJson(rankKey),
                escapeJson(rankDisplay),
                escapeJson(kingdomKey),
                escapeJson(kingdomDisplay),
                level,
                xp,
                reqXp,
                escapeJson(levelTitle),
                escapeJson(activeTitle),
                balRupiah,
                balDiamond,
                titlesJson.toString()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/sync-player"))
                .timeout(Duration.ofSeconds(6))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Apexsions-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        plugin.getLogger().log(Level.FINE, "[WebBridge] Successfully synced player stats for: " + username);
                    } else {
                        plugin.getLogger().log(Level.FINE, "[WebBridge] Player sync responded with status: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.FINE, "[WebBridge] Player sync network error: " + ex.getMessage());
                    return null;
                });
    }

    private double getDiamondBalance(UUID uuid) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
                Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
                Object api = providerClass.getMethod("get").invoke(null);
                if (api != null) {
                    return (double) api.getClass().getMethod("getBalance", UUID.class, String.class).invoke(api, uuid, "diamond");
                }
            }
        } catch (Throwable ignored) {}
        return 0.0;
    }

    public CompletableFuture<LinkResult> verifyLink(Player player, String pin) {
        if (!enabled) {
            return CompletableFuture.completedFuture(new LinkResult(false, "Sistem Web Bridge sedang dinonaktifkan di server."));
        }

        boolean isBedrock = com.apexsions.core.gui.input.BedrockFormAdapter.isBedrockPlayer(player);
        String uuidStr = player.getUniqueId().toString();
        String username = player.getName();

        String jsonPayload = String.format(
                "{\"pin\":\"%s\",\"player_uuid\":\"%s\",\"player_username\":\"%s\",\"is_bedrock\":%b}",
                escapeJson(pin), escapeJson(uuidStr), escapeJson(username), isBedrock
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/verify"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Apexsions-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int code = response.statusCode();
                    String body = response.body();
                    try {
                        JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                        String status = obj.has("status") ? obj.get("status").getAsString() : "";
                        String message = obj.has("message") ? obj.get("message").getAsString() : "Respons tidak dikenal dari server web.";
                        boolean success = "success".equalsIgnoreCase(status) && (code == 200 || code == 201);
                        return new LinkResult(success, message);
                    } catch (Exception e) {
                        if (code == 200) {
                            return new LinkResult(true, "Akun berhasil ditautkan!");
                        }
                        return new LinkResult(false, "Gagal memproses verifikasi dari web platform (Status HTTP: " + code + ").");
                    }
                })
                .exceptionally(ex -> new LinkResult(false, "Tidak dapat menghubungi server web Apexsions: " + ex.getMessage()));
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String cleanMiniMessageTags(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        try {
            return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().stripTags(text).trim();
        } catch (Throwable t) {
            return text.replaceAll("<[^>]*>", "").trim();
        }
    }

    public record LinkResult(boolean success, String message) {}
}

