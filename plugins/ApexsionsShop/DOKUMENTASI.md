# Dokumentasi Lengkap ApexsionsShop

Panduan teknis modul `ApexsionsShop` untuk sistem toko kategori, pasar dinamis, formula harga cuaca/pasokan, pajak kerajaan, dan antarmuka GUI sentuh/Bedrock.

---

## 🌟 Fitur Utama & Mekanisme
1. **6 Kategori Toko Terorganisir**:
   - `blocks.yml`: Blok bangunan, batu, kayu, terraform, dan kaca.
   - `farming.yml`: Hasil panen, bibit, dan produk alam.
   - `food.yml`: Makanan siap santap dan bahan olahan.
   - `ores.yml`: Hasil tambang berharga, mineral mentah, dan ingot.
   - `mob_drops.yml`: Hasil drop monster dan hewan.
   - `dyes.yml`: Pewarna dan varian dekoratif.
2. **Formula Harga Dinamis (*Dynamic Pricing Formula*)**:
   $$\text{Harga Final} = (\text{Harga Dasar} \times M_{\text{Cuaca}} \times M_{\text{Kerajaan}} \times M_{\text{Pasokan}}) \pm \text{Pajak}$$
   - **Rasio Jual Bawaan**: $20\%$ dari harga beli.
   - **Multiplier Cuaca**: Hujan meningkatkan harga komoditas pertanian; badai meningkatkan kelangkaan ore.
   - **Spesialisasi Kerajaan**: Diskon untuk komoditas unggulan kerajaan pembeli (`ApexsionsCoreAPI`).
   - **Pajak Kerajaan**: Pajak sebesar 10% masuk ke kas kerajaan/server.
3. **Antarmuka GUI Ramah Sentuh (Touch / Bedrock Friendly)**:
   - Tombol kontrol navigasi di baris bawah inventaris.
   - GUI Jual Cepat 45-slot (`/sell` / `/sellgui`) untuk drag-and-drop item secara massal.

---

## 📂 Struktur Konfigurasi YAML
```
plugins/ApexsionsShop/
├── config.yml            <-- Opsi global, rasio jual, pajak, dan fallback economy
├── gui.yml               <-- Layout dan dekorasi tombol GUI
├── markets.yml           <-- Multiplier spesialisasi wilayah kerajaan
├── messages.yml          <-- Pesan feedback MiniMessage
└── categories/
    ├── blocks.yml
    ├── farming.yml
    ├── food.yml
    ├── ores.yml
    ├── mob_drops.yml
    └── dyes.yml
```

---

## ⚡ Perintah & Permissions
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/shop` | Membuka menu utama 6 kategori toko | `apexsshop.use` |
| `/shop <kategori>` | Membuka kategori toko spesifik | `apexsshop.use` |
| `/sell` / `/sellgui` | Membuka menu jual instan 45-slot | `apexsshop.sell` |
| `/sellall` | Menjual seluruh item yang cocok di inventaris | `apexsshop.sell` |
| `/sellhand` | Menjual item yang sedang dipegang di tangan utama | `apexsshop.sell` |
| `/shop reload` | Memuat ulang seluruh konfigurasi dan kategori | `apexsshop.admin` |

---

## 🧩 Integrasi API
- `ApexsionsShopProvider.get()`: Mengakses instance `ApexsionsShopAPI`.
- Terhubung secara langsung ke `ApexsionsEconomyAPI` dan `ApexsionsCoreAPI`.
