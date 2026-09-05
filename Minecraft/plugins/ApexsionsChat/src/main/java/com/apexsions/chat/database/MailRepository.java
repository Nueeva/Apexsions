package com.apexsions.chat.database;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.model.Mail;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MailRepository {

    private final ApexsionsChatPlugin plugin;
    private final ChatDatabaseManager db;

    public MailRepository(ApexsionsChatPlugin plugin, ChatDatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public CompletableFuture<Long> sendMailAsync(Mail mail) {
        return db.supplyAsync(() -> {
            String sql = """
                INSERT INTO apexsions_mail (
                    sender_uuid, sender_name, recipient_uuid, recipient_name,
                    subject, body, created_at, is_read, is_archived
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, mail.getSenderUuid().toString());
                ps.setString(2, mail.getSenderName());
                ps.setString(3, mail.getRecipientUuid().toString());
                ps.setString(4, mail.getRecipientName());
                ps.setString(5, mail.getSubject());
                ps.setString(6, mail.getBody());
                ps.setLong(7, mail.getCreatedAt().toEpochMilli());
                ps.setBoolean(8, mail.isRead());
                ps.setBoolean(9, mail.isArchived());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        mail.setMailId(id);
                        return id;
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to send mail: " + e.getMessage());
            }
            return -1L;
        });
    }

    public CompletableFuture<List<Mail>> getPlayerInboxAsync(UUID recipientUuid, int limit, int offset) {
        return db.supplyAsync(() -> {
            List<Mail> list = new ArrayList<>();
            String sql = "SELECT * FROM apexsions_mail WHERE recipient_uuid = ? AND is_archived = 0 ORDER BY created_at DESC LIMIT ? OFFSET ?;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, recipientUuid.toString());
                ps.setInt(2, limit);
                ps.setInt(3, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to fetch inbox: " + e.getMessage());
            }
            return list;
        });
    }

    public CompletableFuture<Integer> countUnreadMailAsync(UUID recipientUuid) {
        return db.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM apexsions_mail WHERE recipient_uuid = ? AND is_read = 0 AND is_archived = 0;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, recipientUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to count unread mail: " + e.getMessage());
            }
            return 0;
        });
    }

    public CompletableFuture<Optional<Mail>> getMailByIdAsync(long mailId) {
        return db.supplyAsync(() -> {
            String sql = "SELECT * FROM apexsions_mail WHERE mail_id = ?;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, mailId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get mail by id: " + e.getMessage());
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<Boolean> markMailAsReadAsync(long mailId) {
        return db.supplyAsync(() -> {
            String sql = "UPDATE apexsions_mail SET is_read = 1, read_at = ? WHERE mail_id = ?;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, Instant.now().toEpochMilli());
                ps.setLong(2, mailId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to mark mail as read: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> deleteMailAsync(long mailId) {
        return db.supplyAsync(() -> {
            String sql = "UPDATE apexsions_mail SET is_archived = 1 WHERE mail_id = ?;";
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, mailId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to archive/delete mail: " + e.getMessage());
                return false;
            }
        });
    }

    private Mail mapResultSet(ResultSet rs) throws SQLException {
        Mail mail = new Mail();
        mail.setMailId(rs.getLong("mail_id"));
        mail.setSenderUuid(UUID.fromString(rs.getString("sender_uuid")));
        mail.setSenderName(rs.getString("sender_name"));
        mail.setRecipientUuid(UUID.fromString(rs.getString("recipient_uuid")));
        mail.setRecipientName(rs.getString("recipient_name"));
        mail.setSubject(rs.getString("subject"));
        mail.setBody(rs.getString("body"));
        mail.setCreatedAt(Instant.ofEpochMilli(rs.getLong("created_at")));
        long readAt = rs.getLong("read_at");
        if (readAt > 0) {
            mail.setReadAt(Instant.ofEpochMilli(readAt));
        }
        mail.setRead(rs.getBoolean("is_read"));
        mail.setArchived(rs.getBoolean("is_archived"));
        return mail;
    }
}
