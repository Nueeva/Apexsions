package com.apexsions.core.integration.web;

import com.apexsions.core.ApexsionsCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;

/**
 * Service to sync server status, online players, and health to the Apexsions Web Platform.
 * Non-blocking asynchronous heartbeat daemon.
 */
public class WebBridgeService {

    private final ApexsionsCorePlugin plugin;
    private final HttpClient httpClient;
    private BukkitTask heartbeatTask;

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
        plugin.getLogger().info("[WebBridge] Web bridge heartbeat scheduled to: " + apiUrl);
    }

    public void stop() {
        if (heartbeatTask != null && !heartbeatTask.isCancelled()) {
            heartbeatTask.cancel();
            heartbeatTask = null;
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
                playersJson.append("\"").append(playerNames.get(i).replace("\"", "\\\"")).append("\"");
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
}
