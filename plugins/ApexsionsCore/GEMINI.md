# GEMINI.md — ApexsionsCore AI & Development Guidelines

Dokumen ini berisi panduan, standar arsitektur, dan instruksi penting untuk AI agent (Gemini / Antigravity) dan pengembang yang bekerja pada proyek **ApexsionsCore**.

---

## 🚨 ATURAN UTAMA (MANDATORY RULES)

1. **Selalu Update Dokumentasi**: Setiap kali selesai mengubah, menambah, atau merefaktor kode/fitur, **WAJIB** memperbarui file dokumentasi terkait ([README.md](file:///c:/Users/Friel/Documents/Rifqi%20Ariansyah/Apexsions/plugins/KingdomCore/README.md) dan/atau [DOCUMENTATION.md](file:///c:/Users/Friel/Documents/Rifqi%20Ariansyah/Apexsions/plugins/KingdomCore/DOCUMENTATION.md)). Dokumentasi tidak boleh usang (*out-of-sync*).
2. **Build Tool & Packaging**:
   - Proyek ini dikompilasi menggunakan Apache Maven yang tersedia di `./apache-maven-3.9.9/bin/mvn.cmd` atau Maven sistem.
   - Perintah build utama:
     ```powershell
     .\apache-maven-3.9.9\bin\mvn.cmd clean package
     ```
   - File output final adalah fat/shaded jar: `target/ApexsionsCore-1.0.0.jar` (dan di-copy ke root `./ApexsionsCore-1.0.0.jar`).
3. **Thread Safety & Async I/O**:
   - **DILARANG KERAS** melakukan blocking JDBC / database query, file I/O, atau network request di Minecraft Server Main Thread.
   - Semua operasi database wajib menggunakan `DatabaseManager.supplyAsync()` atau `DatabaseManager.runAsync()`.
   - Modifikasi object Bukkit / Paper yang *thread-confined* (seperti teleport, inventory, sound, sendMessage) harus dikembalikan ke server thread jika dipicu dari async task.
4. **Adventure Component Standard**:
   - Gunakan Adventure Component (`MiniMessage`, `Component`, `Title`) untuk semua teks, chat, title, dan pesan.
   - **Jangan** menggunakan legacy `ChatColor` atau format string `§`.
5. **No Direct Database Access from Other Plugins**:
   - Plugin luar harus berinteraksi melalui `ApexsionsCoreAPI` (`ApexsionsCoreProvider.get()`), bukan menyentuh PostgreSQL / database langsung.
6. **Anti-Abuse Standard**:
   - Setiap penambahan XP source baru harus memperhitungkan potensi exploit (contoh: tracking block placement, cooldown, distance threshold).
7. **Idempotent Rank Provisioning**:
   - Rank LuckPerms dikelola otomatis secara idempoten oleh `LuckPermsRankProvisioner` tanpa menghapus permission custom admin atau menduplikasi grup.

---

## 📁 Struktur Direktori & Arsitektur

```text
ApexsionsCore/
├── pom.xml                               # Konfigurasi Maven & Maven Shade Plugin
├── build.gradle                          # Konfigurasi Gradle pendukung
├── README.md                             # Panduan umum pengguna & server admin
├── DOCUMENTATION.md                      # Dokumentasi teknis mendalam
├── GEMINI.md                             # Panduan AI agent & rules
├── apache-maven-3.9.9/                   # Bundled Maven runtime
├── src/
│   ├── main/
│   │   ├── java/com/yourserver/apexsionscore/
│   │   │   ├── ApexsionsCorePlugin.java  # Main Bukkit Plugin Entrypoint
│   │   │   ├── api/                      # Public API & Provider
│   │   │   ├── cache/                    # Caffeine in-memory cache
│   │   │   ├── chat/                     # AsyncChatFormatter & listener
│   │   │   ├── command/                  # /lobby, /region, /apexsionscore (/ac), /level
│   │   │   ├── config/                   # ConfigManager
│   │   │   ├── database/                 # HikariCP, Repositories, Migrations
│   │   │   ├── event/                    # Custom Paper Events
│   │   │   ├── integration/              # Hooks: LuckPerms, Vault, PAPI, Essentials
│   │   │   ├── level/                    # LevelManager, Formula, Titles
│   │   │   │   └── xp/                   # 13 XP Handlers, Registry, Anti-Abuse
│   │   │   ├── player/                   # PlayerData, Service, Listener
│   │   │   └── region/                   # Region, Manager, Teleport, GUIs
│   │   └── resources/
│   │       ├── plugin.yml                # Plugin manifest
│   │       ├── config.yml                # Main configuration
│   │       ├── ranks.yml                 # LuckPerms ranks & owner UUID config
│   │       ├── rewards.yml               # Deterministic level & milestone rewards
│   │       ├── kingdoms.yml              # Realms & kingdoms config
│   │       ├── gui.yml                   # GUI configuration
│   │       ├── xp.yml                    # XP per source configuration
│   │       └── db/migration/             # Flyway SQL migrations (V1..V4)
│   └── test/                             # Unit tests (JUnit 5)
```

---

## 🧪 Verifikasi & Kompilasi

Sebelum menandai pekerjaan selesai, selalu jalankan:
```powershell
.\apache-maven-3.9.9\bin\mvn.cmd clean package
```
Pastikan `BUILD SUCCESS` dan `ApexsionsCore-1.0.0.jar` ter-generate dengan benar.
Update selalu `walkthrough.md` atau catatan riwayat bila ada perubahan arsitektural.
