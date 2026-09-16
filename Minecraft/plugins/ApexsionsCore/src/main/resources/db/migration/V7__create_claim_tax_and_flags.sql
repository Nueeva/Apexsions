-- V7: Apexsions Land Claim Tax, Grace Period, Flags & Civilization Roles
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS bank_balance REAL NOT NULL DEFAULT 0.0;
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS daily_upkeep REAL NOT NULL DEFAULT 100.0;
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS grace_period_until TIMESTAMP NULL;
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS last_tax_collected_at TIMESTAMP NULL;
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS flags TEXT NOT NULL DEFAULT '{}';
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS roles TEXT NOT NULL DEFAULT '{}';
ALTER TABLE apexsions_claims ADD COLUMN IF NOT EXISTS kingdom_id VARCHAR(64) NULL;

CREATE INDEX IF NOT EXISTS idx_claims_status ON apexsions_claims(status);
CREATE INDEX IF NOT EXISTS idx_claims_kingdom ON apexsions_claims(kingdom_id);
