# Gemini & Agent Development Guidelines — Apexsions Plugin Suite

Dokumen ini berisi standar arsitektur, konvensi penamaan, dan aturan kerja untuk pengembangan seluruh plugin di ekosistem **Apexsions**.

---

## 🏷️ Aturan Penamaan Merek & Paket (Brand Rules)
1. **Nama Resmi**: **`Apexsions`** (dengan huruf 's' di tengah — **BUKAN** `Apexions`).
2. **Penamaan Plugin (6 Plugin Utama)**:
   - `ApexsionsCore` (Sistem Kerajaan, Progresi, Kingdom War, Combat Tag, & RTP)
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
- **Basis Data**: Multi-database abstraction via HikariCP (SQLite file-based dan PostgreSQL production-ready).

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
