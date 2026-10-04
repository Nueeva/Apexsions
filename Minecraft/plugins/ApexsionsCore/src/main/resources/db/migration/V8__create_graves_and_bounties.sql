-- V8: Apexsions Graves & Bounties Persistence Schema
CREATE TABLE IF NOT EXISTS apexsions_graves (
    id VARCHAR(64) PRIMARY KEY,
    owner_uuid VARCHAR(36) NOT NULL,
    owner_name VARCHAR(32) NOT NULL,
    world VARCHAR(64) NOT NULL,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    z DOUBLE PRECISION NOT NULL,
    yaw REAL NOT NULL DEFAULT 0.0,
    pitch REAL NOT NULL DEFAULT 0.0,
    items_data TEXT NOT NULL,
    xp INTEGER NOT NULL DEFAULT 0,
    cause VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    collected BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_graves_owner ON apexsions_graves(owner_uuid);
CREATE INDEX IF NOT EXISTS idx_graves_collected ON apexsions_graves(collected);

CREATE TABLE IF NOT EXISTS apexsions_bounties (
    id VARCHAR(64) PRIMARY KEY,
    target_uuid VARCHAR(36) NOT NULL,
    target_name VARCHAR(32) NOT NULL,
    placer_uuid VARCHAR(36) NOT NULL,
    placer_name VARCHAR(32) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    claimed_by VARCHAR(36) NULL,
    claimed_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_bounties_target ON apexsions_bounties(target_uuid);
CREATE INDEX IF NOT EXISTS idx_bounties_active ON apexsions_bounties(active);

CREATE TABLE IF NOT EXISTS apexsions_bounty_hunters (
    hunter_uuid VARCHAR(36) PRIMARY KEY,
    hunter_name VARCHAR(32) NOT NULL,
    total_claimed DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    kills INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bounty_hunters_total ON apexsions_bounty_hunters(total_claimed);
