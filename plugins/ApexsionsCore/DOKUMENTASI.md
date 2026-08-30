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
├── gui.yml               <-- Tata letak visual GUI (Profile, Top, Rewards, Warp, Admin Hub)
└── messages.yml          <-- Kumpulan pesan feedback visual MiniMessage
```

---

## ⚡ Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/lobby` | `/hub` | Teleportasi ke lobi pusat server | `apexsionscore.command.lobby` | `true` |
| `/kingdom` | `/region`, `/k`, `/kingdoms` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.region` | `true` |
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

---

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
