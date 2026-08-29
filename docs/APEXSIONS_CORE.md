# ApexsionsCore — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsCore`** (Otoritas Wilayah Kerajaan, Sistem Warp GUI & Admin GUI, Perlindungan PvP Teritorial, Progresi Karakter, XP Engine, Navigasi BlueMap, Kingdom War, Combat Tag, dan Enforcer TPA EssentialsX).

---

## 🏛️ 1. Ikhtisar Modul & Arsitektur

`ApexsionsCore` adalah modul pondasi sentral yang mengatur identitas pemain, pembagian 3 kerajaan besar, sistem progresi berbasis level dan XP, sistem navigasi Warp modern (GUI Player & Admin), rendering wilayah pada web-map (BlueMap), manajemen deklarasi perang kerajaan (*Kingdom War*), PvP combat tagging anti-combat log, serta pengamanan teritorial dari eksploitasi teleportasi dan perkelahian internal kerajaan.

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
- **Spawn & Warp Kerajaan**: Titik pusat kerajaan (`/kingdom spawn`) dengan sambutan selamat datang berbasis MiniMessage.
- **Citizens NPC Integration**: NPC interaktif untuk pemilihan kerajaan dan navigasi kerajaan.
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
  - `✖ Hapus Warp`: Menghapus warp secara permanen dari database SQLite.

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
| `/lobby` | `/hub` | Teleportasi ke lobi pusat server (Mendukung Multiverse) | `apexsionscore.command.lobby` | `true` |
| `/admingui` | `/apexadmin`, `/aadmin`, `/aa` | **Master Admin Hub Terpusat** (Dashboard 54-slot 6 Modul Suite) | `apexsions.admin.gui` | `op` |
| `/warp [nama]` | `/warps` | Membuka GUI navigasi warp atau teleport ke warp tertentu | `apexsionscore.command.warp` | `true` |
| `/warpmgr` | `/warpadmin`, `/warp admin` | Membuka Interactive Admin Warp Management GUI | `apexsionscore.warp.admin` | `op` |
| `/warp set <nama> [kategori]` | - | Membuat warp baru di lokasi berdiri | `apexsionscore.warp.admin` | `op` |
| `/warp delete <nama>` | `/warp del` | Menghapus warp yang ada | `apexsionscore.warp.admin` | `op` |
| `/kingdom` | `/k`, `/region` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka GUI visual pemilihan kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard` | Membuka GUI Hall of Fame & Leaderboard | `apexsionscore.command.level` | `true` |
| `/level` | `/lvl`, `/profile` | Membuka GUI progress bar level dan reward | `apexsionscore.command.level` | `true` |
| `/xpguide` | `/exp` | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/rtp` | `/wild`, `/krtp` | Teleportasi acak di teritori kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/ac setlobby` | `/kc setlobby` | Mengatur titik spawn lobi saat ini (Multiverse-ready) | `apexsionscore.admin` | `op` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang kerajaan resmi | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan perang kerajaan | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status aktif perang kerajaan | `apexsionscore.admin` | `op` |
| `/ac addxp <p> <amt>` | `/kc addxp` | Menambahkan poin XP ke pemain | `apexsionscore.admin` | `op` |
| `/ac setlevel <p> <lvl>`| `/kc setlevel` | Mengubah level pemain secara langsung | `apexsionscore.admin` | `op` |
| `/ac setkingdom <p> <k>`| `/kc setkingdom`| Memindahkan kerajaan pemain secara paksa | `apexsionscore.admin` | `op` |

---

## 🎛️ 8. Master Admin Hub Terpusat (`/admingui`)

- **Dashboard 54-Slot Sentral (`MasterAdminGUI`)**: Menyatukan seluruh modul administrasi plugin dalam satu tampilan intuitif.
- **Header Status Server Real-time**: Memantau RAM server yang sedang terpakai, TPS, jumlah pemain online, pool koneksi database, serta status perang kerajaan.
- **6 Kartu Modul Suite**:
  1. **👑 Core Management**: Sub-menu Warp Manager, Kingdom War Control, dan Lobby Spawn Setup.
  2. **💬 Chat & Reports**: Kotak masuk resolusi tiket laporan pemain staff (`ReportListGUI`).
  3. **💰 Economy & Auction**: Kontrol saldo dan pasar lelang pemain.
  4. **🎫 BattlePass Editor**: Panel visual editor reward, misi, dan seasonal settings (`/abp`).
  5. **🛒 Dynamic Shop**: Pasar dinamis, harga komoditas cuaca, dan monitoring inflasi.
  6. **🖼️ Media & Banners**: Daftar banner aktif, teleportasi ke banner, dan replikasi template (`MediaAdminGUI`).
- **Granular Permission & Visual Lock Indicator**: Kartu modul yang tidak diizinkan untuk staf junior akan tetap tampil tetapi terkunci dengan gembok merah (`🔒 TERKUNCI`), mencegah akses tanpa izin.
- **Universal Breadcrumb Navigation**: Tombol `⬅ KEMBALI KE ADMIN HUB` (Slot 45) tertanam di seluruh sub-menu admin untuk navigasi bolak-balik tanpa harus mengetik perintah ulang.
- **Tombol Reload Suite Serentak**: Memuat ulang konfigurasi seluruh plugin suite dengan 1 klik tombol Redstone Block.
