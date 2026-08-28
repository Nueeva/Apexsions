# Dokumentasi Ekosistem Apexsions Plugin Suite — Minecraft 1.21.4

Dokumentasi induk yang menjelaskan arsitektur menyeluruh, integrasi antar-plugin, matriks permission, perintah admin, dan alur gameplay server **Apexsions**.

---

## 🏛️ 1. Ikhtisar Ekosistem (Plugin Matrix)

Ekosistem Apexsions terdiri dari **5 plugin spesifik berkinerja tinggi** yang saling terhubung secara modular:

```
                               ┌────────────────────────┐
                               │     ApexsionsCore      │
                               │  (Kingdom, Level, RTP) │
                               └───────────┬────────────┘
                                           │
         ┌─────────────────────────┬───────┴─────────┬─────────────────────────┐
         ▼                         ▼                 ▼                         ▼
┌──────────────────┐      ┌─────────────────┐ ┌───────────────────┐ ┌───────────────────┐
│  ApexsionsChat   │      │ApexsionsEconomy │ │ApexsionsBattlepass│ │   ApexsionsShop   │
│ (Chat & Mod Sec) │      │(AH, Trade, Pay) │ │ (Quests & Passes) │ │(Dynamic Eco/Tax)│
└──────────────────┘      └─────────────────┘ └───────────────────┘ └───────────────────┘
```

1. **`ApexsionsCore`**: Otoritas utama wilayah 3 Kerajaan (`Zenithar`, `Solterra`, `Sylvamoor`), progresi level pemain, BlueMap polygon rendering, sistem **Kingdom-Bounded `/rtp`**, formula leveling, dan pembatasan TPA EssentialsX.
2. **`ApexsionsChat`**: Sistem komunikasi server terpadu dengan channel (`Global`, `Kingdom`, `Staff`), pamer item (`/showitem`), surat offline (`/mail`), chat games, pengumuman otomatis, dan sistem moderasi lapis tiga (Anti-Spam, Profanity, Anti-Ad, Exploit Blocker, Staff Alerts).
3. **`ApexsionsEconomy`**: Multi-Currency (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Sistem Barter/Trade 12-Slot dengan deteksi kerajaan, status toggle `/trade toggle`, serta pajak transportasi lintas-kerajaan.
4. **`ApexsionsBattlepass`**: Season battlepass 200 level, Quests (42 Daily, 120 Weekly, 50 Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), Toko Rotasi (*Dynamic Shop*), Auto-Fill Rewards GUI, dan Editor Admin GUI 54-Slot (`/abp`).
5. **`ApexsionsShop`**: Pasar & Toko Dinamis 6 Kategori (`Blocks`, `Makanan`, `Pertanian`, `Ore`, `Mob Drops`, `Dyes`), Rasio Jual **20%**, Formula Dinamis Cuaca & Bioma Kerajaan, Pajak Wilayah 10%, UI Ramah Bedrock/Touchscreen, dan GUI Jual Cepat 45-Slot (`/sell`).

---

## 🔗 2. Integrasi Antar-Plugin (Cross-Plugin Features)

### A. Trade System & Kingdom Integration (`ApexsionsEconomy` $\leftrightarrow$ `ApexsionsCore`)
- **Penyaringan Pemain Kerajaan**: Menu `/trade` secara bawaan memfilter anggota satu kerajaan. Pemain dapat menekan tombol filter di Slot 8 untuk melihat pemain global se-server.
- **Biaya Transportasi Lintas-Kerajaan (*Transport Tariff*)**:
  - **Sesama Kerajaan**: Biaya transportasi = **Rp 0 (GRATIS)**.
  - **Lintas-Kerajaan (Beda Kingdom)**: Kedua pihak dikenakan biaya transportasi sebesar **Rp 5.000** (dapat diubah di `config.yml`) saat konfirmasi transaksi.

### B. Pembatasan TPA EssentialsX (`ApexsionsCore` $\leftrightarrow$ `EssentialsX`)
- **Pengecekan Kerajaan**: Pemain hanya dapat melakukan `/tpa` atau `/tpahere` ke sesama anggota kerajaan.
- **Pengecekan Wilayah Teritorial**: Kedua pemain (pengirim & penerima) **wajib berada di dalam koordinat poligon kerajaan mereka**. Jika salah satu berada di wilderness atau kerajaan lawan, TPA dibatalkan.

### C. Toko Pasar Dinamis (`ApexsionsShop` $\leftrightarrow$ `ApexsionsEconomy` & `ApexsionsCore`)
- **Mata Uang Rupiah**: Menggunakan saldo Rupiah real-time via `ApexsionsEconomyAPI`.
- **Pengaruh Bioma Kerajaan**:
  - *Solterra:* Makanan/Pertanian lebih murah (-20%), Mineral lebih mahal (+25%).
  - *Zenithar:* Mineral/Logam lebih murah (-20%), Makanan lebih mahal (+30%).
  - *Sylvamoor:* Pewarna (Dyes)/Tanaman rimba lebih murah (-25%), Mineral lebih mahal (+20%).
- **Pajak Kerajaan**: Pajak default 10% per transaksi yang dipotong untuk kas kerajaan wilayah.

### D. Toko Exp-Shop & Ekonomi (`ApexsionsBattlepass` $\leftrightarrow$ `ApexsionsEconomy`)
- Toko EXP Battlepass terhubung langsung ke `ApexsionsEconomyAPI` untuk mendukung pembelian menggunakan mata uang `Rupiah` dan `Diamond`.

---

## 📜 3. Matriks Perintah & Permissions

### ApexsionsCore
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/kingdom` | Membuka profil dan status kerajaan pemain | `apexsionscore.kingdom` |
| `/kingdom select` | Membuka menu pemilihan 3 kerajaan | `apexsionscore.kingdom.select` |
| `/rtp` / `/wild` | Random Teleport aman khusus di dalam wilayah kerajaan | `apexsionscore.command.rtp` |
| `/level` | Membuka GUI progress level, XP, & rewards | `apexsionscore.level` |
| `/level rewards` | Membuka menu klaim hadiah level | `apexsionscore.level.rewards` |
| `/xpguide` | Membuka panduan perolehan XP lengkap (13 sumber) | `apexsionscore.xpguide` |
| `/kadmin` | Perintah manajemen admin (XP, level, reload) | `apexsionscore.admin` |

### ApexsionsChat
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/g [pesan]` | Berbicara di obrolan Global | `apexsionschat.channel.global` |
| `/kc [pesan]` | Berbicara di obrolan Kerajaan | `apexsionschat.channel.kingdom` |
| `/sc [pesan]` | Berbicara di obrolan Staf | `apexsionschat.channel.staff` |
| `/showitem` | Memamerkan item yang dipegang ke chat | `apexsionschat.showitem` |
| `/report <pemain> <alasan>` | Mengirim laporan pelanggaran pemain ke staf | `apexsionschat.report` |
| `/reports` | Membuka antarmuka resolusi laporan staf | `apexsionschat.staff.reports` |
| `/mail [send\|read\|clear]` | Mengirim dan membaca surat offline | `apexsionschat.mail` |
| `/apexsionschat <reload\|mute\|clear>` | Kontrol moderasi & reload chat admin | `apexsionschat.admin` |

### ApexsionsEconomy
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/economy` / `/bal` | Melihat saldo Rupiah dan Diamond | `apexeconomy.use` |
| `/pay <pemain> <jumlah>` | Membuka GUI transfer atau kirim uang cepat | `apexeconomy.pay` |
| `/ah` | Membuka pasar lelang dan escrow claim | `apexeconomy.ah` |
| `/trade [pemain]` | Membuka GUI barter item & saldo 12-slot anti-scam | `apexeconomy.trade` |
| `/trade toggle` | Mengaktifkan/menonaktifkan permintaan trade | `apexeconomy.trade` |
| `/aeco` | Perintah manajemen saldo admin | `apexeconomy.admin` |

### ApexsionsBattlepass
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/bp` | Membuka menu utama BattlePass 200 Level pemain | `apexsbp.use` |
| `/bp quests` | Melihat progres misi harian (10 aktif/42 pool), mingguan, bulanan | `apexsbp.quests` |
| `/bp shop` | Membuka toko rotasi BattlePass | `apexsbp.shop` |
| `/abp` | Membuka visual GUI editor 54-slot untuk admin | `apexsbp.admin` |

### ApexsionsShop
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/shop` | Membuka Menu Utama Pasar 6 Kategori | `apexsionsshop.use` |
| `/shop <kategori>` | Membuka langsung kategori (blocks, makanan, pertanian, ores, mob_drops, dyes) | `apexsionsshop.use` |
| `/sell` / `/sellgui` | Membuka GUI Jual Cepat 45-slot drag & drop | `apexsionsshop.sell` |
| `/sellhand` | Menjual item di tangan utama | `apexsionsshop.sell` |
| `/sellall` | Menjual seluruh item valid di inventori | `apexsionsshop.sell` |
| `/shop reload` | Memuat ulang seluruh konfigurasi dan file kategori | `apexsionsshop.admin` |

---

## 🗄️ 4. Konfigurasi Database (SQLite & PostgreSQL)

Setiap plugin mendukung mode database ganda via HikariCP Connection Pool:

```yaml
database:
  type: "SQLITE" # Opsi: "SQLITE" atau "POSTGRESQL"
  file: "data.db"
  postgresql:
    host: "localhost"
    port: 5432
    database: "apexsions_server"
    username: "postgres"
    password: "your_password"
    ssl: false
    maximum-pool-size: 10
```
