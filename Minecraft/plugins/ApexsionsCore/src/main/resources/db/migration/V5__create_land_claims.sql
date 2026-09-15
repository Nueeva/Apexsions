CREATE TABLE IF NOT EXISTS apexsions_claims (
    id VARCHAR(36) PRIMARY KEY,
    owner_id VARCHAR(36) NOT NULL,
    owner_name VARCHAR(32) NOT NULL,
    world VARCHAR(128) NOT NULL,
    chunk_x INTEGER NOT NULL,
    chunk_z INTEGER NOT NULL,
    trusted_players TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_apexsions_claim_chunk UNIQUE (world, chunk_x, chunk_z)
);

CREATE INDEX IF NOT EXISTS idx_claims_owner ON apexsions_claims(owner_id);
CREATE INDEX IF NOT EXISTS idx_claims_world_chunk ON apexsions_claims(world, chunk_x, chunk_z);
