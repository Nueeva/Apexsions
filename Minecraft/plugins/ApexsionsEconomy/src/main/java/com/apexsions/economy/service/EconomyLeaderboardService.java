package com.apexsions.economy.service;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.leaderboard.EconomyLeaderboardEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyLeaderboardService {

    private final ApexsionsEconomy plugin;
    private final Map<String, List<EconomyLeaderboardEntry>> cachedLeaderboards = new ConcurrentHashMap<>();
    private final Map<String, Long> lastCacheTimes = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 60_000L; // 1 minute cache

    public EconomyLeaderboardService(ApexsionsEconomy plugin) {
        this.plugin = plugin;
    }

    public List<EconomyLeaderboardEntry> getLeaderboard(String currencyId) {
        currencyId = currencyId.toLowerCase();
        long now = System.currentTimeMillis();
        Long lastTime = lastCacheTimes.get(currencyId);

        if (lastTime == null || now - lastTime > CACHE_DURATION_MS || !cachedLeaderboards.containsKey(currencyId)) {
            refreshLeaderboard(currencyId);
        }

        return cachedLeaderboards.getOrDefault(currencyId, Collections.emptyList());
    }

    public synchronized void refreshLeaderboard(String currencyId) {
        currencyId = currencyId.toLowerCase();
        try {
            List<EconomyLeaderboardEntry> list = new ArrayList<>(plugin.getRepository().loadTopBalances(currencyId, 100).get());
            Set<UUID> existingUuids = new HashSet<>();
            for (EconomyLeaderboardEntry e : list) {
                existingUuids.add(e.getUuid());
            }

            double defaultStarting = plugin.getCurrencyService().getStartingBalance(currencyId);

            // Include offline players
            for (org.bukkit.OfflinePlayer op : org.bukkit.Bukkit.getOfflinePlayers()) {
                if (op.getUniqueId() != null && !existingUuids.contains(op.getUniqueId())) {
                    plugin.getRepository().saveBalance(op.getUniqueId(), currencyId, defaultStarting);
                    list.add(new EconomyLeaderboardEntry(0, op.getUniqueId(), currencyId, defaultStarting));
                    existingUuids.add(op.getUniqueId());
                }
            }

            // Include online players
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (!existingUuids.contains(p.getUniqueId())) {
                    plugin.getRepository().saveBalance(p.getUniqueId(), currencyId, defaultStarting);
                    list.add(new EconomyLeaderboardEntry(0, p.getUniqueId(), currencyId, defaultStarting));
                    existingUuids.add(p.getUniqueId());
                }
            }

            // Sort: balance DESC, UUID ASC
            list.sort((a, b) -> {
                int cmp = Double.compare(b.getBalance(), a.getBalance());
                if (cmp != 0) return cmp;
                return a.getUuid().compareTo(b.getUuid());
            });

            // Re-rank 1..100 (excluding server staff / admins)
            List<EconomyLeaderboardEntry> ranked = new ArrayList<>();
            int r = 1;
            for (EconomyLeaderboardEntry entry : list) {
                if (isLeaderboardExempt(entry.getUuid())) {
                    continue;
                }
                if (r > 100) break;
                ranked.add(new EconomyLeaderboardEntry(r++, entry.getUuid(), entry.getCurrencyId(), entry.getBalance()));
            }

            cachedLeaderboards.put(currencyId, ranked);
            lastCacheTimes.put(currencyId, System.currentTimeMillis());
        } catch (Exception e) {
            plugin.getLogger().warning("Error refreshing economy leaderboard: " + e.getMessage());
        }
    }

    public int getPlayerRank(UUID uuid, String currencyId) {
        if (isLeaderboardExempt(uuid)) {
            return -1;
        }
        List<EconomyLeaderboardEntry> list = getLeaderboard(currencyId);
        for (EconomyLeaderboardEntry entry : list) {
            if (entry.getUuid().equals(uuid)) {
                return entry.getRank();
            }
        }
        return -1;
    }

    /**
     * Checks if a player is server staff, admin, OP, or exempt from public leaderboards.
     */
    public boolean isLeaderboardExempt(UUID uuid) {
        if (uuid == null) return false;

        // 1. Authoritative check via ApexsionsCore API
        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ApexsionsCore")) {
            try {
                if (com.apexsions.core.api.ApexsionsCoreProvider.isAvailable()) {
                    return com.apexsions.core.api.ApexsionsCoreProvider.get().isLeaderboardExempt(uuid);
                }
            } catch (Throwable ignored) {}
        }

        // 2. Fallback: Bukkit OP
        org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        if (op != null && op.isOp()) {
            return true;
        }

        // 3. Fallback: Online player permissions
        org.bukkit.entity.Player onlineP = org.bukkit.Bukkit.getPlayer(uuid);
        if (onlineP != null && (onlineP.isOp() 
                || onlineP.hasPermission("apexsions.admin") 
                || onlineP.hasPermission("apexsions.staff") 
                || onlineP.hasPermission("apexsions.leaderboard.exempt"))) {
            return true;
        }

        return false;
    }
}
