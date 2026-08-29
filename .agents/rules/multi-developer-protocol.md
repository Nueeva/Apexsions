# Multi-Developer Collaboration & GitHub Sync Protocol

Aturan ini wajib ditaati oleh AI Agent untuk workspace Apexsions:

1. **Selalu Sinkronisasi GitHub Terlebih Dahulu (Git Fetch & Pull)**:
   - Sebelum menganalisis atau mengubah source code / konfigurasi apa pun, AI Agent harus selalu menjalankan:
     ```powershell
     git fetch origin
     git log HEAD..origin/main --oneline
     ```
   - Jika terdapat commit baru dari developer lain atau owner di GitHub (`origin/main`), AI Agent **wajib langsung menarik perubahan terbaru** (`git pull origin main`) dan menerapkannya ke proyek serta server lokal (`C:\server minecraft\plugins`).
   - Jangan pernah membuat asumsi atau menulis kode di atas branch yang tertinggal (*stale branch*).

2. **Periksa Ulang Sebelum Push (*Pre-Push Safety*)**:
   - Sebelum melakukan commit atau push, periksa kembali apakah ada commit baru yang masuk dari developer lain selama proses pengerjaan untuk mencegah race condition.

3. **Integritas Kode & Arsitektur**:
   - Mematuhi seluruh standar di `SYSTEM PROMPT_ Apexsions Senior Minecraft Plugin Engineer.md` dan `GEMINI.md`.
   - Menjaga modularitas 6 plugin di dalam folder `plugins/`.
   - Menggunakan Kyori Adventure + MiniMessage, Paper 1.21.4 API, Java 21, dan HikariCP async.
