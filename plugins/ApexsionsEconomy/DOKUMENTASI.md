# Dokumentasi Lengkap ApexsionsEconomy

Panduan teknis resmi modul **`ApexsionsEconomy`** untuk pengelolaan multi-currency, transfer aman, pasar lelang (*Auction House*) dengan *Escrow Claim*, dan sistem barter terintegrasi kerajaan.

---

## 📂 Struktur Konfigurasi YAML Modular

```
plugins/ApexsionsEconomy/
├── config.yml            <-- Pengaturan database (SQLite/PostgreSQL), mata uang, AH, dan tarif trade
└── plugin.yml            <-- Deklarasi commands, permissions, dan metadata
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/economy` | `/eco`, `/uang`, `/bal` | Membuka menu utama saldo dan statistik keuangan | `apexsionseconomy.use` | `true` |
| `/baltop` | `/topbal` | Menampilkan leaderboard kekayaan pemain server | `apexsionseconomy.use` | `true` |
| `/pay <p> <amt> [curr]` | `/transfer`, `/kirimuang` | Mentransfer uang ke pemain lain secara instan | `apexsionseconomy.pay` | `true` |
| `/ah` | `/lelang`, `/auction` | Membuka antarmuka pasar lelang & brankas klaim | `apexsionseconomy.ah` | `true` |
| `/trade [pemain]` | `/barter`, `/tukar` | Membuka menu barter item dan saldo multi-currency | `apexsionseconomy.trade` | `true` |
| `/trade toggle` | - | Mengaktifkan/menonaktifkan permintaan trade | `apexsionseconomy.trade` | `true` |
| `/ecoadmin reload` | `/apexeconomy reload`, `/adminpay reload` | Memuat ulang konfigurasi ekonomi & mata uang | `apexsionseconomy.admin` | `op` |
| `/ecoadmin give <p> <amt> [curr]`| - | Menambahkan saldo pemain oleh administrator | `apexsionseconomy.admin` | `op` |
| `/ecoadmin take <p> <amt> [curr]`| - | Mengurangi saldo pemain oleh administrator | `apexsionseconomy.admin` | `op` |
| `/ecoadmin set <p> <amt> [curr]` | - | Mengatur nominal saldo pemain secara langsung | `apexsionseconomy.admin` | `op` |

---

## 💵 Sistem Multi-Currency & Format Angka

1. **Rupiah (`rupiah`)**:
   - Mata uang sirkulasi utama server untuk pasar, lelang, dan perdagangan.
   - Simbol: `Rp` (Format cerdas: `Rp 50.000`, `Rp 1,5 Jt`, `Rp 2,5 M`, `Rp 1,0 T`).
2. **Diamond (`diamond`)**:
   - Mata uang komoditas premium berbasis diamond/gem.
   - Simbol: `♦` (Format: `100 ♦`).

---

## 🏛️ Pasar Lelang & Brankas Penampungan (*Escrow Vault*)

- **Pemasangan Item**: Pemain dapat melelang item yang dipegang dengan harga dan masa berlaku yang ditentukan.
- **Sistem Escrow Claim**:
  - Jika lelang kedaluwarsa tanpa pembeli atau dibatalkan, item otomatis disimpan ke brankas penampungan aman (*Escrow Claim Vault*).
  - Menjamin item tidak akan pernah hilang meskipun inventaris pemain sedang penuh saat transaksi selesai.

---

## 🔄 Barter & Trade Terintegrasi Kerajaan

- **Penyaringan Pemain Kerajaan**: Menu trade secara default menyaring hanya pemain dalam satu kerajaan.
- **Tombol Toggle Filter Global (Slot 8)**: Memungkinkan beralih ke mode filter global untuk melihat semua pemain online.
- **Pajak Transportasi Lintas-Kerajaan**:
  - Transaksi sesama anggota kerajaan: **Rp 0 (GRATIS)**.
  - Transaksi lintas kerajaan yang berbeda: Dikenakan biaya transportasi otomatis saat konfirmasi (default `Rp 5.000`).

---

## 🧩 Akses Public API (`ApexsionsEconomyAPI`)

```java
ApexsionsEconomyAPI eco = ApexsionsEconomy.getEconomyAPI();
if (eco != null) {
    boolean hasMoney = eco.has(playerUuid, "rupiah", 10000);
    eco.deposit(playerUuid, "rupiah", 50000);
    eco.withdraw(playerUuid, "diamond", 10);
    String formatted = eco.format(1500000, "rupiah"); // "Rp 1,5 Jt"
}
```
