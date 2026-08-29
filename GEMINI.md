# Gemini & Agent Development Guidelines — Apexsions Plugin Suite

Dokumen ini berisi standar arsitektur, konvensi penamaan, dan aturan kerja wajib untuk pengembangan seluruh plugin di ekosistem **Apexsions**.

---

## 🔄 Aturan Wajib Sinkronisasi Multi-Developer (GitHub Remote First)
Repository dikerjakan bersama oleh beberapa developer. Oleh karena itu, **SEBELUM melakukan perubahan atau saat memulai pekerjaan**:
1. **Wajib Periksa Remote GitHub Terlebih Dahulu**:
   - Jalankan `git fetch origin` untuk mendeteksi apakah ada pembaruan dari developer lain di `origin/main`.
   - Cek perbedaan dengan `git log HEAD..origin/main --oneline`.
2. **Otomatis Masukkan / Sinkronkan Perubahan Baru**:
   - Jika terdapat perubahan baru di remote (`origin/main`) dan working tree lokal bersih, lakukan `git pull --ff-only origin main` (atau sinkronkan dengan aman).
   - Segera terapkan/update ke workspace lokal dan salin JAR terbaru ke server jika relevan.
   - Jangan pernah mulai mengubah kode berdasarkan salinan lama (*stale branch*).
3. **Pemeriksaan Ulang Sebelum Push (*Pre-Push Safety*)**:
   - Sebelum commit/push, selalu lakukan `git fetch origin` kembali untuk memastikan tidak ada commit baru yang masuk saat agent sedang bekerja (*race condition*).
4. **Larangan Destruktif**: Dilarang menggunakan `git reset --hard`, `git push --force`, atau `git clean -fd` tanpa alasan valid yang diverifikasi.

---

## 🏷️ Aturan Penamaan Merek & Paket (Brand Rules)
1. **Nama Resmi**: **`Apexsions`** (dengan huruf 's' di tengah — **BUKAN** `Apexions`).
2. **Penamaan Plugin (6 Plugin Utama)**:
   - `ApexsionsCore` (Sistem Kerajaan, Progresi, Kingdom War, Combat Tag, Multiverse & RTP)
   - `ApexsionsChat` (Sistem Obrolan, Channel Settings GUI, & Moderasi Lapis Tiga)
   - `ApexsionsEconomy` (Sistem Multi-Currency, Lelang & Barter)
   - `ApexsionsBattlepass` (Sistem Quests, Passes & Toko Rotasi)
   - `ApexsionsShop` (Sistem Toko & Pasar Dinamis Kerajaan)
   - `ApexsionsMedia` (Sistem Banner/Logo Interaktif, Raytrace Hover Glow, & Aksi Tautan URL)
3. **Penamaan Paket Java (Standar Konsisten)**:
   - `com.apexsions.core.*`
   - `com.apexsions.chat.*`
   - `com.apexsions.economy.*`
   - `com.apexsions.battlepass.*`
   - `com.apexsions.shop.*`
   - `com.apexsions.media.*`

---

## 🛠️ Standar Toolchain & Teknologi
- **Bahasa**: Java 21 LTS (Records, Pattern Matching, Virtual Threads ready).
- **Target Platform**: Paper API `1.21.4-R0.1-SNAPSHOT` (Minecraft 1.21.4).
- **Build Tool**: PowerShell automated multi-compiler (`build.ps1`) & Apache Maven 3.9.9 (`mvn clean package`).
- **Text & Component Engine**: Kyori Adventure 4.x + MiniMessage (Hindari legacy formatting `&` atau `§` di kode baru; gunakan MiniMessage).
- **Basis Data & Concurrency**: Multi-database abstraction via HikariCP (SQLite file-based dan PostgreSQL production-ready) dengan asynchronous `CompletableFuture`. Semua mutasi Bukkit game state wajib kembali ke Main Thread.
- **Integritas Transaksi**: Operasi ekonomi, `/ah`, `/trade`, dan `/shop` wajib atomic, concurrency-safe, dan mencegah eksploitasi/duplikasi item.

---

## 🧩 Pola Arsitektur Antar-Plugin (Inter-Plugin Integration)
1. **Penyedia API Global (*Service Provider Interface*) & Custom Events**:
   - `ApexsionsCoreProvider.get()` mengekspos `ApexsionsCoreAPI` untuk query status kerajaan, level, XP, dan wilayah poligon.
   - `ApexsionsEconomyAPI` mengekspos method static atomic untuk saldo Rupiah/Diamond dan transaksi.
   - `ApexsionsShopProvider.get()` mengekspos `ApexsionsShopAPI` untuk kalkulasi harga dinamis, pajak, dan inventori toko.
   - Event-driven cross-plugin hooks (`KingdomWarStartEvent`, `PlayerLevelUpEvent`, `MarketPriceChangeEvent`, `PlayerBalanceChangeEvent`, dll).
2. **Ketergantungan Lunak (*Soft Dependency*)**:
   - Setiap plugin harus dapat berjalan mandiri jika plugin lain dinonaktifkan dengan graceful fallback.
3. **Struktur Konfigurasi Modular**:
   - Hindari satu file konfigurasi raksasa. Pecah file YAML ke dalam direktori tematik (misal: `passes/`, `quests/`, `shop/`, `exp-shop/`, `categories/`, `gui.yml`, `messages.yml`, `markets.yml`, `ranks.yml`, `rewards.yml`, `moderation.yml`).

---

## 🚀 Alur Git & Rilis
- Repositori Utama: `https://github.com/Nueeva/Apexsions.git`
- Branch Utama: `main`
- **Aturan Rilis**: Setiap pembaruan kode dan fitur **WAJIB** memperbarui dokumentasi (`README.md`, `DOKUMENTASI.md`, `GEMINI.md`, `docs/`), mengompilasi seluruh JAR via `build.ps1`, serta meng-commit dan mem-push seluruh source code (termasuk folder `src/`, `plugins/`, file build, dan binary `.jar`) ke GitHub!

