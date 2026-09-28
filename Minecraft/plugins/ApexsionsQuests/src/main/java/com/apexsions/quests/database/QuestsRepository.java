package com.apexsions.quests.database;

import com.apexsions.quests.model.PlayerQuestProgress;
import com.apexsions.quests.model.PlayerStreakData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class QuestsRepository {

    private final Plugin plugin;
    private final DatabaseManager databaseManager;

    public QuestsRepository(Plugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public CompletableFuture<PlayerStreakData> loadStreakData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM apx_login_streaks WHERE uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int currentStreak = rs.getInt("current_streak");
                        int highestStreak = rs.getInt("highest_streak");
                        String lastLogin = rs.getString("last_login_date");
                        String lastClaim = rs.getString("last_claim_date");
                        long mask = rs.getLong("monthly_claimed_mask");
                        String rawMilestones = rs.getString("claimed_milestones");
                        int freeze = rs.getInt("streak_freeze_count");
                        int rerolls = rs.getInt("rerolls_used_today");
                        String lastReroll = rs.getString("last_reroll_date");

                        Set<Integer> milestones = new HashSet<>();
                        if (rawMilestones != null && !rawMilestones.isBlank()) {
                            for (String s : rawMilestones.split(",")) {
                                try {
                                    milestones.add(Integer.parseInt(s.trim()));
                                } catch (NumberFormatException ignored) {}
                            }
                        }

                        return new PlayerStreakData(uuid, currentStreak, highestStreak, lastLogin,
                                lastClaim, mask, milestones, freeze, rerolls, lastReroll);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Error loading streak data for " + uuid + ": " + e.getMessage());
            }
            return new PlayerStreakData(uuid, 0, 0, "", "", 0L, new HashSet<>(), 0, 0, "");
        });
    }

    public CompletableFuture<Void> saveStreakData(PlayerStreakData data) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO apx_login_streaks (uuid, current_streak, highest_streak, last_login_date,
                    last_claim_date, monthly_claimed_mask, claimed_milestones, streak_freeze_count,
                    rerolls_used_today, last_reroll_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    current_streak = excluded.current_streak,
                    highest_streak = excluded.highest_streak,
                    last_login_date = excluded.last_login_date,
                    last_claim_date = excluded.last_claim_date,
                    monthly_claimed_mask = excluded.monthly_claimed_mask,
                    claimed_milestones = excluded.claimed_milestones,
                    streak_freeze_count = excluded.streak_freeze_count,
                    rerolls_used_today = excluded.rerolls_used_today,
                    last_reroll_date = excluded.last_reroll_date;
            """;
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data.getUuid().toString());
                ps.setInt(2, data.getCurrentStreak());
                ps.setInt(3, data.getHighestStreak());
                ps.setString(4, data.getLastLoginDate());
                ps.setString(5, data.getLastClaimDate());
                ps.setLong(6, data.getMonthlyClaimedMask());

                StringBuilder sb = new StringBuilder();
                for (int m : data.getClaimedMilestones()) {
                    if (!sb.isEmpty()) sb.append(",");
                    sb.append(m);
                }
                ps.setString(7, sb.toString());
                ps.setInt(8, data.getStreakFreezeCount());
                ps.setInt(9, data.getRerollsUsedToday());
                ps.setString(10, data.getLastRerollDate());

                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Error saving streak data for " + data.getUuid() + ": " + e.getMessage());
            }
        });
    }

    public CompletableFuture<List<PlayerQuestProgress>> loadDailyQuests(UUID uuid, String date) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerQuestProgress> list = new ArrayList<>();
            String sql = "SELECT * FROM apx_player_quests WHERE uuid = ? AND quest_date = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, date);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String id = rs.getString("id");
                        String qId = rs.getString("quest_id");
                        int cur = rs.getInt("current_progress");
                        int tgt = rs.getInt("target_progress");
                        boolean completed = rs.getInt("is_completed") == 1 || rs.getBoolean("is_completed");
                        boolean claimed = rs.getInt("is_claimed") == 1 || rs.getBoolean("is_claimed");

                        list.add(new PlayerQuestProgress(id, uuid, qId, date, cur, tgt, completed, claimed));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Error loading daily quests for " + uuid + ": " + e.getMessage());
            }
            return list;
        });
    }

    public CompletableFuture<Void> saveDailyQuest(PlayerQuestProgress q) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO apx_player_quests (id, uuid, quest_id, quest_date, current_progress, target_progress, is_completed, is_claimed)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    current_progress = excluded.current_progress,
                    is_completed = excluded.is_completed,
                    is_claimed = excluded.is_claimed;
            """;
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, q.getId());
                ps.setString(2, q.getPlayerUuid().toString());
                ps.setString(3, q.getQuestId());
                ps.setString(4, q.getQuestDate());
                ps.setInt(5, q.getCurrentProgress());
                ps.setInt(6, q.getTargetProgress());
                ps.setInt(7, q.isCompleted() ? 1 : 0);
                ps.setInt(8, q.isClaimed() ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Error saving daily quest " + q.getId() + ": " + e.getMessage());
            }
        });
    }
}
