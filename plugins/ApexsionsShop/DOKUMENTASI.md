# Dokumentasi Lengkap ApexsionsShop

Panduan teknis resmi modul **`ApexsionsShop`** untuk sistem pasar dinamis 6 kategori, formula harga berbasis cuaca & bioma kerajaan, batas anti-inflasi (*Price Clamping*), siaran tren pasar, pajak teritorial 10%, dan antarmuka GUI sentuh/Bedrock.

---

## 📂 Struktur Konfigurasi YAML Modular

```
plugins/ApexsionsShop/
├── config.yml            <-- Opsi global, rasio jual (20%), pajak (10%), dan fallback economy
├── gui.yml               <-- Tata letak GUI dan dekorasi tombol ramah sentuh
├── markets.yml           <-- Multiplier spesialisasi wilayah kerajaan dan cuaca
├── messages.yml          <-- Kumpulan template pesan visual MiniMessage
└── categories/
    ├── blocks.yml        <-- Kategori blok bangunan, tanah, batu, dan kaca
    ├── farming.yml       <-- Kategori bibit, hasil panen, dan perkebunan
    ├── food.yml          <-- Kategori makanan siap santap dan bahan olahan
    ├── ores.yml          <-- Kategori bijih mineral mentah dan ingot mulia
    ├── mob_drops.yml     <-- Kategori drop monster dan hewan buruan
    └── dyes.yml          <-- Kategori 16 varian warna pewarna dekoratif
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/shop` | `/pasar`, `/toko`, `/store`, `/bazar` | Membuka menu utama 6 kategori toko | `apexsionsshop.use` | `true` |
| `/shop trends` | - | Membuka dashboard visual tren pasar & fluktuasi | `apexsionsshop.use` | `true` |
| `/shop <kategori>` | `/pasar <kat>` | Membuka langsung kategori toko tertentu | `apexsionsshop.use` | `true` |
| `/shop reload` | `/pasar reload` | Memuat ulang seluruh konfigurasi, formula, & kategori | `apexsionsshop.admin` | `op` |
| `/sell` | `/sellgui`, `/jual` | Membuka GUI jual cepat 45-slot drag-and-drop | `apexsionsshop.sell` | `true` |
| `/sellall` | `/jualsemua` | Menjual seluruh item yang cocok di inventaris | `apexsionsshop.sell` | `true` |
| `/sellhand` | `/jualtangan` | Menjual item yang sedang dipegang di tangan utama | `apexsionsshop.sell` | `true` |

---

## 📈 Formula Pasar Dinamis & Batas Pengaman Anti-Inflasi

$$\text{Harga Final} = \text{Clamp}_{50\%}^{200\%}(\text{Harga Dasar} \times M_{\text{Cuaca}} \times M_{\text{Kerajaan}} \times M_{\text{Pasokan}}) \pm \text{Pajak}$$

1. **Rasio Jual Bawaan**: **20%** dari harga beli dasar.
2. **Price Clamping (50% - 200%)**: Harga satuan efektif tidak akan pernah jatuh di bawah 50% atau melambung melampaui 200% dari harga dasar, melindungi stabilitas ekonomi jangka panjang.
3. **Multiplier Cuaca ($M_{\text{Cuaca}}$)**:
   - Hujan lebat meningkatkan permintaan hasil panen pertanian (+15%).
   - Badai petir meningkatkan kelangkaan komoditas mineral ore (+25%).
4. **Spesialisasi Kerajaan ($M_{\text{Kerajaan}}$)**: Diskon komoditas khusus untuk warga kerajaan pemilik bioma terkait (Zenithar, Solterra, Sylvamoor via `ApexsionsCoreAPI`).
5. **Pajak Kerajaan 10%**: Otomatis disalurkan ke kas perbendaharaan kerajaan pemain.
6. **Siaran Tren Pasar (`MarketBroadcastService`)**: Pengumuman berkala MiniMessage mengenai komoditas yang sedang naik (*BOOM*) atau turun (*DIP*).

---

## 📱 Antarmuka GUI Ramah Sentuh & Bedrock

- **Navigasi Baris Bawah (Bottom-Bar Navigation)**: Tombol kembali (*Back*), tutup (*Close*), dan halaman (*Pagination*) diletakkan di baris ke-6 (Slot 45-53) agar mudah dijangkau oleh pemain mobile / controller Bedrock.
- **GUI Jual Cepat 45-Slot (`/sell`)**: Pemain dapat meletakkan banyak tumpukan item sekaligus, dan total perolehan Rupiah dikalkulasi secara instan saat konfirmasi.

---

## 🧩 Akses Public API (`ApexsionsShopAPI`)

```java
ApexsionsShopAPI shopApi = ApexsionsShopProvider.get();
if (shopApi != null) {
    PriceResult buyPrice = shopApi.calculateBuyPrice(shopItem, player, 64);
    PriceResult sellPrice = shopApi.calculateSellPrice(shopItem, player, 64);
    shopApi.openShop(player);
}
```
