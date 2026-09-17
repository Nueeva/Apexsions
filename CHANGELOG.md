# CHANGELOG.md — Apexsions Development & Evolution History
# Apexsions — The Peak Civilizations

> **Repository:** `Nueeva/Apexsions`  
> **Brand Name:** `Apexsions` (DILARANG menambahkan kata Network/SMP/Kingdom)  
> **Tagline:** `The Peak Civilizations`  
> **Standar Riwayat:** Commit-Based Reverse-Chronological Changelog (95 Commits)  
> **Format:** Berdasarkan standar [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

Dokumen ini mendokumentasikan **seluruh 95 riwayat commit** repositori secara mendalam, terperinci, dan terstruktur ke dalam 5 milestone pengembangan. Dokumen ini dirancang sebagai referensi tunggal bagi developer dan AI Coding Agent untuk memahami riwayat arsitektur, modul yang tersentuh, serta evolusi fitur.

---

## 🤖 Panduan Alih-Tugas untuk AI Coding Agent & Pengembang Baru (Quick Handoff)

Bagi AI Agent atau developer yang melanjutkan pekerjaan di repositori ini, perhatikan kontrak operasional berikut:

1. **Brand Identity Mandate:**
   - Nama server adalah strictly **`Apexsions`**.
   - Dilarang keras menambahkan akhiran seperti "Network", "Kingdom", "SMP" pada teks UI, chat, nama kelas, atau dokumentasi.
2. **Kanon Tiga Kerajaan Berdaulat & Upper Dimension:**
   - **☀️ Zenithar (Timur):** Ibukota *Solarium Spire Citadel*, Pajak Kas `18.0%`. Spesialisasi: Kecepatan (+8%), Keberuntungan (+15%), [Royal Discipline] (+6% Damage), [Royal Aegis] (20% Reduksi Damage Masuk), Diskon Lelang 30%, Bunga Bank +25%, Diskon Blok Toko 15%. Debuff: Racun/Wither +15%, Aristocratic Exhaustion (+12% Cepat Lapar), Mining -10%, Anvil repair +1 EXP.
   - **🔥 Solterra (Selatan):** Ibukota *Ignis Bastion Fortress*, Pajak Kas `20.0%`. Spesialisasi: Penyerang Agresif, [Battle Momentum] (+15% Damage), (+10% Crit Damage), (+2% Defense), (+10% Mining Speed), Rasio Jual Ore Tinggi (65%). Debuff: -2 HP (9 Hati), Vulnerability (+8% Damage Masuk), Cepat Lapar (+7%), Lahan Cepat Kering.
   - **🌿 Sylvamoor (Barat):** Ibukota *Eldergrove Sanctuary*, Pajak Kas `15.0%`. Spesialisasi: Ketahanan Fisik, [Nature's Blessing] (+2 HP / 11 Hati), (+12% Luck), (+7% Mob Drops), Pertahanan Rimba (+15%), [Forest Grace] Racun Stop di 3 Hati, Lahan Abadi Basah. Debuff: Mabuk Ketinggian di Y > 110 (Hunger/Weakness), Kerentanan Terbakar Api (+15%), Mining -10%, Serangan PvP -10% & PvE -5%.
   - **✦ Dimensi Atas Aetherion (`The Aetherial Conclave`):** Seluruh staf (Weight $\ge 80$), operator (OP), dan entitas kosmik tidak terikat pada sistem 3 kerajaan fana, bebas pajak wilayah, dan memiliki klaim tanah tak terbatas.
3. **Standar Kompilasi & Build:**
   - Build spesifik per-plugin: `powershell -ExecutionPolicy Bypass -File .\Minecraft\build.ps1 <PluginName>` (e.g. `Core`, `Chat`, `Fishing`).
   - Hindari full build `-all` kecuali terdapat perubahan arsitektur global pada parent pom.
4. **Deploy & Sinkronisasi Langsung:**
   - **Game Server (SFTP):** `falcon04.jagoanhosting.id:2022` (SFTP via `ssh2`).
   - **Web Server VPS (Azuriom):** `89.144.53.100:22` (SSH via `ssh2`).
   - **WebBridge Deliveries:** Perintah in-game dari web atau script dijalankan via antrean tabel database `deliveries` (`action_id`, `command`, `status: PENDING`).
5. **Kebijakan Git & Autonomous Push:**
   - Selalu lakukan validasi lokal sebelum commit.
   - Begitu build berhasil dan di-commit, **otomatis push ke `origin/main`** tanpa menunggu instruksi manual.

---

## 🚀 Sprint 5 — Kedaulatan Teritorial, Watchdog Keamanan, Sinkronisasi Kanonikal Kerajaan & Optimasi Lintas Platform (Bedrock/Custom Font) [v1.2.0]
> **Periode Pengembangan:** 16 – 17 September 2026 | **Total Commit:** 10 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Fase stabilisasi dan penegakan kedaulatan wilayah. Mengimplementasikan sistem klaim tanah chunks (`/claim`) berbasis sewa progresif dan brankas wilayah, engine moderasi mandiri terpusat (`/ban`), sistem watchdog keamanan berlapis (Anti-Griefing, Anti-Xray spike alert, Redstone Watchdog anti-lag), sinkronisasi 100% spesifikasi kanonikal Tiga Kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%), serta penyediaan resource pack Geyser pembersih angka merah dan font kustom ASCII untuk Bedrock & Java.

### 🔍 Rincian Lengkap Commit (10 Commit)

#### `b360e85` — 2026-09-17 | 📚 DOCS | docs: create comprehensive CHANGELOG and synchronize master documentation with canonical kingdom specs
- **Pesan Commit:** `docs: create comprehensive CHANGELOG and synchronize master documentation with canonical kingdom specs`
  - **Komponen/Berkas:** `CHANGELOG.md`, `DOKUMENTASI.md`, `README.md`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `055bf40` — 2026-09-17 | ✨ FEAT | feat(core): synchronize canonical kingdom buffs, debuffs, and perks across GUIs, configs, and web wiki
- **Pesan Commit:** `feat(core): synchronize canonical kingdom buffs, debuffs, and perks across GUIs, configs, and web wiki`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../core/region/gui/KingdomConfirmGUI.java`, `Website/themes/apexsions/assets/js/app.js`
  - **Rincian Teknis:** Menyelaraskan nilai atribut kanonikal kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%) dan kartu preview slot 13 pada GUI konfirmasi `/k choose`.

#### `48bedec` — 2026-09-17 | ✨ FEAT | feat(ui): add bedrock clean scoreboard pack and optimize tab/scoreboard for bedrock & custom fonts
- **Pesan Commit:** `feat(ui): add bedrock clean scoreboard pack and optimize tab/scoreboard for bedrock & custom fonts`
  - **Komponen/Berkas:** `DOKUMENTASI.md`, `Minecraft/config/tab/config.yml`, `.../packs/ApexsionsCleanScoreboard/manifest.json`, `.../packs/ApexsionsCleanScoreboard/pack_icon.png`, `.../ApexsionsCleanScoreboard/texts/en_US.lang`, `.../ApexsionsCleanScoreboard/texts/languages.json`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `613e6c6` — 2026-09-16 | 🐛 FIX | fix(core): ensure table columns are added before creating indexes to prevent sqlite startup failure
- **Pesan Commit:** `fix(core): ensure table columns are added before creating indexes to prevent sqlite startup failure`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../apexsions/core/database/DatabaseManager.java`
  - **Rincian Teknis:** Memastikan migrasi skema database mengeksekusi DDL `ALTER TABLE ADD COLUMN` sebelum perintah `CREATE INDEX` untuk mencegah crash startup SQLite.

#### `83d783b` — 2026-09-16 | 📚 DOCS | docs: synchronize sovereign claims, security watchdog, centralized moderation, and fishing docs
- **Pesan Commit:** `docs: synchronize sovereign claims, security watchdog, centralized moderation, and fishing docs`
  - **Komponen/Berkas:** `DOKUMENTASI.md`, `README.md`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Mengaktifkan reach-hack check (>5.8m), deteksi lonjakan penambangan bijih langka (60s window), serta isolasi otomatis clock redstone cepat (>25 pulsa/2s).
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.

#### `11b1bd8` — 2026-09-16 | 🐛 FIX | fix(core): resolve NPE in ban and claim commands and grant unlimited claims and tax exemption to Upper Dimension ranks
- **Pesan Commit:** `fix(core): resolve NPE in ban and claim commands and grant unlimited claims and tax exemption to Upper Dimension ranks`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../com/apexsions/core/claim/ClaimCommand.java`, `.../com/apexsions/core/claim/ClaimManager.java`, `.../com/apexsions/core/claim/gui/ClaimGUI.java`, `.../com/apexsions/core/moderation/BanCommand.java`
  - **Rincian Teknis:** Menerapkan fallback lazy-lookup pada manager instance agar sub-command console/WebBridge tidak mengalami NullPointerException saat bootstrap belum tuntas.
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.

#### `848795d` — 2026-09-16 | ✨ FEAT | feat(fishing): implement dialog GUI for rod creator, virtual bait quota system, and balanced junk loot
- **Pesan Commit:** `feat(fishing): implement dialog GUI for rod creator, virtual bait quota system, and balanced junk loot`
  - **Komponen/Berkas:** `.../ApexsionsFishing/ApexsionsFishing-1.0.0.jar`, `.../com/apexsions/fishing/command/FishCommand.java`, `.../com/apexsions/fishing/gui/BaitShopGUI.java`, `.../apexsions/fishing/gui/FishingGUIListener.java`, `.../java/com/apexsions/fishing/gui/RodShopGUI.java`, `.../fishing/gui/admin/AdminRodCreatorGUI.java`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.

#### `e5b0634` — 2026-09-16 | ✨ FEAT | feat(claim): implement progressive territory tax, grace periods, flags, and web admin controls
- **Pesan Commit:** `feat(claim): implement progressive territory tax, grace periods, flags, and web admin controls`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../java/com/apexsions/core/claim/ClaimChunk.java`, `.../com/apexsions/core/claim/ClaimCommand.java`, `.../com/apexsions/core/claim/ClaimManager.java`, `.../core/claim/ClaimProtectionListener.java`, `.../com/apexsions/core/claim/ClaimRepository.java`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `2a3a538` — 2026-09-16 | ✨ FEAT | feat(moderation): integrate centralized ban engine and land claims web admin
- **Pesan Commit:** `feat(moderation): integrate centralized ban engine and land claims web admin`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../com/apexsions/core/claim/ClaimCommand.java`, `.../com/apexsions/core/claim/ClaimManager.java`, `.../apexsions/core/database/DatabaseManager.java`, `.../core/integration/web/WebBridgeService.java`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `1b905de` — 2026-09-16 | ✨ FEAT | feat(core): implement sovereign land claims, anti-griefing protection, anti-xray monitor, and redstone watchdog
- **Pesan Commit:** `feat(core): implement sovereign land claims, anti-griefing protection, anti-xray monitor, and redstone watchdog`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `Minecraft/plugins/ApexsionsCore/DOKUMENTASI.md`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../java/com/apexsions/core/claim/ClaimChunk.java`, `.../com/apexsions/core/claim/ClaimCommand.java`, `.../com/apexsions/core/claim/ClaimManager.java`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengaktifkan reach-hack check (>5.8m), deteksi lonjakan penambangan bijih langka (60s window), serta isolasi otomatis clock redstone cepat (>25 pulsa/2s).

---

## 🚀 Sprint 4 — Ekosistem ApexsionsFishing v1.0.0, Pengerasan Lifecycle AFK, Standarisasi Lore Enchant & Kebijakan Leaderboard 6-Lapis [v1.1.5]
> **Periode Pengembangan:** 14 – 15 September 2026 | **Total Commit:** 8 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Penyempurnaan modul perikanan interaktif dan tata kelola transparansi kompetisi. Memperkenalkan sistem pancing AFK & Active Reel Engine, Fishing Vault 54-slot, rod builder native dialog, Virtual Bait Quota, penataan tata letak lore custom enchant di bawah item level requirement, penyaringan 6-lapis seluruh staf/admin dari leaderboard publik game dan web, serta optimasi SEO sitemap.xml.

### 🔍 Rincian Lengkap Commit (8 Commit)

#### `00ebb82` — 2026-09-15 | 📚 DOCS | docs: synchronize documentation with 9-plugin suite, leaderboard exemptions, and SEO architecture
- **Pesan Commit:** `docs: synchronize documentation with 9-plugin suite, leaderboard exemptions, and SEO architecture`
  - **Komponen/Berkas:** `DOKUMENTASI.md`, `GEMINI.md`, `Minecraft/DOKUMENTASI.md`, `Minecraft/plugins/ApexsionsFishing/DOKUMENTASI.md`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `b408b84` — 2026-09-15 | 🐛 FIX | fix(fishing): harden AFK lifecycle, liquid depth check, rod break cleanup, and vault deposit safeguards
- **Pesan Commit:** `fix(fishing): harden AFK lifecycle, liquid depth check, rod break cleanup, and vault deposit safeguards`
  - **Komponen/Berkas:** `.../ApexsionsFishing/ApexsionsFishing-1.0.0.jar`, `.../com/apexsions/fishing/gui/FishingVaultGUI.java`, `.../fishing/gui/admin/AdminRodCreatorGUI.java`, `.../fishing/listener/FishingListener.java`, `.../fishing/service/AFKFishingService.java`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.

#### `f4448e4` — 2026-09-15 | 🐛 FIX | fix(fishing): harden AFK auto-recast, water validation, slot indexing, and exploit protections
- **Pesan Commit:** `fix(fishing): harden AFK auto-recast, water validation, slot indexing, and exploit protections`
  - **Komponen/Berkas:** `.../ApexsionsFishing/ApexsionsFishing-1.0.0.jar`, `.../com/apexsions/fishing/gui/FishSellGUI.java`, `.../com/apexsions/fishing/gui/FishingVaultGUI.java`, `.../com/apexsions/fishing/gui/VaultShopGUI.java`, `.../fishing/gui/admin/AdminRodCreatorGUI.java`, `.../fishing/listener/FishingListener.java`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.

#### `6d6b14d` — 2026-09-15 | 🐛 FIX | fix(seo): standardize sitemap.xml structure and update lastmod timestamps
- **Pesan Commit:** `fix(seo): standardize sitemap.xml structure and update lastmod timestamps`
  - **Komponen/Berkas:** `Website/public/sitemap.xml`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `6560fa6` — 2026-09-15 | ⏪ REVERT | revert(web): remove battlepass leaderboard from web platform
- **Pesan Commit:** `revert(web): remove battlepass leaderboard from web platform`
  - **Komponen/Berkas:** `.../resources/views/leaderboard.blade.php`, `.../src/Controllers/LeaderboardController.php`, `Website/themes/apexsions/assets/js/app.js`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.

#### `ece15e7` — 2026-09-14 | ✨ FEAT | feat(leaderboard): exclude admin, OP, and upper-dimension entities from web and game leaderboards
- **Pesan Commit:** `feat(leaderboard): exclude admin, OP, and upper-dimension entities from web and game leaderboards`
  - **Komponen/Berkas:** `.../ApexsionsBattlepass-1.0.0.jar`, `.../leaderboard/BattlePassLeaderboardService.java`, `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../apexsions/chat/integration/LuckPermsHook.java`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../apexsions/core/api/ApexsionsCoreAPIImpl.java`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `9790803` — 2026-09-14 | ✨ FEAT | feat(fishing): implement ApexsionsFishing ecosystem and standardize enchant lore layout
- **Pesan Commit:** `feat(fishing): implement ApexsionsFishing ecosystem and standardize enchant lore layout`
  - **Komponen/Berkas:** `Minecraft/build.ps1`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../apexsions/core/gui/admin/MasterAdminGUI.java`, `.../ApexsionsCustomEnchants-1.0.0.jar`, `.../enchant/EnchantmentRegistry.java`, `.../customenchants/gui/AdminItemCreatorGUI.java`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `f21e85e` — 2026-09-14 | ✨ FEAT | feat(balance): integrate Aetherion chat and rebalance Zenithar and Sylvamoor traits
- **Pesan Commit:** `feat(balance): integrate Aetherion chat and rebalance Zenithar and Sylvamoor traits`
  - **Komponen/Berkas:** `LORE.md`, `Minecraft/DOKUMENTASI.md`, `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../com/apexsions/chat/chat/ChatFormatter.java`, `.../java/com/apexsions/chat/chat/ChatListener.java`, `.../com/apexsions/chat/gui/SocialProfileGUI.java`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.

---

## 🚀 Sprint 3 — The Aetherial Conclave, RPG Stat Progression Normalizer, Terra Interdicta Hourly Reset & Custom Vanish Suite [v1.1.0]
> **Periode Pengembangan:** 12 September 2026 | **Total Commit:** 34 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Restrukturisasi kosmik semesta Apexsions dan normalisasi pertarungan RPG. Mengintegrasikan konsep Dimensi Atas (The Aetherial Conclave) yang membebaskan staf dari friksi kerajaan fana, sistem scaling stat level 1-100 dengan Combat Normalizer, zona reruntuhan kuno Terra Interdicta dengan reset temporal berkala, custom stealth `/vanish` tanpa hitbox, BlueMap auto-respawn ibukota teritorial, serta penyusunan master bible LORE.md.

### 🔍 Rincian Lengkap Commit (34 Commit)

#### `7603de0` — 2026-09-12 | 🐛 FIX | fix(admin): remove duplicate text brand name and use image-only ornate logo
- **Pesan Commit:** `fix(admin): remove duplicate text brand name and use image-only ornate logo`
  - **Komponen/Berkas:** `Website/resources/views/admin/layouts/admin.blade.php`, `Website/themes/apexsions/assets/css/admin-apexsions.css`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `74f597d` — 2026-09-12 | 📚 DOCS | docs: document web platform & admin panel lore alignment and Conclave fallbacks
- **Pesan Commit:** `docs: document web platform & admin panel lore alignment and Conclave fallbacks`
  - **Komponen/Berkas:** `DOKUMENTASI.md`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `39aa182` — 2026-09-12 | ✨ FEAT | feat(web): lore-accurate kingdom governance, Aetherion Conclave support, and admin panel fallbacks
- **Pesan Commit:** `feat(web): lore-accurate kingdom governance, Aetherion Conclave support, and admin panel fallbacks`
  - **Komponen/Berkas:** `.../resources/views/admin/players/index.blade.php`, `.../resources/views/admin/players/show.blade.php`, `.../resources/views/public-profile.blade.php`, `.../Controllers/Admin/PlayerAdminController.php`, `.../src/Controllers/Api/PlayerSyncController.php`, `Website/themes/apexsions/assets/js/app.js`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `1732eb9` — 2026-09-12 | 📚 DOCS | docs: update documentation for The Aetherial Conclave, Terra Interdicta, and Spawn Sanctum
- **Pesan Commit:** `docs: update documentation for The Aetherial Conclave, Terra Interdicta, and Spawn Sanctum`
  - **Komponen/Berkas:** `DOKUMENTASI.md`, `Minecraft/plugins/ApexsionsCore/DOKUMENTASI.md`, `README.md`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.

#### `604d054` — 2026-09-12 | ✨ FEAT | feat(core): implement Conclave Upper Dimension mechanics and fix lore inaccuracies across commands & GUIs
- **Pesan Commit:** `feat(core): implement Conclave Upper Dimension mechanics and fix lore inaccuracies across commands & GUIs`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/command/AdminCommand.java`, `.../com/apexsions/core/command/KingdomCommand.java`, `.../com/apexsions/core/command/LobbyCommand.java`, `.../com/apexsions/core/command/SionsCommand.java`, `.../core/gui/admin/PlayerInspectorGUI.java`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `d6e3aaf` — 2026-09-12 | 🐛 FIX | fix(vanish): hide vanished players from online player counters, staff placeholders, TAB, and server ping
- **Pesan Commit:** `fix(vanish): hide vanished players from online player counters, staff placeholders, TAB, and server ping`
  - **Komponen/Berkas:** `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../com/apexsions/chat/chat/MentionParser.java`, `.../apexsions/chat/command/RealNameCommand.java`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../apexsions/core/gui/admin/MasterAdminGUI.java`, `.../core/integration/PlaceholderApiHook.java`
  - **Rincian Teknis:** Menyediakan custom stealth vanish dengan penghilangan hitbox, penolakan targeting mob, serta penyembunyian dari tablist, ping, dan placeholder.

#### `4035c5e` — 2026-09-12 | ✨ FEAT | feat: exclude server staff and admins from all plugin leaderboards
- **Pesan Commit:** `feat: exclude server staff and admins from all plugin leaderboards`
  - **Komponen/Berkas:** `.../ApexsionsBattlepass-1.0.0.jar`, `.../gui/main/BattlePassLeaderboardMenu.java`, `.../leaderboard/BattlePassLeaderboardService.java`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/api/ApexsionsCoreAPI.java`, `.../apexsions/core/api/ApexsionsCoreAPIImpl.java`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `e8936ec` — 2026-09-12 | 🐛 FIX | fix(chat): guard ApexsionsCoreHook with isolated CoreBridge to prevent NoClassDefFoundError
- **Pesan Commit:** `fix(chat): guard ApexsionsCoreHook with isolated CoreBridge to prevent NoClassDefFoundError`
  - **Komponen/Berkas:** `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../com/apexsions/chat/channel/KingdomChannel.java`, `.../com/apexsions/chat/chat/ChatFormatter.java`, `.../com/apexsions/chat/gui/SocialProfileGUI.java`, `.../chat/integration/ApexsionsCoreHook.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `78dd65c` — 2026-09-12 | 🐛 FIX | fix(core): restrict playable kingdoms to Zenithar, Solterra, and Sylvamoor, blocking fallen Sions from admin commands
- **Pesan Commit:** `fix(core): restrict playable kingdoms to Zenithar, Solterra, and Sylvamoor, blocking fallen Sions from admin commands`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/command/AdminCommand.java`, `.../com/apexsions/core/command/KingdomCommand.java`, `.../java/com/apexsions/core/region/Region.java`, `.../com/apexsions/core/region/RegionManager.java`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `6f52105` — 2026-09-12 | 📚 DOCS | docs: synchronize master documentation with RPG progression and combat engine
- **Pesan Commit:** `docs: synchronize master documentation with RPG progression and combat engine`
  - **Komponen/Berkas:** `DOKUMENTASI.md`, `LORE.md`, `README.md`
  - **Rincian Teknis:** Mengimplementasikan formula normalisasi stat tempur, atribut progression level 1-100, dan peredaman lonjakan damage berlebih.

#### `88ed136` — 2026-09-12 | ✨ FEAT | feat(core): implement RPG level progression stat scaling and combat normalizer
- **Pesan Commit:** `feat(core): implement RPG level progression stat scaling and combat normalizer`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../combat/PlayerCombatProgressionListener.java`, `.../core/combat/SmartCombatNormalizer.java`, `.../core/level/stat/PlayerAttributeService.java`, `.../core/level/stat/PlayerProgressionStats.java`
  - **Rincian Teknis:** Mengimplementasikan formula normalisasi stat tempur, atribut progression level 1-100, dan peredaman lonjakan damage berlebih.

#### `c9275f1` — 2026-09-12 | 🐛 FIX | fix(cosmetics): avoid synthetic switch class in CosmeticsMainGUI
- **Pesan Commit:** `fix(cosmetics): avoid synthetic switch class in CosmeticsMainGUI`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../core/cosmetics/gui/CosmeticsMainGUI.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `29557d0` — 2026-09-12 | 🐛 FIX | fix(core): preload caffeine tasks at startup and protect block place tracker
- **Pesan Commit:** `fix(core): preload caffeine tasks at startup and protect block place tracker`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../level/xp/antiabuse/BlockPlacementTracker.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `640e566` — 2026-09-12 | ✨ FEAT | feat(core): implement custom /vanish system with fake broadcast, no-hitbox, and solid self-visibility
- **Pesan Commit:** `feat(core): implement custom /vanish system with fake broadcast, no-hitbox, and solid self-visibility`
  - **Komponen/Berkas:** `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../java/com/apexsions/chat/chat/ChatListener.java`, `.../chat/integration/ApexsionsCoreHook.java`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../com/apexsions/core/api/ApexsionsCoreAPI.java`
  - **Rincian Teknis:** Menyediakan custom stealth vanish dengan penghilangan hitbox, penolakan targeting mob, serta penyembunyian dari tablist, ping, dan placeholder.

#### `1383772` — 2026-09-12 | ✨ FEAT | feat(core): add /k navigation gui and allow rtp from lobby
- **Pesan Commit:** `feat(core): add /k navigation gui and allow rtp from lobby`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../com/apexsions/core/command/KingdomCommand.java`, `.../apexsions/core/region/KingdomRtpService.java`, `.../core/region/gui/KingdomNavigationGUI.java`, `.../core/region/gui/KingdomProfileGUI.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ae9f9a7` — 2026-09-12 | 🐛 FIX | fix(nginx): eliminate ERR_TOO_MANY_REDIRECTS loop on /vote by adjusting regex quantifier and protocol target
- **Pesan Commit:** `fix(nginx): eliminate ERR_TOO_MANY_REDIRECTS loop on /vote by adjusting regex quantifier and protocol target`
  - **Komponen/Berkas:** `Website/deploy/nginx-azuriom.conf`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `027b8d4` — 2026-09-12 | 🐛 FIX | fix(core): automatically bypass Paper 128 channel registration limit
- **Pesan Commit:** `fix(core): automatically bypass Paper 128 channel registration limit`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `1e79376` — 2026-09-12 | 🐛 FIX | fix(seo): add lastmod dates to sitemap and explicit nginx location with X-Robots-Tag
- **Pesan Commit:** `fix(seo): add lastmod dates to sitemap and explicit nginx location with X-Robots-Tag`
  - **Komponen/Berkas:** `Website/deploy/nginx-azuriom.conf`, `Website/public/sitemap.xml`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `b4c173b` — 2026-09-12 | 🐛 FIX | fix(core): eager preload Caffeine RemovalCause and add defensive cache invalidation fallbacks
- **Pesan Commit:** `fix(core): eager preload Caffeine RemovalCause and add defensive cache invalidation fallbacks`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../java/com/apexsions/core/cache/PlayerCache.java`, `.../level/xp/antiabuse/BlockPlacementTracker.java`, `.../ApexsionsMedia/ApexsionsMedia-1.0.0.jar`, `.../com/apexsions/media/engine/ImageRenderer.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `d57c255` — 2026-09-12 | 🐛 FIX | fix(seo): enforce authoritative HTTPS canonicals, resolve rogue domain indexing, and optimize SEO entity schemas
- **Pesan Commit:** `fix(seo): enforce authoritative HTTPS canonicals, resolve rogue domain indexing, and optimize SEO entity schemas`
  - **Komponen/Berkas:** `Website/deploy/nginx-azuriom.conf`, `Website/deploy/setup-vps.sh`, `Website/public/robots.txt`, `Website/public/sitemap.xml`, `.../themes/apexsions/views/layouts/app.blade.php`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `7d67610` — 2026-09-12 | 📚 DOCS | docs: synchronize README, technical documentation, and Admin GUIs with Zenithar rebalance
- **Pesan Commit:** `docs: synchronize README, technical documentation, and Admin GUIs with Zenithar rebalance`
  - **Komponen/Berkas:** `Minecraft/DOKUMENTASI.md`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../apexsions/core/gui/admin/ShopAdminSubGUI.java`, `.../plugins/ApexsionsShop/ApexsionsShop-1.0.0.jar`, `.../shop/gui/AdminKingdomShopSelectorGUI.java`, `README.md`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `581328e` — 2026-09-12 | ✨ FEAT | feat(balance): rebalance Zenithar as Capitalist & Anti-Crit Duelist across plugins and website
- **Pesan Commit:** `feat(balance): rebalance Zenithar as Capitalist & Anti-Crit Duelist across plugins and website`
  - **Komponen/Berkas:** `LORE.md`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../core/kingdom/KingdomBuffListener.java`, `.../apexsions/core/kingdom/KingdomBuffManager.java`, `.../src/main/resources/kingdoms/kingdoms.yml`, `.../ApexsionsEconomy/ApexsionsEconomy-1.0.0.jar`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2fb659c` — 2026-09-12 | ✨ FEAT | feat(gameplay): integrate lore with kingdom treasury, mythicmobs boss, and website wiki
- **Pesan Commit:** `feat(gameplay): integrate lore with kingdom treasury, mythicmobs boss, and website wiki`
  - **Komponen/Berkas:** `.../mythicmobs/droptables/ExampleDropTables.yml`, `Minecraft/config/mythicmobs/items/ExampleItems.yml`, `Minecraft/config/mythicmobs/items/sions_items.yml`, `Minecraft/config/mythicmobs/items/sions_keys.yml`, `.../mythicmobs/items/sions_legendary_items.yml`, `Minecraft/config/mythicmobs/mobs/ExampleMobs.yml`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `5527607` — 2026-09-12 | ✨ FEAT | feat(vote): implement dual action vote buttons and fix trailing parenthesis 404 URL
- **Pesan Commit:** `feat(vote): implement dual action vote buttons and fix trailing parenthesis 404 URL`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/command/VoteCommand.java`, `.../core/gui/input/BedrockFormAdapter.java`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `61ed330` — 2026-09-12 | 🔧 CHORE | chore(build): update ApexsionsCustomEnchants artifact
- **Pesan Commit:** `chore(build): update ApexsionsCustomEnchants artifact`
  - **Komponen/Berkas:** `.../ApexsionsCustomEnchants-1.0.0.jar`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `9a013e6` — 2026-09-12 | 🐛 FIX | fix(customenchants): display item level requirement between enchant list and set bonus in lore
- **Pesan Commit:** `fix(customenchants): display item level requirement between enchant list and set bonus in lore`
  - **Komponen/Berkas:** `.../ApexsionsCustomEnchants-1.0.0.jar`, `.../enchant/EnchantmentRegistry.java`, `.../customenchants/gui/AdminItemCreatorGUI.java`, `.../customenchants/gui/ArmorSetBonusPickerGUI.java`, `.../customenchants/gui/ToolBonusPickerGUI.java`, `.../gui/dialog/ItemEditDialogFlow.java`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `42f3092` — 2026-09-12 | 📚 DOCS | docs: integrate political succession, kingdom stability index, mystery fragments, and staff protocols into LORE.md
- **Pesan Commit:** `docs: integrate political succession, kingdom stability index, mystery fragments, and staff protocols into LORE.md`
  - **Komponen/Berkas:** `LORE.md`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `ce8948b` — 2026-09-12 | 📚 DOCS | docs: integrate Aetherial Conclave, Order vs Chaos, and 3-month seasonal chronicle into LORE.md
- **Pesan Commit:** `docs: integrate Aetherial Conclave, Order vs Chaos, and 3-month seasonal chronicle into LORE.md`
  - **Komponen/Berkas:** `LORE.md`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `052e2a7` — 2026-09-12 | 📚 DOCS | docs: establish tri-layered cosmic lore architecture with Aetherion and Celestial Order in LORE.md
- **Pesan Commit:** `docs: establish tri-layered cosmic lore architecture with Aetherion and Celestial Order in LORE.md`
  - **Komponen/Berkas:** `LORE.md`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `a3741c8` — 2026-09-12 | 📚 DOCS | docs: add technical architecture mapping, system audit, and revision checklist to LORE.md
- **Pesan Commit:** `docs: add technical architecture mapping, system audit, and revision checklist to LORE.md`
  - **Komponen/Berkas:** `LORE.md`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `4b707ed` — 2026-09-12 | 📚 DOCS | docs: enrich LORE.md with comprehensive realm geography, buff/debuff tables, and exclusive Sions admin realm
- **Pesan Commit:** `docs: enrich LORE.md with comprehensive realm geography, buff/debuff tables, and exclusive Sions admin realm`
  - **Komponen/Berkas:** `LORE.md`
  - **Rincian Teknis:** Menyelaraskan nilai atribut kanonikal kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%) dan kartu preview slot 13 pada GUI konfirmasi `/k choose`.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `4dfc613` — 2026-09-12 | 📚 DOCS | docs: create official civilization lore document LORE.md
- **Pesan Commit:** `docs: create official civilization lore document LORE.md`
  - **Komponen/Berkas:** `LORE.md`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `2d0a065` — 2026-09-12 | 🐛 FIX | fix(bridge): auto-detect Bedrock Edition via username prefix and Floodgate UUID across web & in-game sync
- **Pesan Commit:** `fix(bridge): auto-detect Bedrock Edition via username prefix and Floodgate UUID across web & in-game sync`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../core/integration/web/WebBridgeService.java`, `.../resources/views/admin/players/index.blade.php`, `.../src/Controllers/Api/PlayerSyncController.php`, `.../src/Controllers/PublicProfileController.php`, `.../src/Models/MinecraftAccount.php`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `ce22b7c` — 2026-09-12 | ♻️ REFACTOR | refactor(web): overhaul server map section with antislop-ui tactical cartography monolith
- **Pesan Commit:** `refactor(web): overhaul server map section with antislop-ui tactical cartography monolith`
  - **Komponen/Berkas:** `Website/themes/apexsions/assets/css/style.css`, `Website/themes/apexsions/assets/js/app.js`, `Website/themes/apexsions/views/home.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

---

## 🚀 Sprint 2 — Webstore Multi-Currency & Filter Interaktif, BlueMap 3D Server Map, Sistem Auto-Reward Vote & Dual-Theme UI [v1.0.5]
> **Periode Pengembangan:** 10 – 11 September 2026 | **Total Commit:** 26 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Ekspansi integrasi Web-to-Game dan penyempurnaan UI/UX portal Azuriom. Memperkenalkan etalase webstore multi-axis filter (Rupiah & Diamond), integrasi penampil peta 3D interaktif BlueMap dengan dynamic fallback probe, sistem voting lintas platform otomatis, dual-theme engine (Imperial Obsidian & Sovereign Ivory), perbaikan sistem penagihan antrean WebBridge, dan penyusunan master DOKUMENTASI.md.

### 🔍 Rincian Lengkap Commit (26 Commit)

#### `1acdcd8` — 2026-09-11 | ✨ FEAT | feat(web): direct redirect for server map, remove navbar button, and polish homepage UI/UX
- **Pesan Commit:** `feat(web): direct redirect for server map, remove navbar button, and polish homepage UI/UX`
  - **Komponen/Berkas:** `.../src/Controllers/ServerMapController.php`, `Website/themes/apexsions/assets/css/style.css`, `.../apexsions/views/elements/navbar.blade.php`, `Website/themes/apexsions/views/home.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `e9c9a7b` — 2026-09-11 | 🐛 FIX | fix(leaderboard): include all Minecraft server players and populate kingdom statistics
- **Pesan Commit:** `fix(leaderboard): include all Minecraft server players and populate kingdom statistics`
  - **Komponen/Berkas:** `.../apexsions-bridge/src/Controllers/LeaderboardController.php`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.

#### `99012d1` — 2026-09-11 | 🐛 FIX | fix(map,chat): update 3d world perspective and relax chat spam duplicate check
- **Pesan Commit:** `fix(map,chat): update 3d world perspective and relax chat spam duplicate check`
  - **Komponen/Berkas:** `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../com/apexsions/chat/moderation/SpamChecker.java`, `.../src/main/resources/moderation/moderation.yml`, `.../resources/views/server-map.blade.php`, `.../src/Services/ServerMapService.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `6f96c95` — 2026-09-11 | 🐛 FIX | fix(vote): ensure robust url routing and eliminate route name lookup exceptions
- **Pesan Commit:** `fix(vote): ensure robust url routing and eliminate route name lookup exceptions`
  - **Komponen/Berkas:** `.../apexsions-bridge/resources/views/admin/votes/index.blade.php`, `Website/themes/apexsions/views/vote.blade.php`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `6b49daa` — 2026-09-11 | ✨ FEAT | feat(vote): refactor total sistem vote menjadi auto-reward tanpa verifikasi manual
- **Pesan Commit:** `feat(vote): refactor total sistem vote menjadi auto-reward tanpa verifikasi manual`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../com/apexsions/core/command/VoteCommand.java`, `.../core/gui/input/BedrockFormAdapter.java`, `.../core/integration/web/WebBridgeService.java`, `.../com/apexsions/core/listener/VoteListener.java`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `33e3eeb` — 2026-09-11 | 📚 DOCS | docs: create master DOKUMENTASI.md and expand token conservation protocol
- **Pesan Commit:** `docs: create master DOKUMENTASI.md and expand token conservation protocol`
  - **Komponen/Berkas:** `AGENTS.md`, `DOKUMENTASI.md`, `GEMINI.md`, `README.md`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `173be77` — 2026-09-11 | 🐛 FIX | fix(theme): complete light mode backgrounds, contrast & add token conservation policy
- **Pesan Commit:** `fix(theme): complete light mode backgrounds, contrast & add token conservation policy`
  - **Komponen/Berkas:** `AGENTS.md`, `GEMINI.md`, `Website/themes/apexsions/assets/css/style.css`, `.../apexsions/views/elements/footer.blade.php`, `Website/themes/apexsions/views/home.blade.php`, `.../themes/apexsions/views/layouts/app.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `a1c2d90` — 2026-09-10 | ✨ FEAT | feat(web): webstore single benefit expansion, diamond currency, bluemap & seo implementation
- **Pesan Commit:** `feat(web): webstore single benefit expansion, diamond currency, bluemap & seo implementation`
  - **Komponen/Berkas:** `README.md`, `Website/database/seed_minecraft_systems.php`, `.../resources/views/admin/server/index.blade.php`, `.../resources/views/server-map.blade.php`, `Website/plugins/apexsions-bridge/routes/web.php`, `.../Controllers/Admin/ServerAdminController.php`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `a2954c0` — 2026-09-10 | 🔧 CHORE | chore(shop): remove obsolete Vault, nightcore, and ExcellentCrates from softdepend
- **Pesan Commit:** `chore(shop): remove obsolete Vault, nightcore, and ExcellentCrates from softdepend`
  - **Komponen/Berkas:** `.../plugins/ApexsionsShop/ApexsionsShop-1.0.0.jar`, `.../ApexsionsShop/src/main/resources/plugin.yml`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `75df932` — 2026-09-10 | 🐛 FIX | fix(shop): resolve duplicate softdepend in plugin.yml and harden ShopMainMenu loading
- **Pesan Commit:** `fix(shop): resolve duplicate softdepend in plugin.yml and harden ShopMainMenu loading`
  - **Komponen/Berkas:** `.../plugins/ApexsionsShop/ApexsionsShop-1.0.0.jar`, `Minecraft/plugins/ApexsionsShop/pom.xml`, `.../com/apexsions/shop/command/ShopCommand.java`, `.../java/com/apexsions/shop/gui/ShopMainMenu.java`, `.../ApexsionsShop/src/main/resources/plugin.yml`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `f7a9142` — 2026-09-10 | 🐛 FIX | fix(rank): uncap operator/staff ranks with 999 limits and 0s cooldowns
- **Pesan Commit:** `fix(rank): uncap operator/staff ranks with 999 limits and 0s cooldowns`
  - **Komponen/Berkas:** `.../apexsions-bridge/src/Models/RankConfig.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `8111f80` — 2026-09-10 | ✨ FEAT | feat(ranks): populate default baseline values and reference badges in rank edit form
- **Pesan Commit:** `feat(ranks): populate default baseline values and reference badges in rank edit form`
  - **Komponen/Berkas:** `.../resources/views/admin/ranks/edit.blade.php`, `.../src/Controllers/Admin/RankAdminController.php`, `.../apexsions-bridge/src/Models/RankConfig.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2fc4e4d` — 2026-09-10 | 🐛 FIX | fix(bridge): synchronize rank delivery state machine and audit log integrity
- **Pesan Commit:** `fix(bridge): synchronize rank delivery state machine and audit log integrity`
  - **Komponen/Berkas:** `.../Controllers/Api/LinkVerificationController.php`, `.../src/Controllers/Api/PlayerSyncController.php`, `.../apexsions-bridge/src/Services/RankService.php`, `.../src/Services/ServerOpsService.php`, `.../apexsions-bridge/src/Services/VoteService.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `e3f817c` — 2026-09-10 | 📚 DOCS | docs(web): document webstore storefront architecture and multi-axis filter engine
- **Pesan Commit:** `docs(web): document webstore storefront architecture and multi-axis filter engine`
  - **Komponen/Berkas:** `README.md`, `Website/ADMIN_GUIDE.md`, `Website/README.md`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `89d7520` — 2026-09-10 | 🐛 FIX | fix(web): overhaul shop category navigation and multi-axis interactive filters
- **Pesan Commit:** `fix(web): overhaul shop category navigation and multi-axis interactive filters`
  - **Komponen/Berkas:** `.gitignore`, `Website/themes/apexsions/assets/css/style.css`, `.../themes/apexsions/views/layouts/app.blade.php`, `.../views/plugins/shop/categories/index.blade.php`, `.../views/plugins/shop/categories/show.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4f57a23` — 2026-09-10 | 📚 DOCS | docs: update technical documentation and admin guide with integration audit results
- **Pesan Commit:** `docs: update technical documentation and admin guide with integration audit results`
  - **Komponen/Berkas:** `Minecraft/DOKUMENTASI.md`, `Minecraft/plugins/ApexsionsCore/DOKUMENTASI.md`, `README.md`, `Website/ADMIN_GUIDE.md`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `ecc060c` — 2026-09-10 | 🐛 FIX | fix(bridge): resolve compound command delimiter and native tellraw handling
- **Pesan Commit:** `fix(bridge): resolve compound command delimiter and native tellraw handling`
  - **Komponen/Berkas:** `.gitignore`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../core/integration/web/WebBridgeService.java`, `.../apexsions-bridge/src/Services/RankService.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ddb9ef0` — 2026-09-10 | 🐛 FIX | fix(ui): eliminate tablet landscape overflow and prevent double password toggle execution
- **Pesan Commit:** `fix(ui): eliminate tablet landscape overflow and prevent double password toggle execution`
  - **Komponen/Berkas:** `Website/themes/apexsions/assets/css/style.css`, `Website/themes/apexsions/assets/js/app.js`, `.../apexsions/views/elements/navbar.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ccfabb0` — 2026-09-10 | ✨ FEAT | feat(customenchants): add per-item and fullset level requirement in item creator synced with ApexsionsCore
- **Pesan Commit:** `feat(customenchants): add per-item and fullset level requirement in item creator synced with ApexsionsCore`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../ApexsionsCustomEnchants-1.0.0.jar`, `.../ApexsionsCustomEnchantsPlugin.java`, `.../customenchants/gui/AdminItemCreatorGUI.java`, `.../customenchants/gui/ItemLevelPickerGUI.java`, `.../customenchants/gui/ItemModifierGUI.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `65b70a0` — 2026-09-10 | ✨ FEAT | feat(ui): implement multi-device responsive layout, offcanvas navigation, and dual-theme engine
- **Pesan Commit:** `feat(ui): implement multi-device responsive layout, offcanvas navigation, and dual-theme engine`
  - **Komponen/Berkas:** `.../apexsions/assets/css/admin-apexsions.css`, `Website/themes/apexsions/assets/css/style.css`, `Website/themes/apexsions/assets/js/app.js`, `.../themes/apexsions/views/auth/register.blade.php`, `.../apexsions/views/elements/navbar.blade.php`, `.../themes/apexsions/views/layouts/app.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `22fa9ab` — 2026-09-10 | 🐛 FIX | fix(shop): resolve calculateUpgradePrice call in show.blade.php for authenticated users
- **Pesan Commit:** `fix(shop): resolve calculateUpgradePrice call in show.blade.php for authenticated users`
  - **Komponen/Berkas:** `.../themes/apexsions/views/plugins/shop/categories/show.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `3dd104c` — 2026-09-10 | ✨ FEAT | feat(webstore): add centralized webstore manager admin panel and fix package banner 404s
- **Pesan Commit:** `feat(webstore): add centralized webstore manager admin panel and fix package banner 404s`
  - **Komponen/Berkas:** `Website/ADMIN_GUIDE.md`, `.../resources/views/admin/webstore/edit.blade.php`, `.../resources/views/admin/webstore/index.blade.php`, `Website/plugins/apexsions-bridge/routes/web.php`, `.../Controllers/Admin/WebstoreAdminController.php`, `.../Providers/ApexsionsBridgeServiceProvider.php`
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `3d9f51a` — 2026-09-10 | ✨ FEAT | feat(theme): resolve rank card layout collisions, add duration filters & 2x2 spec grid
- **Pesan Commit:** `feat(theme): resolve rank card layout collisions, add duration filters & 2x2 spec grid`
  - **Komponen/Berkas:** `Website/ADMIN_GUIDE.md`, `Website/themes/apexsions/assets/css/style.css`, `.../views/plugins/shop/categories/index.blade.php`, `.../views/plugins/shop/categories/show.blade.php`, `.../views/plugins/shop/packages/show.blade.php`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `c777c68` — 2026-09-10 | 🐛 FIX | fix(theme,docs): resolve /admin/themes 500 by adding authors array & document rank upgrade suite
- **Pesan Commit:** `fix(theme,docs): resolve /admin/themes 500 by adding authors array & document rank upgrade suite`
  - **Komponen/Berkas:** `Minecraft/DOKUMENTASI.md`, `Website/ADMIN_GUIDE.md`, `Website/themes/apexsions/theme.json`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `b03523d` — 2026-09-10 | ✨ FEAT | feat(rank-upgrade): finalize rank upgrade engine, admin management, retention and whatsapp flow
- **Pesan Commit:** `feat(rank-upgrade): finalize rank upgrade engine, admin management, retention and whatsapp flow`
  - **Komponen/Berkas:** `...ate_rank_configs_and_upgrade_history_tables.php`, `.../resources/views/admin/ranks/edit.blade.php`, `.../resources/views/admin/ranks/index.blade.php`, `.../views/admin/ranks/purchases.blade.php`, `.../resources/views/admin/ranks/settings.blade.php`, `Website/plugins/apexsions-bridge/routes/web.php`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `760cdd7` — 2026-09-10 | ✨ FEAT | feat(rank-webstore): overhaul rank benefits, battlepass discounts, and webstore integration
- **Pesan Commit:** `feat(rank-webstore): overhaul rank benefits, battlepass discounts, and webstore integration`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../core/integration/LuckPermsRankProvisioner.java`, `.../java/com/apexsions/core/kit/KitManager.java`, `.../com/apexsions/core/level/LevelManager.java`, `.../apexsions/core/region/KingdomRtpService.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

---

## 🚀 Sprint 1 — Inisiasi Fondasi 9 Plugin Suite, Sistem Gacha Crates Berbobot, Admin Hub Management & Dual-Currency Core [v1.0.0]
> **Periode Pengembangan:** 9 September 2026 | **Total Commit:** 17 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Inisiasi fondasi monorepo 9 plugin suite Apexsions (Paper 26.2 / Java 21 LTS). Membangun sistem peti hadiah ApexsionsCrates berbasis bobot peluang efektif terpadu, isolasi kit player & admin GUI, standarisasi simbol mata uang (Rp., 💎, 🪙), penyusunan suite PlaceholderAPI komprehensif, serta rebuild 13 modul panel admin dengan antarmuka dual-theme.

### 🔍 Rincian Lengkap Commit (17 Commit)

#### `2aa7119` — 2026-09-09 | ✨ FEAT | feat(vote): add vote management to admin sidebar, configure minecraft-mp api key & server id, and implement platform toggle controls
- **Pesan Commit:** `feat(vote): add vote management to admin sidebar, configure minecraft-mp api key & server id, and implement platform toggle controls`
  - **Komponen/Berkas:** `..._000018_add_server_id_to_voting_sites_table.php`, `.../resources/views/admin/votes/index.blade.php`, `Website/plugins/apexsions-bridge/routes/web.php`, `.../src/Controllers/Admin/VoteAdminController.php`, `.../apexsions-bridge/src/Models/VotingSite.php`, `.../Providers/ApexsionsBridgeServiceProvider.php`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `d405767` — 2026-09-09 | 🐛 FIX | fix(vote): resolve /vote 500 error & implement crossplay vote experience for Java and Bedrock
- **Pesan Commit:** `fix(vote): resolve /vote 500 error & implement crossplay vote experience for Java and Bedrock`
  - **Komponen/Berkas:** `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../src/main/resources/broadcast/announcements.yml`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/command/VoteCommand.java`, `.../core/gui/input/BedrockFormAdapter.java`, `.../src/Controllers/VoteController.php`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `e0bac90` — 2026-09-09 | ✨ FEAT | feat(vote): implement real voting system with /vote command, 3x vote keys and rp 1000 rewards, cooldown tracking, and admin dashboard
- **Pesan Commit:** `feat(vote): implement real voting system with /vote command, 3x vote keys and rp 1000 rewards, cooldown tracking, and admin dashboard`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../com/apexsions/core/command/VoteCommand.java`, `.../ApexsionsCore/src/main/resources/plugin.yml`, `..._000017_create_apexsions_vote_system_tables.php`, `.../resources/views/admin/votes/index.blade.php`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `5bbea14` — 2026-09-09 | 🐛 FIX | fix(admin): resolve delivery enum truncation, tellraw json formatting, and player table template bug
- **Pesan Commit:** `fix(admin): resolve delivery enum truncation, tellraw json formatting, and player table template bug`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/command/AdminCommand.java`, `...016_fix_deliveries_status_enum_and_indexing.php`, `.../resources/views/admin/players/index.blade.php`, `.../Controllers/Admin/PlayerAdminController.php`, `.../Controllers/Api/LinkVerificationController.php`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `d8c6b42` — 2026-09-09 | ✨ FEAT | feat(core): isolate kit player gui and implement dedicated kit admin dashboard
- **Pesan Commit:** `feat(core): isolate kit player gui and implement dedicated kit admin dashboard`
  - **Komponen/Berkas:** `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../com/apexsions/core/ApexsionsCorePlugin.java`, `.../apexsions/core/gui/admin/CoreAdminSubGUI.java`, `.../com/apexsions/core/kit/KitAdminCreatorGUI.java`, `.../com/apexsions/core/kit/KitAdminListGUI.java`, `.../com/apexsions/core/kit/KitGUIListener.java`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `c4ee606` — 2026-09-09 | 🐛 FIX | fix(crates): display reward lore and custom enchants above rarity info in preview
- **Pesan Commit:** `fix(crates): display reward lore and custom enchants above rarity info in preview`
  - **Komponen/Berkas:** `.../ApexsionsCrates/ApexsionsCrates-1.0.0.jar`, `.../apexsions/crates/crate/menu/PreviewMenu.java`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `3ecd153` — 2026-09-09 | 📚 DOCS | docs: update master documentation for user management center, auth security, and crates suite
- **Pesan Commit:** `docs: update master documentation for user management center, auth security, and crates suite`
  - **Komponen/Berkas:** `Minecraft/DOKUMENTASI.md`, `README.md`, `Website/ADMIN_GUIDE.md`, `Website/README.md`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `dd00a5f` — 2026-09-09 | ✨ FEAT | feat(crates): calculate reward chance grouped strictly by rarity and show rarity roll chance in preview lore
- **Pesan Commit:** `feat(crates): calculate reward chance grouped strictly by rarity and show rarity roll chance in preview lore`
  - **Komponen/Berkas:** `.../ApexsionsCrates/ApexsionsCrates-1.0.0.jar`, `.../java/com/apexsions/crates/Placeholders.java`, `.../com/apexsions/crates/api/crate/Reward.java`, `.../com/apexsions/crates/crate/impl/Crate.java`, `.../com/apexsions/crates/crate/impl/Rarity.java`, `.../apexsions/crates/crate/menu/PreviewMenu.java`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `777c54a` — 2026-09-09 | ✨ FEAT | feat(auth): resolve /admin/users 500, redesign user management center & add password toggles
- **Pesan Commit:** `feat(auth): resolve /admin/users 500, redesign user management center & add password toggles`
  - **Komponen/Berkas:** `.../app/Http/Controllers/Admin/UserController.php`, `Website/app/Models/User.php`, `.../resources/views/admin/users/_notify.blade.php`, `Website/resources/views/admin/users/edit.blade.php`, `.../resources/views/admin/users/index.blade.php`, `Website/resources/views/auth/login.blade.php`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `406232e` — 2026-09-09 | 🐛 FIX | fix(crates): implement unified tiered effective weight chance system
- **Pesan Commit:** `fix(crates): implement unified tiered effective weight chance system`
  - **Komponen/Berkas:** `.../ApexsionsCrates/ApexsionsCrates-1.0.0.jar`, `.../java/com/apexsions/crates/Placeholders.java`, `.../com/apexsions/crates/api/crate/Reward.java`, `.../com/apexsions/crates/crate/impl/Crate.java`, `.../com/apexsions/crates/crate/impl/Rarity.java`, `.../crates/crate/reward/AbstractReward.java`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `0a17020` — 2026-09-09 | 🐛 FIX | fix(crates): ensure crate key and chest item display names and lore are properly formatted with colors
- **Pesan Commit:** `fix(crates): ensure crate key and chest item display names and lore are properly formatted with colors`
  - **Komponen/Berkas:** `.../ApexsionsCrates/ApexsionsCrates-1.0.0.jar`, `.../com/apexsions/crates/crate/impl/Crate.java`, `.../crates/dialog/generic/GenericNameDialog.java`, `.../java/com/apexsions/crates/key/CrateKey.java`, `.../java/com/apexsions/crates/key/KeyManager.java`, `.../java/com/apexsions/crates/util/CrateUtils.java`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `3ddfcad` — 2026-09-09 | 📚 DOCS | docs: add complete PlaceholderAPI documentation suite for all Apexsions plugins
- **Pesan Commit:** `docs: add complete PlaceholderAPI documentation suite for all Apexsions plugins`
  - **Komponen/Berkas:** `.../ApexsionsBattlepass-1.0.0.jar`, `.../battlepass/integration/PlaceholderAPIHook.java`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../core/integration/PlaceholderApiHook.java`, `.../plugins/ApexsionsShop/ApexsionsShop-1.0.0.jar`, `Minecraft/plugins/ApexsionsShop/pom.xml`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `9065256` — 2026-09-09 | 🐛 FIX | fix(shop,currency): ensure complete currency symbol standardization (Rp., 💎, 🪙) in shop menus, quests, and GUIs
- **Pesan Commit:** `fix(shop,currency): ensure complete currency symbol standardization (Rp., 💎, 🪙) in shop menus, quests, and GUIs`
  - **Komponen/Berkas:** `.../ApexsionsBattlepass-1.0.0.jar`, `.../admin/gui/AdminPlayerDetailMenu.java`, `.../battlepass/admin/gui/AdminShopMenu.java`, `.../battlepass/admin/gui/AdminStatsMenu.java`, `.../admin/gui/quest/AdminQuestEditorMenu.java`, `.../admin/gui/quest/AdminQuestListMenu.java`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `fa31205` — 2026-09-09 | ✨ FEAT | feat(admin): complete 13-point admin refinement, global search, quick actions & dual-theme styling
- **Pesan Commit:** `feat(admin): complete 13-point admin refinement, global search, quick actions & dual-theme styling`
  - **Komponen/Berkas:** `.../views/admin/audit-logs/index.blade.php`, `.../resources/views/admin/players/index.blade.php`, `.../resources/views/admin/players/show.blade.php`, `.../resources/views/admin/ranks/index.blade.php`, `.../resources/views/admin/ranks/show.blade.php`, `Website/plugins/apexsions-bridge/routes/web.php`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `b9b5d41` — 2026-09-09 | 🔧 CHORE | chore(plugins): set author to Nueeva across all plugins
- **Pesan Commit:** `chore(plugins): set author to Nueeva across all plugins`
  - **Komponen/Berkas:** `.../ApexsionsBattlepass-1.0.0.jar`, `.../src/main/resources/plugin.yml`, `.../plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar`, `.../ApexsionsChat/src/main/resources/plugin.yml`, `.../plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar`, `.../ApexsionsCore/src/main/resources/plugin.yml`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `460f2b9` — 2026-09-09 | 🔧 CHORE | chore: remove MythicMobs folder as files are accessible directly via SFTP
- **Pesan Commit:** `chore: remove MythicMobs folder as files are accessible directly via SFTP`
  - **Komponen/Berkas:** `MythicMobs/config/config-general.yml`, `MythicMobs/config/config-items.yml`, `MythicMobs/config/config-mobs.yml`, `MythicMobs/config/config-skills.yml`, `MythicMobs/config/config-spawning.yml`, `MythicMobs/config/lang/en-us/mythicmobs.json`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `1da8a10` — 2026-09-09 | ✨ FEAT | feat(admin): parity audit, high-value actions & dual-theme rebuild
- **Pesan Commit:** `feat(admin): parity audit, high-value actions & dual-theme rebuild`
  - **Komponen/Berkas:** `.agents/rules/multi-developer-protocol.md`, `.gitignore`, `.impeccable/config.json`, `.impeccable/design.json`, `AGENTS.md`, `DESIGN.md`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

---
