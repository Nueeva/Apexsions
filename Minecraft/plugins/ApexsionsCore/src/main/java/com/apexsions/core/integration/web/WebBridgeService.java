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
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
            List<Player> onlineList = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getVanishManager() == null || !plugin.getVanishManager().isVanished(p)) {
                    onlineList.add(p);
                }
            }
            int onlinePlayers = onlineList.size();
            int maxPlayers = Bukkit.getMaxPlayers();
            double tps = 20.0;
            try {
                double[] tpsArr = Bukkit.getTPS();
                if (tpsArr != null && tpsArr.length > 0) {
                    tps = Math.min(20.0, Math.round(tpsArr[0] * 10.0) / 10.0);
                }
            } catch (Throwable ignored) {
            }
            String version = Bukkit.getMinecraftVersion();

            // RAM metrics in MB
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long maxMem = Runtime.getRuntime().maxMemory();
            long ramUsedMb = Math.max(0, (totalMem - freeMem) / (1024L * 1024L));
            long ramMaxMb = Math.max(1, maxMem / (1024L * 1024L));
            long freeRamMb = Math.max(0, freeMem / (1024L * 1024L));

            // Server Uptime in seconds
            long uptimeSeconds = 0;
            try {
                uptimeSeconds = Math.max(0, (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000L);
            } catch (Throwable ignored) {
            }

            // World stats (loaded chunks and entities)
            int loadedChunks = 0;
            int totalEntities = 0;
            for (World w : Bukkit.getWorlds()) {
                loadedChunks += w.getLoadedChunks().length;
                try {
                    totalEntities += w.getEntityCount();
                } catch (Throwable t) {
                    totalEntities += w.getEntities().size();
                }
            }

            double mspt = 15.0;
            try {
                mspt = Math.round(Bukkit.getAverageTickTime() * 10.0) / 10.0;
            } catch (Throwable ignored) {
            }

            boolean maintenance = plugin.getMaintenanceManager() != null && plugin.getMaintenanceManager().isMaintenanceActive();

            StringBuilder playersJson = new StringBuilder("[");
            for (int i = 0; i < onlineList.size(); i++) {
                Player p = onlineList.get(i);
                playersJson.append(String.format(
                        "{\"name\":\"%s\",\"uuid\":\"%s\",\"ping\":%d}",
                        escapeJson(p.getName()),
                        p.getUniqueId().toString(),
                        p.getPing()
                ));
                if (i < onlineList.size() - 1) {
                    playersJson.append(",");
                }
            }
            playersJson.append("]");

            String pluginsJson = com.apexsions.core.integration.plugin.PluginIntegrationRegistry.getInstance().buildPluginsTelemetryJson();

            String jsonPayload = String.format(
                    "{\"online_players\":%d,\"max_players\":%d,\"players\":%s,\"tps\":%.1f,\"mspt\":%.1f,\"version\":\"%s\"," +
                    "\"ram_used_mb\":%d,\"ram_max_mb\":%d,\"free_ram_mb\":%d,\"uptime_seconds\":%d," +
                    "\"loaded_chunks\":%d,\"entities\":%d,\"maintenance\":%b,\"plugins\":%s}",
                    onlinePlayers, maxPlayers, playersJson.toString(), tps, mspt, version,
                    ramUsedMb, ramMaxMb, freeRamMb, uptimeSeconds,
                    loadedChunks, totalEntities, maintenance, pluginsJson.toString()
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
                                    String rawCmd = del.get("command").getAsString();
                                    String username = del.has("player_username") && !del.get("player_username").isJsonNull()
                                            ? del.get("player_username").getAsString() : null;
                                    String actionId = del.has("action_id") && !del.get("action_id").isJsonNull()
                                            ? del.get("action_id").getAsString() : null;

                                    plugin.getLogger().info("[WebBridge] Executing delivery #" + id + " (action: " + actionId + "): " + rawCmd);

                                    // Split compound commands by semicolon or newline
                                    String[] subCommands = rawCmd.split("[;\\n]+");
                                    boolean allSuccess = true;
                                    StringBuilder errCollector = new StringBuilder();

                                    for (String sub : subCommands) {
                                        String trimmed = sub.trim();
                                        if (trimmed.isEmpty()) continue;

                                        try {
                                            boolean ok = executeSingleCommand(trimmed);
                                            if (!ok) {
                                                allSuccess = false;
                                                if (errCollector.length() > 0) errCollector.append("; ");
                                                errCollector.append("Sub-command returned false: ").append(trimmed);
                                                plugin.getLogger().warning("[WebBridge] Sub-command returned false: " + trimmed);
                                            }
                                        } catch (Throwable t) {
                                            allSuccess = false;
                                            if (errCollector.length() > 0) errCollector.append("; ");
                                            errCollector.append("Sub-command error (").append(trimmed).append("): ").append(t.getMessage());
                                            plugin.getLogger().warning("[WebBridge] Sub-command exception: " + t.getMessage());
                                        }
                                    }

                                    // Report execution status back to web with action_id
                                    String errorReport = allSuccess ? null : errCollector.toString();
                                    reportDeliveryStatus(id, allSuccess ? "DELIVERED" : "FAILED", errorReport, actionId);

                                    // If command relates to an online player, re-sync their stats immediately
                                    if (username != null && !username.equalsIgnoreCase("ALL_PLAYERS") && !username.equalsIgnoreCase("GLOBAL")) {
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
     * Execute a single command or handled action safely on Bukkit main thread.
     */
    private boolean executeSingleCommand(String cmd) {
        String trimmed = cmd.trim();
        if (trimmed.isEmpty()) return true;

        if (trimmed.startsWith("broadcast ") || trimmed.startsWith("bc ")) {
            String msg = trimmed.substring(trimmed.indexOf(' ') + 1);
            net.kyori.adventure.text.Component comp = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg);
            Bukkit.broadcast(comp);
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            }
            return true;
        }

        if (trimmed.startsWith("minecraft:tellraw ") || trimmed.startsWith("tellraw ")) {
            String rest = trimmed.startsWith("minecraft:tellraw ")
                    ? trimmed.substring("minecraft:tellraw ".length()).trim()
                    : trimmed.substring("tellraw ".length()).trim();
            int firstSpace = rest.indexOf(' ');
            if (firstSpace > 0) {
                String targetName = rest.substring(0, firstSpace).trim();
                String jsonPayload = rest.substring(firstSpace + 1).trim();
                Player targetPlayer = Bukkit.getPlayerExact(targetName);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    try {
                        net.kyori.adventure.text.Component comp = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(jsonPayload);
                        targetPlayer.sendMessage(comp);
                        targetPlayer.playSound(targetPlayer.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                        return true;
                    } catch (Throwable t) {
                        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), trimmed);
                    }
                } else {
                    plugin.getLogger().info("[WebBridge] Tellraw target '" + targetName + "' is currently offline. Message acknowledged.");
                    return true;
                }
            }
        }

        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), trimmed);
    }

    /**
     * Send execution confirmation of a delivery item back to web platform.
     */
    private void reportDeliveryStatus(int deliveryId, String status, String errorMessage, String actionId) {
        String errorJson = errorMessage != null ? "\"" + escapeJson(errorMessage) + "\"" : "null";
        String actionIdJson = actionId != null ? "\"" + escapeJson(actionId) + "\"" : "null";
        String payload = String.format("{\"status\":\"%s\",\"error_message\":%s,\"action_id\":%s}", status, errorJson, actionIdJson);

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
     * Report an inbound Votifier / in-game vote packet to the web platform asynchronously.
     */
    public void reportInboundVoteAsync(String username, String service, String address, String timestamp) {
        if (!enabled) return;

        try {
            String serviceSlug = service != null ? service.toLowerCase().replaceAll("[^a-z0-9_-]", "-") : "votifier";
            String url = apiUrl + "/vote/callback/" + URLEncoder.encode(serviceSlug, StandardCharsets.UTF_8);

            JsonObject payload = new JsonObject();
            payload.addProperty("username", username);
            payload.addProperty("service", service != null ? service : "Votifier");
            payload.addProperty("address", address != null ? address : "127.0.0.1");
            payload.addProperty("timestamp", timestamp != null ? timestamp : String.valueOf(System.currentTimeMillis()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("X-Apexsions-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            plugin.getLogger().info("[WebBridge] Inbound vote for '" + username + "' synced successfully to web database.");
                        } else {
                            plugin.getLogger().log(Level.FINE, "[WebBridge] Inbound vote sync response: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.FINE, "[WebBridge] Failed to sync inbound vote: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception ex) {
            plugin.getLogger().log(Level.FINE, "[WebBridge] Inbound vote initiation error: " + ex.getMessage());
        }
    }

    /**
     * Ingest an administrative action performed in-game into the Unified Audit Log.
     */
    public CompletableFuture<Boolean> sendAuditLogAsync(
            String actorName,
            String action,
            String targetUuid,
            String targetName,
            String oldValue,
            String newValue,
            String reason,
            String status
    ) {
        if (!enabled) {
            return CompletableFuture.completedFuture(false);
        }
        try {
            String jsonPayload = String.format(
                    "{\"actor_type\":\"INGAME_ADMIN\",\"actor_name\":\"%s\",\"action\":\"%s\"," +
                    "\"target_type\":\"PLAYER\",\"target_id\":\"%s\",\"target_name\":\"%s\"," +
                    "\"old_value\":\"%s\",\"new_value\":\"%s\",\"reason\":\"%s\",\"status\":\"%s\",\"source\":\"INGAME\"}",
                    escapeJson(actorName),
                    escapeJson(action),
                    escapeJson(targetUuid != null ? targetUuid : ""),
                    escapeJson(targetName != null ? targetName : ""),
                    escapeJson(oldValue != null ? oldValue : ""),
                    escapeJson(newValue != null ? newValue : ""),
                    escapeJson(reason != null ? reason : ""),
                    escapeJson(status != null ? status : "SUCCESS")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/audit/log"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("X-Apexsions-Key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 200 || response.statusCode() == 201)
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.FINE, "[WebBridge] Failed to send in-game audit log: " + ex.getMessage());
                        return false;
                    });
        } catch (Exception ex) {
            plugin.getLogger().log(Level.FINE, "[WebBridge] sendAuditLogAsync error: " + ex.getMessage());
            return CompletableFuture.completedFuture(false);
        }
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

        // 3. Resolve Balances & BattlePass
        double balRupiah = getEconomyBalance(uuid, "rupiah", player);
        double balDiamond = getEconomyBalance(uuid, "diamond", null);
        BattlePassStats bpStats = getBattlePassStats(uuid);

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

        boolean isBedrock = com.apexsions.core.gui.input.BedrockFormAdapter.isBedrockPlayer(player);
        String edition = isBedrock ? "BEDROCK" : "JAVA";
        String authMode = isBedrock ? "BEDROCK_FLOODGATE" : "JAVA_ONLINE";

        String jsonPayload = String.format(
                Locale.ROOT,
                "{\"player_uuid\":\"%s\",\"player_username\":\"%s\",\"rank\":\"%s\",\"rank_display\":\"%s\"," +
                "\"kingdom\":\"%s\",\"kingdom_display\":\"%s\",\"level\":%d,\"xp\":%d,\"required_xp\":%d," +
                "\"level_title\":\"%s\",\"active_title\":\"%s\",\"balance_rupiah\":%.2f,\"balance_diamond\":%.2f," +
                "\"battlepass_tier\":%d,\"battlepass_xp\":%d,\"battlepass_required_xp\":%d,\"battlepass_has_premium\":%b,\"battlepass_pass_name\":\"%s\",\"apex_coins\":%d," +
                "\"is_bedrock\":%b,\"edition\":\"%s\",\"auth_mode\":\"%s\"," +
                "\"unlocked_titles\":%s}",
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
                bpStats.tier(),
                bpStats.xp(),
                bpStats.requiredXp(),
                bpStats.hasPremium(),
                escapeJson(bpStats.passName()),
                bpStats.apexCoins(),
                isBedrock,
                edition,
                authMode,
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

    private double getEconomyBalance(UUID uuid, String currencyId, Player fallbackPlayer) {
        double balance = 0.0;
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsEconomy")) {
                Class<?> providerClass = Class.forName("com.apexsions.economy.api.ApexsionsEconomyProvider");
                Object api = providerClass.getMethod("get").invoke(null);
                if (api != null) {
                    balance = (double) api.getClass().getMethod("getBalance", UUID.class, String.class).invoke(api, uuid, currencyId.toLowerCase());
                }
            }
        } catch (Throwable ignored) {}

        // Fallback to Vault for rupiah if economy balance is 0 and Vault has positive balance
        if (balance <= 0.0 && currencyId.equalsIgnoreCase("rupiah") && fallbackPlayer != null && plugin.getVaultHook() != null && plugin.getVaultHook().hasEconomy()) {
            try {
                double vaultBal = plugin.getVaultHook().getBalance(fallbackPlayer);
                if (vaultBal > 0.0) {
                    balance = vaultBal;
                }
            } catch (Throwable ignored) {}
        }
        return balance;
    }

    private record BattlePassStats(int tier, int xp, int requiredXp, boolean hasPremium, String passName, int apexCoins) {}

    private BattlePassStats getBattlePassStats(UUID uuid) {
        int tier = 1;
        int xp = 0;
        int requiredXp = 100;
        boolean hasPremium = false;
        String passName = "Citizen Pass";
        int apexCoins = 0;

        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ApexsionsBattlepass")) {
                Class<?> bpProviderClass = Class.forName("com.apexsions.battlepass.api.ApexsionsBattlepassProvider");
                Object bpApi = bpProviderClass.getMethod("get").invoke(null);
                if (bpApi != null) {
                    tier = (int) bpApi.getClass().getMethod("getPlayerTier", UUID.class).invoke(bpApi, uuid);
                    xp = (int) bpApi.getClass().getMethod("getPlayerXp", UUID.class).invoke(bpApi, uuid);
                    hasPremium = (boolean) bpApi.getClass().getMethod("hasPremiumPass", UUID.class).invoke(bpApi, uuid);
                    apexCoins = (int) bpApi.getClass().getMethod("getPlayerPoints", UUID.class).invoke(bpApi, uuid);

                    try {
                        passName = (String) bpApi.getClass().getMethod("getPlayerHighestPassDisplayName", UUID.class).invoke(bpApi, uuid);
                    } catch (Throwable t) {
                        try {
                            if ((boolean) bpApi.getClass().getMethod("hasPass", UUID.class, String.class).invoke(bpApi, uuid, "exsio")) {
                                passName = "Exsio Pass";
                            } else if ((boolean) bpApi.getClass().getMethod("hasPass", UUID.class, String.class).invoke(bpApi, uuid, "sio")) {
                                passName = "Sio Pass";
                            } else if (hasPremium) {
                                passName = "Premium Pass";
                            }
                        } catch (Throwable ignored) {}
                    }
                }

                org.bukkit.plugin.Plugin bpPlugin = Bukkit.getPluginManager().getPlugin("ApexsionsBattlepass");
                if (bpPlugin != null) {
                    try {
                        Object rewardMgr = bpPlugin.getClass().getMethod("getRewardManager").invoke(bpPlugin);
                        if (rewardMgr != null) {
                            requiredXp = (int) rewardMgr.getClass().getMethod("getRequiredXp", int.class).invoke(rewardMgr, tier);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}

        if (apexCoins <= 0) {
            double ecoCoins = getEconomyBalance(uuid, "apex_coins", null);
            if (ecoCoins <= 0) {
                ecoCoins = getEconomyBalance(uuid, "coins", null);
            }
            if (ecoCoins > 0) {
                apexCoins = (int) ecoCoins;
            }
        }

        return new BattlePassStats(tier, xp, requiredXp, hasPremium, passName, apexCoins);
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

