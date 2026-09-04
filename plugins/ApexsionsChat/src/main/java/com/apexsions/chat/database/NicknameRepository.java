package com.apexsions.chat.database;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.nick.NicknameData;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous persistence repository for player nicknames and token balances.
 */
public class NicknameRepository {

    private final ApexsionsChatPlugin plugin;
    private final ChatDatabaseManager db;

    public NicknameRepository(ApexsionsChatPlugin plugin, ChatDatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public CompletableFuture<Optional<NicknameData>> load(UUID uuid) {
        CompletableFuture<Optional<NicknameData>> future = new CompletableFuture<>();
        db.runAsync(() -> {
            String sql = "SELECT player_uuid, player_name, nickname_raw, color_style, tokens, updated_at " +
                    "FROM apexsions_nicknames WHERE player_uuid = ?";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("player_name");
                        String nick = rs.getString("nickname_raw");
                        String color = rs.getString("color_style");
                        int tokens = rs.getInt("tokens");
                        long updated = rs.getLong("updated_at");

                        NicknameData data = new NicknameData(uuid, name, nick, color, tokens, updated);
                        future.complete(Optional.of(data));
                        return;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load nickname for " + uuid + ": " + e.getMessage());
            }
            future.complete(Optional.empty());
        });
        return future;
    }

    public CompletableFuture<Void> save(NicknameData data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        db.runAsync(() -> {
            String sql = "INSERT INTO apexsions_nicknames (player_uuid, player_name, nickname_raw, color_style, tokens, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(player_uuid) DO UPDATE SET " +
                    "player_name = excluded.player_name, " +
                    "nickname_raw = excluded.nickname_raw, " +
                    "color_style = excluded.color_style, " +
                    "tokens = excluded.tokens, " +
                    "updated_at = excluded.updated_at";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data.getUuid().toString());
                ps.setString(2, data.getPlayerName());
                ps.setString(3, data.getNicknameRaw() != null ? data.getNicknameRaw() : "");
                ps.setString(4, data.getColorStyleId());
                ps.setInt(5, data.getTokens());
                ps.setLong(6, data.getUpdatedAt());
                ps.executeUpdate();
                future.complete(null);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save nickname for " + data.getUuid() + ": " + e.getMessage());
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Optional<NicknameData>> findByNickname(String rawNick) {
        CompletableFuture<Optional<NicknameData>> future = new CompletableFuture<>();
        db.runAsync(() -> {
            String sql = "SELECT player_uuid, player_name, nickname_raw, color_style, tokens, updated_at " +
                    "FROM apexsions_nicknames WHERE LOWER(nickname_raw) = LOWER(?)";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rawNick.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                        String name = rs.getString("player_name");
                        String nick = rs.getString("nickname_raw");
                        String color = rs.getString("color_style");
                        int tokens = rs.getInt("tokens");
                        long updated = rs.getLong("updated_at");

                        future.complete(Optional.of(new NicknameData(uuid, name, nick, color, tokens, updated)));
                        return;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to search nickname: " + e.getMessage());
            }
            future.complete(Optional.empty());
        });
        return future;
    }
}
