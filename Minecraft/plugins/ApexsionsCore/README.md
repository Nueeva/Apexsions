# ApexsionsCore — Minecraft 1.21.4 (Paper 26.2)

Plugin fondasi utama server **Apexsions** yang mengelola sistem 3 Kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*), progresi level pemain & 13 sumber XP, sistem navigasi Warp GUI & Admin Editor, Master Admin Hub (`/admingui`), Title Vault, Particle Cosmetics, perlindungan PvP teritorial kerajaan, Kingdom War, Combat Tagging (15s), dan integrasi BlueMap.

---

## 🌟 Fitur Utama

- **Sistem 3 Kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*)**: Manajemen batas wilayah, BlueMap polygon rendering, NPC spawn teleportasi, dan penentuan gelar kustom per level.
- **Master Admin Hub Terpusat (`/admingui`)**: Dashboard 54-slot untuk memantau server (RAM, TPS, database pool) dan pintasan modul administrasi 6 plugin suite secara terpadu.
- **Deep Player Inspector**: Inspeksi langsung level, saldo, kerajaan, status monarch, live inventory, enderchest, heal, feed, dan gamemode pemain via GUI.
- **Sistem Warp GUI & Admin Editor**:
  - `/warp` & `/warps`: GUI navigasi 54-slot dengan filter tab kategori (`SERVER`, `RESOURCE`, `EVENT`, `KINGDOM`, `PVP`, `GENERAL`).
  - `/warpmgr` / `/warp admin`: GUI manajemen admin interaktif untuk memperbarui koordinat, mengubah ikon dari tangan, kategori, delay timer, toggle hidden, dan hapus warp.
- **Perlindungan PvP Sesama Kerajaan**: Membatalkan 100% serangan (Melee, Panah/Trident, Splash Potion, Pet) antar sesama anggota kerajaan saat berada **di dalam wilayah kerajaan sendiri**.
- **PvP Combat Tagging (15 Detik)**: Mencegah combat logging dan membatalkan segala bentuk teleportasi saat sedang bertarung.
- **Kingdom War Manager (`/ac war`)**: Mode perang resmi antar-kerajaan dengan proteksi penguncian teleportasi di zona perang aktif.
- **Formula Progresi & 13 Sumber XP**: Sistem level non-linear dengan 13 sumber perolehan XP (Mining, Mob Kill, Woodcutting, Fishing, Farming, Crafting, Enchanting, Smelting, Player Kill, Golden Apple, Potion Use, Exploration, Structure Discovery).
- **Title Vault (`/titles`) & Particle Cosmetics (`/cosmetics`)**: GUI untuk melengkapi gelar kehormatan dan efek partikel kosmetik (Head Auras, Footstep Trails, Kill Effects).
- **Kingdom-Bounded `/rtp`**: Teleportasi acak aman yang mewajibkan pemain berada di teritori kerajaannya sendiri.
- **Enforcer TPA EssentialsX**: Membatasi `/tpa` hanya ke sesama anggota kerajaan dan wajib berada di dalam area wilayah teritorial kerajaan.
- **Custom Enchantment Engine (`/enchant`)**: Mendukung level enchant hingga 4x batas vanilla (Sharpness 20, Protection 12, Mending 4) secara aman via command tanpa merusak anvil/meja sihir survival biasa.
- **Anvil Enhancement Engine**: Menghilangkan batasan "Too Expensive!" (level 40) pada anvil, mendukung biaya level tanpa batas (`cost-cap: 0`), dan menjaga custom enchant dari reset/downgrade.

---

## 🛠️ Kompilasi & Build

```powershell
# Kompilasi khusus ApexsionsCore via Turbo Multi-Compiler:
powershell -ExecutionPolicy Bypass -File .\build.ps1 Core
```

Output JAR siap pakai:
- `build/libs/ApexsionsCore-1.0.0.jar`
- `plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`
