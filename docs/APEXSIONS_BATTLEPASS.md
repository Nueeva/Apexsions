# ApexsionsBattlepass — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsBattlepass`** (Sistem Musim / Season, 200 Level BattlePass, Quest Pools Harian/Mingguan/Bulanan, Pass Tiers Inheritance, Toko Rotasi, dan Visual Admin Editor `/abp`).

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

Terdapat 4 tingkatan pass dengan hak klaim bertingkat:

1. **`FREE` Pass**: Terbuka untuk seluruh pemain secara default (mendapatkan reward jalur gratis).
2. **`PREMIUM` Pass**: Membuka jalur hadiah premium tambahan.
3. **`PREMIUM+` Pass**: Membuka seluruh reward premium + bonus 25 level instan dan booster XP 20%.
4. **`ULTIMATE` Pass**: Membuka seluruh jalur hadiah + kosmetik eksklusif + booster XP 50% + hak klaim seluruh tier di bawahnya (`ULTIMATE` $\supset$ `PREMIUM+` $\supset$ `PREMIUM` $\supset$ `FREE`).

---

## 🎯 3. Sistem Quest Pools (Misi Gameplay)

Setiap misi memberikan BattlePass XP (BP-XP) untuk menaikkan level (100 XP fixed per level):

- **Daily Quests (42 Variasi Misi)**: Direset setiap 24 jam. Menyediakan misi sederhana seperti memancing 10 ikan, menambang 32 coal, membunuh 15 zombie.
- **Weekly Quests (120 Variasi Misi)**: Direset setiap pekan. Menyediakan misi berbobot menengah seperti menyelesaikan raid, crafting diamond armor, menjelajahi 500 blocks.
- **Monthly Quests (50 Variasi Misi)**: Misi jangka panjang berhadiah XP masif seperti mengalahkan Wither, menambang Ancient Debris, menaikkan level kerajaan.

---

## 🛠️ 4. Visual Admin Editor 54-Slot (`/abp`)

Admin dapat mengelola seluruh aspek BattlePass langsung dari dalam game:
- **Editor Hadiah Level**: Drag-and-drop item ke slot level untuk menjadikannya reward, mengatur perintah konsol hadiah, atau mengganti material visual.
- **Editor Toko Rotasi**: Mengatur stok barang toko rotasi harian/mingguan dan probabilitas kelangkaan (*Rarity Chances*: `COMMON` s/d `MYTHIC`).
- **Pemberian Pass Instan**: Memberikan pass kepada pemain tanpa perlu mengubah izin LuckPerms secara manual.

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/bp` | `/battlepass` | Membuka menu utama 200 level BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp misi` | Membuka daftar misi harian, mingguan, bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp toko` | Membuka toko rotasi berbasis BP-XP | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin`, `/adminbp` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |
| `/abp setlevel <p> <lvl>`| - | Mengatur level BattlePass pemain secara langsung | `apexsionsbattlepass.admin` | `op` |
| `/abp addxp <p> <xp>` | - | Memberikan poin BP-XP ke pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp givepass <p> <tier>`| `/abp setpass` | Memberikan status pass (`premium`/`ultimate`) | `apexsionsbattlepass.admin` | `op` |
| `/abp reload` | - | Memuat ulang seluruh konfigurasi YAML dan season | `apexsionsbattlepass.reload` | `op` |

---

## 🗄️ 6. Skema Basis Data PostgreSQL / SQLite

```sql
CREATE TABLE IF NOT EXISTS battlepass_players (
    uuid VARCHAR(36) PRIMARY KEY,
    season_id INT NOT NULL DEFAULT 1,
    level INT NOT NULL DEFAULT 1,
    xp INT NOT NULL DEFAULT 0,
    pass_tier VARCHAR(32) NOT NULL DEFAULT 'FREE',
    claimed_free_rewards TEXT,
    claimed_premium_rewards TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
