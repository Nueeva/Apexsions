-- V6: Apexsions Unified Moderation & Ban Engine
CREATE TABLE IF NOT EXISTS apexsions_bans (
    id VARCHAR(36) PRIMARY KEY,
    player_uuid VARCHAR(36) NOT NULL,
    player_name VARCHAR(32) NOT NULL,
    ip_address VARCHAR(45),
    banned_by VARCHAR(64) NOT NULL,
    reason TEXT NOT NULL,
    ban_type VARCHAR(16) NOT NULL DEFAULT 'NAME',
    banned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    unbanned_by VARCHAR(64) NULL,
    unban_reason TEXT NULL,
    unbanned_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_bans_player_uuid ON apexsions_bans(player_uuid);
CREATE INDEX IF NOT EXISTS idx_bans_player_name ON apexsions_bans(player_name);
CREATE INDEX IF NOT EXISTS idx_bans_ip ON apexsions_bans(ip_address);
CREATE INDEX IF NOT EXISTS idx_bans_active ON apexsions_bans(active);
