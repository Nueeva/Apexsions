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

### 2. Minecraft Identity & Account Linking (`plugins/apexsions-bridge`)
- Supports **Java Online**, **Java Offline**, and **Bedrock Floodgate** identity classes (§16 & §70).
- Users request a 6-digit PIN on the web portal (valid for 5 minutes).
- In-game command `/link <code>` calls `/api/apexsions-bridge/verify` to securely link UUID and username.
- Decoupled `deliveries` table enables asynchronous command execution when players are online.

### 3. Apexsions Brand Theme (`themes/apexsions`)
- Modern dark aesthetic tailored for the Apexsions ecosystem with glassmorphism cards.
- Live server status badge and one-click IP copy widget (`play.apexsions.net`).
- Direct integration with official Azuriom Wiki and Shop plugins.

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
