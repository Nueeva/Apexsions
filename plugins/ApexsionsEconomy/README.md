# ApexsionsEconomy — Minecraft 1.21.4 (Paper 26.2)

Plugin ekonomi multi-currency, transfer aman, pasar lelang (*Auction House*) dengan sistem penampungan (*Escrow*), dan sistem barter/trade terintegrasi kerajaan untuk server **Apexsions**.

---

## 🌟 Fitur Utama
- **Multi-Currency Terintegrasi**: Mengelola mata uang `Rupiah` (Rp) dan `Diamond` secara independen dengan formatting cerdas (`K`, `Jt`, `M`, `T`).
- **Transfer Saldo Cepat (`/pay`)**: GUI daftar kepala pemain online dan pencarian nama via chat.
- **Auction House (`/ah`)**: Jual-beli item aman antar-pemain dengan masa berlaku lelang dan sistem *Escrow Claim*.
- **Sistem Barter & Trade Kerajaan (`/trade`)**:
  - Menyaring pemain sesama kerajaan secara otomatis.
  - Tombol filter toggle untuk melihat seluruh pemain online (Global).
  - Pemungutan biaya transportasi lintas-kerajaan saat konfirmasi transaksi.

---

## 🛠️ Kompilasi & Build
```powershell
mvn clean package
```
Output: `target/ApexsionsEconomy-1.0.0.jar`
