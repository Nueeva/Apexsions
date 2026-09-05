CREATE TABLE IF NOT EXISTS players (
    uuid UUID PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    xp BIGINT NOT NULL DEFAULT 0,
    region_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT players_level_range
        CHECK (level >= 1 AND level <= 100),

    CONSTRAINT players_xp_non_negative
        CHECK (xp >= 0)
);
