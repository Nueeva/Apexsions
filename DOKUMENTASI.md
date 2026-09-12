# DOKUMENTASI.md — Master Technical Documentation & Ecosystem State
# Apexsions — The Peak Civilizations

> **Repository:** `Nueeva/Apexsions`  
> **Primary Branch:** `main`  
> **Brand Name:** `Apexsions` (DILARANG menambahkan kata Network/SMP/Kingdom).  
> **Tagline:** `The Peak Civilizations`  
> **Dokumentasi Terakhir:** September 2026 (Sinkronisasi Penuh Pasca-Audit & Reset Memori)

Dokumen ini adalah **Single Source of Truth** untuk seluruh pengembang dan AI Coding Agent. Dokumen ini merangkum arsitektur, konfigurasi server, kredensial produksi, standar keamanan, sistem webstore, BlueMap, dual-theme, serta 8 plugin Minecraft secara komprehensif.

---

## 🏛️ 1. Identitas Brand & Aturan Naming
- **Nama Server / Brand:** `Apexsions` (Wajib murni nama ini di seluruh UI, log, judul, dan dokumentasi).
- **Tagline Resmi:** `The Peak Civilizations`.
- **Dilarang Keras:** Menambahkan imbuhan seperti "Apexsions SMP", "Apexsions Kingdom", "Apexsions Network".
- **Palet Warna Utama:**
  - Dark Mode (*Imperial Obsidian*): `#090A0D` (Void Black), `#111318` (Deep Obsidian), `#181A20` (Obsidian Surface), `#C9A45C` (Royal Gold), `#F4EFE6` (Celestial Ivory).
  - Light Mode (*Sovereign Ivory*): `#F6F8FA` (Ivory Canvas), `#FFFFFF` (White Card), `#0F172A` (Charcoal Slate Text), `#8C6A2E` / `#9E7B3E` (Antique Gold Accent).

---

## 🌐 2. Infrastruktur & Kredensial Server Produksi

### A. Web Server VPS (Azuriom CMS & Web Platform)
- **IP Address:** `89.144.53.100`
- **SSH Port:** `22`
- **Username:** `root`
- **Password:** `9tEMjeqysCYYqhRhxBvH`
- **Domain Resmi:** `http://web.apexsions.my.id` (Akses langsung IP: `http://89.144.53.100`)
- **Web Root:** `/var/www/azuriom`
- **Custom Theme Path:** `/var/www/azuriom/themes/apexsions`
- **Public Theme Assets:** `/var/www/azuriom/public/assets/themes/apexsions`
- **WebBridge Plugin:** `/var/www/azuriom/plugins/apexsions-bridge`
- **Database Seeder Master:** `/var/www/azuriom/database/seed_minecraft_systems.php`

#### Perintah Rutin Operasional VPS
```bash
cd /var/www/azuriom
php artisan view:clear && php artisan cache:clear && php artisan config:clear && php artisan route:clear
chown -R www-data:www-data /var/www/azuriom/themes/apexsions /var/www/azuriom/public/assets/themes/apexsions
systemctl reload nginx
```

### B. Game Server Minecraft (Jagoanhosting Pterodactyl SFTP)
- **Host / Server:** `falcon04.jagoanhosting.id`
- **Port SFTP:** `2022`
- **Username:** `rifqiariansyah123jt3.27e4a2f6`
- **Password:** `NuevaStore123#`
- **Protokol:** SFTP (`sftp://falcon04.jagoanhosting.id:2022`)
- **Runtime:** Paper API (Minecraft 26.2), Java 21 LTS.

### C. WebBridge API & Security Token
- **Endpoint:** `http://web.apexsions.my.id/api/apexsions-bridge`
- **Secret Key:** `apexsions_bridge_key_live_2026`
- **Player Sync API:** `POST /api/apexsions-bridge/sync-player`
- **Konfigurasi Lokal Minecraft:** `Minecraft/plugins/ApexsionsCore/src/main/resources/config.yml` (`web-bridge`)

### D. BlueMap 3D Interactive Server Map
- **Port BlueMap Live:** `32076`
- **URL Direct VPS:** `http://89.144.53.100:32076`
- **URL Internal Website:** `http://web.apexsions.my.id/server-map` (Route `apexsions-bridge.server-map`)
- **Status Integrasi:** Terhubung langsung dengan controller `ServerMapController.php`, live health-check probe port, serta fallback standby jika server offline.

---

## 💎 3. Sistem Webstore & Dual-Currency Architecture

### A. Dual-Currency Model
1. **Rupiah (Rp):** Mata uang resmi transaksi paket rank, privilege, dan bundel donatur webstore.
2. **Diamond (💎):** Mata uang premium Minecraft in-game yang dapat dibeli via webstore dan digunakan di in-game `/shop`, `/ah`, `/ce`, dan lelang.
   - **Kategori Khusus Webstore:** `Diamond Currency` (`/shop/category/diamond-currency`).
   - **Pilihan Paket:**
     - 100 Diamond (Rp 10.000)
     - 250 Diamond (Rp 25.000)
     - 500 Diamond (Rp 50.000)
     - 1.000 Diamond (Rp 95.000)
     - 2.500 Diamond (Rp 225.000)
     - 5.000 Diamond (Rp 425.000)

### B. Direct WhatsApp Order System (Fallback Midtrans)
Karena akun payment gateway Midtrans belum aktif, seluruh checkout dialihkan otomatis ke WhatsApp Founder/Admin:
- **Admin 1:** Rifqi (`6281212994597`)
- **Admin 2:** Friell (`6285883161047`)
- **Admin 3:** Favian (`6287729112281`)

---

## 🎨 4. Dual-Theme Engine (Obsidian Dark & Sovereign Ivory Light)

### A. Fitur & Kepatuhan Visual
- **Toggle Mode:** Tombol ikon bulan/matahari di navbar (`.apx-theme-toggle`). Status tersimpan di `localStorage.apx_theme` dan cookie.
- **Sovereign Ivory Light Mode:**
  - Seluruh kartu container, monolith ledger, kasta sosial, dan section berlatar belakang putih (`#FFFFFF` / `#F6F8FA`).
  - Tipografi charcoal slate kontras tinggi (`#0F172A` / `#334155`) memenuhi standar WCAG AAA (> 14:1).
  - Tiga Kerajaan memiliki warna aksen khas yang adaptif: Zenithar (Royal Gold `#8C6A2E`), Solterra (Crimson `#B91C1C`), Sylvamoor (Azure Blue `#0284C7`).
- **Hard Cache-Busting:**
  - Template `layouts/app.blade.php` memuat stylesheet dengan version tag dinamis:
    `<link rel="stylesheet" href="{{ theme_asset('css/style.css') }}&v=20260911_lightfix_{{ @filemtime(...) ?: time() }}">`
  - Memastikan browser pengunjung langsung memuat perubahan CSS tanpa tersangkut cache lama.

---

## 🌐 5. Sistem Bilingual & Aksesibilitas (`APX_I18N`)
- **Engine Terjemahan:** Klien JavaScript di `themes/apexsions/assets/js/app.js` (sinkron dengan `public/assets/themes/apexsions/js/app.js`).
- **Atribut HTML:** `data-i18n="key"`, `data-i18n-html="key"`, `data-i18n-placeholder="key"`.
- **Bahasa yang Didukung:** Bahasa Indonesia (`id` - Default) dan English (`en`).
- **Peralihan Instan:** Tidak memerlukan reload halaman web, transisi seketika dan tersimpan di `localStorage.apx_lang`.

---

## 🛡️ 6. Standar Keamanan Profil Publik Pemain
- **Format URL Resmi:** `/player/{uuid}` (Menggunakan UUID resmi Minecraft atau ID unik database).
- **Privasi:** DILARANG menggunakan username pemain sebagai URL slug resmi publik guna mencegah username enumeration dan scraping.
- **Legacy Redirect:** Rute lama `/player/{username}` dialihkan secara otomatis menggunakan **HTTP 301 Permanent Redirect** ke `/player/{uuid}`.

---

## 👑 7. Hierarki 11 Kasta Sosial (Official Rank Hierarchy)

Source of truth: `ranks.yml` & tabel database `apexsions_rank_configs`.

| Tingkat | Kasta / Rank | Weight | Default Benefits & Catatan Operasional |
| :--- | :--- | :---: | :--- |
| **Tier V** | `ancestor` | 100 | The Ancestor / Founder / Owner (Apex Authority). |
| **Tier IV** | `architect` | 95 | Realm Architect / Authority Builder (Setara). |
| **Tier IV** | `overseer` | 95 | Integrity & Balance / Tribunal Authority (Setara). |
| **Tier III** | `warden` | 90 | Head Staff / Admin Realm. |
| **Tier III** | `herald` | 80 | Staff / Moderator / Helper Realm. |
| **Tier II** | `sions` | 70 | Apex Donator (Tier Tertinggi Donatur). 10 Homes, 50 AH Slots, +100% Exp, 2x Diskon. |
| **Tier II** | `emperor` | 60 | Donator Tier 4. 7 Homes, 35 AH Slots, +75% Exp. |
| **Tier II** | `sovereign` | 50 | Donator Tier 3. 5 Homes, 25 AH Slots, +50% Exp. |
| **Tier II** | `archon` | 40 | Donator Tier 2. 4 Homes, 18 AH Slots, +30% Exp. |
| **Tier II** | `ascendant` | 30 | Donator Tier 1. 3 Homes, 12 AH Slots, +15% Exp. |
| **Tier I** | `wanderer` | 10 | Warga Baru / Default Citizen (Foundation). 1 Home, 5 AH Slots. |

*Catatan Khusus Staff:* Staff ranks (`ancestor`, `architect`, `overseer`, `warden`, `herald`) adalah **uncapped**, staf dapat membeli rank donatur tanpa terkena validasi "rank Anda sudah lebih tinggi".

---

## 🎮 8. Daftar 8 Plugin Suite Minecraft (Paper 26.2 / Java 21)

Struktur modul berada di folder `Minecraft/plugins/`:

1. **`ApexsionsCore`** (`com.apexsions.core.*`):
   - 3 Kerajaan: **Zenithar** (Timur / Dinasti), **Solterra** (Selatan / Magician), **Sylvamoor** (Barat / Rimba).
   - Auto-respawn ibukota terintegrasi BlueMap (`world.conf`).
   - Progresi Level 1-100 dengan 13 sumber XP.
   - **RPG Stat Scaling (Diminishing Curves):** Injeksi atribut native Paper (`Attribute.MAX_HEALTH` maks +12 HP, `Attribute.ATTACK_DAMAGE` maks +1.90), bonus PvE damage khusus monster (maks +26.5%), dan mitigasi resistensi monster (maks 10%).
   - **Unified Combat Engine & Smart PvP Normalizer:** Pipeline terisolasi dengan prioritas event (`NORMAL` -> `HIGH` -> `HIGHEST`), pemotongan excess attack > +0.80 di PvP, dan normalisasi proporsional defender ber-HP tinggi ke skala 24 HP tanpa bug heart-flicker.
   - **Profil Tempur Real-Time (`/k info`):** Lore kepala pemain di GUI profil menampilkan statistik fisik, keunggulan PvE, dan status profil fair-play PvP.
   - GUI Inspector 54-Slot & Admin Panel (`/ac inspect <p>`, `/ac setspawn`, dll).
   - Warp Navigasi & Editor Admin (`/warp`, `/warpmgr`).
   - Proteksi PvP sesama kerajaan di wilayah teritorial sendiri.
   - NightCore Native Dialog Input GUI (`CustomInputTextGUI`) tanpa anvil/sign crash.
2. **`ApexsionsChat`** (`com.apexsions.chat.*`):
   - Kyori MiniMessage formatting, Chat Channels (`Global`, `Kingdom`, `Staff`).
   - Settings GUI (`/channel settings`), Profile Hub (`/channel profile`), Show Item (`/showitem`), Offline Mail (`/mail`).
   - Staff Reports Desk 54-Slot (`/reports`).
3. **`ApexsionsEconomy`** (`com.apexsions.economy.*`):
   - Atomic multi-currency: Rupiah (Rp) & Diamond (💎).
   - Auction House (`/ah`) dengan sistem Escrow Claim terisolasi.
   - Barter/Trade 12-Slot terikat pajak teritorial antar-kerajaan.
4. **`ApexsionsBattlepass`** (`com.apexsions.battlepass.*`):
   - 200 Level BattlePass, Daily/Weekly/Monthly Quests, 4 Tier Pass.
   - Visual GUI Editor 54-Slot (`/abp`).
5. **`ApexsionsShop`** (`com.apexsions.shop.*`):
   - Dynamic Market 6 kategori, Rasio Jual dasar **20%**, Formula Dinamis Multiplier Cuaca & Bioma Kerajaan.
   - Price Clamping (50%-200%), Siaran tren pasar, GUI Jual Cepat 45-Slot (`/sell`).
6. **`ApexsionsMedia`** (`com.apexsions.media.*`):
   - Render multi-tile banner/logo asinkron, Raytrace line-of-sight hover glow, aksi interaksi URL terkonfirmasi.
7. **`ApexsionsCustomEnchants`** (`com.apexsions.customenchants.*`):
   - Dual-Currency Enchanter GUI (`/ce`), Toko Buku Sihir 54-Slot (`/ce shop`), 28 Custom Enchants, Admin Hub (`/ace`).
8. **`ApexsionsCrates`** (`com.apexsions.crates.*`):
   - Toko Kunci (`/crateshop`), Animasi pembukaan berbasis paket, milestone rewards.

#### Build Command Plugin
```powershell
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Core
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Chat
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Economy
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Battlepass
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Shop
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Media
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 CustomEnchants
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Crates
# Atau full suite:
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 -all
```

---

## ⚡ 9. Protokol Penghematan Token & Kuota (Mandatory Agent Protocol)

Wajib dipatuhi oleh seluruh coding agent di repositori Apexsions:
1. **Dilarang Headless Browser / Playwright Berlebihan:** Dilarang menggunakan `browser_subagent` atau Playwright terus-menerus yang membakar kuota token context secara masif. Gunakan inspeksi kode lokal, verifikasi sintaks, script HTTP fetch/cURL ringkas, atau unit test. Browser automation hanya diizinkan jika diminta eksplisit oleh user atau untuk 1x verifikasi visual akhir ringkas.
2. **Batasi Jangkauan Baca Berkas (`view_file`):** Jangan membaca ratusan baris berkas secara penuh. Selalu batasi dengan `StartLine` dan `EndLine` terfokus (20–60 baris).
3. **Pencarian Terarah (`grep_search`):** Wajib mengarahkan pencarian ke berkas/folder spesifik, hindari pencarian global tanpa filter.
4. **Pembatasan Output Terminal (`run_command`):** Batasi log perintah terminal menggunakan limit/paging (`head -n 30`, `--stat`, `--short`).
5. **Komunikasi Ringkas & Padat:** Hapus basa-basi percakapan dan pengulangan ringkasan yang sudah tercantum di dokumentasi. Langsung laporkan poin inti perubahan, hasil uji, dan hash commit.
6. **Local-First Validation:** Dilarang trial-and-error di VPS produksi. Pastikan validasi lokal lulus 100% sebelum deploy ke VPS.
7. **Autonomous Push Mandate:** Setelah perubahan divalidasi secara lokal dan di-commit, otomatis push ke branch `origin/main` menggunakan safe push practices tanpa menunggu perintah terpisah.

---

## ⚔️ 10. Sistem Tempur RPG, Progresi Atribut & Ekosistem Monster 6-Tier

Ekosistem tempur Apexsions menggabungkan **RPG Character Progression** dengan **Fair-Play PvP Normalization** untuk memastikan pemain merasakan perkembangan kekuatan nyata di PvE tanpa merusak keseimbangan perang antar-kerajaan (*Kingdom War*).

### A. Formula Diminishing Returns Progresi Atribut Pemain (Level 1–100)
Dihitung secara matematis murni di `com.apexsions.core.level.stat.PlayerStatCalculator`:

1. **Max Health Bonus ($HP_{bonus}$):**
   - **Lv 1–25:** $+0.20\text{ HP}$ / level (+$5.0\text{ HP}$ di Lv 25 $\rightarrow$ Total $25.0\text{ HP}$ / 12.5 Hati).
   - **Lv 26–50:** $+0.15\text{ HP}$ / level (+$3.75\text{ HP}$ di Lv 50 $\rightarrow$ Total $28.75\text{ HP}$ / ~14.5 Hati).
   - **Lv 51–75:** $+0.10\text{ HP}$ / level (+$2.50\text{ HP}$ di Lv 75 $\rightarrow$ Total $31.25\text{ HP}$ / ~15.5 Hati).
   - **Lv 76–100:** $+0.03\text{ HP}$ / level (+$0.75\text{ HP}$ di Lv 100 $\rightarrow$ Total **$32.0\text{ HP}$ / 16 Hati Maksimal**).
   - *Injeksi:* Paper Native `Attribute.MAX_HEALTH` (`apex_level_health`).

2. **Attack Damage Bonus ($ATK_{bonus}$):**
   - **Lv 1–30:** $+0.03\text{ Attack}$ / level (+$0.90\text{ Attack}$ di Lv 30).
   - **Lv 31–60:** $+0.02\text{ Attack}$ / level (+$0.60\text{ Attack}$ di Lv 60 $\rightarrow$ Total $+1.50\text{ Attack}$).
   - **Lv 61–100:** $+0.01\text{ Attack}$ / level (+$0.40\text{ Attack}$ di Lv 100 $\rightarrow$ Total **$+1.90\text{ Attack}$ Maksimal**).
   - *Injeksi:* Paper Native `Attribute.ATTACK_DAMAGE` (`apex_level_attack`).

3. **PvE Damage Multiplier ($PvE_{mult}$ - Khusus Monster):**
   - **Lv 1–20:** $+0.50\%$ / level ($+10.0\%$ di Lv 20).
   - **Lv 21–50:** $+0.30\%$ / level ($+9.0\%$ di Lv 50 $\rightarrow$ Total $+19.0\%$).
   - **Lv 51–75:** $+0.20\%$ / level ($+5.0\%$ di Lv 75 $\rightarrow$ Total $+24.0\%$).
   - **Lv 76–100:** $+0.10\%$ / level ($+2.5\%$ di Lv 100 $\rightarrow$ Total **$+26.5\%$ Maksimal**).

4. **PvE Resistance ($RES_{pve}$ - Mitigasi Serangan Monster):**
   - **Lv 1–24:** $0\%$
   - **Lv 25–49:** $2.5\%$
   - **Lv 50–74:** $5.0\%$
   - **Lv 75–99:** $7.5\%$
   - **Lv 100:** **$10.0\%$ Maksimal**

---

### B. Arsitektur Unified Combat Engine & Smart PvP Normalizer
Kalkulasi tempur terbagi dalam 3 tahap terisolasi untuk mencegah *multiplicative compounding trap* dengan buff kerajaan dan armor kit:

```text
[Priority: NORMAL]  PlayerCombatProgressionListener
   ├── PvE Outgoing: Damage × (1 + PvE_mult)
   ├── PvE Incoming: Damage × (1 - RES_pve)
   └── PvP Outgoing: Pemangkasan Excess Attack > +0.80 dari base damage
           ↓
[Priority: HIGH]    KingdomBuffListener & KitArmorSetListener
   ├── Solterra (+15% dmg / +10% crit), Zenithar (+6% dmg / -25% crit taken), Sylvamoor (-10% pvp / -5% pve)
   └── Kit Armor Set Bonus (ATTACK_DAMAGE_BOOST, CRITICAL_DAMAGE_BOOST, Tool Set +25%)
           ↓
[Priority: HIGHEST] SmartCombatNormalizer (PvP Only)
   └── Normalisasi Defender ber-HP tinggi: Damage × (ActualMaxHP / 24.0)
```

#### Keunggulan Desain Smart Normalizer:
* **Anti Double-Survivability:** Karena Bukkit `event.setDamage()` memodifikasi pre-armor damage, faktor $\text{ActualMaxHP}$ di pembilang dan penyebut saling menghilangkan. Pemain Level 100 dengan armor Netherite tetap kehilangan persentase bar darah yang setara dengan kolam $24.0\text{ HP}$ (+4 HP cap).
* **Bebas Glitch:** Tidak mengubah atau memotong bar hati visual pemain saat bertarung di PvP (tidak ada *heart-flickering*).
* **Isolasi Total:** Bonus PvE ($+26.5\%$) dan resistensi monster ($10\%$) mati total ($0\%$) dalam PvP.

---

### C. Ekosistem 6-Tier Monster Progression & Dynamic Spawning
Menghilangkan jurang kekosongan konten (Lv 16–74) dengan pembagian zona bertingkat dan *weighted random levels* di MythicMobs:

| Tier | Wilayah / Zona | Rentang Level | Bobot Spawning & Karakteristik | Peran Gameplay |
| :---: | :--- | :---: | :--- | :--- |
| **Tier 1** | **Wilayah Kerajaan** *(Capital & Claims)* | **Lv. 1 – 5** | • 60% Lv 1–2<br>• 30% Lv 3–4<br>• 10% Lv 5 | Zona aman, adaptasi pemula, farming bahan pokok. |
| **Tier 2** | **Alam Liar (*Wilderness*)** | **Lv. 5 – 20** | • 45% Lv 5–9 (*Forest Stalker*)<br>• 35% Lv 8–16 (*Dune Marauder*)<br>• 20% Lv 12–20 (*Canyon Marksman*) | Eksplorasi malam survival, perburuan bahan standar. |
| **Tier 3** | **Lembah Berbahaya (*Dangerous Wilds*)** | **Lv. 20 – 40** | • Weighted random Lv 20–40<br>• Troll, Spider Matriarch, Dark Cultist | Mid-game barrier, eksplorasi gua & hutan tua. |
| **Tier 4** | **Zona Korupsi & Outpost Bandit** | **Lv. 40 – 65** | • Weighted random Lv 40–65<br>• Drop fragmen relic & custom enchant tier 1-2 | Dungeon bawah tanah, perburuan tim kecil. |
| **Tier 5** | **Reruntuhan Kuno Sions (*Terra Interdicta*)** | **Lv. 65 – 90** | • Lv 65–78: *Sions Fallen Legionnaire*<br>• Lv 65–75: *Sions Void Crawler*<br>• Lv 75–85: *Sions Void Assassin* & *Channeler*<br>• Lv 80–90: *Sions Ruin Sentinel* & *Void Knight* | Endgame grinding, farming Kunci Elit & Dark Core. |
| **Tier 6** | **World Raid Lair** | **Lv. 90 – 100** | • Mini-Boss: *Voran, The Ruined Commander* (**Lv. 90**)<br>• World Raid Boss: *Kaisar Valerius* (**Lv. 100**) | Puncak tantangan server, multi-phase raid boss. |

---

### D. Antarmuka Profil Pemain (`/k info` / `/k profile`)
Setiap pemain dapat melihat profil status fisiknya secara transparan di Slot 13 GUI Profil Kerajaan:
```text
❤ Max Health : 32.0 HP (+12.0 dari Level)
🗡 Base Attack: +1.90 (Bonus Fisik)
🏹 PvE Mastery: +26.5% Dmg • 10.0% Resis
⚖ PvP Profile: Fair-Play Normalized (Cap +4 HP / +0.8 Atk)
```

---

## 🌌 11. The Aetherial Conclave (Dimensi Atas Aetherion) & Standarisasi Lore Sistem

Berdasarkan kanon kosmologi di `LORE.md` (Bab I, II, VIII, dan XIV), tata kelola peran dan peradaban semesta Apexsions distandarkan secara ketat:

### A. Doktrin Transendensi The Aetherial Conclave
Staf dan pengawas tertinggi server (*Ancestor, Architect, Overseer, Warden, Herald*) diakui secara sistemik sebagai entitas dimensi atas yang bersemayam di **The Aether Citadel (Aetherion)**:
* **Penetapan Status Transenden:** Diidentifikasi melalui `LuckPermsHook.isConclaveStaff(player/uuid)` berbasis OP, permission staff (`apexsions.admin`, `apexsions.staff`, `apexsionscore.admin`), dan bobot rank $\ge 80$.
* **Larangan Sumpah Setia Mortal:** Staf Conclave dilarang keras terikat sumpah setia kepada kerajaan bangsa fana (*Zenithar*, *Solterra*, *Sylvamoor*). Upaya memilih kerajaan via `/kingdom choose`, `/k join`, `RegionSelectionGUI`, dan `KingdomConfirmGUI` dibatalkan seketika dengan pesan transenden kosmik.
* **Slot Khusus di GUI Info (`KingdomInfoGUI`):** Slot 49 bagi staf menampilkan *Beacon of The Aetherial Conclave* (bukan tombol "Pilih Kerajaan"). Klik dibatalkan secara aman dengan efek suara `BLOCK_BEACON_ACTIVATE`.
* **Proteksi Administratif & GUI Inspeksi:**
  - Perintah admin `/ac setregion <player> <kingdom>` menolak pendaftaran staf Conclave ke kerajaan mortal manapun.
  - Perintah reset `/ac resetregion` mengembalikan status staf murni ke entitas dimensi Aetherion tanpa prompt pemilihan fana.
  - `PlayerInspectorGUI`: Melindungi staf agar tidak dapat dimasukkan ke kerajaan fana atau dinobatkan sebagai Raja mortal (`toggleMonarch`).
* **Visualisasi Profil & PlaceholderAPI:**
  - Di `KingdomProfileGUI` (`/k info`), identitas kerajaan staf menampilkan `✦ Aetherion (Conclave) ✦` dan slot 20 menampilkan kartu *The Aetherial Conclave*.
  - Placeholder `%apexsions_kingdom%` mengembalikan `AETHERION`.
  - Placeholder `%apexsions_kingdom_name%` mengembalikan `✦ Aetherion ✦`.
  - Placeholder `%apexsions_kingdom_badge%` mengembalikan `<gradient:#00f2fe:#4facfe><bold>[AETHERION]</bold></gradient>`.
* **Pembersihan Otomatis Saat Login (`PlayerListener`):** Menghapus `regionId` mortal secara otomatis jika akun staf sebelumnya tidak sengaja terdaftar, serta memberikan greeting resmi Conclave pada pemain baru berstatus staf tanpa memaksa pembukaan menu pemilihan kerajaan.

---

### B. Arsitektur Zona Terlarang Sions (*Terra Interdicta*)
Kekaisaran Sions bukan lagi kerajaan aktif atau faksi yang dapat dihuni, melainkan zona bahaya tingkat tinggi berstatus **Terra Interdicta**:
* **Teritori Poligon Permanen:** Terdaftar di `RegionManager` sebagai `"Terra Interdicta (Sions)"` dengan 11 titik koordinat batas wilayah.
* **Pemberitahuan Teritorial:** Pemain yang melintasi perbatasan menerima pesan action bar:
  `<dark_red><bold>☠ Teritori Terlarang: </bold><gradient:#8e44ad:#9b59b6><bold>TERRA INTERDICTA</bold></gradient></dark_red> <gray>(Reruntuhan Kuno Sions • Zona Anomali)</gray>`.
* **Anomali Temporal Engine (`/sions`):**
  - Pemulihan struktur reruntuhan kuno otomatis setiap 60 menit (`SionsTemporalService`).
  - Proteksi peti relik kuno dari kehancuran paksa (`SionsContainerLockListener`) dengan sistem kunci 3-tier (*Common*, *Elite*, *Boss*).
  - Siaran rekonstruksi resmi: `ANOMALI TEMPORAL TERRA INTERDICTA (SIONS)!`.

---

### C. Penegakan Brand & Sistem Spawn Sanctum
1. **Nama Server Murni:** Penegakan nama tunggal **`Apexsions`**. Perintah `/kingdom` help menu menggunakan format `Apexsions — Kingdom System Commands:`.
2. **Spawn Sanctum (Bab XIV):** Perintah `/lobby` mengantarkan pemain menuju **Spawn Sanctum (The Threshold of Realities)** sebagai poros gerbang antar-dimensi semesta Apexsions.
3. **Standarisasi Kit GUI:** Judul menu `/kits` distandarkan menjadi `📦 APEXSIONS KITS PERADABAN 📦`.

---

### D. Penyelarasan Web Platform & Admin Panel (Azuriom & WebBridge)
1. **Fallback Mandat Staf Conclave (`AETHERION`):**
   - Staf dimensi atas (`ancestor`, `architect`, `overseer`, `warden`, `herald`, atau weight $\ge 80$) tidak dapat berafiliasi dengan kerajaan fana (*Zenithar*, *Solterra*, *Sylvamoor*).
   - Tindakan `SET_KINGDOM` pada web admin secara cerdas menolak penempatan staf Conclave ke kerajaan mortal demi integritas lore.
   - `RESET_KINGDOM` secara otomatis melakukan fallback transenden ke `AETHERION` (*Aetherion (The Conclave)*) bagi akun staf, dan `NONE` (*Belum Memilih*) bagi warga biasa.
   - Guard `APPOINT_KING`: Memblokir penobatan takhta mortal untuk entitas pengawas dimensi atas Conclave.
2. **Eliminasi Kerajaan Sions pada Web Interface:**
   - Sions (*Terra Interdicta*) dihapus secara total dari pilihan kerajaan aktif pada formulir web admin.
   - Sanitasi otomatis di `PlayerSyncController`: Jika ada data lama atau paket sinkronisasi mengirimkan faksi `SIONS`, sistem otomatis menormalisasikannya ke `NONE` (mortal) atau `AETHERION` (staf).
3. **Penyempurnaan Visual & UI Admin / Publik:**
   - Direktori warga admin (`/admin/players`) memiliki filter khusus `✦ Aetherion (The Conclave)` dan lencana tabel kosmik `bg-info`.
   - Modul Player 360 Overview (`show.blade.php`) menghadirkan kartu eksklusif *The Aetherial Conclave* serta peringatan visual pada modal aksi.
   - Tampilan profil publik (`/player/{uuid}`) dan tema profil (`/profile`) menampilkan lencana faksi Aetherion dengan ikon `bi-stars` dan dukungan bilingual i18n (`kingdom_aetherion_name`).
