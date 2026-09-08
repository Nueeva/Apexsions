# Dokumentasi Lengkap ApexsionsCore

Panduan teknis resmi modul **`ApexsionsCore`** untuk arsitektur kerajaan, sistem progresi & XP, navigasi warp, master admin hub, perlindungan PvP teritorial, dan integrasi gameplay.

---

## 📂 Struktur Konfigurasi YAML Modular

```
plugins/ApexsionsCore/
├── config.yml            <-- Pengaturan database (SQLite/PostgreSQL), cache, dan opsi umum
├── kingdoms.yml          <-- Definisi 3 kerajaan (Zenithar, Solterra, Sylvamoor), spawn, bioma, dan warna
├── xp.yml                <-- Formula perolehan XP untuk 13 kategori gameplay
├── ranks.yml             <-- Hierarki pangkat LuckPerms & bobot weight (Ancestor s/d Wanderer)
├── titles.yml            <-- Daftar gelar prestise dan badge kerajaan per level
├── rewards.yml           <-- Konfigurasi hadiah level (Item, Command, Permission)
├── motd.yml              <-- Kustomisasi MOTD server list ping (MiniMessage gradient, random lines, player slot)
├── gui.yml               <-- Tata letak visual GUI (Profile, Top, Rewards, Warp, Admin Hub)
└── messages.yml          <-- Kumpulan pesan feedback visual MiniMessage
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/lobby` | `/hub` | Teleportasi ke lobi pusat server | `apexsionscore.command.lobby` | `true` |
| `/kingdom` | `/region`, `/k`, `/kingdoms` | Teleportasi ke ibukota kerajaan (jika sudah berikrar) atau membuka menu pemilihan (jika belum berikrar) | `apexsionscore.command.region` | `true` |
| `/kingdom info` | `/k profile`, `/k stats` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.level` | `true` |
| `/kingdom choose` | `/k select` | Membuka antarmuka pemilihan 3 kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard` | Membuka Hall of Fame & Leaderboard GUI | `apexsionscore.command.level` | `true` |
| `/level` | `/lvl`, `/profile`, `/rewards`, `/exp` | Membuka GUI progress bar level (1-100) & klaim hadiah | `apexsionscore.command.level` | `true` |
| `/xpguide` | - | Panduan mendalam 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/rtp` | `/wild`, `/wilderness`, `/krtp` | Teleportasi acak aman di dalam wilayah kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/warp [nama]` | `/warps` | Membuka GUI navigasi warp 54-slot atau teleport langsung | `apexsionscore.command.warp` | `true` |
| `/warpmgr` | `/warpadmin`, `/warp admin` | Membuka Interactive Admin Warp Management GUI | `apexsionscore.warp.admin` | `op` |
| `/warp set <nama> [kat]` | - | Membuat warp baru di lokasi koordinat berdiri | `apexsionscore.warp.admin` | `op` |
| `/warp delete <nama>` | `/warp del` | Menghapus warp secara permanen dari basis data | `apexsionscore.warp.admin` | `op` |
| `/admingui` | `/apexadmin`, `/aadmin`, `/aa` | **Master Admin Hub Terpusat** (Dashboard 54-slot seluruh suite) | `apexsions.admin.gui` | `op` |
| `/titles` | `/tags`, `/title`, `/tag` | Membuka Title Vault GUI untuk memasang gelar & badge | `apexsionscore.command.titles` | `true` |
| `/cosmetics` | `/aura`, `/auras`, `/trail`, `/trails` | Membuka Particle Cosmetics GUI (Head Auras, Trails, Kill FX)| `apexsionscore.command.cosmetics` | `true` |
| `/ac reload` | `/apexsionscore reload`, `/kc reload` | Memuat ulang seluruh konfigurasi Core, Ranks & Rewards | `apexsionscore.admin` | `op` |
| `/ac war start <K1> <K2> [m]`| - | Memulai deklarasi perang resmi antar-kerajaan | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan perang kerajaan aktif | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status dan sisa waktu perang kerajaan | `apexsionscore.admin` | `op` |
| `/ac setlevel <p> <lvl>`| `/kc setlevel` | Mengatur level pemain secara langsung | `apexsionscore.admin` | `op` |
| `/ac addxp <p> <amt>` | `/kc addxp` | Menambahkan poin XP progresi pemain | `apexsionscore.admin` | `op` |
| `/ac setkingdom <p> <k>`| `/kc setk` | Memindahkan kerajaan pemain seketika | `apexsionscore.admin` | `op` |
| `/ac setlobby` | `/kc setlobby` | Menetapkan titik spawn lobi di lokasi berdiri | `apexsionscore.admin` | `op` |
| `/ac info <p>` | `/kc info` | Memeriksa data lengkap level, XP, dan kerajaan pemain | `apexsionscore.admin` | `op` |
| `/ac rewards` | `/kingdom admin rewards` | Membuka Interactive Level Reward Editor (Drag & Drop Items) | `apexsionscore.admin` | `op` |
| `/enchant <ench> <lvl>` | `/customenchant`, `/apexenchant` | Memberikan enchantment custom hingga 4x vanilla limit | `apexsionscore.command.enchant` | `op` |
| `/enchant <p> <ench> <lvl>`| - | Memberikan enchantment custom pada item di tangan pemain target | `apexsionscore.command.enchant` | `op` |
| `/enchant remove <ench>`| - | Menghapus enchantment dari item di tangan (atau level 0) | `apexsionscore.command.enchant` | `op` |
| `/sions [status]` | - | Memeriksa status temporal engine Sions & blok termodifikasi | `apexsionscore.admin.sions` | `op` |
| `/sions restore` | - | Memulihkan paksa seluruh blok Sions seketika | `apexsionscore.admin.sions` | `op` |
| `/sions bypass` | - | Toggle mode bypass arsitek (modifikasi tanpa rollback) | `apexsionscore.admin.sions` | `op` |
| `/sions tp` | - | Teleportasi langsung ke titik pusat ibukota Sions | `apexsionscore.admin.sions` | `op` |

---

## 🎁 Sistem Hadiah Level & Progresi EXP

### 1. Tata Letak GUI Hadiah Level (`LevelRewardsGUI` - 11 Halaman)
- **Halaman 1**: 9 hadiah level reguler (Level 2–10) di baris ke-4 (Slot 27–35). Baris ke-3 tanpa milestone.
- **Halaman 2 s/d 10**:
  - **Baris ke-3 Tengah (Slot 22)**: Milestone Reward Spesial (Level 11, 21, 31, 41, 51, 61, 71, 81, 91) berupa `ENDER_CHEST`.
  - **Baris ke-4 (Slot 27–35)**: 9 hadiah level reguler berikutnya berupa `CHEST` (Hal 2: Lv 12–20; ... Hal 10: Lv 92–99).
- **Halaman 11 (Puncak)**: Hadiah **Level 100** berupa `NETHER_STAR` berdiri di formasi Altar Kaisar Tertinggi (Beacon, Gold Block, Crying Obsidian, Purple Star Trim) di baris ke-3 tengah (Slot 22).
- **Material Status Visual**:
  - **TERKUNCI**: `CHEST` / `ENDER_CHEST` / `NETHER_STAR` dengan **Efek Glowing** (Paper 1.21 `setEnchantmentGlintOverride`).
  - **BISA DIKLAIM**: `CHEST` / `ENDER_CHEST` / `NETHER_STAR` dengan **Efek Glowing** dan prompt hijau.
  - **SUDAH DIKLAIM**: `MINECART` redup.
  - **Border & Latar**: `BLACK_STAINED_GLASS_PANE`.

### 2. Admin Level Reward Editor (Drag & Drop & Stackable Checking)
- Akses via `/admingui` $\rightarrow$ tombol ApexsionsCore $\rightarrow$ **Kelola Hadiah Level**, atau langsung via `/ac rewards`.
- Admin dapat langsung menyeret (*drag and drop*) atau *shift-click* item apa pun dari inventory ke dalam 28 slot tengah editor.
- **Pengecekan Stackable Ala BattlePass**:
  - Item non-stackable (senjata, armor, alat, totem, elytra) otomatis dikunci pada jumlah **1x** dan tidak bisa ditambah lagi.
  - Item stackable (ingot, diamond, makanan) dapat ditambah dengan klik kiri hingga batas maksimum stack (`getMaxStackSize()`).
- Item tersimpan lengkap beserta lore, enchantment, dan NBT ke `progression/rewards.yml`.
- Hadiah item diberikan langsung ke inventory pemain tanpa console command.

### 3. Formula Kenaikan EXP (Kuadratik Terkalibrasi)
- Menggunakan formula kuadratik terkalibrasi di `config.yml`:
  $$\text{EXP}(L) = (510 \times L^2) - (10 \times L)$$
  - Di mana $L$ adalah Level pemain saat ini yang ingin ditingkatkan ke level berikutnya.
  - Lv 1 $\rightarrow$ 500 XP, Lv 2 $\rightarrow$ 2.020 XP, Lv 3 $\rightarrow$ 4.560 XP, Lv 4 $\rightarrow$ 8.120 XP, Lv 10 $\rightarrow$ 50.900 XP, dst.
  - Parameter $a = 510$ dan $b = -10$ dapat dikustomisasi melalui `config.yml` (`level.formula.a` dan `level.formula.b`).

### 4. Kalibrasi Perolehan EXP: Arcane Enchanting, Anvil, & Exploration
- **Meja Sihir (Enchanting Table)**:
  - Base tier: Tier I (15 XP), Tier II (35 XP), Tier III (75 XP).
  - Skala enchant vanilla: Lv I (+15 XP), Lv II (+30 XP), Lv III (+50 XP), Lv IV (+80 XP), Lv V (+120 XP).
  - Skala custom enchant: Lv 6–10 (+40 XP/lvl), Lv 11–15 (+60 XP/lvl), Lv 16–20 (+100 XP/lvl, max 1.500 XP).
- **Anvil Smithing**:
  - Ganti nama: 5 XP.
  - Perbaikan material: 25 XP.
  - Penggabungan alat/buku: Base 25 XP + upgrade vanilla (+30 XP/lvl) + upgrade custom enchant (+60 XP/lvl).
- **Exploration & Movement**:
  - Jalan / Lari: Tiap 8 blok (1 XP).
  - Lompat Parkour: Cooldown 2 detik (1 XP).
  - Terbang Elytra: Tiap 32 blok (2 XP).
  - Berenang: Tiap 16 blok (2 XP).
  - Tunggangan (Kuda/Unta/Babi/Perahu/Strider): Tiap 16 blok (1 XP).


## 🛡️ Mekanisme Keamanan & Integrasi Gameplay

### 1. Perlindungan PvP Sesama Kerajaan di Wilayah Claim (`KingdomProtectionListener`)
- Membatalkan 100% semua serangan (Melee, Proyektil/Panah/Trident, Splash Potion Berbahaya, Pet) antar sesama anggota kerajaan saat berada **di dalam wilayah kerajaan sendiri**.
- Bebas bertarung sesama anggota jika berada di luar wilayah claim (Wilderness / Warzone / Wilayah Musuh).

### 2. PvP Combat Tagging (15 Detik) (`CombatTagService`)
- Memasukkan pemain ke mode combat selama 15 detik saat menyerang atau menerima damage PvP.
- Membatalkan otomatis segala bentuk teleportasi (`/rtp`, `/warp`, `/spawn`, `/lobby`, `/tpa`, `/home`).
- Jika pemain sengaja keluar (*combat log*), karakter langsung dieliminasi secara otomatis.

### 3. Enforcer TPA EssentialsX (`TpaRestrictionListener`)
- Teleportasi `/tpa` dan `/tpahere` hanya diizinkan untuk sesama anggota kerajaan.
- Kedua pemain (pengirim & penerima) wajib berada di dalam batas poligon teritori kerajaan mereka.

### 4. Navigasi BlueMap & Multiverse
- Wilayah teritorial 3 kerajaan dirender secara real-time pada peta web BlueMap.
- Lobi dan dunia kerajaan mendukung penuh sistem multi-world Multiverse.

---

## 🧩 Akses Public API (`ApexsionsCoreAPI`)

Interaksi antar-plugin wajib dilakukan melalui provider:

```java
ApexsionsCoreAPI api = ApexsionsCoreProvider.get();
if (api != null) {
    int level = api.getLevel(playerUuid);
    String kingdom = api.getPlayerRegionKey(playerUuid);
    boolean inTerritory = api.isInKingdomTerritory(player, region);
}
```

---

## ✨ Fitur Custom Enchantment (`/enchant`)

Sistem perintah enchantment khusus yang mengabaikan batasan level vanilla secara terkontrol dan aman:
- **Formula Multiplier**: $\text{Batas Maksimal} = \text{Vanilla Max Level} \times \text{Multiplier (default: 4)}$.
  - *Sharpness* (vanilla 5) $\rightarrow$ Level 20.
  - *Mending* (vanilla 1) $\rightarrow$ Level 4.
  - *Unbreaking* (vanilla 3) $\rightarrow$ Level 12.
  - *Efficiency* (vanilla 5) $\rightarrow$ Level 20.
- **Per-Enchantment Overrides** (`config.yml`):
  - `protection: 12` (dapat disesuaikan secara modular).
- **Penghapusan Bersih**: Mengatur level ke `0` atau menggunakan `/enchant remove <enchantment>` akan menghapus enchantment secara instan dari item.
- **Bypass Permission**: Pemegang `apexsionscore.enchant.bypass` dapat memberikan level hingga hardcap server (`255`).
- **Admin Hub Shortcut**: Terintegrasi pada Slot 25 di `CoreAdminSubGUI` (`/admingui`).

---

## 🔨 Sistem Anvil Enhancement (Remove "Too Expensive!")

Sistem otomatis pada Anvil untuk kenyamanan perbaikan dan penggabungan item:
- **Remove "Too Expensive!"**: Mengabaikan batas level 40 bawaan Minecraft sehingga item tidak pernah terkunci dengan pesan "Too Expensive!".
- **Unlimited Repair Cost (`cost-cap: 0`)**: Berapapun tingginya biaya level perbaikan (misal level 50, 75, 120), proses perbaikan tetap dapat diselesaikan selama pemain memiliki EXP yang mencukupi. Jika diinginkan, admin juga dapat mengeset batas atas (misal cap di level 39) melalui `config.yml`.
- **Bypass Enchant Restriction**: Memastikan custom enchantment (Sharpness 20, Protection 12, Mending 4, dll) tidak ter-reset atau diturunkan levelnya saat digabungkan di anvil.

---

## 🌌 Kerajaan Sions (The Secret Civilization) & Hourly Temporal Engine
- **Secret Territory Polygon**: 11 titik poligon presisi terdaftar via fallback `RegionManager.ensureSionsRegion()`, tetap aktif in-game meski disembunyikan dari BlueMap `world.conf`.
- **Hourly Temporal Engine (`SionsTemporalService`)**:
  - Reset otomatis setiap **60 menit (1 jam)** mengembalikan semua blok rusak, diletakkan, dan diledakkan ke kondisi awal (*pristine state*).
  - Menggunakan struktur in-memory snapshot (`putIfAbsent`) tanpa query database berat.
- **Pencegahan Eksploitasi**: Opsi `prevent-item-drops: true` menonaktifkan drop item dari blok yang ditambang di Sions untuk mencegah duplikasi sebelum reset berkala.
- **Perintah Admin (`apexsionscore.admin.sions`)**:
  - `/sions status`: Memeriksa jumlah blok termodifikasi dan sisa waktu hitung mundur.
  - `/sions restore`: Reset instan darurat.
  - `/sions bypass`: Toggle mode arsitek untuk modifikasi permanen.
  - `/sions tp`: Teleportasi ke pusat ibukota Sions.

---

## 🧙‍♂️ Integrasi Citizens NPC & Native Command Binding (`/k`)

Sistem integrasi NPC untuk pemilihan kerajaan dan navigasi ibukota menggunakan **Native Command Binding** bawaan Citizens 2 untuk menjamin stabilitas klik tanpa konflik event listener hardcoded:

1. **Logika Otomatis Perintah `/k`**:
   - **Pemain Baru (Belum Memilih Kerajaan)**: Menjalankan `/k` otomatis membuka GUI pemilihan 3 kerajaan (`RegionSelectionGUI`).
   - **Pemain Sudah Berikrar**: Menjalankan `/k` otomatis menteleportasi pemain ke ibukota kerajaan asalnya (*Zenithar*, *Solterra*, atau *Sylvamoor*) via `RegionTeleportService` disertai feedback audio dan pesan visual.
2. **Cara Mengaitkan Perintah ke NPC Citizens**:
   Admin/Staff cukup menargetkan NPC di server dan menjalankan:
   ```bash
   /npc sel <id>
   /npc cmd add -p k
   ```
   *Bendera `-p` memastikan perintah dieksekusi atas nama pemain yang mengklik NPC.*

---

## 📡 WebBridge Delivery & Siaran Global In-Game

Modul `ApexsionsCore` terhubung langsung dengan sistem pengiriman asinkron WebBridge Azuriom:
1. **Siaran Global Admin (`broadcast` / `bc`)**:
   - Dispatched langsung dari Web Dashboard (`POST /admin/apexsions/broadcast`) dengan target entitas `GLOBAL` / `ALL_PLAYERS`.
   - Diparsing secara native menggunakan Kyori Adventure `MiniMessage` dan disiarkan ke seluruh pemain aktif disertai efek audio notifikasi (`Sound.BLOCK_NOTE_BLOCK_BELL`).
2. **Sinkronisasi Karakter Otomatis (`sync-player`)**:
   - Menghubungkan statistik in-game (Level, XP, Saldo, Kerajaan, Rank, dan Gelar) ke basis data web.
   - Karakter pemain in-game tetap tercatat di web meskipun belum menautkan akun web (`user_id = null`), sehingga profil publik pemain tetap dapat diakses di portal web.
