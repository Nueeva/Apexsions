# Apexsions Plugin Suite — Minecraft 1.21.4 / Paper 26.2

Kumpulan plugin server Minecraft profesional berkinerja tinggi yang dirancang secara modular, terintegrasi penuh antar-plugin, dan siap digunakan untuk ekosistem server **Apexsions**.

---

## 📦 1. Daftar 6 Plugin Suite Utama

| Plugin | Versi | Status | Package Root Java | Deskripsi & Fokus Utama |
| :--- | :---: | :---: | :--- | :--- |
| **`ApexsionsCore`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.core.*` | Otoritas Wilayah Kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*), Sistem Warp Navigasi GUI & Admin Editor GUI, Perlindungan PvP Teritorial Kerajaan, Progresi Leveling (13 XP Sources), BlueMap Polygons, Leaderboard GUI (`/kingdom top`), Kingdom War Manager, PvP Combat Tag (15s), Kingdom-Bounded `/rtp`, dan Enforcer TPA EssentialsX. |
| **`ApexsionsChat`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.chat.*` | Komunikasi Kyori MiniMessage, Channel (*Global*, *Kingdom*, *Staff*), Chat Settings GUI (`/channel settings`), Pamer Item (`/showitem`), Surat Offline (`/mail`), Chat Games, dan Moderasi Lapis Tiga dengan Staff Reports GUI. |
| **`ApexsionsEconomy`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.economy.*` | Multi-Currency (`Rupiah`, `Diamond`), Transfer Cepat (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Barter/Trade 12-Slot dengan Pajak Transportasi Antar-Kerajaan. |
| **`ApexsionsBattlepass`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.battlepass.*` | 200 Level BattlePass, Season Management, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), dan Visual Admin GUI Editor 54-Slot (`/abp`). |
| **`ApexsionsShop`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.shop.*` | Pasar Dinamis 6 Kategori, Rasio Jual **20%**, Formula Dinamis Multiplier Cuaca & Bioma Kerajaan, Price Clamping (50%-200%), Siaran Tren Pasar Berkelanjutan, Pajak Wilayah 10%, UI Ramah Sentuh/Bedrock, dan GUI Jual Cepat 45-Slot (`/sell`). |
| **`ApexsionsMedia`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.media.*` | Render Banner/Logo Gambar Multi-Tile Asinkron (PNG/JPG/URL) dengan deteksi ukuran otomatis, Raytrace Line-of-Sight Hover Glowing, Actionbar Tooltips, Replikasi/Pindah Banner (`/media place`, `/media copy`), dan Interaksi Tautan Konfirmasi Web (`[Buka URL]` & `[Salin Clipboard]`). |

---

## 🌟 2. Fitur Unggulan Setiap Plugin

### 👑 ApexsionsCore
- **3 Kerajaan Berdaulat**: *Zenithar* (Pegunungan/Tambang), *Solterra* (Gurun/Pertanian), *Sylvamoor* (Hutan/Alam).
- **Sistem Warp GUI Player & Admin Editor GUI**:
  - `/warp` & `/warps`: GUI navigasi 54-slot dengan filter tab kategori (`SERVER`, `RESOURCE`, `EVENT`, `KINGDOM`, `PVP`, `GENERAL`).
  - `/warpmgr` / `/warp admin`: GUI manajemen interaktif admin untuk memperbarui koordinat posisi berdiri, mengubah ikon dari tangan, kategori, delay timer, toggle hidden, dan hapus warp.
- **Perlindungan Teritorial PvP Sesama Kerajaan**:
  - Otomatis membatalkan 100% serangan (Melee, Panah/Trident, Splash Potion, Pet) antar sesama anggota kerajaan saat berada **di dalam wilayah kerajaan sendiri**.
  - Bebas bertarung sesama anggota jika berada di luar wilayah claim (Wilderness / Warzone / Wilayah Musuh).
- **Formula Leveling & 13 Sumber XP**: Mining, Mob Kill, Woodcutting, Fishing, Farming, Crafting, Enchanting, Smelting, Player Kill, Golden Apple, Potion Use, Exploration, Structure Discovery.
- **Hall of Fame & Leaderboard GUI (`/kingdom top`)**: Antarmuka visual 54-slot klasemen peringkat kerajaan dan pemain level tertinggi.
- **Kingdom War Manager (`/ac war`)**: Mode perang resmi antar-kerajaan dengan proteksi penguncian teleportasi di zona perang aktif.
- **PvP Combat Tagging (15 Detik)**: Mencegah combat log dan membatalkan segala bentuk teleportasi (`/tpa`, `/rtp`, `/warp`, `/spawn`, `/lobby`, `/home`) saat sedang bertarung.
- **BlueMap Polygon Integration**: Visualisasi transparan wilayah kerajaan pada peta web real-time.
- **Kingdom-Bounded `/rtp`**: Teleportasi acak aman yang mewajibkan pemain berada di teritori kerajaannya sendiri.
- **Enforcer TPA EssentialsX**: Teleportasi `/tpa` wajib sesama anggota kerajaan dan kedua pemain wajib berada di dalam area kerajaan.

### 💬 ApexsionsChat
- **Channel Terisolasi & Preferensi Visual (`/channel settings`)**: Saluran Global, Kerajaan, dan Staf, dilengkapi GUI personal untuk toggle audio mention pings dan pemilihan channel.
- **Showcase Item Modern (`/showitem`)**: Menampilkan item tangan dengan hover tooltip interaktif tanpa kebocoran syntax tag.
- **Surat Offline (`/mail`)**: Mengirim dan membaca pesan untuk pemain yang sedang offline.
- **Chat Games & Siaran**: Mini game tebak kata & matematika berhadiah, serta pengumuman terjadwal.
- **Moderasi Keamanan Lapis Tiga**: Anti-Spam (Levenshtein rate limiter), Anti-Profanity (sensor kata kotor), Anti-Ad (blocker IP/Link), Exploit Blocker, Notifikasi Staf Real-time, dan Staff Reports Resolution GUI (`/reports`).

### 💰 ApexsionsEconomy
- **Dual Currency Engine**: `Rupiah` (Rp) dan `Diamond` (♦) dengan pemformatan otomatis (`K`, `Jt`, `M`, `T`).
- **Pasar Lelang (`/ah`)**: Jual-beli item bebas antar-pemain dengan sistem brankas klaim (*Escrow Claim*) untuk mencegah kehilangan item saat inventaris penuh.
- **Barter & Trade Terintegrasi Kerajaan (`/trade`)**:
  - Filter pemain sesama kerajaan secara otomatis.
  - Tombol toggle filter global.
  - Pajak transportasi lintas-kerajaan dipungut otomatis saat konfirmasi transaksi.

### 🎫 ApexsionsBattlepass
- **200 Level Hadiah**: 100 XP fixed per level dengan pewarisan klaim hadiah bertingkat (*Tier Inheritance*).
- **Pool Misi Masif**: 42 Misi Harian, 120 Misi Mingguan, dan 50 Misi Bulanan.
- **Toko Rotasi & Exp-Shop**: Penukaran BP-XP untuk item langka dengan batas pembelian per pemain.
- **Admin Visual Editor 54-Slot (`/abp`)**: Pengaturan reward, item shop, probabilitas kelangkaan, dan level langsung via GUI di dalam game.

### 🛒 ApexsionsShop
- **6 Kategori Lengkap**: `blocks.yml`, `farming.yml`, `food.yml`, `ores.yml`, `mob_drops.yml`, `dyes.yml`.
- **Harga Dinamis Cuaca & Kerajaan**: Harga berfluktuasi cerdas sesuai hujan/badai dan keunggulan komoditas kerajaan pembeli.
- **Price Clamping & Siaran Tren Pasar**: Batas pengaman harga (50% - 200%) dan siaran berkala komoditas BOOM/DIP.
- **Pajak Kerajaan 10%**: Otomatis disalurkan ke kas perbendaharaan kerajaan pemain.
- **UI Ramah Sentuh & Bedrock**: Kontrol navigasi di baris terbawah inventaris.
- **GUI Jual Cepat 45-Slot (`/sell` & `/sellgui`)**: Drag-and-drop banyak item sekaligus untuk langsung dijual.

### 🖼️ ApexsionsMedia
- **Render Banner Gambar Multi-Tile Async**: Mendukung PNG, JPG, JPEG dari file lokal atau URL web tanpa format ketat (dimensi & link opsional).
- **Penempatan & Kloning Fleksibel**:
  - `/media place <id>`: Memindahkan/memasang template banner ke dinding yang sedang dilihat via Raytracing.
  - `/media copy <idAsal> <idBaru>`: Menduplikasi konfigurasi banner ke instance baru.
  - `/media resize <id> <w> <h>`: Mengubah ukuran tile banner langsung.
- **Raytrace Line-of-Sight Hover Glowing**: Partikel bercahaya border & actionbar tooltip saat crosshair pemain mengarah ke banner.
- **Interaksi URL Fleksibel**: Pilihan mode Chat MiniMessage (`[🌐 BUKA URL]` & `[📋 SALIN LINK]`) atau GUI Konfirmasi 27-slot.
- **100% Vanilla & Bedrock Compatible**: Tanpa mod klien tambahan, didukung penuh oleh Geyser/Floodgate.

---

## 📜 3. Master Command List & Permissions Matrix

### 👑 Modul ApexsionsCore
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/lobby` | `/hub` | Teleportasi ke lobi utama server (Multiverse-ready) | `apexsionscore.command.lobby` | `true` |
| `/warp [nama]` | `/warps` | Membuka GUI navigasi warp atau teleport langsung | `apexsionscore.command.warp` | `true` |
| `/warpmgr` | `/warpadmin`, `/warp admin` | Membuka Interactive Admin Warp Management GUI | `apexsionscore.warp.admin` | `op` |
| `/warp set <nama> [kat]` | - | Membuat warp baru di lokasi berdiri | `apexsionscore.warp.admin` | `op` |
| `/warp delete <nama>` | `/warp del` | Menghapus warp dari database | `apexsionscore.warp.admin` | `op` |
| `/kingdom` | `/k`, `/region` | Membuka profil dan status kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka menu pemilihan 3 kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard`| Membuka Hall of Fame & Leaderboard GUI | `apexsionscore.command.level` | `true` |
| `/level` | `/lvl`, `/profile` | Membuka GUI progress bar level & hadiah | `apexsionscore.command.level` | `true` |
| `/xpguide` | `/exp` | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/rtp` | `/wild`, `/krtp` | Teleportasi acak aman di wilayah kerajaan | `apexsionscore.command.rtp` | `true` |
| `/ac setlobby` | `/kc setlobby` | Mengatur titik spawn lobi saat ini (Multiverse-ready) | `apexsionscore.admin` | `op` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang resmi antar kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan perang kerajaan aktif (Admin) | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status aktif perang kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/ac addxp <p> <amt>` | `/kc addxp` | Menambahkan XP pemain (Admin) | `apexsionscore.admin` | `op` |
| `/ac setlevel <p> <lvl>`| `/kc setlevel` | Mengatur level pemain langsung (Admin) | `apexsionscore.admin` | `op` |
| `/ac setkingdom <p> <k>`| `/kc setk` | Memindahkan kerajaan pemain (Admin) | `apexsionscore.admin` | `op` |
| `/ac reload` | `/kc reload` | Memuat ulang konfigurasi Core (Admin) | `apexsionscore.admin` | `op` |

### 💬 Modul ApexsionsChat
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/channel [settings]` | `/ch` | Mengganti channel obrolan atau buka preferensi GUI | `apexsionschat.channel` | `true` |
| `/g [pesan]` | `/global` | Berbicara di obrolan Global | `apexsionschat.channel.global` | `true` |
| `/kc [pesan]` | `/kchat` | Berbicara di obrolan Kerajaan | `apexsionschat.channel.kingdom` | `true` |
| `/sc [pesan]` | `/staffchat` | Berbicara di obrolan Staf | `apexsionschat.channel.staff` | `op` |
| `/showitem` | `/item`, `/i` | Memamerkan item di tangan ke obrolan | `apexsionschat.showitem` | `true` |
| `/mail send <p> <pesan>`| - | Mengirim surat offline ke pemain | `apexsionschat.mail` | `true` |
| `/mail read` | `/inbox` | Membaca kotak masuk surat offline | `apexsionschat.mail` | `true` |
| `/report <p> <alasan>`| - | Melaporkan pemain yang melanggar aturan | `apexsionschat.report` | `true` |
| `/reports` | `/reportlist` | Membuka antarmuka resolusi laporan (Staf) | `apexsionschat.staff.reports` | `op` |
| `/apexsionschat mute` | `/chat mute` | Mengunci obrolan global server (Admin) | `apexsionschat.admin` | `op` |
| `/apexsionschat clear`| `/chat clear`| Membersihkan layar obrolan pemain (Admin) | `apexsionschat.admin` | `op` |
| `/apexsionschat reload`| - | Memuat ulang konfigurasi chat (Admin) | `apexsionschat.admin` | `op` |

### 💰 Modul ApexsionsEconomy
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/economy` | `/eco`, `/bal`, `/uang` | Membuka menu utama saldo pemain | `apexsionseconomy.use` | `true` |
| `/baltop` | `/topbal` | Menampilkan papan peringkat kekayaan | `apexsionseconomy.use` | `true` |
| `/pay <p> <amt> [curr]` | `/transfer` | Mentransfer uang ke pemain lain | `apexsionseconomy.pay` | `true` |
| `/ah` | `/lelang`, `/auction` | Membuka pasar lelang & brankas escrow | `apexsionseconomy.ah` | `true` |
| `/ah sell <harga>` | `/lelang jual` | Mendaftarkan item ke pasar lelang | `apexsionseconomy.ah` | `true` |
| `/trade [pemain]` | `/barter`, `/tukar` | Membuka menu barter item & saldo | `apexsionseconomy.trade` | `true` |
| `/trade toggle` | - | Mengaktifkan/menonaktifkan request trade | `apexsionseconomy.trade` | `true` |
| `/ecoadmin give <p> <amt>`| `/aeco give` | Menambah saldo pemain (Admin) | `apexsionseconomy.admin` | `op` |
| `/ecoadmin take <p> <amt>`| `/aeco take` | Mengurangi saldo pemain (Admin) | `apexsionseconomy.admin` | `op` |
| `/ecoadmin set <p> <amt>` | `/aeco set` | Mengatur saldo pemain (Admin) | `apexsionseconomy.admin` | `op` |

### 🎫 Modul ApexsionsBattlepass
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/bp` | `/battlepass` | Membuka antarmuka 200 level BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp misi` | Membuka daftar misi harian/mingguan/bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp toko` | Membuka toko rotasi BattlePass | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin`, `/adminbp` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |
| `/abp setlevel <p> <lvl>`| - | Mengatur level BattlePass pemain (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp addxp <p> <xp>` | - | Memberikan poin BP-XP ke pemain (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp givepass <p> <tier>`| `/abp setpass` | Memberikan status pass tier ke pemain (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp reload` | - | Memuat ulang file konfigurasi BP (Admin) | `apexsionsbattlepass.reload` | `op` |

### 🛒 Modul ApexsionsShop
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/shop` | `/pasar`, `/toko`, `/store` | Membuka menu utama 6 kategori toko | `apexsionsshop.use` | `true` |
| `/shop <kategori>` | `/pasar <kat>` | Membuka kategori toko spesifik | `apexsionsshop.use` | `true` |
| `/sell` | `/sellgui`, `/jual` | Membuka GUI jual cepat 45-slot | `apexsionsshop.sell` | `true` |
| `/sellall` | `/jualsemua` | Menjual seluruh item cocok di inventaris | `apexsionsshop.sell` | `true` |
| `/sellhand` | `/jualtangan` | Menjual item yang sedang dipegang | `apexsionsshop.sell` | `true` |
| `/shop reload` | `/pasar reload` | Memuat ulang kategori & pasar (Admin) | `apexsionsshop.admin` | `op` |

### 🖼️ Modul ApexsionsMedia
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/media create <id> <src> [w] [h] [url] [mode]` | `/banner create` | Memasang banner baru di lokasi target | `apexsionsmedia.admin` | `op` |
| `/media place <id>` | `/banner place` | Memasang banner yang ada ke dinding target raytrace | `apexsionsmedia.admin` | `op` |
| `/media copy <idAsal> <idBaru>` | `/banner copy` | Menduplikasi pengaturan banner ke instance baru | `apexsionsmedia.admin` | `op` |
| `/media resize <id> <w> <h>` | `/banner resize` | Mengubah ukuran lebar & tinggi banner | `apexsionsmedia.admin` | `op` |
| `/media setlink <id> [url\|none] [mode]` | `/banner setlink` | Mengatur tautan URL interaktif banner | `apexsionsmedia.admin` | `op` |
| `/media delete <id>` | `/banner delete` | Menghapus banner dan entity terkait | `apexsionsmedia.admin` | `op` |
| `/media list` | `/banner list` | Menampilkan daftar seluruh banner aktif | `apexsionsmedia.admin` | `op` |
| `/media reload` | `/banner reload` | Memuat ulang konfigurasi & banner | `apexsionsmedia.admin` | `op` |

---

## 🛠️ 4. Kompilasi & Build Otomatis

Kompilasi seluruh suite secara bersamaan atau per-plugin secara kilat menggunakan PowerShell multi-compiler:

```powershell
# 1. Kompilasi KILAT per plugin (hanya ~15-20 detik):
powershell -ExecutionPolicy Bypass -File .\build.ps1 Core
powershell -ExecutionPolicy Bypass -File .\build.ps1 Media
powershell -ExecutionPolicy Bypass -File .\build.ps1 Chat
powershell -ExecutionPolicy Bypass -File .\build.ps1 Economy
powershell -ExecutionPolicy Bypass -File .\build.ps1 Battlepass
powershell -ExecutionPolicy Bypass -File .\build.ps1 Shop

# 2. Kompilasi SELURUH 6 plugin suite serentak:
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

File `.jar` hasil kompilasi siap pasang akan tersedia di:
- `build/libs/ApexsionsCore-1.0.0.jar`
- `build/libs/ApexsionsChat-1.0.0.jar`
- `build/libs/ApexsionsEconomy-1.0.0.jar`
- `build/libs/ApexsionsBattlepass-1.0.0.jar`
- `build/libs/ApexsionsShop-1.0.0.jar`
- `build/libs/ApexsionsMedia-1.0.0.jar`

---

## 📚 5. Indeks Dokumentasi Lengkap (`docs/`)

Untuk panduan teknis mendalam per modul, silakan baca dokumentasi di folder `docs/`:
- 👑 [**ApexsionsCore Manual**](docs/APEXSIONS_CORE.md)
- 💬 [**ApexsionsChat Manual**](docs/APEXSIONS_CHAT.md)
- 💰 [**ApexsionsEconomy Manual**](docs/APEXSIONS_ECONOMY.md)
- 🎫 [**ApexsionsBattlepass Manual**](docs/APEXSIONS_BATTLEPASS.md)
- 🛒 [**ApexsionsShop Manual**](docs/APEXSIONS_SHOP.md)
- 🖼️ [**ApexsionsMedia Manual**](docs/APEXSIONS_MEDIA.md)
- 🏛️ [**Ecosystem Architecture Guide**](docs/ECOSYSTEM_ARCHITECTURE.md)
- 🗄️ [**PostgreSQL Integration Guide**](docs/ECONOMY_INTEGRATION_POSTGRESQL.md)
