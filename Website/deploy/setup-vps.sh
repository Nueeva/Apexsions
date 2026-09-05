#!/usr/bin/env bash
# ==============================================================================
# Apexsions Web Platform — Automated VPS Provisioning & 2 GB RAM Tuning
# Platform: Ubuntu 24.04 LTS (Noble Numbat)
# Bandwidth Optimization: Lean package installation, minimal overhead
# ==============================================================================

set -euo pipefail

echo "=========================================================="
echo " Starting Apexsions Web Platform Provisioning & Tuning    "
echo " Target: Ubuntu 24.04 LTS (2 GB RAM, 25 GB NVMe)          "
echo "=========================================================="

if [[ $EUID -ne 0 ]]; then
   echo "[ERROR] This script must be run as root." 
   exit 1
fi

# ------------------------------------------------------------------------------
# 1. 2 GB Swap Safety Net Configuration (§11.1)
# ------------------------------------------------------------------------------
echo "[STEP 1/7] Configuring 2 GB Swap file safety net..."
if [ ! -f /swapfile ]; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    echo "vm.swappiness=10" >> /etc/sysctl.d/99-swap.conf
    sysctl -p /etc/sysctl.d/99-swap.conf
    echo "[OK] 2 GB Swap created and activated."
else
    echo "[OK] Swap file already exists, skipping creation."
fi

# ------------------------------------------------------------------------------
# 2. Lean Package Installation (Bandwidth-Conscious)
# ------------------------------------------------------------------------------
echo "[STEP 2/7] Installing core web stack packages (Nginx, MariaDB, PHP 8.3)..."
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y --no-install-recommends \
    nginx \
    mariadb-server \
    php8.3 \
    php8.3-fpm \
    php8.3-bcmath \
    php8.3-curl \
    php8.3-gd \
    php8.3-mbstring \
    php8.3-mysql \
    php8.3-xml \
    php8.3-zip \
    php8.3-intl \
    php8.3-opcache \
    unzip \
    git \
    curl \
    ufw \
    fail2ban

# ------------------------------------------------------------------------------
# 3. Apply Resource Tuning for 2 GB RAM (§11.1)
# ------------------------------------------------------------------------------
echo "[STEP 3/7] Applying PHP-FPM and MariaDB resource tuning..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# PHP-FPM Tuning
if [ -f "$SCRIPT_DIR/php-fpm-tuning.conf" ]; then
    cp "$SCRIPT_DIR/php-fpm-tuning.conf" /etc/php/8.3/fpm/pool.d/z-apexsions.conf
fi
systemctl restart php8.3-fpm
systemctl enable php8.3-fpm

# MariaDB Tuning
if [ -f "$SCRIPT_DIR/mariadb-tuning.cnf" ]; then
    cp "$SCRIPT_DIR/mariadb-tuning.cnf" /etc/mysql/mariadb.conf.d/99-apexsions.cnf
fi
systemctl restart mariadb
systemctl enable mariadb

# ------------------------------------------------------------------------------
# 4. Configure Nginx Virtual Host & Cloudflare Real IP (§9 & §10)
# ------------------------------------------------------------------------------
echo "[STEP 4/7] Configuring Nginx virtual host..."
if [ -f "$SCRIPT_DIR/nginx-azuriom.conf" ]; then
    cp "$SCRIPT_DIR/nginx-azuriom.conf" /etc/nginx/sites-available/azuriom
    ln -sf /etc/nginx/sites-available/azuriom /etc/nginx/sites-enabled/azuriom
    rm -f /etc/nginx/sites-enabled/default
    nginx -t
    systemctl reload nginx
    systemctl enable nginx
fi

# ------------------------------------------------------------------------------
# 5. Security & Firewall Configuration (UFW + Fail2ban) (§10)
# ------------------------------------------------------------------------------
echo "[STEP 5/7] Hardening security with UFW firewall and Fail2ban..."
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp comment 'SSH'
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'
ufw --force enable
systemctl restart fail2ban
systemctl enable fail2ban

# ------------------------------------------------------------------------------
# 6. Database Provisioning for Azuriom
# ------------------------------------------------------------------------------
echo "[STEP 6/7] Initializing MariaDB database for Azuriom..."
DB_PASS=$(openssl rand -base64 18 | tr -dc 'a-zA-Z0-9' | head -c 16)

mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS azuriom CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'azuriom_user'@'localhost' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON azuriom.* TO 'azuriom_user'@'localhost';
FLUSH PRIVILEGES;
EOF

echo "[OK] MariaDB database 'azuriom' created."
echo "     User: azuriom_user"
echo "     Password: ${DB_PASS}"

# ------------------------------------------------------------------------------
# 7. Setup Web Application, .env, and Database Migrations
# ------------------------------------------------------------------------------
echo "[STEP 7/7] Deploying Azuriom web application to /var/www/azuriom..."
mkdir -p /var/www/azuriom

ARCHIVE_PATH=""
if [ -f "/tmp/apexsions-web.tar.gz" ]; then
    ARCHIVE_PATH="/tmp/apexsions-web.tar.gz"
elif [ -f "/root/apexsions-web.tar.gz" ]; then
    ARCHIVE_PATH="/root/apexsions-web.tar.gz"
fi

if [ -n "$ARCHIVE_PATH" ]; then
    echo "Extracting application archive from $ARCHIVE_PATH..."
    tar -xzf "$ARCHIVE_PATH" -C /var/www/azuriom/
fi

# Configure .env
if [ ! -f /var/www/azuriom/.env ]; then
    echo "Generating /var/www/azuriom/.env configuration..."
    cat << ENV_EOF > /var/www/azuriom/.env
APP_NAME="Apexsions"
APP_ENV=production
APP_KEY=
APP_DEBUG=false
APP_URL=http://web.apexsions.my.id

LOG_CHANNEL=stack
LOG_DEPRECATIONS_CHANNEL=null
LOG_LEVEL=info

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=azuriom
DB_USERNAME=azuriom_user
DB_PASSWORD=${DB_PASS}

BROADCAST_DRIVER=log
CACHE_DRIVER=file
FILESYSTEM_DISK=local
QUEUE_CONNECTION=database
SESSION_DRIVER=file
SESSION_LIFETIME=120
ENV_EOF
fi

if [ -f /var/www/azuriom/artisan ]; then
    cd /var/www/azuriom
    echo "Generating application key..."
    php artisan key:generate --force || true

    echo "Running database migrations..."
    php artisan migrate --force || true

    echo "Seeding default roles, settings, and permissions..."
    php artisan db:seed --force || true

    echo "Creating storage symlink..."
    php artisan storage:link || true

    echo "Optimizing route and config caches..."
    php artisan config:clear || true
fi

# Ensure storage directories exist
mkdir -p /var/www/azuriom/storage/framework/cache/data
mkdir -p /var/www/azuriom/storage/framework/sessions
mkdir -p /var/www/azuriom/storage/framework/views
mkdir -p /var/www/azuriom/storage/logs
mkdir -p /var/www/azuriom/bootstrap/cache

chown -R www-data:www-data /var/www/azuriom
chmod -R 775 /var/www/azuriom/storage /var/www/azuriom/bootstrap/cache

systemctl restart php8.3-fpm
systemctl reload nginx

echo "=========================================================="
echo " Provisioning & Deployment Completed Successfully!        "
echo " Database Name : azuriom                                  "
echo " Database User : azuriom_user                             "
echo " Database Pass : ${DB_PASS}                               "
echo " Web Domains   : http://web.apexsions.my.id               "
echo "                 http://apexsions.my.id                   "
echo " Direct IP URL : http://89.144.53.100                     "
echo "=========================================================="

