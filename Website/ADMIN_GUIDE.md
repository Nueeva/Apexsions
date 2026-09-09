# Apexsions Admin Dashboard — Official Admin & Operations Guide

> **Platform:** Apexsions Minecraft Server (Paper 26.2 / Java 21 LTS)  
> **Web Platform:** Azuriom (PHP 8.2 / Laravel 12)  
> **Brand:** `Apexsions`  
> **Tagline:** *The Peak Civilizations*  

---

## 1. Project Overview

Apexsions Admin Dashboard adalah sistem kontrol dan pemantauan terpadu untuk ekosistem server Minecraft **Apexsions**. Sistem ini dibangun dengan pendekatan *security-first*, mengintegrasikan data game server secara nyata melalui antrean sinkronisasi dua arah (**WebBridge**) dan suite custom plugin tanpa overengineering.

Dashboard ini ditujukan khusus untuk operasional staf dan administrator:
- Memantau kesehatan server secara nyata (TPS, RAM, disk, latensi antrean bridge).
- Memantau dan mengelola pemain menggunakan identitas authoritatif UUID.
- Melacak transaksi ekonomi multi-currency (Rupiah & Diamonds) secara auditabel.
- Mengelola laporan dan sanksi pemain dengan integrasi log in-game.
- Memantau kesehatan modul plugin custom serta mengesahkan instruksi aman (*Safe Actions*).
- Menginvestigasi anomali dan insiden dengan korelasi event otomatis.
- Mendistribusikan notifikasi insiden kritis dengan proteksi anti-spam dan Approval Gate.

---

## 2. Admin Dashboard Features

### A. Core Operations
1. **Executive Dashboard (`/admin/dashboard` & `/admin/apexsions-bridge/dashboard`):**
   - Ringkasan pemain online, TPS saat ini, antrean bridge, dan insiden terbuka.
   - Shortcut investigasi dan status operasional real-time.

2. **Player Management 360 (`/admin/apexsions-bridge/players`):**
   - Pencarian pemain berdasarkan UUID, username, atau kingdom.
   - Halaman detail komprehensif: level, kingdom, saldo ganda, status verifikasi, riwayat sanksi, riwayat transaksi, dan aksi administratif yang aman.

3. **Moderation Center (`/admin/apexsions-bridge/moderation`):**
   - Manajemen laporan pemain dengan bukti terlampir.
   - Manajemen hukuman aktif (Ban, Mute, Warn, Kick) dengan rekam jejak staf penindak.

4. **Economy Operations & Market Control (`/admin/apexsions-bridge/economy`):**
   - Ledger transaksi transparan dengan filter sender, receiver, tipe, dan mata uang.
   - Pemantau lelang (Auction House) dan saldo perbendaharaan Kingdom.
   - Penyesuaian saldo terotorisasi via template command bridge.

5. **Server Operations (`/admin/apexsions-bridge/server`):**
   - Telemetri real-time: TPS, penggunaan RAM, CPU, dan status pemeliharaan (Maintenance Mode).
   - Tindakan pemeliharaan aman dengan konfirmasi admin dan perekaman audit.

6. **Custom Plugin Registry & Control (`/admin/apexsions-bridge/plugins`):**
   - Pendaftaran dan status kesehatan 6 suite plugin Apexsions.
   - Matriks kapabilitas dan eksekusi aksi aman (*Safe Actions*) berbasis whitelist.

7. **Intelligence & Incident Center (`/admin/apexsions-bridge/incidents`):**
   - Deteksi anomali berbasis aturan (Rule-based detection) tanpa AI palsu.
   - Korelasi event dan timeline investigasi staf.

8. **Notification & Automation Hub (`/admin/apexsions-bridge/notifications` & `/automation`):**
   - Penanganan notifikasi insiden dengan deduplikasi dan cooldown anti-spam.
   - Approval Gate untuk instruksi otomatis berisiko (misal: reload modul degradasi).

9. **Unified Audit Log (`/admin/apexsions-bridge/audit`):**
   - Rekam jejak seluruh mutasi dan aksi staf, tidak dapat diubah (immutable trail).

10. **User Management Center (`/admin/users`):**
   - **Executive KPI Summary Cards**: Agregasi metrik real-time mencakup total akun terdaftar, akun terverifikasi, akun tertunda (unverified), pengguna dengan 2FA aktif, akun dalam penangguhan/ban, dan jumlah akun administrator.
   - **Live Search & Granular Filter**: Pencarian cepat berbasis username dan email, penyaringan berdasarkan status akun (`all`, `verified`, `unverified`, `2fa`, `banned`, `admin`), web role, dan pengurutan multi-kolom (`newest`, `oldest`, `name`, `last_login`).
   - **Integrasi Identitas Minecraft**: Eager loading relasi `minecraftAccount` (`minecraft_accounts` table) langsung pada daftar pengguna, menampilkan IGN in-game, rank in-game, dan copyable UUID.
   - **5-Section User Dossier (`/admin/users/{id}/edit`)**:
     1. *Identity*: Avatar, User ID, Username, Email, Tanggal Registrasi, dan UUID in-game.
     2. *Account Status & Roles*: Pengaturan Web Role, status verifikasi email, dan status sanksi (Ban/Suspension).
     3. *Security & Credentials*: Trigger reset kata sandi, status autentikasi dua faktor (2FA), dan riwayat aktivitas terakhir.
     4. *Minecraft Server Integration*: Panel data pemain terkait (IGN, in-game rank, Level & XP, serta tautan cepat ke `/admin/players/{uuid}`).
     5. *Administrative Audit History*: Catatan riwayat aksi staf dan riwayat sanksi akun.
   - **Administrator Self-Protection**: Kebijakan proteksi mutlak yang mencegah administrator menghapus akunnya sendiri atau mendemosi/menghapus akun administrator terakhir yang tersisa (mengembalikan respon 403 Forbidden aman).

---

## 3. Custom Plugin Integration

Setiap plugin custom Apexsions terhubung melalui antrean WebBridge:

| Plugin Name | Purpose | Data Available | Safe Actions Available |
| :--- | :--- | :--- | :--- |
| **ApexsionsCore** | Core kingdom, level, rank, RTP, combat tag | Level, XP, Kingdom, Rank, Region | `core.reload`, `core.rtp_reset`, `core.sync_ranks` |
| **ApexsionsChat** | Chat channels, profile hub, moderation desk | Chat channels, player reports, mutes | `chat.clear`, `chat.reload` |
| **ApexsionsEconomy** | Multi-currency (Rupiah/Diamonds), Auction | Balances, ledger transactions, auctions | `eco.reload`, `eco.purge_expired_auctions` |
| **ApexsionsBattlepass** | Quests, passes, rotating shops | Pass tier, active pass, quest stats | `bp.reload`, `bp.season_sync` |
| **ApexsionsShop** | Dynamic markets, sell GUI, kingdom shop | Market prices, taxes, categories | `shop.reload`, `shop.rebalance_markets` |
| **ApexsionsMedia** | Interactive banner, raytrace glow, logo | Media displays, interactive banners | `media.reload` |

---

## 4. Granular Permissions Overview

Sistem hak akses membatasi operasional berdasarkan peran administratif:

| Permission Node | Keterangan |
| :--- | :--- |
| `apexsions.admin` | Hak akses master penuh ke seluruh sistem Apexsions |
| `apexsions.players.view` | Melihat daftar dan profil 360 pemain |
| `apexsions.players.manage` | Melakukan tindakan administratif pada pemain |
| `apexsions.moderation.view` | Melihat laporan dan hukuman |
| `apexsions.moderation.manage` | Menindak laporan dan menerbitkan hukuman |
| `apexsions.economy.view` | Memeriksa transaksi dan data lelang |
| `apexsions.economy.manage` | Melakukan intervensi ekonomi aman |
| `apexsions.server.view` | Memantau telemetri server |
| `apexsions.server.manage` | Mengaktifkan mode pemeliharaan dan aksi server |
| `apexsions.plugins.view` | Memeriksa matriks plugin dan kapabilitas |
| `apexsions.plugins.manage` | Menjalankan aksi plugin terdaftar |
| `apexsions.incidents.view` | Membaca berkas insiden dan korelasi event |
| `apexsions.incidents.manage` | Mengubah status insiden dan mencatat investigasi |
| `apexsions.notifications.view` | Melihat pusat notifikasi |
| `apexsions.notifications.manage` | Melakukan acknowledgement notifikasi |
| `apexsions.automation.view` | Melihat riwayat eksekusi otomasi |
| `apexsions.automation.manage` | Memberikan persetujuan (*approval*) atau penolakan |
| `apexsions.audit.view` | Memeriksa rekam jejak Unified Audit Log |
| `admin.users.read` | Melihat daftar pengguna dan inspeksi User Dossier 5-section |
| `admin.users.update` | Memperbarui peran (role), status verifikasi, dan kata sandi |
| `admin.users.ban` | Menangguhkan (suspend/ban) atau mencabut sanksi pengguna |
| `admin.users.delete` | Menghapus akun pengguna (dilindungi proteksi self-deletion) |

---

## 5. Authentication & Security Architecture

Sistem autentikasi dan keamanan pengguna dibangun di atas prinsip ketat:

### A. Password Visibility UX (Eye Toggle)
- Form **Login**, **Register**, **Password Reset**, dan **Confirm Password** dilengkapi tombol toggle visibilitas kata sandi interaktif (`bi-eye` / `bi-eye-slash`).
- Beroperasi murni pada *client-side DOM* (`type="password"` $\leftrightarrow$ `type="text"`) tanpa merekam, menyimpan plaintext, atau mengirimkan string sandi ke endpoint tambahan.
- Ramah aksesibilitas (lengkap dengan atribut dinamis `aria-label` untuk screen reader) serta sepenuhnya kompatibel dengan pengelola kata sandi peramban (Bitwarden, 1Password, Chrome Autofill).
- Dilengkapi proteksi *Double-Submit Locking* yang menonaktifkan tombol submit dan menampilkan spinner indikator pemrosesan saat formulir dikirim.

### B. Registration & Email Verification Lifecycle
1. **Pendaftaran**: Validasi username unik, email unik, dan panjang sandi minimal 8 karakter.
2. **Status Akun Awal**: Akun dibuat dengan status `email_verified_at = null` (Pending/Unverified).
3. **Penerbitan Tautan Terverifikasi**: Tautan verifikasi bertanda tangan kriptografis (*Signed URL*) dibuat dengan masa kedaluwarsa 60 menit.
4. **Enforcement Middleware**: Middleware `EnsureEmailIsVerified` membatasi akun belum terverifikasi dari rute penting webstore dan portal profil.
5. **Aktivasi Akun**: Akses tautan menandai `email_verified_at = now()`, mengaktifkan akun secara penuh.

### C. Pemisahan Tegas Web Role vs In-Game Minecraft Rank
- **Azuriom Web Role** (`Admin`, `Moderator`, `User`) mengatur hak akses dashboard web dan manajemen portal.
- **Minecraft In-Game Rank** (`Ancestor` [100], `Architect` [95], `Overseer` [95], `Warden` [90], `Herald` [80], `Sions` [70], `Emperor` [60], `Sovereign` [50], `Archon` [40], `Ascendant` [30], `Wanderer` [10]) mengatur hak akses in-game LuckPerms dan peradaban.
- Keduanya dipisahkan secara authoritatif untuk mencegah eskalasi hak istimewa (*Privilege Escalation*).

---

## 6. Deployment Requirements

- **PHP Version:** PHP 8.2 atau 8.3 LTS (dengan ekstensi `pdo_sqlite`, `pdo_mysql`, `curl`, `mbstring`, `xml`, `bcmath`).
- **Web Server:** Nginx dengan PHP-FPM (`php8.2-fpm`).
- **Framework:** Azuriom v1.1+ (Laravel 12).
- **Minecraft Server:** Paper 26.2 (Java 21 LTS) dengan plugin `ApexsionsCore` & `WebBridge`.

### Directory Permissions:
```bash
chown -R www-data:www-data /var/www/azuriom/plugins/apexsions-bridge
chown -R www-data:www-data /var/www/azuriom/themes/apexsions
chown -R www-data:www-data /var/www/azuriom/storage /var/www/azuriom/bootstrap/cache
chmod -R 775 /var/www/azuriom/storage /var/www/azuriom/bootstrap/cache
```

---

## 7. Queue Requirements

Notifikasi Discord Webhook dan tugas background diproses secara asinkron untuk menjaga latensi dashboard tetap di bawah 100ms.

Jalankan worker antrean melalui systemd (`/etc/systemd/system/azuriom-worker.service`):
```ini
[Unit]
Description=Azuriom Queue Worker
After=network.target

[Service]
User=www-data
Group=www-data
Restart=always
ExecStart=/usr/bin/php /var/www/azuriom/artisan queue:work --sleep=3 --tries=3 --max-time=3600

[Install]
WantedBy=multi-user.target
```

---

## 8. Scheduler Requirements

Pastikan cronjob Laravel aktif pada server VPS produksi:
```bash
# Tambahkan ke crontab www-data (crontab -u www-data -e)
* * * * * cd /var/www/azuriom && php artisan schedule:run >> /dev/null 2>&1
```

Jadwal pembersihan otomatis yang dikelola:
- `apexsions:clean-events`: Membersihkan event normal lebih dari 30 hari (Daily).
- `apexsions:clean-notifications`: Membersihkan notifikasi terselesaikan lebih dari 60 hari (Weekly).
- `apexsions:process-notifications`: Memproses retry notifikasi gagal setiap 5 menit.

---

## 9. Troubleshooting

| Gejala | Kemungkinan Penyebab | Tindakan Penyelesaian |
| :--- | :--- | :--- |
| **HTTP 500 / Permission Denied di `/admin/users` atau rute lain** | Perintah CLI (seeder/artisan) dijalankan sebagai `root`, menciptakan folder cache `storage/framework/cache/data/` milik `root:root` (0755) sehingga tidak bisa ditulis oleh PHP-FPM (`www-data`). | Jalankan `chown -R www-data:www-data /var/www/azuriom/storage /var/www/azuriom/bootstrap/cache` dan `chmod -R 775 /var/www/azuriom/storage /var/www/azuriom/bootstrap/cache`. |
| **Aksi Bridge Berstatus PENDING lama** | Minecraft Server offline atau task WebBridge in-game belum fetch antrean | Periksa koneksi Minecraft Server dan pastikan `ApexsionsCore` aktif dan token sinkronisasi cocok di `config.yml`. |
| **Notifikasi Discord Tidak Terkirim** | URL webhook belum dikonfigurasi atau diblokir SSRF filter | Periksa URL webhook di pengaturan; pastikan hanya menggunakan domain resmi `discord.com` atau `discordapp.com`. |
| **Error 403 Saat Eksekusi Aksi** | Staf tidak memiliki permission granular yang dibutuhkan | Berikan role atau permission spesifik pada akun pengguna via panel Admin Roles. |
| **Tampilan CSS/JS Belum Terbarui** | Browser atau Laravel view cache masih menyimpan versi lama | Jalankan `php artisan view:clear` dan `php artisan cache:clear` di root web server. |

---

## 10. Backup Recommendations

Lakukan pencadangan rutin harian:
1. **Database:**
   ```bash
   # Jika menggunakan SQLite:
   sqlite3 /var/www/azuriom/database/database.sqlite ".backup '/var/backups/azuriom/db_$(date +%F).sqlite'"
   # Jika menggunakan MySQL/MariaDB:
   mysqldump -u root azuriom > /var/backups/azuriom/db_$(date +%F).sql
   ```
2. **File Konfigurasi & Storage:**
   ```bash
   tar -czf /var/backups/azuriom/storage_$(date +%F).tar.gz /var/www/azuriom/storage /var/www/azuriom/.env
   ```
