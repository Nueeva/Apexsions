# ApexsionsShop — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsShop`** (Sistem Toko 6 Kategori, Pasar Dinamis Cuaca & Pasokan, Price Clamping 85%-120%, Siaran Tren Pasar, Pajak Kerajaan Per-Wilayah, GUI Ramah Bedrock/Touchscreen, dan GUI Jual Cepat 45-Slot `/sell`).

> **Game Server Domain:** `apexsions.com:32348` (Java & Bedrock)
> **Web Platform Domain:** `https://web.apexsions.com`

---

## 🛒 1. Ikhtisar Modul & Arsitektur

`ApexsionsShop` mengelola ekosistem perdagangan dinamis server, di mana harga komoditas berfluktuasi secara cerdas berdasarkan kondisi alam, spesialisasi wilayah kerajaan pembeli, ketersediaan pasokan (*Supply & Demand Curve*), dan batas pengaman anti-inflasi (*Price Clamping*).

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
│Blocks/Farming/Ore│         │Clamping 85%-120%, │         │Bottom Bar Nav,    │
│Food/Drops/Dyes   │         │Market Broadcast   │         │45-Slot Quick Sell │
└──────────────────┘         └───────────────────┘         └───────────────────┘
```

---

## 📦 2. Enam Kategori Toko Terorganisir

1. **`blocks.yml`**: Blok bangunan, batu alam, kayu, terraform, tanah liat, dan kaca.
2. **`farming.yml`**: Bibit, gandum, wortel, tebu, bambu, dan hasil perkebunan.
3. **`food.yml`**: Daging matang, roti, kue, sup, dan makanan olahan.
4. **`ores.yml`**: Bijih mineral, batubara, besi, emas, diamond, netherite, dan ingot. Diamond dikunci sebagai komoditas *Sell-Only* (`buy-enabled: false`, harga jual Rp 250/butir).
5. **`mob_drops.yml`**: Drop monster (*Bones, Gunpowder, Ender Pearl, Blaze Rod, Leather*).
6. **`dyes.yml`**: 16 varian warna pewarna dekoratif.

---

## 📈 3. Formula Harga Dinamis & Ekonomi Berkelanjutan

$$\text{Harga Final} = \text{Clamp}_{85\%}^{120\%}(\text{Harga Dasar} \times M_{\text{Cuaca}} \times M_{\text{Kerajaan}} \times M_{\text{Pasokan}}) \pm \text{Pajak}$$

- **Rasio Jual Bawaan**: **20%** dari harga beli dasar (Solterra mendapatkan keunggulan rasio jual bijih **30%** terkalibrasi).
- **Price Clamping (Batas Pengaman)**: Harga beli efektif dijamin tidak akan pernah jatuh di bawah **85%** atau melambung melampaui **120%** dari harga dasar (`markets.yml`: `min-buy-ratio: 0.85`, `max-buy-ratio: 1.20`).
- **Siaran Tren Pasar Otomatis (`MarketBroadcastService`)**: Pengumuman berkala MiniMessage mengenai komoditas yang sedang 'BOOM' (harga naik) atau 'DIP' (harga anjlok).
- **Multiplier Cuaca ($M_{\text{Cuaca}}$)**: Cuaca cerah meningkatkan harga jual pertanian (1.10); hujan menurunkan pertanian (0.98) namun menaikkan mob drops (1.05); badai petir memaksimalkan mob drops (1.15) dan menurunkan pertanian (0.95).
- **Spesialisasi Kerajaan ($M_{\text{Kerajaan}}$)**: Diskon komoditas khusus untuk warga kerajaan pemilik bioma (Zenithar, Solterra, Sylvamoor).
- **Pajak Kerajaan**: Diambil dari `ApexsionsCore` per wilayah (Zenithar 18%, Solterra 20%, Sylvamoor 15%; fallback default 10%) dan secara otomatis disalurkan ke kas kerajaan pembeli.

---

## 📱 4. Desain GUI Ramah Sentuh & Bedrock (Touch-Friendly)

- **Navigasi Baris Bawah**: Tombol kembali (*Back*), tutup (*Close*), dan halaman (*Pagination*) diletakkan di baris terbawah agar mudah dijangkau oleh pemain mobile / controller Bedrock.
- **GUI Jual Cepat 45-Slot (`/sell` / `/sellgui`)**: Pemain dapat mendrop banyak item sekaligus ke dalam 45 slot inventaris, dan sistem akan mengalkulasi total keuntungan secara instan saat tombol konfirmasi ditekan.

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/shop` | `/pasar`, `/toko`, `/store`, `/bazar` | Membuka menu utama 6 kategori toko | `apexsionsshop.use` | `true` |
| `/shop trends` | - | Membuka dashboard visual tren pasar & fluktuasi | `apexsionsshop.use` | `true` |
| `/shop <kategori>` | `/pasar <kat>` | Membuka kategori toko tertentu secara langsung | `apexsionsshop.use` | `true` |
| `/shop reload` | `/pasar reload` | Memuat ulang konfigurasi kategori, pasar, dan GUI | `apexsionsshop.admin` | `op` |
| `/sell` | `/sellgui`, `/jual` | Membuka GUI jual cepat 45-slot | `apexsionsshop.sell` | `true` |
| `/sellall` | `/jualsemua` | Menjual seluruh item yang cocok di inventaris | `apexsionsshop.sell` | `true` |
| `/sellhand` | `/jualtangan` | Menjual item yang sedang dipegang di tangan utama | `apexsionsshop.sell` | `true` |
