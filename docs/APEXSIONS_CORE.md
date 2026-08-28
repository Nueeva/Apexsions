# ApexsionsCore — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsCore`** (Otoritas Wilayah Kerajaan, Progresi Karakter, XP Engine, Navigasi BlueMap, Kingdom War, Combat Tag, dan Enforcer TPA EssentialsX).

---

## 🏛️ 1. Ikhtisar Modul & Arsitektur

`ApexsionsCore` adalah modul pondasi sentral yang mengatur identitas pemain, pembagian 3 kerajaan besar, sistem progresi berbasis level dan XP, rendering wilayah pada web-map (BlueMap), manajemen deklarasi perang kerajaan (*Kingdom War*), PvP combat tagging anti-combat log, serta pengamanan teritorial dari eksploitasi teleportasi.

```
                               ┌────────────────────────┐
                               │     ApexsionsCore      │
                               │(Kingdom, Level, Ranks) │
                               └───────────┬────────────┘
                                           │
             ┌─────────────────────────────┼─────────────────────────────┐
             ▼                             ▼                             ▼
   ┌───────────────────┐         ┌───────────────────┐         ┌───────────────────┐
   │  3 Kingdom Realms │         │ 13 XP Engine Core │         │  Enforcers & War  │
   │Zenithar / Solterra│         │Leveling, Titles,  │         │Kingdom-Bounded RTP│
   │    Sylvamoor      │         │   Rewards GUI     │         │War & Combat Lock  │
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

## ⚔️ 3. Sistem Kingdom War & PvP Combat Tag

### A. Manajemen Perang Kerajaan (`WarManager`)
- Admin dapat mendeklarasikan perang antar-kerajaan dengan durasi khusus: `/ac war start <Kingdom1> <Kingdom2> [durasi_menit]`.
- Selama masa perang aktif:
  - Seluruh siaran publik menampilkan banner permusuhan visual MiniMessage.
  - **Seluruh fitur teleportasi (`/rtp`, `/tpa`, `/spawn`, `/lobby`) dinonaktifkan** di wilayah kerajaan yang sedang berperang untuk mencegah pelarian instan.

### B. PvP Combat Tagging (`CombatTagService`)
- Ketika pemain menyerang atau menerima damage dari pemain lain:
  - Pemain otomatis masuk ke mode **Combat Tag** selama **15 detik**.
  - Teleportasi dibatalkan secara instan jika pemain mencoba melakukan `/tpa`, `/rtp`, `/spawn`, `/lobby`, `/home`.
  - Jika pemain sengaja keluar (*combat log*), karakter langsung dieliminasi secara otomatis dan disiarkan ke seluruh server.

---

## 📈 4. Formula Leveling & 13 Sumber XP

$$\text{XP Dibutuhkan}(L) = \lfloor 100 \times L^{1.5} + (L \times 50) \rfloor$$

### 13 Kategori Perolehan XP:
1. **Mining**, 2. **Mob Kill**, 3. **Woodcutting**, 4. **Fishing**, 5. **Farming**, 6. **Crafting**, 7. **Enchanting**, 8. **Smelting**, 9. **Player Kill**, 10. **Golden Apple**, 11. **Potion Use**, 12. **Exploration**, 13. **Structure Discovery**.

---

## 🛡️ 5. Pengamanan Teritorial & Teleportasi

### A. Kingdom-Bounded `/rtp` (Strict In-Kingdom Verification)
- Pemain **wajib berada secara fisik di dalam wilayah kerajaannya sendiri** saat mengetik `/rtp`.
- Titik pendaratan acak dihitung asinkron pada Paper heightmap dan dipastikan aman dari lava/air/jurang.

### B. Enforcer TPA EssentialsX (`TpaRestrictionListener`)
- Mencegat perintah `/tpa`, `/tpahere`, `/tpask`, `/tpaccept`, `/tpyes`.
- Validasi sesama anggota kerajaan dan kedua pemain wajib berada di dalam wilayah teritori kerajaan mereka.
- Memblokir TPA jika salah satu pemain sedang terkena Combat Tag atau berada di zona perang aktif.

---

## 📜 6. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/lobby` | `/hub` | Teleportasi ke lobi pusat server | `apexsionscore.command.lobby` | `true` |
| `/kingdom` | `/k`, `/region` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka GUI visual pemilihan kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard` | Membuka GUI Hall of Fame & Leaderboard | `apexsionscore.command.level` | `true` |
| `/level` | `/lvl`, `/profile` | Membuka GUI progress bar level dan reward | `apexsionscore.command.level` | `true` |
| `/xpguide` | `/exp` | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/rtp` | `/wild`, `/krtp` | Teleportasi acak di teritori kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang kerajaan resmi | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan perang kerajaan | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status aktif perang kerajaan | `apexsionscore.admin` | `op` |
| `/ac addxp <p> <amt>` | `/kc addxp` | Menambahkan poin XP ke pemain | `apexsionscore.admin` | `op` |
| `/ac setlevel <p> <lvl>`| `/kc setlevel` | Mengubah level pemain secara langsung | `apexsionscore.admin` | `op` |
| `/ac setkingdom <p> <k>`| `/kc setkingdom`| Memindahkan kerajaan pemain secara paksa | `apexsionscore.admin` | `op` |
| `/ac reload` | `/kc reload` | Memuat ulang konfigurasi YAML & cache | `apexsionscore.admin` | `op` |
