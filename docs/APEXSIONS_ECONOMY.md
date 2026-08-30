# ApexsionsEconomy — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsEconomy`** (Sistem Multi-Currency Rupiah & Diamond, Transfer Cepat `/pay`, Pasar Lelang `/ah` dengan Escrow Claim, serta Barter/Trade Kerajaan).

---

## 💰 1. Ikhtisar Modul & Arsitektur

`ApexsionsEconomy` mengatur peredaran mata uang, perdagangan bebas pemain, transaksi atomik anti-duplikasi, serta integrasi pajak transportasi lintas-kerajaan.

```
                           ┌────────────────────────┐
                           │    ApexsionsEconomy    │
                           │(Multi-Currency Engine) │
                           └───────────┬────────────┘
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
┌──────────────────┐         ┌───────────────────┐         ┌───────────────────┐
│  Multi-Currency  │         │   Auction House   │         │ 12-Slot Barter GUI│
│Rupiah (Rp) / Dia │         │/ah, Expire Escrow,│         │Kingdom Player Flt,│
│Formatter K/Jt/M/T│         │  Category Browse  │         │Cross-Kingdom Tariff│
└──────────────────┘         └───────────────────┘         └───────────────────┘
```

---

## 💵 2. Mata Uang Ganda (Dual Currencies)

1. **Rupiah (`rupiah`)**:
   - Mata uang utama sirkulasi server untuk jual-beli pasar, ongkos transportasi, dan lelang.
   - Simbol: `Rp` (Format: `Rp 50.000`, `Rp 1,5 Jt`, `Rp 2,5 M`, `Rp 1,0 T`).
2. **Diamond (`diamond`)**:
   - Mata uang premium berbasis diamond/gem untuk transaksi komoditas berharga dan toko eksklusif.
   - Simbol: `♦` (Format: `100 ♦`).

---

## 🏛️ 3. Pasar Lelang & Escrow Claim (`/ah`)

- **Pemasangan Item Bebas**: Pemain dapat mendaftarkan item tangan dengan harga dan durasi lelang.
- **Sistem Penampungan Aman (*Escrow Claim*)**:
  - Jika item kedaluwarsa tanpa pembeli atau lelang dibatalkan, item otomatis dipindahkan ke brankas penampungan (*Escrow Vault*).
  - Mencegah item hilang akibat inventaris pemain penuh saat lelang berakhir.
- **Kategori Browsing & Filter**: Filter berdasarkan harga terendah/tertinggi, kategori armor, senjata, blocks, atau bahan sihir.

---

## 🔄 4. Sistem Barter & Trade Terintegrasi Kerajaan (`/trade`)

Menu barter 12-slot interaktif dengan keamanan tinggi:
1. **Filter Pemain Sesama Kerajaan**: Secara otomatis hanya menampilkan pemain satu kerajaan di menu trade untuk mendorong kerjasama internal.
2. **Tombol Toggle Filter Global (Slot 8)**: Memungkinkan pemain beralih ke mode `[🌐 FILTER: SEMUA KERAJAAN]` untuk melihat seluruh pemain di server.
3. **Pajak Transportasi Lintas-Kerajaan (*Transport Tariff*)**:
   - **Sesama Kerajaan**: Pajak transportasi = **Rp 0 (GRATIS)**.
   - **Beda Kerajaan**: Kedua belah pihak dikenakan biaya transportasi sebesar **Rp 5.000** (dikonfigurasi di `config.yml`) yang dipotong secara otomatis saat konfirmasi kedua pihak tercapai.
4. **Proteksi Anti-Scam**: Tombol konfirmasi 2 tahap dengan reset hitungan mundur jika salah satu pihak mengubah item/saldo tawaran di detik terakhir.

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/economy` | `/eco`, `/uang`, `/bal` | Membuka menu utama saldo dan statistik keuangan | `apexsionseconomy.use` | `true` |
| `/baltop` | `/topbal` | Menampilkan leaderboard 10 pemain terkaya server | `apexsionseconomy.use` | `true` |
| `/pay <p> <amt> [curr]` | `/transfer`, `/kirimuang` | Mentransfer uang ke pemain lain secara instan | `apexsionseconomy.pay` | `true` |
| `/ah` | `/lelang`, `/auction` | Membuka antarmuka pasar lelang & brankas klaim | `apexsionseconomy.ah` | `true` |
| `/trade [pemain]` | `/barter`, `/tukar` | Membuka menu barter item dan mata uang | `apexsionseconomy.trade` | `true` |
| `/trade toggle` | - | Mengaktifkan/menonaktifkan permintaan trade | `apexsionseconomy.trade` | `true` |
| `/ecoadmin reload` | `/apexeconomy reload`, `/adminpay reload` | Memuat ulang konfigurasi ekonomi & mata uang | `apexsionseconomy.admin` | `op` |
| `/ecoadmin give <p> <amt> [curr]`| - | Menambahkan saldo pemain oleh administrator | `apexsionseconomy.admin` | `op` |
| `/ecoadmin take <p> <amt> [curr]`| - | Mengurangi saldo pemain oleh administrator | `apexsionseconomy.admin` | `op` |
| `/ecoadmin set <p> <amt> [curr]` | - | Mengatur nominal saldo pemain secara langsung | `apexsionseconomy.admin` | `op` |

---

## 🗄️ 6. Skema Basis Data PostgreSQL / SQLite

```sql
CREATE TABLE IF NOT EXISTS economy_balances (
    uuid VARCHAR(36) NOT NULL,
    currency_id VARCHAR(32) NOT NULL,
    balance NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, currency_id)
);

CREATE TABLE IF NOT EXISTS economy_auctions (
    id VARCHAR(36) PRIMARY KEY,
    seller_uuid VARCHAR(36) NOT NULL,
    seller_name VARCHAR(32) NOT NULL,
    currency_id VARCHAR(32) NOT NULL,
    price NUMERIC(18, 2) NOT NULL,
    item_data TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    buyer_uuid VARCHAR(36)
);

CREATE TABLE IF NOT EXISTS economy_pending_claims (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    type VARCHAR(16) NOT NULL, -- 'MONEY', 'ITEM'
    currency_id VARCHAR(32),
    amount NUMERIC(18, 2) DEFAULT 0,
    item_data TEXT,
    claimed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
