package com.apex.battlepass.leaderboard;

import com.apex.battlepass.ApexsionsBattlepass;
import com.apex.battlepass.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BattlePassLeaderboardService {

    private final ApexsionsBattlepass plugin;
    private List<LeaderboardEntry> cachedEntries = new ArrayList<>();
    private final Map<UUID, Integer> rankMap = new ConcurrentHashMap<>();
    private long lastUpdate = 0;
    private static final long CACHE_DURATION_MS = 15000L; // 15s cache

    public BattlePassLeaderboardService(ApexsionsBattlepass plugin) {
        this.plugin = plugin;
        refreshLeaderboard();
    }

    public synchronized void refreshLeaderboard() {
        if (plugin.getSeasonManager() == null || plugin.getSeasonManager().getCurrentSeason() == null || plugin.getRepository() == null) {
            return;
        }
        int seasonId = plugin.getSeasonManager().getCurrentSeason().getId();
        plugin.getRepository().loadAllPlayerData(seasonId).thenAccept(allData -> {
            // Also merge any online player memory data
            Map<UUID, PlayerData> merged = new HashMap<>();
            for (PlayerData d : allData) {
                merged.put(d.getUuid(), d);
            }
            for (PlayerData mem : plugin.getPlayerManager().getPlayerDataCache().values()) {
                merged.put(mem.getUuid(), mem);
            }

            // Include any offline player that ever joined the server
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                if (op.getUniqueId() != null && !merged.containsKey(op.getUniqueId())) {
                    PlayerData d = new PlayerData(op.getUniqueId(), seasonId);
                    merged.put(op.getUniqueId(), d);
                }
            }

            List<PlayerData> list = new ArrayList<>(merged.values());
            // Sort: 1. Level DESC, 2. XP DESC, 3. Currency DESC, 4. UUID
            list.sort((a, b) -> {
                int cmp = Integer.compare(b.getLevel(), a.getLevel());
                if (cmp != 0) return cmp;
                cmp = Integer.compare(b.getXp(), a.getXp());
                if (cmp != 0) return cmp;
                cmp = Integer.compare(b.getCurrency(), a.getCurrency());
                if (cmp != 0) return cmp;
                return a.getUuid().compareTo(b.getUuid());
            });

            List<LeaderboardEntry> entries = new ArrayList<>();
            Map<UUID, Integer> newRankMap = new HashMap<>();
            int rank = 1;

            for (PlayerData d : list) {
                if (rank > 100) break; // Cap at Top 100

                OfflinePlayer op = Bukkit.getOfflinePlayer(d.getUuid());
                String name = op.getName() != null ? op.getName() : "Player_" + d.getUuid().toString().substring(0, 6);
                boolean isOnline = op.isOnline();

                LeaderboardEntry entry = new LeaderboardEntry(d.getUuid(), name, d.getLevel(), d.getXp(), d.getCurrency(), isOnline, rank);
                entries.add(entry);
                newRankMap.put(d.getUuid(), rank);
                rank++;
            }

            synchronized (this) {
                this.cachedEntries = entries;
                this.rankMap.clear();
                this.rankMap.putAll(newRankMap);
                this.lastUpdate = System.currentTimeMillis();
            }
        });
    }

    public List<LeaderboardEntry> getLeaderboard() {
        if (System.currentTimeMillis() - lastUpdate > CACHE_DURATION_MS) {
            refreshLeaderboard();
        }
        return new ArrayList<>(cachedEntries);
    }

    public int getPlayerRank(UUID uuid) {
        if (System.currentTimeMillis() - lastUpdate > CACHE_DURATION_MS) {
            refreshLeaderboard();
        }
        return rankMap.getOrDefault(uuid, -1);
    }
}
