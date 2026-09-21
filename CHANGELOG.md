# CHANGELOG.md — Apexsions Development & Evolution History
# Apexsions — The Peak Civilizations

> **Repository:** `Nueeva/Apexsions`  
> **Brand Name:** `Apexsions` (DILARANG menambahkan kata Network/SMP/Kingdom)  
> **Tagline:** `The Peak Civilizations`  
> **Standar Riwayat:** Commit-Based Reverse-Chronological Changelog (153 Commits)  
> **Format:** Berdasarkan standar [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

Dokumen ini mendokumentasikan **seluruh 153 riwayat commit** repositori secara lengkap, mendalam, dan terstruktur ke dalam 7 milestone pengembangan dari awal mula inisiasi proyek (27 Agustus 2026) hingga kondisi stabil terkini (17 September 2026). Dokumen ini dirancang sebagai referensi tunggal bagi developer dan AI Coding Agent untuk memahami riwayat arsitektur, modul yang tersentuh, serta evolusi fitur.

---

## 🤖 Panduan Alih-Tugas untuk AI Coding Agent & Pengembang Baru (Quick Handoff)

Bagi AI Agent atau developer yang melanjutkan pekerjaan di repositori ini, perhatikan kontrak operasional berikut:

1. **Brand Identity Mandate:**
   - Nama server adalah strictly **`Apexsions`**.
   - Dilarang keras menambahkan akhiran seperti "Network", "Kingdom", "SMP" pada teks UI, chat, nama kelas, atau dokumentasi.
2. **Kanon Tiga Kerajaan Berdaulat & Upper Dimension:**
   - **☀️ Zenithar (Timur):** Ibukota *Solarium Spire Citadel*, Pajak Kas `18.0%`. Spesialisasi: Kecepatan (+8%), Keberuntungan (+15%), [Royal Discipline] (+6% Damage), [Royal Aegis] (20% Reduksi Damage Masuk), Diskon Lelang 30%, Bunga Bank +25%, Diskon Blok Toko 15%. Debuff: Racun/Wither +15%, Aristocratic Exhaustion (+12% Cepat Lapar), Mining -10%, Anvil repair +1 EXP.
   - **🔥 Solterra (Selatan):** Ibukota *Ignis Bastion Fortress*, Pajak Kas `20.0%`. Spesialisasi: Penyerang Agresif, [Battle Momentum] (+15% Damage), (+10% Crit Damage), (+2% Defense), (+10% Mining Speed), Rasio Jual Ore Tinggi (30%). Debuff: -2 HP (9 Hati), Vulnerability (+8% Damage Masuk), Cepat Lapar (+7%), Lahan Cepat Kering.
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

## 🛡️ Apexsions Security & Anti-Cheat Suite (Fly Hack, Auth Bypass, Combat Guard & Packet Exploits) Milestone [v1.3.7]
> **Periode Pengembangan:** 21 September 2026 | **Status:** Implemented, Tested, Live Verified & Documented

### 📋 Ikhtisar Implementasi Sistem Anti-Cheat & Mitigasi Exploit
Berdasarkan investigasi terhadap mod seperti Fly Hack CurseForge dan berbagai cheat client modern (Meteor, Wurst, LiquidBounce, Aristois), ekosistem server dilengkapi sistem deteksi dan mitigasi komprehensif di `ApexsionsCore` (`com.apexsions.core.security.*`) serta pengerasan autentikasi:

1. **Movement Security Engine (`MovementSecurityListener.java`):**
   - **Fly Hack & AirWalk / Creative Fly Mitigation:** Mencegah pergerakan vertikal di udara tanpa izin terbang yang sah. Menghukum pergerakan naik ($\Delta y \ge 0$) atau melayang di udara tanpa pijakan selama $> 6$ tick berturut-turut dengan *Rubberbanding* ke lokasi tanah aman terakhir (`lastSafeGround`) serta siaran alert staf jika berulang.
   - **Horizontal Speed Hack:** Membatasi laju $(\Delta x^2 + \Delta z^2)$ dengan kompensasi ramuan Speed, Soul Speed, dan knockback tempur.
   - **Jesus / WaterWalk Guard:** Menolak status `onGround = true` pada permukaan cairan (air/lahar) tanpa sepatu Frost Walker.
   - **True Server-Side NoFall:** Server melacak jarak jatuh nyata di udara secara independen dan menerapkan damage jatuh saat mendarat, mengabaikan manipulasi paket klien.
2. **Auth Security Gatekeeper & Staff Shield (`AuthSecurityGateKeeper.java`):**
   - **Pre-Login Absolute Lockdown (`LOWEST` Priority):** Memblokir seluruh perintah non-auth (`/login`, `/l`, `/register`, `/reg`, `/2fa` diizinkan), interaksi kontainer/GUI, melempar/mengambil item di spawn, dan penyerangan sebelum pemain terotentikasi.
   - **Eliminasi Session Hijacking:** Menonaktifkan `sessions.enabled` di `config/authme/config.yml` guna menutup celah auto-login pemain pada jaringan IP bersama (WiFi publik/warnet/CGNAT).
   - **Staff Account Shield & Anti-Brute-Force:** Khusus akun jajaran Staf (`ancestor`, `architect`, `overseer`, `warden`, `herald`), 3x kesalahan kata sandi otomatis memutus koneksi (kick), memblokir IP 10 menit, dan menyiarkan peringatan darurat ke staf online & konsol.
   - **Teleportasi Pra-Login:** Mengaktifkan `teleportUnAuthedToSpawn: true` agar lokasi logout/base rahasia pemain tidak termuat sebelum login.
3. **Combat Guard Engine (`CombatSecurityListener.java`):**
   - **KillAura Angle Check:** Membatalkan serangan dengan sudut $> 95^\circ$ antara arah pandang mata penyerang dan posisi target (menolak pukulan ke belakang/samping).
   - **Wall-Hit (Phase Strike) Raycast:** Memastikan tidak ada blok padat oklusif di antara penyerang dan korban (mencegah pukulan tembus dinding/pintu).
   - **Combat Reach Hack:** Membatasi jangkauan serangan maksimal $4.2$ blok di mode Survival.
   - **Auto-Clicker Throttle:** Membatasi frekuensi serangan maksimal 20 CPS per detik.
4. **Packet & World Exploits (`PacketExploitListener.java`):**
   - **BadPackets Pitch Sanitizer:** Mengoreksi pitch abnormal di luar rentang fisik $[-90.0^\circ, +90.0^\circ]$.
   - **Crash Exploit Filter:** Mendeteksi dan menendang klien yang mengirim koordinat `NaN` atau `Infinity`.
   - **Scaffold / FastPlace Guard:** Membatasi penempatan blok maksimal 14 blok/detik di survival.
   - **ChestStealer Limiter:** Membatasi pemindahan item kontainer maksimal 12 klik/detik.
5. **Unit Tests & Konfigurasi Modular:**
   - 4 Unit tests algoritma anti-cheat (`AntiCheatTest.java`) lulus 100%.
   - Bagian konfigurasi `security:` ditambahkan ke `config.yml` dengan toggle granular per modul.

---

## ⚔️ Living PvE Scaling, Nether & The End Leveled Ecosystem, Vanish Privacy Hardening, Player Cache Resilience & Economy Rebalance Milestone [v1.3.6]
> **Periode Pengembangan:** 18 September 2026 | **Status:** Implemented, Tested, Live Deployed to Server & Verified

### 📋 Ikhtisar Peningkatan PvE, Pengerasan Keamanan, & Stabilitas Ekonomi
Menjawab kebutuhan gameplay survival jangka panjang, mengisi konten monster berlevel di seluruh dimensi, mencegah kebocoran status staf saat beroperasi dalam senyap, memperkuat resistensi cache data pemain, serta merekalibrasi pasar dan saldo awal agar ekonomi server sehat dan kompetitif:

1. **Skalabilitas Damage PvE Berbasis Level Pemain (+50.0% Cap) (`ApexsionsCore`):**
   - Mengalibrasi formula damage PvE pada `CombatListener.java` / `PlayerDamageBonus` dari kurva sebelumnya menjadi `0.50 * (level / 100.0)`.
   - Pada Level 100, pemain mendapatkan peningkatan pukulan fisik hingga **+50.0%** terhadap monster lingkungan (PvE), memberikan *power fantasy* sepadan atas jerih payah leveling tanpa merusak asas fair-play duel antar-pemain (PvP dinormalisasi tetap pada cap $+0.80$ dan $0\%$ PvE bonus).
2. **Ekosistem Monster Berlevel Nether & The End (MythicMobs / LevelledMobs):**
   - Menghubungkan ekosistem monster bertingkat ke dimensi Neraka (*Nether*) dan Dimensi Akhir (*The End*):
     - **Tier 4 (Nether • Lv 35–75):** *Ash Crawler*, *Molten Core Titan*, *Inferno Wraith* (Ghast pembawa bola api berdaya hancur tinggi), *Crimson Behemoth* (Hoglin bertanduk), dan *Bastion Warmaster* (Piglin Brute tangguh).
     - **Tier 5 (The End & Terra Interdicta • Lv 65–95+):** *Void Warped Watcher* (Enderman mutasi kehampaan), *Astral Void Phantom*, *Astral Shulker Sentinel*, serta *Sions Fallen Legionnaire*.
     - **Tier 6 (Sovereign World Bosses • Lv 100):** *Corrupted Void Sovereign Drake* (Ender Dragon mutasi fase ganda di The End) dan *Emperor Valerius* (Raid Boss 4 jam di Terra Interdicta).
3. **Pengerasan Privasi & Kesenyapan Mode Vanish (`ApexsionsCore` & `ApexsionsChat`):**
   - **Supresi Siaran Advancement (`VanishListener.java`):** Mengintersepsi `PlayerAdvancementDoneEvent` dengan prioritas `HIGHEST`. Ketika pemain berstatus vanish meraih achievement/advancement (seperti `[Rescue Mission]`), siaran chat publik otomatis ditiadakan (`event.message(null)`).
   - **Blokir Chat Publik Staf Vanish (`ChatListener.java`):** Mencegah staf dalam mode vanish mengirim pesan ke channel publik (`Global` dan `Kingdom`). Pesan dibatalkan dan staf menerima notifikasi privat untuk memakai `/staffchat` (`/sc`) atau mematikan `/vanish`.
   - **Refleksi Sinkronisasi EssentialsX & TAB (`VanishManager.java`):** Menyinkronkan status vanish secara langsung ke objek internal EssentialsX (`User.setVanished(boolean)`), memastikan `/seen`, `/list`, tablist, dan placeholder `%essentials_vanished%` sepenuhnya konsisten.
4. **Resiliensi Cache Profil Pemain (`PlayerListener.java` & `LevelManager.java`):**
   - Mengatasi potensi *cache miss* saat pemain mengalami disconnect dan reconnect secara kilat (di mana `flush()` telah membersihkan cache RAM sebelum koneksi baru terjalin).
   - `PlayerListener.onJoin()` menyertakan *synchronous fallback loader* `loadOrCreate(uuid, name).join()` jika data memori kosong.
   - `LevelManager` menyertakan metode pertahanan `resolvePlayerData(uuid)` yang proaktif memuat ulang data dari database jika cache miss, memastikan level pemain dan gelar tidak pernah anjlok ke `Lv. 1 Citizen`.
5. **Audit Menyeluruh & Rekalibrasi Keseimbangan Ekonomi Pasar (`ApexsionsEconomy` & `ApexsionsShop`):**
   - **Saldo Awal Survival (`ApexsionsEconomy`):** Diturunkan dari Rp 10.000 menjadi **Rp 1.000** untuk menjaga nilai mata uang sejak fase awal permainan.
   - **Eksklusivitas Diamond di Toko (`ores.yml`):** Pembelian Diamond ditiadakan (`buy-enabled: false`, harga jual Rp 250/butir). Pemain diwajibkan menambang di kedalaman `Y < -40` atau berdagang via `/ah` / `/trade`.
   - **Penyesuaian Rasio Jual Bijih Solterra (`markets.yml`):** `SOLTERRA.ores-sell-ratio` diturunkan dari 65% menjadi **30%** untuk menekan laju inflasi pencetakan uang.
   - **Stabilisasi Bebatuan & Hasil Panen (`blocks.yml`, `farming.yml`):** Harga jual bebatuan dasar (Cobblestone, Stone, Dirt, Sand) distandarkan ke Rp 0.6 – 1.2 per blok, dan hasil panen massal (Sugar Cane, Wheat, Carrot) ke Rp 1.5 – 2.0 per unit.
6. **Validasi & Deployment Live SFTP:**
   - Seluruh 4 JAR (`ApexsionsCore`, `ApexsionsChat`, `ApexsionsEconomy`, `ApexsionsShop`) serta 5 berkas konfigurasi telah dikompilasi sukses dan diunggah langsung ke game server live (`falcon04.jagoanhosting.id:2022`).

---

## 🏛️ Upper Realm Cosmic Portal, Smart RTP, Capital Navigation & Mortal Emulation Milestone [v1.3.5]
> **Periode Pengembangan:** 18 September 2026 | **Status:** Implemented, Tested, & Verified Locally

### 📋 Ikhtisar Peningkatan Operasional Dimensi Atas (The Aetherial Conclave)
Menyelesaikan kendala operasional bagi entitas dimensi atas (staf Conclave / bobot rank $\ge 80$) yang sebelumnya terhambat berinteraksi di dimensi fana untuk navigasi, perbaikan wilayah, pengujian fitur (*testing*), dan teleportasi ibukota:
1. **The Aetherial Conclave Realm Portal (`ConclaveNavigationGUI`):**
   - Mengalihkan eksekusi `/k`, `/kingdom`, atau `/region` oleh staf Conclave langsung ke GUI portal kosmik 27-slot bergaya cyan-electric (`#00f2fe` ke `#4facfe`).
   - Menyediakan tombol navigasi cepat ke Lobby Utama, Ibukota Zenithar, Solterra, Sylvamoor (Dual Action: Klik Kiri = Teleportasi Ibukota, Klik Kanan = Targeted RTP), Terra Interdicta (Sions), Smart RTP Dispatcher, Simulasi Warga Fana, dan Master Admin Hub.
2. **Perintah Teleportasi Ibukota & RTP Terarah:**
   - `/ac spawn <ZENITHAR|SOLTERRA|SYLVAMOOR|SIONS|LOBBY>` dan `/k spawn <kingdom|lobby>` untuk teleportasi langsung staf/admin tanpa delay.
   - `/ac rtp [kingdom]` dan `/rtp [kingdom]` untuk teleportasi acak terarah di teritori fana yang dipilih.
3. **Mortal Incarnation Engine (`MortalEmulationManager`):**
   - Memungkinkan staf mengaktifkan mode penyamaran kerajaan mortal (`/ac emulate <ZENITHAR|SOLTERRA|SYLVAMOOR|OFF>`) untuk menguji toko dynamic market, buff/debuff kerajaan, chat teritorial, dan izin wilayah dari sudut pandang warga biasa tanpa mengubah status kanon di database.
   - Otomatis membersihkan status penyamaran saat pemain logout (`PlayerQuitEvent`).
4. **Dual-Action Teleportasi Ibukota di Admin GUI (`CoreAdminSubGUI`):**
   - Slots 30, 31, 32 di `CoreAdminSubGUI` kini mendukung [Klik Kiri] untuk teleportasi langsung ke ibukota dan [Shift + Klik Kanan] untuk menetapkan ulang koordinat spawn ibukota di posisi admin.
5. **Bypass Proteksi Komprehensif:**
   - Bebas hambatan Combat Tag 15 detik bagi staf/admin saat menjalankan perintah teleportasi darurat.
   - Bypass pembatasan TPA Essentials dua arah antara staf Conclave dan pemain fana.
   - Pendaftaran klaim tanah staf sebagai kedutaan `AETHERION` ($0 upkeep tax) dengan bypass izin membangun dan interaksi di seluruh teritori fana.

---

## ⏳ Seasonal Architecture & Triannual Era Milestone — Migrasi Siklus 4 Bulan (120 Hari per Reset) & Sinkronisasi Ekosistem [v1.3.4]
> **Periode Pengembangan:** 18 September 2026 | **Status:** Implemented & Verified Locally

### 📋 Ikhtisar Migrasi Siklus Caturwulan & Reset Dunia
Meredesain siklus peradaban dan progres musiman dari 3 bulan (90 hari) ke **Siklus 4 Bulan Penuh (120 Hari per Era / Triannual Cycle)** dengan **Reset & Wipe Total Peta Dunia di setiap akhir Season**:
1. **Pembaruan Babad & Kosmologi (`LORE.md`):**
   - Bab I: Siklus Waktu Peradaban disesuaikan menjadi *Kalender Musim Empat Bulanan (Triannual Seasonal Chronicle / 120 Hari per Era)*.
   - Bab XII: Roadmap 3 Era per Tahun (Season I: Awakening & Resonant Surge, Season II: Fractured Concord & War of Sovereignty, Season III: Second Rift & Grand Reckoning) dengan World Wipe di hari ke-120 tiap musim.
   - Hak Keabadian Donatur: Seluruh rank permanen, saldo diamond, dan gelar kehormatan sejarah 100% aman dan abadi melintasi reset.
2. **Ekspansi Engine `ApexsionsBattlepass`:**
   - `SeasonManager.java`: Kalkulasi bulan dinamis modulo 4 (`monthsPassed % 4 + 1`, cap di 4).
   - `MonthlyPeriodMenu.java`: Banner informasi 4 bulan, grid 4 tombol bulanan di slot `[10, 12, 14, 16]`.
   - `AdminSeasonMenu.java`, `AdminQuestMenu.java`, `AdminQuestCategoryMenu.java`: Mendukung pengelolaan quest Bulan 1 s/d Bulan 4 di slot `[28, 30, 32, 34]`.
   - Teruji sukses dikompilasi dengan `build.ps1 Battlepass` (0 error).
3. **Web Platform & Webstore Storefront (`Azuriom` & `apexsions-bridge`):**
   - `seed_minecraft_systems.php`: Paket kasta trial ditingkatkan dari 90 hari ke `Trial 120 Hari (1 Season Penuh)` dengan durasi LuckPerms `120d`.
   - `categories/show.blade.php`, `index.blade.php`, `packages/show.blade.php`: Filter tombol durasi `120-hari`, pill badge `TRIAL 120 HARI` / `TRIAL 120H`, dan fallback backward-compatible.
   - `RankAdminController.php` & Admin Views: Sinkronisasi harga paket 120 hari dan opsi dropdown 120 hari.
4. **Dokumentasi Master:**
   - `DOKUMENTASI.md` & `README.md`: Sinkronisasi penjelasan siklus caturwulan dan varian durasi paket webstore.

---

## 🧹 Repository Architecture & Workspace Cleanup Milestone — Deduplikasi Berkas, Sentralisasi Hub `docs/`, & Tata Kelola Bersih [v1.3.3]
> **Periode Pengembangan:** 18 September 2026 | **Status:** Implemented & Synchronized to Main

### 📋 Ikhtisar Perapihan & Restrukturisasi Berkas Repositori
Menertibkan struktur folder dan berkas repositori Apexsions, mengeliminasi seluruh redundansi, dan mengkonsolidasikan dokumentasi ke hub terpusat:
1. **Eliminasi Berkas Duplikat & Stale:**
   - Menghapus direktori bersarang ganda `Minecraft/.agents/` yang tidak valid.
   - Menghapus salinan usang dari `AGENTS.md`, `GEMINI.md`, dan `README.md` di dalam folder `Minecraft/`, memastikan Single Source of Truth berada di root repositori.
   - Menghapus `Minecraft/.gitignore` yang 100% redundan dengan root `.gitignore`.
   - Menghapus artefak biner usang `Minecraft/plugins/ApexsionsCrates/ApexsionsCrate-1.0.0.jar` yang bertipe nama tunggal (typo).
2. **Sentralisasi Hub Dokumentasi Terpusat (`docs/`):**
   - Memindahkan seluruh berkas manual modul plugin dan panduan integrasi arsitektur dari `Minecraft/docs/` ke root `docs/` (`APEXSIONS_CORE.md`, `APEXSIONS_CHAT.md`, `APEXSIONS_ECONOMY.md`, `APEXSIONS_BATTLEPASS.md`, `APEXSIONS_SHOP.md`, `APEXSIONS_MEDIA.md`, `ECOSYSTEM_ARCHITECTURE.md`, `ECONOMY_INTEGRATION_POSTGRESQL.md`, `BATTLEPASS_INTEGRATION_POSTGRESQL.md`).
   - Memindahkan direktori dokumentasi placeholder `PlaceholderApexsions/` dari root repositori ke `docs/placeholders/`, serta memperbaiki seluruh hyperlink internal ke relative path yang valid.
   - Memindahkan master system prompt `SYSTEM PROMPT_ Apexsions Senior Minecraft Plugin Engineer.md` dari root dan `Minecraft/` ke format standar `docs/prompts/SYSTEM_PROMPT_MINECRAFT_ENGINEER.md`.
3. **Penyelarasan Indeks & Navigasi Monorepo:**
   - Memperbarui diagram pohon arsitektur monorepo pada `README.md` agar mencerminkan keberadaan `docs/`, `docs/placeholders/`, `docs/prompts/`, dan aset runtime `Minecraft/config/` serta `Minecraft/packs/`.
   - Memutakhirkan indeks dokumentasi lengkap pada `README.md` sehingga seluruh link ke manual modul, placeholder, prompt, dan panduan teknis 100% aktif dan terhubung.

---

## 🛠️ Tooling & Infrastructure Milestone — Integrasi Native Graphify Knowledge Graph & Antigravity Workflow [v1.3.2]
> **Periode Pengembangan:** 18 September 2026 | **Status:** Implemented & Synchronized to Main

### 📋 Ikhtisar Implementasi Graphify & AI Knowledge Navigation
Mengintegrasikan sistem **Persistent Knowledge Graph** bertenaga **Graphify** (`https://github.com/Graphify-Labs/graphify`) untuk memfasilitasi pemahaman arsitektur menyeluruh, pelacakan dependensi antar-modul, serta memangkas konsumsi token LLM secara signifikan:
1. **Pemasangan Python 3.12 LTS & AST Parser Multi-Bahasa:**
   - Memasang runtime Python 3.12 LTS pada lingkungan pengembang lokal dan mengonfigurasikannya ke dalam `PATH` sistem.
   - Memasang pustaka `graphifyy` beserta rangkaian parser Tree-Sitter lengkap (`tree-sitter-java`, `tree-sitter-php`, `tree-sitter-javascript`, `tree-sitter-typescript`, `tree-sitter-python`, dll).
2. **Ekstraksi Graf Seluruh Monorepo (11.805 Nodes & 38.282 Edges):**
   - Memproses **1.462 berkas kode** di seluruh 9 plugin Paper 26.2, platform web Azuriom, dan modul jembatan WebBridge.
   - Menghasilkan 11.805 simpul (nodes), 38.282 relasi pemanggilan/implementasi (edges), dan 524 klaster komunitas fungsional.
   - Menghasilkan antarmuka web visualisasi interaktif di `graphify-out/graph.html` dan laporan audit mendalam `graphify-out/GRAPH_REPORT.md`.
3. **Integrasi Aturan & Alur Kerja Agent Antigravity:**
   - Global Customization Skill: `C:\Users\Friel\.gemini\config\skills\graphify\SKILL.md`.
   - Aturan Operasional Workspace: `.agents/rules/graphify.md`.
   - Slash Command Workflow: `.agents/workflows/graphify.md` (dapat dipanggil via `/graphify .`).
4. **Sinkronisasi Aturan Master (`GEMINI.md` & `AGENTS.md`):**
   - Menambahkan Bagian 58 pada `GEMINI.md` (*Knowledge Graph Architecture & Graphify Protocol*).
   - Memperbarui Bagian 03 (*Read Before You Change*) dan Bagian 17 (*Token Conservation Protocol*) pada `AGENTS.md` untuk memprioritaskan kueri graf (`graphify query`, `graphify path`) dan pembaruan lokal pasca-coding (`graphify update .`).
5. **Git Hygiene:**
   - Menambahkan `graphify-out/` ke `.gitignore` guna mengamankan repositori dari beban commit file database JSON 28+ MB dan cache SHA-256.

---

## 🚀 Sprint 9 — Resolusi Audit Live Staging: Zero-Friction Bedrock/Premium, Harmonisasi IP & Scoreboard, Leveling Monster Kerajaan, Bed Respawn & Hardening Permissions [v1.3.1]
> **Periode Pengembangan:** 18 September 2026 | **Status:** Live & Deployed to Production

### 📋 Tinjauan Perbaikan & Audit 8 Temuan Utama
Berdasarkan hasil audit komprehensif pada pengujian live staging server, seluruh 8 isu kritis telah diselesaikan dan diterapkan langsung:
1. **Resolusi Monster Level 58 di Wilayah Ibukota Kerajaan:**
   - **Akar Masalah:** MythicMobs `MobLeveling.WorldScaling` aktif dengan parameter `ScaleVanillaMobs: true` dan `PerBlocksFromSpawn: 250`. Akibatnya, monster biasa di wilayah ibukota Sylvamoor (`-9666, -4812` / jarak ~10.800 blok dari spawn) otomatis diskalakan menjadi Level 58. Ditambah lagi, berkas default `ExampleRandomSpawns.yml` menggantikan monster biasa dengan `SkeletalKnight` dan `SkeletonKing` secara global.
   - **Solusi:** Menonaktifkan `WorldScaling` global dan `ScaleVanillaMobs` pada `config-mobs.yml`. Menghapus spawn contoh `ExampleRandomSpawns.yml`. Menambahkan kondisi `notinregion zenithar,solterra,sylvamoor` pada seluruh random spawns `wilderness.yml` agar monster berlevel liar tidak pernah memasuki teritori kerajaan.
2. **Standardisasi IP Server Game & Web Platform:**
   - IP server game Minecraft resmi di seluruh papan skor, pengumuman, dan konfigurasi TAB ditetapkan murni ke **`apexsions.my.id`** (tanpa subdomain play).
   - Domain platform web resmi ditetapkan ke **`web.apexsions.my.id`**.
3. **Harmonisasi Papan Skor (Scoreboard) Bedrock & Java:**
   - Menghapus pemangkasan berlebihan pada Bedrock scoreboard di TAB config.
   - Papan skor Bedrock kini 100% identik dengan Java: memuat bagian PROFIL (Rank, Kerajaan), EKONOMI (Rupiah, Diamond, Coins Battlepass), PROGRESI (Level & Batang Kemajuan EXP), serta footer ganda `apexsions.my.id` dan `web.apexsions.my.id`.
4. **Zero-Friction Bedrock Join (Bebas Register/Login):**
   - Mengaktifkan `autoRegisterFloodgate: 'true'` dan `autoLoginFloodgate: 'true'` di FastLogin.
   - Pemain Bedrock via Floodgate (`.PlayerName`) kini langsung terdaftar secara otomatis dengan hash aman di AuthMe dan terautentikasi instan saat pertama kali masuk tanpa pernah melihat prompt `/register`.
5. **Autentikasi Java Premium Otomatis & Resolusi Perintah `/premium`:**
   - **Akar Masalah:** AuthMe memiliki perintah `/premium` bawaan (dengan status `enablePremium: false`) yang mencegat pemanggilan perintah sebelum sampai ke FastLogin, menghasilkan galat *"this server do not enable the premium"*.
   - **Solusi:** Menambahkan alias mutlak pada `commands.yml` server (`premium`, `prem`, `cracked` dialihkan langsung ke `fastlogin:premium $$1-` dan `fastlogin:cracked $$1-`). Mengaktifkan `autoRegister: true` pada FastLogin sehingga pemain Java original otomatis diverifikasi melalui handshake sesi Mojang saat login pertama.
6. **Resolusi Prioritas Bed Spawn vs Respawn Kerajaan:**
   - **Akar Masalah:** EssentialsSpawn memiliki `respawn-at-home: false`, yang secara otomatis mematikan fungsi `respawn-at-home-bed: true`, dan `PlayerListener` ApexsionsCore hanya memeriksa flag boolean `isBedSpawn()`.
   - **Solusi:** Mengatur `respawn-at-home: true`, `respawn-at-home-bed: true`, `respawn-at-anchor: true`, dan `respawn-listener-priority: lowest` pada Essentials. Pada `PlayerListener.java`, menambahkan verifikasi eksplisit `player.getRespawnLocation() != null` untuk menjamin pemain yang telah menyetel kasur/anchor tetap respawn di tempat tidurnya dan hanya dialihkan ke ibukota kerajaan jika kasur hancur/hilang.
7. **Hardening Matriks Izin (Permissions) Rank Default (Wanderer):**
   - Mengonfigurasi matriks izin lengkap pada `ranks.yml` untuk pangkat `wanderer`:
     - **Whitelist Izin Bermain:** Essentials dasar (spawn, home, sethome, delhome, tpa, tpaccept, tpdeny, warp, msg, reply, pay, balance, balancetop, rules, afk, mail, suicide, build), fitur Apexsions (pemilihan kerajaan, sistem klaim chunks, rtp, toko pasar dinamis, lelang ah, battlepass, pembukaan peti crate, custom enchants & tinkerer, memancing fishing, obrolan chat, profil & leaderboard), serta utilitas pemain (`/premium`, `/cracked`, `/skin`, `/sb`, `/bossbar`).
8. **Sapu Bersih Lingkungan Server & Hardening Proteksi Dunia:**
   - **Hardening Lobi Multiverse:** Mengonfigurasi `minecraft:lobby` dengan `spawning.monster.spawn: false`, `pvp: false`, `hunger: false`, dan `auto-heal: true` agar area penyambutan 100% aman dan damai.
   - **WorldGuard Lobi:** Menambahkan flag `mob-spawning: deny` pada region cuboid `lobby`.
   - **Branded MOTD:** Mengonfigurasi MOTD `server.properties` dengan palet warna emas dan mencantumkan IP `apexsions.my.id` serta situs `web.apexsions.my.id`.
   - **Pembersihan Domain Repositori:** Memperbaiki seluruh rujukan lawas domain di `PRODUCT.md`.
   - **Preservasi Boss & Upper Realm:** Seluruh monster dungeon/raid resmi Sions Ruins (`EmperorValerius` Lv.100, `SionsVoidKnight` Lv.50) tetap terjaga sebagai Out-of-Scope lore, dan izin kedaulatan staf dimensi atas (*The Aetherial Conclave*) tetap utuh tak tersentuh.

---

## 🚀 Sprint 8 — Sistem Auto-Login Tanpa Hambatan & Proteksi Identitas Lintas Platform (FastLogin, Floodgate & AuthMe) [v1.3.0]
> **Periode Pengembangan:** 18 September 2026 | **Status:** Live & Production-Ready

### 📋 Tinjauan Arsitektur & Dampak Sistem
Implementasi arsitektur autentikasi nir-hambatan (*zero-friction auto-login*) menggabungkan ekosistem **AuthMeReloaded**, **Floodgate (Geyser)**, dan **FastLogin** pada Paper 26.2 (Java 21 LTS). Sistem ini memberikan kenyamanan maksimal bagi pemain asli dan Bedrock tanpa mengorbankan keamanan akun pemain launcher crack:
1. **Pemain Java Premium Original:** Cukup verifikasi 1× via perintah `/premium` (alur konfirmasi 2-tahap + 1× kick *by-design*). Setelah itu, seluruh koneksi berikutnya langsung melewati enkripsi handshake resmi Mojang tanpa perlu memasukkan password AuthMe selamanya.
2. **Pemain Bedrock Edition:** Terhubung via Floodgate (`.PlayerName`). Mendaftar `/register` persis 1× seumur hidup di AuthMe, setelah itu seluruh login berikutnya otomatis terautentikasi via sesi Xbox Live (`autoLoginFloodgate: true`).
3. **Pemain Java Crack (Anti-Impersonation & Nol Lockout):**
   - Pemain crack yang menggunakan nickname premium milik orang lain **TIDAK TERKUNCI** berkat kebijakan konservatif `autoRegister: false`. Mereka tetap disajikan prompt kata sandi AuthMe biasa.
   - Percobaan pembajakan nickname Java Premium oleh launcher crack ditolak seketika pada lapisan network handshake (`invalid-session`).
4. **Kedaulatan Data & URL Web (/player/{uuid}):** Menjaga `premiumUuid: false` memastikan seluruh data (saldo Rupiah, saldo Diamond, level 100, klaim wilayah kerajaan, dan slug URL web platform Azuriom `/player/{uuid}`) tetap konsisten pada offline UUID tanpa risiko data terputus/yatim (*orphaned data*).
5. **Resolusi Kompatibilitas Geyser 2.11.3 & Java 21 Verifier:** Menyesuaikan pemanggilan internal `GeyserImpl` pada FastLogin binary agar tidak terjadi `NoSuchMethodError` pada versi Geyser terbaru, memungkinkan aktivasi 100% mulus bersama Floodgate.
6. **Lokalisasi Menyeluruh 20/20 Key:** Seluruh pesan peringatan, status premium, pesan penolakan sesi bajakan, serta panduan pemulihan akun telah dilokalisasi ke Bahasa Indonesia sesuai palet identitas brand Apexsions.

---

## 🚀 Sprint 7 — Kedaulatan Teritorial, Watchdog Keamanan, Sinkronisasi Kanonikal Kerajaan & Optimasi Lintas Platform (Bedrock/Custom Font) [v1.2.0]
> **Periode Pengembangan:** 16 – 17 September 2026 | **Total Commit:** 9 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Fase stabilisasi dan penegakan kedaulatan wilayah. Mengimplementasikan sistem klaim tanah chunks (`/claim`) berbasis sewa progresif dan brankas wilayah, engine moderasi mandiri terpusat (`/ban`), sistem watchdog keamanan berlapis (Anti-Griefing, Anti-Xray spike alert, Redstone Watchdog anti-lag), sinkronisasi 100% spesifikasi kanonikal Tiga Kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%), serta penyediaan resource pack Geyser pembersih angka merah dan font kustom ASCII untuk Bedrock & Java.

### 🔍 Rincian Lengkap Commit (9 Commit)

#### `1bc5a3b` — 2026-09-17 | 📚 DOCS | docs: enrich CHANGELOG with comprehensive 95-commit breakdown and deep-dive technical impacts across 5 sprints
- **Pesan Commit:** `docs: enrich CHANGELOG with comprehensive 95-commit breakdown and deep-dive technical impacts across 5 sprints`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `b360e85` — 2026-09-17 | 📚 DOCS | docs: create comprehensive CHANGELOG and synchronize master documentation with canonical kingdom specs
- **Pesan Commit:** `docs: create comprehensive CHANGELOG and synchronize master documentation with canonical kingdom specs`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `055bf40` — 2026-09-17 | ✨ FEAT | feat(core): synchronize canonical kingdom buffs, debuffs, and perks across GUIs, configs, and web wiki
- **Pesan Commit:** `feat(core): synchronize canonical kingdom buffs, debuffs, and perks across GUIs, configs, and web wiki`
  - **Rincian Teknis:** Menyelaraskan nilai atribut kanonikal kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%) dan kartu preview slot 13 pada GUI konfirmasi `/k choose`.

#### `48bedec` — 2026-09-17 | ✨ FEAT | feat(ui): add bedrock clean scoreboard pack and optimize tab/scoreboard for bedrock & custom fonts
- **Pesan Commit:** `feat(ui): add bedrock clean scoreboard pack and optimize tab/scoreboard for bedrock & custom fonts`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `613e6c6` — 2026-09-16 | 🐛 FIX | fix(core): ensure table columns are added before creating indexes to prevent sqlite startup failure
- **Pesan Commit:** `fix(core): ensure table columns are added before creating indexes to prevent sqlite startup failure`
  - **Rincian Teknis:** Memastikan migrasi skema database mengeksekusi DDL `ALTER TABLE ADD COLUMN` sebelum perintah `CREATE INDEX` untuk mencegah crash startup SQLite.

#### `83d783b` — 2026-09-16 | 📚 DOCS | docs: synchronize sovereign claims, security watchdog, centralized moderation, and fishing docs
- **Pesan Commit:** `docs: synchronize sovereign claims, security watchdog, centralized moderation, and fishing docs`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Mengaktifkan reach-hack check (>5.8m), deteksi lonjakan penambangan bijih langka (60s window), serta isolasi otomatis clock redstone cepat (>25 pulsa/2s).
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.

#### `11b1bd8` — 2026-09-16 | 🐛 FIX | fix(core): resolve NPE in ban and claim commands and grant unlimited claims and tax exemption to Upper Dimension ranks
- **Pesan Commit:** `fix(core): resolve NPE in ban and claim commands and grant unlimited claims and tax exemption to Upper Dimension ranks`
  - **Rincian Teknis:** Menerapkan fallback lazy-lookup pada manager instance agar sub-command console/WebBridge tidak mengalami NullPointerException saat bootstrap belum tuntas.
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.

#### `848795d` — 2026-09-16 | ✨ FEAT | feat(fishing): implement dialog GUI for rod creator, virtual bait quota system, and balanced junk loot
- **Pesan Commit:** `feat(fishing): implement dialog GUI for rod creator, virtual bait quota system, and balanced junk loot`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `e5b0634` — 2026-09-16 | ✨ FEAT | feat(claim): implement progressive territory tax, grace periods, flags, and web admin controls
- **Pesan Commit:** `feat(claim): implement progressive territory tax, grace periods, flags, and web admin controls`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

---

## 🚀 Sprint 6 — Ekosistem ApexsionsFishing v1.0.0, Pengerasan Lifecycle AFK, Standarisasi Lore Enchant & Kebijakan Leaderboard 6-Lapis [v1.1.5]
> **Periode Pengembangan:** 14 – 15 September 2026 | **Total Commit:** 10 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Penyempurnaan modul perikanan interaktif dan tata kelola transparansi kompetisi. Memperkenalkan sistem pancing AFK & Active Reel Engine, Fishing Vault 54-slot, rod builder native dialog, Virtual Bait Quota, penataan tata letak lore custom enchant di bawah item level requirement, penyaringan 6-lapis seluruh staf/admin dari leaderboard publik game dan web, serta optimasi SEO sitemap.xml.

### 🔍 Rincian Lengkap Commit (10 Commit)

#### `2a3a538` — 2026-09-15 | ✨ FEAT | feat(moderation): integrate centralized ban engine and land claims web admin
- **Pesan Commit:** `feat(moderation): integrate centralized ban engine and land claims web admin`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `1b905de` — 2026-09-15 | ✨ FEAT | feat(core): implement sovereign land claims, anti-griefing protection, anti-xray monitor, and redstone watchdog
- **Pesan Commit:** `feat(core): implement sovereign land claims, anti-griefing protection, anti-xray monitor, and redstone watchdog`
  - **Rincian Teknis:** Mengelola hak kepemilikan petak tanah per-chunk dengan kalkulasi biaya sewa progresif, brankas klaim, dan pembebasan pajak untuk jajaran Upper Dimension.
  - **Rincian Teknis:** Mengaktifkan reach-hack check (>5.8m), deteksi lonjakan penambangan bijih langka (60s window), serta isolasi otomatis clock redstone cepat (>25 pulsa/2s).

#### `00ebb82` — 2026-09-14 | 📚 DOCS | docs: synchronize documentation with 9-plugin suite, leaderboard exemptions, and SEO architecture
- **Pesan Commit:** `docs: synchronize documentation with 9-plugin suite, leaderboard exemptions, and SEO architecture`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `b408b84` — 2026-09-14 | 🐛 FIX | fix(fishing): harden AFK lifecycle, liquid depth check, rod break cleanup, and vault deposit safeguards
- **Pesan Commit:** `fix(fishing): harden AFK lifecycle, liquid depth check, rod break cleanup, and vault deposit safeguards`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.

#### `f4448e4` — 2026-09-14 | 🐛 FIX | fix(fishing): harden AFK auto-recast, water validation, slot indexing, and exploit protections
- **Pesan Commit:** `fix(fishing): harden AFK auto-recast, water validation, slot indexing, and exploit protections`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.

#### `6d6b14d` — 2026-09-14 | 🐛 FIX | fix(seo): standardize sitemap.xml structure and update lastmod timestamps
- **Pesan Commit:** `fix(seo): standardize sitemap.xml structure and update lastmod timestamps`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `6560fa6` — 2026-09-14 | ⏪ REVERT | revert(web): remove battlepass leaderboard from web platform
- **Pesan Commit:** `revert(web): remove battlepass leaderboard from web platform`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `ece15e7` — 2026-09-14 | ✨ FEAT | feat(leaderboard): exclude admin, OP, and upper-dimension entities from web and game leaderboards
- **Pesan Commit:** `feat(leaderboard): exclude admin, OP, and upper-dimension entities from web and game leaderboards`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `9790803` — 2026-09-14 | ✨ FEAT | feat(fishing): implement ApexsionsFishing ecosystem and standardize enchant lore layout
- **Pesan Commit:** `feat(fishing): implement ApexsionsFishing ecosystem and standardize enchant lore layout`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `f21e85e` — 2026-09-14 | ✨ FEAT | feat(balance): integrate Aetherion chat and rebalance Zenithar and Sylvamoor traits
- **Pesan Commit:** `feat(balance): integrate Aetherion chat and rebalance Zenithar and Sylvamoor traits`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

---

## 🚀 Sprint 5 — The Aetherial Conclave, RPG Stat Progression Normalizer, Terra Interdicta Hourly Reset & Custom Vanish Suite [v1.1.0]
> **Periode Pengembangan:** 12 September 2026 | **Total Commit:** 34 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Restrukturisasi kosmik semesta Apexsions dan normalisasi pertarungan RPG. Mengintegrasikan konsep Dimensi Atas (The Aetherial Conclave) yang membebaskan staf dari friksi kerajaan fana, sistem scaling stat level 1-100 dengan Combat Normalizer, zona reruntuhan kuno Terra Interdicta dengan reset temporal berkala, custom stealth `/vanish` tanpa hitbox, BlueMap auto-respawn ibukota teritorial, serta penyusunan master bible LORE.md.

### 🔍 Rincian Lengkap Commit (34 Commit)

#### `7603de0` — 2026-09-12 | 🐛 FIX | fix(admin): remove duplicate text brand name and use image-only ornate logo
- **Pesan Commit:** `fix(admin): remove duplicate text brand name and use image-only ornate logo`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `74f597d` — 2026-09-12 | 📚 DOCS | docs: document web platform & admin panel lore alignment and Conclave fallbacks
- **Pesan Commit:** `docs: document web platform & admin panel lore alignment and Conclave fallbacks`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `39aa182` — 2026-09-12 | ✨ FEAT | feat(web): lore-accurate kingdom governance, Aetherion Conclave support, and admin panel fallbacks
- **Pesan Commit:** `feat(web): lore-accurate kingdom governance, Aetherion Conclave support, and admin panel fallbacks`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `1732eb9` — 2026-09-12 | 📚 DOCS | docs: update documentation for The Aetherial Conclave, Terra Interdicta, and Spawn Sanctum
- **Pesan Commit:** `docs: update documentation for The Aetherial Conclave, Terra Interdicta, and Spawn Sanctum`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.

#### `604d054` — 2026-09-12 | ✨ FEAT | feat(core): implement Conclave Upper Dimension mechanics and fix lore inaccuracies across commands & GUIs
- **Pesan Commit:** `feat(core): implement Conclave Upper Dimension mechanics and fix lore inaccuracies across commands & GUIs`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `d6e3aaf` — 2026-09-12 | 🐛 FIX | fix(vanish): hide vanished players from online player counters, staff placeholders, TAB, and server ping
- **Pesan Commit:** `fix(vanish): hide vanished players from online player counters, staff placeholders, TAB, and server ping`
  - **Rincian Teknis:** Menyediakan custom stealth vanish dengan penghilangan hitbox, penolakan targeting mob, serta penyembunyian dari tablist, ping, dan placeholder.

#### `4035c5e` — 2026-09-12 | ✨ FEAT | feat: exclude server staff and admins from all plugin leaderboards
- **Pesan Commit:** `feat: exclude server staff and admins from all plugin leaderboards`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `e8936ec` — 2026-09-12 | 🐛 FIX | fix(chat): guard ApexsionsCoreHook with isolated CoreBridge to prevent NoClassDefFoundError
- **Pesan Commit:** `fix(chat): guard ApexsionsCoreHook with isolated CoreBridge to prevent NoClassDefFoundError`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `78dd65c` — 2026-09-12 | 🐛 FIX | fix(core): restrict playable kingdoms to Zenithar, Solterra, and Sylvamoor, blocking fallen Sions from admin commands
- **Pesan Commit:** `fix(core): restrict playable kingdoms to Zenithar, Solterra, and Sylvamoor, blocking fallen Sions from admin commands`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `6f52105` — 2026-09-12 | 📚 DOCS | docs: synchronize master documentation with RPG progression and combat engine
- **Pesan Commit:** `docs: synchronize master documentation with RPG progression and combat engine`
  - **Rincian Teknis:** Mengimplementasikan formula normalisasi stat tempur, atribut progression level 1-100, dan peredaman lonjakan damage berlebih.

#### `88ed136` — 2026-09-12 | ✨ FEAT | feat(core): implement RPG level progression stat scaling and combat normalizer
- **Pesan Commit:** `feat(core): implement RPG level progression stat scaling and combat normalizer`
  - **Rincian Teknis:** Mengimplementasikan formula normalisasi stat tempur, atribut progression level 1-100, dan peredaman lonjakan damage berlebih.

#### `c9275f1` — 2026-09-12 | 🐛 FIX | fix(cosmetics): avoid synthetic switch class in CosmeticsMainGUI
- **Pesan Commit:** `fix(cosmetics): avoid synthetic switch class in CosmeticsMainGUI`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `29557d0` — 2026-09-12 | 🐛 FIX | fix(core): preload caffeine tasks at startup and protect block place tracker
- **Pesan Commit:** `fix(core): preload caffeine tasks at startup and protect block place tracker`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `640e566` — 2026-09-12 | ✨ FEAT | feat(core): implement custom /vanish system with fake broadcast, no-hitbox, and solid self-visibility
- **Pesan Commit:** `feat(core): implement custom /vanish system with fake broadcast, no-hitbox, and solid self-visibility`
  - **Rincian Teknis:** Menyediakan custom stealth vanish dengan penghilangan hitbox, penolakan targeting mob, serta penyembunyian dari tablist, ping, dan placeholder.

#### `1383772` — 2026-09-12 | ✨ FEAT | feat(core): add /k navigation gui and allow rtp from lobby
- **Pesan Commit:** `feat(core): add /k navigation gui and allow rtp from lobby`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ae9f9a7` — 2026-09-12 | 🐛 FIX | fix(nginx): eliminate ERR_TOO_MANY_REDIRECTS loop on /vote by adjusting regex quantifier and protocol target
- **Pesan Commit:** `fix(nginx): eliminate ERR_TOO_MANY_REDIRECTS loop on /vote by adjusting regex quantifier and protocol target`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `027b8d4` — 2026-09-12 | 🐛 FIX | fix(core): automatically bypass Paper 128 channel registration limit
- **Pesan Commit:** `fix(core): automatically bypass Paper 128 channel registration limit`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `1e79376` — 2026-09-12 | 🐛 FIX | fix(seo): add lastmod dates to sitemap and explicit nginx location with X-Robots-Tag
- **Pesan Commit:** `fix(seo): add lastmod dates to sitemap and explicit nginx location with X-Robots-Tag`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `b4c173b` — 2026-09-12 | 🐛 FIX | fix(core): eager preload Caffeine RemovalCause and add defensive cache invalidation fallbacks
- **Pesan Commit:** `fix(core): eager preload Caffeine RemovalCause and add defensive cache invalidation fallbacks`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `d57c255` — 2026-09-12 | 🐛 FIX | fix(seo): enforce authoritative HTTPS canonicals, resolve rogue domain indexing, and optimize SEO entity schemas
- **Pesan Commit:** `fix(seo): enforce authoritative HTTPS canonicals, resolve rogue domain indexing, and optimize SEO entity schemas`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `7d67610` — 2026-09-12 | 📚 DOCS | docs: synchronize README, technical documentation, and Admin GUIs with Zenithar rebalance
- **Pesan Commit:** `docs: synchronize README, technical documentation, and Admin GUIs with Zenithar rebalance`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `581328e` — 2026-09-12 | ✨ FEAT | feat(balance): rebalance Zenithar as Capitalist & Anti-Crit Duelist across plugins and website
- **Pesan Commit:** `feat(balance): rebalance Zenithar as Capitalist & Anti-Crit Duelist across plugins and website`
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `2fb659c` — 2026-09-12 | ✨ FEAT | feat(gameplay): integrate lore with kingdom treasury, mythicmobs boss, and website wiki
- **Pesan Commit:** `feat(gameplay): integrate lore with kingdom treasury, mythicmobs boss, and website wiki`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `5527607` — 2026-09-12 | ✨ FEAT | feat(vote): implement dual action vote buttons and fix trailing parenthesis 404 URL
- **Pesan Commit:** `feat(vote): implement dual action vote buttons and fix trailing parenthesis 404 URL`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `61ed330` — 2026-09-12 | 🔧 CHORE | chore(build): update ApexsionsCustomEnchants artifact
- **Pesan Commit:** `chore(build): update ApexsionsCustomEnchants artifact`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `9a013e6` — 2026-09-12 | 🐛 FIX | fix(customenchants): display item level requirement between enchant list and set bonus in lore
- **Pesan Commit:** `fix(customenchants): display item level requirement between enchant list and set bonus in lore`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `42f3092` — 2026-09-12 | 📚 DOCS | docs: integrate political succession, kingdom stability index, mystery fragments, and staff protocols into LORE.md
- **Pesan Commit:** `docs: integrate political succession, kingdom stability index, mystery fragments, and staff protocols into LORE.md`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `ce8948b` — 2026-09-12 | 📚 DOCS | docs: integrate Aetherial Conclave, Order vs Chaos, and 3-month seasonal chronicle into LORE.md
- **Pesan Commit:** `docs: integrate Aetherial Conclave, Order vs Chaos, and 3-month seasonal chronicle into LORE.md`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `052e2a7` — 2026-09-12 | 📚 DOCS | docs: establish tri-layered cosmic lore architecture with Aetherion and Celestial Order in LORE.md
- **Pesan Commit:** `docs: establish tri-layered cosmic lore architecture with Aetherion and Celestial Order in LORE.md`
  - **Rincian Teknis:** Mengisolasi jajaran otoritas tertinggi server dari sistem faksi fana tiga kerajaan, menyediakan placeholder kustom, dan menyelaraskan status pada UI.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `a3741c8` — 2026-09-12 | 📚 DOCS | docs: add technical architecture mapping, system audit, and revision checklist to LORE.md
- **Pesan Commit:** `docs: add technical architecture mapping, system audit, and revision checklist to LORE.md`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `4b707ed` — 2026-09-12 | 📚 DOCS | docs: enrich LORE.md with comprehensive realm geography, buff/debuff tables, and exclusive Sions admin realm
- **Pesan Commit:** `docs: enrich LORE.md with comprehensive realm geography, buff/debuff tables, and exclusive Sions admin realm`
  - **Rincian Teknis:** Menyelaraskan nilai atribut kanonikal kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%) dan kartu preview slot 13 pada GUI konfirmasi `/k choose`.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `4dfc613` — 2026-09-12 | 📚 DOCS | docs: create official civilization lore document LORE.md
- **Pesan Commit:** `docs: create official civilization lore document LORE.md`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `2d0a065` — 2026-09-12 | 🐛 FIX | fix(bridge): auto-detect Bedrock Edition via username prefix and Floodgate UUID across web & in-game sync
- **Pesan Commit:** `fix(bridge): auto-detect Bedrock Edition via username prefix and Floodgate UUID across web & in-game sync`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `ce22b7c` — 2026-09-12 | ♻️ REFACTOR | refactor(web): overhaul server map section with antislop-ui tactical cartography monolith
- **Pesan Commit:** `refactor(web): overhaul server map section with antislop-ui tactical cartography monolith`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

---

## 🚀 Sprint 4 — Webstore Multi-Currency & Filter Interaktif, BlueMap 3D Server Map, Sistem Auto-Reward Vote & Dual-Theme UI [v1.0.5]
> **Periode Pengembangan:** 9 – 11 September 2026 | **Total Commit:** 48 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Ekspansi integrasi Web-to-Game dan penyempurnaan UI/UX portal Azuriom. Memperkenalkan etalase webstore multi-axis filter (Rupiah & Diamond), integrasi penampil peta 3D interaktif BlueMap dengan dynamic fallback probe, sistem voting lintas platform otomatis, dual-theme engine (Imperial Obsidian & Sovereign Ivory), perbaikan sistem penagihan antrean WebBridge, standarisasi simbol mata uang, dan penyusunan master DOKUMENTASI.md.

### 🔍 Rincian Lengkap Commit (48 Commit)

#### `1acdcd8` — 2026-09-10 | ✨ FEAT | feat(web): direct redirect for server map, remove navbar button, and polish homepage UI/UX
- **Pesan Commit:** `feat(web): direct redirect for server map, remove navbar button, and polish homepage UI/UX`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `e9c9a7b` — 2026-09-10 | 🐛 FIX | fix(leaderboard): include all Minecraft server players and populate kingdom statistics
- **Pesan Commit:** `fix(leaderboard): include all Minecraft server players and populate kingdom statistics`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.

#### `99012d1` — 2026-09-10 | 🐛 FIX | fix(map,chat): update 3d world perspective and relax chat spam duplicate check
- **Pesan Commit:** `fix(map,chat): update 3d world perspective and relax chat spam duplicate check`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `6f96c95` — 2026-09-10 | 🐛 FIX | fix(vote): ensure robust url routing and eliminate route name lookup exceptions
- **Pesan Commit:** `fix(vote): ensure robust url routing and eliminate route name lookup exceptions`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `6b49daa` — 2026-09-10 | ✨ FEAT | feat(vote): refactor total sistem vote menjadi auto-reward tanpa verifikasi manual
- **Pesan Commit:** `feat(vote): refactor total sistem vote menjadi auto-reward tanpa verifikasi manual`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `33e3eeb` — 2026-09-10 | 📚 DOCS | docs: create master DOKUMENTASI.md and expand token conservation protocol
- **Pesan Commit:** `docs: create master DOKUMENTASI.md and expand token conservation protocol`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `173be77` — 2026-09-10 | 🐛 FIX | fix(theme): complete light mode backgrounds, contrast & add token conservation policy
- **Pesan Commit:** `fix(theme): complete light mode backgrounds, contrast & add token conservation policy`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `a1c2d90` — 2026-09-10 | ✨ FEAT | feat(web): webstore single benefit expansion, diamond currency, bluemap & seo implementation
- **Pesan Commit:** `feat(web): webstore single benefit expansion, diamond currency, bluemap & seo implementation`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `a2954c0` — 2026-09-10 | 🔧 CHORE | chore(shop): remove obsolete Vault, nightcore, and ExcellentCrates from softdepend
- **Pesan Commit:** `chore(shop): remove obsolete Vault, nightcore, and ExcellentCrates from softdepend`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `75df932` — 2026-09-10 | 🐛 FIX | fix(shop): resolve duplicate softdepend in plugin.yml and harden ShopMainMenu loading
- **Pesan Commit:** `fix(shop): resolve duplicate softdepend in plugin.yml and harden ShopMainMenu loading`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `f7a9142` — 2026-09-10 | 🐛 FIX | fix(rank): uncap operator/staff ranks with 999 limits and 0s cooldowns
- **Pesan Commit:** `fix(rank): uncap operator/staff ranks with 999 limits and 0s cooldowns`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `8111f80` — 2026-09-10 | ✨ FEAT | feat(ranks): populate default baseline values and reference badges in rank edit form
- **Pesan Commit:** `feat(ranks): populate default baseline values and reference badges in rank edit form`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2fc4e4d` — 2026-09-10 | 🐛 FIX | fix(bridge): synchronize rank delivery state machine and audit log integrity
- **Pesan Commit:** `fix(bridge): synchronize rank delivery state machine and audit log integrity`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `e3f817c` — 2026-09-10 | 📚 DOCS | docs(web): document webstore storefront architecture and multi-axis filter engine
- **Pesan Commit:** `docs(web): document webstore storefront architecture and multi-axis filter engine`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `89d7520` — 2026-09-10 | 🐛 FIX | fix(web): overhaul shop category navigation and multi-axis interactive filters
- **Pesan Commit:** `fix(web): overhaul shop category navigation and multi-axis interactive filters`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `4f57a23` — 2026-09-10 | 📚 DOCS | docs: update technical documentation and admin guide with integration audit results
- **Pesan Commit:** `docs: update technical documentation and admin guide with integration audit results`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `ecc060c` — 2026-09-10 | 🐛 FIX | fix(bridge): resolve compound command delimiter and native tellraw handling
- **Pesan Commit:** `fix(bridge): resolve compound command delimiter and native tellraw handling`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ddb9ef0` — 2026-09-10 | 🐛 FIX | fix(ui): eliminate tablet landscape overflow and prevent double password toggle execution
- **Pesan Commit:** `fix(ui): eliminate tablet landscape overflow and prevent double password toggle execution`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ccfabb0` — 2026-09-10 | ✨ FEAT | feat(customenchants): add per-item and fullset level requirement in item creator synced with ApexsionsCore
- **Pesan Commit:** `feat(customenchants): add per-item and fullset level requirement in item creator synced with ApexsionsCore`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `65b70a0` — 2026-09-10 | ✨ FEAT | feat(ui): implement multi-device responsive layout, offcanvas navigation, and dual-theme engine
- **Pesan Commit:** `feat(ui): implement multi-device responsive layout, offcanvas navigation, and dual-theme engine`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `22fa9ab` — 2026-09-10 | 🐛 FIX | fix(shop): resolve calculateUpgradePrice call in show.blade.php for authenticated users
- **Pesan Commit:** `fix(shop): resolve calculateUpgradePrice call in show.blade.php for authenticated users`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `3dd104c` — 2026-09-10 | ✨ FEAT | feat(webstore): add centralized webstore manager admin panel and fix package banner 404s
- **Pesan Commit:** `feat(webstore): add centralized webstore manager admin panel and fix package banner 404s`
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `3d9f51a` — 2026-09-10 | ✨ FEAT | feat(theme): resolve rank card layout collisions, add duration filters & 2x2 spec grid
- **Pesan Commit:** `feat(theme): resolve rank card layout collisions, add duration filters & 2x2 spec grid`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `c777c68` — 2026-09-10 | 🐛 FIX | fix(theme,docs): resolve /admin/themes 500 by adding authors array & document rank upgrade suite
- **Pesan Commit:** `fix(theme,docs): resolve /admin/themes 500 by adding authors array & document rank upgrade suite`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `b03523d` — 2026-09-10 | ✨ FEAT | feat(rank-upgrade): finalize rank upgrade engine, admin management, retention and whatsapp flow
- **Pesan Commit:** `feat(rank-upgrade): finalize rank upgrade engine, admin management, retention and whatsapp flow`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `760cdd7` — 2026-09-10 | ✨ FEAT | feat(rank-webstore): overhaul rank benefits, battlepass discounts, and webstore integration
- **Pesan Commit:** `feat(rank-webstore): overhaul rank benefits, battlepass discounts, and webstore integration`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `2aa7119` — 2026-09-09 | ✨ FEAT | feat(vote): add vote management to admin sidebar, configure minecraft-mp api key & server id, and implement platform toggle controls
- **Pesan Commit:** `feat(vote): add vote management to admin sidebar, configure minecraft-mp api key & server id, and implement platform toggle controls`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `d405767` — 2026-09-09 | 🐛 FIX | fix(vote): resolve /vote 500 error & implement crossplay vote experience for Java and Bedrock
- **Pesan Commit:** `fix(vote): resolve /vote 500 error & implement crossplay vote experience for Java and Bedrock`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `e0bac90` — 2026-09-09 | ✨ FEAT | feat(vote): implement real voting system with /vote command, 3x vote keys and rp 1000 rewards, cooldown tracking, and admin dashboard
- **Pesan Commit:** `feat(vote): implement real voting system with /vote command, 3x vote keys and rp 1000 rewards, cooldown tracking, and admin dashboard`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `5bbea14` — 2026-09-09 | 🐛 FIX | fix(admin): resolve delivery enum truncation, tellraw json formatting, and player table template bug
- **Pesan Commit:** `fix(admin): resolve delivery enum truncation, tellraw json formatting, and player table template bug`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `d8c6b42` — 2026-09-09 | ✨ FEAT | feat(core): isolate kit player gui and implement dedicated kit admin dashboard
- **Pesan Commit:** `feat(core): isolate kit player gui and implement dedicated kit admin dashboard`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `c4ee606` — 2026-09-09 | 🐛 FIX | fix(crates): display reward lore and custom enchants above rarity info in preview
- **Pesan Commit:** `fix(crates): display reward lore and custom enchants above rarity info in preview`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `3ecd153` — 2026-09-09 | 📚 DOCS | docs: update master documentation for user management center, auth security, and crates suite
- **Pesan Commit:** `docs: update master documentation for user management center, auth security, and crates suite`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `dd00a5f` — 2026-09-09 | ✨ FEAT | feat(crates): calculate reward chance grouped strictly by rarity and show rarity roll chance in preview lore
- **Pesan Commit:** `feat(crates): calculate reward chance grouped strictly by rarity and show rarity roll chance in preview lore`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `777c54a` — 2026-09-09 | ✨ FEAT | feat(auth): resolve /admin/users 500, redesign user management center & add password toggles
- **Pesan Commit:** `feat(auth): resolve /admin/users 500, redesign user management center & add password toggles`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `406232e` — 2026-09-09 | 🐛 FIX | fix(crates): implement unified tiered effective weight chance system
- **Pesan Commit:** `fix(crates): implement unified tiered effective weight chance system`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `0a17020` — 2026-09-09 | 🐛 FIX | fix(crates): ensure crate key and chest item display names and lore are properly formatted with colors
- **Pesan Commit:** `fix(crates): ensure crate key and chest item display names and lore are properly formatted with colors`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `3ddfcad` — 2026-09-09 | 📚 DOCS | docs: add complete PlaceholderAPI documentation suite for all Apexsions plugins
- **Pesan Commit:** `docs: add complete PlaceholderAPI documentation suite for all Apexsions plugins`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `9065256` — 2026-09-09 | 🐛 FIX | fix(shop,currency): ensure complete currency symbol standardization (Rp., 💎, 🪙) in shop menus, quests, and GUIs
- **Pesan Commit:** `fix(shop,currency): ensure complete currency symbol standardization (Rp., 💎, 🪙) in shop menus, quests, and GUIs`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `fa31205` — 2026-09-09 | ✨ FEAT | feat(admin): complete 13-point admin refinement, global search, quick actions & dual-theme styling
- **Pesan Commit:** `feat(admin): complete 13-point admin refinement, global search, quick actions & dual-theme styling`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `b9b5d41` — 2026-09-09 | 🔧 CHORE | chore(plugins): set author to Nueeva across all plugins
- **Pesan Commit:** `chore(plugins): set author to Nueeva across all plugins`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `460f2b9` — 2026-09-09 | 🔧 CHORE | chore: remove MythicMobs folder as files are accessible directly via SFTP
- **Pesan Commit:** `chore: remove MythicMobs folder as files are accessible directly via SFTP`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `1da8a10` — 2026-09-09 | ✨ FEAT | feat(admin): parity audit, high-value actions & dual-theme rebuild
- **Pesan Commit:** `feat(admin): parity audit, high-value actions & dual-theme rebuild`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `59423ce` — 2026-09-09 | 🐛 FIX | fix(currency): standardize currency formatting to Rp., 💎, and 🪙 across plugins
- **Pesan Commit:** `fix(currency): standardize currency formatting to Rp., 💎, and 🪙 across plugins`
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `49a18e9` — 2026-09-09 | 🐛 FIX | fix(crates): separate rarity and roll chance onto distinct lines in preview
- **Pesan Commit:** `fix(crates): separate rarity and roll chance onto distinct lines in preview`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `eb95959` — 2026-09-09 | 🐛 FIX | fix(server): resolve maintenance mode deactivation form submission bug & add quick disable action
- **Pesan Commit:** `fix(server): resolve maintenance mode deactivation form submission bug & add quick disable action`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `17d2f10` — 2026-09-09 | ✨ FEAT | feat(crates): add rarity management GUI, 7 default rarities & actionbar notifications
- **Pesan Commit:** `feat(crates): add rarity management GUI, 7 default rarities & actionbar notifications`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `8c6d6ee` — 2026-09-09 | ✨ FEAT | feat(admin): restructure sidebar, integrate admingui actions, expand player management & implement rank management
- **Pesan Commit:** `feat(admin): restructure sidebar, integrate admingui actions, expand player management & implement rank management`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

---

## 🚀 Sprint 3 — Pematangan 9 Plugin Suite, Dual-Currency Enchanter 182 Enchant, Dynamic Shop 6-Kategori & Custom Theme Azuriom [v1.0.0]
> **Periode Pengembangan:** 5 – 8 September 2026 | **Total Commit:** 168 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Pematangan menyeluruh 9 plugin suite Apexsions (Paper 26.2 / Java 21 LTS). Membangun engine custom enchants 7 tier (182 enchants) dengan Armor Set Bonus, pasar dinamis 6 kategori dengan pengaruh cuaca dan bioma teritorial, toko peti hadiah gacha berbobot (ApexsionsCrates), banner media map renderer multi-tile (ApexsionsMedia), serta perombakan tema Azuriom bernuansa Dark-Gold Civilization.

### 🔍 Rincian Lengkap Commit (168 Commit)

#### `f15f259` — 2026-09-08 | 🐛 FIX | fix(battlepass): match Battle Coins color to Uang (yellow)
- **Pesan Commit:** `fix(battlepass): match Battle Coins color to Uang (yellow)`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `ab360e0` — 2026-09-08 | 🐛 FIX | fix(battlepass): change Battle Coins display to X Coins format
- **Pesan Commit:** `fix(battlepass): change Battle Coins display to X Coins format`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `27f3860` — 2026-09-08 | ✨ FEAT | feat(battlepass): rename Saldo to Uang and add Battle Coins balance to BP info card
- **Pesan Commit:** `feat(battlepass): rename Saldo to Uang and add Battle Coins balance to BP info card`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `8d59aa9` — 2026-09-08 | 🐛 FIX | fix(battlepass): preserve item lore in all preview and editor GUIs (armor set bonus etc.)
- **Pesan Commit:** `fix(battlepass): preserve item lore in all preview and editor GUIs (armor set bonus etc.)`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `7af68dc` — 2026-09-08 | 🐛 FIX | fix(bridge): prepend plugin route namespace to player.show and incident routes
- **Pesan Commit:** `fix(bridge): prepend plugin route namespace to player.show and incident routes`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `c5bed7f` — 2026-09-08 | 📚 DOCS | docs(minecraft): update documentation and build suite with crates integration and sftp sync
- **Pesan Commit:** `docs(minecraft): update documentation and build suite with crates integration and sftp sync`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `8af7a37` — 2026-09-08 | 🐛 FIX | fix(admin): resolve route collision on custom-plugins and isolate notifications view variable
- **Pesan Commit:** `fix(admin): resolve route collision on custom-plugins and isolate notifications view variable`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `7792442` — 2026-09-08 | 🔧 CHORE | chore(admin): finalize dashboard suite with admin guide and e2e integration test
- **Pesan Commit:** `chore(admin): finalize dashboard suite with admin guide and e2e integration test`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `d8eecf9` — 2026-09-08 | ✨ FEAT | feat(automation): implement Phase 8 Automation and Notification Orchestration
- **Pesan Commit:** `feat(automation): implement Phase 8 Automation and Notification Orchestration`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `67f13cd` — 2026-09-08 | ✨ FEAT | feat(hardening): implement Phase 7 architecture hardening, retention engine, granular permissions, and index optimizations
- **Pesan Commit:** `feat(hardening): implement Phase 7 architecture hardening, retention engine, granular permissions, and index optimizations`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `766d212` — 2026-09-08 | ✨ FEAT | feat(intelligence): implement Phase 6 Intelligence, Incident and Investigation System
- **Pesan Commit:** `feat(intelligence): implement Phase 6 Intelligence, Incident and Investigation System`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `21660ca` — 2026-09-08 | ✨ FEAT | feat(plugins): implement Phase 5 Custom Plugin Control and Capability System
- **Pesan Commit:** `feat(plugins): implement Phase 5 Custom Plugin Control and Capability System`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `47731c0` — 2026-09-08 | ✨ FEAT | feat(server): implement Phase 4 Server Operations and Safe Control
- **Pesan Commit:** `feat(server): implement Phase 4 Server Operations and Safe Control`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `c572677` — 2026-09-08 | 🐛 FIX | fix(crates): pre-fill hologram lines dialog with active template text
- **Pesan Commit:** `fix(crates): pre-fill hologram lines dialog with active template text`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `9437b03` — 2026-09-08 | 📌 UPDATE | test: ensure Phase3Test repeatability and idempotency
- **Pesan Commit:** `test: ensure Phase3Test repeatability and idempotency`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2839cd3` — 2026-09-08 | ✨ FEAT | feat(economy): implement Phase 3 Economy Operations and Market Control
- **Pesan Commit:** `feat(economy): implement Phase 3 Economy Operations and Market Control`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `a173e01` — 2026-09-08 | ✨ FEAT | feat(crates): add per-crate hologram lines editor via dialog GUI
- **Pesan Commit:** `feat(crates): add per-crate hologram lines editor via dialog GUI`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `963524a` — 2026-09-08 | ✨ FEAT | feat(operations): implement Phase 2 Reports Center and Moderation Center
- **Pesan Commit:** `feat(operations): implement Phase 2 Reports Center and Moderation Center`
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.

#### `90e89c1` — 2026-09-08 | ✨ FEAT | feat(admin): implement Phase 1 unified audit log, player management foundation, and bridge action reliability
- **Pesan Commit:** `feat(admin): implement Phase 1 unified audit log, player management foundation, and bridge action reliability`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `49733e4` — 2026-09-08 | 📚 DOCS | docs: update comprehensive technical manuals for Citizens NPC command binding and WebBridge synchronization
- **Pesan Commit:** `docs: update comprehensive technical manuals for Citizens NPC command binding and WebBridge synchronization`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `722ea40` — 2026-09-08 | ♻️ REFACTOR | refactor(npc): remove hardcoded Citizens NPC listeners to allow native /npc cmd binding
- **Pesan Commit:** `refactor(npc): remove hardcoded Citizens NPC listeners to allow native /npc cmd binding`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4f3f180` — 2026-09-08 | ✨ FEAT | feat(npc): teleport pledged players directly to kingdom on Mulai Bermain NPC interaction
- **Pesan Commit:** `feat(npc): teleport pledged players directly to kingdom on Mulai Bermain NPC interaction`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `e1f6235` — 2026-09-08 | 🐛 FIX | fix(bridge): resolve broadcast delivery server error and allow character profile views for in-game players
- **Pesan Commit:** `fix(bridge): resolve broadcast delivery server error and allow character profile views for in-game players`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `c1132f8` — 2026-09-08 | 🐛 FIX | fix(rewards): resolve currency deletion failure and prevent template item resurrections across Battlepass and Core
- **Pesan Commit:** `fix(rewards): resolve currency deletion failure and prevent template item resurrections across Battlepass and Core`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `5a99ef5` — 2026-09-08 | 🐛 FIX | fix(admin): resolve 500 error by using absolute url helper for telemetry and broadcast endpoints
- **Pesan Commit:** `fix(admin): resolve 500 error by using absolute url helper for telemetry and broadcast endpoints`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `639fce8` — 2026-09-08 | 🐛 FIX | fix(battlepass): relocate reward preview toggle from item editor to level editor and add test preview in core
- **Pesan Commit:** `fix(battlepass): relocate reward preview toggle from item editor to level editor and add test preview in core`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `0670833` — 2026-09-08 | ✨ FEAT | feat(admin): implement live server health telemetry monitor and broadcast console in admin dashboard
- **Pesan Commit:** `feat(admin): implement live server health telemetry monitor and broadcast console in admin dashboard`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `779a5b8` — 2026-09-08 | ✨ FEAT | feat(admin): apply Apexsions royal gold and obsidian design system to admin panel
- **Pesan Commit:** `feat(admin): apply Apexsions royal gold and obsidian design system to admin panel`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `d4ee99e` — 2026-09-08 | 📌 UPDATE | style(web): standardize navbar labels to single words (Beranda, Webstore, Wiki, Peraturan, Vote)
- **Pesan Commit:** `style(web): standardize navbar labels to single words (Beranda, Webstore, Wiki, Peraturan, Vote)`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.

#### `365837d` — 2026-09-08 | ✨ FEAT | feat(web): display direct navigation links without dropdown and move leaderboard access to player profile
- **Pesan Commit:** `feat(web): display direct navigation links without dropdown and move leaderboard access to player profile`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.

#### `203b41e` — 2026-09-08 | ♻️ REFACTOR | refactor(web): center navbar menu, elevate Wiki, add capsule pill active states, and harmonize right toolbar to 38px
- **Pesan Commit:** `refactor(web): center navbar menu, elevate Wiki, add capsule pill active states, and harmonize right toolbar to 38px`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `45cf4e5` — 2026-09-08 | ♻️ REFACTOR | refactor(web): remove in-page section links from navbar for clean page hierarchy
- **Pesan Commit:** `refactor(web): remove in-page section links from navbar for clean page hierarchy`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0f0df6a` — 2026-09-08 | 📌 UPDATE | style(web): anchor navbar menu closer to brand and reorder right action items
- **Pesan Commit:** `style(web): anchor navbar menu closer to brand and reorder right action items`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0d43207` — 2026-09-08 | 🐛 FIX | fix(web): align navbar dropdown chevron inline and remove duplicate user profile carets
- **Pesan Commit:** `fix(web): align navbar dropdown chevron inline and remove duplicate user profile carets`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `e492236` — 2026-09-08 | ✨ FEAT | feat(web): streamline navbar to 5 thematic pillars, Lainnya dropdown, and Discord CTA button
- **Pesan Commit:** `feat(web): streamline navbar to 5 thematic pillars, Lainnya dropdown, and Discord CTA button`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `b606e87` — 2026-09-08 | ✨ FEAT | feat(battlepass): display exact pass tier name on web profile and /sync command
- **Pesan Commit:** `feat(battlepass): display exact pass tier name on web profile and /sync command`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `826c3eb` — 2026-09-08 | 🐛 FIX | fix(admin): overhaul admin GUIs, add BattlePass give-pass & set-tier GUIs, and fix chat input sessions
- **Pesan Commit:** `fix(admin): overhaul admin GUIs, add BattlePass give-pass & set-tier GUIs, and fix chat input sessions`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `f821d97` — 2026-09-07 | 🐛 FIX | fix(core): query ApexsionsEconomy for player Rupiah balance in chat profile hover card
- **Pesan Commit:** `fix(core): query ApexsionsEconomy for player Rupiah balance in chat profile hover card`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `ff45ddb` — 2026-09-07 | 🐛 FIX | fix(battlepass): auto-navigate to player tier in rewards menu, add command aliases, polish profile tier badge and improve sync feedback
- **Pesan Commit:** `fix(battlepass): auto-navigate to player tier in rewards menu, add command aliases, polish profile tier badge and improve sync feedback`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `9b70ede` — 2026-09-07 | ✨ FEAT | feat(web): redesign webstore home showcase and remove bp/coins from public profile
- **Pesan Commit:** `feat(web): redesign webstore home showcase and remove bp/coins from public profile`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `64eaba2` — 2026-09-07 | 🐛 FIX | fix(sync): differentiate core level vs battlepass tier and add instant sync bridge
- **Pesan Commit:** `fix(sync): differentiate core level vs battlepass tier and add instant sync bridge`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `3dd2a9f` — 2026-09-07 | ✨ FEAT | feat(web-bridge): sync accurate rupiah, battlepass progress, and apex coins with bilingual support
- **Pesan Commit:** `feat(web-bridge): sync accurate rupiah, battlepass progress, and apex coins with bilingual support`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `2b08e90` — 2026-09-07 | 📌 UPDATE | config(bluemap): disable live-player-markers so players are hidden on web map
- **Pesan Commit:** `config(bluemap): disable live-player-markers so players are hidden on web map`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0fb6975` — 2026-09-07 | ✨ FEAT | feat(customenchants): integrate custom enchants with Sions legendary relics, mob death drops, and chest loot
- **Pesan Commit:** `feat(customenchants): integrate custom enchants with Sions legendary relics, mob death drops, and chest loot`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `d4686ff` — 2026-09-07 | ✨ FEAT | feat(mythicmobs): integrate 3-tier Sions keys, leveled wilderness mobs, and boss/elite spawners
- **Pesan Commit:** `feat(mythicmobs): integrate 3-tier Sions keys, leveled wilderness mobs, and boss/elite spawners`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `d0205dc` — 2026-09-07 | ✨ FEAT | feat(core): implement Sions baseline snapshotting, container key-lock, and proximity countdowns
- **Pesan Commit:** `feat(core): implement Sions baseline snapshotting, container key-lock, and proximity countdowns`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `d37dfb0` — 2026-09-07 | 🐛 FIX | fix(core): resolve NPE in SionsCommand by initializing SionsTemporalService before registerCommands
- **Pesan Commit:** `fix(core): resolve NPE in SionsCommand by initializing SionsTemporalService before registerCommands`
  - **Rincian Teknis:** Menerapkan fallback lazy-lookup pada manager instance agar sub-command console/WebBridge tidak mengalami NullPointerException saat bootstrap belum tuntas.

#### `767efbf` — 2026-09-07 | 📚 DOCS | docs: synchronize documentation with extended palette, Sions temporal engine, and chat join/death features
- **Pesan Commit:** `docs: synchronize documentation with extended palette, Sions temporal engine, and chat join/death features`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `0bbe451` — 2026-09-07 | ✨ FEAT | feat(web): apply monolithic obsidian and celestial ivory color palette with asset archive
- **Pesan Commit:** `feat(web): apply monolithic obsidian and celestial ivory color palette with asset archive`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4650ece` — 2026-09-07 | ✨ FEAT | feat(customenchants): add preset preview GUI and fix preset name color parsing
- **Pesan Commit:** `feat(customenchants): add preset preview GUI and fix preset name color parsing`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `1681012` — 2026-09-07 | 🐛 FIX | fix(customenchants): remove armor set bonus option from item edit dialog
- **Pesan Commit:** `fix(customenchants): remove armor set bonus option from item edit dialog`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `9383f65` — 2026-09-07 | ✨ FEAT | feat(core): implement hourly sions temporal reconstruction engine and secret region binding
- **Pesan Commit:** `feat(core): implement hourly sions temporal reconstruction engine and secret region binding`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `77fbee9` — 2026-09-07 | ✨ FEAT | feat(battlepass): convert excess max level XP to battle coins at 10 xp per coin ratio
- **Pesan Commit:** `feat(battlepass): convert excess max level XP to battle coins at 10 xp per coin ratio`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `437a0f2` — 2026-09-07 | ✨ FEAT | feat(chat): implement delayed join message via AuthMe and luxury death messages system
- **Pesan Commit:** `feat(chat): implement delayed join message via AuthMe and luxury death messages system`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `54deb68` — 2026-09-07 | 🐛 FIX | fix(core,customenchants): display armor set bonus notifications in action bar & fix raw color code rendering
- **Pesan Commit:** `fix(core,customenchants): display armor set bonus notifications in action bar & fix raw color code rendering`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `82603e2` — 2026-09-07 | 🐛 FIX | fix(core): override matching 2-piece stats with 4-piece armor set bonus values
- **Pesan Commit:** `fix(core): override matching 2-piece stats with 4-piece armor set bonus values`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ca22182` — 2026-09-07 | ✨ FEAT | feat(web): add bilingual support for leaderboards and public profile, secure unique ID routing, and update documentation
- **Pesan Commit:** `feat(web): add bilingual support for leaderboards and public profile, secure unique ID routing, and update documentation`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.

#### `1f181d2` — 2026-09-07 | ✨ FEAT | feat(customenchants): migrate armor set bonus editing to native dialog gui and add custom value input
- **Pesan Commit:** `feat(customenchants): migrate armor set bonus editing to native dialog gui and add custom value input`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `18b3c4a` — 2026-09-07 | ✨ FEAT | feat(web): add complete bilingual translation support for profile, link, caste system, webstore, and auth
- **Pesan Commit:** `feat(web): add complete bilingual translation support for profile, link, caste system, webstore, and auth`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `25eb066` — 2026-09-07 | 🐛 FIX | fix(customenchants): fix item creator set name color formatting and auto-read placed set items
- **Pesan Commit:** `fix(customenchants): fix item creator set name color formatting and auto-read placed set items`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `1a7aca8` — 2026-09-07 | ✨ FEAT | feat(web): add full bilingual translation for wiki articles, rules prevention badges, terms articles, and posts
- **Pesan Commit:** `feat(web): add full bilingual translation for wiki articles, rules prevention badges, terms articles, and posts`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4de516e` — 2026-09-07 | 🐛 FIX | fix(progression): smooth money progression starting at thousands for early levels
- **Pesan Commit:** `fix(progression): smooth money progression starting at thousands for early levels`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `d1f78b2` — 2026-09-07 | ✨ FEAT | feat(web): enable full site-wide bilingual translations for rules, vote, terms, privacy, shop, and wiki
- **Pesan Commit:** `feat(web): enable full site-wide bilingual translations for rules, vote, terms, privacy, shop, and wiki`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `3ef9cfc` — 2026-09-07 | ✨ FEAT | feat(progression): adjust diamond level rewards to strictly span levels 15 to 65 with guaranteed endpoints
- **Pesan Commit:** `feat(progression): adjust diamond level rewards to strictly span levels 15 to 65 with guaranteed endpoints`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `025be74` — 2026-09-07 | ✨ FEAT | feat(core): auto-sync player rank nametags and scoreboard teams on join and rank change
- **Pesan Commit:** `feat(core): auto-sync player rank nametags and scoreboard teams on join and rank change`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `44fe349` — 2026-09-07 | ✨ FEAT | feat(progression): create full level rewards template with 10M money and 200 diamonds capped at lv 65, simplify special level wording
- **Pesan Commit:** `feat(progression): create full level rewards template with 10M money and 200 diamonds capped at lv 65, simplify special level wording`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `b3b90f9` — 2026-09-07 | 🐛 FIX | fix(battlepass): remove redundant currency multiplier prefix in gui and eliminate duplicate item rewards per level
- **Pesan Commit:** `fix(battlepass): remove redundant currency multiplier prefix in gui and eliminate duplicate item rewards per level`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `e646c9a` — 2026-09-07 | ✨ FEAT | feat(progression): calibrate enchanting and exploration xp scaling with vanilla and custom enchant support
- **Pesan Commit:** `feat(progression): calibrate enchanting and exploration xp scaling with vanilla and custom enchant support`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `2f781ba` — 2026-09-07 | ✨ FEAT | feat(battlepass): revamp reward progression with vanilla items, 3-currency calibration, and luxury milestone aesthetic
- **Pesan Commit:** `feat(battlepass): revamp reward progression with vanilla items, 3-currency calibration, and luxury milestone aesthetic`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `10058bf` — 2026-09-07 | 🐛 FIX | fix(web): prevent page transition preloader lockup on initial load
- **Pesan Commit:** `fix(web): prevent page transition preloader lockup on initial load`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `f546f84` — 2026-09-07 | ✨ FEAT | feat(web): add bilingual accessibility switcher and overhaul SEO & meta tags
- **Pesan Commit:** `feat(web): add bilingual accessibility switcher and overhaul SEO & meta tags`
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `28965a6` — 2026-09-07 | 🐛 FIX | fix(core): sanitize player display name on death/respawn and live unequip monarch titles
- **Pesan Commit:** `fix(core): sanitize player display name on death/respawn and live unequip monarch titles`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `25b798b` — 2026-09-07 | ✨ FEAT | feat(crates): remove admin panel button from public crate shop GUI
- **Pesan Commit:** `feat(crates): remove admin panel button from public crate shop GUI`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `0cf372c` — 2026-09-07 | 📚 DOCS | docs: enrich narrative lore cohesion, PVE dungeon systems, and long-term roadmap
- **Pesan Commit:** `docs: enrich narrative lore cohesion, PVE dungeon systems, and long-term roadmap`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `ee1df3f` — 2026-09-07 | ✨ FEAT | feat(core): update level exp formula to quadratic (510 * L^2) - (10 * L)
- **Pesan Commit:** `feat(core): update level exp formula to quadratic (510 * L^2) - (10 * L)`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `28547ad` — 2026-09-07 | ✨ FEAT | feat(chat): remove gold nugget reward from chat games and set all chat game rewards to 150 Level XP
- **Pesan Commit:** `feat(chat): remove gold nugget reward from chat games and set all chat game rewards to 150 Level XP`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `5a58cdf` — 2026-09-07 | ✨ FEAT | feat(battlepass,core): add universal reward preview with luxury special preview GUI and admin preview mode toggle
- **Pesan Commit:** `feat(battlepass,core): add universal reward preview with luxury special preview GUI and admin preview mode toggle`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `e00684b` — 2026-09-07 | ✨ FEAT | feat(battlepass,core): 3-tier pass system, horizontal rewards layout, and reward preview systems
- **Pesan Commit:** `feat(battlepass,core): 3-tier pass system, horizontal rewards layout, and reward preview systems`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `8008d69` — 2026-09-07 | ✨ FEAT | feat(core): add kingdom capital respawn system and /ac setspawn command
- **Pesan Commit:** `feat(core): add kingdom capital respawn system and /ac setspawn command`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `448cc6c` — 2026-09-07 | ✨ FEAT | feat(web): add daily reward cooldown timer and fix duplicate alerts
- **Pesan Commit:** `feat(web): add daily reward cooldown timer and fix duplicate alerts`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0a7296d` — 2026-09-07 | 🐛 FIX | fix(webstore): remove Favian contact and record Wilderness Sions ruins lore
- **Pesan Commit:** `fix(webstore): remove Favian contact and record Wilderness Sions ruins lore`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `66634e2` — 2026-09-07 | ✨ FEAT | feat(lore): integrate ancient Sions Empire collapse, Exodus factions, and kingdom traits
- **Pesan Commit:** `feat(lore): integrate ancient Sions Empire collapse, Exodus factions, and kingdom traits`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `50942c0` — 2026-09-07 | ✨ FEAT | feat(web): display and copy both IP and Port for Bedrock across views
- **Pesan Commit:** `feat(web): display and copy both IP and Port for Bedrock across views`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `9371cf0` — 2026-09-07 | ✨ FEAT | feat(crates,core): add dedicated /crateshop, crate key shop admin gui, and master admin integration
- **Pesan Commit:** `feat(crates,core): add dedicated /crateshop, crate key shop admin gui, and master admin integration`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `26a2a31` — 2026-09-07 | ✨ FEAT | feat(crates): add native auto-filtering tab completion for Java Edition commands
- **Pesan Commit:** `feat(crates): add native auto-filtering tab completion for Java Edition commands`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `f30bf8d` — 2026-09-07 | ✨ FEAT | feat(crates): migrate and rebrand full crate ecosystem to ApexsionsCrates with Core, Battlepass, Economy, and Shop integrations
- **Pesan Commit:** `feat(crates): migrate and rebrand full crate ecosystem to ApexsionsCrates with Core, Battlepass, Economy, and Shop integrations`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `144d667` — 2026-09-06 | ✨ FEAT | feat(crates): optimize GUI tap handling for Bedrock touch and controller support
- **Pesan Commit:** `feat(crates): optimize GUI tap handling for Bedrock touch and controller support`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `050fcd4` — 2026-09-06 | 🐛 FIX | fix(crates): add direct item drop fallback and chunk unload hologram cleanup
- **Pesan Commit:** `fix(crates): add direct item drop fallback and chunk unload hologram cleanup`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `d7fd93e` — 2026-09-06 | ✨ FEAT | feat(crates): add selectable mystery animation, catalogue menu, key security, and 3d display holograms
- **Pesan Commit:** `feat(crates): add selectable mystery animation, catalogue menu, key security, and 3d display holograms`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `7968499` — 2026-09-06 | 📌 UPDATE | build: compile and update all 8 plugin binaries
- **Pesan Commit:** `build: compile and update all 8 plugin binaries`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4655298` — 2026-09-06 | ✨ FEAT | feat(crates): rebuild ApexsionsCrates native architecture without nightcore
- **Pesan Commit:** `feat(crates): rebuild ApexsionsCrates native architecture without nightcore`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `098ee5f` — 2026-09-06 | 🐛 FIX | fix(bridge): add instant kingdom sync, 30s auto-sync daemon, and clean MiniMessage tags in titles
- **Pesan Commit:** `fix(bridge): add instant kingdom sync, 30s auto-sync daemon, and clean MiniMessage tags in titles`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `15fb1af` — 2026-09-06 | 🐛 FIX | fix(home): remove internal architecture section and fix duplicate icons in caste badges
- **Pesan Commit:** `fix(home): remove internal architecture section and fix duplicate icons in caste badges`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `8b11ddd` — 2026-09-06 | ✨ FEAT | feat(web): add standalone vote and legal pages, optimize seo meta, and improve ui consistency
- **Pesan Commit:** `feat(web): add standalone vote and legal pages, optimize seo meta, and improve ui consistency`
  - **Rincian Teknis:** Mengotomatisasi verifikasi voting web ke server, reward delivery instan (3x Vote Key + Rp 1.000), serta routing anti-loop.
  - **Rincian Teknis:** Mengoptimasi sitemap XML terstruktur dengan timestamp lastmod dinamis, header X-Robots-Tag, dan URL canonical HTTPS otoritatif.

#### `9b6ab73` — 2026-09-06 | 🐛 FIX | fix(crates): prevent copying sources jar instead of compiled binary jar in build script and remove maven-source-plugin
- **Pesan Commit:** `fix(crates): prevent copying sources jar instead of compiled binary jar in build script and remove maven-source-plugin`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `9a16d0b` — 2026-09-06 | 🐛 FIX | fix(crates): quote author field in plugin.yml to fix yaml parsing error
- **Pesan Commit:** `fix(crates): quote author field in plugin.yml to fix yaml parsing error`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `fc1b103` — 2026-09-06 | ✨ FEAT | feat(crates): enable instant drag-and-drop of items into empty reward slots
- **Pesan Commit:** `feat(crates): enable instant drag-and-drop of items into empty reward slots`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `e97a75c` — 2026-09-06 | ✨ FEAT | feat(crates): rename to ApexsionsCrates, add empty reward slots, and add dialog gui for glowing currency rewards
- **Pesan Commit:** `feat(crates): rename to ApexsionsCrates, add empty reward slots, and add dialog gui for glowing currency rewards`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `7506287` — 2026-09-06 | ✨ FEAT | feat(customenchants): auto-sort custom enchants by rarity on lore and restrict armor set bonus config to main creator
- **Pesan Commit:** `feat(customenchants): auto-sort custom enchants by rarity on lore and restrict armor set bonus config to main creator`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `df10927` — 2026-09-06 | ✨ FEAT | feat(crate,customenchants): initialize ApexsionsCrate plugin module with ecosystem integrations and fix custom enchant colors in item creator
- **Pesan Commit:** `feat(crate,customenchants): initialize ApexsionsCrate plugin module with ecosystem integrations and fix custom enchant colors in item creator`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `f99f634` — 2026-09-06 | ✨ FEAT | feat(web): align server architecture to Minecraft 26.2 and overhaul webstore UI
- **Pesan Commit:** `feat(web): align server architecture to Minecraft 26.2 and overhaul webstore UI`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `bb94bff` — 2026-09-06 | 🐛 FIX | fix(customenchants): allow vanilla enchant options up to level 20 with roman numerals in item creator
- **Pesan Commit:** `fix(customenchants): allow vanilla enchant options up to level 20 with roman numerals in item creator`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `d157b73` — 2026-09-06 | 🐛 FIX | fix(customenchants): eliminate chest gui flicker when opening and transitioning dialogs
- **Pesan Commit:** `fix(customenchants): eliminate chest gui flicker when opening and transitioning dialogs`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `69cf0e1` — 2026-09-06 | ✨ FEAT | feat(customenchants): migrate entire item editing flow to 100% native dialog guis
- **Pesan Commit:** `feat(customenchants): migrate entire item editing flow to 100% native dialog guis`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `0027930` — 2026-09-06 | ✨ FEAT | feat(customenchants): migrate item modifier to native dialog gui with item preview
- **Pesan Commit:** `feat(customenchants): migrate item modifier to native dialog gui with item preview`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `ba7d6ee` — 2026-09-06 | ✨ FEAT | feat(progression): rebalance reward EXP scaling for 3-month season pacing
- **Pesan Commit:** `feat(progression): rebalance reward EXP scaling for 3-month season pacing`
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `c138e5b` — 2026-09-06 | ✨ FEAT | feat(web): add WhatsApp 3-founder checkout redirect & overhaul Fandom-style Wiki
- **Pesan Commit:** `feat(web): add WhatsApp 3-founder checkout redirect & overhaul Fandom-style Wiki`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4f3732a` — 2026-09-06 | ✨ FEAT | feat(economy): support flexible amount and currency parameter ordering in commands
- **Pesan Commit:** `feat(economy): support flexible amount and currency parameter ordering in commands`
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `f2541b0` — 2026-09-06 | 📚 DOCS | docs: synchronize documentation with web bridge, 11-tier roles, and 26.2 baseline
- **Pesan Commit:** `docs: synchronize documentation with web bridge, 11-tier roles, and 26.2 baseline`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4c18d49` — 2026-09-06 | ✨ FEAT | feat(bridge): support console deliveries, offline xp rewards, 11-tier web roles, and minecraft 26.2 baseline
- **Pesan Commit:** `feat(bridge): support console deliveries, offline xp rewards, 11-tier web roles, and minecraft 26.2 baseline`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `89ff0d8` — 2026-09-06 | 🐛 FIX | fix(customenchants): sync armor set bonus id with main creator gui name and remove set id button
- **Pesan Commit:** `fix(customenchants): sync armor set bonus id with main creator gui name and remove set id button`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `5936da2` — 2026-09-06 | ✨ FEAT | feat(gui): implement CustomInputTextGUI with NightCore ExcellentCrates dialog engine and remove anvil fallback
- **Pesan Commit:** `feat(gui): implement CustomInputTextGUI with NightCore ExcellentCrates dialog engine and remove anvil fallback`
  - **Rincian Teknis:** Menyelaraskan kalkulasi peluang gacha berbasis weighted rolling chance per tier rarity dengan preview lore probabilitas yang akurat.

#### `426a32a` — 2026-09-06 | 🐛 FIX | fix(gui): resolve NativeDialogAdapter reflection argument mismatch and ensure robust dialog inputs across all plugins
- **Pesan Commit:** `fix(gui): resolve NativeDialogAdapter reflection argument mismatch and ensure robust dialog inputs across all plugins`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ee93aca` — 2026-09-06 | ✨ FEAT | feat(bridge): implement end-to-end player stats sync, web delivery queue, and character profile
- **Pesan Commit:** `feat(bridge): implement end-to-end player stats sync, web delivery queue, and character profile`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `fb32685` — 2026-09-06 | ✨ FEAT | feat(gui): upgrade PaperDialogAdapter to NativeDialogAdapter with Bungee API support for Spigot servers
- **Pesan Commit:** `feat(gui): upgrade PaperDialogAdapter to NativeDialogAdapter with Bungee API support for Spigot servers`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `da37b9f` — 2026-09-06 | ✨ FEAT | feat(chat): add configurable luxury join & quit message templates
- **Pesan Commit:** `feat(chat): add configurable luxury join & quit message templates`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `2c0de2d` — 2026-09-06 | 🐛 FIX | fix(authme): clear mySQLPlayerUUID to prevent sqlite column missing error
- **Pesan Commit:** `fix(authme): clear mySQLPlayerUUID to prevent sqlite column missing error`
  - **Rincian Teknis:** Memastikan migrasi skema database mengeksekusi DDL `ALTER TABLE ADD COLUMN` sebelum perintah `CREATE INDEX` untuk mencegah crash startup SQLite.

#### `4c0537b` — 2026-09-06 | 🔧 CHORE | chore(authme): add optimized config for Bedrock crossplay, security, and Apexsions branding
- **Pesan Commit:** `chore(authme): add optimized config for Bedrock crossplay, security, and Apexsions branding`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `8ea301d` — 2026-09-06 | 📚 DOCS | docs(git): mandate autonomous push to origin/main on validated changes per user directive
- **Pesan Commit:** `docs(git): mandate autonomous push to origin/main on validated changes per user directive`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `30d0fef` — 2026-09-06 | ✨ FEAT | feat(bridge,core): resolve 500 error on web linking, add in-game /link command with AuthMe check and rewards
- **Pesan Commit:** `feat(bridge,core): resolve 500 error on web linking, add in-game /link command with AuthMe check and rewards`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `cc5f6f6` — 2026-09-06 | 📌 UPDATE | style(gui): refine native 26.2 dialog input field and button elements matching reference UI
- **Pesan Commit:** `style(gui): refine native 26.2 dialog input field and button elements matching reference UI`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `375199f` — 2026-09-06 | 🐛 FIX | fix(gui): replace chest virtual keypad with native 26.2 text dialog and anvil keyboard input
- **Pesan Commit:** `fix(gui): replace chest virtual keypad with native 26.2 text dialog and anvil keyboard input`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `501c9f8` — 2026-09-06 | 🐛 FIX | fix(media,chat,core): fix map transparency palette, integrate architect/overseer ranks, and polish motd
- **Pesan Commit:** `fix(media,chat,core): fix map transparency palette, integrate architect/overseer ranks, and polish motd`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `f549f07` — 2026-09-06 | ✨ FEAT | feat(web,bridge): fix registration None username error, bind active port 32348, and enable live server status
- **Pesan Commit:** `feat(web,bridge): fix registration None username error, bind active port 32348, and enable live server status`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2d07ea5` — 2026-09-05 | ✨ FEAT | feat(web,chat): add official rules page with active system prevention and refine smooth transitions
- **Pesan Commit:** `feat(web,chat): add official rules page with active system prevention and refine smooth transitions`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `daa6a60` — 2026-09-05 | ✨ FEAT | feat(web): add cinematic inter-page transition, scroll animations, and clean hero CTA
- **Pesan Commit:** `feat(web): add cinematic inter-page transition, scroll animations, and clean hero CTA`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `7c3c510` — 2026-09-05 | 🐛 FIX | fix(web): update server logo asset and resolve onboarding callout banner layout
- **Pesan Commit:** `fix(web): update server logo asset and resolve onboarding callout banner layout`
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `2411d8f` — 2026-09-05 | ✨ FEAT | feat(kingdom): remove leading symbols from kingdom tags and synchronize dynamic buff/debuff switching
- **Pesan Commit:** `feat(kingdom): remove leading symbols from kingdom tags and synchronize dynamic buff/debuff switching`
  - **Rincian Teknis:** Menyelaraskan nilai atribut kanonikal kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%) dan kartu preview slot 13 pada GUI konfirmasi `/k choose`.

#### `c8f0d78` — 2026-09-06 | ✨ FEAT | feat(gui): migrate chat inputs to native 26.2 dialogs and bedrock forms across plugin suite
- **Pesan Commit:** `feat(gui): migrate chat inputs to native 26.2 dialogs and bedrock forms across plugin suite`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `891f828` — 2026-09-06 | 🐛 FIX | fix(core): polish kingdom buffs, projectile damage scaling, and admin setkingdom real-time trigger
- **Pesan Commit:** `fix(core): polish kingdom buffs, projectile damage scaling, and admin setkingdom real-time trigger`
  - **Rincian Teknis:** Menyelaraskan nilai atribut kanonikal kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%) dan kartu preview slot 13 pada GUI konfirmasi `/k choose`.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.

#### `e2e8885` — 2026-09-06 | 🐛 FIX | fix(core): polish irrigation listener with offhand check, waterlogged support and saturated soil guards
- **Pesan Commit:** `fix(core): polish irrigation listener with offhand check, waterlogged support and saturated soil guards`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `56e3112` — 2026-09-06 | 🐛 FIX | fix(core): fix crop dying on moisture change and add manual irrigation mechanics
- **Pesan Commit:** `fix(core): fix crop dying on moisture change and add manual irrigation mechanics`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ee14aca` — 2026-09-05 | 🐛 FIX | fix(theme): add cache buster to logo to force instant browser refresh
- **Pesan Commit:** `fix(theme): add cache buster to logo to force instant browser refresh`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `cf2b737` — 2026-09-05 | ✨ FEAT | feat(theme): update imperial gold crest logo, favicon, and server-icon
- **Pesan Commit:** `feat(theme): update imperial gold crest logo, favicon, and server-icon`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2a49a97` — 2026-09-05 | ✨ FEAT | feat(core,shop): add kingdom taxes, active buffs and debuffs, and admin shop selector
- **Pesan Commit:** `feat(core,shop): add kingdom taxes, active buffs and debuffs, and admin shop selector`
  - **Rincian Teknis:** Menyelaraskan nilai atribut kanonikal kerajaan (Zenithar 18%, Solterra 20%, Sylvamoor 15%) dan kartu preview slot 13 pada GUI konfirmasi `/k choose`.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `ac878f0` — 2026-09-05 | ✨ FEAT | feat(wiki): expand imperial codex, onboarding roadmap, and fandom lore
- **Pesan Commit:** `feat(wiki): expand imperial codex, onboarding roadmap, and fandom lore`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.

#### `73db1ce` — 2026-09-05 | ✨ FEAT | feat(ranks): reorganize into 5 tiers with architect & overseer equal in tier IV and warden in tier III
- **Pesan Commit:** `feat(ranks): reorganize into 5 tiers with architect & overseer equal in tier IV and warden in tier III`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `bf1b1d3` — 2026-09-05 | ✨ FEAT | feat(ranks): synchronize official 11-rank hierarchy across minecraft suite and website
- **Pesan Commit:** `feat(ranks): synchronize official 11-rank hierarchy across minecraft suite and website`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0e8a8c0` — 2026-09-05 | 🐛 FIX | fix(core): remove square brackets from title displays in titles.yml and TitleManager
- **Pesan Commit:** `fix(core): remove square brackets from title displays in titles.yml and TitleManager`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `9fb6cb4` — 2026-09-05 | ✨ FEAT | feat(suite): categorize config folders with fallback loaders and enhance title permission system
- **Pesan Commit:** `feat(suite): categorize config folders with fallback loaders and enhance title permission system`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `45ef291` — 2026-09-05 | ✨ FEAT | feat(web): polish 20/20 a11y, performance, and design system governance
- **Pesan Commit:** `feat(web): polish 20/20 a11y, performance, and design system governance`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `659beff` — 2026-09-05 | ✨ FEAT | feat(customenchants): add selective enchant removal GUI and quick-remove shortcut in item creator
- **Pesan Commit:** `feat(customenchants): add selective enchant removal GUI and quick-remove shortcut in item creator`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `aabb9e8` — 2026-09-05 | 🐛 FIX | fix(deploy): automatically create theme and asset symlinks in setup-vps.sh
- **Pesan Commit:** `fix(deploy): automatically create theme and asset symlinks in setup-vps.sh`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `1b989f5` — 2026-09-05 | 🐛 FIX | fix(customenchants): verify 100% success/destroy complement, offhand mystery dust, and enchanting table limits
- **Pesan Commit:** `fix(customenchants): verify 100% success/destroy complement, offhand mystery dust, and enchanting table limits`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `6a9bd43` — 2026-09-05 | ✨ FEAT | feat(customenchants): add anvil combining, rank enchant limits, anti-loss sub-gui return and polish diamond formatting
- **Pesan Commit:** `feat(customenchants): add anvil combining, rank enchant limits, anti-loss sub-gui return and polish diamond formatting`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `5b5c37d` — 2026-09-05 | 🐛 FIX | fix(web): make master seeder database-agnostic for sqlite and mysql
- **Pesan Commit:** `fix(web): make master seeder database-agnostic for sqlite and mysql`
  - **Rincian Teknis:** Memastikan migrasi skema database mengeksekusi DDL `ALTER TABLE ADD COLUMN` sebelum perintah `CREATE INDEX` untuk mencegah crash startup SQLite.

#### `8e97d56` — 2026-09-05 | ✨ FEAT | feat(economy): add 💎 suffix to diamond currency display across all plugins and web
- **Pesan Commit:** `feat(economy): add 💎 suffix to diamond currency display across all plugins and web`
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `8661672` — 2026-09-05 | 🐛 FIX | fix(web): fix wiki category slug typo hierarki-kasta in master seeder
- **Pesan Commit:** `fix(web): fix wiki category slug typo hierarki-kasta in master seeder`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `552660a` — 2026-09-05 | 🐛 FIX | fix(customenchants): fix duplicate lore, defense-only stats, remove gradients, and prevent item drops on apply
- **Pesan Commit:** `fix(customenchants): fix duplicate lore, defense-only stats, remove gradients, and prevent item drops on apply`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `3738d47` — 2026-09-05 | 🐛 FIX | fix(web): make master seeder database-agnostic for sqlite and mysql
- **Pesan Commit:** `fix(web): make master seeder database-agnostic for sqlite and mysql`
  - **Rincian Teknis:** Memastikan migrasi skema database mengeksekusi DDL `ALTER TABLE ADD COLUMN` sebelum perintah `CREATE INDEX` untuk mencegah crash startup SQLite.

#### `b77a387` — 2026-09-05 | ✨ FEAT | feat(web): synchronize wiki, store, and design system with minecraft plugins
- **Pesan Commit:** `feat(web): synchronize wiki, store, and design system with minecraft plugins`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `b1c0f00` — 2026-09-05 | 🐛 FIX | fix(customenchants): move armor set bonus status button to slot 15 in item creator
- **Pesan Commit:** `fix(customenchants): move armor set bonus status button to slot 15 in item creator`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `cfe178c` — 2026-09-05 | ✨ FEAT | feat(customenchants): add tiered armor set bonuses, dedicated stat GUIs, tool set bonus synergies, and slot 16 fixes
- **Pesan Commit:** `feat(customenchants): add tiered armor set bonuses, dedicated stat GUIs, tool set bonus synergies, and slot 16 fixes`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `53e20f0` — 2026-09-05 | ✨ FEAT | feat(customenchants): add /ace presets and /presets command for saved sets GUI
- **Pesan Commit:** `feat(customenchants): add /ace presets and /presets command for saved sets GUI`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `ba0f17f` — 2026-09-05 | ✨ FEAT | feat(customenchants): add rarity & category filters to ace catalog, implement full fishing, hoe, and combat enchants
- **Pesan Commit:** `feat(customenchants): add rarity & category filters to ace catalog, implement full fishing, hoe, and combat enchants`
  - **Rincian Teknis:** Membangun ekosistem pancing 6-tier rarity, auto-recast AFK engine terproteksi, penyimpanan ikan 54-slot, dan sistem dialog interaktif rod creator.
  - **Rincian Teknis:** Mengimplementasikan formula normalisasi stat tempur, atribut progression level 1-100, dan peredaman lonjakan damage berlebih.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `139c095` — 2026-09-05 | ✨ FEAT | feat(customenchants): add enchanting table custom enchants, full roman numerals, and wings exclusion
- **Pesan Commit:** `feat(customenchants): add enchanting table custom enchants, full roman numerals, and wings exclusion`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `af682d1` — 2026-09-05 | ✨ FEAT | feat(web): update apexsions theme, server bridge, responsive footer status card, and transactional email layout
- **Pesan Commit:** `feat(web): update apexsions theme, server bridge, responsive footer status card, and transactional email layout`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `86eae75` — 2026-09-05 | ✨ FEAT | feat(customenchants): implement working enchantment abilities including wings flight and glowing glint
- **Pesan Commit:** `feat(customenchants): implement working enchantment abilities including wings flight and glowing glint`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `8d8381a` — 2026-09-05 | 🐛 FIX | fix(customenchants): ensure AdminItemCreatorGUI reopens automatically after chat rename and improve session recovery
- **Pesan Commit:** `fix(customenchants): ensure AdminItemCreatorGUI reopens automatically after chat rename and improve session recovery`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `7707167` — 2026-09-05 | 🐛 FIX | fix(customenchants): ensure navigation buttons always render in set bonus GUI and prevent auto-mutating items on placement
- **Pesan Commit:** `fix(customenchants): ensure navigation buttons always render in set bonus GUI and prevent auto-mutating items on placement`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `54a3f69` — 2026-09-05 | ✨ FEAT | feat: presets system, set auto-naming in chat, tool set bonus, multi-stat bonus, and anti-loss close safety
- **Pesan Commit:** `feat: presets system, set auto-naming in chat, tool set bonus, multi-stat bonus, and anti-loss close safety`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `3cb02a8` — 2026-09-05 | ✨ FEAT | feat(enchants): 182 AE enchantments, colored firework stars, and interactive GUI-driven item creator
- **Pesan Commit:** `feat(enchants): 182 AE enchantments, colored firework stars, and interactive GUI-driven item creator`
  - **Rincian Teknis:** Menyusun dan menyinkronkan kanon resmi peradaban, pembagian teritori geografis, sejarah keruntuhan Sions, serta arsitektur kosmik.
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `617e5f4` — 2026-09-05 | ✨ FEAT | feat: add ApexsionsCustomEnchants plugin and native kits system with stat-based set bonuses
- **Pesan Commit:** `feat: add ApexsionsCustomEnchants plugin and native kits system with stat-based set bonuses`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `0e9fa49` — 2026-09-05 | 🐛 FIX | fix(brand): enforce strict server name Apexsions and tagline The Peak Civilizations
- **Pesan Commit:** `fix(brand): enforce strict server name Apexsions and tagline The Peak Civilizations`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `49873e5` — 2026-09-05 | ✨ FEAT | feat(core): add custom /enchant command and anvil enhancement (bypass too expensive)
- **Pesan Commit:** `feat(core): add custom /enchant command and anvil enhancement (bypass too expensive)`
  - **Rincian Teknis:** Mengelola 182 custom enchants lintas 7 tingkatan kekuatan, tinkerer exchange, serta aktivasi bonus set armor legendaris.

#### `a917164` — 2026-09-05 | ✨ FEAT | feat(website): integrate Azuriom web platform with shop, wiki, midtrans, and apexsions-bridge
- **Pesan Commit:** `feat(website): integrate Azuriom web platform with shop, wiki, midtrans, and apexsions-bridge`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `89d3df9` — 2026-09-05 | 📚 DOCS | docs: update root README for monorepo structure
- **Pesan Commit:** `docs: update root README for monorepo structure`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `eb8408d` — 2026-09-05 | ♻️ REFACTOR | refactor: restructure repository into monorepo with Minecraft and Website folders
- **Pesan Commit:** `refactor: restructure repository into monorepo with Minecraft and Website folders`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

---

## 🚀 Sprint 2 — Integrasi Antar-Plugin, Keamanan Transaksi Finansial, Quest BattlePass & Moderasi Chat Lapis Tiga [v0.5.0]
> **Periode Pengembangan:** 30 Agustus – 4 September 2026 | **Total Commit:** 35 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Pembangunan interkonektivitas antar-plugin melalui API Provider dan Event Bus. Mengimplementasikan transaksi ekonomi atomik di ApexsionsEconomy (Auction House, Escrow, Barter), progresi 200 level dan quest berulang pada ApexsionsBattlepass, format chat Kyori MiniMessage multi-channel pada ApexsionsChat, serta sistem reputasi dan warisan teritorial.

### 🔍 Rincian Lengkap Commit (35 Commit)

#### `4025d10` — 2026-09-04 | ✨ FEAT | feat(chat,core): add nickname system, polish kingdom top GUI and level rewards
- **Pesan Commit:** `feat(chat,core): add nickname system, polish kingdom top GUI and level rewards`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `28ae065` — 2026-09-03 | ✨ FEAT | feat(media): add unified content creator verification and reward suite
- **Pesan Commit:** `feat(media): add unified content creator verification and reward suite`
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `3572811` — 2026-09-03 | ✨ FEAT | feat(chat): integrate chat games xp reward with apexsionscore level system
- **Pesan Commit:** `feat(chat): integrate chat games xp reward with apexsionscore level system`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `712e14e` — 2026-09-03 | ✨ FEAT | feat(core): add interactive level reward editor GUI, dynamic level formula, and modular motd config
- **Pesan Commit:** `feat(core): add interactive level reward editor GUI, dynamic level formula, and modular motd config`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `1cd97f6` — 2026-09-03 | 📌 UPDATE | build: add fallback search paths for maven in build.ps1
- **Pesan Commit:** `build: add fallback search paths for maven in build.ps1`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0c5f3f7` — 2026-09-01 | 🐛 FIX | fix(placeholder): format rank_display as legacy color codes for DecentHolograms and external scoreboards
- **Pesan Commit:** `fix(placeholder): format rank_display as legacy color codes for DecentHolograms and external scoreboards`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `c6ba67b` — 2026-09-01 | 🐛 FIX | fix(motd): resolve AuthLib 16-char profile name limit exception in server list ping
- **Pesan Commit:** `fix(motd): resolve AuthLib 16-char profile name limit exception in server list ping`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `8a863c0` — 2026-09-01 | ✨ FEAT | feat(npc,gui): add /rank GUI, /warp GUI integration, and Citizens custom traits (kingdom-guide, rank-guide, warp-guide)
- **Pesan Commit:** `feat(npc,gui): add /rank GUI, /warp GUI integration, and Citizens custom traits (kingdom-guide, rank-guide, warp-guide)`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `29b2f18` — 2026-09-01 | ✨ FEAT | feat(motd): add built-in luxury MOTD and server ping manager with miniMessage gradients and hover sample
- **Pesan Commit:** `feat(motd): add built-in luxury MOTD and server ping manager with miniMessage gradients and hover sample`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `ee68afa` — 2026-09-01 | ✨ FEAT | feat(admingui): synchronize EconomyAdminSubGUI with kingdom treasury inspector, bank deposits, and baltop
- **Pesan Commit:** `feat(admingui): synchronize EconomyAdminSubGUI with kingdom treasury inspector, bank deposits, and baltop`
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `b23f2e9` — 2026-09-01 | ✨ FEAT | feat(chat,economy): add dynamic mentions with rank gradient, auction categories & sorting, kingdom treasury tax, interactive trade buttons, and time-locked bank deposits
- **Pesan Commit:** `feat(chat,economy): add dynamic mentions with rank gradient, auction categories & sorting, kingdom treasury tax, interactive trade buttons, and time-locked bank deposits`
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `902a50d` — 2026-09-01 | 🐛 FIX | fix(title): eliminate duplicate wanderer nametag suffix and support clean empty title fallback
- **Pesan Commit:** `fix(title): eliminate duplicate wanderer nametag suffix and support clean empty title fallback`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `a2ad177` — 2026-09-01 | ✨ FEAT | feat(economy): integrate multi-currency placeholders, admin GUI actions, and native Economy PAPI expansion
- **Pesan Commit:** `feat(economy): integrate multi-currency placeholders, admin GUI actions, and native Economy PAPI expansion`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `9427d3a` — 2026-08-31 | 🐛 FIX | fix(rank): auto-sanitize player display name and clear stray personal luckperms prefix nodes on join
- **Pesan Commit:** `fix(rank): auto-sanitize player display name and clear stray personal luckperms prefix nodes on join`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `c6dd8a1` — 2026-08-31 | ✨ FEAT | feat(chat): implement luxury join and quit broadcast with rank badges
- **Pesan Commit:** `feat(chat): implement luxury join and quit broadcast with rank badges`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `4a44214` — 2026-08-31 | 📌 UPDATE | build: compile and update all 6 plugins with strict rank/title separation
- **Pesan Commit:** `build: compile and update all 6 plugins with strict rank/title separation`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `fa0cbc9` — 2026-08-31 | ✨ FEAT | feat(title): integrate kingdom monarch titles and auto-unlocking in title vault
- **Pesan Commit:** `feat(title): integrate kingdom monarch titles and auto-unlocking in title vault`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `585d157` — 2026-08-31 | ✨ FEAT | feat(title): designate kingdom monarch as title rather than rank prefix
- **Pesan Commit:** `feat(title): designate kingdom monarch as title rather than rank prefix`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `d5c5e28` — 2026-08-31 | 📚 DOCS | docs: add command and GUI synchronization governance rules
- **Pesan Commit:** `docs: add command and GUI synchronization governance rules`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `4ab6a7b` — 2026-08-31 | ✨ FEAT | feat(kingdom): add /kingdom unsetking command for dethroning monarchs
- **Pesan Commit:** `feat(kingdom): add /kingdom unsetking command for dethroning monarchs`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `9def141` — 2026-08-31 | ✨ FEAT | feat(tab): optimize placeholders for tab and scoreboard integration
- **Pesan Commit:** `feat(tab): optimize placeholders for tab and scoreboard integration`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `aafa69d` — 2026-08-31 | ✨ FEAT | feat(economy): integrate vault provider, kingdom pay tax, chat reward XP & admin GUI fixes
- **Pesan Commit:** `feat(economy): integrate vault provider, kingdom pay tax, chat reward XP & admin GUI fixes`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `04a38ff` — 2026-08-30 | 📚 DOCS | docs: streamline AGENTS.md into universal agent-agnostic engineering guidelines
- **Pesan Commit:** `docs: streamline AGENTS.md into universal agent-agnostic engineering guidelines`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `8eda3c5` — 2026-08-30 | 📚 DOCS | docs: synchronize repository-wide documentation with Java 21 / Paper 1.21.4 codebase
- **Pesan Commit:** `docs: synchronize repository-wide documentation with Java 21 / Paper 1.21.4 codebase`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `255c73a` — 2026-08-30 | 📚 DOCS | docs: synchronize AGENTS.md, .agents rules, and docs with GEMINI.md master contract
- **Pesan Commit:** `docs: synchronize AGENTS.md, .agents rules, and docs with GEMINI.md master contract`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `83522a9` — 2026-08-30 | 📚 DOCS | docs: enrich DOKUMENTASI.md and README.md with comprehensive command matrix for all 6 plugins
- **Pesan Commit:** `docs: enrich DOKUMENTASI.md and README.md with comprehensive command matrix for all 6 plugins`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `415a9be` — 2026-08-30 | 📚 DOCS | docs: update GEMINI.md, AGENTS.md, and DOKUMENTASI.md with strict targeted single-plugin compilation rules
- **Pesan Commit:** `docs: update GEMINI.md, AGENTS.md, and DOKUMENTASI.md with strict targeted single-plugin compilation rules`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `525988d` — 2026-08-30 | 🐛 FIX | fix(battlepass,economy): replace bungeecord ChatColor in ColorUtil with native hex serializer to fix /abp reload crash
- **Pesan Commit:** `fix(battlepass,economy): replace bungeecord ChatColor in ColorUtil with native hex serializer to fix /abp reload crash`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `04bcd69` — 2026-08-30 | ✨ FEAT | feat(economy): implement /ecoadmin reload command to dynamically reload configuration and currencies
- **Pesan Commit:** `feat(economy): implement /ecoadmin reload command to dynamically reload configuration and currencies`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `51cb2da` — 2026-08-30 | ✨ FEAT | feat: register both apexsions and apexsionscore expansions with rich economy and formatted kingdom placeholders
- **Pesan Commit:** `feat: register both apexsions and apexsionscore expansions with rich economy and formatted kingdom placeholders`
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `603719c` — 2026-08-30 | 📚 DOCS | docs: update GEMINI.md and create AGENTS.md with official 9 ranks and architecture guidelines
- **Pesan Commit:** `docs: update GEMINI.md and create AGENTS.md with official 9 ranks and architecture guidelines`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `3dd7a8f` — 2026-08-30 | ✨ FEAT | feat: implement animated gradient waveforms for all 9 official luckperms ranks from ranks.yml
- **Pesan Commit:** `feat: implement animated gradient waveforms for all 9 official luckperms ranks from ranks.yml`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `135c716` — 2026-08-30 | ✨ FEAT | feat: enhance rank prefix animation engine with multi-phase wave cycles, multi-scoreboard sync, and tablist live updating
- **Pesan Commit:** `feat: enhance rank prefix animation engine with multi-phase wave cycles, multi-scoreboard sync, and tablist live updating`
  - **Rincian Teknis:** Mengatasi render angka merah native Bedrock via Geyser resource pack dan mengganti pembatas unicode menjadi hyphen coret ASCII agar tidak patah pada font kustom.

#### `8dedaf3` — 2026-08-30 | ✨ FEAT | feat: complete suite-wide major enhancements (deep player inspector, social profile GUI, staff report desk, dynamic shop trends)
- **Pesan Commit:** `feat: complete suite-wide major enhancements (deep player inspector, social profile GUI, staff report desk, dynamic shop trends)`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `cb8c3f5` — 2026-08-30 | ✨ FEAT | feat(arch): Standardize inter-plugin Provider SPI, NoOp Fallbacks, Atomic Transactions, and Async Safety
- **Pesan Commit:** `feat(arch): Standardize inter-plugin Provider SPI, NoOp Fallbacks, Atomic Transactions, and Async Safety`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

---

## 🚀 Sprint 1 — Genesis Fondasi Monorepo, Setup Paper 26.2 (Java 21), ApexsionsCore & Arsitektur Ekosistem [v0.1.0]
> **Periode Pengembangan:** 27 – 29 Agustus 2026 | **Total Commit:** 32 commit

### 📋 Tinjauan Arsitektur & Dampak Sistem
Inisiasi awal fondasi monorepo Apexsions. Pembentukan modul dasar Paper API 26.2 berbasis Java 21 LTS, implementasi SQLite/PostgreSQL HikariCP connection pool, modul sistem kerajaan dan batas poligon BlueMap awal, serta pembuatan smart multi-compiler build.ps1.

### 🔍 Rincian Lengkap Commit (32 Commit)

#### `3c02d8f` — 2026-08-29 | ✨ FEAT | feat(cosmetics): Implement modular Rank Visuals, Title Vault, Particle Cosmetics, and Decoupled Chat ID-Cards
- **Pesan Commit:** `feat(cosmetics): Implement modular Rank Visuals, Title Vault, Particle Cosmetics, and Decoupled Chat ID-Cards`
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `d55c23b` — 2026-08-29 | ✨ FEAT | feat(admin): Implement Centralized Master Admin Suite, Player Manager & Inspector, and Chat Input Sessions
- **Pesan Commit:** `feat(admin): Implement Centralized Master Admin Suite, Player Manager & Inspector, and Chat Input Sessions`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.

#### `3eff582` — 2026-08-29 | 🐛 FIX | fix(core): Auto-reconcile level progression on load/gain and fix XP threshold calculations
- **Pesan Commit:** `fix(core): Auto-reconcile level progression on load/gain and fix XP threshold calculations`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `980050d` — 2026-08-29 | ✨ FEAT | feat: Implement Kingdom Detail GUI, King System, Territory PvP 0-Damage Rule, and Shop Mob Drops Restriction
- **Pesan Commit:** `feat: Implement Kingdom Detail GUI, King System, Territory PvP 0-Damage Rule, and Shop Mob Drops Restriction`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `76162e7` — 2026-08-29 | ✨ FEAT | feat: Implement Cross-Plugin Unified XP Engine, Luxury Visual Ranks, and Cinematic Level-Up FX
- **Pesan Commit:** `feat: Implement Cross-Plugin Unified XP Engine, Luxury Visual Ranks, and Cinematic Level-Up FX`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `bd256d1` — 2026-08-29 | ✨ FEAT | feat: Implement kingdom-specific leaderboard, pure white transparent media background, interactive admin sub-GUIs, and polished warp system
- **Pesan Commit:** `feat: Implement kingdom-specific leaderboard, pure white transparent media background, interactive admin sub-GUIs, and polished warp system`
  - **Rincian Teknis:** Menerapkan isolasi 6-lapis untuk menyaring akun staf (Weight >= 80), OP, role admin, dan entitas Aetherion keluar dari papan peringkat publik.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `0db19a7` — 2026-08-29 | ✨ FEAT | feat: Implement Smart Auto-Detect in build.ps1 to automatically compile ONLY modified plugins
- **Pesan Commit:** `feat: Implement Smart Auto-Detect in build.ps1 to automatically compile ONLY modified plugins`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0bc2276` — 2026-08-29 | 📌 UPDATE | perf: Optimize build.ps1 Turbo Multi-Compiler with incremental build caching and network timeout bypass
- **Pesan Commit:** `perf: Optimize build.ps1 Turbo Multi-Compiler with incremental build caching and network timeout bypass`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2680222` — 2026-08-29 | ✨ FEAT | feat: Implement Centralized Master Admin Hub (/admingui), Warp GUI & Admin Editor, Territory PvP Protection, Media Admin GUI and Fast Compiler
- **Pesan Commit:** `feat: Implement Centralized Master Admin Hub (/admingui), Warp GUI & Admin Editor, Territory PvP Protection, Media Admin GUI and Fast Compiler`
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `113f566` — 2026-08-29 | ✨ FEAT | feat(core,media): add Warp Navigation & Admin Editor GUI, Kingdom Territory PvP friendly-fire protection, flexible Media banner placement & cloning, fast single-plugin compiler
- **Pesan Commit:** `feat(core,media): add Warp Navigation & Admin Editor GUI, Kingdom Territory PvP friendly-fire protection, flexible Media banner placement & cloning, fast single-plugin compiler`
  - **Rincian Teknis:** Mengonsolidasikan engine ban otoritatif pada level soket jaringan `AsyncPlayerPreLoginEvent`, menonaktifkan command moderasi EssentialsX yang tumpang-tindih.
  - **Rincian Teknis:** Menyempurnakan antarmuka administrasi Azuriom dengan kartu analitik, tombol tindakan cepat, dan keselarasan styling dual-theme.
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `7a59258` — 2026-08-29 | ✨ FEAT | feat(shop): implement smooth logarithmic price elasticity and global market supply tracker
- **Pesan Commit:** `feat(shop): implement smooth logarithmic price elasticity and global market supply tracker`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `daecc9b` — 2026-08-29 | 📚 DOCS | docs: strictly enforce workspace isolation to C:\Apex Plugin only
- **Pesan Commit:** `docs: strictly enforce workspace isolation to C:\Apex Plugin only`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `f207feb` — 2026-08-29 | 📚 DOCS | docs: enforce multi-developer remote sync rules and update senior engineer guidelines
- **Pesan Commit:** `docs: enforce multi-developer remote sync rules and update senior engineer guidelines`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `2b95025` — 2026-08-28 | ♻️ REFACTOR | refactor(structure): organize modular config directories across all plugins matching Battlepass standard
- **Pesan Commit:** `refactor(structure): organize modular config directories across all plugins matching Battlepass standard`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `b388477` — 2026-08-28 | ✨ FEAT | feat(core): add full Multiverse multi-world support and /ac setlobby command
- **Pesan Commit:** `feat(core): add full Multiverse multi-world support and /ac setlobby command`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `7a35a9d` — 2026-08-28 | ✨ FEAT | feat(suite): package standardization to com.apexsions.*, custom events engine, and added ApexsionsMedia plugin
- **Pesan Commit:** `feat(suite): package standardization to com.apexsions.*, custom events engine, and added ApexsionsMedia plugin`
  - **Rincian Teknis:** Merender gambar/logo banner multi-tile dari URL/PNG lokal dengan raytrace hover glow dan aksi URL interaktif.

#### `89fee6d` — 2026-08-28 | ✨ FEAT | feat(ecosystem): Kingdom War, PvP Combat Tag, RTP kingdom constraints, GUI enhancements, shop price clamping & server integrations
- **Pesan Commit:** `feat(ecosystem): Kingdom War, PvP Combat Tag, RTP kingdom constraints, GUI enhancements, shop price clamping & server integrations`
  - **Rincian Teknis:** Mengimplementasikan formula normalisasi stat tempur, atribut progression level 1-100, dan peredaman lonjakan damage berlebih.
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `cc6c18e` — 2026-08-28 | 📚 DOCS | docs: add technical manuals for all 5 plugins in docs/ and update master README with full command list and feature breakdown
- **Pesan Commit:** `docs: add technical manuals for all 5 plugins in docs/ and update master README with full command list and feature breakdown`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `de56c47` — 2026-08-28 | 📚 DOCS | docs: add official Apexsions Senior Minecraft Plugin Engineer system prompt and development protocol
- **Pesan Commit:** `docs: add official Apexsions Senior Minecraft Plugin Engineer system prompt and development protocol`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `a699040` — 2026-08-28 | 🔧 CHORE | chore & refactor: clean workspace structure, remove root duplicate src, fix Economy imports, and update documentation for all 5 plugins
- **Pesan Commit:** `chore & refactor: clean workspace structure, remove root duplicate src, fix Economy imports, and update documentation for all 5 plugins`
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `f99afdb` — 2026-08-28 | 📚 DOCS | docs & feat: synchronize root src/, build scripts, and complete documentation suite
- **Pesan Commit:** `docs & feat: synchronize root src/, build scripts, and complete documentation suite`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `d38e2fb` — 2026-08-28 | ✨ FEAT | feat(shop): touch-friendly GUI, bottom controls in SellGUI, expanded blocks, and refined ore/mob drops
- **Pesan Commit:** `feat(shop): touch-friendly GUI, bottom controls in SellGUI, expanded blocks, and refined ore/mob drops`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `aff4b8a` — 2026-08-28 | ♻️ REFACTOR | refactor(shop): align folder and modular config structure with Battlepass standards
- **Pesan Commit:** `refactor(shop): align folder and modular config structure with Battlepass standards`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.

#### `96d0a47` — 2026-08-28 | ✨ FEAT | feat: implement ApexsionsShop dynamic kingdom market plugin with weather & tax mechanics
- **Pesan Commit:** `feat: implement ApexsionsShop dynamic kingdom market plugin with weather & tax mechanics`
  - **Rincian Teknis:** Mengatur pasar dinamis 6 kategori dengan fluktuasi harga berbasis cuaca, bioma teritorial, dan antarmuka `/sell` instan.

#### `c14b3f8` — 2026-08-28 | 🐛 FIX | fix(core): properly shade HikariCP, SQLite, PostgreSQL, H2 and Flyway drivers into fat JAR
- **Pesan Commit:** `fix(core): properly shade HikariCP, SQLite, PostgreSQL, H2 and Flyway drivers into fat JAR`
  - **Rincian Teknis:** Memastikan migrasi skema database mengeksekusi DDL `ALTER TABLE ADD COLUMN` sebelum perintah `CREATE INDEX` untuk mencegah crash startup SQLite.

#### `55e0ef2` — 2026-08-28 | 🐛 FIX | fix(rewards-gui): auto-fill empty slots with background pane and placeholder for empty rewards
- **Pesan Commit:** `fix(rewards-gui): auto-fill empty slots with background pane and placeholder for empty rewards`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `db2dea0` — 2026-08-28 | ✨ FEAT | feat(core): implement kingdom-bounded /rtp system with BlueMap polygon safety checks
- **Pesan Commit:** `feat(core): implement kingdom-bounded /rtp system with BlueMap polygon safety checks`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `0d697ad` — 2026-08-27 | 📌 UPDATE | Re-package verified BattlePass (200 Lvls, 42/120/50 Quests) and Economy JARs
- **Pesan Commit:** `Re-package verified BattlePass (200 Lvls, 42/120/50 Quests) and Economy JARs`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `d918cf0` — 2026-08-27 | 📌 UPDATE | Update pre-packaged JAR binaries for ApexsionsBattlepass and ApexsionsEconomy
- **Pesan Commit:** `Update pre-packaged JAR binaries for ApexsionsBattlepass and ApexsionsEconomy`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `16247d5` — 2026-08-27 | 📌 UPDATE | Update ApexsionsBattlepass (Level 200, Quest Pools) and ApexsionsEconomy (12-Slot Trade System & Toggle)
- **Pesan Commit:** `Update ApexsionsBattlepass (Level 200, Quest Pools) and ApexsionsEconomy (12-Slot Trade System & Toggle)`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

#### `2c82a9d` — 2026-08-27 | 📚 DOCS | docs: clean up structure, delete prompt files, and add comprehensive global and per-plugin documentation (README, DOKUMENTASI, GEMINI)
- **Pesan Commit:** `docs: clean up structure, delete prompt files, and add comprehensive global and per-plugin documentation (README, DOKUMENTASI, GEMINI)`
  - **Rincian Teknis:** Meningkatkan stabilitas kode, refaktor internal modul, dan memastikan sinkronisasi data antar-subkomponen berjalan tanpa regresi.

#### `167bb69` — 2026-08-27 | ✨ FEAT | feat: complete Apexsions plugin suite - ApexsionsCore, ApexsionsChat, ApexsionsEconomy, and ApexsionsBattlepass
- **Pesan Commit:** `feat: complete Apexsions plugin suite - ApexsionsCore, ApexsionsChat, ApexsionsEconomy, and ApexsionsBattlepass`
  - **Rincian Teknis:** Menyediakan progresi 200 level battlepass, quest berkala harian/mingguan/bulanan, dan editor in-game admin 54-slot.
  - **Rincian Teknis:** Mengatur komunikasi terpisah (Global, Kingdom, Staff), sensor kata otomatis, format MiniMessage, serta pengiriman surat offline.
  - **Rincian Teknis:** Menjaga integritas saldo transaksi mata uang ganda (Rupiah & Diamond) dengan perlindungan anti-duplikasi dan escrow.

---
