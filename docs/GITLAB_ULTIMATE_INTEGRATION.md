# Panduan Pemanfaatan & Strategi Siklus Hidup GitLab Ultimate — Apexsions

> **Status Lisensi:** GitLab Ultimate Trial (Aktif s/d **12 Oktober 2026**)  
> **Target Proyek:** [`nueva-group2/apexsions`](https://gitlab.com/nueva-group2/apexsions) (Project ID: `86719404`)  
> **Arsitektur:** **Dual-Push Hybrid Mirroring** (GitHub sebagai *Single Source of Truth* utama, GitLab Ultimate sebagai *DevSecOps & CI/CD Engine*).

---

## 1. Arsitektur Sinkronisasi (Dual-Push Hybrid)

Untuk menjaga workflow tetap ringkas dan tidak membebani developer:
* **GitHub (`Nueeva/Apexsions`)** adalah pusat repositori utama (**Single Source of Truth**). Seluruh commit, branch, dan PR tetap berpusat di GitHub.
* Git lokal telah dikonfigurasi menggunakan **Dual-Push**:
  ```powershell
  # Sekali eksekusi "git push origin main", commit otomatis terkirim ke GitHub DAN GitLab sekaligus
  git push origin main
  ```
* **GitLab (`nueva-group2/apexsions`)** menerima commit secara instan dan langsung mengeksekusi pipeline otomatis di cloud runner.

---

## 2. Rencana Aksi Selama Masa Trial (21 September – 12 Oktober 2026)

Masa trial 30 hari menyediakan akses penuh ke fitur enterprise GitLab Ultimate senilai \$99/user/bln. Berikut cara memanfaatkannya secara maksimal:

### A. Remediasi Keamanan Kode (Vulnerability Report)
* GitLab Ultimate telah memindai seluruh kode Java 21 dan PHP 8.2 secara otomatis.
* Buka **Security** > [**Vulnerability report**](https://gitlab.com/nueva-group2/apexsions/-/security/vulnerabilities):
  1. **SAST (Static Application Security Testing):** Memeriksa potensi SQL Injection, deserialisasi Java tidak aman, dan logika izin di 9 plugin Minecraft serta Azuriom.
  2. **Dependency Scanning (Gemnasium):** Memeriksa seluruh library di `pom.xml` dan `composer.json` terhadap database CVE dunia (misalnya advisory pada `league/commonmark`).
  3. **Secret Detection:** Menjamin tidak ada API key, token bot, atau kredensial server yang lolos ke commit publik.
* **Target:** Bersihkan dan remedi seluruh temuan berkategori *High* dan *Medium* sebelum tanggal 12 Oktober agar basis kode Apexsions bersih dan tahan audit.

### B. Otomasi Build Maven Multi-Plugin di Cloud
* Menghemat daya dan baterai laptop lokal: Runner cloud `maven:3.9-eclipse-temurin-21` mengompilasi ke-9 plugin resmi Apexsions secara otomatis setiap ada push ke `main`.
* **Unduh Artefak:** File `.jar` yang berhasil dikompilasi tersimpan rapi di tab **Build > Artifacts** dan dapat diunduh langsung kapan saja selama 30 hari.

### C. Continuous Deployment (CD via SFTP)
* Pekerjaan `deploy:game-server` di [`.gitlab-ci.yml`](../.gitlab-ci.yml) siap mengunggah hasil build langsung ke server game Jagoanhosting (`falcon04.jagoanhosting.id:2022`).
* Secara default diatur `when: manual` sehingga Anda cukup mengeklik tombol **Play (▶)** di halaman pipeline GitLab saat server siap diperbarui.

---

## 3. Strategi Transisi Pasca-Trial (Setelah 12 Oktober 2026)

> [!IMPORTANT]
> **Zero Operational Impact (100% Aman):**
> Berakhirnya masa trial GitLab Ultimate **TIDAK AKAN MENGGANGGU** operasional server game Minecraft, VPS web Azuriom, database, ataupun kelancaran development di GitHub.

Setelah 12 Oktober 2026, akun GitLab Anda akan otomatis beralih ke **GitLab Free Tier**. Anda memiliki 2 pilihan strategis:

### Opsi 1: Tetap Menggunakan GitLab Free (Direkomendasikan)
* **Keuntungan:** Anda tetap mendapatkan **400 menit/bulan kuota gratis GitLab Runner**.
* **Fitur yang Tetap Berfungsi:**
  - Build otomatis 9 plugin Maven Java 21 (`build:plugins`).
  - Linter validasi sintaks PHP (`lint:php`).
  - Unggah otomatis ke game server via SFTP (`deploy:game-server`).
  - Laporan scan SAST & Secret Detection dasar (dalam format file artefak JSON).
* **Fitur yang Berhenti:** Visual grafik interaktif pada dashboard *Vulnerability Report* berbayar akan dikunci oleh GitLab, namun pipeline CI/CD inti tetap 100% berjalan normal.

### Opsi 2: Melepaskan GitLab & Kembali Murni ke GitHub
Jika setelah 12 Oktober Anda memutuskan untuk tidak lagi menggunakan GitLab sama sekali, cukup putuskan remote GitLab dari Git lokal dengan 2 perintah berikut:

```powershell
# 1. Hapus endpoint push GitLab dari remote origin
git remote set-url --delete --push origin "https://oauth2:glpat-RXtf8_oQg0oUKnvmM5qRUmM6MQpvOjEKdTpwOHZ0eQ8.01.17068ksk0@gitlab.com/nueva-group2/apexsions.git"

# 2. Hapus remote sekunder gitlab
git remote remove gitlab
```

Setelah perintah di atas dijalankan:
* `git push origin main` akan kembali murni mengirim ke GitHub saja.
* Seluruh file konfigurasi di laptop lokal dan GitHub tetap utuh tanpa kendala.

---

## 4. Rincian Konfigurasi CI/CD Variabel

| Variabel | Fungsi | Nilai Default | Keterangan |
| :--- | :--- | :--- | :--- |
| `SFTP_HOST` | Host SFTP Game Server | `falcon04.jagoanhosting.id` | Otomatis terkonfigurasi di `.gitlab-ci.yml` |
| `SFTP_PORT` | Port SFTP Game Server | `2022` | Port game server Jagoanhosting |
| `SFTP_USER` | Username Akun SFTP | `rifqiariansyah123jt3.27e4a2f6` | Akun game server |
| `SFTP_PASS` | Password Akun SFTP | *(Diset di GitLab Settings)* | Pasang di *Settings > CI/CD > Variables* (Masked & Protected) |
