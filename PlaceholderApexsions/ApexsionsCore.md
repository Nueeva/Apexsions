# 👑 PlaceholderAPI — ApexsionsCore

> **Plugin:** `ApexsionsCore`  
> **Author:** Antigravity / Apexsions Team  
> **Identifier PAPI:**  
> - `%apexsions_<placeholder>%`  
> - `%apexsionscore_<placeholder>%` *(Dukungan alias ganda)*  

Plugin **ApexsionsCore** merupakan fondasi utama peradaban server Apexsions. Seluruh sistem esensial—mulai dari Leveling, XP, Wilayah Kerajaan (*Kingdom*), Pangkat (*Rank LuckPerms*), Badge Beranimasi, Gelar Adat (*Custom Title*), Deteksi Teritori, Pertarungan (*Combat Tag*), Perang Kerajaan (*Kingdom War*), Statistik Online Kerajaan, hingga Kosmetik—tersedia secara lengkap melalui PlaceholderAPI.

---

## 📊 Daftar Placeholder Lengkap

### 1. Level & Progres Pengalaman (XP)

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsions_level%` | - | Angka (Integer) | `15` | Level peradaban pemain saat ini. |
| `%apexsions_xp%` | - | Angka (Long) | `4250` | Total XP yang dimiliki pemain pada level saat ini. |
| `%apexsions_req_xp%` | `%apexsions_xp_needed%`<br>`%apexsions_next_xp%` | Angka / Teks | `10000` atau `MAX` | Jumlah XP yang diperlukan untuk mencapai level berikutnya. Menampilkan `MAX` jika sudah level tertinggi. |
| `%apexsions_xp_progressbar%` | `%apexsions_progressbar%` | Visual Bar | `&e████&8░░░░░░` | Batang kemajuan (progress bar 10 segmen). Kuning (`&e`) untuk bagian terisi dan abu-abu (`&8`) untuk sisa. |
| `%apexsions_level_title%` | - | String | `Kesatria Pemula` | Gelar level resmi yang didapatkan sesuai pencapaian level pemain. |
| `%apexsions_level_badge%` | - | MiniMessage | `<gradient:#f1c40f:#e67e22><bold>[Lv.15]</bold></gradient>` | Badge level bergaya gradien emas-oranye untuk chat, TAB, atau title. |

---

### 2. Wilayah Kerajaan (Kingdom & Territory)

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsions_region%` | `%apexsions_kingdom%` | ID / Key (String) | `ZENITHAR`, `SOLTERRA`, `SYLVAMOOR`, `NONE` | ID internal kerajaan yang dipilih pemain. Menampilkan `NONE` jika belum memilih. |
| `%apexsions_region_name%` | `%apexsions_kingdom_name%`<br>`%apexsions_kingdom_formatted%` | String | `Zenithar`, `Solterra`, `Sylvamoor`, `Belum Memilih` | Nama tampilan resmi kerajaan pemain. |
| `%apexsions_kingdom_badge%` | - | MiniMessage | `<gradient:#ffd700:#ffa502><bold>[ZENITHAR]</bold></gradient>` | Badge kerajaan bergaya warna gradien khas masing-masing kerajaan. |
| `%apexsions_current_territory%` | - | ID / Key | `ZENITHAR` atau `WILDERNESS` | ID wilayah tempat pemain sedang berdiri saat ini. Menampilkan `WILDERNESS` di alam liar. |
| `%apexsions_current_territory_name%` | - | String | `Zenithar` atau `Wilderness` | Nama tampilan wilayah tempat pemain berdiri saat ini. |
| `%apexsions_in_own_territory%` | - | Boolean | `true` atau `false` | Memeriksa apakah pemain sedang berada di dalam wilayah kerajaannya sendiri. |

---

### 3. Pangkat & Animasi Rank (LuckPerms & Rank Animation)

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsions_rank%` | - | String | `ancestor`, `architect`, `warden`, `wanderer` | Nama grup/pangkat LuckPerms murni pemain. |
| `%apexsions_rank_name%` | - | String | `Ancestor`, `Architect`, `Wanderer` | Display name pangkat pemain dari LuckPerms. |
| `%apexsions_rank_display%` | `%apexsions_rank_formatted%` | Legacy Color | `§c§lANCESTOR`, `§7Wanderer` | Tampilan pangkat pemain yang telah diserialisasi ke format warna Minecraft standar. |
| `%apexsions_rank_color%` | - | Hex Color | `#8B0000`, `#00FFFF`, `#808080` | Kode warna Hex resmi dari pangkat pemain. |
| `%apexsions_rank_animated%` | `%apexsions_animated_rank%`<br>`%apexsions_prefix%` | String / MiniMessage | *Animasi bergerak/gradien* | Prefix rank beranimasi dinamis sesuai konfigurasi modul animasi Apexsions. |
| `%apexsions_rank_badge%` | `%apexsions_badge%` | MiniMessage Badge | `<gradient:#8B0000:#FF0000><bold>👑 ANCESTOR</bold></gradient>` | Badge pangkat mewah siap pakai dengan ikon mahkota/pedang/perisai sesuai hierarki pangkat. |

#### Daftar Tampilan `%apexsions_rank_badge%` Berdasarkan Pangkat:
- **Ancestor**: `👑 ANCESTOR` (Gradien Merah Gelap - Merah Terang)
- **Architect**: `📐 ARCHITECT` (Gradien Ungu Royal)
- **Overseer**: `👁 OVERSEER` (Gradien Emas - Oranye)
- **Warden**: `🛡 WARDEN` (Gradien Biru Navy)
- **Herald**: `📜 HERALD` (Gradien Pink - Coral)
- **Sions**: `✦ SIONS ✦` (Gradien Cyan - Emas)
- **Emperor**: `⚔ EMPEROR` (Gradien Merah Crimson)
- **Sovereign**: `⚜ SOVEREIGN` (Gradien Amber - Kuning Emas)
- **Archon**: `💎 ARCHON` (Gradien Cyan - Biru Cerah)
- **Ascendant**: `☘ ASCENDANT` (Gradien Teal - Hijau Zamrud)
- **Wanderer**: `Wanderer` (Warna Abu-abu Perak)

---

### 4. Gelar Adat & Kustom (Custom Title)

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsions_active_title%` | `%apexsions_custom_title%` | String | `👑 Raja Zenithar` atau `Pemberani` | Gelar aktif pemain. Otomatis menampilkan `👑 Raja <Kerajaan>` apabila pemain terdaftar sebagai Raja Kerajaan di konfigurasi. |
| `%apexsions_title_display%` | `%apexsions_active_title_display%` | String | `👑 Raja Zenithar` atau `&7-` | Sama seperti di atas, namun mengembalikan tanda strip (`&7-`) jika tidak memiliki gelar. |
| `%apexsions_title_suffix%` | `%apexsions_active_title_suffix%` | String | ` &8[&fPemberani&8]` atau *(kosong)* | Format akhiran (suffix) gelar siap tempel pada format chat atau tablist. |

---

### 5. Integrasi Saldo Ekonomi Cepat (Cross-Hook ke ApexsionsEconomy)

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsions_balance_rupiah%` | `%apexsions_economy_balance_rupiah%` | Angka (Double) | `50000` | Saldo Rupiah mentah pemain. |
| `%apexsions_balance_rupiah_formatted%` | `%apexsions_economy_balance_rupiah_formatted%` | String Terformat | `Rp. 50,000` | Saldo Rupiah pemain dengan pemisah ribuan dan simbol `Rp.`. |
| `%apexsions_balance_diamond%` | `%apexsions_economy_balance_diamond%` | Angka (Double) | `125` | Saldo Diamond mentah pemain. |
| `%apexsions_balance_diamond_formatted%` | `%apexsions_economy_balance_diamond_formatted%` | String Terformat | `125 💎` | Saldo Diamond pemain dengan simbol permata `💎`. |

---

### 6. Pertarungan & Perang Kerajaan (Combat Tag & War)

| Placeholder | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- |
| `%apexsions_combat_tagged%` | Boolean | `true` atau `false` | Status apakah pemain sedang berada dalam status pertarungan (*in-combat*). |
| `%apexsions_combat_timer%` | Angka (Detik) | `15` | Sisa waktu (detik) status pertarungan sebelum pemain aman untuk logout/teleport. |
| `%apexsions_war_status%` | Teks Status | `WAR` atau `PEACE` | Status perang antar-kerajaan yang sedang berlangsung. |
| `%apexsions_war_timer%` | Angka (Detik) | `1800` | Sisa durasi perang kerajaan saat ini dalam detik. |

---

### 7. Statistik Pemain & Kerajaan Online

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsions_online_zenithar%` | - | Angka | `12` | Jumlah anggota Kerajaan Zenithar yang sedang online di server. |
| `%apexsions_online_solterra%` | - | Angka | `9` | Jumlah anggota Kerajaan Solterra yang sedang online di server. |
| `%apexsions_online_sylvamoor%` | - | Angka | `15` | Jumlah anggota Kerajaan Sylvamoor yang sedang online di server. |
| `%apexsions_online_kingdom_members%` | - | Angka | `12` | Jumlah anggota online dari kerajaan yang sama dengan pemain. |
| `%apexsions_staff_online%` | `%apexsions_staffonline%` | Angka | `4` | Jumlah staf yang sedang online (OP, permission `apexsions.staff`, atau grup staf). |

---

### 8. Kosmetik Aktif

| Placeholder | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- |
| `%apexsions_cosmetic_aura%` | String | `Flame Spiral` atau `None` | Nama efek aura kosmetik yang sedang dipakai pemain. |
| `%apexsions_cosmetic_trail%` | String | `Golden Dust` atau `None` | Nama jejak partikel (*trail*) yang sedang dipakai pemain. |
| `%apexsions_cosmetic_kill%` | String | `Lightning Strike` atau `None` | Nama efek eliminasi (*kill effect*) yang sedang dipakai pemain. |

---

## 💡 Contoh Konfigurasi TAB v6.1+

Simpan pada file konfigurasi plugin TAB (`plugins/TAB/config.yml`):

```yaml
tablist-name-formatting:
  default: "%apexsions_rank_badge% &f%player_name% %apexsions_title_suffix%"
header:
  - "<gradient:#FFD700:#FFA500><bold>APEXSIONS NETWORK</bold></gradient>"
  - "&7The Peak Civilizations &8| &fPemain: &a%server_online%&7/&f100"
  - ""
footer:
  - ""
  - "&fWilayah: &e%apexsions_current_territory_name% &8| &fStatus: &a%apexsions_war_status%"
  - "&fStaf Online: &b%apexsions_staff_online% &8| &fKerajaan Kamu: &6%apexsions_online_kingdom_members% Online"
  - "&7play.apexsions.my.id"
```
