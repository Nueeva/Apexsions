# 👑 ApexsionsCore — Minecraft Paper 26.2 Core Plugin

**ApexsionsCore** adalah plugin inti untuk server Minecraft **Apexsions** (Paper 26.2 / Java 21+) yang mengatur sistem **Kerajaan / Realms (Zenithar, Solterra, Sylvamoor)**, **Progression Level 1–100**, **Sistem Klaim Hadiah Level Deterministik & Hadiah Milestone (11, 21, 31, 41, 51, 61, 71, 81, 91, 100)**, **Penyediaan Rank LuckPerms Otomatis & Idempoten (9 Ranks)**, **Interactive Chest GUIs**, **13 Kategori XP Gameplay dengan Anti-Abuse**, **Dynamic Realm Level Titles**, **Integrasi BlueMap**, **Integrasi Citizens2 NPC**, dan **PlaceholderAPI**.

---

## 🌟 Fitur Utama

- 🏰 **Sistem Kerajaan / Realms (Apexsions)**:
  - Tiga kerajaan resmi dengan warna, blok tema, dan batas BlueMap tersinkronisasi:
    - 🟡 **Zenithar** (`ZENITHAR`): Warna Kuning/Emas (`GOLD_BLOCK`), Spawn di `world (-3028, 64, -5597)`.
    - 🔴 **Solterra** (`SOLTERRA`): Warna Merah/Crimson (`REDSTONE_BLOCK`), Spawn di `world (-5843, 65, 889)`.
    - 🔵 **Sylvamoor** (`SYLVAMOOR`): Warna Biru/Azure (`DIAMOND_BLOCK`), Spawn di `world (-9666, 64, -4812)`.
  - Interactive Selection GUI (`/region choose` / `/kingdom choose`) 45-slot.
  - Interactive Profile & Stats GUI (`/level` / `/kingdom info`) 45-slot dengan player head, XP progress bar, realm card, tombol klaim reward, dan warp instan.
  - Command `/lobby` untuk kembali ke lobby utama server.

- 🛡️ **Penyediaan Rank LuckPerms Otomatis & Idempoten (9 Ranks)**:
  - Otomatis mendaftarkan dan memelihara 9 grup rank server pada saat startup:
    1. **The Ancestor** (`ancestor`): Rank Owner Khusus (Dark Red, Bold, diproteksi berdasarkan `owner.uuid` di `ranks.yml`).
    2. **Warden** (`warden`): Admin (Dark Blue).
    3. **Herald** (`herald`): Helper (Pink).
    4. **Sions** (`sions`): Rank Tertinggi Normal (Gradien Aqua → Gold).
    5. **Emperor** (`emperor`): Bright Red.
    6. **Sovereign** (`sovereign`): Gold.
    7. **Archon** (`archon`): Cyan / Aqua.
    8. **Ascendant** (`ascendant`): Light Green.
    9. **Wanderer** (`wanderer`): Gray (Default rank untuk pemain baru).
  - **Idempoten**: Dijalankan 1x, 10x, atau 100x tidak akan membuat duplikasi grup (`wanderer2`, dll) dan tidak menghapus permission custom admin.

- 🎁 **Sistem Klaim Hadiah Level Deterministik (1–100)**:
  - Dibagi menjadi 10 halaman terstruktur rapi di GUI `/level` / `/rewards`.
  - **Fixed Milestone Slot (Slot 31)**: Hadiah istimewa milestone (angka 1 tiap 10 level: 11, 21, 31, ..., 91 & Lv. 100) selalu berada di **Slot 31 pada SETIAP halaman**, tidak akan bergeser karena pagination.
  - Dukungan tombol **"1-Click Claim All" (Slot 6)** untuk mengumpulkan seluruh hadiah tertunda.

- 📁 **Struktur Konfigurasi Modular (9 File YAML)**:
  - `config.yml`: Database, server name, lobby, BlueMap, level formula.
  - `ranks.yml`: Definisi 9 rank LuckPerms dan konfigurasi UUID owner.
  - `kingdoms.yml`: Definisi 3 realm (Zenithar, Solterra, Sylvamoor), deskripsi, spawn, warna.
  - `titles.yml`: Tangga gelar level (Level 1–100) untuk tiap realm.
  - `xp.yml`: Pengaturan 13 sumber XP gameplay dan parameter anti-abuse.
  - `rewards.yml`: Konfigurasi detail hadiah level 1–100 dan hadiah istimewa milestone.
  - `chat.yml`: Format chat Adventure MiniMessage dan integrasi rank.
  - `messages.yml`: Semua teks pesan, mini-message, notifikasi, dan audio sound.
  - `gui.yml`: Konfigurasi judul dan ukuran inventori GUI.

- ⚔️ **13 Kategori XP Gameplay dengan Nilai Spesifik Tiap Item & Mob (`xp.yml`)**:
  - Seluruh 43+ jenis monster/mob terdaftar dengan spawn egg dan slot individu.
  - Mining, Woodcutting, Fishing, Farming, Anvil, Enchanting, Cooking, Golden Apple, Brewing, Potion Use, Exploration, Mob Combat, PvP.

---

## 📌 Commands & Permissions

| Command | Aliases | Permission | Deskripsi |
|---|---|---|---|
| `/lobby` | - | `apexsionscore.command.lobby` | Teleport langsung ke lobby utama |
| `/region` | `/kingdom`, `/k`, `/kingdoms` | `apexsionscore.command.region` | Teleport ke realm/kerajaan asal atau `/region choose` |
| `/level` | `/lvl`, `/profile`, `/rewards`, `/exp` | `apexsionscore.command.level` | Buka menu profil, hadiah level, atau direktori XP |
| `/apexsionscore` | `/ac`, `/apexionscore`, `/kingdomcore`, `/kc` | `apexsionscore.admin` | Manajemen admin (reload, setlevel, addxp, setkingdom) |

---

## 🛠️ Kompilasi & Build

```powershell
$env:JAVA_HOME = "c:\Users\Friel\Documents\Rifqi Ariansyah\Apexsions\plugins\KingdomCore\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\apache-maven-3.9.9\bin\mvn.cmd clean package
```
Output fat jar final: `target/ApexsionsCore-1.0.0.jar` (dan `./ApexsionsCore-1.0.0.jar`).
