# ApexsionsFishing — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsFishing`** (Sistem Memancing Interaktif & AFK Fishing, Rarity & Weight Engine 6-Tier, Fishing Vault Brankas 54-Slot Multi-Halaman, Toko Joran Spesial, Pasar Penjualan Ikan Dinamis, dan Papan Peringkat Top Angler).

---

## 🎣 1. Ikhtisar Modul & Arsitektur

`ApexsionsFishing` adalah modul peradaban memancing komprehensif yang dirancang untuk **Apexsions — The Peak Civilizations**. Modul ini memadukan mekanik memancing aktif (*Active Reel*) dengan sistem AFK berpenghasilan terukur, sistem penyimpanan brankas ikan khusus, pasar penjualan dual-currency, serta papan peringkat nelayan terbaik yang terlindungi dari akun staf dan admin.

```
                           ┌────────────────────────┐
                           │    ApexsionsFishing    │
                           │ (Civilization Fishing) │
                           └───────────┬────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          ▼                            ▼                            ▼
┌───────────────────┐        ┌───────────────────┐        ┌───────────────────┐
│ Active & AFK Reel │        │ 6-Tier Weight Lore│        │ Dual-Currency Shop│
│Strike Speed, Joran│        │Gaussian Kg Engine │        │Sell Market & Vault│
│Auto-Catch Multipl.│        │Common -> Secret   │        │Leaderboard Exclude│
└───────────────────┘        └───────────────────┘        └───────────────────┘
```

---

## 🐟 2. Rarity & Weight Engine 6-Tier

Setiap ikan yang berhasil ditangkap memiliki identitas unik berupa nama spesies, tingkatan kelangkaan, bioma penangkapan, dan berat badan spesimen (kg) yang dikalkulasi menggunakan fungsi distribusi Gauss (*Gaussian Weight Curve*):

| Tier Kelangkaan | Peluang Relatif | Karakteristik Bobot | Contoh Spesies Ikan |
| :---: | :---: | :---: | :--- |
| **COMMON** | 35.0% - 20.0% | Bobot ringan (0.5 – 6.5 kg) | Lele Rawa, Mujair Kolam, Ikan Mas, Nila |
| **UNCOMMON** | 15.0% - 8.0% | Bobot sedang (2.0 – 9.0 kg) | Kakap Merah, Bandeng Laut, Salmon Liar |
| **RARE** | 4.5% - 3.5% | Bobot tinggi (10.0 – 50.0 kg) | Tuna Sirip Biru, Kerapu Raksasa |
| **EPIC** | 1.8% - 1.2% | Predator laut dalam (40.0 – 220.0 kg) | Pari Emas, Marlin Biru, Barakuda |
| **LEGENDARY** | 0.4% - 0.25% | Makhluk mitos pesisir (150.0 – 800.0 kg) | Kraken Muda, Megalodon Bayi, Naga Danau |
| **SECRET** | 0.05% - 0.03% | Anomali kuno primordial (500.0 – 2000.0 kg) | Leviathan Purbakala, Abyssal Monarch |

---

## 📈 3. Formula Penjualan Ikan Dinamis

$$\text{Harga Jual Final} = \text{Base Price} \times \left(1 + \frac{\text{Weight} - \text{Min Weight}}{\text{Max Weight} - \text{Min Weight}} \times 0.5\right) \times M_{\text{Rod Bonus}}$$

- **Apresiasi Bobot**: Ikan dengan berat mendekati batas maksimal (*Max Weight*) bernilai hingga 50% lebih mahal dibandingkan spesimen terkecil dari spesies yang sama.
- **Pencairan Atomic**: Saldo penjualan disalurkan seketika ke akun pemain melalui `ApexsionsEconomyAPI.deposit(playerUuid, "rupiah", totalRevenue)`.

---

## 🗄️ 4. Fishing Vault Storage Multi-Halaman (`/vault`)

- **Kapasitas Masif**: Mendukung hingga **30 halaman** penyimpanan 54-slot per pemain.
- **Filter Ketat (Fishing-Only Validation)**: Hanya menerima item tangkapan ikan resmi dari ekosistem Apexsions. Seluruh item lain (blok, senjata, armor vanilla) otomatis ditolak saat dipindahkan ke dalam vault.
- **Skema Buka Kunci Dual-Currency (`/fish vaultshop`)**:
  - Halaman 2 hingga 5: Dibeli menggunakan **Rupiah** (biaya berjenjang Rp 25.000 s/d Rp 200.000).
  - Halaman 6 hingga 30: Dibeli menggunakan **Diamond** (biaya berjenjang 25 💎 s/d 500 💎).

---

## 🎣 5. Sistem Joran Auto-Catch & Umpan Virtual

- **Auto-Catch Rods (`/fish shop`)**: Joran khusus dengan kemampuan menarik kail secara otomatis tanpa interaksi pemain saat ikan menyambar kail.
- **Bait System (`/fish bait`)**: Kuota umpan virtual (*virtual bait counter*) yang memberikan pengganda peluang (*chance multiplier*) menangkap ikan tier Rare, Epic, Legendary, dan Secret.
- **Admin Rod Creator (`/fish creator`)**: Antarmuka dialog terintegrasi untuk membuat joran pancing baru dengan model khusus, level requirement, dan durabilitas kustom.

---

## 🏆 6. Papan Peringkat Top Angler & Pengecualian Staf

- **Leaderboard Nelayan (`/fish top`)**: Menampilkan 10 nelayan dengan tangkapan terberat dan akumulasi poin pancingan tertinggi.
- **Kebijakan Pengecualian 6-Lapis**: Sesuai tata kelola repositori, seluruh akun staf (bobot LuckPerms $\ge 80$), operator (OP), dan entitas Aetherion otomatis disaring keluar dari leaderboard menggunakan `ApexsionsCoreAPI.isLeaderboardExempt(uuid)`.

---

## ⚡ 7. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/fish` | `/fishing`, `/mancing` | Membuka Menu Utama Peradaban Memancing Apexsions | `apexsions.fishing.use` | `true` |
| `/vault` | `/fishvault`, `/fvault` | Membuka Fishing Vault brankas penyimpanan hasil tangkapan | `apexsions.fishing.vault` | `true` |
| `/fish shop` | `/fish toko` | Membuka Toko Joran Spesial & Auto-Catch Rods (`RodShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish vaultshop` | `/fish belibrankas` | Membuka Toko Peningkatan Kapasitas Brankas (`VaultShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish sell` | `/fish jual` | Membuka Antarmuka Penjualan Ikan & Delivery Market | `apexsions.fishing.use` | `true` |
| `/fish top` | `/fish leaderboard`, `/fish peringkat` | Papan peringkat Top Angler | `apexsions.fishing.use` | `true` |
| `/fish journal` | `/fish pedia`, `/fish jurnal` | Ensiklopedia spesies ikan & hasil tangkapan | `apexsions.fishing.use` | `true` |
| `/fish bait` | `/fish umpan` | Membuka Toko Kuota Umpan Virtual (`BaitShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish admin` | - | Panel Administrasi Nelayan (Admin Hub GUI) | `apexsions.fishing.admin` | `op` |
| `/fish creator` | `/fish create` | Native Dialog Admin Rod Creator GUI | `apexsions.fishing.admin` | `op` |
| `/fish reload` | - | Memuat ulang konfigurasi ikan, rarity, bioma, dan bobot | `apexsions.fishing.admin` | `op` |
