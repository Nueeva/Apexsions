# Dokumentasi Master Apexsions Plugin Suite — Minecraft 1.21.4 / Paper 26.2

Dokumentasi resmi yang merangkum arsitektur menyeluruh, interaksi antar-plugin, matriks izin & perintah, konfigurasi modular, serta integrasi gameplay untuk 6 plugin utama di ekosistem **Apexsions**.

---

## 🏛️ 1. Ikhtisar Arsitektur 6 Plugin (Plugin Ecosystem Matrix)

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
                    ┌───────────────────┴───────────────────┐
                    ▼                                       ▼
          ┌───────────────────┐                   ┌───────────────────┐
          │  ApexsionsShop    │                   │  ApexsionsMedia   │
          │ (Dynamic Markets) │                   │(Interactive Visual│
          └───────────────────┘                   └───────────────────┘
```

1. **`ApexsionsCore`** (`com.apexsions.core.*`): Otoritas wilayah 3 Kerajaan (`Zenithar`, `Solterra`, `Sylvamoor`), progresi level & formula XP, BlueMap polygon rendering, sistem `/rtp` terikat kerajaan, Kingdom War Manager, PvP Combat Tag (15s), dan pencegahan TPA lintas-wilayah EssentialsX.
2. **`ApexsionsChat`** (`com.apexsions.chat.*`): Sistem komunikasi Adventure/MiniMessage dengan channel (`Global`, `Kingdom`, `Staff`), preferensi obrolan GUI (`/channel settings`), pamer item (`/showitem`), surat offline (`/mail`), chat games, pengumuman otomatis, dan sistem moderasi lapis tiga dengan Staff Reports GUI.
3. **`ApexsionsEconomy`** (`com.apexsions.economy.*`): Multi-Currency (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Sistem Barter/Trade 12-Slot terintegrasi kerajaan & pajak transportasi.
4. **`ApexsionsBattlepass`** (`com.apexsions.battlepass.*`): Season battlepass 200 level, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), Toko Rotasi (*Dynamic Shop*), dan Editor Admin GUI 54-Slot (`/abp`).
5. **`ApexsionsShop`** (`com.apexsions.shop.*`): Pasar & toko dinamis 6 kategori (`blocks`, `farming`, `food`, `ores`, `mob_drops`, `dyes`), rasio jual dasar **20%**, formula multiplier cuaca & bioma kerajaan, price clamping (50%-200%), siaran tren pasar berkala, pajak wilayah 10%, UI ramah sentuh/Bedrock, dan GUI jual cepat 45-slot (`/sell`).
6. **`ApexsionsMedia`** (`com.apexsions.media.*`): Sistem render banner/logo gambar multi-tile asinkron (PNG/JPG/URL), raytrace line-of-sight hover glowing & actionbar tooltip, serta aksi interaksi tautan URL web/salin clipboard terkonfirmasi (100% vanilla & Bedrock compatible).

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
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/admingui` | `/apexadmin`, `/aadmin`, `/aa` | Master Admin Control Hub & Deep Player Inspector | `apexsions.admin.gui` | `op` |
| `/lobby` | `/hub` | Teleportasi ke lobi utama server | `apexsionscore.command.lobby` | `true` |
| `/warp` | `/warps` | Membuka GUI navigasi warp 54-slot | `apexsionscore.command.warp` | `true` |
| `/warpmgr` | `/warp admin` | Membuka Interactive Admin Warp Management GUI | `apexsionscore.warp.admin` | `op` |
| `/kingdom` | `/k`, `/region` | Membuka profil dan status kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka menu pemilihan 3 kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard`| Membuka Hall of Fame & Leaderboard GUI | `apexsionscore.command.level` | `true` |
| `/level` | `/lvl`, `/profile` | Membuka GUI progress bar level & hadiah | `apexsionscore.command.level` | `true` |
| `/xpguide` | `/exp` | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/rtp` | `/wild`, `/krtp` | Teleportasi acak aman di wilayah kerajaan | `apexsionscore.command.rtp` | `true` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang resmi antar-kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan perang kerajaan aktif (Admin) | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status aktif perang kerajaan (Admin) | `apexsionscore.admin` | `op` |

### 💬 ApexsionsChat
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/channel [settings]` | `/ch` | Mengganti channel atau buka pengaturan GUI | `apexsionschat.channel` | `true` |
| `/channel profile <p>`| - | Membuka antarmuka interaksi profil sosial pemain | `apexsionschat.channel` | `true` |
| `/g [pesan]` | `/global` | Berbicara di obrolan Global | `apexsionschat.channel.global` | `true` |
| `/kc [pesan]` | `/kchat` | Berbicara di obrolan Kerajaan | `apexsionschat.channel.kingdom` | `true` |
| `/sc [pesan]` | `/staffchat` | Berbicara di obrolan Staf | `apexsionschat.channel.staff` | `op` |
| `/showitem` | `/item`, `/i` | Memamerkan item di tangan ke obrolan | `apexsionschat.showitem` | `true` |
| `/mail send <p> <msg>`| - | Mengirim surat offline ke pemain | `apexsionschat.mail` | `true` |
| `/mail read` | `/inbox` | Membaca kotak masuk surat offline | `apexsionschat.mail` | `true` |
| `/report <p> <alasan>`| - | Melaporkan pemain yang melanggar aturan | `apexsionschat.report` | `true` |
| `/reports` | `/reportlist` | Membuka antarmuka resolusi laporan & meja staf 54-slot | `apexsionschat.staff.reports` | `op` |

### 💰 ApexsionsEconomy
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/economy` | `/eco`, `/bal` | Membuka menu utama saldo pemain | `apexsionseconomy.use` | `true` |
| `/baltop` | `/topbal` | Menampilkan peringkat kekayaan | `apexsionseconomy.use` | `true` |
| `/pay <p> <amt> [curr]` | `/transfer` | Mentransfer uang ke pemain lain | `apexsionseconomy.pay` | `true` |
| `/ah` | `/lelang` | Membuka pasar lelang & brankas escrow | `apexsionseconomy.ah` | `true` |
| `/trade [pemain]` | `/barter` | Membuka menu barter item & saldo | `apexsionseconomy.trade` | `true` |

### 🎫 ApexsionsBattlepass
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/bp` | `/battlepass` | Membuka antarmuka 200 level BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp misi` | Membuka daftar misi harian/mingguan/bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp toko` | Membuka toko rotasi BattlePass | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |

### 🛒 ApexsionsShop
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/shop` | `/pasar` | Membuka menu utama 6 kategori toko | `apexsionsshop.use` | `true` |
| `/sell` | `/sellgui` | Membuka GUI jual cepat 45-slot | `apexsionsshop.sell` | `true` |
| `/sellall` | `/jualsemua` | Menjual seluruh item cocok di inventaris | `apexsionsshop.sell` | `true` |
| `/sellhand` | `/jualtangan` | Menjual item yang sedang dipegang | `apexsionsshop.sell` | `true` |

### 🖼️ ApexsionsMedia
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/media create <id> <src> <w> <h> [url] [mode]` | `/banner create` | Memasang banner baru di lokasi target | `apexsionsmedia.admin` | `op` |
| `/media delete <id>` | `/banner delete` | Menghapus banner dan entity terkait | `apexsionsmedia.admin` | `op` |
| `/media list` | `/banner list` | Menampilkan daftar seluruh banner aktif | `apexsionsmedia.admin` | `op` |
| `/media setlink <id> <url> [mode]` | `/banner setlink` | Mengubah tautan URL interaktif banner | `apexsionsmedia.admin` | `op` |
| `/media reload` | `/banner reload` | Memuat ulang konfigurasi & banner | `apexsionsmedia.admin` | `op` |
