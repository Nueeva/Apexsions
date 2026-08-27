# PRD — KingdomCore

**Status:** Draft v1.3  
**Target:** Minecraft Paper 26.2  
**Plugin:** `KingdomCore`  
**Database:** PostgreSQL  
**Future external application Backend:** Go  
**Frontend:** Astro  
**Primary Language:** Java

---

## 1. Ringkasan Produk

**KingdomCore** adalah plugin inti custom untuk server Minecraft yang menjadi pusat sistem:

- Region/Kingdom pemain
- Level 1–100
- XP dan progression
- Level title berdasarkan Region
- Integrasi rank LuckPerms
- Chat formatting
- Player metadata
- `/region`
- `/region choose`
- `/region info`
- `/lobby`
- Sinkronisasi data dengan website
- API untuk sistem eksternal

KingdomCore **bukan pengganti LuckPerms atau EssentialsX**.

### Arsitektur tingkat tinggi

```text
                    INTERNET
                       │
                       │ HTTPS
                       ▼
              ┌─────────────────┐
              │    WEBSITE      │
              │ Astro Frontend  │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   Go Backend    │
              │    REST API     │
              └────────┬────────┘
                       │
                       ▼
                ┌─────────────┐
                │ PostgreSQL  │
                └──────┬──────┘
                       ▲
                       │
                 PostgreSQL
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              MINECRAFT SERVER              │
│                                             │
│                Paper 26.2                   │
│                     │                       │
│              ┌──────▼──────┐                │
│              │ KingdomCore │                │
│              └──────┬──────┘                │
│                     │                       │
│     ┌───────────────┼────────────────┐      │
│     ▼               ▼                ▼      │
│ LuckPerms        Vault         PlaceholderAPI│
│     │               │                │      │
│     └───────────────┼────────────────┘      │
│                     ▼                       │
│                EssentialsX                 │
│                                             │
│                     │                       │
│                     ▼                       │
│                    TAB                     │
│              (konfigurasi eksternal)       │
└─────────────────────────────────────────────┘
```

---

# 2. Tujuan

## Primary Goals

1. Membuat sistem Region yang fleksibel.
2. Membuat progression level 1–100.
3. Membuat title berbeda berdasarkan Region + level.
4. Mengintegrasikan rank LuckPerms.
5. Menyediakan format chat yang konsisten.
6. Menyediakan `/lobby` dan `/region`.
7. Menyimpan data progression secara persisten di PostgreSQL.
8. Menyediakan API untuk website.
9. Membuat arsitektur modular yang mudah dikembangkan menggunakan AI coding agent.
10. Menyediakan API internal agar plugin lain dapat berinteraksi dengan KingdomCore tanpa mengakses database secara langsung.

## Non-Goals

KingdomCore tidak bertanggung jawab atas:

- Permission management utama
- Tablist management
- Economy EssentialsX
- Homes
- Teleport umum
- Mini-games
- World generation
- World protection
- Authentication website
- Sistem quest kompleks pada MVP

---

# 3. Technology Stack

## Minecraft

| Komponen | Teknologi |
|---|---|
| Server | Paper 26.2 |
| Plugin Language | Java |
| API | Paper API |
| Build System | Gradle Kotlin DSL |
| Database | PostgreSQL |
| Database Driver | PostgreSQL JDBC |
| Connection Pool | HikariCP |
| Cache | Caffeine |
| Migration | Flyway |
| Serialization | Jackson |
| Configuration | YAML |
| Text/Chat | Adventure API |
| Logging | Paper Logger / SLF4J |

> Paper 26.2 compatibility is the implementation baseline.
>
> - Runtime/build target: **Java 25+**
> - Build system: **Gradle Kotlin DSL**
> - Plugin manifest: **`plugin.yml`**
> - API target: **Paper API 26.2**
> - Platform target: **Paper only for MVP**
> - Folia is **not supported in MVP**.
> - Use Paper/Bukkit public APIs and Adventure APIs wherever possible.
> - Do not depend on NMS/internal implementation classes unless a requirement is proven impossible through the public API.
> - Do not use experimental `paper-plugin.yml` as the primary plugin model for MVP.
>
> The exact Paper API build may be pinned during implementation. The project must not silently float across incompatible API versions in production.

---

# 3A. Paper 26.2 Implementation Baseline

This section is normative for implementation.

## Project Layout

```text
KingdomCore/
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        ├── java/
        └── resources/
            ├── plugin.yml
            ├── config.yml
            └── db/
                └── migration/
```

## Gradle Baseline

```kotlin
repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}
```

For production builds, the exact Paper API build may be pinned after validation rather than relying indefinitely on a moving `+` selector.

## Plugin Manifest

Use the standard Bukkit/Paper-compatible `plugin.yml` model for MVP.

Minimum baseline:

```yaml
name: KingdomCore
version: 0.1.0
main: com.yourserver.kingdomcore.KingdomCorePlugin
api-version: '26.2'
```

The final manifest must also declare commands, permissions, authorship/description where appropriate, and dependency/soft-dependency relationships according to the actual installed stack.

`paper-plugin.yml` is not the primary manifest for MVP because Paper's modern plugin model is experimental.

## API Compatibility

Implementation must target the public Paper 26.2 API and avoid deprecated/legacy APIs when an equivalent supported API exists.

For text, chat, titles, messages, and other rich text output, use Adventure Components rather than building new functionality around legacy `ChatColor`/legacy-string formatting.

# 4. Plugin Dependencies

## Required

```text
Paper 26.2
LuckPerms
Vault
EssentialsX
EssentialsXSpawn
```

## Optional / External

```text
PlaceholderAPI
TAB
Citizens / NPC plugins
Mini-game plugins
World protection plugins
```

### Dependency Rules

- **LuckPerms**: required. Used for rank, prefix, metadata, and permission state.
- **Vault**: required for compatibility with the server integration layer where needed.
- **EssentialsX**: required for the planned server utility stack.
- **EssentialsXSpawn**: required if EssentialsX is used to provide spawn functionality.
- **PlaceholderAPI**: optional. KingdomCore must still start and operate when PlaceholderAPI is absent. Its integration layer is disabled gracefully.
- **TAB**: optional and externally configured. KingdomCore exposes data through PlaceholderAPI/API; TAB owns presentation.
- **Citizens / NPC plugins**: optional. KingdomCore does **not** require a Citizens dependency or Citizens API hook for the MVP. NPC plugins may dispatch KingdomCore commands.
- Mini-game and world-protection plugins are external integrations and are not KingdomCore dependencies.

KingdomCore must not access another plugin's private database directly.

---

# 5. Future external application Stack

## Frontend

```text
Astro
```

Frontend bertanggung jawab terhadap:

- Player profile
- Future leaderboard integrations
- Region information
- Server status
- Public statistics
- Admin dashboard pada fase berikutnya

## Backend

```text
Go
REST API
JSON
PostgreSQL
```

Backend bertanggung jawab terhadap:

- Authentication
- Authorization
- Player API
- Region API
- Future leaderboard integration API
- Admin API
- Future external application business logic
- Rate limiting
- Database access

### Future external application Architecture

```text
Browser
   │
   │ HTTPS
   ▼
Astro
   │
   │ HTTPS/JSON
   ▼
Go API
   │
   ▼
PostgreSQL
```

PostgreSQL **tidak boleh diekspos langsung ke internet**.

---

# 6. Database Architecture

PostgreSQL menjadi source of truth untuk data persisten KingdomCore.

## Player Data

```sql
CREATE TABLE players (
    uuid UUID PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    xp BIGINT NOT NULL DEFAULT 0,
    region_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT players_level_range
        CHECK (level >= 1 AND level <= 100),

    CONSTRAINT players_xp_non_negative
        CHECK (xp >= 0)
);
```

## Region Data

```sql
CREATE TABLE regions (
    id UUID PRIMARY KEY,
    key VARCHAR(32) UNIQUE NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    world_name VARCHAR(128) NOT NULL,

    spawn_x DOUBLE PRECISION,
    spawn_y DOUBLE PRECISION,
    spawn_z DOUBLE PRECISION,
    spawn_yaw FLOAT,
    spawn_pitch FLOAT,

    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
```

## Relasi

```text
players.region_id
        │
        ▼
regions.id
```

Player tidak menyimpan nama region secara langsung.

Contoh:

```text
Player
├── UUID
├── Username
├── Level
├── XP
└── Region ID
       │
       ▼
    Region
    ├── Key
    ├── Name
    ├── World
    └── Spawn
```

---

# 7. Region System

Region adalah domain object milik KingdomCore.

Region **bukan** LuckPerms group.

## Pembagian tanggung jawab

```text
KingdomCore
└── Region
    ├── Region selection
    ├── Region storage
    ├── Region information
    └── Region teleportation

LuckPerms
└── Rank
    ├── Wanderer
    ├── Ascendant
    ├── Archon
    ├── Sovereign
    ├── Emperor
    ├── Sions
    ├── Herald
    ├── Warden
    └── The Ancestor
```

## Kenapa tidak menggunakan `/tag` Minecraft?

Minecraft `/tag` hanya cocok untuk flag sederhana.

Contoh:

```text
region_nusantara
```

Tetapi kebutuhan server memerlukan:

- Persistence
- Region lookup
- Region display name
- Region teleport
- Region-specific level title
- Region API
- Future external application integration
- Region statistics
- Kemungkinan region economy/quest di masa depan

Karena itu `/tag` tidak digunakan sebagai source of truth.

## Kenapa tidak menggunakan LuckPerms?

LuckPerms dapat digunakan untuk context, tetapi region adalah data domain server, bukan permission group.

Region disimpan oleh KingdomCore.

---

# 8. Region Selection

Ketika player pertama kali masuk:

```text
Player Join
    │
    ▼
Has Region?
   / \
 NO   YES
 │     │
 ▼     ▼
Choose Continue
Region
```

Command:

```text
/region choose
```

akan membuka GUI pilihan region.

Contoh:

```text
┌───────────────────────────────┐
│       CHOOSE YOUR KINGDOM     │
│                               │
│       [ NUSANTARA ]           │
│       [ REGION II ]           │
│       [ REGION III ]          │
└───────────────────────────────┘
```

### Command Sender Rules

`/region choose` adalah player-context command.

```text
Player sender  → buka GUI
Console sender → tolak dengan pesan yang sesuai
NPC sender     → valid jika NPC mendispatch command sebagai player
```

`/region`, `/region info`, dan `/lobby` juga harus memvalidasi sender sesuai kebutuhan command.

Setelah memilih:

```text
GUI
 │
 ▼
RegionManager
 │
 ▼
PlayerData
 │
 ▼
PostgreSQL
```

Region pertama menjadi region player.

Perubahan region setelah pemilihan pertama tidak boleh dilakukan secara bebas.

Mekanisme perubahan region di masa depan dapat berupa:

```text
/region change
```

dengan cooldown, permission, biaya, atau mekanisme lore.

---

# 9. Level System

Level player:

```text
1 → 100
```

Player baru:

```text
level = 1
xp = 0
```

## LevelManager API

```java
int getLevel(UUID player);

long getXp(UUID player);

void addXp(UUID player, long amount);

void setLevel(UUID player, int level);

void setXp(UUID player, long xp);
```

Level tidak dibuat sebagai 100 group LuckPerms.

Jangan membuat:

```text
level1
level2
level3
...
level100
```

Level adalah data progression.

---

# 10. XP System

XP adalah sumber utama progression level KingdomCore. Prinsip desainnya adalah **aktivitas gameplay player menjadi sumber XP**, bukan hanya beberapa aktivitas yang dipilih secara sempit.

Level tetap berada pada rentang:

```text
1 → 100
```

Player baru:

```text
level = 1
xp    = 0
```

## 10.1 XP Sources

MVP harus menyediakan XP hooks/handlers untuk seluruh kategori aktivitas player yang relevan.

### Mining / Block Breaking

Player mendapatkan XP dari mining/breaking block yang **spawn atau terbentuk secara natural sebagai bagian dari world resource**, termasuk namun tidak terbatas pada stone, deepslate, ore, dan resource block natural lainnya.

Contoh:

```text
Stone
Deepslate
Coal Ore
Copper Ore
Iron Ore
Gold Ore
Redstone Ore
Lapis Ore
Diamond Ore
Emerald Ore
Nether Quartz Ore
Nether Gold Ore
Ancient Debris
dan resource block natural lainnya
```

**Block building/decorative placement tidak menjadi sumber mining XP.**

Sistem harus mencegah abuse dari loop block placement → break untuk menghasilkan XP tanpa batas.

### Fishing

XP diberikan dari aktivitas memancing, dengan hasil/catch dapat menjadi dasar scaling XP jika balancing membutuhkannya.

### Woodcutting

XP diberikan dari penebangan/log breaking yang valid.

Sistem harus membedakan aktivitas woodcutting dengan block building/decorative placement dan mencegah farming XP melalui artificial block placement loops.

### Mob Killing

XP diberikan ketika player membunuh entity/mob yang valid, mencakup:

```text
Hostile mobs
Passive mobs
Neutral mobs
```

Nilai XP dapat berbeda berdasarkan entity type dan tingkat kesulitan/rarity.

### Player Killing

Player kill dapat memberikan XP.

Sistem harus memiliki konfigurasi anti-abuse untuk mencegah farming XP melalui kill berulang terhadap player yang sama, alternate account, atau pola kill yang tidak wajar.

### Farming / Agriculture

Aktivitas farming yang valid dapat memberikan XP, termasuk crop harvesting dan aktivitas agriculture/breeding yang ditentukan konfigurasi.

Nilai XP dan cooldown/eligibility harus configurable agar automated farms tidak menghasilkan XP tak terbatas.

### Anvil

Aktivitas valid menggunakan anvil dapat memberikan XP, seperti repair, rename, atau combine yang benar-benar berhasil.

XP harus diberikan berdasarkan aksi yang berhasil dan tidak sekadar ketika UI anvil dibuka.

### Enchanting

Aktivitas enchanting yang berhasil dapat memberikan XP.

### Cooking / Smelting

Aktivitas memasak atau smelting yang berhasil melalui furnace-family mechanics dapat memberikan XP.

Contoh:

```text
Furnace
Blast Furnace
Smoker
```

XP harus dikaitkan dengan hasil proses yang benar-benar selesai, bukan sekadar memasukkan item ke furnace.

### Golden Apple Consumption

Makan hanya menjadi XP source untuk:

```text
Golden Apple
Enchanted Golden Apple
```

Makanan biasa tidak otomatis memberikan XP melalui kategori ini.

### Brewing

Aktivitas brewing/pembuatan ramuan yang valid dapat memberikan XP.

### Potion Consumption / Usage

Menggunakan atau meminum potion yang valid dapat memberikan XP sesuai konfigurasi.

Sistem harus membedakan aksi konsumsi/penggunaan yang benar-benar terjadi dari sekadar memegang atau memindahkan potion.

### Exploration / Movement

Eksplorasi merupakan XP source tersendiri.

Aktivitas yang termasuk:

```text
Walking
Running
Swimming
Jumping
General movement/exploration
```

XP movement **tidak boleh diberikan setiap tick** secara naif.

Implementasi harus menggunakan sistem berbasis jarak/waktu/cooldown atau checkpoint:

```text
Player bergerak sejumlah distance threshold
        ↓
XP eligible?
        ↓
Grant XP
```

Sistem wajib mencegah AFK movement, repeated jumping in one location, water movement loops, atau bot-like movement menjadi sumber XP tak terbatas.

## 10.2 XP Source Registry

XP source harus dibuat extensible.

Arsitektur yang disarankan:

```text
XP Event
   ↓
XpSourceHandler
   ↓
Eligibility Check
   ↓
Anti-Abuse / Cooldown
   ↓
XP Amount Resolver
   ↓
LevelManager.addXp()
```

Contoh package:

```text
progression/
├── LevelManager
├── XpService
├── XpSource
├── XpSourceHandler
├── XpAmountResolver
├── LevelTitleResolver
└── antiabuse/
```

Jangan membuat seluruh XP logic menjadi satu `PlayerListener` raksasa.

## 10.3 XP Configuration

Semua nilai XP harus configurable.

Contoh schema:

```yaml
level:
  min: 1
  max: 100

xp:
  formula: "configurable"

  sources:

    mining:
      enabled: true
      default: 1
      natural_blocks_only: true

    fishing:
      enabled: true
      default: 5

    woodcutting:
      enabled: true
      default: 2

    mob_kill:
      enabled: true
      hostile_default: 5
      passive_default: 2

    player_kill:
      enabled: true
      default: 25

    farming:
      enabled: true
      default: 2

    anvil:
      enabled: true
      default: 5

    enchanting:
      enabled: true
      default: 10

    cooking:
      enabled: true
      default: 2

    golden_apple:
      enabled: true
      golden_apple: 10
      enchanted_golden_apple: 25

    brewing:
      enabled: true
      default: 5

    potion_use:
      enabled: true
      default: 3

    exploration:
      enabled: true
      movement:
        distance_threshold: 16
        xp: 1
      swimming:
        distance_threshold: 16
        xp: 1
      jumping:
        cooldown_seconds: 10
        xp: 1
```

**Angka di atas hanya contoh schema, bukan balancing final.**

Balancing XP harus dapat diubah tanpa recompiling plugin.

## 10.4 Level Formula

Formula XP level harus configurable dan mendefinisikan XP yang dibutuhkan untuk mencapai level berikutnya.

KingdomCore tidak boleh mengasumsikan semua level membutuhkan XP yang sama.

Formula final belum ditentukan oleh PRD ini dan harus ditetapkan setelah balancing/testing.

## 10.5 XP Validation

KingdomCore harus:

- Menolak XP negatif.
- Membatasi level pada 1–100.
- Memvalidasi semua XP source.
- Tidak mempercayai nilai XP dari client.
- Mencegah duplicate event processing.
- Mencegah artificial block placement → break XP loops.
- Mencegah repeated player-kill farming.
- Mencegah movement/jump AFK farming.
- Menangani XP overflow ketika player sudah level 100.

## 10.6 Maximum Level

Pada level 100:

```text
level = 100
```

XP progression berhenti menaikkan level.

Default behavior:

```text
XP yang masuk setelah level 100 → tidak menaikkan level
```

Perilaku penyimpanan overflow XP harus configurable. Untuk MVP, disarankan XP progression tidak terus bertambah tanpa batas setelah level 100.

## 10.7 Events

KingdomCore harus menyediakan event internal/public API:

```text
XpGainedEvent
LevelUpEvent
```

Contoh:

```text
XpGainedEvent
├── player UUID
├── source
├── amount
├── oldXp
└── newXp

LevelUpEvent
├── player UUID
├── oldLevel
├── newLevel
└── region
```

Event dapat digunakan oleh:

```text
Quest
Achievement
Future server-integrated statistics
Future leaderboard integration
Future progression systems
```

## 10.8 Anti-Abuse Principle

XP harus diberikan karena **aksi gameplay yang bermakna**, bukan sekadar event teknis.

Contoh yang harus ditolak:

```text
Place block
    ↓
Break block
    ↓
Place block
    ↓
Break block
    ↓
∞ XP
```

atau:

```text
Player A kills Player B
Player B respawn
Player A kills Player B
∞ XP
```

atau:

```text
Jump
Jump
Jump
Jump
Jump
∞ XP
```

Sistem XP harus mempertimbangkan cooldown, distance threshold, source validation, ownership/origin information, dan histori aktivitas bila diperlukan.

---

# 11. Level Title System

Title adalah **derived data**.

Title ditentukan oleh:

```text
Region + Level
```

Bukan disimpan sebagai kolom player.

Contoh:

```text
Region: NUSANTARA
Level: 17
       │
       ▼
LevelTitleResolver
       │
       ▼
Configured 11-20 title
```

## Configuration

```yaml
level-titles:

  NUSANTARA:
    "1-10": "Citizen"
    "11-20": ""
    "21-30": ""
    "31-40": ""
    "41-50": ""
    "51-60": ""
    "61-70": ""
    "71-80": ""
    "81-90": ""
    "91-100": ""

  REGION_2:
    "1-10": "Citizen"
    "11-20": ""
    "21-30": ""
    "31-40": ""
    "41-50": ""
    "51-60": ""
    "61-70": ""
    "71-80": ""
    "81-90": ""
    "91-100": ""

  REGION_3:
    "1-10": "Citizen"
    "11-20": ""
    "21-30": ""
    "31-40": ""
    "41-50": ""
    "51-60": ""
    "61-70": ""
    "71-80": ""
    "81-90": ""
    "91-100": ""
```

Jika title belum diisi, resolver harus memiliki fallback yang aman.

---

# 12. Rank System

Rank dikelola oleh LuckPerms.

## Rank List

| Rank | Role | Color |
|---|---|---|
| The Ancestor | Owner | Dark Red + Bold |
| Warden | Admin | Dark Blue |
| Herald | Helper | Pink |
| Wanderer | Default | Gray |
| Ascendant | Player | Light Green |
| Archon | Player | Cyan |
| Sovereign | Player | Gold |
| Emperor | Player | Bright Red |
| Sions | Highest Player Rank | Aqua → Gold Gradient |

KingdomCore hanya membaca data rank dari LuckPerms.

KingdomCore tidak membuat tabel rank sendiri.

---

# 13. Rank Hierarchy

Player progression:

```text
Wanderer
    ↓
Ascendant
    ↓
Archon
    ↓
Sovereign
    ↓
Emperor
    ↓
Sions
```

Staff hierarchy:

```text
Herald
    ↓
Warden
    ↓
The Ancestor
```

Staff hierarchy dan player progression harus dipisahkan.

Contohnya:

```text
Sions ≠ Admin
Warden ≠ Player progression
```

Rank tinggi tidak otomatis memberikan permission staff.

---

# 14. LuckPerms Integration

KingdomCore menggunakan LuckPerms API untuk membaca:

- Primary group
- Prefix
- Metadata
- Permission state

Contoh:

```text
KingdomCore
      │
      │ LuckPerms API
      ▼
LuckPerms
      │
      ▼
Sovereign
```

KingdomCore tidak boleh membaca database LuckPerms secara langsung.

---

# 15. Chat Format

Format final:

```text
[Lv. 1 Citizen][Wanderer][Nusantara] Rifqi >> Halo!
```

Komponen:

```text
[Lv. {level} {title}]
[{rank}]
[{region}]
{name} >> {message}
```

Contoh level lain:

```text
[Lv. 23 Guardian][Ascendant][Nusantara] Rifqi >> Halo!
```

Data source:

```text
Level       → KingdomCore
Title       → KingdomCore
Rank        → LuckPerms
Region      → KingdomCore
Name        → Minecraft
Message     → Chat Event
```

---

# 16. PlaceholderAPI Integration

KingdomCore menyediakan PlaceholderAPI expansion.

## Placeholder

```text
%kingdomcore_level%
%kingdomcore_xp%
%kingdomcore_region%
%kingdomcore_region_name%
%kingdomcore_level_title%
```

Contoh:

```text
%kingdomcore_level%
```

Output:

```text
17
```

Contoh:

```text
%kingdomcore_region_name%
```

Output:

```text
Nusantara
```

PlaceholderAPI menjadi optional integration layer dengan plugin lain. Jika PlaceholderAPI tidak tersedia, KingdomCore tetap harus dapat berjalan dan menonaktifkan expansion hook dengan aman.

---

# 17. Nametag

Target:

```text
Lv. 17 Guardian
Rifqi
```

KingdomCore menyediakan data:

```text
Level
Title
Rank
Region
```

TAB dapat mengambil data tersebut menggunakan PlaceholderAPI atau mekanisme integrasi yang tersedia.

KingdomCore tidak wajib melakukan packet/NMS-based nametag rendering untuk MVP. Jika TAB menjadi renderer nametag, KingdomCore hanya menyediakan source data.

KingdomCore tidak mengambil alih konfigurasi TAB.

Architecture:

```text
TAB
 │
 ▼
PlaceholderAPI
 │
 ▼
KingdomCore
 │
 ├── Level
 └── Level Title
```

---

# 18. Citizens / NPC Compatibility

Citizens atau plugin NPC lain **bukan dependency KingdomCore**.

Untuk MVP, NPC hanya bertindak sebagai perantara/interaksi yang menjalankan command KingdomCore. Contoh:

```text
Player
  │
  │ interact
  ▼
NPC / Citizens
  │
  │ dispatch command as player
  ▼
/region
  │
  ▼
KingdomCore
  │
  ▼
RegionManager
  │
  ▼
Region Spawn
```

### Rules

1. KingdomCore tidak perlu mengimpor Citizens API hanya untuk menerima command.
2. `/region`, `/region info`, dan command lain yang membutuhkan player context harus dapat dijalankan sebagai player sender.
3. NPC harus meng-dispatch command **dengan player sebagai command sender** ketika command membutuhkan identitas player.
4. Console execution harus ditolak untuk command yang membutuhkan player context.
5. KingdomCore tidak boleh mengandalkan keberadaan Citizens untuk startup.
6. Jika Citizens tidak terpasang, seluruh fitur KingdomCore tetap berfungsi.
7. GUI region selection tetap menjadi tanggung jawab KingdomCore ketika `/region choose` dipanggil oleh player.

Citizens tidak menjadi source of truth untuk Region.

---

# 18. EssentialsX Integration

EssentialsX digunakan untuk utility server:

```text
/home
/sethome
/spawn
/tpa
/tpaccept
/msg
/r
/back
```

KingdomCore menangani command khusus:

```text
/lobby
/region
/region choose
/region info
```

KingdomCore tidak memodifikasi source code EssentialsX.

---

# 19. `/region`

Command:

```text
/region
```

Flow:

```text
Player
  │
  ▼
RegionManager
  │
  ▼
Player Region
  │
  ▼
Region Spawn
  │
  ▼
Teleport
```

Contoh:

```text
Region = NUSANTARA
       ↓
world_nusantara
       ↓
Nusantara spawn
```

---

# 20. `/region info`

Command:

```text
/region info
```

Contoh output:

```text
Region: Nusantara
Level: 17
Title: Guardian
```

Command ini juga digunakan untuk debugging dan player information.

---

# 21. `/lobby`

Command:

```text
/lobby
```

Lokasi disimpan dalam config:

```yaml
locations:

  lobby:
    world: "lobby"
    x: 0.5
    y: 100
    z: 0.5
    yaw: 0
    pitch: 0
```

Tidak boleh hard-code koordinat dalam Java.

---

# 22. Command Contract

KingdomCore owns these commands:

```text
/lobby
/region
/region choose
/region info
```

## Sender Requirements

| Command | Player | Console | NPC as Player |
|---|---:|---:|---:|
| `/lobby` | Yes | No | Yes |
| `/region` | Yes | No | Yes |
| `/region choose` | Yes | No | Yes |
| `/region info` | Yes | No | Yes |

An NPC that dispatches a command as console is not considered a player-context execution.

## Command Permissions

Command permission nodes must be declared explicitly in `plugin.yml` and documented in the implementation plan.

Example namespace:

```text
kingdomcore.command.lobby
kingdomcore.command.region
kingdomcore.command.region.choose
kingdomcore.command.region.info
kingdomcore.admin.*
```

The final permission matrix is implementation-specific and must not grant staff privileges merely because a player has a high progression rank.

---

# 22. Database Cache

KingdomCore menggunakan:

```text
PostgreSQL
     │
     ▼
Repository
     │
     ▼
Caffeine Cache
     │
     ▼
Minecraft Runtime
```

Data player yang sedang online disimpan di memory.

Chat tidak boleh melakukan query PostgreSQL setiap kali ada message.

---

# 23. Threading Rules

Database operation tidak boleh dilakukan pada main server thread.

Buruk:

```java
onPlayerJoin() {
    database.query();
}
```

Benar:

```text
Minecraft Main Thread
        │
        ├── Async DB Load
        │
        ▼
    PostgreSQL
        │
        ▼
Main Thread
        │
        ▼
PlayerData Cache
```

Semua operasi Bukkit/Paper yang membutuhkan main thread harus dikembalikan ke scheduler/main thread sesuai API Paper.

Database/network I/O wajib asynchronous. Jangan pernah melakukan blocking JDBC, HTTP, atau filesystem I/O pada main server thread.

Jangan mengakses object Bukkit/Paper yang thread-confined dari asynchronous task kecuali API tersebut secara eksplisit menyatakan aman. Pola umum:

```text
Main Thread
    │
    ├── capture immutable identifiers/data
    │
    ▼
Async Task
    │
    ├── PostgreSQL / HTTP / blocking I/O
    │
    ▼
Main Thread
    │
    └── apply Bukkit/Paper state changes
```

Untuk MVP, jangan menambahkan Folia-specific scheduler abstractions hanya untuk mengejar kompatibilitas Folia. Paper 26.2 adalah target platform.

---

# 24. Database Migration

Gunakan:

```text
Flyway
```

Contoh:

```text
src/main/resources/db/migration/

V1__create_players.sql
V2__create_regions.sql
V3__create_indexes.sql
```

Migration lama tidak boleh diedit setelah digunakan di production.

---

# 25. Internal KingdomCore API

Plugin lain tidak boleh mengakses database KingdomCore secara langsung.

KingdomCore menyediakan API:

```java
KingdomCoreAPI api = KingdomCore.getAPI();
```

Contoh:

```java
PlayerData data = api.getPlayerData(uuid);
```

```java
Region region = api.getRegion(uuid);
```

```java
String title = api.getLevelTitle(uuid);
```

```java
api.getLevelService().addXp(uuid, 500);
```

---

# 26. Custom Events

KingdomCore menyediakan event:

```text
KingdomXpGainEvent
KingdomLevelUpEvent
KingdomRegionChooseEvent
KingdomRegionChangeEvent
```

Contoh:

```text
Mini-game
    │
    ▼
addXp()
    │
    ▼
KingdomXpGainEvent
    │
    ▼
LevelManager
    │
    ▼
Level Up?
    │
    ▼
KingdomLevelUpEvent
```

Ini memungkinkan plugin mini-game, quest, achievement, dan sistem lain berintegrasi tanpa coupling langsung ke database.

---

# 27. Future external application Integration

Future external application tidak boleh langsung mengakses database dari frontend.

Architecture:

```text
Browser
   │
   ▼
Astro
   │ HTTPS
   ▼
Go Backend
   │
   ▼
PostgreSQL
```

Minecraft:

```text
KingdomCore
   │
   ▼
PostgreSQL
```

Untuk sinkronisasi real-time di masa depan:

```text
KingdomCore
     │
     ▼
Redis Pub/Sub
     │
     ▼
Go Backend
```

Redis belum diperlukan untuk MVP.

---

# 28. Future external application API

## Player

```http
GET /api/v1/players/{uuid}
```

Response:

```json
{
  "uuid": "...",
  "username": "Rifqi",
  "level": 17,
  "xp": 8230,
  "region": {
    "key": "NUSANTARA",
    "name": "Nusantara"
  },
  "title": "Guardian"
}
```

## Regions

```http
GET /api/v1/regions
```

## Future leaderboard integration

```http
GET /api/v1/leaderboards/level
```

## Admin XP

```http
POST /api/v1/admin/players/{uuid}/xp
```

Admin endpoints harus memiliki authentication dan authorization.

---

# 29. API Security

Wajib:

- HTTPS
- Authentication
- Authorization
- Rate limiting
- Input validation
- UUID validation
- Region validation
- Level validation
- Prepared statements
- Secret melalui environment variable
- Tidak expose PostgreSQL ke internet

Contoh environment:

```text
DATABASE_URL
DATABASE_USER
DATABASE_PASSWORD
KINGDOMCORE_API_KEY
JWT_SECRET
```

Secret tidak boleh disimpan di Git repository.

---

# 30. Package Architecture

```text
com.yourserver.kingdomcore
│
├── KingdomCorePlugin
│
├── command
│   ├── LobbyCommand
│   └── RegionCommand
│
├── player
│   ├── PlayerData
│   ├── PlayerDataService
│   └── PlayerListener
│
├── region
│   ├── Region
│   ├── RegionManager
│   ├── RegionRepository
│   └── RegionTeleportService
│
├── level
│   ├── LevelManager
│   ├── LevelTitleResolver
│   └── XpService
│
├── chat
│   └── ChatFormatter
│
├── nametag
│   └── NameTagService
│
├── integration
│   ├── LuckPermsHook
│   ├── VaultHook
│   ├── PlaceholderApiHook
│   └── EssentialsHook
│
├── database
│   ├── DatabaseManager
│   ├── PlayerRepository
│   └── RegionRepository
│
├── cache
│   └── PlayerCache
│
├── config
│   └── ConfigManager
│
└── api
    └── KingdomCoreAPI
```

---

# 31. Configuration Architecture

Semua content/balancing harus configurable.

Contoh:

```yaml
server:
  name: "Your Server"

level:
  min: 1
  max: 100

xp:
  formula: "configurable"

regions:
  NUSANTARA:
    display-name: "Nusantara"

  REGION_2:
    display-name: "Region II"

  REGION_3:
    display-name: "Region III"

level-titles:
  NUSANTARA:
    "1-10": "Citizen"
    "11-20": ""
    "21-30": ""
    "31-40": ""
    "41-50": ""
    "51-60": ""
    "61-70": ""
    "71-80": ""
    "81-90": ""
    "91-100": ""

locations:
  lobby:
    world: "lobby"
    x: 0.5
    y: 100
    z: 0.5
    yaw: 0
    pitch: 0

chat:
  format: "[Lv. {level} {title}][{rank}][{region}] {player} >> {message}"
```

Jangan hard-code content yang seharusnya bisa diubah administrator.

---

# 32. Logging

Contoh:

```text
[KingdomCore] Player data loaded: Rifqi
[KingdomCore] Region selected: NUSANTARA
[KingdomCore] Player level increased: 16 -> 17
```

Jangan pernah log:

```text
Database password
API key
JWT secret
Session token
```

---

# 33. Security Rules

KingdomCore harus:

- Menggunakan prepared statements.
- Tidak melakukan SQL concatenation.
- Memvalidasi semua command argument.
- Memvalidasi region key.
- Membatasi level 1–100.
- Menolak XP negatif.
- Tidak mempercayai data dari client.
- Tidak menyimpan secret dalam source.
- Tidak mengekspos credential pada log.
- Tidak query database pada main thread.
- Tidak bergantung pada internal NMS jika Paper API mencukupi.

---

# 34. MVP Scope

Versi pertama KingdomCore:

```text
[ ] Paper 26.2 compatibility
[ ] Java 25 toolchain
[ ] plugin.yml with api-version 26.2
[ ] Paper public API / Adventure-only implementation where possible
[ ] No NMS dependency unless explicitly justified
[ ] Gradle project
[ ] PostgreSQL
[ ] HikariCP
[ ] Flyway
[ ] Caffeine
[ ] PlayerData
[ ] Region entity
[ ] Region repository
[ ] Region selection
[ ] Region persistence
[ ] Level 1–100
[ ] XP
[ ] XP source registry
[ ] Mining XP
[ ] Fishing XP
[ ] Woodcutting XP
[ ] Mob kill XP
[ ] Player kill XP
[ ] Farming XP
[ ] Anvil XP
[ ] Enchanting XP
[ ] Cooking/smelting XP
[ ] Golden apple XP
[ ] Brewing XP
[ ] Potion use XP
[ ] Exploration/movement XP
[ ] XP anti-abuse protections
[ ] Level title
[ ] LuckPerms integration
[ ] Vault integration
[ ] PlaceholderAPI integration
[ ] Chat formatter
[ ] /region
[ ] /region choose
[ ] /region info
[ ] /lobby
[ ] Internal KingdomCore API
[ ] Custom events
[ ] Basic REST integration design
```

---

# 35. Post-MVP

Setelah MVP stabil:

```text
[ ] Future external application player profile
[ ] Future leaderboard integration
[ ] Admin dashboard
[ ] Player statistics
[ ] Region statistics
[ ] Redis Pub/Sub
[ ] Real-time website updates
[ ] Quest system
[ ] Achievement system
[ ] Kingdom economy
[ ] Kingdom war
[ ] Region-specific progression
[ ] Metrics
[ ] Prometheus
[ ] Grafana
```

---

# 36A. Paper 26.2 Compatibility Checklist

Before declaring the MVP implementation complete:

```text
[ ] Runs on Java 25+
[ ] Built with Gradle Kotlin DSL
[ ] Uses plugin.yml
[ ] api-version is 26.2
[ ] Compiles against Paper API 26.2
[ ] No accidental dependency on NMS/internal server classes
[ ] No new usage of deprecated legacy text/chat APIs where Adventure is available
[ ] Blocking JDBC/HTTP/file I/O is never executed on the main server thread
[ ] Bukkit/Paper state mutations occur on the appropriate server thread
[ ] Plugin starts when PlaceholderAPI is absent
[ ] Plugin starts when Citizens is absent
[ ] Citizens is not a required dependency
[ ] NPC command dispatch works when executed as the interacting player
[ ] Console cannot invoke player-context commands
[ ] TAB remains externally configurable
[ ] Folia is not claimed as supported
[ ] Reload/restart does not corrupt cached player data
```

# 35A. Development Roadmap

## Phase 1 — Server Foundation

```text
[ ] Paper 26.2 server running
[ ] Java 25 runtime/toolchain verified
[ ] Required plugins installed
[ ] Server worlds configured
[ ] Basic permissions configured
```

## Phase 2 — KingdomCore Core

```text
[ ] Plugin bootstrap
[ ] Configuration loading
[ ] PostgreSQL/persistence layer if enabled
[ ] Player data model
[ ] Region system
[ ] Region selection
[ ] Region persistence
```

## Phase 3 — Progression

```text
[ ] XP source registry
[ ] XP handlers
[ ] XP anti-abuse
[ ] Level 1–100
[ ] Level-up events
[ ] Region-based titles
[ ] Level data exposed to integrations
```

## Phase 4 — Server Integration

```text
[ ] LuckPerms integration
[ ] Rank resolution
[ ] Chat format
[ ] EssentialsX integration
[ ] Vault integration
[ ] PlaceholderAPI integration (optional)
[ ] Citizens/NPC command compatibility (optional)
[ ] /lobby
[ ] /region
```

## Phase 5 — Server Presentation / External Plugins

```text
[ ] TAB configured by server owner
[ ] Mini-game plugins configured by server owner
[ ] NPCs configured by server owner
[ ] Final server UX testing
[ ] XP balancing
[ ] Performance testing
[ ] Exploit/abuse testing
```

## Phase 6 — Future Web Platform

**Not part of the current MVP.**

Only after the Minecraft server and KingdomCore are stable:

```text
[ ] Define web requirements
[ ] Define public/private data boundary
[ ] Design API/integration layer
[ ] Build web backend
[ ] Build web frontend
[ ] Build web player profile
[ ] Build leaderboard/statistics
[ ] Build web admin tools
```

The web phase must not block or dictate the initial Minecraft plugin implementation.

---

# 36. Definition of Done

## Current MVP Boundary

KingdomCore MVP is considered complete when the **Minecraft server-side functionality** is operational and tested.

A web application is not required for MVP completion.



KingdomCore dianggap berhasil untuk MVP jika:

## First Join

```text
Player Join
    ↓
Level 1
    ↓
XP 0
    ↓
Region selection
    ↓
Region saved
    ↓
Rank = Wanderer
```

## Chat

```text
[Lv. 1 Citizen][Wanderer][Nusantara] Player >> Halo!
```

## Level Up

```text
XP Gain
    ↓
Level 10
    ↓
Level 11
    ↓
Title berubah sesuai Region
    ↓
Chat berubah
    ↓
Nametag berubah
```

## Region

```text
/region
    ↓
Read player region
    ↓
Find region spawn
    ↓
Teleport
```

## Lobby

```text
/lobby
    ↓
Lobby spawn
```

## Future external application

```text
Future external application
    ↓
GET /api/v1/players/{uuid}
    ↓
Go Backend
    ↓
PostgreSQL
    ↓
Player Data
```

---

# 37. Final Architecture

```text
                       ┌──────────────────┐
                       │  Astro Future external application   │
                       └────────┬─────────┘
                                │
                              HTTPS
                                │
                                ▼
                       ┌──────────────────┐
                       │    Go Backend    │
                       │     REST API     │
                       └────────┬─────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │   PostgreSQL     │
                       └────────▲─────────┘
                                │
                                │
┌───────────────────────────────┼───────────────────────────────┐
│                         MINECRAFT                             │
│                                                               │
│                         Paper 26.2                             │
│                              │                                │
│                      ┌───────▼────────┐                       │
│                      │  KingdomCore   │                       │
│                      │                │                       │
│                      │ Region         │                       │
│                      │ Level          │                       │
│                      │ XP             │                       │
│                      │ Titles         │                       │
│                      │ Chat           │                       │
│                      │ Commands       │                       │
│                      │ Player API     │                       │
│                      └───────┬────────┘                       │
│                              │                                │
│          ┌───────────────────┼────────────────────┐           │
│          │                   │                    │           │
│          ▼                   ▼                    ▼           │
│     PlaceholderAPI         Vault             LuckPerms       │
│          │                   │                    │           │
│          ▼                   ▼                    ▼           │
│         TAB             EssentialsX         Rank/Perms       │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

---

# 38. Core Design Principle

Pembagian tanggung jawab final:

```text
KingdomCore
├── Region
├── Level
├── XP
├── Level Title
├── Player Data
├── /region
├── /lobby
├── Chat
├── Internal API
└── Custom Events

LuckPerms
├── Rank
├── Prefix
├── Permissions
└── Staff Permissions

Vault
└── Compatibility Layer

EssentialsX
├── Homes
├── Spawn
├── TPA
├── Messaging
└── General Utility

PlaceholderAPI
└── Placeholder Integration

TAB
└── Tablist / Nametag presentation

PostgreSQL
└── Persistent Data

Go Backend
├── Future external application API
├── Authentication
├── Admin API
└── Future external application Business Logic

Astro
└── Future external application UI
```

**Kesimpulan:** KingdomCore menjadi **domain layer server**, bukan plugin yang mencoba menggantikan semua plugin lain. Region, progression, title, player state, command khusus, event, dan API adalah milik KingdomCore. Rank tetap milik LuckPerms, utility tetap milik EssentialsX, placeholder menjadi integration layer, dan website berkomunikasi melalui Go Backend + PostgreSQL.


---

# 39. External Technical Baseline References

This section records the external technical references used when updating this PRD for Paper 26.2.

- Paper 26.2 API Javadocs: https://jd.papermc.io/paper/26.2/
- Paper project setup: https://docs.papermc.io/paper/dev/project-setup/
- Paper `plugin.yml`: https://docs.papermc.io/paper/dev/plugin-yml/
- Paper plugins documentation: https://docs.papermc.io/paper/dev/getting-started/paper-plugins/

These references establish the implementation baseline for Java 25, the Paper 26.2 API target, the Gradle dependency format, and the `plugin.yml` manifest model.

The PRD remains the product/source-of-truth specification for KingdomCore behavior. External documentation is the source for platform/API constraints and may supersede implementation details that are explicitly version-dependent.


---

# 40. Current Non-Goals

The following must not be interpreted as current implementation requirements:

```text
- Building a website
- Building a web dashboard
- Building a public player profile site
- Building web authentication
- Building web leaderboard UI
- Building a public REST API solely for the future website
- Implementing web-specific frontend logic inside KingdomCore
- Requiring TAB, Citizens, or mini-game plugins as KingdomCore hard dependencies
```

The immediate objective is to make the **Minecraft server and KingdomCore plugin stable, modular, configurable, and testable first**.

Future web integration should be designed after real server behavior, progression data, and operational requirements are known.
