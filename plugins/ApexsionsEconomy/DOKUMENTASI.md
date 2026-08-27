# Dokumentasi Lengkap ApexsionsEconomy

Panduan teknis modul `ApexsionsEconomy` untuk manajemen mata uang, konfigurasi lelang, dan integrasi barter lintas-kerajaan.

---

## 📂 Struktur Konfigurasi YAML
- `config.yml`: Pengaturan database, mata uang (`rupiah`, `diamond`), pengaturan auction house, dan biaya transportasi barter kerajaan.
- `plugin.yml`: Deklarasi commands dan permissions.

---

## ⚡ Perintah & Permissions
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/economy` / `/bal` | Melihat saldo pribadi atau orang lain | `apexeconomy.use` |
| `/baltop` | Menampilkan leaderboard kekayaan pemain | `apexeconomy.use` |
| `/pay <pemain> <jumlah>` | Membuka menu transfer atau kirim uang | `apexeconomy.pay` |
| `/ah` | Membuka antarmuka pasar lelang | `apexeconomy.ah` |
| `/ah sell <harga>` | Memasang item di tangan ke pasar lelang | `apexeconomy.ah` |
| `/trade [pemain]` | Membuka GUI barter item dan uang | `apexeconomy.trade` |
| `/aeco give <p> <curr> <amt>` | Menambahkan saldo pemain oleh admin | `apexeconomy.admin` |
| `/aeco set <p> <curr> <amt>` | Mengatur saldo pemain oleh admin | `apexeconomy.admin` |
| `/aeco take <p> <curr> <amt>` | Mengurangi saldo pemain oleh admin | `apexeconomy.admin` |

---

## 🌐 Integrasi Barter Lintas-Kerajaan
Pada `config.yml`:
```yaml
trade:
  enabled: true
  request-timeout-seconds: 60
  # Biaya transportasi dipotong dari kedua pemain jika berasal dari kerajaan yang berbeda
  cross-kingdom-transport-fee: 5000.0
```
Jika kedua pemain berasal dari kerajaan yang sama (`ApexsionsCoreHook.isSameKingdom == true`), biaya transportasi adalah **Rp 0 (GRATIS)**.
