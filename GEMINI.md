# Gemini & Agent Development Guidelines — Apexsions Plugin Suite

Dokumen ini berisi standar arsitektur, konvensi penamaan, dan aturan kerja untuk pengembangan seluruh plugin di ekosistem **Apexsions**.

---

## 🏷️ Aturan Penamaan Merek & Paket (Brand Rules)
1. **Nama Resmi**: **`Apexsions`** (dengan huruf 's' di tengah — **BUKAN** `Apexions`).
2. **Penamaan Plugin**:
   - `ApexsionsCore` (Sistem Kerajaan & Progresi)
   - `ApexsionsChat` (Sistem Obrolan & Moderasi)
   - `ApexsionsEconomy` (Sistem Multi-Currency, Lelang & Barter)
   - `ApexsionsBattlepass` (Sistem Quests, Passes & Toko Rotasi)
3. **Penamaan Paket Java**:
   - `com.yourserver.apexsionscore.*`
   - `com.yourserver.apexsionschat.*`
   - `com.apex.economy.*`
   - `com.apex.battlepass.*`

---

## 🛠️ Standar Toolchain & Teknologi
- **Bahasa**: Java 21 LTS (Records, Pattern Matching, Virtual Threads ready).
- **Target Platform**: Paper API `1.21.4-R0.1-SNAPSHOT` (Minecraft 1.21.4).
- **Build Tool**: Apache Maven 3.9.9 (`mvn clean package`).
- **Text & Component Engine**: Kyori Adventure 4.x + MiniMessage (Hindari legacy formatting `&` atau `§` di kode baru; gunakan MiniMessage).
- **Basis Data**: Multi-database abstraction via HikariCP (SQLite file-based dan PostgreSQL production-ready).

---

## 🧩 Pola Arsitektur Antar-Plugin (Inter-Plugin Integration)
1. **Penyedia API Global (*Service Provider Interface*)**:
   - `ApexsionsCoreProvider.get()` mengekspos `ApexsionsCoreAPI` untuk query status kerajaan, level, XP, dan wilayah poligon.
   - `ApexsionsEconomyAPI` mengekspos method static atomic untuk saldo Rupiah/Diamond dan transaksi.
2. **Ketergantungan Lunak (*Soft Dependency*)**:
   - Setiap plugin harus dapat berjalan mandiri jika plugin lain dinonaktifkan dengan graceful fallback.
3. **Struktur Konfigurasi Modular**:
   - Hindari satu file konfigurasi raksasa. Pecah file YAML ke dalam direktori tematik (misal: `passes/`, `quests/`, `shop/`, `exp-shop/`, `ranks.yml`, `rewards.yml`, `moderation.yml`).

---

## 🚀 Alur Git & Rilis
- Repositori Utama: `https://github.com/Nueeva/Apexsions.git`
- Branch Utama: `main`
- Setiap update kode harus lolos kompilasi `mvn clean package` dan semua automated unit tests sebelum di-push.
