# Apexsions Ecosystem — Monorepo

Repository monorepo resmi untuk ekosistem **Apexsions**:
- 🎮 **`Minecraft/`**: Plugin suite Minecraft server profesional (Paper 1.21.4 / Java 21)
- 🌐 **`Website/`**: Portal web, storefront, dan integrasi ekosistem Apexsions

---

## 📂 Struktur Monorepo

```text
Apexsions/
├── Minecraft/             # Seluruh kode sumber, konfigurasi, dan build plugin Minecraft
│   ├── plugins/          # 7 Plugin Suite (ApexsionsCore, Chat, Economy, Battlepass, Shop, Media, CustomEnchants)
│   ├── docs/             # Dokumentasi teknis & arsitektur plugin
│   ├── build.ps1         # Smart Turbo Multi-Compiler PowerShell
│   ├── build.gradle      # Gradle root configuration
│   └── DOKUMENTASI.md    # Manual teknis plugin
├── Website/               # Aplikasi web portal & store
├── GEMINI.md              # AI Agent Development Guidelines
├── AGENTS.md             # Universal Coding Agent Guidelines
└── README.md             # Dokumentasi utama repositori
```

---

## 📦 1. Daftar 7 Plugin Suite Utama (di `Minecraft/plugins/`)

| Plugin | Versi | Status | Package Root Java | Deskripsi & Fokus Utama |
| :--- | :---: | :---: | :--- | :--- |
| **`ApexsionsCore`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.core.*` | Otoritas Wilayah Kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*), Sistem Warp Navigasi GUI & Admin Editor GUI, Perlindungan PvP Teritorial Kerajaan, Progresi Leveling (13 XP Sources), BlueMap Polygons, Leaderboard GUI (`/kingdom top`), Kingdom War Manager, PvP Combat Tag (15s), Kingdom-Bounded `/rtp`, Enforcer TPA EssentialsX, dan Sistem Kit Kerajaan Terintegrasi (`/kits`) dengan Armor Set Bonus Berbasis Stat. |
| **`ApexsionsChat`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.chat.*` | Komunikasi Kyori MiniMessage, Channel (*Global*, *Kingdom*, *Staff*), Chat Settings GUI (`/channel settings`), Pamer Item (`/showitem`), Surat Offline (`/mail`), Chat Games, dan Moderasi Lapis Tiga dengan Staff Reports GUI. |
| **`ApexsionsEconomy`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.economy.*` | Multi-Currency (`Rupiah`, `Diamond`), Transfer Cepat (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Barter/Trade 12-Slot dengan Pajak Transportasi Antar-Kerajaan. |
| **`ApexsionsBattlepass`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.battlepass.*` | 200 Level BattlePass, Season Management, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), dan Visual Admin GUI Editor 54-Slot (`/abp`). |
| **`ApexsionsShop`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.shop.*` | Pasar Dinamis 6 Kategori, Rasio Jual **20%**, Formula Dinamis Multiplier Cuaca & Bioma Kerajaan, Price Clamping (50%-200%), Siaran Tren Pasar Berkelanjutan, Pajak Wilayah 10%, UI Ramah Sentuh/Bedrock, dan GUI Jual Cepat 45-Slot (`/sell`). |
| **`ApexsionsMedia`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.media.*` | Render Banner/Logo Gambar Multi-Tile Asinkron (PNG/JPG/URL) dengan deteksi ukuran otomatis, Raytrace Line-of-Sight Hover Glowing, Actionbar Tooltips, Replikasi/Pindah Banner (`/media place`, `/media copy`), dan Interaksi Tautan Konfirmasi Web (`[Buka URL]` & `[Salin Clipboard]`). |
| **`ApexsionsCustomEnchants`** | `1.0.0` | ![Active](https://img.shields.io/badge/Status-Active-brightgreen) | `com.apexsions.customenchants.*` | Sistem Custom Enchantment Mewah: Enchanter Gacha Dual-Currency (`/ce`), Toko Buku Spesifik 3x Harga, Mystery & Magic Dust Booster, White & Black Scrolls, Admin Hub (`/ace`), Replika Katalog `/ae admin` (`/ace enchants`), Interactive Item & Armor Set Builder (`/ace create`), dan Tinkerer Coming Soon. |

---

## 🌟 2. Fitur Unggulan Setiap Plugin

### 👑 ApexsionsCore
- **3 Kerajaan Berdaulat**: *Zenithar* (Pegunungan/Tambang), *Solterra* (Gurun/Pertanian), *Sylvamoor* (Hutan/Alam).
- **Ultimate Admin Control Panel & Deep Player Inspector (54-Slot GUI)**:
  - Akses penuh administrasi pemain dari GUI: Ubah saldo Rupiah/Diamond, ubah level (1-100) & XP, ganti kerajaan seketika, dan penobatan **👑 Raja Kerajaan (Monarch)** dengan siaran global.
  - Quick Tooling: Teleportasi, tarik pemain, inspeksi live inventory & EnderChest, Heal & Feed instan, GameMode switcher (Survival/Creative/Adventure/Spectator), dan Kick sanksi.
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
- **Sistem Kit Kerajaan Terintegrasi (`/kits`)**:
  - GUI Pemain 27/54-slot untuk preview isi kit (Klik Kiri) dan klaim langsung (Klik Kanan).
  - Validasi hierarki rank LuckPerms (mengacu pada bobot resmi `ranks.yml`) dan pelacakan cooldown mandiri per kit.
  - GUI Pembuat Kit Admin (`/kits create <id>`): Validasi ketat slot armor (maksimal 1 full set armor kepala hingga kaki; menolak duplikat helm atau armor di slot ekstra).
  - **Armor Set Bonus Berbasis Stat**: Mendukung bonus persentase stat tempur (Bukan efek potion biasa!):
    - `DAMAGE_REDUCTION`: Pengurangan damage masuk (%)
    - `ATTACK_DAMAGE_BOOST`: Peningkatan damage keluar (%)
    - `DODGE_CHANCE`: Peluang menghindari serangan total dengan partikel & suara swoosh (%)
    - `CRITICAL_DAMAGE_BOOST`: Peningkatan kerusakan pukulan kritis (%)
    - `EXTRA_MAX_HEALTH`: Peningkatan atribut hati maksimal
    - `MOVEMENT_SPEED_BOOST`: Peningkatan kecepatan lari/gerak atribut

### 💬 ApexsionsChat
- **Interactive Chat ID-Card & Social Profile GUI (27-Slot)**:
  - Klik nama pemain di chat untuk membuka profil sosial: Rincian gelar, rank, kerajaan, level, saldo Rupiah, ping, serta tombol aksi cepat (`/msg`, `/trade`, `/mail send`, `/report`).
- **Staff Reports Investigation & Rapid Action Desk (54-Slot GUI)**:
  - `/reports`: Rincian tiket laporan, teleport ke TKP / posisi terlapor, perubahan status (Reviewing, Resolved, Dismissed), dan eksekusi sanksi 1-klik (Mute 10m, Warn, Kick, Ban 1h).
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
- **Badge Visual Tren Pasar & Dashboard (`/shop trends`)**: Indikator visual langsung pada lore item (`[PASOKAN MELIMPAH]`, `[LANGKA / PERMINTAAN TINGGI]`, `[EFEK CUACA]`, `[DISKON KERAJAAN]`) dan menu tren ekonomi khusus.
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

### ⚡ ApexsionsCustomEnchants
- **Dual-Currency Enchanter Gacha GUI (`/ce`)**:
  - Gacha buku sihir acak per tier (`SIMPLE`, `UNIQUE`, `ELITE`, `ULTIMATE`, `LEGENDARY`, `FABLED`).
  - Mendukung mata uang **Rupiah** dan **Diamond** via `ApexsionsEconomyAPI`.
  - Tier `HEROIC` dikunci sebagai **Coming Soon** dengan visual interaktif eksklusif.
- **Toko Buku Sihir Spesifik 54-Slot (`/ce shop`)**:
  - Pembelian langsung buku sihir yang diinginkan dengan harga 3x lipat dari gacha acak tier tersebut.
  - Peluang sukses terkalibrasi tetap **50%** di semua tier untuk menjaga stabilitas gameplay.
- **28 Custom Enchantments Berkekuatan Tinggi**:
  - Efek tempur & utilitas: Bleed, Lifesteal, Vampire, Cleave, Rage, Blind, Paralyze, Disarm, Cactus, Enlightened, Inquisitive, Obsidianshield, Overload, Phoenix, AutoSmelt, Telepathy, MultiArrow, Sniper, Unbreakable, dll.
- **Magic Dust & Protection Scrolls**:
  - **Mystery Dust**: Klik kanan untuk mengungkap Magic Dust atau Failed Secret Dust.
  - **Magic Dust**: Menambah success rate buku sihir (+1% hingga +15%) saat di-drag-and-drop.
  - **White Scroll**: Melindungi item dari kehancuran saat tempaan sihir gagal.
  - **Black Scroll**: Mengekstrak custom enchant dari senjata/armor secara aman menjadi buku sihir.
- **Central Admin Hub GUI (`/ace`)**:
  - Mengetik `/ace` langsung membuka dashboard administrasi utama 45-slot.
  - `/ace enchants`: Replika persis katalog `/ae admin` dari AdvancedEnchantments (45 sihir per halaman, pencarian nama/id, klik kiri untuk mendapatkan buku, klik kanan untuk menempa langsung ke item tangan).
  - `/ace create`: Interactive Builder untuk membuat item kustom (base material, custom enchants, vanilla enchants) sekaligus **Custom Armor Set Bonus** non-kit.
  - `/ace pricing`: Pengaturan harga gacha per tier, toggle Rupiah/Diamond, multiplier toko spesifik, dan peluang sukses buku.
  - `/ce tinkerer`: Antarmuka Tinkerer Kerajaan (Coming Soon).

---

## 📜 3. Master Command List & Permissions Matrix

### 👑 Modul ApexsionsCore
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/admingui` | `/apexadmin`, `/aadmin`, `/aa` | **Master Admin Hub Terpusat** (Dashboard 54-slot seluruh suite) | `apexsions.admin.gui` | `op` |
| `/admingui player <p>`| - | Membuka Deep Player Inspector (Level, Saldo, Kerajaan, Inv) | `apexsions.admin.gui` | `op` |
| `/admingui warp` | - | Pintasan langsung ke Admin Warp Management GUI | `apexsions.admin.gui` | `op` |
| `/admingui economy` | - | Pintasan langsung ke Admin Economy Controls | `apexsions.admin.gui` | `op` |
| `/admingui pass` | - | Pintasan langsung ke BattlePass Visual Editor | `apexsions.admin.gui` | `op` |
| `/admingui shop` | - | Pintasan langsung ke Dashboard Tren Pasar Toko | `apexsions.admin.gui` | `op` |
| `/admingui media` | - | Pintasan langsung ke Media Banner Admin GUI | `apexsions.admin.gui` | `op` |
| `/admingui reload` | - | Me-reload konfigurasi terpusat seluruh suite | `apexsions.admin.gui` | `op` |
| `/lobby` | `/hub` | Teleportasi ke lobi utama server (Multiverse-ready) | `apexsionscore.command.lobby` | `true` |
| `/warp [nama]` | `/warps` | Membuka GUI navigasi warp 54-slot atau teleport langsung | `apexsionscore.command.warp` | `true` |
| `/warpmgr` | `/warpadmin`, `/warp admin` | Membuka Interactive Admin Warp Management GUI | `apexsionscore.warp.admin` | `op` |
| `/warp set <nama> [kat]` | - | Membuat warp baru di lokasi berdiri | `apexsionscore.warp.admin` | `op` |
| `/warp delete <nama>` | `/warp del` | Menghapus warp dari database server | `apexsionscore.warp.admin` | `op` |
| `/kingdom` | `/k`, `/region` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka menu pemilihan 3 kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard`| Membuka Hall of Fame & Leaderboard GUI klasemen kerajaan | `apexsionscore.command.level` | `true` |
| `/level` | `/lvl`, `/profile`, `/exp`, `/rewards` | Membuka GUI progress bar level & hadiah | `apexsionscore.command.level` | `true` |
| `/xpguide` | - | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/titles` | `/tags`, `/title`, `/tag` | Membuka Title Vault GUI untuk memasang gelar & badge | `apexsionscore.command.titles` | `true` |
| `/cosmetics` | `/auras`, `/trails`, `/aura`, `/trail` | Membuka Particle Cosmetics GUI (Head Auras, Trails, Kill FX) | `apexsionscore.command.cosmetics` | `true` |
| `/rtp` | `/wild`, `/wilderness`, `/krtp` | Teleportasi acak aman di wilayah kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/ac reload` | `/apexsionscore reload`, `/kc reload` | Memuat ulang seluruh file konfigurasi Core, Ranks & Rewards | `apexsionscore.admin` | `op` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang resmi antar kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan perang kerajaan aktif (Admin) | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status aktif perang kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/ac setlevel <p> <lvl>`| `/kc setlevel` | Mengatur level pemain langsung (Admin) | `apexsionscore.admin` | `op` |
| `/ac addxp <p> <amt>` | `/kc addxp` | Menambahkan XP pemain (Admin) | `apexsionscore.admin` | `op` |
| `/ac setkingdom <p> <k>`| `/kc setk` | Memindahkan kerajaan pemain seketika (Admin) | `apexsionscore.admin` | `op` |
| `/ac setlobby` | `/kc setlobby` | Mengatur titik spawn lobi saat ini (Multiverse-ready) | `apexsionscore.admin` | `op` |
| `/ac info <p>` | `/kc info` | Memeriksa data progresi & kerajaan pemain (Admin) | `apexsionscore.admin` | `op` |
| `/kits` | `/kit` | Membuka antarmuka kit kerajaan (Preview & Klaim) | `apexsionscore.kits` | `true` |
| `/kits preview <id>` | - | Melihat isi item & armor set bonus suatu kit | `apexsionscore.kits` | `true` |
| `/kits create <id>` | - | Membuka GUI Pembuat Kit Admin (Validasi 1 full set armor) | `apexsionscore.admin` | `op` |
| `/kits delete <id>` | - | Menghapus kit kerajaan dari sistem | `apexsionscore.admin` | `op` |
| `/kits list` | - | Menampilkan daftar seluruh kit aktif | `apexsionscore.admin` | `op` |
| `/kits resetcd <p> <id>` | - | Mereset cooldown kit pemain tertentu | `apexsionscore.admin` | `op` |

### 💬 Modul ApexsionsChat
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/channel [settings]` | `/ch` | Mengganti channel obrolan atau buka preferensi GUI | `apexsionschat.channel` | `true` |
| `/channel profile <p>`| - | Membuka antarmuka interaksi profil sosial pemain (ID-Card) | `apexsionschat.channel` | `true` |
| `/g [pesan]` | `/global` | Berbicara di obrolan Global | `apexsionschat.channel.global` | `true` |
| `/kc [pesan]` | `/kchat`, `/kingdomchat` | Berbicara di obrolan Kerajaan | `apexsionschat.channel.kingdom` | `true` |
| `/sc [pesan]` | `/staffchat` | Berbicara di obrolan Staf | `apexsionschat.channel.staff` | `op` |
| `/showitem` | `/item`, `/i`, `/hand` | Memamerkan item di tangan ke obrolan | `apexsionschat.showitem` | `true` |
| `/mail send <p> <pesan>`| - | Mengirim surat offline ke pemain | `apexsionschat.mail` | `true` |
| `/mail read` | `/inbox` | Membaca kotak masuk surat offline | `apexsionschat.mail` | `true` |
| `/mail clear` | - | Menghapus seluruh pesan di kotak masuk | `apexsionschat.mail` | `true` |
| `/report <p> <alasan>`| - | Melaporkan pemain yang melanggar aturan | `apexsionschat.report` | `true` |
| `/reports` | `/reportlist` | Membuka antarmuka resolusi laporan meja staf 54-slot | `apexsionschat.staff.reports` | `op` |
| `/apexsionschat reload`| `/chatadmin reload`, `/acchat reload` | Memuat ulang konfigurasi chat, games & pengumuman (Admin) | `apexsionschat.admin` | `op` |
| `/apexsionschat mute` | `/acchat lock` | Mengunci obrolan global server (Admin) | `apexsionschat.admin` | `op` |
| `/apexsionschat clear`| - | Membersihkan layar obrolan pemain (Admin) | `apexsionschat.admin` | `op` |
| `/apexsionschat game start`| - | Memulai paksa chat game mini-event (Admin) | `apexsionschat.admin` | `op` |
| `/apexsionschat announce`| - | Menyiarkan pengumuman berkala berikutnya instan (Admin) | `apexsionschat.admin` | `op` |

### 💰 Modul ApexsionsEconomy
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/economy` | `/eco`, `/bal`, `/uang` | Membuka menu utama saldo pemain | `apexsionseconomy.use` | `true` |
| `/baltop` | `/topbal` | Menampilkan papan peringkat kekayaan server | `apexsionseconomy.use` | `true` |
| `/pay <p> <amt> [curr]` | `/transfer`, `/kirimuang` | Mentransfer uang ke pemain lain | `apexsionseconomy.pay` | `true` |
| `/ah` | `/lelang`, `/auction` | Membuka pasar lelang & brankas escrow | `apexsionseconomy.ah` | `true` |
| `/trade [pemain]` | `/barter`, `/tukar` | Membuka menu barter item & saldo | `apexsionseconomy.trade` | `true` |
| `/trade toggle` | - | Mengaktifkan/menonaktifkan request trade | `apexsionseconomy.trade` | `true` |
| `/ecoadmin reload` | `/apexeconomy reload`, `/adminpay reload` | Memuat ulang konfigurasi ekonomi & mata uang (Admin) | `apexsionseconomy.admin` | `op` |
| `/ecoadmin give <p> <amt> [curr]`| - | Menambah saldo Rupiah/Diamond pemain (Admin) | `apexsionseconomy.admin` | `op` |
| `/ecoadmin take <p> <amt> [curr]`| - | Mengurangi saldo Rupiah/Diamond pemain (Admin) | `apexsionseconomy.admin` | `op` |
| `/ecoadmin set <p> <amt> [curr]` | - | Mengatur saldo Rupiah/Diamond pemain (Admin) | `apexsionseconomy.admin` | `op` |

### 🎫 Modul ApexsionsBattlepass
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/bp` | `/battlepass` | Membuka antarmuka 200 level BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp misi` | Membuka daftar misi harian/mingguan/bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp toko` | Membuka toko rotasi BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp pass` | - | Membuka menu pembelian/peningkatan tier pass | `apexsionsbattlepass.use` | `true` |
| `/bp season` | - | Memeriksa status & sisa waktu season aktif | `apexsionsbattlepass.use` | `true` |
| `/bp claim [level]` | - | Mengklaim reward level BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp level` | - | Menampilkan level dan progress XP BP saat ini | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |
| `/abp reload` | - | Memuat ulang seluruh file konfigurasi BP (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp givepass <p> <tier>`| - | Memberikan tier pass ke pemain (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp setlevel <p> <lvl>`| - | Mengatur level BattlePass pemain (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp addxp <p> <amt>` | - | Memberikan poin BP-XP ke pemain (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp reset <p>` | - | Mereset total progresi BattlePass pemain (Admin) | `apexsionsbattlepass.admin` | `op` |
| `/abp editor` | - | Membuka visual editor hadiah & toko (Admin) | `apexsionsbattlepass.admin` | `op` |

### 🛒 Modul ApexsionsShop
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/shop` | `/pasar`, `/toko`, `/store` | Membuka menu utama 6 kategori toko | `apexsionsshop.use` | `true` |
| `/shop trends` | - | Membuka dashboard tren pasar & indikator fluktuasi | `apexsionsshop.use` | `true` |
| `/shop <kategori>` | `/pasar <kat>` | Membuka kategori toko spesifik | `apexsionsshop.use` | `true` |
| `/shop reload` | `/pasar reload` | Memuat ulang kategori & pasar dinamis (Admin) | `apexsionsshop.admin` | `op` |
| `/sell` | `/sellgui`, `/jual` | Membuka GUI jual cepat 45-slot drag-and-drop | `apexsionsshop.sell` | `true` |
| `/sellall` | `/jualsemua` | Menjual seluruh item cocok di inventaris | `apexsionsshop.sell` | `true` |
| `/sellhand` | `/jualtangan` | Menjual item yang sedang dipegang | `apexsionsshop.sell` | `true` |

### 🖼️ Modul ApexsionsMedia
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/media create <id> <src> [w] [h] [url] [mode]` | `/banner create` | Memasang banner baru di lokasi target | `apexsionsmedia.admin` | `op` |
| `/media place <id>` | `/banner place`, `/media paste` | Memasang banner ke dinding target via Raytracing | `apexsionsmedia.admin` | `op` |
| `/media copy <idAsal> <idBaru>` | `/banner copy`, `/media clone` | Menduplikasi pengaturan banner ke instance baru | `apexsionsmedia.admin` | `op` |
| `/media move <id>` | `/banner move`, `/media moveto` | Memindahkan lokasi banner yang ada | `apexsionsmedia.admin` | `op` |
| `/media resize <id> <w> <h>` | `/banner resize` | Mengubah ukuran lebar & tinggi banner | `apexsionsmedia.admin` | `op` |
| `/media setlink <id> [url\|none] [mode]` | `/banner setlink` | Mengatur tautan URL interaktif banner | `apexsionsmedia.admin` | `op` |
| `/media delete <id>` | `/banner delete`, `/media remove` | Menghapus banner dan entity terkait | `apexsionsmedia.admin` | `op` |
| `/media list` | `/banner list` | Menampilkan daftar seluruh banner aktif | `apexsionsmedia.admin` | `op` |
| `/media reload` | `/banner reload` | Memuat ulang konfigurasi & render banner | `apexsionsmedia.admin` | `op` |
| `/media gui` | `/media admin`, `/banner gui` | Membuka Interactive Media Admin Management GUI | `apexsionsmedia.admin` | `op` |

### ⚡ Modul ApexsionsCustomEnchants
| Perintah | Alias | Deskripsi | Hak Akses (Permission) | Default |
| :--- | :--- | :--- | :--- | :--- |
| `/ce` | `/customenchants`, `/enchanter` | Membuka Enchanter Gacha Utama (Rupiah/Diamond) | `apexsions.ce.user` | `true` |
| `/ce shop` | `/ce books` | Membuka Toko Buku Sihir Spesifik (3x harga, 50% sukses) | `apexsions.ce.user` | `true` |
| `/ce tinkerer` | `/ce tinker` | Membuka antarmuka Tinkerer Kerajaan (Coming Soon) | `apexsions.ce.user` | `true` |
| `/ce info <sihir>` | `/ce detail` | Menampilkan deskripsi, tier, target, & level maks sihir | `apexsions.ce.user` | `true` |
| `/ace` | `/ace admin`, `/acehub` | Membuka Central Admin GUI Hub (Dashboard Terpusat) | `apexsions.admin` | `op` |
| `/ace enchants [p/filter]` | `/ace catalog`, `/ace ae` | Membuka Katalog Replika `/ae admin` (45 enchant/page) | `apexsions.admin` | `op` |
| `/ace create` | `/ace creator` | Interactive Item, Custom Enchant, & Armor Set Bonus Builder | `apexsions.admin` | `op` |
| `/ace pricing` | `/ace prices` | Atur harga gacha tier, mata uang, multiplier toko, & odds | `apexsions.admin` | `op` |
| `/ace givebook <p> <enchant> <lvl>` | - | Memberikan buku custom enchant ke pemain | `apexsions.admin` | `op` |
| `/ace givedust <p> <type> [rate]` | - | Memberikan Mystery Dust atau Magic Dust ke pemain | `apexsions.admin` | `op` |
| `/ace givescroll <p> <type>` | - | Memberikan White Scroll atau Black Scroll ke pemain | `apexsions.admin` | `op` |
| `/ace reload` | - | Memuat ulang seluruh konfigurasi custom enchants | `apexsions.admin` | `op` |

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
powershell -ExecutionPolicy Bypass -File .\build.ps1 CustomEnchants

# 2. Kompilasi SELURUH 7 plugin suite serentak:
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
