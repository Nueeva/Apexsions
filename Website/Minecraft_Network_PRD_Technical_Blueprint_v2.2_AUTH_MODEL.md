# Minecraft Network Web Platform
## Product Requirements Document (PRD) + Technical Blueprint

**Version:** 2.2 (Implementation-Ready — Auth Model & Bedrock Support)  
**Status:** Implementation-Ready — Authentication Architecture Updated  
**Target Market:** Indonesia (primary launch) & International (later phase)  
**Web Infrastructure:** ECO Memory-Optimized 2 GB VPS (1 vCore, 2 GB RAM, 25 GB NVMe, 10 Gbps, 1 public IPv4 + IPv6/64), Frankfurt, Germany  
**Minecraft Infrastructure:** Existing Minecraft server on separate Pterodactyl hosting  
**CMS Decision:** **Azuriom — CONFIRMED** (validated, see §6.4)  
**Payment Decision:** **Midtrans — CONFIRMED as sole provider for this build phase** (scaffolding only, live API wiring deferred — see §22)

---

## Changelog v2.0 (Final — Technology Validation & Scope Lock)

1. **Azuriom resmi dipilih** (bukan lagi kandidat) setelah validasi teknis langsung terhadap dokumentasi & source resmi Azuriom. Lihat §6.4 untuk detail temuan.
2. **Payment gateway dipersempit ke Midtrans saja** untuk fase build ini. PayPal/Stripe/Binance tetap ada di abstraction layer tapi implementasinya ditunda ke fase berikutnya (lihat §22, §23).
3. **Midtrans di fase ini = scaffolding only.** Class `PaymentMethod` custom untuk Midtrans dibuat strukturnya (id, name, form konfigurasi di admin), tapi pemanggilan API Midtrans yang sesungguhnya (Snap token, webhook signature check, dsb) **belum diimplementasikan** — ini pekerjaan fase selanjutnya setelah kredensial merchant Midtrans siap. Lihat §4 dan §22.
4. **Fitur baru: Wiki (fitur & command server)** — pakai plugin resmi Azuriom Wiki, bukan bikin dari nol. Lihat §15.3.
5. **Catatan untuk AI agent**: agent yang mengerjakan proyek ini terintegrasi langsung dengan Minecraft server yang sudah berjalan (tahu plugin dan cara kerja server sebenarnya). Ini mengubah cara agent seharusnya bekerja — lihat rule baru di §67 ("Live Server Context Rule").

---

## Changelog v1.2 (Review & Adjustment)

Infrastruktur sudah dikonfirmasi menggunakan **public IPv4** (bukan NAT), jadi seluruh asumsi arsitektur di dokumen ini (port 80/443 langsung, Cloudflare sebagai proxy opsional-tapi-direkomendasikan, bukan workaround wajib) sudah konsisten dan tidak perlu diubah dari sisi jaringan.

Review ini menemukan dan memperbaiki beberapa **logic gap**:

1. **Multi-currency pricing** — schema produk lama hanya punya satu `price` + `currency`, padahal target pasar butuh IDR & USD sekaligus. Diperbaiki dengan tabel `product_prices` (lihat §18, §35).
2. **Granularitas delivery** — `deliveries` lama mengikat 1 baris per `order_id`, padahal 1 order bisa berisi beberapa `order_item` yang tujuannya berbeda server. Diperbaiki agar delivery dibuat per `order_item` (lihat §31, §33, §35).
3. **Keamanan verifikasi link akun Minecraft** — flow lama tidak menyebutkan expiry, single-use, atau rate limit pada kode verifikasi. Ditambahkan (lihat §17).
4. **Kepemilikan status online/offline player** — versi lama menaruh logic "cek player online" di sisi Website, padahal itu melanggar prinsip pemisahan tanggung jawab di §70 sendiri ("Minecraft owns player state"). Diperbaiki agar Website hanya mengirim delivery job, dan keputusan online/offline + queue-on-join jadi tanggung jawab plugin (lihat §32).
5. **Order expiry** — status `EXPIRED` sudah didefinisikan tapi tidak ada field/mechanism pemicunya. Ditambahkan `expires_at` + expiry job (lihat §19, §35).
6. **Webhook vs CSRF** — perlu penegasan eksplisit bahwa endpoint webhook payment harus dikecualikan dari CSRF middleware (karena request datang dari server provider, bukan browser) namun tetap wajib signature-verified (lihat §25).
7. **Cloudflare SSL mode** — ditambahkan requirement eksplisit "Full (Strict)", bukan "Flexible", supaya koneksi Cloudflare↔origin tetap terenkripsi (lihat §9).
8. **Fraud/carding risk** — webstore digital goods adalah target umum untuk card testing (carding). Ditambahkan mitigasi dasar (lihat §44a).
9. **Resource tuning untuk 2 GB RAM** — ditambahkan rekomendasi swap file & tuning PHP-FPM karena RAM lebih kecil dari estimasi awal (lihat §11).

---

# 1. Executive Summary

Proyek ini bertujuan membangun website resmi untuk Minecraft server yang berfungsi sebagai:

- official website;
- player account platform;
- Minecraft account linking;
- online store/e-commerce;
- payment platform;
- automatic Minecraft reward delivery;
- server status/information;
- community/news platform;
- administrative dashboard.

Minecraft server utama **sudah tersedia dan berjalan pada hosting terpisah menggunakan Pterodactyl**. Website tidak menggantikan Minecraft server tersebut.

Website akan berjalan pada VPS terpisah dengan public IPv4.

Arsitektur awal:

```text
PLAYER
   |
   v
CLOUDFLARE
   |
   v
WEB VPS
   |
   +---- APPLICATION
   |
   +---- DATABASE
   |
   +---- PAYMENT SERVICE
   |
   +---- MINECRAFT INTEGRATION
                    |
                    v
            EXISTING MINECRAFT SERVER
                    |
                    v
               PTERODACTYL
```

---

# 2. Product Vision

Membangun platform Minecraft yang dapat berkembang dari website sederhana menjadi Minecraft network platform tanpa melakukan rewrite total.

Tahap awal:

```text
1 Website
1 Minecraft Server
1 Database
1+ Payment Provider
```

Target arsitektur jangka panjang:

```text
1 Website
Multiple Minecraft Servers
Multiple Payment Providers
Multiple Countries
Multiple Currencies
Multiple Product Types
```

Prinsip utama:

> Build simple first, but design clear boundaries so the system can scale later.

---

# 3. Goals

## 3.1 Primary Goals

Website harus:

1. Menjadi official website Minecraft server.
2. Menyediakan store untuk produk virtual Minecraft.
3. Mendukung account system.
4. Mendukung Minecraft account linking.
5. Mendukung pembayaran Indonesia.
6. Mendukung pembayaran internasional.
7. Memproses webhook payment dengan aman.
8. Mengirim reward Minecraft secara otomatis.
9. Menyimpan riwayat transaksi.
10. Menyediakan admin dashboard.
11. Memiliki audit dan delivery log.
12. Aman dan maintainable.
13. Efisien pada VPS 2 GB RAM.

## 3.2 Business Goals

Platform harus memungkinkan monetisasi melalui:

- rank;
- membership;
- cosmetic;
- crate/key;
- item;
- currency;
- bundle;
- subscription jika diperlukan;
- produk digital lainnya.

---

# 4. Non-Goals untuk MVP

Fitur berikut tidak menjadi prioritas MVP:

- mobile application;
- social network;
- forum kompleks;
- live chat;
- referral system;
- affiliate system;
- loyalty point kompleks;
- marketplace antar-player;
- advanced analytics;
- microservices architecture;
- multi-region deployment;
- Kubernetes;
- **implementasi live Midtrans API** (Snap token, webhook signature verification, settlement) — struktur/abstraction dan konfigurasi disiapkan, tapi koneksi ke API sesungguhnya menyusul di iterasi berikutnya setelah kredensial merchant siap (v2.0);
- **payment provider internasional (PayPal/Stripe/Binance) live** — abstraction layer dirancang agar bisa ditambahkan tanpa rework, tapi tidak diimplementasikan di fase ini (v2.0).

Fitur tersebut dapat dipertimbangkan setelah MVP stabil.

---

# 5. Target Users

## 5.1 Guest

Guest dapat:

- membuka website;
- melihat informasi server;
- melihat status server;
- melihat store;
- melihat produk;
- membaca berita;
- melakukan registrasi.

## 5.2 Player

Player dapat:

- login;
- menghubungkan Minecraft account;
- membeli produk;
- melakukan pembayaran;
- melihat order;
- melihat riwayat pembelian;
- melihat status delivery.

## 5.3 Staff

Staff dapat diberikan permission tertentu:

- melihat user;
- melihat order;
- customer support;
- melihat payment;
- melihat delivery.

## 5.4 Administrator

Admin dapat:

- mengelola produk;
- mengelola user;
- mengelola order;
- melihat payment;
- mengelola delivery;
- mengelola server;
- mengelola konfigurasi;
- melihat audit log.

---

# 6. Technology Strategy

Sebelum membuat custom application dari nol, lakukan evaluation terhadap:

1. Azuriom
2. NamelessMC
3. Custom Application

## 6.1 Azuriom — CONFIRMED

Azuriom adalah basis platform yang dipilih untuk proyek ini. Implementasi menggunakan Azuriom core + official plugins + custom plugin/theme/integration jika diperlukan.

- core functionality;
- plugin;
- theme;
- custom integration.

Keuntungan pendekatan ini:

```text
Azuriom
   |
   +---- Existing Features
   +---- Plugins
   +---- Theme
   +---- Custom Plugin
   +---- Minecraft Integration
```

## 6.2 NamelessMC — REJECTED FOR THIS PROJECT

NamelessMC tidak dipilih sebagai basis utama karena kebutuhan proyek lebih berat pada e-commerce, payment integration, Minecraft delivery, dan extensibility melalui plugin.

## 6.3 Custom Application — REJECTED FOR THIS PROJECT

Custom application dari nol tidak dipilih untuk MVP. Custom code hanya dibuat sebagai plugin/theme/integration di atas Azuriom jika fitur existing tidak mencukupi.

### Decision Rule

Jangan membuat ulang fitur yang sudah tersedia dan stabil.

Custom development harus dibenarkan oleh gap teknis atau kebutuhan bisnis yang tidak dapat dipenuhi secara wajar oleh Azuriom.

## 6.4 Validation Result — Azuriom CONFIRMED (v2.0)

Evaluasi teknis terhadap dokumentasi resmi Azuriom (azuriom.com/docs, github.com/Azuriom, market.azuriom.com) mengonfirmasi Azuriom **memenuhi seluruh kebutuhan arsitektur di PRD ini**:

| Kebutuhan | Temuan |
|---|---|
| Open source & bisa dimodifikasi | Azuriom adalah "complete open source web solution for game servers", berbasis **Laravel** (PHP). Plugin bisa dibuat via `php artisan plugin:create`, dengan struktur route (`web.php`, `api.php`, `admin.php`), view Blade, dan model Eloquent sendiri — sama seperti aplikasi Laravel biasa. |
| Store/Shop | Plugin resmi **Shop** tersedia (dipakai 1.800+ server, 2 juta+ user), mendukung kategori produk, cart, checkout, coupon, subscription. |
| Custom payment gateway (untuk Midtrans) | Shop plugin **tidak** menyediakan Midtrans secara native (bawaan: PayPal, PayPal Checkout, Mollie, Xsolla, Paysafecard, Stripe, PaymentWall + plugin tambahan Dedipass/Cryptomus/dll), **tapi** dokumentasi resminya secara eksplisit menyediakan jalur extend: buat class custom yang extend `Azuriom\Plugin\Shop\Payment\PaymentMethod\PaymentMethod`, implementasikan `startPayment()`, dan daftarkan sebagai plugin baru. Ini persis pola yang dibutuhkan untuk custom Midtrans gateway. |
| Wiki fitur & command | Plugin resmi **Wiki** tersedia ("A complete wiki for your website"), tinggal install dari market/admin panel — tidak perlu dibuat dari nol. |
| Integrasi server Minecraft (status, kirim command) | Tersedia konsep **Server Bridge** yang built-in: instance terhubung ke `Azuriom\Models\Server`, dipakai untuk ambil jumlah player online dan mengirim command ke game server. |
| Cloudflare | Ada plugin resmi **Cloudflare Support** — rewrite IP visitor asli & dukungan Flexible/Full SSL saat website di belakang Cloudflare. |

**Keputusan final:** Azuriom dipakai sebagai basis platform. NamelessMC tidak dipilih (fokus lebih ke forum/community, bukan e-commerce). Custom application dari nol tidak dibutuhkan — modifikasi dilakukan lewat plugin custom di atas Azuriom (mengikuti struktur plugin resminya), bukan rewrite core.

---

# 7. Infrastructure

## 7.1 Web VPS

VPS yang digunakan:

### ECO Memory-Optimized 2 GB

- 1 vCore AMD EPYC / Intel Xeon
- 2 GB DDR4 ECC RAM
- 25 GB NVMe SSD
- 2500 GB bandwidth
- 10 Gbps network
- 1 public IPv4
- IPv6 /64
- 1 backup slot
- Frankfurt, Germany

Harga berdasarkan informasi yang diberikan:

- US$2.15/month
- US$21.50/year

## 7.2 Minecraft Server

Minecraft server sudah berjalan pada hosting terpisah menggunakan Pterodactyl.

Website tidak memindahkan Minecraft server ke VPS web.

---

# 8. Infrastructure Architecture

```text
                         INTERNET
                            |
                            v
                    +---------------+
                    |  CLOUDFLARE   |
                    | DNS / WAF /   |
                    | CDN / TLS     |
                    +-------+-------+
                            |
                            v
              +--------------------------+
              |       WEB VPS             |
              |      Frankfurt             |
              |                            |
              |  Nginx / Web Server       |
              |          |                |
              |          v                |
              |    Application           |
              |          |                |
              |    +-----+-----+          |
              |    |           |          |
              |    v           v          |
              | Database   Payment        |
              |            Integration    |
              |                |           |
              |                v           |
              |       Minecraft Service   |
              +---------------+------------+
                              |
                              | HTTPS / API
                              v
                    +-------------------+
                    | Minecraft Hosting |
                    |   Pterodactyl     |
                    +---------+---------+
                              |
                              v
                       Minecraft Server
```

---

# 9. Cloudflare

Cloudflare digunakan sebagai edge/security layer.

Fungsi:

- DNS;
- CDN;
- WAF;
- DDoS protection;
- TLS/HTTPS;
- reverse proxy;
- caching untuk static assets.

Cloudflare bukan dependency bisnis aplikasi.

Website harus tetap dapat berfungsi jika suatu hari traffic tidak lagi melalui Cloudflare.

### 9.1 SSL Mode (v1.2)

Gunakan mode **Full (Strict)**, bukan Flexible dan bukan Full biasa.

```text
Flexible  -> Cloudflare<->Origin TIDAK terenkripsi (rawan)
Full      -> terenkripsi, tapi terima sertifikat self-signed
Full (Strict) -> terenkripsi + validasi sertifikat origin (direkomendasikan)
```

Origin server tetap wajib pasang sertifikat TLS valid (Let's Encrypt/Origin CA Cloudflare), bukan hanya mengandalkan sertifikat di edge Cloudflare.

---

# 10. Networking Requirements

Public endpoints:

```text
80  HTTP
443 HTTPS
```

HTTP harus redirect ke HTTPS.

SSH harus diamankan menggunakan:

- SSH key;
- firewall;
- rate limiting / fail2ban jika diperlukan;
- pembatasan akses apabila memungkinkan.

Database tidak boleh terbuka ke internet.

```text
Internet
   X
   |
Database
```

Application mengakses database melalui private/local interface.

---

# 11. Resource Constraints

VPS hanya mempunyai:

```text
1 vCore
2 GB RAM
25 GB NVMe
```

Karena itu MVP harus resource-efficient.

Hindari service yang tidak diperlukan.

Contoh stack minimal:

```text
Nginx
 |
Application Runtime
 |
Database
```

Redis/queue worker hanya ditambahkan apabila kebutuhan aktual membenarkannya.

Jangan melakukan premature infrastructure engineering.

## 11.1 Tuning untuk 2 GB RAM (v1.2)

Dengan RAM hanya 2 GB, alokasi tanpa tuning berisiko OOM saat traffic naik bersamaan (misal saat promo/event). Rekomendasi minimum sebelum production:

- **Swap file** minimal 1–2 GB sebagai safety net (bukan pengganti RAM, tapi mencegah proses di-kill paksa oleh OOM killer saat lonjakan singkat).
- **PHP-FPM**: gunakan mode `pm = ondemand` atau `dynamic` dengan `pm.max_children` dihitung dari RAM tersedia (jangan pakai default yang mengasumsikan RAM besar).
- **OPcache** wajib aktif untuk mengurangi beban CPU/parsing PHP berulang.
- **MySQL/MariaDB**: kecilkan `innodb_buffer_pool_size` sesuai RAM tersisa setelah PHP-FPM dan Nginx (jangan pakai default yang bisa terlalu besar untuk 2 GB RAM).
- Jalankan queue worker sebagai proses tunggal (bukan banyak worker paralel) di awal, naikkan hanya jika delivery queue benar-benar menumpuk.

---

# 12. Performance Requirements

Aplikasi harus:

- menggunakan database query yang efisien;
- menggunakan pagination;
- melakukan caching bila diperlukan;
- mengoptimalkan asset;
- mengompres response;
- menghindari query N+1;
- menghindari proses berat pada HTTP request;
- menggunakan background job untuk pekerjaan yang tidak harus synchronous.

Static asset dapat di-cache melalui Cloudflare.

---

# 13. Storage Strategy

25 GB NVMe digunakan untuk:

- application;
- database;
- logs;
- temporary files;
- configuration;
- required assets.

Backup jangka panjang tidak boleh bergantung pada storage VPS yang sama.

Backup harus memiliki retention policy.

---

# 14. Backup Strategy

Minimal backup:

```text
Database
Application files
Configuration
```

Backup harus:

- otomatis;
- memiliki retention;
- dapat direstore;
- diuji secara berkala.

Backup yang belum pernah diuji restore tidak boleh dianggap reliable.

---

# 15. Core Features

## 15.1 Homepage

Homepage menampilkan:

- server name;
- server description;
- server status;
- player count;
- server address;
- navigation;
- store CTA;
- news/update;
- leaderboard jika tersedia.

## 15.2 Server Status

Status harus berasal dari sumber aktual.

Contoh:

```text
ONLINE
Players: 127/500
Version: ...
```

Jika Minecraft server offline:

```text
OFFLINE
```

Website tetap harus dapat diakses.

## 15.3 Wiki (Fitur & Command Server) (v2.0 — new)

Ini adalah kebutuhan yang eksplisit diminta product owner: pemain harus bisa membaca dokumentasi fitur dan command server langsung dari website.

**Implementasi:** gunakan plugin resmi **Azuriom Wiki** (lihat §6.4), bukan modul custom baru — install dari admin panel, jangan bangun dari nol.

Struktur konten minimal:

```text
Wiki
 |
 +---- Getting Started (cara join, cara link akun)
 +---- Ranks & Perks (daftar rank, benefit tiap rank)
 +---- Commands (daftar command per plugin, contoh penggunaan)
 +---- Server Rules
 +---- FAQ
```

Aturan pengisian konten (lihat juga "Live Server Context Rule" di §67):

- Daftar command **wajib** berasal dari plugin yang benar-benar terpasang di server saat ini (LuckPerms, Essentials, dsb) — bukan daftar command generik/template.
- Setiap entri command minimal berisi: nama command, syntax, permission yang dibutuhkan (jika ada), contoh penggunaan singkat.
- Konten wiki dikelola dari admin dashboard (role `STAFF`/`ADMIN`), bukan hardcode di kode aplikasi — supaya bisa diupdate tanpa deploy ulang.
- Wiki harus tetap bisa diakses meski Minecraft server sedang offline (sama seperti prinsip di §15.2).

---

# 16. Minecraft Identity & Authentication Model (v2.2)

Website account identity and Minecraft identity are separate concepts.

The platform must explicitly support these Minecraft identity classes:

```text
JAVA_ONLINE
JAVA_OFFLINE
BEDROCK_FLOODGATE
```

Where:

- `JAVA_ONLINE` = Java Edition player authenticated through Mojang/Microsoft online-mode identity.
- `JAVA_OFFLINE` = Java Edition player on an offline-mode server. The website must not assume that an offline username alone proves ownership.
- `BEDROCK_FLOODGATE` = Bedrock player entering through Geyser/Floodgate. Floodgate is specifically designed to allow Bedrock accounts to join Java servers without requiring a paid Java Edition account. citeturn745244search1turn745244search13

## 16.1 Authentication Architecture Decision

**Recommended:** build a small custom Minecraft authentication/linking plugin that integrates directly with the existing server, Geyser/Floodgate (if installed), and the website API.

**AuthMeReloaded:** may be evaluated for Java offline-player session authentication if the live server currently depends on it, but it must not become the website's source of truth for account linking.

Reason:

- website account linking needs a stable identity mapping;
- Bedrock/Floodgate changes the login path;
- Geyser documentation warns that offline-mode authenticator plugins can cause issues and states that Geyser does not support offline-mode usage with those authenticators. citeturn745244search5turn745244search9
- a custom integration plugin can expose one consistent verification/linking protocol to the website.

Final choice must be validated against the actual live server plugin stack before implementation.

## 16.2 Identity Data Model

`minecraft_accounts` should be expanded:

```text
minecraft_accounts
- id
- user_id
- edition
- auth_mode
- minecraft_uuid
- minecraft_username
- floodgate_uuid
- verified_at
- last_seen_at
- created_at
- updated_at
```

Recommended values:

```text
edition:
JAVA
BEDROCK

auth_mode:
ONLINE
OFFLINE
FLOODGATE
```

Rules:

- never use username as the sole permanent identifier;
- use the strongest stable identifier available for the actual server configuration;
- for Floodgate players, preserve the Floodgate identity separately from any Java identity;
- one active Minecraft identity must not be linked to multiple website users;
- username changes must not silently create a second identity if a stable UUID exists.

## 16.3 Java Online Accounts

For online-mode Java players, the website should store the verified Minecraft UUID returned/observed by the integration layer.

The website should not rely on a user-typed username for ownership proof.

## 16.4 Java Offline / "Cracked" Accounts

The platform may support Java offline-mode players because the existing server may operate in offline mode.

However:

```text
username != proof of ownership
```

Therefore the account-link process must require an in-game verification action.

Example:

```text
WEBSITE
   |
   | Generate one-time link code
   v
PLAYER
   |
   | Execute link command in Minecraft
   v
CUSTOM AUTH/LINK PLUGIN
   |
   | Verify code + current player identity
   v
WEBSITE API
   |
   v
Minecraft identity linked
```

The website must not treat registration of a username alone as sufficient proof.

## 16.5 Bedrock Accounts

Bedrock players should be identified through the Geyser/Floodgate integration when available.

The custom plugin should detect and preserve the actual Floodgate identity instead of forcing the Bedrock player into the same assumptions as an offline Java player.

Geyser/Floodgate documentation states that Floodgate allows Bedrock accounts to join Java servers without requiring a paid Java Edition account and exposes Bedrock-specific identity/integration capabilities. citeturn745244search1turn745244search13

## 16.6 Cross-Platform Linking Policy

Do not automatically merge:

```text
Java Offline
Java Online
Bedrock
```

just because the usernames are equal.

Any cross-platform account merge must be an explicit verified action.

Example:

```text
WEBSITE ACCOUNT
   |
   +---- Java identity
   |
   +---- Bedrock/Floodgate identity
```

Whether one website account may own multiple Minecraft identities is a product decision, but the database should be designed to support it safely.

## 16.7 Player State Ownership

Minecraft continues to own:

- login state;
- online/offline status;
- session identity;
- permission state;
- gameplay state.

The website owns:

- website account;
- verified link relationships;
- purchases;
- payment records;
- delivery jobs.

The integration plugin is the trust boundary between the two.

---

# 16. Authentication

User dapat:

- register;
- login;
- logout;
- reset password;
- update profile;
- melihat order history.

Security:

- password hashing;
- secure session;
- rate limiting;
- CSRF protection;
- secure cookies;
- authorization.

---

# 17. Minecraft Account Linking

Username saja tidak boleh menjadi bukti kepemilikan Minecraft account.

Recommended flow:

```text
WEBSITE
   |
   | Generate verification code
   v
PLAYER
   |
   | Enter code in Minecraft
   v
MINECRAFT PLUGIN
   |
   | Verify code
   v
WEBSITE
   |
   v
Minecraft Account Linked
```

Gunakan Minecraft UUID sebagai identifier utama jika memungkinkan.

### Verification Code Rules (v1.2)

Kode verifikasi bukan sekadar string acak tanpa batas — harus punya aturan eksplisit:

- **Expiry**: kode kedaluwarsa singkat, contoh 10 menit.
- **Single-use**: kode langsung invalid setelah berhasil dipakai sekali.
- **Cukup random**: minimal setara 6 digit numerik acak atau lebih kuat, bukan pola tebakan (bukan sequential/username-based).
- **Rate limit generate**: user tidak boleh generate kode baru berkali-kali dalam waktu singkat (cegah spam/brute force).
- **Rate limit verify**: percobaan verifikasi kode di sisi plugin/API dibatasi (cegah brute force menebak kode orang lain).
- Satu `minecraft_uuid` hanya boleh terhubung ke satu `user_id` aktif pada satu waktu.

Data is defined in §16.2 and must support Java Online, Java Offline, and Bedrock/Floodgate identities.

---

# 18. Store

Store harus mendukung:

- category;
- product;
- product detail;
- pricing;
- cart;
- checkout;
- order.

## Product

Minimal:

```text
id
category_id
name
slug
description
image
status
created_at
updated_at
```

Harga tidak disimpan langsung di produk — lihat "Multi-Currency Pricing" di bawah (`product_prices`).

## Product Types

```text
RANK
COSMETIC
ITEM
CURRENCY
KEY
BUNDLE
SUBSCRIPTION
OTHER
```

### Multi-Currency Pricing (v1.2)

Satu `price` + `currency` per produk tidak cukup karena target pasar butuh IDR **dan** USD tampil sekaligus (bukan hasil konversi kurs real-time yang fluktuatif terhadap revenue).

Gunakan tabel terpisah agar satu produk bisa punya harga tetap di beberapa currency:

```text
product_prices
- id
- product_id
- currency
- price
- is_default
```

Checkout menggunakan `product_prices` sesuai currency yang dipilih user/region, bukan konversi otomatis dari satu harga dasar. Penambahan currency baru = insert baris baru, bukan migrasi schema.

---

# 19. Order System

Order harus memiliki lifecycle yang jelas.

Recommended statuses:

```text
PENDING
PAYMENT_PROCESSING
PAID
DELIVERY_PENDING
DELIVERED
DELIVERY_FAILED
CANCELLED
REFUNDED
EXPIRED
```

State flow:

```text
PENDING
   |
   +----> EXPIRED (jika melewati batas waktu tanpa dibayar)
   |
   v
PAYMENT_PROCESSING
   |
   v
PAID
   |
   v
DELIVERY_PENDING
   |
   +----> DELIVERED
   |
   +----> DELIVERY_FAILED
```

### Order Expiry (v1.2)

Status `EXPIRED` butuh pemicu eksplisit, bukan cuma nama status tanpa mekanisme:

- Tambahkan field `expires_at` pada `orders` (lihat §35), diisi saat order dibuat (contoh: `created_at + 60 menit`).
- Scheduled job (cron/queue) memindai order berstatus `PENDING`/`PAYMENT_PROCESSING` yang sudah lewat `expires_at` -> ubah ke `EXPIRED`.
- Order yang sudah `EXPIRED` tidak boleh lagi diproses jika webhook pembayaran datang terlambat — webhook yang telat untuk order expired harus dicatat sebagai anomali (butuh review manual admin), bukan otomatis mengubah status kembali.

---

## 19.1 Order State Transition Matrix

Hanya transition berikut yang valid:

```text
PENDING
 ├──> PAYMENT_PROCESSING
 ├──> EXPIRED
 └──> CANCELLED

PAYMENT_PROCESSING
 ├──> PAID
 ├──> EXPIRED
 └──> CANCELLED

PAID
 └──> DELIVERY_PENDING

DELIVERY_PENDING
 ├──> DELIVERED
 └──> DELIVERY_FAILED

DELIVERY_FAILED
 └──> DELIVERY_PENDING

PAID
 └──> REFUNDED
```

Transition lain harus ditolak dan dicatat sebagai invalid state transition.

Late payment untuk order `EXPIRED` tidak boleh otomatis mengembalikan order ke `PAID`.

# 20. Order Items

Order item harus menyimpan snapshot data produk.

```text
order_items
- id
- order_id
- product_id
- product_name_snapshot
- unit_price
- quantity
- subtotal
```

Alasan:

Jika harga produk berubah, historical order tidak boleh ikut berubah.

---

# 21. Payment Architecture

Payment harus menggunakan abstraction layer.

```text
PaymentService
      |
      +---- Indonesia Provider
      |
      +---- International Provider
```

Tujuan:

- provider dapat diganti;
- provider baru dapat ditambahkan;
- checkout tidak bergantung langsung pada satu provider.

---

# 22. Indonesia Payment (v2.0 — Midtrans CONFIRMED, scaffold only)

**Provider final: Midtrans.** Tidak perlu evaluasi kandidat lain lagi — keputusan sudah diambil oleh product owner.

DANA H2H/Xendit/provider lain **tidak** masuk scope build ini. Jika dibutuhkan lagi nanti, tambahkan sebagai `PaymentMethod` baru mengikuti pola yang sama seperti Midtrans, tanpa mengubah abstraction layer.

**Scope build ini (v2.0):**

- Buat plugin/class custom `MidtransPaymentMethod` yang extend `Azuriom\Plugin\Shop\Payment\PaymentMethod\PaymentMethod` (lihat §6.4) — struktur class, `$id`, `$name`, halaman konfigurasi admin (client key, server key, mode sandbox/production) — semuanya disiapkan.
- Method `startPayment()` **boleh berupa stub/placeholder** (misal langsung return halaman "pembayaran belum aktif") — pemanggilan API Snap Midtrans yang sesungguhnya **ditunda**, menunggu kredensial merchant Midtrans production siap.
- Endpoint webhook (`route('shop.payments.notification', 'midtrans')` atau setara) tetap disiapkan strukturnya (menerima request, log, validasi dasar), tapi verifikasi signature Midtrans yang sesungguhnya menyusul bersamaan dengan `startPayment()`.
- Field `payments.provider` akan bernilai `'midtrans'`, `payments.method` menyimpan sub-metode (QRIS/VA/e-wallet/dsb — Midtrans mendukung semuanya lewat satu integrasi Snap).

**Kapan API sungguhan dikerjakan:** setelah akun merchant Midtrans (sandbox lalu production) tersedia — task terpisah dari build ini, bukan bagian dari handoff PRD ini.

---

# 23. International Payment (v2.0 — Deferred)

**Tidak masuk scope build ini.** PayPal, Stripe, dan Binance Pay tetap jadi kandidat masa depan begitu target pasar internasional mulai digarap serius, tapi tidak diimplementasikan sekarang.

Yang tetap harus disiapkan di fase ini (karena menyangkut desain, bukan implementasi):

- Payment abstraction layer (§21) harus tetap provider-agnostic, supaya menambah provider baru nanti = tambah 1 class, bukan rework checkout.
- Minimal target currency tetap:

```text
IDR
USD
```

- `product_prices` (§18) sudah mendukung multi-currency dari awal, jadi menambahkan payment provider USD nanti tidak butuh migrasi schema produk.

---

# 24. Payment Flow

```text
PLAYER
  |
  v
SELECT PRODUCT
  |
  v
CHECKOUT
  |
  v
CREATE ORDER
  |
  v
CREATE PAYMENT
  |
  v
PAYMENT PROVIDER
  |
  v
PLAYER PAYS
  |
  v
WEBHOOK
  |
  v
VERIFY PAYMENT
  |
  v
MARK ORDER PAID
  |
  v
CREATE DELIVERY JOB
  |
  v
MINECRAFT INTEGRATION
  |
  v
DELIVER REWARD
  |
  v
RECORD DELIVERY
```

---

# 25. Payment Security

Webhook tidak boleh dipercaya begitu saja.

Server harus:

1. memvalidasi request;
2. memverifikasi signature jika tersedia;
3. memverifikasi transaction ID;
4. memverifikasi amount;
5. memverifikasi currency;
6. memverifikasi order;
7. memastikan order belum diproses;
8. menggunakan idempotency.

Frontend tidak boleh menentukan:

- final price;
- payment status;
- transaction success;
- reward eligibility.

### CSRF vs Webhook (v1.2)

Endpoint webhook (`POST /api/v1/payment/webhook/{provider}`) **harus dikecualikan** dari middleware CSRF standar, karena request datang dari server payment provider, bukan browser dengan session — CSRF token tidak relevan di sini dan justru akan menolak webhook yang sah.

Pengecualian dari CSRF ini **tidak mengurangi keamanan** selama gantinya tetap wajib: signature verification, validasi IP/allowlist provider (jika provider menyediakan), dan idempotency check seperti dijelaskan di atas. Jangan mengecualikan endpoint lain dari CSRF hanya karena alasan kepraktisan.

---

# 26. Idempotency

Jika payment provider mengirim webhook yang sama berkali-kali:

```text
Webhook #1 -> Process
Webhook #2 -> Ignore
Webhook #3 -> Ignore
Webhook #4 -> Ignore
```

Tidak boleh terjadi duplicate reward.

Gunakan unique identifier dari provider dan database constraint jika sesuai.

---

# 27. Payment Database

```text
payments
- id
- order_id
- provider
- provider_transaction_id
- method
- amount
- currency
- status
- raw_reference
- paid_at
- created_at
- updated_at
```

Sensitive payment data tidak boleh disimpan tanpa kebutuhan yang jelas.

## 27.1 Payment Events / Webhook Log

`payments` menyimpan current state; event webhook menyimpan historical events.

```text
payment_events
- id
- payment_id
- provider_event_id
- event_type
- payload_hash
- payload
- status
- received_at
- processed_at
- created_at
```

Aturan:

- `provider_event_id` harus unique bila provider menyediakan identifier event;
- payload sensitif harus diminimalkan dan dilindungi;
- event yang sama tidak boleh diproses dua kali;
- event log digunakan untuk audit/debugging, bukan sebagai pengganti status canonical pada `payments`.



---

# 28. Minecraft Integration

Website harus menggunakan integration layer.

Preferred architecture:

```text
Website
   |
   | HTTPS / Authenticated API
   v
Minecraft Integration
   |
   v
Minecraft Plugin
   |
   v
Minecraft Server
```

RCON dapat digunakan untuk kebutuhan sederhana, tetapi custom plugin/API lebih disarankan untuk architecture jangka panjang.

Pterodactyl adalah infrastructure/control panel, bukan business integration layer utama.

---

# 29. Minecraft Delivery

Payment success tidak boleh langsung menjalankan reward dari frontend.

Recommended:

```text
PAYMENT SUCCESS
      |
      v
ORDER = PAID
      |
      v
DELIVERY JOB
      |
      v
MINECRAFT SERVICE
      |
      v
PLUGIN
      |
      v
MINECRAFT SERVER
```

Delivery harus memiliki:

- status;
- attempts;
- retry;
- logs;
- error handling.

---

# 30. Delivery Status

```text
PENDING
PROCESSING
DELIVERED
FAILED
CANCELLED
```

Jika gagal:

```text
FAILED
   |
   v
RETRY
   |
   v
PROCESSING
   |
   +----> DELIVERED
   |
   +----> FAILED
```

Retry harus dibatasi.

---

# 31. Delivery Database

Delivery dibuat **per order_item**, bukan per order (v1.2 — lihat §33). Satu order dengan beberapa item (misal rank di server Survival + cosmetic di server Lobby) menghasilkan beberapa baris delivery independen, masing-masing dengan status dan retry sendiri.

```text
deliveries
- id
- order_item_id
- order_id
- minecraft_account_id
- server_id
- status
- attempts
- scheduled_at
- completed_at
- created_at
- updated_at
```

Delivery logs:

```text
delivery_logs
- id
- delivery_id
- attempt
- action
- response
- status
- created_at
```

---

# 32. Player Offline Handling

Jika reward harus diberikan tetapi player sedang offline, sistem harus memiliki strategy.

### Koreksi Pembagian Tanggung Jawab (v1.2)

Versi sebelumnya menaruh keputusan "cek online/offline" di sisi Website, padahal §70 sendiri menegaskan **Website != Minecraft Server**: player state adalah milik Minecraft, bukan Website. Website yang mengurus online/offline check akan selalu terlambat/tidak akurat dibanding plugin yang berjalan langsung di server.

Pembagian yang benar:

```text
Order PAID
   |
   v
Delivery Job Created (Website)
   |
   v
Website -> Minecraft Integration: "deliver reward X to player Y"
   |
   v
Plugin menerima request
   |
   +---- Player online  -> eksekusi langsung, kirim ACK
   |
   +---- Player offline -> plugin simpan sebagai pending reward
                            di sisi server, eksekusi otomatis
                            saat player join, baru kirim ACK
```

Website hanya perlu tahu **status delivery** (`PENDING` / `DELIVERED` / `FAILED`) dari ACK yang dikirim plugin — bukan menentukan sendiri kapan player online. Ini juga membuat sistem tahan terhadap Minecraft server restart/maintenance tanpa perlu Website terus-menerus polling status player.

Jangan menganggap player harus online saat pembayaran.

---

# 33. Multi-Server Future Support

Database tidak boleh mengasumsikan hanya ada satu server.

Contoh future:

```text
Network
 |
 +-- Lobby
 |
 +-- Survival
 |
 +-- Skyblock
 |
 +-- BedWars
```

Entity:

```text
servers
- id
- name
- identifier
- address
- status
- created_at
- updated_at
```

Product dapat memiliki delivery rules untuk server tertentu.

**Implikasi (v1.2):** karena tiap `order_item` mengacu ke satu `product`, dan tiap `product` bisa punya target `server` berbeda, maka delivery **harus** dibuat per `order_item` (lihat §31, §35) — bukan satu delivery untuk keseluruhan order. Jika delivery tetap dibuat per-order, order berisi item lintas-server tidak bisa direpresentasikan dengan benar.

---

# 34. Database ERD

Initial entities:

```text
USERS
 |
 +----< MINECRAFT_ACCOUNTS
 |
 +----< ORDERS
             |
             +----< ORDER_ITEMS >---- PRODUCTS >---- PRODUCT_PRICES
             |            |
             |            +----< DELIVERIES
             |                          |
             |                          +----< DELIVERY_LOGS
             |
             +----< PAYMENTS

PRODUCT_CATEGORIES
       |
       +----< PRODUCTS

SERVERS
       |
       +----< DELIVERIES

PAYMENT_PROVIDERS

AUDIT_LOGS
```

Relational view (v1.2 — delivery dipindah ke level ORDER_ITEM, bukan ORDER, karena satu order bisa berisi item untuk server berbeda):

```text
USER
 |
 +----< MINECRAFT_ACCOUNT
 |
 +----< ORDER
          |
          +----< ORDER_ITEM >---- PRODUCT >---- PRODUCT_PRICE
          |            |
          |            +----< DELIVERY >---- SERVER
          |                          |
          |                          +----< DELIVERY_LOG
          |
          +----< PAYMENT
```

---

# 35. Database Design

## users

```text
id
email
username
password_hash
role
status
created_at
updated_at
```

## minecraft_accounts

```text
id
user_id
edition
auth_mode
minecraft_uuid
minecraft_username
floodgate_uuid
verified_at
last_seen_at
created_at
updated_at
```

See §16.2 for identity rules and uniqueness constraints.

## product_categories

```text
id
name
slug
description
status
created_at
updated_at
```

## products

```text
id
category_id
name
slug
description
image
status
created_at
updated_at
```

## product_prices (v1.2 — lihat §18)

```text
id
product_id
currency
price
is_default
created_at
updated_at
```

## orders

```text
id
user_id
order_number
currency
subtotal
discount
total
status
expires_at
created_at
updated_at
```

## order_items

```text
id
order_id
product_id
product_name_snapshot
unit_price
quantity
subtotal
delivery_snapshot
created_at
updated_at
```

`delivery_snapshot` menyimpan konfigurasi delivery yang sudah ditetapkan pada saat checkout/order creation agar perubahan product/delivery rule di masa depan tidak mengubah transaksi lama.

## payments

```text
id
order_id
provider
provider_transaction_id
method
amount
currency
status
raw_reference
paid_at
created_at
updated_at
```

## servers

```text
id
name
identifier
address
status
created_at
updated_at
```

## deliveries (v2.1 — per order_item)

```text
id
order_item_id
minecraft_account_id
server_id
action_type
payload_snapshot
status
attempts
next_attempt_at
scheduled_at
completed_at
last_error
created_at
updated_at
```

`order_item_id` menjadi single source of truth untuk order item yang dilayani. `payload_snapshot` menjaga konfigurasi delivery tetap konsisten dengan transaksi saat order dibuat.

## delivery_logs

```text
id
delivery_id
attempt
action
response
status
created_at
```

## payment_providers

```text
id
name
identifier
status
configuration_reference
created_at
updated_at
```

Actual secret/API credentials harus disimpan secara aman, bukan plaintext di database kecuali terdapat encryption strategy yang tepat.

## audit_logs

```text
id
actor_user_id
action
target_type
target_id
metadata
ip_address
created_at
```

---

# 36. DFD Level 0

```text
                 +-------------+
                 |    PLAYER   |
                 +------+------+
                        |
                        v
                +---------------+
                |    WEBSITE    |
                +-------+-------+
                        |
          +-------------+-------------+
          |             |             |
          v             v             v
     +---------+   +---------+   +-----------+
     | DATABASE|   | PAYMENT |   | MINECRAFT |
     |         |   | GATEWAY |   | INTEGRATION|
     +---------+   +---------+   +-----+-----+
                                      |
                                      v
                              +---------------+
                              | MINECRAFT     |
                              | SERVER        |
                              +---------------+
```

---

# 37. DFD Level 1: Checkout

```text
PLAYER
  |
  v
PRODUCT
  |
  v
CART
  |
  v
CHECKOUT
  |
  v
ORDER SERVICE
  |
  +----> DATABASE
  |
  v
PAYMENT SERVICE
  |
  v
PAYMENT PROVIDER
  |
  v
PLAYER
```

---

# 38. DFD Level 1: Payment Webhook

```text
PAYMENT PROVIDER
      |
      | Webhook
      v
WEB APPLICATION
      |
      v
VALIDATE WEBHOOK
      |
      v
VERIFY TRANSACTION
      |
      v
DATABASE
      |
      v
UPDATE ORDER
      |
      v
CREATE DELIVERY
```

---

# 39. DFD Level 1: Delivery

```text
DELIVERY QUEUE
      |
      v
MINECRAFT SERVICE
      |
      v
AUTHENTICATED REQUEST
      |
      v
MINECRAFT PLUGIN
      |
      v
MINECRAFT SERVER
      |
      v
DELIVERY RESULT
      |
      v
DATABASE
```

---

# 40. Sequence Diagram: Purchase

```text
Player -> Website: Select Product
Website -> Database: Get Product
Database --> Website: Product Data

Player -> Website: Checkout
Website -> Database: Create Order
Database --> Website: Order Created

Website -> Payment Provider: Create Payment
Payment Provider --> Website: Payment URL / Reference

Website --> Player: Payment Instructions

Player -> Payment Provider: Pay

Payment Provider -> Website: Webhook
Website -> Payment Provider: Verify Transaction
Payment Provider --> Website: Valid

Website -> Database: Mark Order PAID
Website -> Delivery System: Create Delivery

Delivery System -> Minecraft Integration: Deliver Reward
Minecraft Integration -> Minecraft Plugin: Authenticated Request
Minecraft Plugin -> Minecraft Server: Execute Reward

Minecraft Server --> Minecraft Plugin: Result
Minecraft Plugin --> Minecraft Integration: Result
Minecraft Integration -> Database: Record Delivery

Website --> Player: Order Status
```

---

# 41. Security Architecture

Security layers:

```text
Internet
   |
   v
Cloudflare
   |
   +---- WAF
   +---- DDoS Protection
   +---- TLS
   |
   v
Nginx
   |
   +---- Rate Limit
   +---- Request Filtering
   |
   v
Application
   |
   +---- Authentication
   +---- Authorization
   +---- Validation
   +---- CSRF Protection
   |
   v
Database
```

---

# 42. Security Requirements

## Authentication

- Password hashing.
- Secure sessions.
- Secure cookies.
- Rate limiting.
- Password reset security.
- CSRF protection.

## Authorization

Use RBAC.

Minimum roles:

```text
USER
STAFF
ADMIN
SUPER_ADMIN
```

Every administrative endpoint must perform authorization.

## Input

Validate all user input.

Protect against:

- SQL injection;
- XSS;
- CSRF;
- command injection;
- SSRF;
- path traversal;
- privilege escalation.

---

# 43. Minecraft API Security

Website-to-Minecraft communication must use authentication.

Possible mechanisms:

- API key;
- signed requests;
- HMAC;
- mTLS for advanced deployments.

Secrets must:

- not be committed;
- not be hardcoded;
- not be sent to frontend;
- be stored using environment variables or secure secret storage.

---

# 44. Payment Security Threat Model

Potential threats:

### Fake Payment Success

Attacker modifies frontend response.

Mitigation:

```text
Server-side verification
```

### Amount Manipulation

Attacker changes price.

Mitigation:

```text
Server calculates price from database
```

### Webhook Replay

Same webhook sent repeatedly.

Mitigation:

```text
Idempotency + unique provider transaction ID
```

### Fake Webhook

Attacker sends request to webhook endpoint.

Mitigation:

```text
Signature verification
Provider verification
Validation
Rate limiting
```

### Duplicate Delivery

Same payment processed multiple times.

Mitigation:

```text
Order state machine
Idempotency
Unique constraints
Delivery status
```

### Carding / Card Testing (v1.2)

Webstore digital goods (rank/item/currency) adalah target umum untuk carding — attacker mencoba banyak kartu curian dengan transaksi kecil untuk memvalidasi kartu mana yang masih aktif.

Indikasi: banyak transaksi gagal berturut-turut dari IP/user/device yang sama dalam waktu singkat, atau banyak percobaan dengan nominal kecil dan metode kartu berbeda-beda.

Mitigation:

```text
Rate limit percobaan pembayaran gagal per user/IP
Gunakan fraud controls yang tersedia pada provider yang benar-benar aktif
CAPTCHA/Cloudflare Turnstile di halaman checkout jika dibutuhkan
Batasi/monitor guest checkout untuk transaksi kartu
Pantau chargeback rate jika metode kartu diaktifkan
```

Ini penting khusus untuk provider kartu (Stripe/PayPal), tidak berlaku sama untuk QRIS/DANA/Binance yang punya karakteristik fraud berbeda.

---

# 45. Logging

## Application Logs

Record:

- errors;
- warnings;
- integration failures;
- unexpected conditions.

## Payment Logs

Record:

- provider;
- transaction reference;
- order;
- event type;
- status;
- timestamps.

Do not log secrets or unnecessary sensitive payment data.

## Delivery Logs

Record:

- order;
- player;
- server;
- action;
- attempt;
- result;
- error.

## Audit Logs

Record administrative actions:

```text
Actor
Action
Target
Timestamp
IP
Result
```

---

# 46. Admin Dashboard

## Dashboard

Display:

- revenue;
- orders;
- successful payments;
- failed payments;
- pending deliveries;
- failed deliveries;
- online players.

## Products

Admin can:

- create;
- update;
- deactivate;
- categorize;
- configure delivery.

## Orders

Admin can:

- search;
- filter;
- view;
- inspect payment;
- inspect delivery.

## Users

Admin can:

- search;
- view account;
- view Minecraft account;
- view order history.

## Delivery

Admin can:

- view pending;
- view delivered;
- view failed;
- retry where safe.

## Payment

Admin can:

- view providers;
- view transactions;
- inspect webhook status;
- inspect payment errors.

---

# 47. Internationalization

Initial languages:

```text
id-ID
en-US
```

Initial currencies:

```text
IDR
USD
```

All UI strings should be localizable.

Do not hardcode user-facing text throughout business logic.

---

# 48. Currency Handling

Do not use floating point for monetary calculations.

Use integer minor units or a decimal type appropriate to the database.

Example:

```text
IDR 25,000
USD 1.50
```

Store:

- amount;
- currency;
- provider transaction amount;
- provider transaction currency.

Currency conversion must be explicit.

---

# 49. Refund

Refund workflow should be represented separately from simple order cancellation.

Possible states:

```text
REFUND_REQUESTED
REFUND_PROCESSING
REFUNDED
REFUND_FAILED
```

Refund must not automatically revoke Minecraft rewards unless a deliberate business rule exists.

Refund policy must be defined before production.

---

## 50.1 Delivery Rule Database

Delivery rules harus mempunyai persistence model sendiri.

```text
product_delivery_rules
- id
- product_id
- server_id
- action_type
- parameters
- sort_order
- enabled
- created_at
- updated_at
```

Relasi:

```text
PRODUCT
   |
   +----< PRODUCT_DELIVERY_RULE >---- SERVER
```

Aturan:

- `action_type` berasal dari allowlist yang ditentukan sistem;
- `parameters` tidak boleh menjadi jalur arbitrary command execution untuk user biasa;
- perubahan delivery rule tidak boleh mengubah historical delivery yang sudah dibuat;
- ketika order dibuat, informasi delivery yang diperlukan untuk transaksi harus di-snapshot ke delivery/job atau struktur immutable yang setara.

# 50. Product Delivery Rules

Product should not merely contain an arbitrary command string without validation.

Recommended abstraction:

```text
Product
   |
   v
Delivery Rule
   |
   +---- Action
   +---- Server
   +---- Parameters
```

Examples of conceptual actions:

```text
GRANT_RANK
GRANT_ITEM
GRANT_CURRENCY
GRANT_KEY
EXECUTE_REWARD
```

Commands should be controlled by trusted server-side configuration.

Never allow normal users to inject arbitrary Minecraft commands.

---

# 51. Failure Handling

The system must define behavior for:

## Payment Gateway Timeout

```text
Payment = PROCESSING
```

Do not automatically mark failed without evidence.

## Minecraft Server Offline

```text
Order = PAID
Delivery = PENDING
```

Delivery retries later.

## Player Offline

```text
Delivery = PENDING
```

## Minecraft API Failure

```text
Delivery = FAILED
```

Then retry according to policy.

## Database Failure

Application should fail safely and avoid partially confirming transactions.

---

# 52. Reliability Principles

Important rule:

> Payment success and Minecraft delivery are separate states.

Never assume:

```text
PAID = DELIVERED
```

Instead:

```text
PAID
  |
  v
DELIVERY_PENDING
  |
  v
DELIVERED
```

This prevents lost purchases when Minecraft is temporarily unavailable.

---

# 53. API Design

The API section defines capabilities, not mandatory implementation details. Exact routes/controllers must follow Azuriom/Laravel conventions.

```text
POST /api/v1/minecraft/link
POST /api/v1/minecraft/verify
POST /api/v1/minecraft/delivery

GET  /api/v1/server/status

POST /api/v1/payment/webhook/{provider}

GET  /api/v1/orders
GET  /api/v1/orders/{order}

POST /api/v1/checkout
```

The following are conceptual capabilities:

```text
Minecraft:
- link account
- verify account
- submit delivery

Server:
- get server status

Payment:
- receive provider webhook

Orders:
- list orders
- get order
- create checkout

```

Exact routes/controllers are implementation details and should follow Azuriom/Laravel conventions.

Do not create an API simply because REST endpoints look impressive.

---

# 54. API Response Principle

Use consistent response structure.

Conceptually:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

Error:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Invalid request"
  }
}
```

Actual implementation should follow the conventions of the chosen framework.

---

# 55. Environment Configuration

Secrets/configuration should be environment-based.

Example:

```text
APP_ENV=
APP_URL=

DATABASE_URL=

PAYMENT_PROVIDER_KEY=
PAYMENT_PROVIDER_SECRET=
PAYMENT_WEBHOOK_SECRET=

MINECRAFT_API_URL=
MINECRAFT_API_KEY=

CLOUDFLARE_CONFIGURATION=
```

Do not commit real credentials.

Provide:

```text
.env.example
```

without real secrets.

---

# 56. Deployment

Initial deployment:

```text
Developer
   |
   v
Git Repository
   |
   v
VPS
   |
   +---- Nginx
   +---- Application
   +---- Database
   +---- SSL
   +---- Firewall
   +---- Backup
```

Production deployment should use version control and repeatable deployment procedures.

---

# 57. Development Environment

Development should ideally be reproducible.

Recommended:

```text
Repository
 |
 +---- Application
 +---- Database migration
 +---- Configuration example
 +---- Documentation
 +---- Tests
```

Do not depend on manual undocumented configuration.

---

# 58. Testing Strategy

## Unit Tests

Test:

- price calculation;
- order state;
- payment state;
- delivery state;
- permission checks.

## Integration Tests

Test:

- database;
- payment provider;
- webhook;
- Minecraft API.

## Security Tests

Test:

- SQL injection;
- XSS;
- CSRF;
- authentication bypass;
- authorization bypass;
- webhook replay;
- fake payment;
- amount manipulation;
- command injection.

## Failure Tests

Simulate:

- payment timeout;
- duplicate webhook;
- Minecraft server offline;
- player offline;
- API timeout;
- database failure;
- application restart.

---

# 59. Acceptance Criteria

## Account

- User can register.
- User can login.
- User can reset password.
- User can link Minecraft account.
- Minecraft account ownership is verified.

## Store

- Products display correctly.
- User can checkout.
- Order is persisted.
- Historical product pricing remains correct.

## Payment — Build Phase

- Test/manual payment can be created.
- Test/manual payment flow can move an order through the expected state machine.
- Test webhook structure can be received and processed idempotently.
- Duplicate test webhook does not duplicate reward.
- Real Midtrans payment creation and signature verification are explicitly deferred until merchant credentials are available.

## Payment — Midtrans Integration Phase

- Real Midtrans payment can be created.
- Real Midtrans webhook is received.
- Real Midtrans signature/transaction verification passes.
- Order becomes PAID only after valid server-side verification.
- Duplicate real webhook does not duplicate reward.

## Minecraft

- Valid player can receive reward.
- Delivery is logged.
- Failed delivery can retry.
- Player offline does not lose reward.

## Admin

- Admin can manage products.
- Admin can inspect orders.
- Admin can inspect payment.
- Admin can inspect delivery.
- Admin actions are logged.

## Security

- Secrets are not in source control.
- Database is not publicly exposed.
- Admin endpoints are protected.
- Webhooks are verified.
- Sensitive actions are rate limited.

---

# 60. Development Phases

## Phase 0 — Technical Validation

Technology decision is already locked: Azuriom.

Tasks:

- inspect installed/available Azuriom plugins;
- verify the Azuriom extension points used by this project;
- inspect payment integration requirements;
- inspect Minecraft integration requirements;
- confirm the live Minecraft server context.

Output:

```text
Implementation Gap Record
Integration Decision Record
```

---

## Phase 1 — System Design

Create:

```text
PRD.md
ARCHITECTURE.md
ERD.md
DFD.md
SEQUENCE.md
API.md
SECURITY.md
PAYMENT.md
MINECRAFT-INTEGRATION.md
DATABASE.md
DEPLOYMENT.md
TESTING.md
ROADMAP.md
```

---

## Phase 2 — Infrastructure

Setup:

```text
VPS
 |
 +---- OS
 +---- SSH
 +---- Firewall
 +---- Nginx
 +---- Runtime
 +---- Database
 +---- SSL
 +---- Cloudflare
 +---- Backup
 +---- Monitoring
```

---

## Phase 3 — Core Website

Implement:

- homepage;
- server status;
- authentication;
- profile;
- Minecraft linking for Java Online, Java Offline, and Bedrock/Floodgate identities;
- Wiki plugin installed + initial content structure (v2.0, see §15.3).

---

## Phase 4 — Store

Implement:

- categories;
- products;
- product_prices (multi-currency, see §18);
- cart;
- checkout;
- orders.

---

## Phase 5 — Payment (v2.0 — scope narrowed to Midtrans scaffold)

Implement:

- payment abstraction layer (provider-agnostic, see §21);
- `MidtransPaymentMethod` class structure + admin configuration page (client key, server key, sandbox/production toggle);
- webhook endpoint structure (route, logging, basic request validation) — **without** real Midtrans signature verification yet;
- idempotency mechanism (ready for when real webhook is wired in);
- order/payment state machine fully working end-to-end using a **manual/test payment method** for internal testing (so checkout → order → delivery can be tested without live Midtrans).

**Explicitly NOT in this phase:** real Midtrans Snap API call, real signature verification, international provider (PayPal/Stripe/Binance) — see §22, §23.

---

## Phase 6 — Minecraft Integration

Implement:

- custom Minecraft integration/plugin;
- authentication/linking protocol;
- Java Online identity handling;
- Java Offline identity handling;
- Bedrock/Floodgate identity handling if enabled;
- delivery;
- retry;
- delivery logs;
- player offline handling.

---

## Phase 7 — Admin

Implement:

- dashboard;
- products;
- users;
- orders;
- payments;
- deliveries;
- audit logs.

---

## Phase 8 — Security & Testing

Run:

- functional tests;
- integration tests;
- security tests;
- failure tests;
- load/performance tests.

---

## Phase 9 — Production

Before production:

- backup verified;
- HTTPS verified;
- payment webhook verified;
- Minecraft delivery verified;
- monitoring configured;
- logging configured;
- recovery procedure documented.

---

# 61. Technology Decision Matrix

| Criteria | Azuriom | NamelessMC | Custom |
|---|---:|---:|---:|
| Minecraft focus | High | High | Depends |
| Existing features | High | High | Low |
| Development speed | High | High | Low |
| Customization | High | High | Very High |
| Maintenance | Lower | Lower | Higher |
| Initial complexity | Low | Low | High |
| Payment extension | Evaluate | Evaluate | Full control |
| Minecraft integration | Evaluate | Evaluate | Full control |
| Community features | Good | Strong | Custom |
| Long-term flexibility | Good | Good | Very High |

Technology decision is locked: Azuriom. Remaining gap analysis is for implementation details and custom plugin scope, not for selecting a different CMS.

---

# 62. Architecture Decision

Recommended MVP architecture:

```text
             +----------------+
             |   CLOUDFLARE   |
             +-------+--------+
                     |
                     v
             +---------------+
             | NGINX         |
             +-------+-------+
                     |
                     v
             +---------------+
             | APPLICATION   |
             +---+-------+---+
                 |       |
                 v       v
             DATABASE  PAYMENT
                         |
                         v
                    PROVIDERS

APPLICATION
     |
     v
MINECRAFT INTEGRATION
     |
     v
MINECRAFT PLUGIN
     |
     v
MINECRAFT SERVER
```

Use a modular monolith.

Do not start with microservices.

---

# 63. Scaling Strategy

If resource usage becomes too high:

```text
2 GB VPS
   |
   v
4 GB / larger VPS
   |
   v
Optimize application
   |
   v
Separate database
   |
   v
Separate workers
   |
   v
Horizontal scaling if actually required
```

Scale based on measurements:

- CPU usage;
- RAM usage;
- disk usage;
- database load;
- request rate;
- payment volume;
- delivery volume.

---

# 64. Monitoring

Monitor:

```text
CPU
RAM
DISK
NETWORK
DATABASE
HTTP STATUS
HTTP ERROR RATE
PAYMENT FAILURES
DELIVERY FAILURES
```

Recommended health endpoints:

```text
/health
/ready
```

Exact implementation depends on framework.

---

# 65. Admin Operational Rules

Admin should never directly edit payment status without an audit trail.

Manual intervention should:

- require appropriate permission;
- create audit log;
- record reason;
- preserve transaction history.

Never delete successful transaction records merely to "clean the database."

---

# 66. Data Integrity Rules

1. Transaction records are historical records.
2. Product snapshots must be stored in order items.
3. Payment provider transaction IDs should be unique where appropriate.
4. Delivery must reference an order.
5. Minecraft account ownership must be verified.
6. Currency must always accompany monetary amounts.
7. Order status transitions must be validated.
8. Payment status transitions must be validated.
9. Delivery status transitions must be validated.

---

# 67. Developer / AI Agent Instructions

You are acting as:

- Senior Software Architect;
- Backend Engineer;
- DevOps Engineer;
- Security Engineer;
- Minecraft Integration Engineer.

Your job is not merely to produce code that runs. Your job is to produce code that is secure, maintainable, testable, observable, and consistent with this PRD.

## Before Coding

Always:

1. Inspect repository.
2. Identify framework/CMS.
3. Identify existing architecture.
4. Identify existing database schema.
5. Identify existing integrations.
6. Identify reusable features.
7. Identify risks.
8. Propose implementation plan.

Do not blindly rewrite existing functionality.

## Existing CMS Rule

If Azuriom or another CMS already provides a feature:

- use it;
- extend it;
- create a plugin;
- create a theme;
- integrate with it.

Do not rebuild the feature unless necessary.

## Live Server Context Rule (v2.0)

This project's AI agent has a direct integration with the actual, already-running Minecraft server — it can see which plugins are installed and how they are configured, not just a generic Minecraft server.

Because of this:

- Never invent generic example commands (`/rank set`, `/give`, etc.) for the Wiki (§15.3) or for delivery action mapping (§50) without first checking what is actually installed on the real server (LuckPerms, EssentialsX, custom plugins, etc.).
- Before implementing Minecraft delivery actions (`GRANT_RANK`, `GRANT_ITEM`, ...), inspect the real plugin(s) that will execute them and match the actual command syntax/permission node they expose.
- Before deciding between AuthMeReloaded and a custom authentication/link plugin, inspect the actual server mode (`online-mode`), Geyser/Floodgate presence, proxy setup, and existing authentication plugins. Do not assume AuthMe is compatible with the live Bedrock path.
- Before writing Wiki content (§15.3), pull the actual command list and actual rank/perk structure from the live server instead of drafting placeholder content for the owner to rewrite later.
- If server-side information contradicts an assumption made elsewhere in this PRD (e.g. a different permission plugin than LuckPerms is actually in use), flag it explicitly and adapt the integration layer — do not silently force-fit the server to match the document.
- This does not change the security rules above: the agent still must not expose arbitrary command execution to normal users, and reward commands must remain server-side controlled (see Minecraft Rule below).

## Database Rule

Every schema change requires migration.

Do not make destructive schema changes without considering:

- backup;
- migration;
- rollback;
- existing data.

## Payment Rule

Never trust client-side:

- price;
- amount;
- payment status;
- transaction success.

Everything important must be validated server-side.

## Webhook Rule

Every payment webhook must be:

- validated;
- authenticated where supported;
- signature-verified where supported;
- idempotent;
- logged;
- associated with an existing order;
- checked against expected amount/currency.

## Minecraft Rule

Never expose arbitrary Minecraft command execution to normal users.

Reward commands/actions must be controlled server-side.

## Security Rule

Always consider:

- authentication;
- authorization;
- RBAC;
- input validation;
- SQL injection;
- XSS;
- CSRF;
- SSRF;
- command injection;
- path traversal;
- rate limiting;
- replay attacks;
- privilege escalation.

## Secret Rule

Never commit:

- passwords;
- API keys;
- webhook secrets;
- tokens;
- private keys.

Use environment variables or proper secret management.

## Reliability Rule

Always consider failure scenarios:

- payment provider timeout;
- duplicate webhook;
- Minecraft server offline;
- player offline;
- API timeout;
- database failure;
- application restart.

Payment and delivery are separate states.

## Resource Rule

Remember the production MVP server has:

```text
1 vCore
2 GB RAM
25 GB NVMe
```

Avoid unnecessary services.

## Implementation Process

For every significant task:

```text
UNDERSTAND
    |
INSPECT
    |
ANALYZE
    |
PLAN
    |
IMPLEMENT
    |
TEST
    |
REVIEW
    |
DOCUMENT
```

## Before Significant Changes

Output:

```text
## Understanding

## Current Architecture

## Problem

## Proposed Solution

## Files Affected

## Database Changes

## Security Considerations

## Testing Plan
```

## After Implementation

Output:

```text
## Changes Made

## Tests Performed

## Result

## Known Limitations

## Next Recommended Step
```

Do not claim tests were run if they were not actually run.

Do not claim an API works if it has not been verified.

Do not invent undocumented provider capabilities.

---

# 68. Immediate Project Roadmap

```text
START
  |
  v
Azuriom Validation
  |
  v
Implementation Gap Analysis
  |
  v
Architecture
  |
  v
Architecture
  |
  +---- ERD
  +---- DFD
  +---- Sequence Diagram
  +---- API Specification
  +---- Security Model
  |
  v
Infrastructure
  |
  v
MVP Development
  |
  +---- Account
  +---- Minecraft Linking
  +---- Store
  +---- Payment
  +---- Delivery
  +---- Admin
  |
  v
Testing
  |
  v
Security Review
  |
  v
Production
```

---

# 69. Final MVP Definition (v2.0 — updated scope)

MVP for this build phase is complete when a player can:

```text
Visit Website
     |
     v
Create Account
     |
     v
Link Minecraft Account
(Java Online / Java Offline / Bedrock if enabled)
     |
     v
Read Wiki (features & commands)
     |
     v
Browse Store
     |
     v
Select Product
     |
     v
Checkout
     |
     v
Pay (test/manual payment method — Midtrans not yet live, see §22)
     |
     v
Payment Verified
     |
     v
Order PAID
     |
     v
Delivery Created
     |
     v
Minecraft Reward Delivered
     |
     v
Delivery Recorded
```

Note: the end-to-end order → payment → delivery flow must work completely using the test/manual payment method from §22 Phase 5. Wiring the real Midtrans API into the same flow later must not require changing this flow — only swapping which `PaymentMethod` class is active.

And an administrator can:

```text
Login
  |
  +---- Manage Products
  +---- Manage Wiki Content
  +---- View Users
  +---- View Orders
  +---- View Payments
  +---- View Deliveries
  +---- Retry Failed Delivery
  +---- View Audit Logs
```

---

# 70. Authentication Decision Summary (v2.2)

```text
Website Account
       |
       +----------------------------+
       |                            |
       v                            v
Java Identity                 Bedrock Identity
       |                            |
       +---- Online                 +---- Floodgate
       |
       +---- Offline
```

## Preferred Implementation

```text
Custom Minecraft Link/Auth Plugin
              |
              +---- Website API
              |
              +---- Java Online
              |
              +---- Java Offline
              |
              +---- Geyser/Floodgate
```

AuthMeReloaded is treated as an **existing-server compatibility option**, not the architectural source of truth for website identity.

If the actual server already relies on AuthMeReloaded, the AI agent must first inspect its configuration and test coexistence with Geyser/Floodgate. Geyser documentation explicitly warns about offline authenticator plugins in Floodgate setups. citeturn745244search5

The custom plugin should own the website verification/link protocol regardless of which login/authentication plugin is used for gameplay.

---

# 70. Final Architectural Principle

The most important design principle of this project is:

```text
PAYMENT
   !=
DELIVERY
```

and:

```text
WEBSITE
   !=
MINECRAFT SERVER
```

The website owns:

- users;
- products;
- orders;
- payments;
- delivery jobs;
- transaction/delivery history.

Minecraft owns:

- player state;
- gameplay;
- permissions;
- in-game execution.

The integration layer connects them.

This separation allows the system to survive:

- payment provider changes;
- Minecraft server migration;
- Pterodactyl migration;
- VPS migration;
- addition of new Minecraft servers;
- addition of new payment providers;
- international expansion.

---

# 71. v2.2 Review Fix Summary

The following specification gaps were explicitly corrected in v2.1:

1. Midtrans scaffold vs acceptance criteria contradiction resolved.
2. Azuriom decision locked; old candidate language removed.
3. Minecraft link token persistence model added.
4. Product delivery rule persistence model added.
5. Historical delivery configuration snapshot added.
6. `deliveries.order_item_id` made the canonical relationship; redundant `order_id` removed from the delivery schema.
7. Payment event/webhook history separated from current payment state.
8. Order state transition matrix made explicit.
9. Carding mitigation wording made provider-agnostic for the current build phase.
10. API section changed from rigid endpoint requirements to capability contracts.
11. Roadmap no longer reopens the already-locked CMS decision.
12. Added explicit Java Online / Java Offline / Bedrock-Floodgate identity model.
13. Added Minecraft link-token and identity handling requirements for offline/cracked and Bedrock players.
14. Added AuthMeReloaded compatibility rule and custom-plugin preference for the website verification boundary.

## Final Implementation Position

```text
CMS
  -> Azuriom

Web Stack
  -> Laravel/PHP via Azuriom
  -> Nginx
  -> Database

Infrastructure
  -> ECO Memory-Optimized 2 GB
  -> Frankfurt
  -> Public IPv4
  -> Cloudflare edge/security layer

Payment — Current Build
  -> Manual/Test Payment
  -> Midtrans scaffold only

Payment — Next Integration Phase
  -> Midtrans live

International Payments
  -> Deferred

Minecraft
  -> Existing Pterodactyl-hosted server
  -> Custom authenticated integration/plugin

Architecture
  -> Modular monolith
```

## Ready-for-Implementation Rules

Before writing production code, the AI agent must:

- inspect the real Azuriom installation and installed plugins;
- inspect the actual Minecraft server and installed plugins;
- confirm the exact Minecraft command/permission mappings;
- create/validate database migrations;
- implement test/manual payment flow first;
- keep real payment credentials out of source control;
- verify all state transitions;
- test duplicate webhook and duplicate delivery behavior;
- test Minecraft offline/restart behavior;
- document any deviation from this PRD.


# END OF DOCUMENT
