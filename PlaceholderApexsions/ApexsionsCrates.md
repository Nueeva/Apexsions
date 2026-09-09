# 📦 PlaceholderAPI & Internal Placeholders — ApexsionsCrates

> **Plugin:** `ApexsionsCrates`  
> **Author:** ApexTeam / Apexsions  
> **Identifier PAPI:** `%apexsionscrates_<placeholder>_<crateId>%`  

Plugin **ApexsionsCrates** menyajikan sistem peti hadiah peradaban (*crates*) dengan dukungan multi-rarity bertingkat (**Common, Uncommon, Rare, Epic, Legendary, Mythic, Secret**), sistem milestone pembukaan, peluang independen (*rarity chance & item weight*), serta hologram interaktif.

Plugin ini memiliki dua kategori placeholder:
1. **PlaceholderAPI (PAPI):** Digunakan di Scoreboard, TAB, Actionbar, Chat, dan plugin pihak ketiga lainnya.
2. **Internal Placeholders:** Digunakan di dalam konfigurasi crate, file bahasa (`lang.yml`), pesan pengumuman (*broadcast*), lore item reward, dan hologram peti.

---

## 🌐 Bagian 1: PlaceholderAPI (PAPI Eksternal)

Seluruh placeholder PAPI dari ApexsionsCrates memerlukan ID peti hadiah (*crate ID*) di akhir nama placeholder.  
Contoh ID Peti: `common`, `mythic`, `war_chest`, `daily_crate`.

| Format Placeholder | Contoh Penggunaan | Contoh Output | Keterangan Lengkap |
| :--- | :--- | :--- | :--- |
| `%apexsionscrates_keys_<crateId>%` | `%apexsionscrates_keys_mythic%` | `3` atau `∞` | Menampilkan jumlah kunci / kesempatan pembukaan peti yang dimiliki pemain. Menampilkan `∞` jika tidak membutuhkan kunci. |
| `%apexsionscrates_openings_available_<crateId>%` | `%apexsionscrates_openings_available_mythic%` | `3` | Sama dengan `keys`, menghitung jumlah pembukaan yang dapat dilakukan saat ini. |
| `%apexsionscrates_openings_<crateId>%` | `%apexsionscrates_openings_mythic%` | `45` | Total berapa kali pemain tersebut telah membuka peti ini (dengan pemisah ribuan). |
| `%apexsionscrates_openings_raw_<crateId>%` | `%apexsionscrates_openings_raw_mythic%` | `45` | Total pembukaan peti pemain dalam format angka bulat murni. |
| `%apexsionscrates_openings_remaining_<crateId>%` | `%apexsionscrates_openings_remaining_mythic%` | `5` atau `∞` | Sisa batas pembukaan yang tersisa untuk pemain sebelum terkena batasan limit harian. |
| `%apexsionscrates_cooldown_<crateId>%` | `%apexsionscrates_cooldown_daily%` | `04h 12m` atau `Siap` | Sisa waktu tunggu (cooldown) hingga peti dapat dibuka kembali oleh pemain. Menampilkan status `Siap` (*Ready*) jika sudah bisa dibuka. |
| `%apexsionscrates_next_milestone_openings_<crateId>%` | `%apexsionscrates_next_milestone_openings_mythic%` | `5` | Jumlah pembukaan yang masih diperlukan pemain untuk mencapai hadiah milestone berikutnya. |
| `%apexsionscrates_next_milestone_reward_<crateId>%` | `%apexsionscrates_next_milestone_reward_mythic%` | `Pedang Dewa Api` | Nama item hadiah yang akan didapatkan pada milestone berikutnya. |
| `%apexsionscrates_latest_opener_<crateId>%` | `%apexsionscrates_latest_opener_mythic%` | `PlayerOne` | Nama pemain yang terakhir kali membuka peti hadiah tersebut. |
| `%apexsionscrates_latest_rolled_reward_<crateId>%` | `%apexsionscrates_latest_rolled_reward_mythic%` | `Batu Kristal Langka` | Nama hadiah terakhir yang berhasil didapatkan dari peti tersebut oleh pembuka terakhir. |

---

## ⚙️ Bagian 2: Internal Placeholders (Konfigurasi & Hologram)

Placeholder berikut diproses secara langsung oleh mesin internal **ApexsionsCrates** dan dapat digunakan pada file konfigurasi peti (`crates/*.yml`), hologram (`holograms.yml`), dan pesan bahasa (`lang/*.yml`):

### 1. Variabel Peti (Crate)
- `%crate_id%` : ID unik dari peti (misal: `mythic_crate`)
- `%crate_name%` : Nama tampilan peti lengkap dengan format warna/gradien
- `%crate_description%` : Deskripsi peti hadiah
- `%crate_last_opener%` : Nama pemain yang terakhir kali membuka peti
- `%crate_last_reward%` : Nama hadiah yang didapat pembuka terakhir
- `%crate_open_cost%` : Biaya atau kunci yang dibutuhkan untuk membuka

### 2. Variabel Peluang & Rarity (Rarity)
- `%rarity_id%` : ID tingkat kelangkaan (`common`, `uncommon`, `rare`, `epic`, `legendary`, `mythic`, `secret`)
- `%rarity_name%` : Nama tampilan rarity dengan warna (misal: `&d&lMYTHIC`)
- `%rarity_weight%` : Nilai bobot kelangkaan (misal: `10`)
- `%rarity_roll_chance%` : Persentase peluang kemunculan tingkat rarity tersebut (misal: `5.25%`)

### 3. Variabel Hadiah (Reward)
- `%reward_id%` : ID item hadiah
- `%reward_name%` : Nama item hadiah
- `%reward_description%` : Lore deskripsi hadiah
- `%reward_weight%` : Bobot item di dalam tingkat rarity-nya
- `%reward_roll_chance%` : Persentase peluang relatif item tersebut terpilih di dalam rarity-nya
- `%reward_rarity_name%` : Nama rarity dari hadiah tersebut
- `%reward_rarity_weight%` : Bobot rarity tempat hadiah tersebut bernaung
- `%reward_rarity_roll_chance%` : Peluang keseluruhan tingkat rarity hadiah tersebut

### 4. Variabel Milestone & Kunci (Milestone & Keys)
- `%milestone_openings%` : Target jumlah pembukaan milestone
- `%milestone_reward_id%` : ID hadiah yang dihadiahkan pada milestone tersebut
- `%key_id%` : ID kunci crate
- `%key_name%` : Nama tampilan kunci crate
- `%cost_id%` / `%cost_name%` : ID dan nama biaya pembukaan peti

---

## 📌 Contoh Penerapan Hologram Crate (DecentHolograms / Internal)

Berikut adalah contoh susunan baris teks hologram di atas peti crate:

```yaml
lines:
  - "<gradient:#FFD700:#FFA500><bold>PETI MYTHIC PERADABAN</bold></gradient>"
  - "&7Klik kanan untuk membuka &8| &7Klik kiri untuk pratinjau"
  - ""
  - "&fKunci Kamu: &e%apexsionscrates_keys_mythic% Kunci"
  - "&fPembuka Terakhir: &a%apexsionscrates_latest_opener_mythic%"
  - "&fHadiah Didapat: &6%apexsionscrates_latest_rolled_reward_mythic%"
  - ""
  - "&7Milestone Berikutnya: &b%apexsionscrates_next_milestone_openings_mythic% &7pembukaan lagi"
```
