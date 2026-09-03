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
| `/ac rewards` | `/kingdom admin rewards` | Membuka Interactive Level Reward Editor (Drag & Drop Items) | `apexsionscore.admin` | `op` |

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

### 3. Formula Kenaikan EXP (Multiplier 1,1x Per Level)
- Menggunakan formula geometrik terkalibrasi di `config.yml`:
  $$\text{RequiredXP}(level) = \text{round}\big(\text{base} \times \text{multiplier}^{(level - 1)}\big)$$
  - Default: $\text{base} = 100$, $\text{multiplier} = 1.1$.
  - Lv 1 $\rightarrow$ 100 XP, Lv 2 $\rightarrow$ 110 XP, Lv 3 $\rightarrow$ 121 XP, Lv 4 $\rightarrow$ 133 XP, dst.


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
