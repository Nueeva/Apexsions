# Apexsions Plugin Suite — Master Ecosystem Architecture

Dokumentasi arsitektur terpadu yang merangkum interaksi antar-plugin, kontrak Service Provider Interface (SPI), alur event data, arsitektur database HikariCP, dan matriks integrasi ekosistem **Apexsions**.

---

## 🏛️ 1. Diagram Keterhubungan 6 Plugin Suite

```
                            ┌────────────────────────┐
                            │     ApexsionsCore      │
                            │  (Kingdom & Leveling)  │
                            └───────────┬────────────┘
                                        │
         ┌──────────────────────────────┼──────────────────────────────┐
         ▼                              ▼                              ▼
┌──────────────────┐          ┌───────────────────┐          ┌───────────────────┐
│  ApexsionsChat   │          │ ApexsionsEconomy  │          │ApexsionsBattlepass│
│ (Chat & Mod Sec) │          │ (AH, Trade, Pay)  │          │ (Quests & Passes) │
└──────────────────┘          └─────────┬─────────┘          └───────────────────┘
                                        │
                    ┌───────────────────┴───────────────────┐
                    ▼                                       ▼
          ┌───────────────────┐                   ┌───────────────────┐
          │  ApexsionsShop    │                   │  ApexsionsMedia   │
          │ (Dynamic Markets) │                   │(Interactive Visual│
          └───────────────────┘                   └───────────────────┘
```

---

## 🧩 2. Service Provider Interface (SPI) Contracts

Setiap plugin mengekspos public API melalui pattern singleton provider yang aman, decoupled, dan thread-safe:

### 1. `ApexsionsCoreProvider.get()` $\to$ `ApexsionsCoreAPI`
- `getPlayerRegionKey(UUID uuid)`: Mendapatkan kunci kerajaan pemain (`"ZENITHAR"`, `"SOLTERRA"`, `"SYLVAMOOR"`, `"NONE"`).
- `getRegion(UUID uuid)`: Mendapatkan objek teritorial kerajaan pemain.
- `getLevel(UUID uuid)` & `getXp(UUID uuid)`: Mengambil level progresi (1-100) dan total poin XP pemain.
- `getLevelTitle(UUID uuid)`: Resolusi gelar prestise pemain berdasarkan level dan kerajaannya.
- `getKingdomAt(Location location)`: Mendeteksi wilayah kerajaan yang melingkupi koordinat spasial (via BlueMap bounds).
- `isInKingdomTerritory(Player player, Region region)`: Memeriksa apakah pemain berdiri di dalam wilayah kerajaan tertentu.
- `addXp(UUID uuid, long amount, XpSource source)`: Menambahkan XP dengan mencatat sumber aktivitasnya.
- `setLevel(UUID uuid, int level)` & `setXp(UUID uuid, long xp)`: Mengatur level/XP pemain secara terisolasi.
- `getAdminHubManager()` & `registerAdminModule(AdminModule module)`: Mendaftarkan kartu modul ke Master Admin Hub (`/admingui`).
- `getPlayerChatProfile(UUID uuid)`: Mengambil DTO profil chat untuk tooltip hover, tablist, dan ID-card.

### 2. `ApexsionsChatAPI`
- `sendMail(UUID senderUuid, String senderName, UUID recipientUuid, String recipientName, String subject, String body)`: Mengirim surat offline asinkron.
- `getUnreadMailCount(UUID playerUuid)`: Menghitung jumlah surat masuk yang belum dibaca.
- `createReport(Report report)`: Mendaftarkan tiket laporan pelanggaran aturan ke antrean staf.
- `getPlayerChannel(Player player)` & `setPlayerChannel(Player player, String channelId)`: Manajemen channel obrolan aktif.
- `broadcastAnnouncement(String miniMessageContent)`: Menyiarkan pengumuman server berbasis MiniMessage.

### 3. `ApexsionsEconomyAPI`
- `getBalance(UUID uuid, String currencyId)`: Mengambil saldo pemain (`"rupiah"`, `"diamond"`).
- `has(UUID uuid, String currencyId, double amount)`: Validasi kecukupan saldo non-blocking.
- `deposit(UUID uuid, String currencyId, double amount)`: Penambahan saldo atomik dan concurrency-safe.
- `withdraw(UUID uuid, String currencyId, double amount)`: Pengurangan saldo atomik dengan validasi anti-minus.
- `transfer(UUID senderUuid, UUID receiverUuid, String currencyId, double amount)`: Transfer saldo antar-pemain.
- `format(double amount, String currencyId)`: Format tampilan cerdas (contoh: `Rp 1,5 Jt`, `100 ♦`).
- `formatCompact(double amount)`: Format ringkas angka besar (`K`, `Jt`, `M`, `T`).

### 4. `ApexsionsBattlepassAPI`
- `getCurrentSeasonId()`: Mendapatkan ID Season yang sedang aktif.
- `getPlayerTier(UUID uuid)` & `getPlayerXp(UUID uuid)`: Mendapatkan tier level dan progress BP-XP pemain.
- `addPlayerXp(UUID uuid, int xp)`: Menambahkan poin BP-XP ke pemain.
- `hasPremiumPass(UUID uuid)` & `hasPass(UUID uuid, String passId)`: Memeriksa kepemilikan tier pass.
- `getPlayerPoints(UUID uuid)`, `addPlayerPoints(UUID uuid, int points)`, `removePlayerPoints(UUID uuid, int points)`: Manajemen koin/poin BattlePass.

### 5. `ApexsionsShopProvider.get()` $\to$ `ApexsionsShopAPI`
- `calculateBuyPrice(ShopItem item, Player player, int quantity)`: Kalkulasi harga beli dinamis setelah multiplier cuaca, spesialisasi kerajaan, kurva pasokan, dan batas clamping (50%-200%).
- `calculateSellPrice(ShopItem item, Player player, int quantity)`: Kalkulasi harga jual dinamis (rasio dasar 20%).
- `getPlayerKingdomTaxPercent(Player player)`: Mengambil tarif pajak kerajaan pembeli.
- `openShop(Player player)`, `openCategory(Player player, ShopCategory category)`, `openSellGui(Player player)`: Navigasi GUI pasar.

### 6. `ApexsionsMediaProvider.get()` $\to$ `ApexsionsMediaAPI`
- `getBanner(String id)` & `getAllBanners()`: Mengambil data instance banner media aktif.
- `createBanner(String id, Location location, BlockFace facing, int width, int height, String source, String linkUrl, ClickMode mode)`: Membuat instance banner gambar baru.
- `deleteBanner(String id)`: Menghapus banner dan membersihkan entity map frame terkait.

---

## 🗄️ 3. Arsitektur Basis Data HikariCP & Multi-Engine

Seluruh 6 plugin mendukung sistem penyimpanan ganda (*Dual Database Engine*):
- **SQLite (Development / Standalone)**: File database `.db` lokal cepat dan tanpa overhead jaringan.
- **PostgreSQL (Production Enterprise)**: Mendukung multi-server / network berskala besar dengan connection pooling HikariCP 6.2.1 dan driver PostgreSQL 42.7.5.

---

## 🛡️ 4. Matriks Integrasi Keamanan Transaksi & Wilayah

1. **Proteksi Duplikasi & Atomic Locking**: Seluruh transaksi keuangan, lelang escrow, barter trade, dan penjualan toko menggunakan atomic queries dan locking untuk mencegah eksploitasi race condition.
2. **Proteksi Teritorial PvP**: `ApexsionsCore` membatalkan 100% friendly-fire sesama warga kerajaan di dalam batas teritorial klaim.
3. **Combat Tagging**: Mencegah pelarian instan via teleportasi selama 15 detik saat terlibat perkelahian PvP.
