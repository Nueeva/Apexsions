# 🎯 PlaceholderAPI — ApexsionsBattlepass

> **Plugin:** `ApexsionsBattlepass`  
> **Author:** ApexTeam / Apexsions  
> **Identifier PAPI:** `%apexsionsbattlepass_<placeholder>%`  

Plugin **ApexsionsBattlepass** mengelola sistem misi harian/mingguan (*quests*), tingkatan tiket (*Pass Tiers: Free & Premium*), toko berputar (*rotating shop*), hadiah berkala per level, serta mata uang musiman koin battlepass (**Battle Coins**).

Simbol resmi mata uang Battle Coins adalah koin emas: **`🪙`** (contoh: `1,250 🪙`).

---

## 📊 Daftar Placeholder Lengkap

| Placeholder | Alias Alternatif | Tipe Nilai | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- | :--- |
| `%apexsionsbattlepass_level%` | - | Angka (Integer) | `12` | Level Battlepass pemain pada musim saat ini. Mengembalikan `1` untuk pemain baru. |
| `%apexsionsbattlepass_xp%` | - | Angka (Integer) | `450` | Jumlah Battlepass XP yang sedang dikumpulkan pada level saat ini. |
| `%apexsionsbattlepass_required_xp%` | - | Angka (Integer) | `1000` | Jumlah XP yang diperlukan untuk naik ke level Battlepass berikutnya. |
| `%apexsionsbattlepass_progress_percent%` | - | Persentase (String) | `45%` | Persentase kemajuan XP menuju level pass berikutnya (dihitung otomatis dari perbandingan XP saat ini dan target). |
| `%apexsionsbattlepass_progressbar%` | - | Visual Bar | `&e████&8░░░░░░` | Batang kemajuan visual 10 segmen. Kuning (`&e`) untuk progres tercapai dan abu-abu (`&8`) untuk sisa. |
| `%apexsionsbattlepass_currency%` | `%apexsionsbattlepass_coins%` | Angka Mentah (Long) | `850` | Total Battle Coins yang dimiliki pemain dalam bentuk angka mentah (tanpa simbol). Sangat tepat untuk syarat pembelian di GUI. |
| `%apexsionsbattlepass_currency_formatted%` | `%apexsionsbattlepass_coins_formatted%` | String Terformat | `850 🪙` | Total Battle Coins terformat lengkap dengan pemisah ribuan dan simbol koin resmi `🪙`. |
| `%apexsionsbattlepass_pass%` | - | String | `FREE` atau `FREE, PREMIUM` | Daftar jenis pass yang telah diaktifkan/dibeli oleh pemain. |
| `%apexsionsbattlepass_season%` | - | String | `Season 1: Era Kejayaan` | Nama resmi musim Battlepass yang sedang berlangsung di server saat ini. |
| `%apexsionsbattlepass_season_time_left%` | - | Format Waktu | `18h 45m` atau `24d 12h` | Sisa durasi musim Battlepass sebelum musim berganti dan progres direset. |

---

## 💡 Contoh Implementasi di Server

### 1. Tampilan Scoreboard Sidebar:
```yaml
lines:
  - "&e&lBATTLEPASS &7(%apexsionsbattlepass_season%)"
  - "&7• &fLevel: &e%apexsionsbattlepass_level% &7(%apexsionsbattlepass_progress_percent%)"
  - "&7• &fProgres: %apexsionsbattlepass_progressbar%"
  - "&7• &fTiket: &a%apexsionsbattlepass_pass%"
  - "&7• &fKoin: &e%apexsionsbattlepass_coins_formatted%"
  - "&7• &fSisa Waktu: &c%apexsionsbattlepass_season_time_left%"
```

### 2. Tampilan Hologram di Lobby / Spawn:
```text
&e&l⚡ BATTLEPASS MUSIM INI ⚡
&6%apexsionsbattlepass_season%
&7Sisa Waktu Musim: &c%apexsionsbattlepass_season_time_left%
&fKetik &e/battlepass &funtuk menyelesaikan misi harian!
```

### 3. Syarat Pembelian Toko Exp-Shop / Daily Shop (DeluxeMenus):
```yaml
items:
  'legendary_sword':
    material: NETHERITE_SWORD
    display_name: "&6Pedang Pahlawan Peradaban"
    lore:
      - "&7Harga: &e1,500 🪙"
      - "&7Koin Kamu: &f%apexsionsbattlepass_coins_formatted%"
    click_requirement:
      requirements:
        coins_check:
          type: '>='
          input: '%apexsionsbattlepass_coins%'
          output: '1500'
          deny_commands:
            - "[message] &cBattle Coins kamu belum cukup! Butuh 1,500 🪙."
```
