# Dokumentasi Lengkap ApexsionsBattlepass

Panduan teknis modul `ApexsionsBattlepass` untuk pengelolaan season, pass tiers, questing, dan editor admin.

---

## 📂 Struktur Direktori YAML Modular
- `config.yml`: Pengaturan database dan opsi global.
- `seasons.yml`: Definisi season aktif dan durasi.
- `rewards.yml`: Definisi hadiah level untuk setiap tingkatan pass.
- `passes/`: File konfigurasi masing-masing jenis pass (`free.yml`, `premium.yml`, `elite.yml`, `ultimate.yml`).
- `quests/`: File misi harian, mingguan, dan bulanan.
- `shop/`: Kategori dan barang toko rotasi.
- `exp-shop/`: Toko penukaran EXP untuk item spesial.

---

## ⚡ Perintah & Permissions
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/bp` | Membuka menu utama BattlePass | `apexsbp.use` |
| `/bp quests` | Membuka daftar misi dan progres | `apexsbp.quests` |
| `/bp shop` | Membuka toko rotasi | `apexsbp.shop` |
| `/abp` | Membuka editor GUI visual admin | `apexsbp.admin` |
| `/abp setlevel <p> <lvl>` | Mengubah level BattlePass pemain | `apexsbp.admin` |
| `/abp addxp <p> <xp>` | Memberikan XP BattlePass | `apexsbp.admin` |
| `/abp setpass <p> <tier>` | Memberikan status pass ke pemain | `apexsbp.admin` |
| `/abp reload` | Memuat ulang seluruh konfigurasi YAML | `apexsbp.admin` |
