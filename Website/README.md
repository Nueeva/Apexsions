# Apexsions Web Platform

Official web portal, store integration, and player identity bridge for the Apexsions Minecraft Server Network.

Built on **Azuriom (Laravel-based CMS)** according to [`Minecraft_Network_PRD_Technical_Blueprint_v2.2_AUTH_MODEL.md`](./Minecraft_Network_PRD_Technical_Blueprint_v2.2_AUTH_MODEL.md).

---

## Directory Structure

```text
Website/
├── Minecraft_Network_PRD_Technical_Blueprint_v2.2_AUTH_MODEL.md  # Master technical specification
├── README.md                                                     # Architecture & onboarding guide
├── deploy/                                                       # VPS provisioning & tuning (2 GB RAM)
│   ├── setup-vps.sh                                              # Master idempotent deployment script
│   ├── nginx-azuriom.conf                                        # Nginx vhost with Cloudflare real-IP
│   ├── php-fpm-tuning.conf                                       # PHP 8.3 FPM memory & worker tuning
│   └── mariadb-tuning.cnf                                        # MariaDB InnoDB buffer pool tuning
├── plugins/
│   ├── shop/                                                     # Official Azuriom Shop plugin
│   ├── wiki/                                                     # Official Azuriom Wiki plugin (fitur & command)
│   ├── midtrans/                                                 # Midtrans Indonesian payment gateway (Shop)
│   │   ├── plugin.json
│   │   ├── src/Providers/MidtransServiceProvider.php
│   │   ├── src/PaymentMethod/MidtransPaymentMethod.php
│   │   └── resources/views/admin/gateways/midtrans.blade.php
│   └── apexsions-bridge/                                         # Multi-platform Minecraft identity & queue
│       ├── plugin.json
│       ├── database/migrations/                                  # minecraft_accounts & deliveries tables
│       ├── src/Models/                                           # MinecraftAccount & Delivery models
│       ├── src/Controllers/                                      # Web PIN link & REST API verification
│       ├── routes/                                               # web.php & api.php
│       └── resources/views/link.blade.php                        # Player linking dashboard
└── themes/
    └── apexsions/                                                # Custom dark theme with neon accents
        ├── theme.json
        ├── assets/css/style.css                                  # Glassmorphism & responsive styles
        ├── assets/js/app.js                                      # Clipboard copy & live server ping
        └── views/                                                # Home, layouts, navbar, footer
```

---

## Custom Modules Overview

### 1. Midtrans Payment Gateway Scaffolding (`plugins/midtrans`)
- Implements `Azuriom\Plugin\Shop\Payment\PaymentMethod\PaymentMethod`.
- Admin configuration for Client Key, Server Key, Merchant ID, and Sandbox/Production toggle.
- Standardized Snap payload generation and SHA-512 webhook signature verification stub.

### 2. Minecraft Identity, WebBridge & Rank Sync (`plugins/apexsions-bridge`)
- Supports **Java Online**, **Java Offline**, and **Bedrock Floodgate** identity classes (§16 & §70).
- Users request a 6-digit PIN on the web portal (valid for 5 minutes).
- In-game command `/link <code>` calls `/api/apexsions-bridge/verify` to securely link UUID and username.
- **11-Tier Official Rank Synchronization**: Bidirectional rank syncing from in-game LuckPerms to Azuriom Web roles (`Ancestor` [100], `Architect` [95], `Overseer` [95], `Warden` [90], `Herald` [80], `Sions` [70], `Emperor` [60], `Sovereign` [50], `Archon` [40], `Ascendant` [30], `Wanderer` [10]) with official branding colors and power levels.
- **Decoupled Asynchronous Deliveries**: The `deliveries` table enables queued command dispatching via Console, safely rewarding players whether they are currently online or offline (`/eco give/take/set`, `/ac addxp`, `/ac setlevel`).

### 3. Apexsions Brand Theme (`themes/apexsions`)
- Modern dark aesthetic tailored for the Apexsions ecosystem with glassmorphism cards and noble gold accents.
- Live server status badge and one-click IP copy widget (`apexsions.my.id:32348`).
- **Webstore Storefront & Multi-Axis Filtering Engine**:
  - **Dual-Axis Dynamic Filter**: Saring 15 varian paket kasta berdasarkan Kategori Kasta (`Semua Kasta`, `Ascendant`, `Archon`, `Sovereign`, `Emperor`, `✦ SIONS ✦`) dan Durasi (`Semua Durasi`, `Permanen`, `Trial 90 Hari`, `Trial 30 Hari`) secara bersamaan tanpa reload halaman, lengkap dengan efek kilau (*glow*) khas peradaban pada tombol aktif.
  - **Subkategori Spesifik**: Filter instan untuk `Pundi Koin & Booster` (`Apex Coins 💎`, `Booster Server ⚡`) dan `Battlepass` (`Sio Pass 🏆`, `Exsio Pass 👑`).
  - **Pills Navigasi Sinkron**: Bar navigasi atas (`.apx-store-nav-bar`) mengintegrasikan tombol *Beranda Toko* dan seluruh kategori dengan badge jumlah paket aktif.
  - **Empty-State Cerdas**: Fallback notifikasi elegan dengan tombol *"Reset Semua Filter"* jika kombinasi filter tidak menghasilkan paket.
  - **Script Stack Lifecycle**: Integrasi ganda `@push('scripts')` dan `@stack('footer-scripts')` pada `layouts/app.blade.php`, menjamin event listener modal dan filter selalu terpasang sempurna.
- **Webstore WhatsApp Checkout (2 Founders)**: Automated direct order via WhatsApp (`wa.me`) supporting 2 Founders (`Rifqi`, `Friell`) with auto-filled order templates (package name, price, category, player IGN, and email) selagi gateway Midtrans dalam proses pengajuan.
- **Fandom Gamepedia Wiki Portal**: Ensiklopedia peradaban interaktif dengan Fandom Infoboxes (3 Kerajaan, 11 Kasta, 28 Custom Enchants & Set Bonuses), tabel perbandingan responsif, dan tombol one-click copy command.
- Direct integration with official Azuriom Wiki and Shop plugins.

### 4. User Management Center & Authentication Hardening (`resources/views/admin/users/` & `auth/`)
- **Executive KPI Metrics**: Live dashboard cards displaying total registered users, verified accounts, unverified/pending accounts, 2FA enabled users, banned accounts, and system administrators.
- **Search & Filtering Engine**: Instant query builder for usernames, emails, account verification states, web roles, and multi-field sorting.
- **Minecraft Player Dossier Linkage**: Integrates with `minecraft_accounts` to show linked IGN, in-game rank, copyable UUID, and direct jump to the player's 360 profile.
- **5-Section User Dossier**: Structured administrative layout covering Identity, Roles, Security/Credentials, Minecraft Link, and Audit History.
- **Administrator Self-Protection**: Enforced backend logic in `UserController` preventing administrators from deleting themselves or demoting/deleting the last active administrator.
- **Client-side Password Visibility UX**: Accessible eye toggle (`bi-eye` / `bi-eye-slash`) on Login, Register, Reset, and Confirm Password forms without storing or transmitting plaintext passwords.
- **Strict Role Decoupling**: Complete authoritative separation between Azuriom Web Roles (`Admin`, `Moderator`, `User`) and in-game Minecraft LuckPerms Ranks (`Ancestor` through `Wanderer`).

---

## VPS Deployment (Ubuntu 24.04 LTS / 2 GB RAM)

To provision and tune the production VPS:

```bash
cd /var/www/apexsions/Website/deploy
sudo bash setup-vps.sh
```

The script automatically:
1. Allocates a **2 GB Swap safety net** to avoid OOM crashes during traffic surges.
2. Installs **Nginx**, **MariaDB**, and **PHP 8.3-FPM** with required extensions.
3. Tunes PHP-FPM (`pm = dynamic`, `pm.max_children = 10`) and MariaDB (`innodb_buffer_pool_size = 256M`).
4. Configures UFW firewall (22/SSH, 80/HTTP, 443/HTTPS) and Fail2ban.
5. Restores original visitor IPs when proxied through Cloudflare.
