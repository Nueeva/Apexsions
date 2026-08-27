CREATE TABLE IF NOT EXISTS regions (
    id VARCHAR(36) PRIMARY KEY,
    key VARCHAR(32) UNIQUE NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    world_name VARCHAR(128) NOT NULL,

    spawn_x DOUBLE PRECISION,
    spawn_y DOUBLE PRECISION,
    spawn_z DOUBLE PRECISION,
    spawn_yaw REAL,
    spawn_pitch REAL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Insert default starter kingdoms for Apexsions matching BlueMap world.conf
INSERT INTO regions (id, key, display_name, world_name, spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch, enabled)
VALUES 
    ('a0000000-0000-0000-0000-000000000001', 'ZENITHAR', 'Zenithar', 'world', -3028.5, 64.0, -5597.5, 0.0, 0.0, true),
    ('a0000000-0000-0000-0000-000000000002', 'SOLTERRA', 'Solterra', 'world', -5843.5, 65.0, 889.5, 0.0, 0.0, true),
    ('a0000000-0000-0000-0000-000000000003', 'SYLVAMOOR', 'Sylvamoor', 'world', -9666.5, 64.0, -4812.5, 0.0, 0.0, true)
ON CONFLICT (key) DO NOTHING;
