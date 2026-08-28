# Dokumentasi Master Apexsions Plugin Suite — Minecraft 1.21.4 / Paper 26.2

Dokumentasi resmi yang merangkum arsitektur menyeluruh, interaksi antar-plugin, matriks izin & perintah, konfigurasi modular, serta integrasi gameplay untuk 5 plugin utama di ekosistem **Apexsions**.

---

## 🏛️ 1. Ikhtisar Arsitektur 5 Plugin (Plugin Ecosystem Matrix)

```
                            ┌────────────────────────┐
                            │     ApexsionsCore      │
                            │  (Kingdom & Leveling)  │
                            └───────────┬────────────┘
                                        │
         ┌──────────────────────────────┼──────────────────────────────┐
         ▼                              ▼                              ▼
┌──────────────────┐          ┌───────────────────┐          ┌───────────────────┐
│  ApexsionsChat   │          │ ApexsionsEconomy  │          │ApexsionsBattlepass│
│ (Chat & Mod Sec) │          │ (AH, Trade, Pay)  │          │ (Quests & Passes) │
└──────────────────┘          └─────────┬─────────┘          └───────────────────┘
                                        │
                                        ▼
                              ┌───────────────────┐
                              │  ApexsionsShop    │
                              │ (Dynamic Markets) │
                              └───────────────────┘
```

1. **`ApexsionsCore`**: Otoritas wilayah 3 Kerajaan (`Zenithar`, `Solterra`, `Sylvamoor`), progresi level & formula XP, BlueMap polygon rendering, sistem `/rtp` terikat kerajaan, serta pencegahan TPA lintas-wilayah EssentialsX.
2. **`ApexsionsChat`**: Sistem komunikasi Adventure/MiniMessage dengan channel (`Global`, `Kingdom`, `Staff`), pamer item (`/showitem`), surat offline (`/mail`), chat games, pengumuman otomatis, dan sistem moderasi lapis tiga (*Anti-Spam, Profanity, Anti-Ad, Exploit Blocker, Staff Alerts*).
3. **`ApexsionsEconomy`**: Multi-Currency (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Sistem Barter/Trade 12-Slot terintegrasi kerajaan & pajak transportasi.
4. **`ApexsionsBattlepass`**: Season battlepass 200 level, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), Toko Rotasi (*Dynamic Shop*), dan Editor Admin GUI 54-Slot (`/abp`).
5. **`ApexsionsShop`**: Pasar & toko dinamis 6 kategori (`blocks`, `farming`, `food`, `ores`, `mob_drops`, `dyes`), rasio jual dasar **20%**, multiplier cuaca & bioma kerajaan, pajak wilayah 10%, UI ramah sentuh/Bedrock, dan GUI jual cepat 45-slot (`/sell`).

---

## 🔗 2. Integrasi & Komunikasi Antar-Plugin

### A. Toko Dinamis & Ekonomi (`ApexsionsShop` $\leftrightarrow$ `ApexsionsEconomy` & `ApexsionsCore`)
- **Multi-Currency:** Mendukung transaksi berbasis `Rupiah` (Rp) dan `Diamond` secara atomic lewat `ApexsionsEconomyAPI`.
- **Spesialisasi Pasar Kerajaan:** Mengambil status kerajaan pembeli dari `ApexsionsCoreAPI`. Anggota kerajaan mendapat diskon khusus untuk komoditas unggulan wilayahnya.
- **Pajak & Multiplier Cuaca:** Harga beli & jual berfluktuasi dinamis berdasarkan kondisi cuaca dunia (hujan/badai/cerah) dan ketersediaan pasokan.

### B. Barter / Trade Terintegrasi Kerajaan (`ApexsionsEconomy` $\leftrightarrow$ `ApexsionsCore`)
- **Penyaringan Pemain Kerajaan:** Menu `/trade` secara default menyaring hanya anggota satu kerajaan. Pemain dapat menekan tombol filter di Slot 8 untuk melihat seluruh pemain server (Global).
- **Pajak Transportasi Lintas-Kerajaan:** Jika bertransaksi dengan anggota kerajaan lain, kedua belah pihak dikenakan biaya transportasi saat konfirmasi (default `Rp 5.000`). Transaksi sesama kerajaan adalah **Rp 0 (GRATIS)**.

### C. Enforcer TPA EssentialsX (`ApexsionsCore` $\leftrightarrow$ `EssentialsX`)
- **Pengecekan Kerajaan:** Teleportasi `/tpa` dan `/tpahere` hanya diizinkan untuk pemain dalam kerajaan yang sama.
- **Pengecekan Wilayah Teritorial:** Kedua pemain (pengirim & penerima) **wajib berada di dalam batas poligon kerajaan mereka**. Jika salah satu berada di luar wilayah teritorial (misal di wilderness atau kerajaan lawan), TPA dibatalkan secara otomatis.

### D. Exp-Shop & Progresi (`ApexsionsBattlepass` $\leftrightarrow$ `ApexsionsEconomy` & `ApexsionsCore`)
- Toko EXP Battlepass dapat menggunakan mata uang `Rupiah` dan `Diamond` melalui `ApexsionsEconomyAPI`.
- Menyinkronkan progres quest dengan 13 aksi gameplay `ApexsionsCore`.

---

## 📜 3. Matriks Perintah & Hak Akses (Commands & Permissions)

### 👑 ApexsionsCore
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/kingdom` | Membuka profil dan status kerajaan pemain | `apexsionscore.kingdom` |
| `/kingdom select` | Membuka antarmuka pemilihan 3 kerajaan | `apexsionscore.kingdom.select` |
| `/level` | Membuka GUI status level, XP, dan hadiah | `apexsionscore.level` |
| `/xpguide` | Membuka panduan 13 kategori perolehan XP | `apexsionscore.xpguide` |
| `/rtp` | Teleportasi acak aman di dalam wilayah kerajaan sendiri | `apexsionscore.rtp` |
| `/kadmin <addxp\|setlevel\|reload>` | Perintah administrasi kerajaan dan progresi | `apexsionscore.admin` |

### 💬 ApexsionsChat
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/g [pesan]` | Mengirim pesan ke obrolan Global | `apexsionschat.channel.global` |
| `/kc [pesan]` | Mengirim pesan ke obrolan Kerajaan | `apexsionschat.channel.kingdom` |
| `/sc [pesan]` | Mengirim pesan ke obrolan Staf | `apexsionschat.channel.staff` |
| `/showitem` | Memamerkan item di tangan ke obrolan | `apexsionschat.showitem` |
| `/mail [send\|read\|clear]` | Mengirim dan membaca surat offline | `apexsionschat.mail` |
| `/report <pemain> <alasan>` | Melaporkan pelanggaran pemain ke staf | `apexsionschat.report` |
| `/reports` | Membuka GUI manajemen laporan untuk staf | `apexsionschat.staff.reports` |
| `/apexsionschat <reload\|mute\|clear>` | Kontrol moderasi & reload chat admin | `apexsionschat.admin` |

### 💰 ApexsionsEconomy
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/economy` / `/bal` | Melihat saldo Rupiah dan Diamond | `apexeconomy.use` |
| `/baltop` | Menampilkan papan peringkat kekayaan | `apexeconomy.use` |
| `/pay <pemain> <jumlah>` | Membuka menu transfer atau kirim uang cepat | `apexeconomy.pay` |
| `/ah` | Membuka antarmuka pasar lelang & escrow claim | `apexeconomy.ah` |
| `/ah sell <harga>` | Mendaftarkan item di tangan ke pasar lelang | `apexeconomy.ah` |
| `/trade [pemain]` | Membuka GUI barter item dan uang anti-scam | `apexeconomy.trade` |
| `/trade toggle` | Mengaktifkan/menonaktifkan permintaan trade | `apexeconomy.trade` |
| `/aeco <give\|take\|set>` | Perintah manipulasi saldo oleh admin | `apexeconomy.admin` |

### 🎫 ApexsionsBattlepass
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/bp` | Membuka menu utama BattlePass | `apexsbp.use` |
| `/bp quests` | Membuka daftar misi harian, mingguan, bulanan | `apexsbp.quests` |
| `/bp shop` | Membuka toko rotasi BattlePass | `apexsbp.shop` |
| `/abp` | Membuka visual GUI editor 54-slot untuk admin | `apexsbp.admin` |
| `/abp <setlevel\|addxp\|setpass\|reload>` | Perintah kontrol administrasi BattlePass | `apexsbp.admin` |

### 🛒 ApexsionsShop
| Perintah | Deskripsi | Permission |
| :--- | :--- | :--- |
| `/shop` | Membuka menu utama toko dinamis 6 kategori | `apexsshop.use` |
| `/shop <kategori>` | Membuka kategori toko tertentu secara langsung | `apexsshop.use` |
| `/sell` / `/sellgui` | Membuka GUI jual instan 45-slot | `apexsshop.sell` |
| `/sellall` | Menjual seluruh item yang dapat dijual di inventaris | `apexsshop.sell` |
| `/sellhand` | Menjual item yang sedang dipegang di tangan | `apexsshop.sell` |
| `/shop reload` | Memuat ulang seluruh file kategori & pasar | `apexsshop.admin` |

---

## 🗂️ 4. Struktur Direktori Modular Server

Setiap plugin mengelola file konfigurasi di dalam foldernya masing-masing:

```
plugins/
├── ApexsionsCore/
│   ├── config.yml, kingdoms.yml, xp.yml, ranks.yml, titles.yml, rewards.yml, gui.yml, chat.yml
│
├── ApexsionsChat/
│   ├── config.yml, channels.yml, moderation.yml, games.yml, announcements.yml, mail.yml
│
├── ApexsionsEconomy/
│   └── config.yml (currencies, auction house, trade tariffs, escrow settings)
│
├── ApexsionsBattlepass/
│   ├── config.yml, seasons.yml, rewards.yml, gui.yml, messages.yml
│   ├── passes/ (free.yml, premium.yml, premium-plus.yml, ultimate.yml)
│   ├── quests/ (daily.yml, weekly.yml, monthly.yml)
│   ├── shop/ (daily.yml, weekly.yml, monthly.yml)
│   └── exp-shop/ (packages.yml)
│
└── ApexsionsShop/
    ├── config.yml, gui.yml, markets.yml, messages.yml
    └── categories/ (blocks.yml, farming.yml, food.yml, ores.yml, mob_drops.yml, dyes.yml)
```
