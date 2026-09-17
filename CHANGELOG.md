# CHANGELOG.md — Apexsions Development & Evolution History
# Apexsions — The Peak Civilizations

> **Repository:** `Nueeva/Apexsions`  
> **Brand Name:** `Apexsions` (DILARANG menambahkan kata Network/SMP/Kingdom)  
> **Tagline:** `The Peak Civilizations`  
> **Format:** Berdasarkan standar [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

Dokumen ini mendokumentasikan seluruh riwayat perubahan, penambahan fitur, perbaikan bug, keputusan arsitektur, serta panduan alih-tugas (*AI Agent & Developer Handoff*) untuk ekosistem **Apexsions**.

---

## 🤖 Panduan Alih-Tugas untuk AI Coding Agent & Pengembang Baru (Quick Handoff)

Bagi AI Agent atau developer yang melanjutkan pekerjaan di repositori ini, perhatikan kontrak operasional berikut:

1. **Brand Identity Mandate:**
   - Nama server adalah strictly **`Apexsions`**.
   - Dilarang keras menambahkan akhiran seperti "Network", "Kingdom", "SMP" pada teks UI, chat, nama kelas, atau dokumentasi.
2. **Kanon Tiga Kerajaan Berdaulat:**
   - **Zenithar (Timur):** Ibukota *Solarium Spire Citadel*, Pajak Kas `18.0%`. Spesialisasi: Kecepatan (+8%), Keberuntungan (+15%), [Royal Discipline] (+6% Damage), [Royal Aegis] (20% Reduksi Damage Masuk), Hak Perbankan & Diskon Toko.
   - **Solterra (Selatan):** Ibukota *Ignis Bastion Fortress*, Pajak Kas `20.0%`. Spesialisasi: Penyerang Agresif, [Battle Momentum] (+15% Damage), (+10% Crit Damage), (+10% Mining Speed), Rasio Jual Ore Tinggi (65%).
   - **Sylvamoor (Barat):** Ibukota *Eldergrove Sanctuary*, Pajak Kas `15.0%`. Spesialisasi: Ketahanan Fisik, [Nature's Blessing] (+2 HP / 11 Hati), (+12% Luck), (+7% Mob Drops), Pertahanan Rimba (+15%), Racun Stop di 3 Hati, Lahan Abadi Basah.
   - **Dimensi Atas Aetherion (`The Aetherial Conclave`):** Seluruh staf (Weight $\ge 80$), operator (OP), dan entitas kosmik tidak terikat pada sistem 3 kerajaan, bebas pajak wilayah, dan memiliki klaim tanah tak terbatas.
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

## [1.2.0] — 2026-09-17

### 🌟 Added
- **Preview & Summary Card pada GUI Konfirmasi Kerajaan (`KingdomConfirmGUI`):**
  - Menambahkan Slot 13 pada menu konfirmasi pemilihan `/k choose` yang menampilkan kartu identitas kerajaan: nama ibukota, persentase pajak wilayah, daftar lengkap buff kanonikal, daftar debuff kanonikal, serta peringatan baiat seumur hidup.
- **Server Resource Pack Bersih untuk Bedrock (`ApexsionsCleanScoreboard.zip`):**
  - Mengintegrasikan server pack otomatis pada `plugins/Geyser-Spigot/packs/` yang meng-override `ui/scoreboards.json` (`"scoreboard_sidebar/main/lists/scores": { "ignored": true }`).
  - Menghilangkan kolom angka merah bawaan Bedrock (`15, 14, 13...`) pada sidebar.
- **Scoreboard Khusus Bedrock Mobile (`%bedrock%=true`):**
  - Menyediakan tata letak ultra-ringkas 8 baris esensial (Rank, Kerajaan, Saldo, Level, Online, IP) dengan panjang teks $\le 19$ karakter, memangkas penggunaan layar ponsel dari 35% menjadi hanya 10%–12%.
- **Sistem Kedaulatan Wilayah Tanah (`/claim`) & Pajak Progresif:**
  - Pemain dapat mengklaim petak tanah chunk berdaulat dengan sistem deposit brankas klaim (`/claim deposit`).
  - Biaya upkeep harian progresif berdasarkan jumlah total chunk yang dimiliki.
  - Masa tenggang tunggakan (*Grace Period*) sebelum wilayah otomatis dilepas kembali ke alam liar (*Wilderness*).
- **Web Admin Finansial & Teritorial Terpadu (`/admin/claims`):**
  - Dashboard web Azuriom dengan 5 metrik emas gelap, filter pencarian instan, suntik saldo darurat (*Admin Deposit*), dan penagihan pajak manual (*Force Collect Tax*).
- **Engine Moderasi Terpusat Otoritatif (`BanManager` & `BanCommand`):**
  - Menggantikan perintah moderasi bawaan EssentialsX dengan penegakan sanksi tingkat jaringan pada soket `AsyncPlayerPreLoginEvent` sebelum AuthMe berjalan.
- **Sistem Watchdog Keamanan Berlapis:**
  - `ClaimProtectionListener`: Proteksi penuh blok, kontainer, api, dan ledakan di wilayah berdaulat.
  - `AntiXrayListener`: Raytrace reach validation ($> 5.8\text{ m}$) dan pelacak lonjakan bijih langka ($\ge 8$ bijih / 60 detik) dengan alert lonceng ke meja staf.
  - `RedstoneWatchdogListener`: Pembekuan otomatis osilasi clock redstone cepat ($> 25$ pulsa / 2 detik) untuk menjaga 20 TPS server.
- **Ekosistem ApexsionsFishing v1.0.0:**
  - Sistem pancing AFK dan Active Reel Engine dengan mini-game visual.
  - Rarity 6 tingkat (`COMMON` hingga `MYTHIC`) berbasis kalkulasi bobot.
  - Brankas ikan 54-slot (`/vault`), pasar ekspor instan (`/fish sell`), joran auto-catch (`/fish rods`), serta Virtual Bait Quota (`/fish bait`).
  - Dialog GUI Native untuk Admin Rod Creator (`AdminRodCreatorGUI`).

### 🔄 Changed
- **Penyelarasan Nilai Buff & Debuff Kanonikal 100%:**
  - Menyelaraskan seluruh berkas live SFTP `kingdoms.yml`, in-game GUI (`RegionSelectionGUI`, `KingdomInfoGUI`, `KingdomConfirmGUI`), serta ensiklopedia web wiki (`Website/themes/apexsions/assets/js/app.js` dan `Website/public/assets/themes/apexsions/js/app.js`).
  - Pajak resmi: Zenithar `18.0%`, Solterra `20.0%`, Sylvamoor `15.0%`.
- **Kompatibilitas Custom Font Java pada Scoreboard TAB:**
  - Mengganti seluruh garis Unicode pembatas kotak `──────────` menjadi karakter hyphen ASCII berkode coret (`&8&m------------------`).
  - Mencegah garis patah-patah (*jagged line gaps*) dan kotak tanda tanya saat pemain menggunakan Resource Pack ber-font kustom.
- **Pengecualian 6-Lapis Leaderboard Publik:**
  - Seluruh staf (Weight $\ge 80$), operator (OP), entitas Aetherion, dan role admin disaring keluar dari seluruh leaderboard game (`/kingdom top`, `/fish top`, level, ekonomi) dan leaderboard web platform (`/leaderboard`).
- **Peniadaan Papan Peringkat Battlepass di Web:**
  - Menghapus leaderboard BattlePass dari portal web agar eksklusif di in-game guna menjaga fokus kompetisi peradaban pada Level/EXP dan Kekayaan Rupiah.

### 🐛 Fixed
- **Kegagalan Startup SQLite pada Database Manager (`no such column: status`):**
  - Memperbaiki urutan eksekusi migrasi skema tabel `territory_claims`. Memastikan pengecekan dan penambahan kolom (`ALTER TABLE territory_claims ADD COLUMN status ...`) dijalankan sebelum perintah pembuatan indeks (`CREATE INDEX idx_claims_status`).
- **NullPointerException (NPE) pada Console / WebBridge Execution:**
  - Mengimplementasikan pola failsafe Lazy Auto-Resolution pada `BanCommand` dan `ClaimCommand` sehingga perintah tidak error saat dipanggil oleh WebBridge atau console sebelum lifecycle selesai.
- **Hak Istimewa Dimensi Atas Conclave pada Sistem Klaim:**
  - Memastikan rank tingkat atas (*Ancestor, Architect, Overseer, Warden, Herald*) bebas dari pajak upkeep wilayah dan memiliki kuota klaim tak terbatas sesuai lore kanonikal.
- **Nginx Redirect Loop pada Halaman Web:**
  - Menghilangkan loop `ERR_TOO_MANY_REDIRECTS` pada rute web tertentu dengan membetulkan regex quantifier dan protokol SSL target pada reverse-proxy VPS.

---

## [1.1.0] — 2026-09-15

### 🌟 Added
- **Arsitektur The Aetherial Conclave (Dimensi Atas Aetherion):**
  - Entitas staf pengawas diisolasi dari friksi fana kerajaan dunia bawah.
  - Placeholder kustom `%apexsions_kingdom%` menghasilkan `AETHERION` untuk staf.
- **Zona Terlarang Reruntuhan Kuno Sions (Terra Interdicta):**
  - Wilayah anomali maut 11 titik poligon dengan *Hourly Temporal Engine* yang mereset kerusakan blok setiap 60 menit.
  - Peti relik terkunci dengan kunci 3-tier (*Common*, *Elite*, *Boss*).
- **Auto-Respawn Ibukota Kerajaan Terintegrasi BlueMap:**
  - Pemain yang tewas otomatis dibangkitkan di titik pusat ibukota kerajaannya masing-masing berdasarkan poligon BlueMap.
- **Sistem Moderasi Lapis Tiga pada ApexsionsChat:**
  - Meja laporan staf visual (`StaffReportsGUI`), sensor kata dinamis, dan sistem isolasi chat spam.

### 🔄 Changed
- **Pembersihan Branding Server:**
  - Menghapus seluruh imbuhan "Kingdom", "Network", dan "SMP" pada header UI, scoreboard, pesan selamat datang, dan website.
- **Standar Bilingual Klien (`APX_I18N`):**
  - Seluruh teks antarmuka web Azuriom wajib menggunakan atribut data bilingual (`data-i18n`) dengan perpindahan instan via `localStorage`.

---

## [1.0.0] — 2026-09-01

### 🌟 Added
- **Inisialisasi 9 Plugin Suite Ekosistem Apexsions:**
  - `ApexsionsCore` (v1.0.0): Inti peradaban, level 100 progresif, kerajaan, dan BlueMap.
  - `ApexsionsChat` (v1.0.0): MiniMessage formatting, multi-channel, dan mail.
  - `ApexsionsEconomy` (v1.0.0): Dual-Currency (Rupiah & Diamond), Auction House, Escrow.
  - `ApexsionsBattlepass` (v1.0.0): 200 Level BattlePass, Quest harian/mingguan, Admin GUI.
  - `ApexsionsShop` (v1.0.0): Toko dinamis 6 kategori, pengaruh cuaca & bioma, quick sell.
  - `ApexsionsMedia` (v1.0.0): Banner map renderer, raytrace hover glow, URL actions.
  - `ApexsionsCustomEnchants` (v1.0.0): 182 Enchants, dual-currency enchanter, armor set bonus.
  - `ApexsionsCrates` (v1.0.0): Peti misteri, animasi buka peti, milestone progression.
  - `ApexsionsFishing` (v1.0.0): Mesin pancing modern terintegrasi.
- **Web Platform Azuriom & Custom Theme `apexsions`:**
  - Tema sinematik bernuansa *Dark-Gold Civilization*.
  - Plugin integrasi `apexsions-bridge` untuk sinkronisasi data pemain in-game dan web secara real-time.
- **PowerShell Smart Turbo Compiler (`build.ps1`):**
  - Script build modular cepat untuk kompilasi parsial tanpa harus membangun ulang 9 plugin secara redundan.
