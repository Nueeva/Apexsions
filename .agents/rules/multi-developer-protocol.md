# Multi-Developer Collaboration & GitHub Sync Protocol

Aturan ini wajib ditaati oleh AI Agent untuk workspace Apexsions:

1. **Selalu Sinkronisasi GitHub Terlebih Dahulu (Git Fetch & Pull)**:
   - Sebelum menganalisis atau mengubah source code / konfigurasi apa pun, AI Agent harus selalu menjalankan:
     ```powershell
     git fetch origin
     git log HEAD..origin/main --oneline
     ```
   - Jika terdapat commit baru dari developer lain atau owner di GitHub (`origin/main`), AI Agent **wajib langsung menarik perubahan terbaru** (`git pull --ff-only origin main`) dan menerapkannya ke proyek di dalam root workspace.
   - Jangan pernah membuat asumsi atau menulis kode di atas branch yang tertinggal (*stale branch*).

2. **Isolasi Workspace Ketat (Strict Workspace Isolation)**:
   - DILARANG KERAS mengakses, memodifikasi, atau menyalin file ke direktori mana pun di luar repository/project root.
   - Seluruh source code, config, dan file binary JAR (`build/libs/`, `plugins/`) hanya dikelola di dalam workspace repository root.

3. **Periksa Ulang Sebelum Push (*Pre-Push Safety*)**:
   - Sebelum melakukan commit atau push, periksa kembali apakah ada commit baru yang masuk dari developer lain selama proses pengerjaan (`git fetch origin`) untuk mencegah race condition.

4. **Integritas Kode & Arsitektur**:
   - Mematuhi seluruh standar di `GEMINI.md` dan `AGENTS.md`.
   - Menjaga modularitas 6 plugin di dalam folder `plugins/` (`ApexsionsCore`, `ApexsionsChat`, `ApexsionsEconomy`, `ApexsionsBattlepass`, `ApexsionsShop`, `ApexsionsMedia`).
   - Menggunakan Kyori Adventure + MiniMessage, Paper 1.21.4 API, Java 21, dan HikariCP async.
   - Mengikuti **Targeted Build Rule** (`powershell -ExecutionPolicy Bypass -File .\build.ps1 <PluginName>`).

5. **Validasi Lokal Terlebih Dahulu Sebelum Deploy ke VPS (Local-First Testing Before Remote Deployment)**:
   - DILARANG KERAS melakukan trial-and-error, eksperimen arsitektur, atau live-debugging langsung di server remote / VPS production.
   - Semua perubahan pada web portal (Azuriom theme Blade, logic plugin, route definition, provider namespace/autoloading, dan skema database) maupun Minecraft server WAJIB diuji dan divalidasi di environment lokal terlebih dahulu (`php -l`, syntax check, artisan CLI, atau local runtime).
   - Pastikan kode 100% bebas dari syntax error, missing class, atau route conflict secara lokal sebelum melakukan sinkronisasi/deployment ke VPS remote.
   - Deployment ke VPS hanya dilakukan sebagai langkah akhir verifikasi produksi (*clean deployment*), bukan tempat *troubleshooting* awal.

6. **Integritas Brand & Nama Server (Strict Brand Identity)**:
   - Nama resmi server adalah murni **Apexsions** (bukan `Apexsions Network`, `Apexsions Kingdom`, `Apexsions SMP`, dll).
   - Tagline resmi server adalah **The Peak Civilizations**.
   - DILARANG KERAS menambahkan embel-embel atau variasi kata buatan pada nama brand server di dalam kode, template UI/Blade, pesan chat, konfigurasi, maupun dokumentasi.
