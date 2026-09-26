# Dokumentasi Lengkap ApexsionsCrates

Panduan teknis resmi modul **`ApexsionsCrates`** untuk ekosistem peti hadiah, kunci dual-currency, animasi berbasis paket (ProtocolLib / PacketEvents), milestone pity progression, dan integrasi Master Admin Hub **Apexsions — The Peak Civilizations**.

> **Game Server Domain:** `apexsions.com:32348` (Java & Bedrock)
> **Web Platform Domain:** `https://web.apexsions.com`

---

## 📂 Struktur Direktori & Konfigurasi YAML Modular

```
plugins/ApexsionsCrates/
├── config.yml            <-- Opsi global, batas jeda animasi, pengaturan database/cache
├── plugin.yml            <-- Deklarasi commands, permissions, dependensi (nightcore)
├── crates/               <-- Konfigurasi individual peti hadiah (Loot, Animasi, Hologram)
│   ├── bronze.yml        <-- Peti perunggu (Pemain baru & voting)
│   ├── silver.yml        <-- Peti perak (Hadiah mingguan & event)
│   ├── golden.yml        <-- Peti emas (Donatur & milestone)
│   └── apex.yml          <-- Peti kedaulatan tertinggi (Apex Milestone)
├── keys/                 <-- Definisi kunci fisik dan virtual per-crate
│   └── keys.yml
├── shop/                 <-- Konfigurasi Toko Pembelian Kunci Peti (/crateshop)
│   └── keyshop.yml       <-- Harga kunci (Rupiah & Diamond), slot GUI, dan batas beli
└── milestones/           <-- Progresi milestone dan jaminan pity reward
    └── milestones.yml
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/crateshop` | `/keyshop`, `/cratekeyshop` | Membuka Toko Kunci Peti Dual-Currency GUI | `apexsionscrates.command.menu` | `true` |
| `/crate` | `/crates` | Menampilkan antarmuka daftar peti hadiah dan milestone | `apexsionscrates.command.menu` | `true` |
| `/crate open <id>` | - | Membuka peti hadiah tertentu secara langsung | `apexsionscrates.command.open` | `true` |
| `/crate preview <id>` | - | Membuka GUI pratinjau seluruh hadiah dan probabilitas | `apexsionscrates.command.preview` | `true` |
| `/crate key <give\|take\|set\|show> <p> <crate> <amt>` | - | Mengelola jumlah kunci peti virtual pemain | `apexsionscrates.command.key` | `op` |
| `/crate give <p> <crate> [amt]` | - | Memberikan item/kunci fisik peti kepada pemain | `apexsionscrates.command.give` | `op` |
| `/crate editor` | - | Editor interaktif peti, peluang hadiah, dan milestone | `apexsionscrates.command.editor` | `op` |
| `/crate reload` | - | Memuat ulang seluruh konfigurasi peti dan toko | `apexsionscrates.command.reload` | `op` |

---

## 💎 1. Toko Kunci Dual-Currency (`/crateshop`)

Sistem pembelian kunci resmi terintegrasi langsung dengan perbankan `ApexsionsEconomy`:

1. **Metode Pembayaran Fleksibel**:
   - Setiap tipe kunci peti dapat dikonfigurasi berharga **Rupiah** (mata uang standar) atau **Diamond** (hard currency).
   - Validasi saldo dan pemotongan dilakukan secara non-blocking dan atomik via `ApexsionsEconomyAPI`.
2. **Keamanan Transaksi**:
   - Mencegah *race condition* atau klik ganda (*spam click*) dengan sistem lock sesi pemain selama transaksi diproses.
   - Kunci yang dibeli langsung ditambahkan ke inventaris pemain (jika kunci fisik) atau dikreditkan ke saldo kunci virtual pemain.
3. **Integrasi Admin**:
   - Memungkinkan admin mengubah harga jual dan mata uang kunci secara langsung melalui antarmuka editor `/crateshop admin`.

---

## 🎬 2. Sistem Animasi Berbasis Paket & Hologram

- **Packet-Level Rendering**: Menggunakan packet injection ProtocolLib dan PacketEvents untuk merender display item dan efek partikel berputar. Hal ini menjamin server tidak membebani entity tracker Bukkit utama.
- **Hologram Interaktif**: Menampilkan nama peti, kunci yang dibutuhkan, dan instruksi klik kanan/kiri di atas blok peti dunia nyata.
- **Efek Suara & Partikel**: Mengiringi proses pemilihan acak (tick sound) dan dentuman meriah saat pemain memperoleh hadiah kelangkaan tinggi (*Legendary* / *Apex*).

---

## 🏆 3. Sistem Milestone Rewards & Pity Progression

Untuk memberikan rasa keadilan bagi pemain, sistem dilengkapi mesin pity terukur:

- **Persistent Openings Counter**: Setiap peti yang dibuka mencatat penghitung akumulasi di database.
- **Milestone Guarantee**: Jika pemain membuka peti sebanyak $N$ kali tanpa mendapatkan hadiah teratas, sistem pity secara otomatis menjamin pemberian hadiah milestone pada pembukaan berikutnya.
- **PlaceholderAPI Hook**: Statistik milestone dapat dipantau di papan skor dan profil menggunakan `%apexsionscrates_milestone_next%` dan `%apexsionscrates_openings_total%`.

---

## 🧩 4. Integrasi Master Admin Hub (`/admingui`)

Modul `ApexsionsCrates` otomatis mendaftarkan `ApexsionsCratesAdminModule` ke dalam `ApexsionsCoreProvider.get().getAdminHubManager()`:
- Slot khusus di Master Admin Hub menampilkan status modul, total peti aktif, dan akses cepat ke `/crate editor` serta `/crateshop reload`.
