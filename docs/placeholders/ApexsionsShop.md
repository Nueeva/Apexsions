# 🛒 PlaceholderAPI — ApexsionsShop

> **Plugin:** `ApexsionsShop`  
> **Author:** Nueeva / Apexsions Team  
> **Identifier PAPI:** `%apexsionsshop_<placeholder>%`  

Plugin **ApexsionsShop** mengelola sistem pasar dinamis peradaban (*dynamic market*), toko multi-kategori, sistem perpajakan kerajaan (*Kingdom Tax Service*), tren fluktuasi harga berdasarkan cuaca dunia (*Weather Price Fluctuation*), serta menu penjualan cepat (*Sell GUI*).

---

## 📊 Daftar Placeholder Lengkap

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsionsshop_tax_rate%` | `%apexsionsshop_tax_percent%` | Persentase (String) | `5.0%` atau `10.0%` | Tarif pajak pasar yang dikenakan pada transaksi penjualan pemain saat ini, disesuaikan dengan status kerajaan pemain dari `ApexsionsCore`. |
| `%apexsionsshop_tax_raw%` | - | Angka (Double) | `5.0` | Nilai angka murni dari persentase pajak pasar (tanpa simbol persen). |
| `%apexsionsshop_total_items%` | - | Angka (Integer) | `142` | Total jumlah item barang dagangan yang terdaftar di seluruh kategori toko peradaban. |
| `%apexsionsshop_total_categories%` | - | Angka (Integer) | `6` | Jumlah kategori toko yang aktif (misal: Blok, Pertanian, Drop Monster, Ore/Tambang, Peralatan, dll.). |
| `%apexsionsshop_weather%` | - | ID / Key (String) | `CLEAR`, `RAIN`, `THUNDER` | Status kondisi cuaca di dunia pemain saat ini yang memengaruhi harga jual hasil panen dan drop monster. |
| `%apexsionsshop_weather_display%` | - | String Berikon | `☀ Cerah`, `🌧 Hujan`, `⚡ Badai Petir` | Tampilan status cuaca berikon yang menarik untuk diletakkan pada papan skor atau header toko. |

---

## 🌾 Pengaruh Cuaca Terhadap Pasar Dinamis

Plugin `ApexsionsShop` memperhitungkan kondisi cuaca dalam fluktuasi ekonomi:
- **☀ Cuaca Cerah (`CLEAR`):** Hasil panen pertanian (*Farming*) mengalami peningkatan harga jual (+10%).
- **🌧 Cuaca Hujan (`RAIN`):** Penjualan drop monster (*Mob Drops*) meningkat (+5%), sementara bibit tanaman stabil.
- **⚡ Badai Petir (`THUNDER`):** Bahaya tinggi di luar ruangan, harga drop mob meningkat drastis.

---

## 💡 Contoh Implementasi di Server

### Contoh Tampilan Sidebar Pasar:
```yaml
lines:
  - "&6&lINFO PASAR KERAJAAN"
  - "&7• &fPajak Transaksi: &c%apexsionsshop_tax_rate%"
  - "&7• &fKondisi Cuaca: &e%apexsionsshop_weather_display%"
  - "&7• &fTotal Komoditas: &a%apexsionsshop_total_items% Item"
  - ""
  - "&fBuka Pasar: &e/shop &8| &fJual Cepat: &e/sell"
```
