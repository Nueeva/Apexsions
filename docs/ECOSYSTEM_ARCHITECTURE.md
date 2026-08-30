# Apexsions Plugin Suite — Master Ecosystem Architecture

Dokumentasi arsitektur terpadu yang menjelaskan pola integrasi antar-plugin, alur event data, Service Provider Interface (SPI), dan koneksi database HikariCP di server **Apexsions**.

---

## 🏛️ 1. Diagram Keterhubungan 6 Plugin

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

## 🧩 2. Service Provider Interface (SPI)

Setiap plugin mengekspos API publik melalui singleton provider pattern yang aman dan decoupled:

1. **`ApexsionsCoreProvider.get()` $\to$ `ApexsionsCoreAPI`**:
   - `getKingdom(UUID playerId)`: Mendapatkan nama kerajaan pemain (`Zenithar`, `Solterra`, `Sylvamoor`).
   - `getLevel(UUID playerId)`: Mengambil level progresi pemain (1-100).
   - `getXp(UUID playerId)`: Mengambil total poin XP pemain.
   - `isInTerritory(Location loc)`: Memeriksa apakah lokasi berada di dalam batas poligon kerajaan.
   - `isSameKingdom(UUID p1, UUID p2)`: Memvalidasi apakah dua pemain berada di satu kerajaan.

2. **`ApexsionsEconomyAPI`**:
   - `has(UUID playerId, String currency, double amount)`: Cek saldo non-blocking.
   - `withdraw(UUID playerId, String currency, double amount)`: Pengurangan saldo atomik.
   - `deposit(UUID playerId, String currency, double amount)`: Penambahan saldo atomik.
   - `format(double amount, String currency)`: Format tampilan cerdas (misal: `Rp 1,5 Jt`).

3. **`ApexsionsShopProvider.get()` $\to$ `ApexsionsShopAPI`**:
   - `calculateBuyPrice(Material material, Player player)`: Harga beli dinamis setelah cuaca & diskon kerajaan.
   - `calculateSellPrice(Material material, Player player)`: Harga jual dinamis.

---

## 🗄️ 3. Pooling Koneksi Database (HikariCP)

Seluruh plugin menggunakan sistem database ganda (*Dual Database Engine*):
- **SQLite (Development / Single Instance)**: File basis data lokal `.db` cepat dan tanpa konfigurasi server.
- **PostgreSQL (Production Enterprise)**: Mendukung multi-server / BungeeCord network dengan connection pooling HikariCP berkemampuan tinggi.
