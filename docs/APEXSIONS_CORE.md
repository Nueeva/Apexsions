# ApexsionsCore — Comprehensive Technical Manual

Panduan teknis dan operasional lengkap untuk modul **`ApexsionsCore`** (Otoritas Wilayah Kerajaan, Progresi Karakter, XP Engine, Navigasi BlueMap, dan Enforcer TPA EssentialsX).

---

## 🏛️ 1. Ikhtisar Modul & Arsitektur

`ApexsionsCore` adalah modul pondasi sentral yang mengatur identitas pemain, pembagian 3 kerajaan besar, sistem progresi berbasis level dan XP, rendering wilayah pada web-map (BlueMap), serta pengamanan teritorial dari eksploitasi teleportasi.

```
                               ┌────────────────────────┐
                               │     ApexsionsCore      │
                               │(Kingdom, Level, Ranks) │
                               └───────────┬────────────┘
                                           │
             ┌─────────────────────────────┼─────────────────────────────┐
             ▼                             ▼                             ▼
   ┌───────────────────┐         ┌───────────────────┐         ┌───────────────────┐
   │  3 Kingdom Realms │         │ 13 XP Engine Core │         │  Enforcers & RTP  │
   │Zenithar / Solterra│         │Leveling, Titles,  │         │Kingdom-Bounded RTP│
   │    Sylvamoor      │         │   Rewards GUI     │         │EssentialsX Enforce│
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

---

## 📈 3. Formula Leveling & 13 Sumber XP

Sistem progresi karakter menggunakan formula non-linear dinamis dengan 13 sumber perolehan XP yang terpisah secara itemized:

$$\text{XP Dibutuhkan}(L) = \lfloor 100 \times L^{1.5} + (L \times 50) \rfloor$$

### 13 Kategori Perolehan XP:
1. **Mining**: Menambang ore, batu mulia, dan material bawah tanah.
2. **Mob Kill**: Mengeliminasi monster berbahaya dan bos dunia.
3. **Woodcutting**: Menebang berbagai jenis pohon dan kayu langka.
4. **Fishing**: Memancing ikan, sampah laut, dan harta karun mistis.
5. **Farming**: Memanen gandum, wortel, kentang, dan tanaman perkebunan.
6. **Crafting**: Membuat peralatan, senjata, dan perkakas tingkat tinggi.
7. **Enchanting**: Memberikan sihir pada perlengkapan di Enchanting Table / Anvil.
8. **Smelting**: Memasak bijih tambang dan makanan di furnace / blast furnace.
9. **Player Kill**: Memenangkan pertempuran PvP di area perang kerajaan.
10. **Golden Apple**: Mengonsumsi Golden Apple atau Enchanted Golden Apple.
11. **Potion Use**: Meminum atau melempar ramuan sihir (*Splash/Lingering Potion*).
12. **Exploration**: Menjelajahi chunk baru di wilayah kerajaan.
13. **Structure Discovery**: Menemukan struktur dunia kuno (*Fortress, Trial Chamber, End City*).

---

## 🛡️ 4. Pengamanan Teritorial & Teleportasi

### A. Kingdom-Bounded `/rtp` (Random Teleport Aman)
- Perintah `/rtp` atau `/wild` menghitung koordinat acak **khusus di dalam batas poligon wilayah kerajaan pemain**.
- Mencegah pemain terdampar di wilayah kerajaan musuh atau di lautan lepas tanpa arah.

### B. Enforcer TPA EssentialsX (`TpaRestrictionListener`)
- Mencegat perintah `/tpa`, `/tpahere`, `/tpask`, `/tpaccept`, dan `/tpyes`.
- **Validasi 1 (Sesama Kerajaan)**: Kedua pemain wajib terdaftar dalam kerajaan yang sama.
- **Validasi 2 (Di Dalam Teritori)**: Kedua pemain **wajib berada di dalam koordinat fisik wilayah kerajaan mereka**. Jika salah satu berada di luar wilayah teritorial (misal di wilderness luar atau kerajaan lawan), teleportasi langsung dibatalkan.

---

## 📜 5. Matriks Perintah & Permissions

| Perintah | Alias | Deskripsi | Permission | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/lobby` | `/hub` | Teleportasi ke lobi pusat server | `apexsionscore.command.lobby` | `true` |
| `/kingdom` | `/k`, `/region` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka GUI visual pemilihan kerajaan | `apexsionscore.command.region` | `true` |
| `/level` | `/lvl`, `/profile` | Membuka GUI progress bar level dan reward | `apexsionscore.command.level` | `true` |
| `/xpguide` | `/exp` | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/rtp` | `/wild`, `/krtp` | Teleportasi acak di teritori kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/kadmin addxp <p> <amt>` | `/ac addxp` | Menambahkan poin XP ke pemain | `apexsionscore.admin` | `op` |
| `/kadmin setlevel <p> <lvl>`| `/ac setlevel` | Mengubah level pemain secara langsung | `apexsionscore.admin` | `op` |
| `/kadmin setkingdom <p> <k>`| `/ac setk` | Memindahkan kerajaan pemain secara paksa | `apexsionscore.admin` | `op` |
| `/kadmin reload` | `/ac reload` | Memuat ulang konfigurasi YAML & cache | `apexsionscore.admin` | `op` |

---

## 🗄️ 6. Skema Basis Data (Database DDL)

Mendukung SQLite dan PostgreSQL secara otomatis via HikariCP:

```sql
CREATE TABLE IF NOT EXISTS players (
    uuid VARCHAR(36) PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    kingdom VARCHAR(32) NOT NULL DEFAULT 'NONE',
    level INT NOT NULL DEFAULT 1,
    xp BIGINT NOT NULL DEFAULT 0,
    claimed_rewards TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_players_kingdom ON players(kingdom);
CREATE INDEX IF NOT EXISTS idx_players_level ON players(level DESC);
```
