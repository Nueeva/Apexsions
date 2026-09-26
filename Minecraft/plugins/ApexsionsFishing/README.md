# ApexsionsFishing 🎣

> **Plugin Suite:** Apexsions
> **Brand:** `Apexsions`
> **Tagline:** `The Peak Civilizations`
> **Game Server Domain:** `apexsions.com:32348` (Java & Bedrock)
> **Web Platform Domain:** `https://web.apexsions.com`
> **Target Runtime:** Paper 26.2 (Minecraft 26.2, Java 21 LTS)

Modul ekosistem peradaban memancing resmi untuk **Apexsions — The Peak Civilizations**. Menghadirkan sistem AFK Fishing interaktif, Active Reel Engine, Rarity & Weight Engine 6-tier, Fishing Vault multi-halaman, pasar penjualan ikan dinamis dual-currency, sistem umpan virtual, custom auto-catch rods, ensiklopedia ikan (*Fish-o-pedia*), dan papan peringkat Top Angler terintegrasi kebijakan pengecualian staf.

---

## 🌟 Fitur Utama

- **Active Reel Engine & AFK Fishing:** Pengalaman memancing interaktif dengan kecepatan sambaran dinamis (*custom strike speed*) dan mekanisme auto-reel otomatis saat menggunakan joran spesial.
- **Rarity & Weight Engine 6-Tier:** Setiap tangkapan ikan memiliki kelangkaan (`COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`, `SECRET`) dan bobot spesimen nyata (kg) berbasis distribusi Gaussian.
- **Formula Penjualan Ikan Dinamis:** Harga jual ikan dihitung secara proporsional berdasarkan bobot tubuh ikan terhadap batas maksimalnya, menghasilkan nilai jual lebih tinggi untuk ikan raksasa (*trophy catch*).
- **Fishing Vault Storage 54-Slot (`/vault`):** Brankas penyimpanan ikan multi-halaman (hingga 30 halaman) dengan filter ketat (hanya menerima hasil pancingan). Pembelian halaman ekspansi menggunakan Rupiah (halaman 2-5) dan Diamond (halaman 6-30).
- **Auto-Catch Rods & Creator (`/fish shop` & `/fish creator`):** Berbagai varian joran dengan bonus auto-catch, syarat level progresi pemain, dan perlindungan penggabungan anvil.
- **Toko Umpan Virtual (`/fish bait`):** Kuota umpan virtual untuk meningkatkan peluang menangkap ikan langka atau legendaris.
- **Fish Journal (`/fish journal`):** Ensiklopedia ikan yang mencatat rekor tangkapan terberat pemain dan jenis ikan yang telah berhasil ditemukan.
- **Papan Peringkat Top Angler (`/fish top`):** Kompetisi nelayan terbaik dengan penyaringan otomatis akun staf dan entitas Aetherion.

---

## ⌨️ Daftar Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/fish` | `/fishing`, `/mancing` | Membuka Menu Utama Peradaban Memancing Apexsions | `apexsions.fishing.use` | `true` |
| `/vault` | `/fishvault`, `/fvault` | Membuka Fishing Vault brankas penyimpanan hasil tangkapan | `apexsions.fishing.vault` | `true` |
| `/fish shop` | `/fish toko` | Membuka Toko Joran Spesial & Auto-Catch Rods (`RodShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish vaultshop` | `/fish belibrankas` | Membuka Toko Peningkatan Kapasitas Brankas (`VaultShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish sell` | `/fish jual` | Membuka Antarmuka Penjualan Ikan & Delivery Market | `apexsions.fishing.use` | `true` |
| `/fish top` | `/fish leaderboard`, `/fish peringkat` | Papan peringkat Top Angler | `apexsions.fishing.use` | `true` |
| `/fish journal` | `/fish pedia`, `/fish jurnal` | Ensiklopedia spesies ikan & hasil tangkapan pemain | `apexsions.fishing.use` | `true` |
| `/fish bait` | `/fish umpan` | Membuka Toko Kuota Umpan Virtual (`BaitShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish admin` | - | Panel Administrasi Nelayan (Admin Hub GUI) | `apexsions.fishing.admin` | `op` |
| `/fish creator` | `/fish create` | Native Dialog Admin Rod Creator GUI | `apexsions.fishing.admin` | `op` |
| `/fish reload` | - | Memuat ulang konfigurasi ikan, rarity, bioma, dan bobot | `apexsions.fishing.admin` | `op` |

---

## 🔗 Dependensi
- **ApexsionsCore** (Required / Progression & Leaderboard Exemption)
- **ApexsionsEconomy** (Required / Transaksi penjualan ikan & toko)
- **ApexsionsCustomEnchants** (Softdepend / Kompatibilitas enchantment joran)
- **PlaceholderAPI** (Softdepend / Placeholder ekspansi nelayan)
