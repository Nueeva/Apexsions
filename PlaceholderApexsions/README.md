# 🌟 Panduan Lengkap PlaceholderAPI — Apexsions Plugin Suite

> **Server / Network:** Apexsions  
> **Tagline:** *The Peak Civilizations*  
> **Target Runtime:** Minecraft 1.21.4 (Paper)  
> **Framework:** PlaceholderAPI (PAPI) v2.11.x  

Selamat datang di direktori dokumentasi resmi **PlaceholderAPI (PAPI)** untuk seluruh modul plugin dalam ekosistem **Apexsions**. Direktori ini menyajikan daftar placeholder terlengkap, format nilai, contoh output di dalam server, dan contoh integrasi pada TAB, Scoreboard, Hologram, Actionbar, serta Menu GUI.

---

## 📂 Daftar Berkas Dokumentasi per Plugin

Klik tautan berkas di bawah untuk membuka dokumentasi mendalam masing-masing plugin:

| Berkas Dokumentasi | Plugin Target | Identifier PAPI | Ringkasan Fungsi Placeholder |
| :--- | :--- | :--- | :--- |
| [ApexsionsCore.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsCore.md) | **ApexsionsCore** | `%apexsions_*%`<br>`%apexsionscore_*%` | Level, XP, Progress Bar, Kerajaan (Kingdom/Region), Rank LuckPerms beranimasi, Custom Title, Territory, Combat Tag, Status Perang, Online per Kingdom, Staff Online, dan Kosmetik. |
| [ApexsionsEconomy.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsEconomy.md) | **ApexsionsEconomy** | `%apexsionseconomy_*%` | Multi-Currency (`rupiah` = `Rp.`, `diamond` = `💎`), saldo mentah, saldo terformat, dan total lelang aktif di Auction House. |
| [ApexsionsBattlepass.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsBattlepass.md) | **ApexsionsBattlepass** | `%apexsionsbattlepass_*%` | Level Battlepass, XP, Required XP, Battle Coins mentah & terformat (`🪙`), Persentase Progres, Progress Bar visual, Tipe Pass (`FREE`/`PREMIUM`), Season aktif, dan sisa waktu season. |
| [ApexsionsChat.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsChat.md) | **ApexsionsChat** | `%apexsionschat_*%` | Channel obrolan aktif pemain (`Global`, `Staff`, `Kingdom`), jumlah surat/mail belum terbaca di kotak pos, dan jumlah laporan (`open_reports`) staf. |
| [ApexsionsCrates.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsCrates.md) | **ApexsionsCrates** | `%apexsionscrates_*%` | Kunci crate per ID, pembukaan tersedia, total openings, sisa cooldown pembukaan, milestone crate berikutnya, hadiah milestone, pembuka terakhir, hadiah terakhir rolled, serta placeholder internal konfigurasi. |
| [ApexsionsShop.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsShop.md) | **ApexsionsShop** | `%apexsionsshop_*%` | Tarif pajak pasar kerajaan (`tax_rate`), pajak murni, total item terdaftar, jumlah kategori toko, serta kondisi cuaca dinamis pengubah harga pasar. |
| [ApexsionsMedia.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsMedia.md) | **ApexsionsMedia** | Status & Integrasi | Integrasi banner interaktif, raytrace hover glow, URL action links, serta konsumsi placeholder PAPI dalam metadata banner. |
| [ApexsionsCustomEnchants.md](file:///c:/Apex%20Plugin/PlaceholderApexsions/ApexsionsCustomEnchants.md) | **ApexsionsCustomEnchants** | Status & Integrasi | Status integrasi placeholder untuk sistem custom enchantment, trigger set perlengkapan, dan lore item. |

---

## ⚡ Standar Penulisan Format Mata Uang di Apexsions

Sesuai dengan regulasi dan arsitektur resmi server **Apexsions**, seluruh penulisan mata uang wajib mematuhi standar simbol berikut:

1. **Rupiah (Mata Uang Utama / Ekonomi Standar)**:
   - Simbol/Prefix: `Rp.`
   - Contoh Output: `Rp. 25,000`
2. **Diamond (Mata Uang Premium / Hard Currency)**:
   - Simbol/Suffix: `💎`
   - Contoh Output: `150 💎`
3. **Battle Coins (Mata Uang Musiman Battlepass)**:
   - Simbol/Suffix: `🪙`
   - Contoh Output: `500 🪙`

---

## 🛠 Panduan Instalasi & Penggunaan

1. **Pastikan PlaceholderAPI terpasang:**  
   Unduh dan letakkan plugin `PlaceholderAPI-2.11.x.jar` di dalam folder `plugins/` server Minecraft Anda.
2. **Pasang Plugin Apexsions:**  
   Seluruh plugin Apexsions (`ApexsionsCore`, `ApexsionsEconomy`, dll.) akan secara otomatis mendeteksi dan mendaftarkan ekspansinya ke PlaceholderAPI saat server menyala (*onEnable*).
3. **Reload Konfigurasi:**  
   Jika Anda mengubah konfigurasi scoreboard, TAB, atau hologram, jalankan:
   ```text
   /papi reload
   /tab reload
   /dh reload
   ```
4. **Verifikasi Placeholder di Dalam Game:**  
   Gunakan perintah bawaan PAPI untuk menguji apakah placeholder berjalan semestinya:
   ```text
   /papi parse me %apexsions_level%
   /papi parse me %apexsionseconomy_rupiah_formatted%
   /papi parse me %apexsionsbattlepass_coins_formatted%
   ```

---

## 📌 Contoh Integrasi Papan Skor (Scoreboard)

Berikut adalah contoh implementasi pada plugin scoreboard (misal: TitleManager / TAB / Scoreboard-r):

```yaml
title: "<gradient:#FFD700:#FFA500><bold>APEXSIONS</bold></gradient>"
lines:
  - "&7&m---------------------"
  - "&fNama: &e%player_name%"
  - "&fPangkat: %apexsions_rank_badge%"
  - "&fKerajaan: %apexsions_kingdom_badge%"
  - "&7&m---------------------"
  - "&6Level: &f%apexsions_level% &7(%apexsions_xp_progressbar%&7)"
  - "&6Rupiah: &a%apexsionseconomy_rupiah_formatted%"
  - "&bDiamond: &f%apexsionseconomy_diamond_formatted%"
  - "&ePass Coins: &f%apexsionsbattlepass_coins_formatted%"
  - "&7&m---------------------"
  - "&fWilayah: &b%apexsions_current_territory_name%"
  - "&fStatus Perang: &c%apexsions_war_status%"
  - "&7&m---------------------"
  - "&eplay.apexsions.my.id"
```
