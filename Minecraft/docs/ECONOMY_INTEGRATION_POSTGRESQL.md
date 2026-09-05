# ApexsionsEconomy — PostgreSQL & Web Integration Guide

Dokumentasi resmi arsitektur basis data, Web Marketplace (Auction House), REST API, dan schema **PostgreSQL** untuk **ApexsionsEconomy**.

---

## 1. Arsitektur Basis Data PostgreSQL

Berikut adalah DDL Script PostgreSQL resmi untuk tabel `ApexsionsEconomy`.

```sql
-- ====================================================================
-- TABEL 1: SALDO MULTI-CURRENCY PEMAIN (economy_balances)
-- ====================================================================
CREATE TABLE IF NOT EXISTS economy_balances (
    uuid VARCHAR(36) NOT NULL,
    currency_id VARCHAR(32) NOT NULL, -- 'rupiah', 'diamond', custom currencies
    balance NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, currency_id)
);

CREATE INDEX IF NOT EXISTS idx_economy_leaderboard ON economy_balances(currency_id, balance DESC);

-- ====================================================================
-- TABEL 2: LOG TRANSAKSI KEUANGAN & AUDIT (economy_transactions)
-- ====================================================================
CREATE TABLE IF NOT EXISTS economy_transactions (
    id BIGSERIAL PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    sender_uuid VARCHAR(36),
    receiver_uuid VARCHAR(36),
    currency_id VARCHAR(32) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL,
    type VARCHAR(32) NOT NULL, -- 'TRANSFER', 'AUCTION_BUY', 'AUCTION_SOLD', 'WEB_TOPUP', 'ADMIN_SET'
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_economy_tx_sender ON economy_transactions(sender_uuid);
CREATE INDEX IF NOT EXISTS idx_economy_tx_receiver ON economy_transactions(receiver_uuid);
CREATE INDEX IF NOT EXISTS idx_economy_tx_time ON economy_transactions(timestamp DESC);

-- ====================================================================
-- TABEL 3: AUCTION HOUSE / PASAR LELANG (economy_auctions)
-- ====================================================================
CREATE TABLE IF NOT EXISTS economy_auctions (
    id VARCHAR(36) PRIMARY KEY,
    seller_uuid VARCHAR(36) NOT NULL,
    seller_name VARCHAR(32) NOT NULL,
    currency_id VARCHAR(32) NOT NULL,
    price NUMERIC(18, 2) NOT NULL,
    item_data TEXT NOT NULL, -- Base64 encoded Minecraft NBT ItemStack
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'SOLD', 'CANCELLED', 'EXPIRED'
    buyer_uuid VARCHAR(36),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_economy_auctions_status ON economy_auctions(status, expires_at);
CREATE INDEX IF NOT EXISTS idx_economy_auctions_seller ON economy_auctions(seller_uuid);

-- ====================================================================
-- TABEL 4: PENDING CLAIMS / ESCROW (economy_pending_claims)
-- ====================================================================
CREATE TABLE IF NOT EXISTS economy_pending_claims (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    type VARCHAR(16) NOT NULL, -- 'MONEY', 'ITEM'
    currency_id VARCHAR(32),
    amount NUMERIC(18, 2) DEFAULT 0,
    item_data TEXT,
    claimed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_economy_claims_user ON economy_pending_claims(uuid, claimed);
```

---

## 2. Query Integrasi Website (SQL Examples)

### A. Leaderboard Kekayaan Top 100 (Web Leaderboards)
```sql
SELECT 
    uuid,
    currency_id,
    balance,
    ROW_NUMBER() OVER (ORDER BY balance DESC) AS rank
FROM economy_balances
WHERE currency_id = 'rupiah'
ORDER BY balance DESC
LIMIT 100;
```

### B. Dompet Web Player (Web Wallet)
```sql
SELECT 
    currency_id,
    balance
FROM economy_balances
WHERE uuid = 'player-uuid-here';
```

### C. Webstore: Top-up Rupiah / Diamond Otomatis Setelah Pembayaran (Payment Gateway)
```sql
-- Menggunakan safe atomic upsert (PostgreSQL ON CONFLICT)
INSERT INTO economy_balances (uuid, currency_id, balance, updated_at)
VALUES ('player-uuid-here', 'rupiah', 50000, CURRENT_TIMESTAMP)
ON CONFLICT (uuid, currency_id)
DO UPDATE SET 
    balance = economy_balances.balance + EXCLUDED.balance,
    updated_at = CURRENT_TIMESTAMP;

-- Catat ke log transaksi
INSERT INTO economy_transactions (timestamp, sender_uuid, receiver_uuid, currency_id, amount, type, details)
VALUES (
    EXTRACT(EPOCH FROM NOW()) * 1000,
    'WEB_STORE_GATEWAY',
    'player-uuid-here',
    'rupiah',
    50000,
    'WEB_TOPUP',
    'Top-up sukses via QRIS / Midtrans Order #ORD-10928'
);
```

### D. Web Auction House: Mengambil Daftar Item Lelang Aktif (Web Marketplace)
```sql
SELECT 
    id,
    seller_uuid,
    seller_name,
    currency_id,
    price,
    item_data,
    created_at,
    expires_at
FROM economy_auctions
WHERE status = 'ACTIVE' AND expires_at > (EXTRACT(EPOCH FROM NOW()) * 1000)
ORDER BY created_at DESC
LIMIT 50;
```

---

## 3. Integrasi Backend Website (Node.js / Express Example)

```typescript
import { Pool } from 'pg';

const pool = new Pool({
  connectionString: process.env.POSTGRES_URL,
});

// GET /api/economy/balances/:uuid
export async function getPlayerBalances(uuid: string) {
  const result = await pool.query(
    'SELECT currency_id, balance FROM economy_balances WHERE uuid = $1',
    [uuid]
  );
  return result.rows;
}

// POST /api/payment/webhook (Payment Gateway Callback)
export async function handleTopupWebhook(orderId: string, playerUuid: string, currency: string, amount: number) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    // 1. Update Saldo Pemain
    await client.query(`
      INSERT INTO economy_balances (uuid, currency_id, balance, updated_at)
      VALUES ($1, $2, $3, CURRENT_TIMESTAMP)
      ON CONFLICT (uuid, currency_id)
      DO UPDATE SET balance = economy_balances.balance + $3, updated_at = CURRENT_TIMESTAMP;
    `, [playerUuid, currency, amount]);

    // 2. Catat Log Audit Transaksi
    await client.query(`
      INSERT INTO economy_transactions (timestamp, sender_uuid, receiver_uuid, currency_id, amount, type, details)
      VALUES ($1, 'WEB_GATEWAY', $2, $3, $4, 'WEB_TOPUP', $5);
    `, [Date.now(), playerUuid, currency, amount, `Payment Completed for ${orderId}`]);

    await client.query('COMMIT');
    return { success: true };
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}
```

---

## 4. Java Plugin API (Untuk Integrasi Antar-Plugin Minecraft)

Gunakan `ApexsionsEconomyAPI` yang telah disediakan:

```java
import com.apexsions.economy.api.ApexsionsEconomyAPI;

public class ShopPluginIntegration {

    public void purchaseItem(Player buyer, Player seller, double price) {
        // Cek saldo
        if (ApexsionsEconomyAPI.has(buyer.getUniqueId(), "rupiah", price)) {
            // Transfer langsung
            boolean success = ApexsionsEconomyAPI.transfer(
                buyer.getUniqueId(), 
                seller.getUniqueId(), 
                "rupiah", 
                price
            );
            
            if (success) {
                buyer.sendMessage("§aPembelian berhasil!");
            }
        }
    }
}
```
