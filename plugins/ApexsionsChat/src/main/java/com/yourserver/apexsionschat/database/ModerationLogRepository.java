package com.yourserver.apexsionschat.database;

import com.yourserver.apexsionschat.ApexsionsChatPlugin;
import com.yourserver.apexsionschat.model.ModerationLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ModerationLogRepository {

    private final ApexsionsChatPlugin plugin;
    private final ChatDatabaseManager db;

    public ModerationLogRepository(ApexsionsChatPlugin plugin, ChatDatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void logAsync(ModerationLogEntry entry) {
        db.runAsync(() -> {
            String sql = """
                INSERT INTO apexsions_moderation_logs (
                    player_uuid, player_name, message_snippet, channel, rule_violated, action_taken, timestamp
                ) VALUES (?, ?, ?, ?, ?, ?, ?);
            """;
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entry.getPlayerUuid().toString());
                ps.setString(2, entry.getPlayerName());
                ps.setString(3, entry.getMessageSnippet());
                ps.setString(4, entry.getChannel());
                ps.setString(5, entry.getRuleViolated());
                ps.setString(6, entry.getActionTaken());
                ps.setLong(7, entry.getTimestamp().toEpochMilli());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to log moderation event: " + e.getMessage());
            }
        });
    }
}
