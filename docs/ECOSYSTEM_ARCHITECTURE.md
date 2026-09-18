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
4. **Auto-Respawn Ibukota Kerajaan**: Pemain yang gugur langsung direlokasi ke titik pusat ibukota kerajaannya (sinkron dengan marker BlueMap) alih-alih terlempar ke lobby, mempertahankan kesinambungan pertahanan teritorial.

---

## 📜 5. Kesinambungan Lore & Arsitektur Sistem (The Narrative Mechanics Matrix)

Seluruh arsitektur plugin di Apexsions dibangun di atas pondasi narasi kanonik **Kekaisaran Kuno Sions**:

```
                              ┌──────────────────────────────────┐
                              │     RERUNTUHAN KEKAISARAN SIONS  │
                              │ (Bencana Energi Dark Dimension)  │
                              └─────────────────┬────────────────┘
                                                │ Terbelah Menjadi 3 Peradaban
                  ┌─────────────────────────────┼─────────────────────────────┐
                  ▼                             ▼                             ▼
       ┌─────────────────────┐       ┌─────────────────────┐       ┌─────────────────────┐
       │      ZENITHAR       │       │      SOLTERRA       │       │      SYLVAMOOR      │
       │  (Arah Timur/Zenith)│       │    (Arah Selatan)   │       │    (Arah Barat)     │
       │ Bangsawan Dinasti,  │       │ Magician Tempur &   │       │ Kaum Pekerja,       │
       │ Kavaleri Kehormatan │       │ Penempa Gunung Api  │       │ Pemburu & Petani    │
       └──────────┬──────────┘       └──────────┬──────────┘       └──────────┬──────────┘
                  │                             │                             │
                  └─────────────────────────────┼─────────────────────────────┘
                                                ▼
                              ┌──────────────────────────────────┐
                              │     THE WILDERNESS & SANCTUM     │
                              │(Monster Berlevel, Mythic Bosses, │
                              │    Peninggalan Relik Sions)      │
                              └──────────────────────────────────┘
```

| Modul Plugin | Kaitan Erat dengan Lore & Dunia Game |
| :--- | :--- |
| **`ApexsionsCore`** | Otoritas teritorial 3 kerajaan pecahan Sions (*Zenithar, Solterra, Sylvamoor*). Auto-respawn di ibukota peradaban masing-masing. Deklarasi perang (*Kingdom War*) sebagai perebutan legitimasi tahta Sions. |
| **`ApexsionsEconomy`** | Mata uang ganda `Rupiah` & `Diamond` sebagai mata uang perdagangan peradaban. Pajak transportasi lintas-kerajaan membatasi penyelundupan antar-wilayah. Relik reruntuhan Sions menjadi komoditas pasar lelang (`/ah`) bernilai tinggi. |
| **`ApexsionsShop`** | Fluktuasi dinamis berbasis iklim dan keunggulan regional peradaban (Solterra unggul pangan/pewarna, Zenithar logam mulia/ores, Sylvamoor hasil hutan/drops alam). |
| **`ApexsionsBattlepass`** | Ekspedisi dan misi musiman untuk mengungkap misteri masa lalu Sions, penjelajahan belantara Wilderness, dan perburuan monster berlevel. |
| **`ApexsionsChat`** | Saluran obrolan terisolasi (`/kc` Kingdom Chat) untuk koordinasi internal faksi dan jalur diplomasi global (`/g`). |
| **`ApexsionsMedia`** | Menampilkan lambang kebesaran panji dinasti 3 kerajaan dan ornamen mistis era Sions di benteng-benteng kota. |
| **`ApexsionsCustomEnchants`**| Sihir penempaan kuno yang diwarisi dari perpustakaan rahasia era kekaisaran Sions sebelum keruntuhannya. |

---

## ⚔️ 6. Integrasi Ekosistem PVE: Reruntuhan Kuno Sions (MythicMobs & Relic Loop)

Reruntuhan Kekaisaran Sions di tengah Wilderness terintegrasi secara modular dengan sistem ekonomi dan leveling Apexsions:

1. **Level-Scaled Wilderness PVE**:
   - Monster alam liar berlevel (Level 10-30) memberikan drop exp yang terhubung dengan **13 XP Sources** `ApexsionsCore` (sumber `MOB_KILL`).
2. **Dungeon Boss Encounters (The Forbidden Sanctum)**:
   - Area tahta reruntuhan dihuni oleh World Boss multi-fase (*Emperor Valerius, The Void-Touched* Lv. 100) dan penjaga elit (*Sions Void Assassin*, *Ruin Sentinel*).
3. **Loop Sirkulasi Relik Kuno**:
   $$\text{PVE Boss Kill} \longrightarrow \text{Drop Pecahan Relik Sions / Void Core} \longrightarrow \text{Pasar Lelang / Toko Dinamis} \longrightarrow \text{Apex Coins / Hadiah Level}$$

---

## 🗺️ 7. Master Roadmap & Visi Jangka Panjang (Long-Term Evolution)

Arsitektur sistem Apexsions dirancang untuk bertumbuh melalui 4 fase evolusi berkelanjutan:

```
[FASE 1: Fondasi Kerajaan] ──► [FASE 2: Raid & Relik Sions] ──► [FASE 3: Pengepungan Benteng] ──► [FASE 4: Web Live Sync]
  • 6 Modul Inti Aktif          • World Boss MythicMobs          • Kingdom Castle Siege          • WebBridge v2 Real-time
  • Proteksi Teritorial PvP     • Loop Relik & Toko Kuno        • Perebutan Outpost Wilayah     • Dynamic Web Territory Map
  • Auto-Respawn Ibukota        • Event Bencana Void             • Pajak Wilayah Taklukan        • Webstore Instant Gateway
```

### Rincian Fase:
- **Fase 1 (Fondasi & Kedaulatan Teritorial — Selesai/Aktif)**: Stabilitas 7 plugin, sinkronisasi poligon BlueMap, formula 13 XP leveling (1-100), proteksi friendly-fire, dual-currency atomic engine, dan auto-respawn ibukota terikat BlueMap.
- **Fase 2 (The Sions Cataclysm & World Raids — Saat Ini/Aktif)**: Integrasi MythicMobs di Reruntuhan Sions, mekanik serangan telegraphed, drop item legendaris (*Valerius Voidblade*, *Crown of Sions*), serta sirkulasi relik di pasar lelang (`/ah`).
- **Fase 3 (Pengepungan Benteng & Perluasan Wilayah — Rencana Menengah)**: Fitur *Kingdom Outpost Siege* di mana kerajaan dapat memperebutkan benteng perbatasan di Wilderness untuk memperluas batas klaim teritorial dan memungut pajak jalur dagang.
- **Fase 4 (Sinkronisasi Web & Jaringan Terpadu — Rencana Jangka Panjang)**: Peluncuran WebBridge v2 dengan web-socket real-time untuk menampilkan kontrol wilayah di portal web, live battle ranking di situs web, dan integrasi payment gateway otomatis saat perizinan legal selesai.

