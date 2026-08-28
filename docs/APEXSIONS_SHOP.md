# ApexsionsShop — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsShop`** (Sistem Toko 6 Kategori, Pasar Dinamis Cuaca & Pasokan, Pajak Kerajaan 10%, GUI Ramah Bedrock/Touchscreen, dan GUI Jual Cepat 45-Slot `/sell`).

---

## 🛒 1. Ikhtisar Modul & Arsitektur

`ApexsionsShop` mengelola ekosistem perdagangan dinamis server, di mana harga komoditas berfluktuasi secara cerdas berdasarkan kondisi alam, spesialisasi wilayah kerajaan pembeli, dan ketersediaan pasokan.

```
                           ┌────────────────────────┐
                           │     ApexsionsShop      │
                           │   (Dynamic Markets)    │
                           └───────────┬────────────┘
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
┌──────────────────┐         ┌───────────────────┐         ┌───────────────────┐
│ 6 Shop Categories│         │  Dynamic Pricing  │         │  Bedrock GUI Hub  │
│Blocks/Farming/Ore│         │Weather Multiplier,│         │Bottom Bar Nav,    │
│Food/Drops/Dyes   │         │Kingdom Bias, Tax  │         │45-Slot Quick Sell │
└──────────────────┘         └───────────────────┘         └───────────────────┘
```

---

## 📦 2. Enam Kategori Toko Terorganisir

1. **`blocks.yml`**: Blok bangunan, batu alam, kayu, terraform, tanah liat, dan kaca.
2. **`farming.yml`**: Bibit, gandum, wortel, tebu, bambu, dan hasil perkebunan.
3. **`food.yml`**: Daging matang, roti, kue, sup, dan makanan olahan.
4. **`ores.yml`**: Bijih mineral, batubara, besi, emas, diamond, netherite, dan ingot.
5. **`mob_drops.yml`**: Drop monster (*Bones, Gunpowder, Ender Pearl, Blaze Rod, Leather*).
6. **`dyes.yml`**: 16 varian warna pewarna dekoratif.

---

## 📈 3. Formula Harga Dinamis & Ekonomi Berkelanjutan

Untuk mencegah inflasi dan mendorong perdagangan strategis, harga ditentukan dengan formula:

$$\text{Harga Final} = (\text{Harga Dasar} \times M_{\text{Cuaca}} \times M_{\text{Kerajaan}} \times M_{\text{Pasokan}}) \pm \text{Pajak}$$

- **Rasio Jual Bawaan**: **20%** dari harga beli (mencegah eksploitasi perputaran uang instan).
- **Multiplier Cuaca ($M_{\text{Cuaca}}$)**:
  - Hujan lebat meningkatkan permintaan hasil pertanian (+15%).
  - Badai petir (*Thunderstorm*) meningkatkan harga ore langka (+25%).
- **Spesialisasi Kerajaan ($M_{\text{Kerajaan}}$)**:
  - Pemain yang berbelanja komoditas unggulan kerajaannya sendiri (misal Zenithar membeli ore) mendapatkan potongan harga khusus.
- **Pajak Kerajaan (10%)**: Pajak 10% dipotong dari setiap transaksi untuk kas kerajaan/server.

---

## 📱 4. Desain GUI Ramah Sentuh & Bedrock (Touch-Friendly)

- **Navigasi Baris Bawah**: Tombol kembali (*Back*), tutup (*Close*), dan halaman (*Pagination*) diletakkan di baris terbawah agar mudah dijangkau oleh pemain mobile / controller Bedrock.
- **GUI Jual Cepat 45-Slot (`/sell` / `/sellgui`)**: Pemain dapat mendrop banyak item sekaligus ke dalam 45 slot inventaris, dan sistem akan mengalkulasi total keuntungan secara instan saat tombol konfirmasi ditekan.

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/shop` | `/pasar`, `/toko`, `/store` | Membuka menu utama 6 kategori toko | `apexsionsshop.use` | `true` |
| `/shop <kategori>` | `/pasar <kat>` | Membuka kategori toko tertentu secara langsung | `apexsionsshop.use` | `true` |
| `/sell` | `/sellgui`, `/jual` | Membuka GUI jual cepat 45-slot | `apexsionsshop.sell` | `true` |
| `/sellall` | `/jualsemua` | Menjual seluruh item yang cocok di inventaris | `apexsionsshop.sell` | `true` |
| `/sellhand` | `/jualtangan` | Menjual item yang sedang dipegang di tangan utama | `apexsionsshop.sell` | `true` |
| `/shop reload` | `/pasar reload` | Memuat ulang konfigurasi kategori, pasar, dan GUI | `apexsionsshop.admin` | `op` |

---

## 🧩 6. Integrasi API Publik

```java
// Mendapatkan instance API toko
ApexsionsShopAPI shopAPI = ApexsionsShopProvider.get();

// Mengambil harga dinamis item
double buyPrice = shopAPI.calculateBuyPrice(material, player);
double sellPrice = shopAPI.calculateSellPrice(material, player);
```
