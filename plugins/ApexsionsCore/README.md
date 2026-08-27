# ApexsionsCore — Minecraft 1.21.4 (Paper 26.2)

Plugin fondasi utama server **Apexsions** yang mengelola sistem 3 Kerajaan (*Zenithar*, *Solterra*, *Sylvamoor*), progresi level pemain, integrasi BlueMap Polygon, dan pembatasan TPA EssentialsX.

---

## 🌟 Fitur Utama
- **Sistem 3 Kerajaan (*Kingdoms*)**: Manajemen wilayah, BlueMap boundary renderer, NPC spawn teleportasi, dan penentuan gelar kustom per level.
- **Formula Progresi & 13 Sumber XP**: Sistem level non-linear dengan 13 sumber perolehan XP (Mining, Mob Kill, Woodcutting, Fishing, Farming, Crafting, Enchanting, Smelting, Player Kill, Golden Apple, Potion Use, Exploration, Structure Discovery).
- **GUI Interaktif Modern**: Menu profil kerajaan (`/kingdom`), panduan perolehan XP itemized (`/xpguide`), dan klaim hadiah level (`/level`).
- **Enforcer TPA EssentialsX**: Membatasi `/tpa` hanya ke sesama anggota kerajaan dan wajib berada di dalam area wilayah teritorial kerajaan.

---

## 🛠️ Kompilasi & Build
```powershell
mvn clean package
```
Output: `target/ApexsionsCore-1.0.0.jar`
