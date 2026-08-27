# 📖 Technical Documentation — ApexsionsChat Plugin

Dokumentasi teknis komprehensif untuk pengembang dan arsitek sistem yang bekerja pada **ApexsionsChat** (Minecraft Paper 26.2, Java 21+).

---

## 📑 Daftar Isi

1. [Arsitektur & Diagram Integrasi](#1-arsitektur--diagram-integrasi)
2. [Struktur Paket & File Konfigurasi](#2-struktur-paket--file-konfigurasi)
3. [Database & Skema Tabel (SQLite & PostgreSQL)](#3-database--skema-tabel-sqlite--postgresql)
4. [Sistem Moderasi & Filter Pipeline](#4-sistem-moderasi--filter-pipeline)
5. [Sistem Saluran Obrolan (Channels)](#5-sistem-saluran-obrolan-channels)
6. [Chat Games (Unscramble & Quick Math)](#6-chat-games-unscramble--quick-math)
7. [Item Showcase & Safe GUI Display](#7-item-showcase--safe-gui-display)
8. [Sistem Offline Mail & Book Delivery](#8-sistem-offline-mail--book-delivery)
9. [Sistem Laporan & Staff GUI](#9-sistem-laporan--staff-gui)
10. [Public API (`ApexsionsChatAPI`)](#10-public-api-apexsionschatapi)

---

## 1. Arsitektur & Diagram Integrasi

```text
┌────────────────────────────────────────────────────────┐
│                      ApexsionsCore                     │
│  (Region, Level, XP, Level Titles, Rewards, Ranks)     │
└───────────────────────────┬────────────────────────────┘
                            │ Public API (ApexsionsCoreAPI)
                            ▼
┌────────────────────────────────────────────────────────┐
│                      ApexsionsChat                     │
│  - Chat Formatting & Channels (Global, Kingdom, Staff) │
│  - Mentions (@Player, @all) & Actionbar alerts         │
│  - Moderation Pipeline (Spam, Ads, Profanity, Hate)    │
│  - Reports & Staff Moderation GUI                      │
│  - Item Showcase & Interactive GUI                     │
│  - Chat Games (Unscramble, Quick Math)                 │
│  - Scheduled Announcements                             │
│  - Offline Mail System & Book Reader                   │
└───────────────────────────┬────────────────────────────┘
                            │
             ┌──────────────┴──────────────┐
             ▼                             ▼
       LuckPerms / Vault             PlaceholderAPI
```

---

## 2. Struktur Paket & File Konfigurasi

### Package Namespace: `com.yourserver.apexsionschat`

| Package | Tanggung Jawab Utama |
|---|---|
| `.api` | Interface publik `ApexsionsChatAPI` dan service locator `ApexsionsChatProvider`. |
| `.announcement` | `AnnouncementManager` pengelola siaran terjadwal otomatis. |
| `.channel` | `ChatChannel`, `ChannelManager`, `GlobalChannel`, `KingdomChannel`, `StaffChannel`. |
| `.chat` | `ChatListener` (AsyncChatEvent), `ChatFormatter`, `MentionParser`, `ItemShowcaseService`. |
| `.command` | Executor dan tab-completer untuk `/channel`, `/showitem`, `/report`, `/reports`, `/mail`, `/apexsionschat`. |
| `.config` | `ChatConfigManager` pengelola 7 file konfigurasi modular. |
| `.database` | `ChatDatabaseManager`, `ReportRepository`, `MailRepository`, `ModerationLogRepository`. |
| `.game` | `ChatGame`, `ChatGameManager`, `UnscrambleGame`, `QuickMathGame`. |
| `.gui` | `BaseChatGUI`, `GUIListener`, `ItemShowcaseGUI`, `ReportListGUI`, `ReportDetailGUI`, `MailListGUI`, `MailDetailGUI`. |
| `.integration` | `ApexsionsCoreHook`, `LuckPermsHook`, `VaultHook`, `PlaceholderApiHook`. |
| `.model` | `Report`, `ReportStatus`, `Mail`, `ModerationLogEntry`. |
| `.moderation` | `ModerationEngine`, `SpamChecker`, `AdvertisementChecker`, `ProfanityChecker`, `HateSpeechChecker`. |

---

## 3. Database & Skema Tabel (SQLite & PostgreSQL)

### 1. `apexsions_reports`
- `report_id` (INTEGER PRIMARY KEY AUTOINCREMENT)
- `reporter_uuid` (VARCHAR(36))
- `reporter_name` (VARCHAR(32))
- `reported_uuid` (VARCHAR(36))
- `reported_name` (VARCHAR(32))
- `reason` (TEXT)
- `server` (VARCHAR(64))
- `world` (VARCHAR(64))
- `timestamp` (BIGINT)
- `status` (`OPEN`, `REVIEWING`, `RESOLVED`, `DISMISSED`)
- `moderator_uuid` (VARCHAR(36))
- `moderator_name` (VARCHAR(32))
- `resolution` (TEXT)
- `resolved_at` (BIGINT)

### 2. `apexsions_mail`
- `mail_id` (INTEGER PRIMARY KEY AUTOINCREMENT)
- `sender_uuid` (VARCHAR(36))
- `sender_name` (VARCHAR(32))
- `recipient_uuid` (VARCHAR(36))
- `recipient_name` (VARCHAR(32))
- `subject` (VARCHAR(64))
- `body` (TEXT)
- `created_at` (BIGINT)
- `read_at` (BIGINT)
- `is_read` (BOOLEAN)
- `is_archived` (BOOLEAN)

### 3. `apexsions_moderation_logs`
- `event_id` (INTEGER PRIMARY KEY AUTOINCREMENT)
- `player_uuid` (VARCHAR(36))
- `player_name` (VARCHAR(32))
- `message_snippet` (TEXT)
- `channel` (VARCHAR(32))
- `rule_violated` (VARCHAR(64))
- `action_taken` (VARCHAR(32))
- `timestamp` (BIGINT)
