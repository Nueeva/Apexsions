# 📖 Technical Documentation — ApexsionsCore Plugin

Dokumentasi teknis komprehensif untuk pengembang dan arsitek sistem yang bekerja pada **ApexsionsCore** (Minecraft Paper 26.2, Java 21+).

---

## 📑 Daftar Isi

1. [Arsitektur & Diagram Sistem](#1-arsitektur--diagram-sistem)
2. [Struktur Paket & File Modular YAML](#2-struktur-paket--file-modular-yaml)
3. [Database & Skema Migrasi (Flyway)](#3-database--skema-migrasi-flyway)
4. [Sistem Level, Formula, & Dynamic Titles](#4-sistem-level-formula--dynamic-titles)
5. [Sistem Klaim Hadiah & Milestone Rewards (Level 1–100)](#5-sistem-klaim-hadiah--milestone-rewards-level-1100)
6. [Interactive Chest GUIs](#6-interactive-chest-guis)
7. [Sistem 13 XP Handlers & Anti-Abuse](#7-sistem-13-xp-handlers--anti-abuse)
8. [Integrasi BlueMap & Spatial Territory](#8-integrasi-bluemap--spatial-territory)
9. [Integrasi Citizens2 NPC](#9-integrasi-citizens2-npc)
10. [Public API & Custom Events](#10-public-api--custom-events)

---

## 1. Arsitektur & Diagram Sistem

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        MINECRAFT SERVER RUNTIME                        │
│                                                                        │
│  [Player Events]  ──►  [13 XP Handlers]  ──► [Anti-Abuse Checks]       │
│         │                                           │                  │
│         │                                           ▼                  │
│         │                                   [LevelManager]             │
│         │                                           │                  │
│         ▼                                           ▼                  │
│  [Citizens NPC] ──► [KingdomProfileGUI]      [LevelUpEvent]            │
│         │                  │                        │                  │
│         │                  ▼                        ▼                  │
│         │          [LevelRewardsGUI] ◄───   [RewardManager]            │
│         │                  │                        │                  │
│         ▼                  ▼                        ▼                  │
│   [BlueMap] ◄────── [RegionManager] ◄────── [PlayerData] ◄── [Cache]  │
│                                                     │                  │
│                                              (Async I/O Batch)         │
│                                                     │                  │
└─────────────────────────────────────────────────────┼──────────────────┘
                                                      ▼
                                          ┌───────────────────────┐
                                          │    DatabaseManager    │
                                          │   (HikariCP Pool)     │
                                          └───────────┬───────────┘
                                                      │
                                                      ▼
                                          ┌───────────────────────┐
                                          │ PostgreSQL / SQLite   │
                                          └───────────────────────┘
```

---

## 2. Struktur Paket & File Modular YAML

### Package Namespace: `com.yourserver.apexsionscore`

| Package | Tanggung Jawab Utama |
|---|---|
| `.api` | Interface publik `ApexsionsCoreAPI`, implementasi `ApexsionsCoreAPIImpl`, dan service locator `ApexsionsCoreProvider`. |
| `.cache` | `PlayerCache` berbasis Caffeine Cache ($O(1)$) untuk penyimpanan profile pemain aktif in-memory. |
| `.command` | Executor dan tab-completer untuk `/lobby`, `/region` (alias `/kingdom`), `/level`, dan `/apexsionscore` (alias `/ac`, `/kc`). |
| `.config` | `ConfigManager` yang mengelola 9 file YAML modular terpisah. |
| `.database` | `DatabaseManager` (HikariCP + Flyway + SQLite Fallback), `PlayerRepository`, dan `RegionRepository`. |
| `.event` | Event kustom Paper: `KingdomXpGainEvent`, `KingdomLevelUpEvent`, `KingdomRegionChooseEvent`, `KingdomRegionChangeEvent`. |
| `.integration`| Integrasi soft-dependency: `LuckPermsRankProvisioner`, `LuckPermsHook`, `VaultHook`, `PlaceholderApiHook`, `EssentialsHook`, `BlueMapHook`, `CitizensHook`. |
| `.level` | `LevelManager`, `LevelFormula`, `LevelTitleResolver`. |
| `.level.reward` | `Reward` domain model dan `RewardManager` (pengelola klaim reward level 1–100 dan milestone). |
| `.level.xp` | `XpSource` enum, `XpSourceHandler` interface, `XpSourceRegistry`, dan `XpService`. |
| `.level.xp.antiabuse` | `BlockPlacementTracker`, `PvpKillTracker`, `MovementTracker`. |
| `.level.xp.handlers` | 13 class event handler untuk masing-masing kategori XP. |
| `.player` | `PlayerData` domain model, `PlayerDataService`, `PlayerListener`, `TerritoryListener`. |
| `.region` | `Region` domain model, `RegionManager`, `RegionTeleportService`, `TerritoryPolygon`. |
| `.region.gui` | `RegionSelectionGUI` (45 slots), `KingdomProfileGUI` (45 slots), `LevelRewardsGUI` (54 slots deterministik), `XpGuideGUI` (45/54 slots paginated). |

### File Konfigurasi Modular (9 File YAML):
- **`config.yml`**: Database, server name (`Apexsions`), lobby location, BlueMap settings, level formula parameters.
- **`ranks.yml`**: Definisi 9 rank LuckPerms (The Ancestor, Sions, Emperor, Sovereign, Archon, Ascendant, Warden, Herald, Wanderer) dan konfigurasi UUID owner.
- **`kingdoms.yml`**: Definisi lengkap 3 kerajaan (Zenithar, Solterra, Sylvamoor), blok icon, slot GUI, lore, spawn, warna.
- **`titles.yml`**: Tangga level title untuk default dan tiap kerajaan (Level 1–10 s/d 91–100).
- **`xp.yml`**: Pengaturan 13 sumber XP dan parameter cache/cooldown anti-abuse.
- **`rewards.yml`**: Pengaturan hadiah Level 2 s/d 100 dengan hadiah istimewa milestone (11, 21, 31, 41, 51, 61, 71, 81, 91, 100).
- **`chat.yml`**: Pengaturan template chat, format tag warna kingdom (Zenithar, Solterra, Sylvamoor, Unpledged), dan rank LuckPerms.
- **`messages.yml`**: Pesan teks, MiniMessage templates, toast, dan notifikasi server.
- **`gui.yml`**: Konfigurasi judul dan ukuran inventori Chest GUI.

---

## 3. Sistem Chat Formatting & Integrasi LuckPerms (`chat.yml`)

Chat di-render melalui event `io.papermc.paper.event.player.AsyncChatEvent` menggunakan Adventure `MiniMessage`:

```text
[Lv. {level} {title}] [{rank}] {kingdom} {player} » {message}
```

- `{level}`: Nilai level (1–100) dari cache KingdomCore.
- `{title}`: Gelar level pemain dari `LevelTitleResolver`.
- `{rank}`: Group/Prefix aktif dari LuckPerms via `LuckPermsHook` (mendukung konversi kode warna `&`).
- `{kingdom}`: Tag nama kerajaan dengan warna resmi:
  - 🟡 **Zenithar**: `<yellow>[Zenithar]</yellow>` (Gold/Yellow)
  - 🔴 **Solterra**: `<red>[Solterra]</red>` (Red/Crimson)
  - 🔵 **Sylvamoor**: `<aqua>[Sylvamoor]</aqua>` (Blue/Azure)
  - ⚪ **Unpledged**: `<gray>[Unpledged]</gray>` (Gray)
- `{player}`: Nama pemain pengirim pesan.
- `{message}`: Isi pesan chat.

---

## 3. Database & Skema Migrasi (Flyway)

Tabel `players` menyimpan identitas pemain, level, XP, afiliasi kerajaan, dan daftar reward yang telah diklaim (`claimed_rewards`):

```sql
CREATE TABLE IF NOT EXISTS players (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    xp BIGINT NOT NULL DEFAULT 0,
    region_id VARCHAR(36),
    claimed_rewards TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. Sistem Level, Formula, & Dynamic Titles

- **Kurva XP**: `requiredXp = base * (level ^ exponent)`
- **Dynamic Titles**: Ditentukan berdasarkan kombinasi Kerajaan + Rentang Level pemain (contoh: Level 25 di Zenithar $\to$ `Sky Warden`, Level 95 di Solterra $\to$ `Lord of Solterra`).

---

## 5. Sistem Klaim Hadiah & Milestone Rewards (Level 1–100)

Sistem progression reward diatur melalui `RewardManager`:
- Setiap kenaikan level membuka paket hadiah baru.
- **Hadiah Istimewa Milestone (Angka 1 Tiap 10 Level & Level 100)**:
  - Level 11, 21, 31, 41, 51, 61, 71, 81, 91, dan 100 memiliki efek glowing, suara istimewa, siaran broadcast server, koin berlipat, dan kunci peti langka.
- Pemain dapat mengklaim satu per satu atau menggunakan tombol **1-Click Claim All** (`HOPPER` di slot 4).

---

## 6. Interactive Chest GUIs & InventoryHolder Architecture

Semua GUI dibangun menggunakan pola **Custom `InventoryHolder`** (`RegionSelectHolder`, `KingdomProfileHolder`, `LevelRewardsHolder`, `XpGuideHubHolder`, `XpCategoryDetailHolder`), menjamin deteksi klik 100% akurat tanpa bergantung pada pencocokan string teks judul.

1. **`RegionSelectionGUI` (45 Slots)**:
   - Menampilkan 3 kartu kerajaan dengan border gelap dan sudut cyan.
   - Deteksi status kewarganegaraan: `✔ WARGA RESMI KERAJAAN`, `🔒 TERKUNCI`, atau `» KLIK UNTUK MEMILIH KERAJAAN «`.
2. **`KingdomProfileGUI` (45 Slots)**:
   - Dibuka melalui `/level`, `/kingdom info`, atau klik NPC Guide.
   - Player Head dengan status level, visual XP Progress Bar (`■■■■■■░░░░░░ 50%`), gelar tingkat, kerajaan, territory lokasi berdiri, dan status reward tertunda.
   - Label dan teks lore didesain ringkas, bersih, dan tidak memotong batas layar.
   - Shortcut ke Menu Hadiah Level, Direktori Panduan XP, dan Fast Travel ke Ibukota.
3. **`LevelRewardsGUI` (54 Slots)**:
   - **Player Progression Crest (Slot 4)**: Status level, EXP, progress bar, gelar, dan hadiah yang belum diklaim.
   - **Tombol 1-Click Claim All (Slot 6)**: Klaim instan seluruh reward dengan efek berkilau ketika ada reward tertunda.
   - **Authentic Reward Icons (Grid Slots 10–43)**: Ikon asli dari `rewards.yml` dengan badge ringkas (`✔ Level X`, `★ Lv.X Milestone`, `🔒 Level X`).
   - **Pagination Indicator**: Halaman 1–4 terstruktur rapi.
4. **`XpGuideGUI` (Hub 45 Slots & Sub-Kategori 54 Slots dengan Pagination)**:
   - **Main Hub (45 Slots)**: 10 kartu direktori kategori gameplay (Mining, Woodcutting, Farming, Monster & Boss, Duel PvP, Fishing, Cooking, Brewing, Enchanting, Exploration).
   - **Sub-Menu Kategori (54 Slots dengan Pagination)**:
     - **100% Dipisah Masing-Masing**: Seluruh 43+ monster (Spider, Cave Spider, Zombie, Husk, Drowned, Skeleton, Stray, Wither Skeleton, Slime, Magma Cube, Cow, Sheep, Pig, Chicken, dll) memiliki Spawn Egg dan slot tersendiri tanpa digabung.
     - Seluruh ore batuan, jenis kayu, tanaman panen, hewan breeding, dan masakan memiliki kartu individu dengan rincian XP presisi dan 2 baris lore yang rapi.
     - Dilengkapi tombol navigasi halaman (`« Prev`, `Hal X/Y`, `Next »`) jika kategori memuat lebih dari 28 jenis.

---

## 7. Sistem 13 XP Handlers dengan Pemetaan Spesifik Per-Item & Per-Mob

| Kategori | Listener & Mekanisme | Cakupan Item / Mob Spesifik & Anti-Abuse |
|---|---|---|
| **Mining** | `BlockBreakEvent` | 35+ item: Ancient Debris (50 XP), Deepslate Emerald (30), Emerald (25), Raw Gold (25), Deepslate Diamond (20), Diamond (15), Crying Obsidian (15), Obsidian (12), Gold (8-10), Redstone/Lapis (6-7), Iron (5-6), Coal/Copper (3-4), Amethyst (6), Stones (1). `BlockPlacementTracker` mencegah XP dari blok buatan pemain. |
| **Woodcutting** | `BlockBreakEvent` | 15+ jenis kayu: Crimson/Warped Stem (4 XP), Jungle/Dark Oak/Cherry/Pale Oak/Mangrove/Acacia (3 XP), Oak/Birch/Spruce/Bamboo (2 XP), Mushroom Blocks (1 XP). |
| **Fishing** | `PlayerFishEvent` | 16+ tangkapan: Enchanted Book (50 XP), Nautilus Shell (45 XP), Saddle (35 XP), Name Tag (30 XP), Pufferfish (20 XP), Tropical Fish (15 XP), Salmon (10 XP), Cod (8 XP), Trash (1-5 XP). |
| **Mob Combat** | `EntityDeathEvent` | 45+ mobs: Ender Dragon (500 XP), Wither (250 XP), Warden (200 XP), Elder Guardian (80 XP), Ravager (60 XP), Evoker (40 XP), Piglin Brute (35 XP), Breeze (30 XP), Shulker (25 XP), Wither Skeleton/Witch/Iron Golem (20 XP), Ghast (18 XP), Enderman (16 XP), Guardian/Vindicator (15 XP), Phantom (14 XP), Creeper/Blaze/Pillager (12 XP), Zombie/Skeleton/Drowned/Husk (8-10 XP), Hewan (1-3 XP). |
| **Player Kill** | `PlayerDeathEvent` | 25 XP per kill pemain. `PvpKillTracker` menerapkan cooldown 120 detik per pasangan killer-victim (anti-farming). |
| **Farming** | `BlockBreakEvent` & `EntityBreedEvent` | 16+ tanaman matang (Torchflower/Pitcher 6 XP, Nether Wart/Chorus 5 XP, Cocoa 4 XP, Wheat/Carrot/Potato/Beetroot/Melon/Pumpkin 3 XP, Berries 2 XP, Cane/Cactus/Bamboo 1 XP) & Breeding per animal (Sniffer 25 XP, Unta 15 XP, Panda 12 XP, Kuda/Penyu 10 XP, Sapi/Domba/Babi 5 XP, Ayam 3 XP). |
| **Anvil** | `InventoryClickEvent` | Combine item (10 XP), Apply Book (8 XP), Repair Gear (6 XP), Rename (3 XP). |
| **Enchanting** | `EnchantItemEvent` | Tier 3 (50 XP), Tier 2 (25 XP), Tier 1 (10 XP) + Bonus 2 XP per level cost. |
| **Cooking** | `FurnaceExtractEvent` | Netherite Scrap (30 XP), Gold Ingot (6 XP), Iron Ingot (4 XP), Daging Panggang/Salmon (3 XP), Ayam/Kentang/Tembaga (2 XP), Kaca/Batu (1 XP). |
| **Golden Apple** | `PlayerItemConsumeEvent` | Enchanted Golden Apple (60 XP), Golden Apple (10 XP). |
| **Brewing** | `InventoryClickEvent` | Turtle Master (15 XP), Invisibility (12 XP), Regen/Strength (10 XP), Healing/Fire Res/Slow Falling (8 XP), Speed/Night Vision (7 XP), Poison/Weakness (6 XP). |
| **Potion Use** | `PlayerItemConsumeEvent` & `PotionSplashEvent` | Lingering Potion (6 XP), Splash Potion (4 XP), Drink Potion (3 XP). |
| **Exploration** | `PlayerMoveEvent` | Berenang 16m (2 XP), Elytra 32m (2 XP), Jalan/Lari 16m (1 XP), Tunggangan 32m (1 XP), Melompat (1 XP / 5s cd). `MovementTracker` mengakumulasi jarak real-time. |

---

## 8. Integrasi BlueMap & Spatial Territory

- **`BlueMapConfigParser`**: Membaca file `world.conf` untuk mengekstrak definisi koordinat polygon `shape: [{x, z}]`, titik spawn, dan warna.
- **`TerritoryPolygon`**: Mesin komputasi geometri 2D/3D berbasis *Ray-Casting Point-in-Polygon* dengan fast bounding box rejection ($O(1)$).
- **`TerritoryListener`**: Mengirimkan pemberitahuan ActionBar saat pemain melintasi batas kerajaan atau memasuki zona netral Wilderness.

---

## 9. Integrasi Citizens2 NPC

- **`KingdomGuideTrait`**: Trait kustom Citizens dengan identifier `kingdom-guide`.
- **`CitizensHook`**: Mendeteksi interaksi klik kanan pada NPC bertrait `kingdom-guide` atau bernuansa nama *Guide / Penjaga / Kingdom*.
  - Pemain yang belum memilih kerajaan $\to$ membuka `RegionSelectionGUI`.
  - Pemain yang sudah terdaftar $\to$ membuka `KingdomProfileGUI`.

---

## 10. Public API & Custom Events

Plugin eksternal dapat berinteraksi langsung melalui `KingdomCoreProvider.get()`:

```java
KingdomCoreAPI api = KingdomCoreProvider.get();

// Level & XP
int level = api.getLevel(player.getUniqueId());
String title = api.getLevelTitle(player.getUniqueId());
api.addXp(player.getUniqueId(), 500, XpSource.CUSTOM);

// Spatial Territory
Optional<Region> currentKingdom = api.getKingdomAt(player.getLocation());
boolean inTerritory = currentKingdom.map(reg -> api.isInKingdomTerritory(player, reg)).orElse(false);
```
