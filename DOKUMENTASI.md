# Dokumentasi Ekosistem Apexsions Plugin Suite — Minecraft 1.21.4

Dokumentasi induk yang menjelaskan arsitektur menyeluruh, integrasi antar-plugin, matriks permission, perintah admin, dan alur gameplay server **Apexsions**.

---

## 🏛️ 1. Ikhtisar Ekosistem (Plugin Matrix)

Ekosistem Apexsions terdiri dari 4 plugin spesifik berkinerja tinggi yang saling terhubung:

```
                      ┌──────────────────────┐
                      │    ApexsionsCore     │
                      │  (Kingdom & Level)   │
                      └──────────┬───────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         ▼                       ▼                       ▼
┌──────────────────┐   ┌───────────────────┐   ┌───────────────────┐
│  ApexsionsChat   │   │ ApexsionsEconomy  │   │ApexsionsBattlepass│
│ (Chat & Mod Sec) │   │ (AH, Trade, Pay)  │   │ (Quests & Passes) │
└──────────────────┘   └───────────────────┘   └───────────────────┘
```

1. **ApexsionsCore**: Otoritas utama wilayah 3 Kerajaan (`Zenithar`, `Solterra`, `Sylvamoor`), progresi level pemain, BlueMap polygon rendering, formula leveling, dan pembatasan TPA EssentialsX.
2. **ApexsionsChat**: Sistem komunikasi server terpadu dengan channel (`Global`, `Kingdom`, `Staff`), pamer item (`/showitem`), surat offline (`/mail`), chat games, pengumuman otomatis, dan sistem moderasi lapis tiga (Anti-Spam, Profanity, Anti-Ad, Exploit Blocker, Staff Alerts).
3. **ApexsionsEconomy**: Multi-Currency (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Sistem Barter/Trade dengan deteksi kerajaan & pajak transportasi lintas-kerajaan.
4. **ApexsionsBattlepass**: Season battlepass, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `ELITE`, `ULTIMATE`), Toko Rotasi (*Dynamic Shop*), dan Editor Admin GUI 54-Slot (`/abp`).

---

## 🔗 2. Integrasi Antar-Plugin (Cross-Plugin Features)

### A. Trade System & Kingdom Integration (`ApexsionsEconomy` $\leftrightarrow$ `ApexsionsCore`)
- **Penyaringan Pemain Kerajaan**: Menu `/trade` secara bawaan memfilter hanya anggota satu kerajaan. Pemain dapat menekan tombol filter di Slot 8 untuk melihat pemain global se-server.
- **Biaya Transportasi Lintas-Kerajaan (*Transport Tariff*)**:
  - **Sesama Kerajaan**: Biaya transportasi = **Rp 0 (GRATIS)**.
  - **Lintas-Kerajaan (Beda Kingdom)**: Kedua pihak dikenakan biaya transportasi sebesar **Rp 5.000** (dapat diubah di `config.yml`) saat konfirmasi transaksi.

### B. Pembatasan TPA EssentialsX (`ApexsionsCore` $\leftrightarrow$ `EssentialsX`)
- **Pengecekan Kerajaan**: Pemain hanya dapat melakukan `/tpa` atau `/tpahere` ke sesama anggota kerajaan.
- **Pengecekan Wilayah Teritorial**: Kedua pemain (pengirim & penerima) **wajib berada di dalam koordinat poligon kerajaan mereka**. Jika salah satu berada di wilderness atau kerajaan lawan, TPA dibatalkan.

### C. Toko Exp-Shop & Ekonomi (`ApexsionsBattlepass` $\leftrightarrow$ `ApexsionsEconomy`)
- Toko EXP Battlepass terhubung langsung ke `ApexsionsEconomyAPI` untuk mendukung pembelian menggunakan mata uang `Rupiah` dan `Diamond`.

---

## 📜 3. Matriks Perintah & Permissions

### ApexsionsCore
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/kingdom` | Membuka profil dan status kerajaan pemain | `apexsionscore.kingdom` |
| `/kingdom select` | Membuka menu pemilihan 3 kerajaan | `apexsionscore.kingdom.select` |
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
| `/trade [pemain]` | Membuka GUI barter item & saldo anti-scam | `apexeconomy.trade` |
| `/aeco` | Perintah manajemen saldo admin | `apexeconomy.admin` |

### ApexsionsBattlepass
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/bp` | Membuka menu utama BattlePass pemain | `apexsbp.use` |
| `/bp quests` | Melihat progres misi harian/mingguan/bulanan | `apexsbp.quests` |
| `/bp shop` | Membuka toko rotasi BattlePass | `apexsbp.shop` |
| `/abp` | Membuka visual GUI editor 54-slot untuk admin | `apexsbp.admin` |

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
