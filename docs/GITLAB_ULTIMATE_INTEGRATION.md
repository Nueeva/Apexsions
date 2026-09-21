# Panduan Pemanfaatan GitLab Ultimate — Apexsions

Dokumen ini menjelaskan langkah demi langkah cara memanfaatkan lisensi **GitLab Ultimate** untuk ekosistem **Apexsions** melalui arsitektur **Hybrid Mirroring** (tetap memakai GitHub sebagai pusat kerja sehari-hari, dan GitLab Ultimate sebagai mesin DevSecOps & CI/CD otomatis).

---

## 1. Menghubungkan Repositori GitHub ke GitLab (Mirroring)

Agar Anda tidak perlu repot melakukan push dua kali (ke GitHub dan GitLab secara manual):
1. Buat proyek baru di akun GitLab Ultimate Anda:
   * Nama Proyek: `Apexsions`
   * Visibility: `Private` atau `Public` sesuai kebutuhan.
2. Masuk ke **Settings** > **Repository** di GitLab.
3. Buka bagian **Mirroring repositories**:
   * **Git repository URL**: `https://github.com/Nueeva/Apexsions.git`
   * **Mirror direction**: `Pull` (GitLab akan otomatis menarik setiap commit yang masuk ke GitHub `main`).
   * **Authentication method**: `Username and Personal Access Token` (Gunakan GitHub Username dan PAT GitHub Anda).
   * Centang **Trigger pipelines for mirror updates**.
   * Klik **Mirror repository**.

> [!TIP]
> Setiap kali agen atau Anda melakukan `git push origin main` ke GitHub, GitLab akan menyalin commit secara otomatis dalam beberapa detik dan langsung menjalankan pipeline CI/CD.

---

## 2. Fitur GitLab Ultimate yang Otomatis Aktif via `.gitlab-ci.yml`

Berkas [`.gitlab-ci.yml`](../.gitlab-ci.yml) yang telah dipasang di root repositori mencakup:

### A. DevSecOps (Security & Compliance)
1. **SAST (Static Application Security Testing)**:
   * Menggunakan analyzer Semgrep & SpotBugs untuk memindai kode Java 21 (`Minecraft/plugins/`) dan PHP 8.2 (`Website/themes/` & `Website/plugins/`).
   * Mendeteksi otomatis: potensi exploit duplikasi, SQL Injection, buffer overflow, perizinan tidak aman, dan deserialisasi berbahaya.
2. **Secret Detection**:
   * Memastikan tidak ada token bot, private key, atau password infrastruktur yang tidak sengaja ter-commit.
3. **Dependency Scanning**:
   * Memeriksa seluruh dependensi `pom.xml` terhadap database kelemahan CVE dunia (Supply-Chain Security).

### B. Otomasi Build Maven (Java 21 LTS)
* Menjalankan container `maven:3.9-eclipse-temurin-21` di cloud GitLab Runner.
* Mengompilasi ke-9 plugin resmi Apexsions secara otomatis.
* Mengumpulkan file `.jar` ke folder `build/libs/` dan menyimpannya sebagai **Downloadable Artifacts** (tersedia untuk diunduh langsung dari GitLab selama 30 hari).

### C. Continuous Deployment (CD via SFTP)
* Pekerjaan `deploy:game-server` memungkinkan pengunggahan otomatis seluruh JAR hasil kompilasi ke game server (`falcon04.jagoanhosting.id:2022`).
* Secara default diatur `when: manual` sehingga Anda bisa mengeklik tombol **Play (▶)** di GitLab saat siap rilis.

---

## 3. Konfigurasi Rahasia (CI/CD Variables) di GitLab

Untuk mengaktifkan fitur deployment otomatis tanpa mengekspos kredensial di kode:
1. Masuk ke menu **Settings** > **CI/CD** di proyek GitLab Anda.
2. Buka bagian **Variables** lalu klik **Add variable**:
   * **Key**: `SFTP_PASS`
   * **Value**: Masukkan password SFTP game server (`NuevaStore123#`).
   * Centang **Mask variable** (agar tidak muncul di log runner).
   * Centang **Protect variable** (jika pipeline hanya berjalan di branch `main`).
   * Klik **Save variable**.

---

## 4. Melihat Dashboard Keamanan (Security Dashboard)

Begitu pipeline pertama selesai berjalan:
1. Buka menu **Security** di sidebar kiri GitLab:
   * **Vulnerability Report**: Menampilkan daftar celah keamanan terdeteksi lengkap dengan tingkat keparahan (*Critical*, *High*, *Medium*, *Low*).
   * **Dependency List**: Menampilkan Software Bill of Materials (SBOM) seluruh library yang digunakan server Apexsions.
2. Anda dapat mengeklik setiap kerentanan untuk melihat baris kode penyebabnya serta rekomendasi perbaikannya.
