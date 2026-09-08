# ApexsionsCore — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsCore`** (Otoritas Wilayah Kerajaan, Sistem Warp GUI & Admin GUI, Master Admin Hub, Title Vault, Particle Cosmetics, Perlindungan PvP Teritorial, Progresi Karakter, XP Engine, Navigasi BlueMap, Kingdom War, Combat Tag, dan Enforcer TPA EssentialsX).

---

## 🏛️ 1. Ikhtisar Modul & Arsitektur

`ApexsionsCore` adalah modul pondasi sentral yang mengatur identitas pemain, pembagian 3 kerajaan besar, sistem progresi berbasis level dan XP, sistem navigasi Warp modern (GUI Player & Admin), Master Admin Hub (`/admingui`), Title Vault (`/titles`), Particle Cosmetics (`/cosmetics`), rendering wilayah pada web-map (BlueMap), manajemen deklarasi perang kerajaan (*Kingdom War*), PvP combat tagging anti-combat log, serta pengamanan teritorial dari eksploitasi teleportasi dan perkelahian internal kerajaan.

```
                                ┌────────────────────────┐
                                │     ApexsionsCore      │
                                │(Kingdom, Level, Ranks) │
                                └───────────┬────────────┘
                                            │
              ┌─────────────────────────────┼─────────────────────────────┐
              ▼                             ▼                             ▼
    ┌───────────────────┐         ┌───────────────────┐         ┌───────────────────┐
    │  3 Kingdom Realms │         │ 13 XP Engine Core │         │  Warp & War Locks │
    │Zenithar / Solterra│         │Leveling, Titles,  │         │Warp GUI & AdminGUI│
    │    Sylvamoor      │         │   Rewards GUI     │         │Territory PvP Lock │
    └───────────────────┘         └───────────────────┘         └───────────────────┘
```

---

## 👑 2. Sistem 3 Kerajaan (Kingdom Realms)

Setiap pemain di server diwajibkan memilih dan berikrar pada salah satu dari 3 Kerajaan:

| Kerajaan | Nuansa Wilayah / Bioma | Keunggulan Komoditas | Warna Wilayah / Tag |
| :--- | :--- | :--- | :--- |
| **Zenithar** | Dataran Tinggi, Pegunungan, & Tambang Kristal | Hasil Tambang & Logam Mulia (*Ores & Ingot*) | `<gold>#FFAA00` |
| **Solterra** | Gurun Pasir Emas, Savanna, & Kota Dagang | Pertanian Panas, Pewarna, & Makanan | `<yellow>#FFFF55` |
| **Sylvamoor** | Hutan Belantara Lebat, Rawa, & Lembah Mistis | Kayu Langka, Mob Drops, & Ramuan | `<green>#55FF55` |

### Fitur Teritorial Kerajaan:
- **BlueMap Polygon Rendering**: Menampilkan batas wilayah poligon kerajaan secara transparan dan estetik di peta web BlueMap.
- **Auto-Respawn Ibukota Kerajaan (`PlayerRespawnEvent`)**: Pemain yang telah bersumpah setia pada kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*) akan otomatis di-respawn di titik pusat ibukota kerajaan masing-masing saat gugur di medan perang/alam liar (menggantikan fallback default ke lobby).
  - Mengambil koordinat `"position"` marker dari konfigurasi BlueMap secara otomatis saat server berjalan.
  - Prioritas tempat tidur (*Bed/Anchor priority*): Secara default pemain dengan kasur aktif tetap respawn di basenya (`spawn.override-bed-spawn: false`). Jika kasur hancur/terhalang atau disetel `override-bed-spawn: true`, pemain selalu dipulangkan ke ibukota.
- **Spawn & Navigasi Kerajaan (`/kingdom`, `/k`)**: Perintah cerdas dua arah tanpa argumen:
  - Pemain yang belum memilih kerajaan otomatis dibukakan antarmuka pemilihan 3 kerajaan (`RegionSelectionGUI`).
  - Pemain yang sudah berikrar kerajaan langsung diteleportasikan ke koordinat ibukota kerajaannya via `RegionTeleportService` disertai audio feedback dan partikel.
- **In-Game Capital Spawn Manager**: Perintah admin `/ac setspawn <kingdom>` dan `/kingdom setspawn <kingdom>` untuk memindahkan titik spawn ibukota secara langsung in-game dengan persistensi SQL dan update `kingdoms.yml`.
- **Integrasi Citizens NPC (Native Command Binding)**: Menggunakan perintah bawaan Citizens (`/npc sel <id>` lalu `/npc cmd add -p k`). Seluruh listener dan custom trait hardcoded ditiadakan untuk menjaga stabilitas klik kanan pemain tanpa konflik plugin.
- **Hall of Fame & Leaderboard GUI (`/kingdom top`)**: Antarmuka visual 54-slot yang menampilkan statistik kerajaan terkuat dan top level pemain.

---

## 🚀 3. Sistem Warp Terpadu (Player GUI & Admin GUI)

Sistem navigasi teleportasi publik server dengan antarmuka grafis modern dan manajemen langsung dari dalam game:

### A. Player Warp Navigation GUI (`/warp`, `/warps`)
- Antarmuka 54-slot dengan tab kategori interaktif di baris atas (`ALL`, `SERVER`, `RESOURCE`, `EVENT`, `KINGDOM`, `PVP`, `GENERAL`).
- Countdown teleportasi visual di Actionbar (default 3 detik) dengan pembatalan instan jika pemain bergerak atau menerima damage.
- Terkunci otomatis jika pemain terkena status **Combat Tag** atau kerajaan sedang dalam **War Aktif**.

### B. Interactive Admin Warp GUI (`/warpmgr`, `/warp admin`)
- **Daftar Seluruh Warp**: Melihat semua warp publik maupun hidden.
- **Pembuatan Warp Seketika**: Membuat warp baru di lokasi koordinat pemain berdiri dengan 1 klik tombol `+ Buat Warp Baru`.
- **Menu Editor Interaktif (`WarpEditorGUI`)**:
  - `⚑ Perbarui Lokasi`: Mengubah titik koordinat warp ke posisi pemain saat ini.
  - `✦ Ubah Ikon`: Menerapkan item di tangan utama pemain menjadi ikon warp.
  - `🏷 Ubah Kategori`: Mengganti kategori warp (`SERVER`, `RESOURCE`, `EVENT`, dll).
  - `⏱ Ubah Delay`: Mengatur delay teleportasi (0s Instan, 3s, 5s, 10s).
  - `👁 Toggle Hidden`: Mengubah status publik / admin-only hidden.
  - `✖ Hapus Warp`: Menghapus warp secara permanen dari database SQLite/PostgreSQL.

---

## 🛡️ 4. Perlindungan PvP Sesama Kerajaan di Dalam Wilayah Claim

- **Pencegahan Friendly-Fire di Dalam Wilayah Kerajaan Sendiri**:
  - Pemain yang berada di kerajaan yang sama **dilarang saling menyerang** saat berada di dalam wilayah claim kerajaan (`KingdomProtectionListener`).
  - Membatalkan 100% semua jenis serangan: **Melee (Pedang/Kapak), Panah/Trident, Splash Potion Beracun/Harmful, dan Serangan Pet**.
  - Mengirimkan notifikasi peringatan halus di Actionbar penyerang: `<red><bold>⚔ PERLINDUNGAN KERAJAAN: </bold><gray>Dilarang menyerang sesama anggota di dalam wilayah kerajaan!</gray></red>`.
- **PvP Bebas di Luar Wilayah**:
  - Jika pertarungan terjadi di luar wilayah claim (Wilderness / Warzone / Wilayah Musuh), pertarungan sesama anggota kerajaan diperbolehkan secara bebas.

---

## ⚔️ 5. Sistem Kingdom War & PvP Combat Tag

### A. Manajemen Perang Kerajaan (`WarManager`)
- Admin dapat mendeklarasikan perang antar-kerajaan dengan durasi khusus: `/ac war start <Kingdom1> <Kingdom2> [durasi_menit]`.
- Selama masa perang aktif:
  - Seluruh siaran publik menampilkan banner permusuhan visual MiniMessage.
  - **Seluruh fitur teleportasi (`/rtp`, `/tpa`, `/warp`, `/spawn`, `/lobby`) dinonaktifkan** di wilayah kerajaan yang sedang berperang untuk mencegah pelarian instan.

### B. PvP Combat Tagging (`CombatTagService`)
- Ketika pemain menyerang atau menerima damage dari pemain lain:
  - Pemain otomatis masuk ke mode **Combat Tag** selama **15 detik**.
  - Teleportasi dibatalkan secara instan jika pemain mencoba melakukan `/tpa`, `/rtp`, `/warp`, `/spawn`, `/lobby`, `/home`.
  - Jika pemain sengaja keluar (*combat log*), karakter langsung dieliminasi secara otomatis dan disiarkan ke seluruh server.

---

## 📈 6. Formula Leveling & 13 Sumber XP

$$\text{XP Dibutuhkan}(L) = \lfloor 100 \times L^{1.5} + (L \times 50) \rfloor$$

### 13 Kategori Perolehan XP:
1. **Mining**, 2. **Mob Kill**, 3. **Woodcutting**, 4. **Fishing**, 5. **Farming**, 6. **Crafting**, 7. **Enchanting**, 8. **Smelting**, 9. **Player Kill**, 10. **Golden Apple**, 11. **Potion Use**, 12. **Exploration**, 13. **Structure Discovery**.

---

## 📜 7. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/lobby` | `/hub` | Teleportasi ke lobi pusat server | `apexsionscore.command.lobby` | `true` |
| `/admingui` | `/apexadmin`, `/aadmin`, `/aa` | **Master Admin Hub Terpusat** (Dashboard 54-slot seluruh suite) | `apexsions.admin.gui` | `op` |
| `/warp [nama]` | `/warps` | Membuka GUI navigasi warp atau teleport ke warp tertentu | `apexsionscore.command.warp` | `true` |
| `/warpmgr` | `/warpadmin`, `/warp admin` | Membuka Interactive Admin Warp Management GUI | `apexsionscore.warp.admin` | `op` |
| `/warp set <nama> [kat]` | - | Membuat warp baru di lokasi koordinat berdiri | `apexsionscore.warp.admin` | `op` |
| `/warp delete <nama>` | `/warp del` | Menghapus warp dari database | `apexsionscore.warp.admin` | `op` |
| `/kingdom` | `/k`, `/region`, `/kingdoms` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka GUI visual pemilihan 3 kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard` | Membuka GUI Hall of Fame & Leaderboard | `apexsionscore.command.level` | `true` |
| `/kingdom setspawn <k>`| `/k setspawn` | Menetapkan titik spawn ibukota kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/kingdom setking <k> <p>`| - | Menobatkan Raja Tertinggi kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/kingdom unsetking <k>`| `/kingdom removeking` | Mencabut gelar Raja dari kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/level` | `/lvl`, `/profile`, `/rewards`, `/exp` | Membuka GUI progress bar level (1-100) dan reward | `apexsionscore.command.level` | `true` |
| `/xpguide` | - | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/titles` | `/tags`, `/title`, `/tag` | Membuka Title Vault GUI untuk memasang gelar & badge | `apexsionscore.command.titles` | `true` |
| `/cosmetics` | `/aura`, `/auras`, `/trail`, `/trails` | Membuka Particle Cosmetics GUI (Auras, Trails, Kill FX) | `apexsionscore.command.cosmetics` | `true` |
| `/rtp` | `/wild`, `/wilderness`, `/krtp` | Teleportasi acak di teritori kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/ac reload` | `/apexsionscore reload`, `/kc reload` | Memuat ulang seluruh konfigurasi Core, Ranks & Rewards | `apexsionscore.admin` | `op` |
| `/ac setspawn <kingdom>`| `/ac setcapital` | Menetapkan koordinat spawn ibukota kerajaan di posisi berdiri | `apexsionscore.admin` | `op` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang kerajaan resmi | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan perang kerajaan aktif | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status aktif perang kerajaan | `apexsionscore.admin` | `op` |
| `/ac setlevel <p> <lvl>`| `/kc setlevel` | Mengubah level pemain secara langsung | `apexsionscore.admin` | `op` |
| `/ac addxp <p> <amt>` | `/kc addxp` | Menambahkan poin XP ke pemain | `apexsionscore.admin` | `op` |
| `/ac setkingdom <p> <k>`| `/kc setk` | Memindahkan kerajaan pemain secara paksa | `apexsionscore.admin` | `op` |
| `/ac setlobby` | `/kc setlobby` | Mengatur titik spawn lobi saat ini | `apexsionscore.admin` | `op` |
| `/ac info <p>` | `/kc info` | Memeriksa rincian level, XP, dan kerajaan pemain | `apexsionscore.admin` | `op` |
| `/sions [status]` | - | Memeriksa status temporal engine & blok termodifikasi | `apexsionscore.admin.sions` | `op` |
| `/sions set [minY] [maxY]` | `/sions snapshot` | Mengambil snapshot baseline permanen seluruh blok wilayah Sions | `apexsionscore.admin.sions` | `op` |
| `/sions restore` | - | Memulihkan paksa seluruh blok ke baseline seketika | `apexsionscore.admin.sions` | `op` |
| `/sions setkey` | - | Mengubah item di tangan menjadi Kunci Kuno Resmi Sions | `apexsionscore.admin.sions` | `op` |
| `/sions givekey [p] [qty]` | - | Memberikan Kunci Kuno Sions resmi kepada pemain | `apexsionscore.admin.sions` | `op` |
| `/sions bypass` | - | Toggle mode bypass admin (modifikasi tanpa direkam) | `apexsionscore.admin.sions` | `op` |
| `/sions tp` | - | Teleportasi langsung ke titik pusat ibukota Sions | `apexsionscore.admin.sions` | `op` |

---

## 🎛️ 8. Master Admin Hub Terpusat (`/admingui`)

- **Dashboard 54-Slot Sentral (`MasterAdminGUI`)**: Menyatukan seluruh modul administrasi plugin dalam satu tampilan intuitif.
- **Header Status Server Real-time**: Memantau RAM server yang sedang terpakai, TPS, jumlah pemain online, pool koneksi database, serta status perang kerajaan.
- **6 Kartu Modul Suite**:
  1. **👑 Core Management**: Sub-menu Warp Manager, Kingdom War Control, dan Lobby Spawn Setup.
  2. **💬 Chat & Reports**: Kotak masuk resolusi tiket laporan pemain staff (`ReportsAdminGUI`).
  3. **💰 Economy & Auction**: Kontrol saldo dan pasar lelang pemain.
  4. **🎫 BattlePass Editor**: Panel visual editor reward, misi, dan seasonal settings (`/abp editor`).
  5. **🛒 Dynamic Shop**: Pasar dinamis, harga komoditas cuaca, dan monitoring inflasi.
  6. **🖼️ Media & Banners**: Daftar banner aktif, teleportasi ke banner, dan replikasi template (`MediaAdminGUI`).
- **Granular Permission & Visual Lock Indicator**: Kartu modul yang tidak diizinkan untuk staf junior akan tetap tampil tetapi terkunci dengan gembok merah (`🔒 TERKUNCI`).
- **Universal Breadcrumb Navigation**: Tombol `⬅ KEMBALI KE ADMIN HUB` (Slot 45) tertanam di seluruh sub-menu admin untuk navigasi bolak-balik tanpa harus mengetik perintah ulang.
- **Tombol Reload Suite Serentak**: Memuat ulang konfigurasi seluruh plugin suite dengan 1 klik tombol Redstone Block.

---

## 🌌 9. Kerajaan Sions (The Secret Civilization) & Hourly Temporal Engine

Wilayah misterius kerajaan keempat yang tersembunyi dari peradaban umum:

1. **Perlindungan Teritorial & Fallback Polygon (`RegionManager`)**:
   - Teritori Sions didefinisikan menggunakan 11 titik poligon presisi (`X: -6119, Y: 92, Z: -3457`, `minY: -64, maxY: 1000`).
   - Melalui `ensureSionsRegion()`, poligon ini didaftarkan otomatis ke memory server, sehingga disembunyikannya marker Sions dari web BlueMap (`world.conf`) tidak membatalkan proteksi dan deteksi in-game.
   - Deteksi perbatasan mengirimkan alert visual: `⚑ Territory: Kerajaan Sions (The Secret Civilization)`.
2. **Hourly Snapshot Temporal Engine (`SionsTemporalService`)**:
   - Berjalan otomatis secara berkala setiap **60 menit (per jam)** untuk mereset seluruh wilayah kembali ke kondisi semula.
   - Pola **Snapshot In-Memory Asli (`putIfAbsent`)**: Menghafal keadaan blok awal (*pristine state*) sebelum dimodifikasi. Modifikasi berulang tidak menimpa blok awal.
   - **Baseline Snapshot Terkompresi (`/sions set`)**: Admin dapat mengunci kondisi awal seluruh struktur Sions ke file biner terkompresi GZIP (`sions_baseline.dat`). Saat server restart atau reset hourly terjadi, blok akan dipulihkan presisi sesuai snapshot baseline ini.
   - Memulihkan blok yang dihancurkan (*break*), diletakkan (*place*), diledakkan TNT/Creeper (*explosion*), terbakar (*fire*), dan mencair/memudar (*fade*).
3. **Anti-Duplikasi Sumber Daya (`prevent-item-drops: true`)**:
   - Blok yang dihancurkan oleh pemain biasa di dalam wilayah Sions tidak menjatuhkan item drop, mencegah eksploitasi grinding ore/mineral sebelum reset temporal berlangsung.
4. **Hierarki 3-Tier Peti Kuno & Segel Wadah (`SionsContainerLockListener`)**:
   - Seluruh container di wilayah Sions terkunci oleh segel gaib kuno berdasarkan 3 tingkatan (tier):
     - **Peti Biasa (`common`)**: Normal Chest & Barrel. Membutuhkan **Kunci Kuno Sions** (`SionsCommonKey`). Peluang drop 15% dari Cecunguk Minions.
     - **Peti Khazanah Ksatria (`elite`)**: Trapped Chest, Dispenser, Dropper, Hopper. Membutuhkan **Kunci Khazanah Ksatria Sions** (`SionsEliteKey`). Peluang drop 35%–50% dari Ksatria & Sentinel Elit.
     - **Peti Tahta Kaisar (`boss`)**: Shulker Box, Ender Chest, Vault. Membutuhkan **Kunci Void Kaisar Valerius** (`SionsBossKey`). Dijamin 100% drop dari Raid Boss Emperor Valerius.
   - Kunci dikonsumsi 1x pakai saat pertama kali membuka peti dalam 1 siklus temporal. Selama siklus 60 menit berjalan, peti yang telah dibuka tetap dapat diakses tanpa mengonsumsi kunci tambahan, lalu terkunci kembali saat reset temporal berlangsung.
5. **Integrasi MythicMobs & Spawner Ekosistem Sions**:
   - **Raid Boss Terkuat (`EmperorValerius` Lv.100)**: Spawner permanen di koordinat `-6089, 93, -3454` dengan cooldown respawn **4 jam (14.400s)**.
   - **Ksatria Elit Sions (`SionsVoidKnight` Lv.50 / Sentinel Lv.55)**: Spawner permanen di barak pertahanan `-6126, 92, -3459` dengan cooldown **30 menit (1.800s)**.
   - **Cecunguk Minions (`SionsLegionnaire`, `SionsVoidCrawler`)**: Otomatis spawn berkala di dalam wilayah teritorial Sions (`sions_ruins_spawns.yml`).
   - **Wilderness Leveled Mobs (Lv.5–15)**: Monster alam liar berlevel ringan (`WildForestStalker`, `WildDuneReaper`, `WildCanyonSavage`) spawn alami di luar zona aman peradaban.
6. **Peringatan Proksimitas Temporal Terarah**:
   - Hitung mundur menjelang anomali temporal (10 menit, 5 menit, 1 menit, dan 10 detik) disiarkan secara khusus kepada pemain yang **berada di dalam teritori Sions** melalui Chat, Actionbar, dan efek audio atmosferik (`BLOCK_BELL_RESONATE`, `BLOCK_CONDUIT_DEACTIVATE`, `BLOCK_END_PORTAL_SPAWN`).
7. **Alat Kelola Admin (`/sions`)**:
   - `/sions status`: Memeriksa status temporal, hitung mundur reset, dan jumlah modifikasi.
   - `/sions set [minY] [maxY]`: Memindai seluruh poligon wilayah Sions dan mengunci kondisi awal struktur ke disk.
   - `/sions restore`: Memicu pemulihan instan darurat tanpa menunggu countdown 60 menit.
   - `/sions setkey [common|elite|boss]`: Mengonversi item di tangan menjadi template kunci tier yang dipilih.
   - `/sions givekey <player> [common|elite|boss] [qty]`: Memberikan kunci Sions sesuai tier kepada pemain target.
   - `/sions bypass`: Mode membangun khusus staf/arsitek agar modifikasi blok permanen dan tidak di-rollback.

---

## 📡 10. Integrasi WebBridge & Pengumuman Global In-Game

Modul `ApexsionsCore` bertindak sebagai agen penerima antrean WebBridge untuk komunikasi asinkron antara portal web dan server game:

1. **Pengumuman Global Admin (`broadcast` / `bc`)**:
   - Perintah siaran dari Web Dashboard (`POST /admin/apexsions/broadcast`) dikirimkan melalui tabel antrean `deliveries` dengan identitas `player_uuid = 'GLOBAL'` dan `player_username = 'ALL_PLAYERS'`.
   - `WebBridgeService` mendeteksi perintah tersebut, mem-parse pesan menggunakan Kyori Adventure `MiniMessage`, memancarkannya ke seluruh pemain di server (`Bukkit.broadcast`), dan memicu efek suara lonceng (`BLOCK_NOTE_BLOCK_BELL`).
2. **Sinkronisasi Karakter Pemain Otomatis (`sync-player`)**:
   - Statistik in-game (Level, XP, Saldo Rupiah/Diamond, Kerajaan, Rank, dan Gelar) dikirimkan secara berkala saat event login, logout, level up, dan mutasi saldo.
   - Portal web Azuriom menyimpan profil karakter pemain in-game bahkan sebelum pemain mendaftarkan atau menautkan akun web (`user_id = null`), memastikan seluruh pemain aktif memiliki laman profil publik yang valid di `/player/{uuid}`.
