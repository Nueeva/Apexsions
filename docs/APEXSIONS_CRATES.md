# ApexsionsCrates — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsCrates`** (Sistem Peti Hadiah, Animasi Berbasis Paket ProtocolLib/PacketEvents, Toko Kunci Dual-Currency `/crateshop`, Progresi Pity & Milestone, serta Integrasi Master Admin Hub).

> **Game Server Domain:** `apexsions.com:32348` (Java & Bedrock)
> **Web Platform Domain:** `https://web.apexsions.com`

---

## 📦 1. Ikhtisar Modul & Arsitektur

`ApexsionsCrates` adalah modul peti hadiah dan kunci resmi dalam ekosistem **Apexsions — The Peak Civilizations**. Modul ini menyediakan sistem hadiah berbobot probabilitas, toko pembelian kunci terintegrasi perbankan multi-currency, animasi pembukaan virtual berbasis manipulasi paket klien, dan jaminan hadiah milestone bagi pemain.

```
                           ┌────────────────────────┐
                           │    ApexsionsCrates     │
                           │(NightCore Crate Engine)│
                           └───────────┬────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          ▼                            ▼                            ▼
┌───────────────────┐        ┌───────────────────┐        ┌───────────────────┐
│ Crate Key Shop    │        │ Packet Animations │        │ Milestone & Pity  │
│Dual-Currency /eco │        │ProtocolLib/Packets│        │Persistent Progress│
│Rupiah & Diamond   │        │Zero-Lag Display   │        │Guaranteed Rewards │
└───────────────────┘        └───────────────────┘        └───────────────────┘
```

---

## 💎 2. Toko Kunci Dual-Currency (`/crateshop`)

- **Dukungan Dua Mata Uang**:
  - Kunci peti dapat dibeli menggunakan **Rupiah** (mata uang standar) atau **Diamond** (hard currency).
  - Menggunakan transaksi atomik `ApexsionsEconomyAPI` yang aman dari *race condition* dan eksploitasi duplikasi saldo.
- **Konfigurasi Toko Fleksibel**:
  - Konfigurasi harga, jumlah kunci per pembelian, dan batasan harian dapat disesuaikan pada `shop/keyshop.yml`.
  - Admin dapat mengubah parameter toko langsung dari dalam game via `/crateshop admin`.

---

## 🎬 3. Mesin Animasi Pembukaan Berbasis Paket

- **Zero-Lag Packet Manipulation**:
  - Mengirimkan paket entitas palsu (*virtual display entities*) langsung ke koneksi klien pemain menggunakan PacketEvents & ProtocolLib.
  - Entitas tidak didaftarkan ke dunia server (*world entity tracker*), sehingga ratusan pembukaan peti simultan tidak menyebabkan penurunan MSPT (Milliseconds Per Tick) server.
- **Tampilan Hologram**:
  - Blok peti fisik di dunia nyata dihiasi hologram dinamis yang menampilkan nama peti, tier kelangkaan, dan jumlah kunci pemain yang tersisa.
- **Efek Audio-Visual**:
  - Suara klik roda putar roulette (*roulette tick*) yang melambat secara realistis sebelum berhenti di item hadiah pemenang.

---

## 🏆 4. Sistem Milestone & Mesin Pity

- **Perhitungan Akumulatif Persisten**:
  - Setiap pembukaan peti dicatat secara persisten ke database per UUID pemain.
- **Jaminan Pity (Bad-Luck Protection)**:
  - Mengeliminasi frustrasi pemain dengan memberikan hadiah kelas *Legendary* atau *Apex* setelah sejumlah pembukaan tertentu jika pemain belum mendapatkannya.
- **Reward Dispatcher Multi-Tipe**:
  - Mendukung hadiah berupa item kustom berkekuatan sihir (`ApexsionsCustomEnchants`), deposit saldo ekonomi, perintah konsol, atau koin pass musiman (`ApexsionsBattlepass`).

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/crateshop` | `/keyshop`, `/cratekeyshop` | Membuka Toko Kunci Peti Dual-Currency GUI | `apexsionscrates.command.menu` | `true` |
| `/crate` | `/crates` | Menampilkan antarmuka daftar peti hadiah dan milestone | `apexsionscrates.command.menu` | `true` |
| `/crate open <id>` | - | Membuka peti hadiah tertentu secara langsung | `apexsionscrates.command.open` | `true` |
| `/crate preview <id>` | - | Pratinjau daftar hadiah peti dan peluangnya | `apexsionscrates.command.preview` | `true` |
| `/crate key <give\|take\|set\|show> <p> <crate> <amt>` | - | Mengelola jumlah kunci peti virtual pemain | `apexsionscrates.command.key` | `op` |
| `/crate give <p> <crate> [amt]` | - | Memberikan item/kunci fisik peti kepada pemain | `apexsionscrates.command.give` | `op` |
| `/crate editor` | - | Editor interaktif peti, hadiah, dan milestone | `apexsionscrates.command.editor` | `op` |
| `/crate reload` | - | Memuat ulang seluruh konfigurasi peti dan toko | `apexsionscrates.command.reload` | `op` |

---

## 🧩 6. Integrasi Ekosistem & SPI

Modul terintegrasi secara otomatis saat startup:
- **ApexsionsCore**: Mendaftarkan `ApexsionsCratesAdminModule` ke dalam Master Admin Hub (`/admingui`).
- **ApexsionsEconomy**: Mengonsumsi `ApexsionsEconomyAPI` untuk pengecekan dan penarikan saldo pemain.
- **PlaceholderAPI**: Mengekspos placeholder kunci, sisa cooldown, dan milestone progresi pemain (`%apexsionscrates_*%`).
