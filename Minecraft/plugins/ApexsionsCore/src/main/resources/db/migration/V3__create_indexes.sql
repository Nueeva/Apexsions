CREATE INDEX IF NOT EXISTS idx_players_region_id ON players(region_id);
CREATE INDEX IF NOT EXISTS idx_players_level ON players(level);
CREATE INDEX IF NOT EXISTS idx_players_xp ON players(xp);
CREATE INDEX IF NOT EXISTS idx_regions_key ON regions(key);
