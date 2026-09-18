# ApexsionsBattlepass — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsBattlepass`** (Sistem Musim / Season 200 Level, Quest Pools Harian/Mingguan/Bulanan, Pass Tiers Inheritance, Toko Rotasi, Exp-Shop, dan Visual Admin Editor `/abp`).

---

## 🎫 1. Ikhtisar Modul & Arsitektur

`ApexsionsBattlepass` adalah sistem retensi dan gamifikasi pemain dengan 200 level hadiah, misi terintegrasi aktivitas gameplay, serta editor GUI visual 54-slot untuk mempermudah konfigurasi admin tanpa menyentuh file YAML secara manual.

```
                          ┌────────────────────────┐
                          │  ApexsionsBattlepass   │
                          │   (Season & Quests)    │
                          └───────────┬────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        ▼                             ▼                             ▼
┌──────────────────┐        ┌───────────────────┐         ┌───────────────────┐
│ 200 Level Tiers  │        │ Comprehensive Qst │         │  Admin GUI Editor │
│Free / Premium /  │        │Daily, Weekly, Mon │         │/abp 54-Slot Visual│
│Premium+ / Ultimate│       │Dynamic Shop & Exp │         │In-game Reward Mgt │
└──────────────────┘        └───────────────────┘         └───────────────────┘
```

---

## 👑 2. Tingkatan Pass & Sistem Pewarisan (Pass Tier Inheritance)

Terdapat tingkatan pass dengan hak klaim bertingkat:

1. **`FREE` Pass**: Terbuka untuk seluruh pemain secara default (`apexsionsbattlepass.pass.free`).
2. **`PREMIUM` Pass**: Membuka jalur reward premium tambahan (`apexsionsbattlepass.pass.premium`).
3. **`ULTIMATE` / `VIP` Pass**: Membuka seluruh reward premium + bonus booster XP + mewarisi hak klaim seluruh tier di bawahnya (`ULTIMATE` $\supset$ `PREMIUM+` $\supset$ `PREMIUM` $\supset$ `FREE`, permission `apexsionsbattlepass.pass.vip`).

---

## 🎯 3. Sistem Quest Pools (Misi Gameplay)

Setiap misi memberikan BattlePass XP (BP-XP) untuk menaikkan level (100 XP fixed per level):

- **Daily Quests (42 Variasi Misi)**: Direset setiap 24 jam. Menyediakan misi sederhana seperti memancing 10 ikan, menambang 32 coal, membunuh 15 zombie.
- **Weekly Quests (120 Variasi Misi)**: Direset setiap pekan. Menyediakan misi berbobot menengah seperti menyelesaikan raid, crafting diamond armor, menjelajahi 500 blocks.
- **Monthly Quests (50 Variasi Misi)**: Misi jangka panjang berhadiah XP masif seperti mengalahkan Wither, menambang Ancient Debris, menaikkan level kerajaan.

---

## 🛠️ 4. Visual Admin Editor 54-Slot (`/abp editor`)

Admin dapat mengelola seluruh aspek BattlePass langsung dari dalam game:
- **Editor Hadiah Level**: Drag-and-drop item ke slot level untuk menjadikannya reward, mengatur perintah konsol hadiah, atau mengganti material visual.
- **Editor Toko Rotasi**: Mengatur stok barang toko rotasi harian/mingguan dan probabilitas kelangkaan (*Rarity Chances*: `COMMON` s/d `MYTHIC`).
- **Pemberian Pass Instan**: Memberikan pass kepada pemain secara langsung (`/abp givepass <p> <tier>`).

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `/bp` | `/battlepass` | Membuka menu utama 200 level BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp misi` | Membuka daftar misi harian, mingguan, bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp toko` | Membuka toko rotasi berbasis poin BP-XP | `apexsionsbattlepass.use` | `true` |
| `/bp pass` | - | Membuka menu peningkatan tier pass | `apexsionsbattlepass.use` | `true` |
| `/bp season` | - | Memeriksa status, waktu tersisa, dan periode season | `apexsionsbattlepass.use` | `true` |
| `/bp claim [level]` | - | Mengklaim hadiah level yang telah tercapai | `apexsionsbattlepass.use` | `true` |
| `/bp level` | - | Menampilkan level dan sisa XP BattlePass saat ini | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin`, `/adminbp`, `/apexsionsbattlepass` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |
| `/abp reload` | - | Memuat ulang seluruh konfigurasi season & quest | `apexsionsbattlepass.reload` | `op` |
| `/abp givepass <p> <tier>`| `/abp setpass` | Memberikan tier pass ke pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp setlevel <p> <lvl>`| - | Mengatur level BattlePass pemain secara manual | `apexsionsbattlepass.admin` | `op` |
| `/abp addxp <p> <amt>` | - | Memberikan poin BP-XP ke pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp reset <p>` | - | Mereset total seluruh data progresi pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp editor` | - | Membuka visual editor hadiah & toko | `apexsionsbattlepass.admin` | `op` |

---

## 🗄️ 6. Skema Basis Data PostgreSQL / SQLite

```sql
CREATE TABLE IF NOT EXISTS abp_player_data (
    uuid VARCHAR(36) NOT NULL,
    season_id INTEGER NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    xp INTEGER NOT NULL DEFAULT 0,
    currency INTEGER NOT NULL DEFAULT 0,
    passes TEXT NOT NULL DEFAULT 'FREE',
    claimed_rewards TEXT NOT NULL DEFAULT '',
    last_daily_reset BIGINT NOT NULL DEFAULT 0,
    last_weekly_reset BIGINT NOT NULL DEFAULT 0,
    last_monthly_reset BIGINT NOT NULL DEFAULT 0,
    daily_refresh_count INTEGER NOT NULL DEFAULT 0,
    total_refresh_count INTEGER NOT NULL DEFAULT 0,
    shop_rotations TEXT DEFAULT '',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, season_id)
);
```
