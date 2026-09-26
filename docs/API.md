# Apexsions WebBridge & Web Platform API Reference

> **Platform:** Azuriom WebBridge (`Website/plugins/apexsions-bridge`)  
> **API Version:** 1.3.8 (Paper 26.2 / Java 21 LTS ↔ Azuriom Laravel 12)  
> **Base URL:** `https://web.apexsions.com/api/apexsions-bridge`  
> **Authentication:** `X-Apexsions-Key: <APEXSIONS_BRIDGE_KEY>` (Default: `apexsions_bridge_key_live_2026`)

---

## 1. Authentication & Security Policy

Setiap pemanggilan endpoint bridge dari game server Minecraft wajib menyertakan token keamanan valid:
- **Header:** `X-Apexsions-Key: apexsions_bridge_key_live_2026`
- **Alternatif Payload:** `"key": "apexsions_bridge_key_live_2026"`

Jika kunci tidak disediakan atau salah:
```json
{
  "status": "error",
  "message": "Unauthorized: Invalid or missing API key."
}
```
*HTTP Status:* `401 Unauthorized`

---

## 2. Server & Account Synchronization Endpoints

### A. Verify PIN & Link Account
Memverifikasi 6-digit PIN yang diterbitkan oleh portal web `/link` ketika pemain mengetik `/link <code>` di dalam game.

- **Route:** `POST /api/apexsions-bridge/verify`
- **Request Body:**
  ```json
  {
    "pin": "123456",
    "uuid": "069a79f4-44e9-4726-a5be-fca90e38aaf5",
    "username": "Nueeva",
    "platform": "java"
  }
  ```
- **Responses:**
  - `200 OK`:
    ```json
    {
      "status": "success",
      "message": "Account linked successfully to Nueeva.",
      "user": {
        "id": 1,
        "name": "Rifqi"
      }
    }
    ```
  - `422 Unprocessable Entity`:
    ```json
    {
      "status": "error",
      "message": "Invalid or expired PIN code."
    }
    ```

---

### B. Sync Player Stats & Lifecycle
Gateway authoritatif untuk pendaftaran pertama kali (*first join*) dan pembaruan telemetri pemain (Level, XP, Saldo, Kerajaan, Kasta). Dilengkapi cache throttle 60 detik untuk pembersihan rank trial dan cooldown 5 menit untuk pengiriman perintah sinkronisasi rank LuckPerms.

- **Route:** `POST /api/apexsions-bridge/sync-player`
- **Request Body:**
  ```json
  {
    "uuid": "069a79f4-44e9-4726-a5be-fca90e38aaf5",
    "username": "Nueeva",
    "level": 45,
    "xp": 12850,
    "balance_rupiah": 250000.0,
    "balance_diamond": 120,
    "kingdom": "ZENITHAR",
    "rank": "sions",
    "is_online": true
  }
  ```
- **Responses:**
  - `200 OK`:
    ```json
    {
      "status": "success",
      "message": "Player data synchronized successfully.",
      "account_id": 1
    }
    ```

---

### C. Server Telemetry Heartbeat
Pelaporan berkala (setiap 30-60 detik) kondisi kesehatan runtime Paper Minecraft.

- **Route:** `POST /api/apexsions-bridge/heartbeat`
- **Request Body:**
  ```json
  {
    "tps": 20.0,
    "ram_used_mb": 4096,
    "ram_max_mb": 8192,
    "online_players": 18,
    "max_players": 100
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "status": "success",
    "recorded_at": "2026-09-23T15:30:00Z"
  }
  ```

---

## 3. Delivery Queue & Command Dispatching

### A. Poll Pending Deliveries
Diambil oleh daemon `WebBridgeService` in-game. Endpoint ini diamankan dengan `DB::transaction()` + `lockForUpdate()` dan batas sewa (*lease*) 60 detik untuk mencegah duplikasi eksekusi multi-server, serta auto-fail (*dead-lettering*) 7 hari untuk antrean yang macet.

- **Route:** `GET /api/apexsions-bridge/deliveries/pending`
- **Response (`200 OK`):**
  ```json
  {
    "status": "success",
    "count": 1,
    "deliveries": [
      {
        "id": 105,
        "action_id": "act_68e2f94a_20260923",
        "player_uuid": "069a79f4-44e9-4726-a5be-fca90e38aaf5",
        "player_name": "Nueeva",
        "commands": [
          "lp user Nueeva parent set sions",
          "eco give Nueeva 300000"
        ],
        "status": "PROCESSING",
        "created_at": "2026-09-23T15:00:00Z"
      }
    ]
  }
  ```

---

### B. Update Delivery Status
Melaporkan hasil eksekusi perintah konsol in-game kembali ke database pusat.

- **Route:** `POST /api/apexsions-bridge/deliveries/{id}/status`
- **Request Body:**
  ```json
  {
    "status": "DELIVERED",
    "error_reason": null
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "status": "success",
    "delivery_id": 105,
    "current_status": "DELIVERED"
  }
  ```

---

## 4. In-Game Ingestion Endpoints

### A. Unified Audit Log Ingestion
- **Route:** `POST /api/apexsions-bridge/audit/log`
- **Request Body:**
  ```json
  {
    "actor_type": "STAFF",
    "actor_name": "Friell",
    "action": "BAN_PLAYER",
    "target": "BadActor123",
    "details": "Fly hack & Speed exploit detected in Zenithar capital",
    "ip_address": "127.0.0.1"
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "status": "success",
    "log_id": 42
  }
  ```

---

### B. Land Claims Snapshot Sync
Sinkronisasi penuh kedaulatan wilayah tanah (`/claim`) dengan proteksi penghapusan snapshot kosong.

- **Route:** `POST /api/apexsions-bridge/claims/sync-all`
- **Request Body:**
  ```json
  {
    "claims": [
      {
        "claim_id": "c_zenithar_01",
        "owner_uuid": "069a79f4-44e9-4726-a5be-fca90e38aaf5",
        "kingdom": "ZENITHAR",
        "world": "world",
        "x1": 100,
        "z1": 100,
        "x2": 150,
        "z2": 150,
        "upkeep_daily": 500.0,
        "treasury_balance": 15000.0
      }
    ]
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "status": "success",
    "synced_count": 1
  }
  ```

---

### C. Player Bounties Snapshot Sync
Sinkronisasi kontrak buronan (`/bounty`).

- **Route:** `POST /api/apexsions-bridge/bounties/sync-all`
- **Request Body:**
  ```json
  {
    "bounties": [
      {
        "target_uuid": "550e8400-e29b-41d4-a716-446655440000",
        "target_name": "OutlawJoe",
        "issuer_name": "SheriffBob",
        "reward_amount": 50000.0,
        "currency": "RUPIAH"
      }
    ]
  }
  ```
- **Response (`200 OK`):**
  ```json
  {
    "status": "success",
    "synced_count": 1
  }
  ```

---

### D. Dynamic Market Shop Configuration
Penyedia konfigurasi pasar dinamis untuk server game.

- **Route:** `GET /api/apexsions-bridge/shop/config`
- **Response (`200 OK`):**
  ```json
  {
    "status": "success",
    "version": "1.0",
    "base_sell_ratio": 0.2,
    "solterra_sell_ratio": 0.3,
    "diamond_sell_price": 250,
    "taxes": {
      "zenithar": 0.18,
      "solterra": 0.20,
      "sylvamoor": 0.15
    },
    "categories": ["ores", "farming", "mob_drops", "blocks", "dyes", "food"]
  }
  ```

---

## 5. Public Web Endpoints (Read-Only)

| Endpoint | Method | Keterangan | Auth |
| :--- | :---: | :--- | :---: |
| `/leaderboard` | `GET` | Papan peringkat publik Level & Perbendaharaan Rupiah | Publik |
| `/player/{identifier}` | `GET` | Inspeksi profil 360 publik (strictly read-only, via UUID atau DB ID) | Publik |
| `/server-map` | `GET` | Peta dunia real-time BlueMap | Publik |
| `/feed` | `GET` | Siaran event dunia real-time | Publik |
| `/bounties` | `GET` | Kontrak buronan pemain aktif | Publik |
| `/vote` | `GET` | Halaman vote server berhadiah Crate Keys | Publik |
