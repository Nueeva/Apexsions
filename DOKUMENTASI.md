# DOKUMENTASI.md — Master Technical Documentation & Ecosystem State
# Apexsions — The Peak Civilizations

> **Repository:** `Nueeva/Apexsions`  
> **Primary Branch:** `main`  
> **Brand Name:** `Apexsions` (DILARANG menambahkan kata Network/SMP/Kingdom).  
> **Tagline:** `The Peak Civilizations`  
> **Dokumentasi Terakhir:** September 2026 (Sinkronisasi Penuh Pasca-Audit & Reset Memori)  
> **Riwayat Perubahan & Handoff AI:** Lihat [CHANGELOG.md](file:///c:/Users/Friel/Documents/Rifqi%20Ariansyah/Apexsions/CHANGELOG.md) untuk detail kronologis pembaruan.

Dokumen ini adalah **Single Source of Truth** untuk seluruh pengembang dan AI Coding Agent. Dokumen ini merangkum arsitektur, konfigurasi server, kredensial produksi, standar keamanan, sistem webstore, BlueMap, dual-theme, serta 9 plugin Minecraft secara komprehensif.

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

### C. Varian Durasi Paket Kasta & Kebijakan Reset Season (120 Hari)
- **Varian Durasi:**
  - **Permanen:** Seumur hidup, aman dan tetap abadi melintasi seluruh siklus reset dunia/wipe.
  - **Trial 120 Hari (1 Season Penuh):** Berlaku sepanjang 1 siklus Era penuh (4 bulan / 120 hari).
  - **Trial 30 Hari (1 Bulan):** Paket uji coba hemat per satu bulan kalender.
- **Siklus Reset Peta (120 Hari / 4 Bulan per Era):**
  - **Wiped:** Peta dunia, klaim wilayah, inventory, level RPG (Lv 1-100), saldo kas perbendaharaan kerajaan (`economy_kingdom_treasury`), dan progres Battlepass.
  - **Permanent (Kept):** Rank donatur permanen (`sions`, `emperor`, `sovereign`, `archon`, `ascendant`), sisa saldo Diamond webstore, dan gelar kehormatan sejarah.

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

## 🎮 8. Daftar 9 Plugin Suite Minecraft (Paper 26.2 / Java 21)

Struktur modul berada di folder `Minecraft/plugins/`:

1. **`ApexsionsCore`** (`com.apexsions.core.*`):
   - 3 Kerajaan: **Zenithar** (Timur / Dinasti), **Solterra** (Selatan / Magician), **Sylvamoor** (Barat / Rimba).
   - Auto-respawn ibukota terintegrasi BlueMap (`world.conf`).
   - Progresi Level 1-100 dengan 13 sumber XP.
   - **RPG Stat Scaling (Diminishing Curves):** Injeksi atribut native Paper (`Attribute.MAX_HEALTH` maks +12 HP, `Attribute.ATTACK_DAMAGE` maks +1.90), bonus PvE damage khusus monster (maks +26.5%), dan mitigasi resistensi monster (maks 10%).
   - **Unified Combat Engine & Smart PvP Normalizer:** Pipeline terisolasi dengan prioritas event (`NORMAL` -> `HIGH` -> `HIGHEST`), pemotongan excess attack > +0.80 di PvP, dan normalisasi proporsional defender ber-HP tinggi ke skala 24 HP tanpa bug heart-flicker.
   - **Sovereign Land Claiming & Upkeep Economy (`/claim`):** Brankas deposit mandiri per wilayah (`Claim Bank`), Pajak Harian Progresif ($100 \times (1 + (\text{Total Chunks} - 1) \times 0.15)$), 50% setoran otomatis ke Kas Kerajaan (`KingdomTreasury`), Masa Tenggang 72 Jam (*Grace Period*) dengan auto-unclaim saat penunggakan berlanjut.
   - **Kedaulatan Upper Dimension Conclave:** Kuota klaim **Tanpa Batas (`∞`)** dan **Bebas Pajak Upkeep (`Rp 0.0/hari`)** bagi entitas Conclave (Weight $\ge 80$: `ancestor`, `architect`, `overseer`, `warden`, `herald`) sesuai kanon `LORE.md`.
   - **Flags & Peran Granular:** Pengaturan flags wilayah (`pvp`, `mob_spawn`, `fire_spread`, `explosions`, `greeting`, `farewell`) dan 4 hierarki peran warga (`OWNER`, `MANAGER`, `BUILDER`, `VISITOR`).
   - **Kingdom War Siege Mode:** Perlindungan wilayah musuh terbuka untuk diserbu saat status perang resmi berkobar (jika pemilik online).
   - **Centralized Moderation Engine:** Sistem ban/unban otoritatif terpadu (`/ban`, `/tempban`, `/unban`, `/pardon`, `/banip`, `/unbanip`, `/checkban`, `/banlist`), socket-level pre-login gatekeeper menangkal bypass AuthMe, dan eliminasi fragmentasi sanksi EssentialsX.
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
   - 200 Level BattlePass, Daily/Weekly/Monthly Quests (Siklus 4 Bulan / 120 Hari per Era), 4 Tier Pass.
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
9. **`ApexsionsFishing`** (`com.apexsions.fishing.*`):
   - Sistem **AFK Fishing** dan **Active Reel Engine** dengan mekanik tangkapan interaktif.
   - **Rarity & Weight Engine 6-Tier:** `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`, `MYTHIC` dengan bobot berat gram realistis dan nilai jual dinamis.
   - **Virtual Bait Quota System (`/fish bait` / `BaitShopGUI`):** Kuota umpan virtual tersimpan di database (`baits.yml`) dengan peluang gigitan dan bonus bobot ikan langka.
   - **Native Dialog Admin Rod Creator GUI (`AdminRodCreatorGUI`):** Pembuatan dan konfigurasi joran khusus admin via `NativeDialogAdapter` & `FishingInputGUI` tanpa resiko crash anvil.
   - **Fishing Vault Storage 54-Slot (`/vault`):** Brankas penyimpanan tangkapan ikan eksklusif per pemain dengan fitur upgrade kapasitas (`VaultShopGUI`).
   - **Fish Market & Instant Delivery (`/fish sell`):** Pasar penjualan ikan terintegrasi `ApexsionsEconomy` (Rupiah/Diamond) dengan bonus pengiriman.
   - **Auto-Catch Rods & Upgrade Engine (`/fish rods`):** Joran pancing khusus dengan durabilitas, kecepatan gigitan, dan auto-reel chance.
   - **Top Angler Leaderboard:** Terintegrasi dengan kebijakan pengecualian staf, OP, dan dimensi atas Aetherion.

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
powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 Fishing
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

---

## 🏆 12. Kebijakan Papan Peringkat (Leaderboard Exemption Policy) & Struktur Leaderboard

Untuk menjaga asas integritas kompetisi, keadilan bermain (*fair-play*), dan keselarasan kanon kosmologi (staf adalah entitas transenden The Conclave, bukan warga fana yang bersaing di papan peringkat), seluruh leaderboard baik di platform web maupun in-game menerapkan kebijakan pengecualian terstandarisasi.

### A. Kebijakan 6-Lapis Pengecualian Akun (Leaderboard Exemption Contract)
Pemain otomatis **dikecualikan (dieliminasi)** dari seluruh papan peringkat publik jika memenuhi salah satu dari kriteria berikut:
1. **Bukkit Operator (OP):** Terdaftar di `ops.json` atau berstatus `player.isOp() == true`.
2. **Staff Rank Weight $\ge 80$:** Memiliki rank staf LuckPerms (`ancestor` [100], `architect` [95], `overseer` [95], `warden` [90], `herald` [80]).
3. **Afiliasi Kerajaan Transenden Aetherion:** Terdaftar dengan `kingdom_id = 'AETHERION'` (The Aetherial Conclave).
4. **Hak Akses & Permission Nodes:** Memiliki salah satu permission administratif:
   - `apexsions.admin`
   - `apexsions.staff`
   - `apexsionscore.admin`
   - `apexsions.conclave`
   - `apexsions.leaderboard.exempt`
5. **Hak Akses Web Azuriom CMS:** Memiliki role administrator web (`role->is_admin == true`).
6. **Daftar Hitam Akun Founder & Developer:** Akun staf/founder yang terdaftar dalam daftar hitam username:
   - `nueeva`, `nuevaid`, `rifqi`, `friell`, `favian`, `fanerf`, `kazrienvall`.

### B. Struktur Papan Peringkat Web Platform (`/leaderboard`)
Halaman papan peringkat web (`https://web.apexsions.my.id/leaderboard`) secara ketat menyajikan **2 Tabel Utama**:
1. **🏆 Peringkat Level & EXP Warga (*Civilization Level & Mastery*):**
   - Mengurutkan warga berdasarkan akumulasi level (1–100) dan total perolehan XP.
   - Dilengkapi avatar 3D pemain, lencana kasta donatur/warga, serta afiliasi kerajaan mortal (*Zenithar*, *Solterra*, *Sylvamoor*).
2. **💰 Peringkat Perbendaharaan Saldo Rupiah (*Economic Wealth & Treasury*):**
   - Mengurutkan warga berdasarkan total saldo Rupiah (Rp) yang tersimpan di rekening moneter server.
   - Format mata uang Rupiah presisi (`Rp xxx.xxx`).
- **Batasan Arsitektur Web (MANDATORY):** Leaderboard BattlePass **DITIADAKAN DARI WEB** dan bersifat eksklusif in-game (`/abp top`). Kebijakan ini menjaga antarmuka web tetap bersih, mewah, terfokus pada status peradaban permanen, dan tidak membebani performa query web server.

### C. Sinkronisasi Leaderboard In-Game (Cross-Plugin Enforcement)
Di lingkungan Minecraft server, filter pengecualian dieksekusi secara asinkron sebelum rendering GUI:
- **`ApexsionsCore` (`KingdomTopGUI` & `/kingdom top`):** Menyaring akun yang memenuhi `ApexsionsCoreAPI.isLeaderboardExempt(uuid)` dari daftar peringkat level tertinggi kerajaan dan server.
- **`ApexsionsEconomy` (`EconomyLeaderboardService` & `/baltop`):** Menyaring akun staf dan OP dari daftar pemain terkaya.
- **`ApexsionsBattlepass` (`BattlePassLeaderboardService` & `/abp top`):** Menyaring akun staf dari daftar progres tier pass tertinggi.
- **`ApexsionsFishing` (`VaultStorageManager` & `/vault top`):** Menyaring akun staf dari daftar tangkapan ikan terbanyak dan ikan terberat (*Top Anglers*).

---

## 🔍 13. Standarisasi SEO, Google Search Console, Schema JSON-LD & Proteksi Domain

Platform web Apexsions menerapkan arsitektur SEO enterprise dan proteksi integritas domain yang ketat untuk memastikan visibilitas pencarian Google optimal dan terlindungi dari manipulasi eksternal.

### A. Standarisasi `sitemap.xml` & Nginx Response Headers
- **Format Berkas:** Berada di `Website/public/sitemap.xml` dengan namespace W3C resmi (`<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">`).
- **Index URLs:** Memuat seluruh rute publik penting (`/`, `/about-us`, `/rules`, `/leaderboard`, `/server-map`, `/shop`, `/vote`, `/wiki`).
- **Pembaruan Berkala:** Tanggal `<lastmod>` distandarkan ke versi termutakhir (`2026-09-15`).
- **Nginx Server Directives (`/etc/nginx/sites-available/azuriom`):**
  ```nginx
  location = /sitemap.xml {
      root /var/www/azuriom/public;
      default_type application/xml;
      add_header Content-Type "application/xml; charset=utf-8";
      add_header X-Robots-Tag "all, index, follow";
      add_header Access-Control-Allow-Origin "*";
      add_header Cache-Control "public, max-age=3600";
      try_files $uri =404;
  }
  ```
  Menjamin bot mesin pencari (khususnya Googlebot) menerima `Content-Type: application/xml` murni tanpa terkena intercept routing aplikasi.

### B. Schema.org Structured Data (JSON-LD) & Entity Disambiguation
Disuntikkan secara statis di `<head>` master layout `themes/apexsions/views/layouts/app.blade.php`:
1. **Entitas `@type: Organization` & `@type: WebSite`:**
   - Menetapkan nama resmi entitas: `Apexsions`.
   - Menetapkan URL canonical resmi: `https://web.apexsions.my.id/`.
   - Menetapkan slogan: `The Peak Civilizations`.
2. **Disambiguating Description (Pembeda Entitas Google Knowledge Graph):**
   - Menegaskan deskripsi entitas: *"Apexsions adalah server peradaban Minecraft Indonesia independen bertema 3 Kerajaan besar (Zenithar, Solterra, Sylvamoor) dengan ekonomi atomic Rupiah dan RPG progression. Apexsions sama sekali tidak berafiliasi dengan perusahaan software bernama Apexion atau penyedia hosting bernama Apex Hosting."*
   - Memastikan algoritma pencarian Google tidak mencampuradukkan indeks atau menyajikan snippet yang keliru terhadap entitas bisnis lain.

### C. Proteksi Domain Liar & Mitigasi Backlink Parasitik (*Rogue Domain Neutralization*)
Server VPS (`89.144.53.100`) dikonfigurasi dengan blok `default_server` pada Nginx untuk menangkal domain eksternal yang mengarahkan IP address atau DNS record tanpa izin (misalnya domain spam `professionelle-dachsanierung.com`):
```nginx
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;
    return 301 https://web.apexsions.my.id$request_uri;
}
```
- **Fungsi Proteksi:** Mengalihkan seluruh traffic dan robot crawler domain liar menggunakan **HTTP 301 Permanent SEO Redirect** ke domain resmi `https://web.apexsions.my.id/`.
- **Dampak SEO:** Mengonsolidasikan otoritas link (link equity) kembali ke Apexsions dan menghapus asosiasi negatif domain spam dari indeks mesin pencari.

### D. Status Indeks Google Search Console
- **Status URL:** Terverifikasi resmi dengan status **"URL ada di Google"** (*URL is on Google*).
- **Pengindeksan:** Halaman canonical dinyatakan valid (`https://web.apexsions.my.id/`).
- **Spider Crawl:** Telah dirayapi oleh *Googlebot untuk Ponsel cerdas* (Smartphone Crawler) dengan kepatuhan penuh terhadap standar keramahan seluler (*mobile-friendly*) dan keterbacaan aset CSS/JS.

---

## 🏰 14. Sistem Kedaulatan Wilayah (Sovereign Land Claiming), Upkeep Progresif & Hak Istimewa Upper Dimension

Sistem kedaulatan tanah di Apexsions menghubungkan proteksi anti-griefing, ekonomi sewa wilayah progresif, kas kerajaan, dan perang peradaban dalam satu ekosistem terpadu:

### A. Brankas Wilayah & Formula Pajak Progresif (Upkeep Economy)
1. **Brankas Wilayah (Claim Bank):**
   Setiap petak klaim memiliki brankas dana mandiri. Pemain menyetor saldo Rupiah via `/claim deposit <nominal>` atau tombol setoran cepat Rp1.000 / Rp10.000 di `/claim gui`.
2. **Formula Pajak Progresif:**
   $$\text{Tarif Harian Per Chunk} = 100 \times (1 + (\text{Total Chunks Milik Pemain} - 1) \times 0.15)$$
   Mekanisme ini mencegah penimbunan tanah kosong berlebih oleh segelintir pemain dan merangsang perputaran ekonomi peradaban.
3. **Aliran Kas Kerajaan (Kingdom Treasury Split):**
   Setiap siklus 24 jam, pemotongan sewa otomatis terjadi:
   - **50%** disalurkan langsung ke Kas Kerajaan pemain (`ApexsionsEconomyProvider.depositKingdomTreasury`) untuk mendanai pertahanan kerajaan.
   - **50%** dibakar dari peredaran (*money sink* server).

### B. Masa Tenggang (Grace Period 72 Jam) & Penyitaan Otomatis (Auto-Unclaim)
1. **Status Menunggak:** Jika brankas klaim kosong saat jatuh tempo, status tanah berubah menjadi `GRACE_PERIOD` selama 72 jam (3 hari).
2. **Peringatan Login & Movement:** Pemilik yang login atau melangkah ke dalam wilayah menunggak menerima audio alert dan notifikasi durasi sisa masa tenggang.
3. **Penyitaan Wilayah (Auto-Unclaim):** Jika 72 jam habis tanpa setoran saldo baru, sistem melepas klaim tanah secara otomatis dan mengembalikannya menjadi alam liar (*Wilderness*).

### C. Hak Istimewa Kedaulatan Upper Dimension Conclave (Lore-Compliant per `LORE.md`)
Berdasarkan kanon kosmologi `LORE.md` (Bab I & Bab VIII: The Aetherial Conclave):
1. **Klaim Tanpa Batas (Unlimited Claims):**
   Entitas Upper Dimension (`ancestor` [100], `architect` [95], `overseer` [95], `warden` [90], `herald` [80], serta pemegang izin `apexsions.claim.unlimited`) memiliki kuota tanpa batas (`-1` / `Integer.MAX_VALUE`). Tugas suci mereka merajut realitas dan membangun monumen peradaban tidak dibatasi oleh kuota fana.
2. **Bebas Pajak Sewa Wilayah (Upkeep Tax Exemption):**
   Tarif harian dihitung `Rp 0.0/hari`. Wilayah Conclave dan staf dikecualikan dari pemotongan pajak dan tidak akan pernah mengalami masa tenggang (*grace period*) maupun penyitaan (*auto-unclaim*).
3. **Format Antarmuka:** Kuota di `/claim gui` dan `/claim list` menampilkan `"∞ (Tak Terbatas)"`.

### D. Flags Wilayah & Pembagian Peran Granular
- **Pengaturan Flag Mandiri (`/claim flag <flag> <nilai>`):**
  - `pvp`: On/Off (Duel antar-pemain di dalam klaim).
  - `mob_spawn`: On/Off (Mencegah monster agresif muncul di dalam base).
  - `fire_spread`: On/Off (Mencegah api menyebar dari lahar/petir).
  - `explosions`: On/Off (Mencegah kerusakan akibat ledakan creeper/tnt).
  - `greeting` & `farewell`: Judul sinematik saat melintasi batas klaim.
- **Hierarki Peran Warga (`/claim role <pemain> <peran>`):**
  - `OWNER`: Hak kepemilikan mutlak, penarikan dana, pengaturan flag, delegasi izin.
  - `MANAGER`: Pengelola yang berhak mengundang warga, mengubah flag, dan memantau brankas.
  - `BUILDER`: Berhak membangun, menaruh/menghancurkan blok, dan mengakses kontainer.
  - `VISITOR`: Hanya bisa melintas tanpa izin modifikasi blok atau kontainer.

### E. Integrasi Perang Pengepungan (Kingdom War Siege Mode)
- Wilayah klaim bernaung di bawah panji Kerajaan pemiliknya (`kingdom_id`).
- Saat status **Kingdom War** resmi aktif antar dua kerajaan, wilayah klaim musuh dapat diserbu (*Siege Mode*) jika pemilik atau anggota wilayah sedang online.
- Setelah masa perang selesai, wilayah otomatis kembali ke proteksi damai 100%.

---

## ⚖️ 15. Sistem Moderasi Otoritatif Terpusat (Centralized Ban Engine) & Integrasi Web Admin

Menghilangkan fragmentasi sistem penegakan disiplin antar-plugin dan mengintegrasikan moderasi in-game dengan Web Admin Azuriom:

### A. Perintah Otoritatif Terpadu (ApexsionsCore)
- Perintah aktif: `/ban`, `/tempban`, `/unban`, `/pardon`, `/banip`, `/unbanip`, `/checkban`, `/banlist`.
- **Eliminasi Redundansi EssentialsX:** Perintah moderasi EssentialsX (`ban`, `tempban`, `unban`, `pardon`, `banip`, `unbanip`, `kick`, `mute`, `unmute`) dinonaktifkan permanen pada `disabled-commands` di `Essentials/config.yml` serta dineutralkan oleh `EssentialsBanOverride`.
- **Socket-Level Pre-Login Gatekeeper (`AsyncPlayerPreLoginEvent`):** Pemeriksaan ban dijalankan pada soket jaringan sebelum AuthMe berjalan. Pemain ter-ban langsung diputus koneksinya dengan layar cinematic (*Disconnection Screen*), mencegah eksploitasi bypass password atau login evasion.
- **Arsitektur Failsafe Lazy Auto-Resolution:** `ClaimCommand` dan `BanCommand` menggunakan dynamic fallback `plugin.getClaimManager()` dan `plugin.getBanManager()`, mencegah risiko `NullPointerException` jika perintah dipanggil sebelum inisialisasi modul selesai.

### B. Dashboard Finansial & Teritorial Web Admin (`/admin/claims`)
- **5 Kartu Metrik Emas Gelap:** Total Chunks Terklaim, Wilayah Lunas/Aktif, Menunggak (Grace Period), Total Saldo Brankas Terkumpul, dan Total Pemilik Aktif.
- **Filter Komprehensif:** Pencarian instan berdasarkan nama pemain, UUID, dimensi dunia, status pembayaran (Lunas/Menunggak/Kedaluwarsa), dan afiliasi kerajaan.
- **Tindakan Admin:**
  - **Suntik Saldo (*Admin Deposit*):** Menyuntikkan dana darurat ke brankas klaim pemain via antrean `Delivery` console.
  - **Tagih Pajak Sekarang (*Force Collect Tax*):** Memaksa eksekusi penagihan pajak dan evaluasi masa tenggang manual dari web.
  - **Sync In-Game:** Memicu sinkronisasi data instan dari game server ke database web.
  - **Live BlueMap Link:** Tautan koordinat langsung ke penampil peta 3D.
- **Panel Moderasi Terpadu (`/admin/players/{id}`):** Tab Ban/Unban terpusat untuk menjatuhkan dan mencabut sanksi pemain langsung dari browser.

---

## 🛡️ 16. Sistem Keamanan Anti-Griefing, Anti-Xray Monitor & Redstone Watchdog Engine

ApexsionsCore dilengkapi sistem proteksi keamanan dan integritas server berlapis tinggi:

### A. Proteksi Anti-Griefing Wilayah Berdaulat (`ClaimProtectionListener`)
1. **Proteksi Blok & Kontainer:**
   - Pemain tanpa izin (`VISITOR` / non-anggota) dicegah 100% dari menaruh blok (`BlockPlaceEvent`), menghancurkan blok (`BlockBreakEvent`), serta mengakses kontainer (`CHEST`, `BARREL`, `SHULKER_BOX`, `HOPPER`, `FURNACE`, dll).
   - Tindakan ilegal dibatalkan dengan audio alert `BLOCK_CHEST_LOCKED` dan actionbar real-time ber-throttle (1,5 detik) agar tidak membebani network bandwidth.
2. **Proteksi Bencana Alam & Lingkungan:**
   - **Penyebaran Api:** Mencegah api melahap blok di dalam klaim jika flag `fire_spread` bernilai `false`.
   - **Ledakan Lingkungan:** Melindungi struktur dari ledakan Creeper, TNT liar, Wither, dan Respawn Anchor jika flag `explosions` bernilai `false`.
   - **Pencegahan Pencurian Kendaraan & Lukisan:** Melindungi Armor Stand, Item Frame, Lukisan, dan Minecart dari kerusakan oleh entitas non-izin.
3. **Pemisahan Kedaulatan & Wilderness:**
   - Wilayah alam liar (*Wilderness*) tetap bebas untuk dieksplorasi dan ditambang secara wajar, namun wilayah peradaban yang berdaulat terlindungi penuh.

### B. Anti-Xray Ore Mining Spike Tracker & Reach Gatekeeper (`AntiXrayListener`)
1. **Raytrace Reach Validation:**
   - Memvalidasi jarak interaksi blok pemain mode Survival/Adventure terhadap jarak mata (`getEyeLocation`).
   - Interaksi di atas jarak wajar ($> 5.8\text{ meter}$) dibatalkan seketika (`BlockBreakEvent.setCancelled(true)`) dengan notifikasi actionbar pencegahan reach-hack.
2. **Deteksi Anomali Penambangan Bijih Langka (Ore Spike):**
   - Memantau penambangan bijih krusial: `DIAMOND_ORE`, `DEEPSLATE_DIAMOND_ORE`, `ANCIENT_DEBRIS`, `EMERALD_ORE`, `DEEPSLATE_EMERALD_ORE`.
   - Melacak lonjakan penambangan dalam sliding window 60 detik. Jika pemain menambang $\ge 8$ bijih langka dalam tempo 60 detik, sistem otomatis mendeteksi anomali.
3. **Peringatan Staf Otomatis (Staff Alert Desk):**
   - Staf online (pemegang izin `apexsions.staff` atau OP) menerima siaran pesan alert merah tua bersuara lonceng (`BLOCK_NOTE_BLOCK_BELL`) yang mencantumkan nama pemain, jumlah bijih, durasi, serta koordinat persis $[X, Y, Z]$.
   - Dilengkapi cooldown peringatan 45 detik per pemain untuk mencegah spam log staf.

### C. Watchdog Osilasi Redstone Cepat Anti-Lag (`RedstoneWatchdogListener`)
1. **Pencegahan Mesin Lag (Lag Machine Suppression):**
   - Memantau frekuensi osilasi sinyal redstone (`BlockRedstoneEvent`) pada setiap koordinat blok menggunakan pelacak pulsa geser (*Sliding Pulse Tracker*).
2. **Ambang Batas Keamanan:**
   - Batas toleransi: Maksimal **25 pulsa per 2.000 milidetik (2 detik)**.
   - Jika sirkuit melebihi batas ini (mengindikasikan redstone clock ilegal atau loop laggy), watchdog otomatis membekukan sinyal dengan menetapkan `event.setNewCurrent(0)`.
3. **Visual & Audio Alert:**
   - Memunculkan partikel asap (`Particle.SMOKE`) dan suara pemadaman (`Sound.BLOCK_FIRE_EXTINGUISH`) di titik sumber clock.
   - Mengirim notifikasi actionbar ke seluruh pemain dalam radius 15 blok: *"⚠ Sirkuit redstone cepat dibekukan sementara demi menjaga kestabilan 20 TPS server."*

---

## 📱 17. Integrasi UI Lintas Platform (Bedrock Mobile & Java Custom Font Friendly)

Ekosistem Apexsions mengadopsi standar antarmuka lintas platform (Java & Bedrock Edition) yang dirancang khusus untuk kenyamanan visual:

### A. Bedrock Clean Scoreboard Server Pack (`Geyser-Spigot`)
1. **Peniadaan Angka Merah Klien Bedrock:**
   - Secara *hardcoded native*, Minecraft Bedrock Edition selalu merender kolom skor merah (`15, 14, 13...`) di sisi kanan sidebar.
   - Diatasi melalui Server Resource Pack otomatis (`ApexsionsCleanScoreboard.zip`) yang ditempatkan pada `plugins/Geyser-Spigot/packs/`.
   - Berkas `ui/scoreboards.json` menetapkan `"scoreboard_sidebar/main/lists/scores": { "ignored": true }`, sehingga antarmuka Bedrock melewatkan render kolom angka secara penuh tanpa perlu tindakan manual dari sisi pemain.

### B. Conditional Scoreboard Khusus Bedrock (`%bedrock%=true`)
1. **Ultra-Compact Mobile Layout:**
   - Menghindari pembengkakan lebar layar (sebelumnya mencapai 30%–40% layar sentuh ponsel akibat baris teks panjang dan 17 baris vertikal).
   - Template `scoreboard-bedrock` memangkas konten menjadi 8 baris esensial (Rank, Kerajaan, Saldo Rupiah, Level, Online, dan Server IP).
   - Membatasi panjang teks maksimal $\le 19\text{ karakter}$, menciutkan ukuran antarmuka menjadi hanya $\sim 10\%-12\%$ di pojok kanan atas layar ponsel tanpa menghalangi tombol kendali sentuh.

### C. Kompatibilitas Penuh Font Kustom Java (Custom Font Friendly)
1. **Pemberantasan Garis Pecah Unicode (`\u2500`):**
   - Karakter pembatas kotak Unicode `──────────` diubah menjadi karakter ASCII hyphen standar berkode coret (`&8&m------------------`).
   - Menghilangkan celah garis terputus-putus (*jagged gaps*), kotak tanda tanya (*missing glyphs*), maupun ketidaksejajaran baseline saat pemain menggunakan Resource Pack ber-font kustom (Faithful, Modern Font, TTF, dll).
2. **Penyelarasan Header & Brand:**
   - Mengganti sub-header menjadi format universal `>> PROFIL`, `>> EKONOMI`, `>> PROGRESI`.
   - Menghapus embel-embel "Kingdom" pada judul Tablist/Header (`APEXSIONS`) sesuai aturan identitas brand resmi.

---

## 👑 18. Spesifikasi Kanonikal Tiga Kerajaan Berdaulat (Buff, Debuff & Perks)

Spesifikasi atribut, persentase pajak wilayah, dan kondisi fisik ketiga kerajaan berdaulat telah disinkronkan 100% di seluruh komponen (`kingdoms.yml`, Java GUI, listener gameplay, dan web wiki):

### A. ☀️ Kerajaan Zenithar (Arah Timur / Puncak Cakrawala)
- **Afiliasi & Lore:** Penerus dinasti kerajaan dan korps pengawal kehormatan Kekaisaran Sions yang bertahan di puncak cakrawala (*Solarium Spire Citadel*).
- **Ibukota:** Solarium Spire Citadel `world (-3028, 64, -5597)`
- **Pajak Kas Wilayah:** `18.0%`
- **Buff Kanonikal:**
  - `+8% Speed Boost` (Kecepatan gerak meningkat).
  - `+15% Luck Boost` (Peningkatan peluang drop & gacha).
  - `[Royal Discipline] +6% All Damage` (Disiplin kavaleri istana).
  - `[Royal Aegis] 20% Reduksi Damage Masuk` (Peredam serangan fisik & proyektil).
  - Hak Istimewa Pasar: Diskon 30% Pajak Lelang & Bunga Simpanan Bank +25%.
  - Diskon 15% Blok Bangunan Megah di Toko Kerajaan.
- **Debuff Kanonikal:**
  - `+15% Kerentanan Racun & Wither` (Damage & durasi efek meningkat akibat gaya hidup higienis istana).
  - `+12% Cepat Lapar` (*Aristocratic Exhaustion*).
  - `-10% Kecepatan Menambang` (Bukan pekerja kasar tambang).
  - Biaya tempa anvil sedikit lebih mahal (+1 Level EXP).

### B. 🔥 Kerajaan Solterra (Arah Selatan / Kawah Vulkanik Cadas)
- **Afiliasi & Lore:** Persekutuan pesulap tempur agung (*Arcanists*) dan prajurit garis depan terkuat bekas legiun Sions yang menaklukkan alam vulkanik mematikan (*Ignis Bastion Fortress*).
- **Ibukota:** Ignis Bastion Fortress `world (-5843, 65, 889)`
- **Pajak Kas Wilayah:** `20.0%`
- **Buff Kanonikal:**
  - `[Battle Momentum] +15% Total Damage` (Kekuatan ofensif brutal).
  - `+10% Critical Damage` (Daya rusak serangan kritikal).
  - `+2% Defense` (Ketahanan tubuh terlatih perang).
  - `+10% Kecepatan Menambang` (Eksploitasi cadas vulkanik).
  - Rasio Jual Ore Tinggi (65% dari harga beli pasar).
- **Debuff Kanonikal:**
  - `-2 HP Maksimal (9 Hati)` (Tubuh rapuh terpapar panas ekstrem kawah).
  - `+8% Kerentanan Damage Masuk` (Gaya bertarung mengorbankan pertahanan diri).
  - `+7% Cepat Lapar` (Metabolisme tempur tinggi).
  - Lahan pertanian cepat mengering di tanah cadas.

### C. 🌿 Kerajaan Sylvamoor (Arah Barat / Rimba Kanopi Purba)
- **Afiliasi & Lore:** Kaum pembangun, petani lumbung, pekerja, dan prajurit garda rakyat yang hidup selaras menjaga kelestarian Pohon Dunia (*Eldergrove Sanctuary*).
- **Ibukota:** Eldergrove Sanctuary `world (-9666, 64, -4812)`
- **Pajak Kas Wilayah:** `15.0%`
- **Buff Kanonikal:**
  - `[Nature's Blessing] +2 HP Maksimal (11 Hati)` (Vitalitas alami kanopi purba).
  - `+12% Luck Boost` (Keberkahan alam rimba).
  - `+7% Extra Mob Drops` (Kelimpahan hasil buruan).
  - `Pertahanan Rimba (+15%)` saat berada di bioma hutan/rimba.
  - `[Forest Grace]` Efek racun otomatis berhenti saat darah tersisa 3 hati.
  - Kelembapan lahan pertanian abadi (tanah tidak pernah tandus).
- **Debuff Kanonikal:**
  - `Mabuk Ketinggian di Y > 110` (Efek Hunger & Weakness akibat terbiasa di bawah kanopi).
  - `+15% Kerentanan Kerusakan Terbakar Api`.
  - `-10% Kecepatan Menambang`.
  - `-10% Kerusakan PvP` & `-5% Kerusakan PvE` (Filosofi pasifis dan cinta kedamaian).

### D. ✦ The Aetherial Conclave (Dimensi Atas Aetherion)
- **Status:** Entitas kosmik non-fana pengawas semesta (Weight $\ge 80$, OP, dan jajaran staf).
- **Hak Istimewa:** Bebas permanen dari pajak wilayah, klaim chunk tanpa batas, serta isolasi dari bias konflik 3 kerajaan dunia fana.

---

## 🔐 19. Arsitektur Autentikasi Nir-Hambatan (Zero-Friction Auto-Login) & Proteksi Identitas Dual-Platform

Mengintegrasikan ekosistem **AuthMeReloaded (v6.0.1)**, **Floodgate (v2.2.5)**, dan **FastLogin (v1.12-kick-toggle)** pada runtime Paper 26.2 (Java 21 LTS) untuk menghadirkan pengalaman masuk server instan tanpa kompromi keamanan:

### A. Matriks Autentikasi 3-Tier
| Tipe Klien / Pemain | Metode Autentikasi | Interaksi Masuk Server | Status Keamanan & Enkripsi |
|---|---|---|---|
| **Java Premium Original** | Mojang Session Encryption via FastLogin | Ketik `/premium` 2× saat pertama kali (1× kick by-design). Selanjutnya **auto-login instan** tanpa password selamanya. | Handshake resmi Mojang via ProtocolLib v5.4.0. Anti-pembajakan 100%. |
| **Bedrock Edition (Mobile/Win10/Console)** | Xbox Live Authentication via Floodgate | Registrasi AuthMe (`/register`) **persis 1× seumur hidup**. Seterusnya **auto-login otomatis** via sesi Floodgate (`autoLoginFloodgate: true`). | Namespace terlindungi prefix `.` (tidak bisa dispoof Java). |
| **Java Crack (Non-Paid Launcher)** | Standar AuthMe Password Protection | Wajib memasukkan kata sandi AuthMe (`/register` & `/login`) setiap kali masuk ke server. | Password hash Argon2/BCrypt di AuthMe. Nol risiko pembajakan akun pemain lain. |

### B. Kebijakan Keamanan Konservatif (Nol Risiko Lockout Veteran)
1. **`autoRegister: false`:**
   - Menjamin bahwa pemain crack veteran yang memakai nickname original milik pemain luar **TIDAK PERNAH DIKUNCI** dengan password acak.
   - FastLogin tidak pernah secara sepihak mendaftarkan akun di AuthMe secara otomatis.
2. **`premiumUuid: false` (Integritas UUID Mutlak):**
   - Seluruh data pemain (saldo Rupiah, saldo Diamond, level karakter 1–100, klaim wilayah kerajaan, dan link profil publik web Azuriom `/player/{uuid}`) **tetap mengacu pada Offline UUID (`OfflinePlayer:<name>`)**.
   - Ketika pemain Java beralih menjadi Premium (`/premium`), UUID mereka tidak pernah berganti, sehingga 0% risiko data loss atau data terputus (*orphaned data*).
3. **`allowFloodgateNameConflict: false`:**
   - Mencegah konflik nama antara pemain Bedrock dan Java, serta memastikan namespace Bedrock selalu terisolasi via prefix `.`.

### C. Alur Konfirmasi Dua Tahap `/premium` & 1× Kick By-Design
1. Pemain Java Original login ke server menggunakan password AuthMe untuk terakhir kali.
2. Pemain mengetik `/premium` $\rightarrow$ Muncul peringatan resmi berbahasa Indonesia (`premium-warning: true`).
3. Pemain mengetik `/premium` kedua kali untuk mengonfirmasi $\rightarrow$ Server mengeluarkan pemain 1× (`kick-toggle: true`).
   * *Catatan Penting:* Pengeluaran ini adalah mekanisme wajib (*by-design*) agar pada sambungan berikutnya, ProtocolLib dapat menginisiasi enkripsi handshake langsung dengan server otentikasi Mojang.
4. Pemain masuk kembali $\rightarrow$ Auto-login aktif permanen.

### D. Isolasi Izin LuckPerms & Prosedur Pemulihan Akun Staf
1. **Isolasi Pemain Bedrock:**
   - Agar pemain Bedrock tidak sengaja memicu verifikasi sesi Java Mojang:
     ```powershell
     lp group default permission set fastlogin.bukkit.command.premium false context[origin=bedrock]
     ```
2. **Izin Pemulihan untuk Staf Tiket (Tier III & IV):**
   - Diberikan ke grup `warden` dan `overseer`:
     ```powershell
     lp group warden permission set fastlogin.bukkit.command.cracked true
     lp group overseer permission set fastlogin.bukkit.command.cracked true
     ```
3. **SOP Penanganan Salah Ketik / Akun Terkunci:**
   - Jika pemain launcher crack tidak sengaja mengetik `/premium` hingga terkunci (`invalid-session`):
     1. Pemain melapor via tiket Discord atau Staff Reports Desk.
     2. Staf memeriksa status akun via `%fastlogin_status%`.
     3. Staf mengeksekusi perintah: `/cracked <player>`.
     4. Status akun dikembalikan menjadi crack seketika di `FastLogin.db`, dan pemain dapat login kembali menggunakan kata sandi AuthMe lamanya tanpa kehilangan data apa pun.

### E. Resolusi Kompatibilitas Geyser 2.11.3 & Java 21 Verifier
- Rilis FastLogin upstream memanggil metode `GeyserImpl.getConfig()` lama yang tidak kompatibel dengan Geyser 2.11.3 (MC 26.2).
- Dilakukan penyesuaian biner terarah pada `FastLoginBukkit.class` agar inisialisasi melewati wrapper lama Geyser dan langsung mengaitkan **`FloodgateService` resmi (`FloodgateApi.getInstance()`)**.
- Penyesuaian ini mematuhi standar StackMapTable Java 21 LTS, menghasilkan proses inisialisasi boot yang 100% bersih tanpa `NoSuchMethodError` atau `VerifyError`.

### F. Lokalisasi Menyeluruh 20/20 Key (`plugins/FastLogin/messages.yml`)
Seluruh 20 string lokalisasi FastLogin telah diterjemahkan ke Bahasa Indonesia dengan standar visual dan warna resmi Apexsions (`&8[&6Apexsions&8]&r`, `&a`, `&c`, `&e`), termasuk pesan krusial `invalid-session` dan `premium-warning`.

---

## 🧠 20. Arsitektur Graphify Knowledge Graph & Navigasi Kode Berbasis Graf (Developer & AI Tooling)

Untuk mempercepat pemahaman arsitektur, mendeteksi *god nodes*, serta menghemat kuota token AI secara drastis, repositori Apexsions telah dilengkapi dengan **Persistent Knowledge Graph** bertenaga **Graphify**:

### A. Metrik & Ruang Lingkup Graf Pengetahuan
* **Total Simbol (Nodes):** 11.805 entitas (Kelas Java, Method, Interface, Service, Controller Web, Listener, dan Config Section).
* **Total Relasi (Edges):** 38.282 relasi pemanggilan (*calls*), implementasi (*implements*), referensi dependensi (*references*), dan keterkaitan modul.
* **Komunitas Modul (Clusters):** 524 cluster fungsional yang terdeteksi via algoritma modularitas Leiden/Louvain.
* **Berkas Sumber Terindeks:** 1.462 berkas kode di seluruh 9 plugin Minecraft, WebBridge Azuriom, dan konfigurasi inti.

### B. Lingkungan Runtime Perkakas
* **Python Baseline:** Python 3.12 LTS (Windows 64-bit).
* **Parser Engine:** Tree-Sitter dengan pustaka multi-bahasa lengkap (`tree-sitter-java`, `tree-sitter-php`, `tree-sitter-javascript`, `tree-sitter-typescript`, `tree-sitter-python`, dll).
* **Package CLI:** `graphifyy` (v0.9.63+).

### C. Panduan Penggunaan Harian untuk Pengembang & AI Agent
1. **Pencarian Konsep & Relasi Cepat (BFS Traversal):**
   ```bash
   graphify query "PlayerListener"
   graphify query "WebBridgeService"
   ```
2. **Menemukan Jalur Ketergantungan Terpendek (Shortest Path):**
   ```bash
   graphify path "PlayerListener" "RankProvisioner"
   ```
3. **Penjelasan Rinci Node/Simbol:**
   ```bash
   graphify explain "ApexsionsCorePlugin"
   ```
4. **Sinkronisasi Graf Pasca-Pengembangan Kode (Cepat & Bebas Biaya Token):**
   ```powershell
   graphify update .
   ```
   *Catatan:* Perintah ini hanya memproses berkas yang berubah berdasarkan hash SHA-256 dan mengekstrak AST secara lokal tanpa memanggil API LLM.
5. **Inspeksi Visual Interaktif (Browser):**
   ```powershell
   Start-Process "graphify-out/graph.html"
   ```
54: 
---

## 🏛️ 16. Sistem Navigasi Upper Realm, Portal Kosmik Conclave & Emulasi Mortal

Sistem ini didesain khusus untuk menyelesaikan tantangan operasional staf dan pengawas dimensi atas (*The Aetherial Conclave* / Bobot Rank $\ge 80$), di mana entitas Conclave sebelumnya dibatasi dari interaksi fana sehingga menyulitkan navigasi, perbaikan wilayah, pengujian fitur (*testing*), dan teleportasi ke ibukota mortal.

### A. Portal Navigasi Kosmik Conclave (`ConclaveNavigationGUI`)
- **Akses Otomatis:** Mengetik `/k`, `/kingdom`, atau `/region` oleh staf Upper Realm otomatis membuka antarmuka portal 27-slot bergaya kosmik (`#00f2fe` ke `#4facfe`), alih-alih menampilkan pesan error atau menu fana standar.
- **Fitur Tombol Terintegrasi:**
  - **Slot 4:** Teleportasi ke *The Aether Citadel / Spawn Lobby Utama*.
  - **Slot 10, 12, 14:** Ibukota *Zenithar*, *Solterra*, dan *Sylvamoor* dengan dual-action:
    - `[Klik Kiri]`: Teleportasi instan ke titik spawn resmi ibukota.
    - `[Klik Kanan]`: *Random Teleport (RTP)* langsung di wilayah kedaulatan kerajaan tersebut.
  - **Slot 16:** Teleportasi ke *Terra Interdicta* (Reruntuhan Kuno Sions).
  - **Slot 19:** *Smart Kingdom RTP Dispatcher* (mendeteksi teritori berdiri atau acak kerajaan mortal).
  - **Slot 22:** *Simulasi Warga Fana* (Mortal Incarnation Mode) dengan rotasi satu-klik: Zenithar $\rightarrow$ Solterra $\rightarrow$ Sylvamoor $\rightarrow$ Off.
  - **Slot 25:** Akses pintas ke *Master Admin Hub* (`/admin`).

### B. Perintah Teleportasi & RTP Terarah
1. **Teleportasi Ibukota:**
   - `/ac spawn <ZENITHAR|SOLTERRA|SYLVAMOOR|SIONS|LOBBY>`: Teleportasi admin langsung tanpa delay.
   - `/k spawn <ZENITHAR|SOLTERRA|SYLVAMOOR|SIONS|LOBBY>`: Perintah langsung yang dapat digunakan staf Conclave dengan tab-completion lengkap.
2. **Targeted & Smart RTP:**
   - `/ac rtp [ZENITHAR|SOLTERRA|SYLVAMOOR|WILD]`: Memicu teleportasi acak terarah di teritori kerajaan target.
   - `/rtp [kingdom]`: Mendukung argumen kerajaan khusus bagi staf Conclave dan admin.

### C. Mode Emulasi Warga Fana (`MortalEmulationManager`)
- **Tujuan:** Memungkinkan staf Conclave menguji toko (`ApexsionsShop`), buff/debuff kerajaan (`KingdomBuffManager`), chat faksi (`ApexsionsChat`), dan batas wilayah tanpa mengubah data permanen di database (`ranks.yml` dan `PlayerData` tetap murni Aetherion).
- **Perintah:**
  - `/ac emulate <ZENITHAR|SOLTERRA|SYLVAMOOR>`: Mengaktifkan mode penyamaran fana.
  - `/ac emulate off`: Mengakhiri simulasi dan kembali ke eksistensi murni Aetherion.
- **Pembersihan Otomatis:** Saat staf logout (`PlayerQuitEvent`), status emulasi otomatis dihapus untuk mencegah memory leak.

### D. Bypass Proteksi & Hak Khusus Conclave
1. **Combat Tag (15 Detik):** Staf Upper Realm dan pemegang izin `apexsionscore.admin.bypass.combat` otomatis bebas dari pembatasan combat tag saat teleportasi darurat.
2. **Teleportasi Dua Arah (TPA):** Essentials TPA antara staf Conclave dan pemain fana otomatis meloloskan pengecekan kesamaan faksi (`validateTpa`) dan batas teritorial.
3. **Klaim Tanah Kedutaan:** Saat staf Conclave mengklaim tanah mortal, chunk dicatat sebagai kedutaan `AETHERION` dengan tarif pajak upkeep bebas ($Rp0$), serta memiliki bypass interaksi (`canInteract`) dan pembangunan (`canBuild`) di seluruh teritori fana.
4. **Dual-Action di Admin GUI (`CoreAdminSubGUI`):**
   - `[Klik Kiri]`: Teleportasi langsung ke ibukota kerajaan.
   - `[Shift + Klik Kanan]`: Menetapkan ulang titik spawn ibukota kerajaan pada koordinat admin saat ini.



