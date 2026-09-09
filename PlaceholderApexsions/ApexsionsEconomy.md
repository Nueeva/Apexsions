# 💰 PlaceholderAPI — ApexsionsEconomy

> **Plugin:** `ApexsionsEconomy`  
> **Author:** ApexTeam / Apexsions  
> **Identifier PAPI:** `%apexsionseconomy_<placeholder>%`  

Plugin **ApexsionsEconomy** mengelola sistem multi-mata uang (*multi-currency*), transaksi atomik, sistem lelang (*Auction House*), brankas (*escrow*), serta pertukaran barang (*barter*).

Semua nilai mata uang telah distandarisasi secara ketat dengan aturan penulisan mata uang Apexsions:
- **Rupiah:** Menggunakan prefix `Rp.` (misal: `Rp. 10,000`)
- **Diamond:** Menggunakan suffix simbol permata `💎` (misal: `100 💎`)

---

## 📊 Daftar Placeholder Lengkap

### 1. Saldo Mata Uang Standar & Singkatan

| Placeholder | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- |
| `%apexsionseconomy_rupiah%` | Angka Mentah (Double) | `15000` | Menampilkan saldo Rupiah pemain tanpa format koma ataupun simbol. Sangat cocok digunakan untuk evaluasi logika kondisi (misal: pada DeluxeMenus atau Skript). |
| `%apexsionseconomy_rupiah_formatted%` | String Terformat | `Rp. 15,000` | Menampilkan saldo Rupiah pemain lengkap dengan pemisah ribuan dan simbol resmi `Rp.`. Sangat ideal untuk Scoreboard, TAB, dan pesan obrolan. |
| `%apexsionseconomy_diamond%` | Angka Mentah (Double) | `75` | Menampilkan saldo Diamond pemain tanpa simbol untuk keperluan perbandingan logika. |
| `%apexsionseconomy_diamond_formatted%` | String Terformat | `75 💎` | Menampilkan saldo Diamond pemain lengkap dengan simbol permata `💎`. |

---

### 2. Sintaks Dinamis Seluruh Mata Uang (Dynamic Registry)

Jika di masa mendatang server menambahkan mata uang baru ke dalam registry `ApexsionsEconomy` (misal: `koin`, `gold`, dll.), placeholder dinamis berikut dapat langsung digunakan tanpa memerlukan update kode:

| Pola Placeholder | Contoh Penggunaan | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- |
| `%apexsionseconomy_balance_<currency>%` | `%apexsionseconomy_balance_rupiah%`<br>`%apexsionseconomy_balance_diamond%` | `50000`<br>`200` | Mengambil nilai saldo mentah untuk jenis mata uang `<currency>`. Mengembalikan `0` jika mata uang tidak ditemukan. |
| `%apexsionseconomy_balance_<currency>_formatted%` | `%apexsionseconomy_balance_rupiah_formatted%`<br>`%apexsionseconomy_balance_diamond_formatted%` | `Rp. 50,000`<br>`200 💎` | Mengambil nilai saldo terformat resmi lengkap dengan simbol dan pemisah ribuan sesuai pengaturan mata uang tersebut. |

---

### 3. Rumah Lelang (Auction House / AH)

| Placeholder | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- |
| `%apexsionseconomy_active_auctions%` | Angka (Integer) | `38` | Menampilkan total jumlah lelang item yang sedang aktif di pasar lelang (`/ah`) server. |

---

## 💻 Contoh Penggunaan di Scoreboard & Menu GUI

### Contoh Konfigurasi Sidebar Scoreboard:
```yaml
lines:
  - "&6&lDOMPET PEMAIN"
  - "&7• &fRupiah: &a%apexsionseconomy_rupiah_formatted%"
  - "&7• &fDiamond: &b%apexsionseconomy_diamond_formatted%"
  - ""
  - "&e&lPASAR LELANG"
  - "&7• &fItem Lelang: &e%apexsionseconomy_active_auctions% Barang"
```

### Contoh Persyaratan Kondisi (DeluxeMenus):
```yaml
view_requirement:
  requirements:
    has_money:
      type: '>='
      input: '%apexsionseconomy_rupiah%'
      output: '5000'
      deny_commands:
        - "[message] &cSaldo Rupiah kamu kurang! Butuh Rp. 5,000 untuk membeli item ini."
```
