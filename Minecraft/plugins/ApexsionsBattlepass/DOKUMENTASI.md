# Dokumentasi Lengkap ApexsionsBattlepass

Panduan teknis resmi modul **`ApexsionsBattlepass`** untuk pengelolaan Season 200 level, pool questing (Daily, Weekly, Monthly), tingkatan Pass, Toko Rotasi, Exp-Shop, dan Visual Admin GUI Editor.

---

## 📂 Struktur Direktori YAML Modular

```
plugins/ApexsionsBattlepass/
├── config.yml            <-- Pengaturan database (SQLite/PostgreSQL) dan opsi global
├── seasons.yml           <-- Definisi status season aktif, tanggal mulai, dan durasi
├── passes.yml            <-- Definisi nama dan hak akses tingkatan pass
├── rewards.yml           <-- Definisi reward 200 level untuk jalur Free & Premium
├── gui.yml               <-- Tata letak GUI 54-slot visual editor dan battlepass browser
├── messages.yml          <-- Pesan notifikasi MiniMessage
├── passes/               <-- File konfigurasi detail per pass (free, premium, ultimate, dll)
├── quests/
│   ├── daily.yml         <-- 42 variasi misi harian
│   ├── weekly.yml        <-- 120 variasi misi mingguan
│   └── monthly.yml       <-- 50 variasi misi bulanan
├── shop/
│   ├── daily.yml         <-- Rotasi barang toko harian
│   ├── weekly.yml        <-- Rotasi barang toko mingguan
│   └── monthly.yml       <-- Rotasi barang toko bulanan
└── exp-shop/
    └── packages.yml      <-- Paket penukaran BP-XP untuk item spesial
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/bp` | `/battlepass` | Membuka antarmuka utama 200 level BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp misi` | Membuka daftar misi harian/mingguan/bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp toko` | Membuka toko rotasi berbasis poin BP-XP | `apexsionsbattlepass.use` | `true` |
| `/bp pass` | - | Membuka menu peningkatan tier pass | `apexsionsbattlepass.use` | `true` |
| `/bp season` | - | Memeriksa status, waktu tersisa, dan periode season | `apexsionsbattlepass.use` | `true` |
| `/bp claim [level]` | - | Mengklaim hadiah level yang telah tercapai | `apexsionsbattlepass.use` | `true` |
| `/bp level` | - | Menampilkan level dan progress XP saat ini | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin`, `/adminbp` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |
| `/abp reload` | - | Memuat ulang seluruh konfigurasi season & quest | `apexsionsbattlepass.reload` | `op` |
| `/abp givepass <p> <tier>`| `/abp setpass` | Memberikan tier pass ke pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp setlevel <p> <lvl>`| - | Mengatur level BattlePass pemain secara manual | `apexsionsbattlepass.admin` | `op` |
| `/abp addxp <p> <amt>` | - | Memberikan poin BP-XP ke pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp reset <p>` | - | Mereset total seluruh data progresi pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp editor` | - | Membuka visual editor hadiah & toko | `apexsionsbattlepass.admin` | `op` |

---

## 👑 Tingkatan Pass & Sistem Pewarisan (*Tier Inheritance*)

- **`FREE`**: Jalur hadiah gratis yang terbuka untuk seluruh pemain secara default (`apexsionsbattlepass.pass.free`).
- **`PREMIUM`**: Membuka jalur reward premium eksklusif (`apexsionsbattlepass.pass.premium`).
- **`ULTIMATE` / `VIP`**: Membuka seluruh reward premium + bonus booster XP + mewarisi hak klaim seluruh tier di bawahnya (`apexsionsbattlepass.pass.vip`).

---

## 🛠️ Visual Admin Editor 54-Slot (`/abp editor`)

Admin dapat memodifikasi seluruh reward dan barang toko langsung dari dalam game:
- Drag-and-drop item dari inventaris ke slot level reward.
- Menyetel command console reward, material ikon, dan jumlah XP level.
- Pengaturan langsung tersimpan ke `rewards.yml` dan `shop/*.yml` secara real-time.
