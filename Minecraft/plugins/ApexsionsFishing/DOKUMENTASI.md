# Dokumentasi Lengkap ApexsionsFishing

Panduan teknis resmi modul **`ApexsionsFishing`** untuk ekosistem peradaban memancing **Apexsions — The Peak Civilizations**. Modul ini mencakup sistem AFK Fishing, Active Reel Engine, Rarity & Weight Engine 6-tier, Fishing Vault Storage 54-slot, pasar penjualan ikan dual-currency, custom rods creator, dan integrasi papan peringkat nelayan terbaik (*Top Angler*).

---

## 📂 Struktur Direktori & Konfigurasi YAML Modular

```
plugins/ApexsionsFishing/
├── config.yml            <-- Pengaturan global AFK fishing, jeda reel, kedalaman air, dan batas halaman vault
├── loot.yml              <-- Definisi ikan, 6 tier rarity, rentang berat (kg), harga jual, dan bioma tangkapan
├── rods.yml              <-- Konfigurasi joran pancing khusus, bonus auto-reel, durabilitas, dan syarat level
├── baits.yml             <-- Definisi jenis umpan virtual, tier, pengganda peluang rarity & harga kuota
├── vault-prices.yml      <-- Skema harga unlock halaman Fishing Vault (Rupiah & Diamond)
└── plugin.yml            <-- Deklarasi commands, permissions, dan metadata plugin
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `/fish` | `/fishing`, `/mancing` | Membuka Menu Utama Peradaban Memancing Apexsions | `apexsions.fishing.use` | `true` |
| `/vault` | `/fishvault`, `/fvault` | Membuka Fishing Vault brankas penyimpanan hasil tangkapan | `apexsions.fishing.vault` | `true` |
| `/fish shop` | `/fish toko` | Membuka Toko Joran Spesial & Auto-Catch Rods (`RodShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish vaultshop` | `/fish belibrankas` | Membuka Toko Peningkatan Kapasitas Brankas (`VaultShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish sell` | `/fish jual` | Membuka Antarmuka Penjualan Ikan & Delivery Market (Dual-Currency) | `apexsions.fishing.use` | `true` |
| `/fish top` | `/fish leaderboard`, `/fish peringkat` | Papan peringkat Top Angler | `apexsions.fishing.use` | `true` |
| `/fish journal` | `/fish pedia`, `/fish jurnal` | Ensiklopedia spesies ikan & hasil tangkapan | `apexsions.fishing.use` | `true` |
| `/fish bait` | `/fish umpan` | Membuka Toko Kuota Umpan Virtual (`BaitShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish admin` | - | Panel Administrasi Nelayan (Admin Hub GUI) | `apexsions.fishing.admin` | `op` |
| `/fish creator` | `/fish create` | Native Dialog Admin Rod Creator GUI | `apexsions.fishing.admin` | `op` |
| `/fish reload`| - | Memuat ulang konfigurasi ikan, rarity, bioma, dan bobot tangkapan | `apexsions.fishing.admin` | `op` |

---

## 🐟 1. Rarity & Weight Engine 6-Tier

Setiap tangkapan ikan dikalkulasi secara dinamis berdasarkan parameter unik:

| Tier Kelangkaan | Peluang Relatif (chance-weight) | Karakteristik & Visual | Contoh Tangkapan |
| :---: | :---: | :--- | :--- |
| **COMMON** | 35.0 / 25.0 / 20.0 | Ikan konsumsi harian, bobot ringan (0.5 – 6.5 kg) | Lele Rawa, Mujair Kolam, Ikan Mas |
| **UNCOMMON** | 15.0 / 12.0 / 10.0 / 8.0 | Ikan sungai & muara, bobot sedang (2.0 – 9.0 kg) | Kakap Merah, Bandeng Laut, Salmon Liar |
| **RARE** | 4.5 / 3.5 | Ikan laut dalam, bobot tinggi | Tuna Sirip Biru, Kerapu Raksasa |
| **EPIC** | 1.8 / 1.2 | Ikan predator langka berharga tinggi (40.0 – 220.0 kg) | Pari Emas, Marlin Biru, Barakuda |
| **LEGENDARY** | 0.4 / 0.25 | Makhluk mitos pesisir kerajaan (150.0 – 800.0 kg) | Kraken Muda, Megalodon Bayi, Naga Danau |
| **SECRET** | 0.05 / 0.03 | Anomali kuno laut primordial (500.0 – 2000.0 kg) | Leviathan Purbakala, Abyssal Monarch |

### Formula Harga Jual Dinamis:
$$\text{Harga Jual Final} = \text{Base Price} \times \left(1 + \frac{\text{Weight} - \text{Min Weight}}{\text{Max Weight} - \text{Min Weight}} \times 0.5\right) \times M_{\text{Rod Bonus}}$$

- Memberikan nilai lebih tinggi untuk spesimen ikan yang berbobot lebih berat di kelasnya.
- Hasil penjualan disalurkan langsung secara atomic ke saldo **Rupiah** atau **Diamond** pemain melalui `ApexsionsEconomyAPI`.

---

## 🎣 2. Sistem AFK Fishing & Active Reel Engine

1. **Active Reel Engine:**
   - Saat kail bergerak dan pelampung tenggelam, pemain yang melakukan klik kanan tepat waktu mendapatkan bonus *Catch Quality* dan peluang lebih tinggi mendapatkan tier langka.
2. **AFK Fishing Automation (`AFKFishingService`):**
   - Mengizinkan pemain memancing otomatis saat standby menggunakan joran pancing bertipe *Auto-Catch Rod*.
   - **Syarat Kedalaman Air:** Kail wajib berada di air terbuka dengan kedalaman minimal 2 blok (`min-water-depth: 2`) untuk mencegah eksploitasi perangkap air sempit 1x1.
   - Dilengkapi proteksi durabilitas, efek suara ambient, dan partikel percikan air native.

---

## 📦 3. Fishing Vault Storage System (`/vault`)

1. **Brankas Ikan Khusus (Spesialisasi Kargo):**
   - Menampung hingga 30 halaman (54-slot per halaman).
   - Filter ketat (`VaultStorageManager`): Hanya menerima ikan, umpan, joran pancing, dan material hasil tangkapan laut. Item ilegal/blok bangunan biasa ditolak secara otomatis demi menjaga keteraturan brankas.
2. **Skema Pembelian Halaman (`vault-prices.yml`):**
   - Halaman 1: Gratis bawaan bagi seluruh warga baru (*Wanderer*).
   - Halaman 2–5: Dapat dibeli menggunakan mata uang **Rupiah** (`Rp 15.000` – `Rp 50.000`).
   - Halaman 6–30: Terbuka khusus donatur kasta tinggi atau dapat dibeli menggunakan **Diamond**.

---

## 🔗 4. Integrasi & Kebijakan Pengecualian Leaderboard

1. **Integrasi Ekonomi (`ApexsionsEconomy`):**
   - Transaksi jual beli ikan dan pembelian upgrade vault terhubung penuh ke perbankan atomic.
2. **Integrasi Progresi & Syarat Level (`ApexsionsCore`):**
   - Beberapa joran pancing kelas atas memerlukan batas level progresi tertentu (misal Minimal Level 25 atau Level 50) yang divalidasi via `ApexsionsCoreAPI`.
3. **Penyaringan Papan Peringkat Nelayan (*Top Angler Exemption*):**
   - Mengikuti **Kebijakan Pengecualian 6-Lapis** Apexsions. Seluruh akun staf (rank weight $\ge 80$), operator (OP), entitas transenden Aetherion, dan founder disaring keluar dari papan peringkat `/fish top`.
   - Menjamin trofi nelayan terhebat server murni diperebutkan oleh warga fana peradaban.
