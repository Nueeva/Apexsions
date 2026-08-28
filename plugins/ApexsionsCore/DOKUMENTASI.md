# Dokumentasi Lengkap ApexsionsCore

Panduan teknis modul `ApexsionsCore` untuk konfigurasi, perintah, API eksternal, dan integrasi gameplay.

---

## 📂 Struktur Konfigurasi YAML
- `config.yml`: Pengaturan database (SQLite/PostgreSQL) dan opsi umum.
- `kingdoms.yml`: Definisi 3 kerajaan, titik spawn, nama tampilan, dan warna wilayah.
- `xp.yml`: Pengaturan perolehan XP untuk 13 kategori gameplay.
- `ranks.yml` & `titles.yml`: Hierarki pangkat dan gelar kerajaan per level.
- `rewards.yml`: Daftar reward item, perintah, dan hak akses per tingkat level.
- `gui.yml`: Layout dan dekorasi antarmuka inventaris.

---

## ⚡ Perintah & Permissions
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/kingdom` | Membuka profil dan info kerajaan | `apexsionscore.kingdom` |
| `/kingdom select` | Membuka menu pemilihan kerajaan | `apexsionscore.kingdom.select` |
| `/level` | Membuka progress bar level & reward | `apexsionscore.level` |
| `/xpguide` | Membuka panduan 13 kategori XP | `apexsionscore.xpguide` |
| `/rtp` | Teleportasi acak aman di dalam wilayah kerajaan sendiri | `apexsionscore.rtp` |
| `/kadmin addxp <pemain> <jumlah>` | Menambahkan XP ke pemain | `apexsionscore.admin` |
| `/kadmin setlevel <pemain> <level>` | Mengubah level pemain secara manual | `apexsionscore.admin` |
| `/kadmin reload` | Memuat ulang seluruh file konfigurasi | `apexsionscore.admin` |

---

## 📍 Integrasi TPA EssentialsX
`TpaRestrictionListener` mencegah bypass wilayah melalui perintah TPA:
1. Memastikan `getPlayerRegionKey(sender)` == `getPlayerRegionKey(target)` dan bukan `"NONE"`.
2. Memastikan `region.containsLocation(loc)` bernilai `true` untuk **kedua** pemain sebelum teleportasi diizinkan.
