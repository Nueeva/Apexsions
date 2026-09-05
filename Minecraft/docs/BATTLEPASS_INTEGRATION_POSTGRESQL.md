# ApexsionsBattlepass — PostgreSQL & Web Integration Guide

Dokumentasi resmi arsitektur basis data, REST/Web integration, dan schema **PostgreSQL** untuk **ApexsionsBattlepass**.

---

## 1. Arsitektur Basis Data PostgreSQL

Berikut adalah DDL Script PostgreSQL resmi untuk tabel `ApexsionsBattlepass`.

```sql
-- ====================================================================
-- TABEL 1: DATA PEMAIN & PROGRESS BATTLEPASS (abp_player_data)
-- ====================================================================
CREATE TABLE IF NOT EXISTS abp_player_data (
    uuid VARCHAR(36) NOT NULL,
    season_id INTEGER NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    xp INTEGER NOT NULL DEFAULT 0,
    currency INTEGER NOT NULL DEFAULT 0,
    passes TEXT NOT NULL DEFAULT 'FREE', -- Format CSV / JSON array (e.g. 'FREE,PREMIUM,ULTIMATE')
    claimed_rewards TEXT NOT NULL DEFAULT '', -- Format CSV / JSON list level yang sudah diklaim
    last_daily_reset BIGINT NOT NULL DEFAULT 0,
    last_weekly_reset BIGINT NOT NULL DEFAULT 0,
    last_monthly_reset BIGINT NOT NULL DEFAULT 0,
    daily_refresh_count INTEGER NOT NULL DEFAULT 0,
    total_refresh_count INTEGER NOT NULL DEFAULT 0,
    shop_rotations TEXT DEFAULT '', -- JSON data rotasi item shop player saat ini
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, season_id)
);

CREATE INDEX IF NOT EXISTS idx_abp_player_leaderboard ON abp_player_data(season_id, level DESC, xp DESC, currency DESC);

-- ====================================================================
-- TABEL 2: PROGRESS QUEST PEMAIN (abp_quest_progress)
-- ====================================================================
CREATE TABLE IF NOT EXISTS abp_quest_progress (
    uuid VARCHAR(36) NOT NULL,
    quest_id VARCHAR(64) NOT NULL,
    progress INTEGER NOT NULL DEFAULT 0,
    completed INTEGER NOT NULL DEFAULT 0, -- 0 = belum selesai, 1 = selesai
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, quest_id)
);

CREATE INDEX IF NOT EXISTS idx_abp_quest_user ON abp_quest_progress(uuid);

-- ====================================================================
-- TABEL 3: RIWAYAT PEMBELIAN SHOP PEMAIN (abp_shop_purchases)
-- ====================================================================
CREATE TABLE IF NOT EXISTS abp_shop_purchases (
    uuid VARCHAR(36) NOT NULL,
    shop_item_id VARCHAR(64) NOT NULL,
    purchase_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, shop_item_id)
);

-- ====================================================================
-- TABEL 4: DATA SEASON AKTIF & HISTORI (abp_seasons)
-- ====================================================================
CREATE TABLE IF NOT EXISTS abp_seasons (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    max_level INTEGER NOT NULL DEFAULT 100,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

---

## 2. Query Integrasi Website (SQL Examples)

### A. Menampilkan Top 100 Leaderboard BattlePass (Untuk Web Ranking)
```sql
SELECT 
    uuid,
    level,
    xp,
    currency AS battle_coins,
    passes,
    ROW_NUMBER() OVER (ORDER BY level DESC, xp DESC, currency DESC) AS rank
FROM abp_player_data
WHERE season_id = 1
ORDER BY level DESC, xp DESC, currency DESC
LIMIT 100;
```

### B. Mendapatkan Profil BattlePass Pemain (Web Profile Page)
```sql
SELECT 
    uuid,
    season_id,
    level,
    xp,
    currency AS battle_coins,
    passes,
    daily_refresh_count,
    total_refresh_count
FROM abp_player_data
WHERE uuid = 'player-uuid-here' AND season_id = 1;
```

### C. Webstore: Top-up Battle Coins ke Pemain Secara Langsung
```sql
-- Aman dilakukan saat pemain offline maupun online (dengan cache invalidation)
UPDATE abp_player_data
SET currency = currency + 500,
    updated_at = CURRENT_TIMESTAMP
WHERE uuid = 'player-uuid-here' AND season_id = 1;
```

### D. Webstore: Upgrade Pass Pemain (Misal Pembelian Pass Premium di Web)
```sql
UPDATE abp_player_data
SET passes = CASE 
    WHEN passes = 'FREE' THEN 'FREE,PREMIUM'
    WHEN passes NOT LIKE '%PREMIUM%' THEN passes || ',PREMIUM'
    ELSE passes
END,
updated_at = CURRENT_TIMESTAMP
WHERE uuid = 'player-uuid-here' AND season_id = 1;
```

---

## 3. Integrasi Backend Website (Node.js / TypeScript Example)

```typescript
// Contoh implementasi REST API / Server Action di Next.js / Express
import { Pool } from 'pg';

const pool = new Pool({
  connectionString: process.env.POSTGRES_URL,
});

// GET /api/battlepass/leaderboard?season=1&page=1
export async function getLeaderboard(seasonId: number = 1, page: number = 1, limit: number = 10) {
  const offset = (page - 1) * limit;
  const query = `
    SELECT 
      uuid,
      level,
      xp,
      currency,
      passes,
      ROW_NUMBER() OVER (ORDER BY level DESC, xp DESC, currency DESC) AS rank
    FROM abp_player_data
    WHERE season_id = $1
    ORDER BY level DESC, xp DESC, currency DESC
    LIMIT $2 OFFSET $3;
  `;
  const result = await pool.query(query, [seasonId, limit, offset]);
  return result.rows;
}

// POST /api/webstore/reward-coins (Webhook payment gateway)
export async function addCoinsAfterPayment(uuid: string, seasonId: number, coins: number) {
  const query = `
    UPDATE abp_player_data
    SET currency = currency + $1,
        updated_at = CURRENT_TIMESTAMP
    WHERE uuid = $2 AND season_id = $3
    RETURNING currency;
  `;
  const result = await pool.query(query, [coins, uuid, seasonId]);
  return result.rows[0];
}
```

---

## 4. Java Plugin API (Untuk Integrasi Antar-Plugin Minecraft)

Tambahkan dependensi `ApexsionsBattlepass` di `pom.xml` atau `build.gradle`:

```java
import com.apexsions.battlepass.ApexsionsBattlepass;
import com.apexsions.battlepass.player.PlayerData;

public class MyPluginIntegration {

    public void giveRewardToPlayer(Player player, int exp, int coins) {
        ApexsionsBattlepass bp = ApexsionsBattlepass.getInstance();
        
        // 1. Tambah XP
        bp.getPlayerManager().addXp(player, exp);
        
        // 2. Tambah Battle Coins
        bp.getCurrencyService().addCurrency(player.getUniqueId(), coins);
        
        // 3. Cek Pass Pemain
        PlayerData data = bp.getPlayerManager().getPlayerData(player);
        boolean isPremium = data.hasPass("PREMIUM");
    }
}
```
