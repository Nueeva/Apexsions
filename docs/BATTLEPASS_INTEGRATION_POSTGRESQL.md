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
    passes TEXT NOT NULL DEFAULT 'CITIZEN', -- Format CSV / JSON array (e.g. 'CITIZEN,SIO,EXSIO')
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

### D. Webstore: Upgrade Pass Pemain (Misal Pembelian Pass Sio / Exsio di Web)
```sql
UPDATE abp_player_data
SET passes = CASE 
    WHEN passes = 'CITIZEN' THEN 'CITIZEN,SIO'
    WHEN passes NOT LIKE '%SIO%' THEN passes || ',SIO'
    ELSE passes
END,
updated_at = CURRENT_TIMESTAMP
WHERE uuid = 'player-uuid-here' AND season_id = 1;
```

---

## 3. Integrasi Backend Website (Azuriom / Laravel + WebBridge)

Platform web Apexsions berjalan di atas **Azuriom (Laravel-based CMS)**, bukan Next.js/Express. Integrasi BattlePass game↔web dilakukan melalui plugin `apexsions-bridge`:

1. **Game → Web (Sync)**: Plugin game mengirim progres level/XP/koin/pass pemain ke endpoint `POST http://web.apexsions.my.id/api/apexsions-bridge` (header API key `apexsions_bridge_key_live_2026`), lalu tersimpan di tabel `minecraft_accounts` (kolom `battlepass_level`, `battlepass_xp`, `battlepass_pass_name`, `apex_coins`).
2. **Web → Game (Perintah)**: Aksi admin web ditulis ke tabel `deliveries` (status `PENDING`); plugin game mengonsumsinya saat pemain online dan menandainya `COMPLETED`.
3. **Leaderboard BattlePass bersifat eksklusif in-game** (`/abp top`); portal web hanya menampilkan tabel Level & Saldo Rupiah (lihat kebijakan leaderboard).

Contoh pembacaan data melalui Laravel (`Azuriom\Plugin\ApexsionsBridge`):

```php
use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;

// Ambil akun Minecraft pemain terhubung beserta data BattlePass tersinkron
$account = MinecraftAccount::where('user_id', $userId)->first();
$level   = $account?->battlepass_level;
$passName = $account?->battlepass_pass_name; // citizen, sio, atau exsio
```

---

## 4. Java Plugin API (Untuk Integrasi Antar-Plugin Minecraft)

Akses API publik melalui provider singleton `ApexsionsBattlepassProvider.get()` yang mengembalikan `ApexsionsBattlepassAPI`:

```java
import com.apexsions.battlepass.api.ApexsionsBattlepassAPI;
import com.apexsions.battlepass.api.ApexsionsBattlepassProvider;

public class MyPluginIntegration {

    public void giveRewardToPlayer(Player player, int xp, int points) {
        ApexsionsBattlepassAPI bp = ApexsionsBattlepassProvider.get();

        // 1. Tambah XP (menggunakan UUID)
        bp.addPlayerXp(player.getUniqueId(), xp);

        // 2. Tambah Battle Coins / Poin
        bp.addPlayerPoints(player.getUniqueId(), points);

        // 3. Cek kepemilikan Pass (id: "citizen", "sio", "exsio")
        boolean hasSio = bp.hasPass(player.getUniqueId(), "sio");
        String highestPass = bp.getPlayerHighestPassId(player.getUniqueId());
    }
}
```
