# AGENTS.md

# Gemini & Agent Development Guidelines — Apexsions Plugin Suite

> **Repository:** `Nueeva/Apexsions`
> **Primary Branch:** `main`
> **Project Type:** Minecraft Server Plugin Suite
> **Architecture:** Multi-plugin modular ecosystem
> **Brand:** `Apexsions`

Dokumen ini merupakan **operational contract** untuk AI coding agent yang bekerja pada ekosistem Apexsions.

Agent wajib mengikuti aturan, workflow, arsitektur, konvensi, dan batasan dalam dokumen ini. Jika sebuah task dapat diselesaikan dengan perubahan kecil dan aman, jangan mengubah arsitektur secara berlebihan.

---

# 00. Mission & Core Principles

Tujuan agent adalah:

1. Memahami existing code sebelum melakukan perubahan.
2. Mempertahankan functionality yang sudah bekerja.
3. Menghindari duplicate implementation.
4. Menjaga kompatibilitas antar-plugin.
5. Menjaga integritas data dan transaksi.
6. Menjaga keamanan server.
7. Meminimalkan perubahan yang tidak diperlukan.
8. Melakukan validasi nyata, bukan hanya mengandalkan compilation.
9. Menjaga repository tetap aman untuk multi-developer workflow.
10. Menghasilkan perubahan yang maintainable dan dapat dikembangkan.

Prinsip utama:

```text
Inspect
  ↓
Synchronize
  ↓
Understand
  ↓
Plan
  ↓
Implement
  ↓
Validate
  ↓
Review Diff
  ↓
Synchronize Again
  ↓
Commit
  ↓
Push only when authorized
```

Gunakan prinsip:

> **Search → Understand → Reuse → Extend → Create only if necessary.**

Jangan gunakan:

> **Assume → Rewrite → Hope.**

---

# 01. Rule Priority

Jika terdapat konflik antar-instruksi, gunakan prioritas berikut:

1. System/platform safety requirements.
2. Repository safety dan data integrity.
3. Explicit user task.
4. Dokumen `GEMINI.md`.
5. Existing architecture dan repository conventions.
6. Developer convenience.

Jangan mengorbankan data, keamanan, atau integritas repository hanya untuk menyelesaikan task lebih cepat.

---

# 02. Agent Operating Protocol

Setiap pekerjaan wajib mengikuti fase berikut.

## Phase 0 — Understand

Sebelum mengubah kode:

1. Baca `GEMINI.md`.
2. Baca `AGENTS.md` jika tersedia.
3. Baca dokumentasi relevan.
4. Identifikasi plugin yang terdampak.
5. Identifikasi dependency antar-plugin.
6. Identifikasi existing implementation.

Agent harus mengetahui:

```text
Apa yang sudah bekerja?
Apa yang rusak?
Apa yang duplicated?
Apa yang harus diubah?
Apa yang harus tetap untouched?
```

---

## Phase 1 — Synchronize

Pastikan berada di repository root.

Jalankan:

```powershell
git status --short
git fetch origin
git log HEAD..origin/main --oneline
```

Jika `origin/main` memiliki commit baru:

### Working tree bersih

Gunakan:

```powershell
git pull --ff-only origin main
```

### Working tree memiliki perubahan

Jangan otomatis:

```text
git reset --hard
git clean -fd
git restore
git checkout -- <file>
git stash
```

Identifikasi terlebih dahulu perubahan lokal dan perubahan remote.

Jika sinkronisasi aman tidak dapat dilakukan tanpa risiko kehilangan pekerjaan developer, **jangan mengambil tindakan destruktif otomatis**.

---

# 03. Strict Workspace Boundary

Agent hanya boleh bekerja di dalam repository/project root yang sedang ditugaskan.

DILARANG:

* membaca project lain yang tidak relevan;
* menyalin file dari project lain;
* memodifikasi server eksternal;
* memodifikasi folder Minecraft server di luar repository;
* mengakses credential atau secret di luar scope task;
* memindahkan hasil build ke folder server eksternal;
* mengubah konfigurasi sistem operasi tanpa kebutuhan task.

Seluruh:

```text
source
build output
JAR
configuration
generated artifacts
test artifacts
```

harus tetap berada di dalam repository/project root kecuali user secara eksplisit meminta lokasi lain.

---

# 04. Existing Code First

Sebelum membuat:

```text
class
interface
service
manager
repository
listener
command
event
utility
configuration
API
```

agent wajib mencari implementasi yang sudah ada.

Periksa:

```text
src/
plugin metadata
configuration
existing services
existing providers
existing events
existing repositories
existing utilities
existing APIs
```

Jika functionality serupa sudah tersedia:

1. gunakan implementation existing;
2. extend jika diperlukan;
3. refactor hanya jika memang diperlukan;
4. jangan membuat duplicate abstraction.

Contoh buruk:

```text
EconomyManager
EconomyService
EconomyHandler
EconomyController
EconomyHelper
EconomyUtils
```

untuk functionality yang sebenarnya sudah dimiliki satu service.

Abstraction baru harus mempunyai alasan teknis yang jelas.

---

# 05. Scope Control

Agent tidak boleh memperluas scope tanpa kebutuhan teknis.

Jika task adalah:

```text
Bugfix
```

jangan melakukan refactor besar unrelated.

Jika task adalah:

```text
UI change
```

jangan mengubah backend yang tidak berkaitan.

Jika task adalah:

```text
Command change
```

jangan mengganti seluruh command architecture kecuali diperlukan.

Perubahan tambahan hanya diperbolehkan apabila:

1. diperlukan agar task berhasil;
2. diperlukan untuk mencegah regression;
3. diperlukan untuk memperbaiki security/integrity issue yang ditemukan.

Perubahan tambahan harus dilaporkan.

---

# 06. Git & Multi-Developer Protocol

Repository dikerjakan oleh beberapa developer.

## Before Work

Selalu:

```powershell
git status --short
git fetch origin
git log HEAD..origin/main --oneline
```

Jangan mulai mengubah kode dari branch yang diketahui stale.

---

## During Work

Agent harus menghindari:

```text
git reset --hard
git clean -fd
git push --force
git push --force-with-lease
```

kecuali terdapat alasan teknis yang jelas dan tindakan tersebut memang diizinkan.

Jangan menghapus perubahan developer lain.

---

## Pre-Push Safety

Sebelum push:

```powershell
git fetch origin
git log HEAD..origin/main --oneline
git status --short
git diff
```

Jika remote berubah selama agent bekerja:

1. jangan force push;
2. integrasikan perubahan secara aman;
3. resolve conflict dengan mempertahankan intent kedua sisi;
4. ulangi build/test;
5. periksa `git diff` kembali.

---

## Commit Policy

Commit harus:

* atomic;
* memiliki message jelas;
* tidak mencampurkan unrelated changes;
* berisi source/config/documentation yang memang terkait.

Contoh:

```text
feat(core): add kingdom region resolver
fix(chat): prevent duplicate chat pipeline
refactor(economy): isolate transaction service
docs: update plugin architecture
```

---

## Push Policy

Commit dan push adalah dua tindakan berbeda.

Agent boleh membuat commit setelah validation berhasil.

Agent **tidak boleh menganggap push sebagai default**.

Push hanya dilakukan jika:

1. task secara eksplisit meminta push; atau
2. repository workflow secara eksplisit mengizinkan autonomous push.

Target branch harus diverifikasi sebelum push.

DILARANG force push ke shared branch.

---

# 07. Brand Rules

Nama resmi:

```text
Apexsions
```

Bukan:

```text
Apexions
```

Kesalahan spelling brand tidak boleh diperkenalkan pada:

* plugin name;
* documentation;
* package;
* command;
* configuration;
* log;
* artifact;
* API;
* generated output.

---

# 08. Official Plugin Suite

Enam plugin utama:

```text
ApexsionsCore
ApexsionsChat
ApexsionsEconomy
ApexsionsBattlepass
ApexsionsShop
ApexsionsMedia
```

## ApexsionsCore

```text
Kingdom
Region
Progression
Level
XP
Rewards
Kingdom War
Combat Tag
Multiverse
RTP
Rank Animation
Admin Inspector
```

## ApexsionsChat

```text
Chat
MiniMessage formatting
Chat Channels
Channel Settings GUI
Social Profile Hub
Staff Reports Desk
Moderation
Mentions
Mail
Chat notifications
```

## ApexsionsEconomy

```text
Multi-Currency
Atomic Transactions
Auction House
Escrow
Barter
```

## ApexsionsBattlepass

```text
Quests
Passes
Rotating Shop
Battlepass progression
```

## ApexsionsShop

```text
Kingdom Shop
Dynamic Market
Market Trends
Sell GUI
```

## ApexsionsMedia

```text
Interactive Banner
Interactive Logo
Raytrace Hover Glow
URL Actions
```

---

# 09. Java Package Convention

Gunakan:

```text
com.apexsions.core.*
com.apexsions.chat.*
com.apexsions.economy.*
com.apexsions.battlepass.*
com.apexsions.shop.*
com.apexsions.media.*
```

Jangan mencampurkan package antar-plugin.

Contoh:

```text
com.apexsions.chat.*
```

tidak boleh berisi business logic milik Economy.

---

# 10. Official Rank Hierarchy

Source of truth:

```text
ranks.yml
```

Official ranks:

| Rank        | Weight | Role                           |
| ----------- | -----: | ------------------------------ |
| `ancestor`  |    100 | The Ancestor / Owner / Founder |
| `warden`    |     90 | Head Staff / Admin             |
| `herald`    |     80 | Staff / Moderator              |
| `sions`     |     70 | Apex Donator                   |
| `emperor`   |     60 | Donator Tier 4                 |
| `sovereign` |     50 | Donator Tier 3                 |
| `archon`    |     40 | Donator Tier 2                 |
| `ascendant` |     30 | Donator Tier 1                 |
| `wanderer`  |     10 | Default / Warga Baru           |

Jangan membuat rank baru tanpa explicit requirement.

Jangan hardcode rank definition di banyak tempat.

---

# 11. Technology Baseline

## Language

```text
Java 21 LTS
```

Gunakan fitur Java 21 secara wajar.

## Minecraft Platform

```text
Paper API 1.21.4-R0.1-SNAPSHOT
Minecraft 1.21.4
```

Jangan mengubah target platform tanpa explicit project decision.

## Build

```text
Apache Maven 3.9.9
PowerShell build.ps1
```

Primary build:

```powershell
mvn clean package
```

Targeted build:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 Core
powershell -ExecutionPolicy Bypass -File .\build.ps1 Chat
powershell -ExecutionPolicy Bypass -File .\build.ps1 Economy
powershell -ExecutionPolicy Bypass -File .\build.ps1 Battlepass
powershell -ExecutionPolicy Bypass -File .\build.ps1 Shop
powershell -ExecutionPolicy Bypass -File .\build.ps1 Media
```

Full build:

```powershell
.\build.ps1 -all
```

Gunakan `-all` hanya jika seluruh plugin memang terdampak perubahan global.

---

# 12. Build Rule

Jika hanya 1–3 plugin berubah:

```text
Build only affected plugins.
```

Jangan menjalankan full multi-plugin build tanpa alasan.

Jika perubahan menyentuh:

```text
shared API
parent build configuration
cross-plugin contract
shared module
global dependency
```

tentukan plugin terdampak terlebih dahulu.

Jika seluruh suite terpengaruh, gunakan:

```powershell
.\build.ps1 -all
```

Compilation success bukan bukti bahwa fitur benar-benar bekerja.

---

# 13. Adventure & MiniMessage

Gunakan:

```text
Kyori Adventure
MiniMessage
Paper Components
```

Untuk kode baru:

DILARANG menjadikan legacy formatting sebagai architecture utama:

```text
&
§
ChatColor string
```

Gunakan Adventure Components.

MiniMessage harus diparse secara aman.

Jangan mempercayai user-generated MiniMessage input sebagai trusted markup.

---

# 14. Threading & Concurrency

## Async Work

Boleh dilakukan async:

```text
Database query
Database write
HTTP/network request
CPU-heavy calculation
File processing
Serialization
```

## Main Thread

Bukkit/Paper game-state mutation harus dilakukan pada Main Thread apabila API yang digunakan tidak thread-safe.

Contoh:

```text
Inventory mutation
Player state mutation
Entity mutation
World mutation
Teleportation
Bukkit-sensitive operations
```

Pattern:

```text
Async operation
      ↓
CompletableFuture
      ↓
Main-thread scheduling
      ↓
Bukkit state mutation
```

DILARANG melakukan blocking database/network operation di Main Thread.

---

# 15. Database Architecture

Database abstraction harus mendukung:

```text
SQLite
PostgreSQL
```

Gunakan:

```text
HikariCP
CompletableFuture
Repository abstraction
```

Jangan membuat database system kedua jika existing architecture sudah menyediakan abstraction yang sesuai.

---

# 16. Database Migration & Data Safety

Sebelum mengubah persistence:

```text
Inspect schema
Inspect serialization
Inspect migrations
Inspect existing player data
Inspect backward compatibility
```

Jika schema berubah:

```text
Old Data
   ↓
Migration
   ↓
New Structure
```

DILARANG:

```text
Delete database
↓
Start over
```

Jangan mereset:

```text
XP
Level
Region
Rewards
Currency
Transaction history
```

sebagai efek samping refactor.

Migration harus mempertimbangkan SQLite dan PostgreSQL jika keduanya masih supported.

Destructive migration memerlukan alasan yang jelas dan strategi pemulihan.

---

# 17. Transaction Integrity

Operasi berikut harus atomic dan concurrency-safe:

```text
Economy
Auction House
Trade
Shop
Barter
Currency transfer
Item exchange
```

Setiap transaksi harus mencegah:

```text
Duplicate item
Duplicate currency
Negative balance
Double execution
Partial transaction
Race condition
Lost item
Lost currency
```

Ideal transaction flow:

```text
Validate
   ↓
Acquire required locks
   ↓
Verify state again
   ↓
Execute transaction
   ↓
Persist atomically
   ↓
Release locks
   ↓
Publish event
```

Event publik tidak boleh dipakai sebagai pengganti transaction atomicity.

---

# 18. Economy Security

Jangan percaya data dari client.

Server harus menentukan dan memvalidasi:

```text
player UUID
item ownership
currency balance
transaction amount
target player
transaction state
permissions
```

Validasi harus mencakup:

```text
negative values
overflow
duplicate requests
stale state
race condition
invalid item data
invalid transaction state
```

UUID adalah identity authoritative.

Player name hanya display metadata.

---

# 19. Inter-Plugin Architecture

Gunakan API/provider dan event-driven integration.

Core menyediakan:

```text
ApexsionsCoreProvider.get()
ApexsionsCoreAPI
```

untuk:

```text
Region
Level
XP
Level title
Player progression
Kingdom state
```

Economy menyediakan:

```text
ApexsionsEconomyAPI
```

untuk:

```text
Balance
Currency
Atomic transactions
```

Shop menyediakan:

```text
ApexsionsShopProvider.get()
ApexsionsShopAPI
```

untuk:

```text
Dynamic pricing
Tax
Shop inventory
```

---

# 20. API Boundary Rules

Plugin lain harus mengakses public API, bukan implementation internal.

DILARANG:

```text
Plugin A
   ↓
directly access Plugin B database
```

Gunakan:

```text
Plugin A
   ↓
Plugin B API
   ↓
Plugin B implementation
   ↓
Database
```

API publik harus:

* minimal;
* jelas;
* stabil;
* terdokumentasi;
* backward-compatible jika memungkinkan.

Jangan mengekspos internal implementation tanpa alasan.

---

# 21. Dependency Direction

Target dependency:

```text
                 ApexsionsCore
                /      |      \
               ↓       ↓       ↓
            Chat    Economy   Media
                       ↓
                     Shop
                       ↓
                  Battlepass
```

Dependency aktual harus mengikuti kebutuhan repository.

Prinsip:

```text
Core provides fundamental APIs.
Feature plugins consume APIs.
```

ApexsionsCore tidak boleh memiliki hard dependency terhadap plugin feature hanya untuk mengakses functionality feature.

Jika circular dependency ditemukan:

```text
STOP
↓
inspect dependency graph
↓
redesign contract
```

Jangan menyelesaikan circular dependency dengan workaround yang menciptakan coupling lebih buruk.

---

# 22. Soft Dependencies

Optional plugin harus tetap graceful apabila tidak tersedia.

Contoh:

```text
LuckPerms
PlaceholderAPI
Citizens
TAB
EssentialsX
Vault
```

Jika dependency optional tidak tersedia:

```text
disable optional integration
continue core functionality
log clear information
```

Jangan membuat optional plugin menjadi hard dependency tanpa alasan.

---

# 23. Custom Events

Gunakan event-driven architecture untuk cross-plugin communication apabila sesuai.

Contoh:

```text
KingdomWarStartEvent
PlayerLevelUpEvent
MarketPriceChangeEvent
PlayerBalanceChangeEvent
```

Event tidak boleh menggantikan direct API ketika synchronous result diperlukan.

Jangan membuat event hanya demi terlihat "enterprise".

---

# 24. Configuration Architecture

Gunakan modular configuration.

Contoh:

```text
config/
├── gui.yml
├── messages.yml
├── ranks.yml
├── rewards.yml
├── moderation.yml
├── markets.yml
├── passes/
├── quests/
├── shop/
├── exp-shop/
└── categories/
```

Setiap configuration value harus mempunyai source of truth tunggal.

Jangan menduplikasi value yang sama ke:

```text
Java constant
YAML lain
Database
Hardcoded string
```

kecuali terdapat alasan teknis yang jelas.

---

# 25. Configuration Validation

Configuration harus divalidasi saat startup.

Jika invalid:

```text
Identify file
Identify key
Identify invalid value
Explain expected value
Fail safely
```

Jangan silently menggunakan nilai berbahaya.

Contoh invalid:

```yaml
starting-balance: -999999999
```

harus ditolak atau ditangani menggunakan safe fallback yang memang valid.

---

# 26. Commands

Semua command harus memvalidasi:

```text
Sender type
Permission
Arguments
Target
Cooldown
State
```

Jangan mengandalkan client-side validation.

Permission harus eksplisit.

Contoh:

```text
apexsions.admin
apexsions.staff
apexsions.core.*
```

Wildcard permission tidak boleh menjadi default untuk user biasa.

---

# 27. Security Requirements

Semua fitur harus mempertimbangkan:

```text
Permission bypass
Privilege escalation
Command injection
SQL injection
Unsafe deserialization
Arbitrary file access
Race condition
Item duplication
Currency duplication
Integer overflow
Invalid client input
Unauthorized administrative actions
```

Security issue yang ditemukan selama implementation harus diprioritaskan dibanding cosmetic refactor.

---

# 28. GUI Architecture

GUI harus reusable.

Gunakan pola seperti:

```text
gui/
├── BaseGui
├── GuiPaginator
├── GuiItemFactory
├── ReportGui
├── ReportDetailGui
├── ShopGui
├── TradeGui
└── MailGui
```

Jangan menduplikasi seluruh inventory-click handling di setiap GUI.

Inventory event harus divalidasi berdasarkan:

```text
GUI instance
player
slot
action
state
```

Jangan mengandalkan display name item sebagai satu-satunya identifier GUI.

---

# 29. Logging

Log harus membantu debugging tanpa membocorkan secret.

Gunakan level yang sesuai:

```text
INFO
WARNING
SEVERE
DEBUG
```

Jangan log:

```text
password
database credentials
tokens
API keys
private secrets
```

Error harus menjelaskan:

```text
what happened
where it happened
why it failed
what system was affected
```

Jangan menangkap exception lalu mengabaikannya secara silent.

---

# 30. Testing Philosophy

Testing harus mengikuti:

```text
Reproduce
   ↓
Trace
   ↓
Root Cause
   ↓
Fix
   ↓
Regression Test
   ↓
Build
   ↓
Runtime Validation
```

Jangan menyatakan bug selesai hanya karena compilation berhasil.

---

# 31. Regression Prevention

Setiap bug penting yang diperbaiki harus sebisa mungkin mempunyai regression coverage.

Prioritas tinggi:

```text
Economy duplication
Item duplication
Permission bypass
Data loss
Transaction race
Chat moderation bypass
GUI state bugs
Reward duplication
```

---

# 32. Documentation Synchronization

Jika behavior, architecture, API, command, configuration, atau workflow berubah, update documentation yang terdampak.

Dokumentasi utama:

```text
README.md
DOKUMENTASI.md
GEMINI.md
AGENTS.md
```

Tidak semua file harus berubah untuk setiap commit.

Update hanya dokumentasi yang memang terdampak.

Jangan mengubah dokumentasi agar terlihat updated jika behavior sebenarnya tidak berubah.

---

# 33. Generated Artifacts

Build artifacts harus berasal dari source code yang telah divalidasi.

Jika repository memang menetapkan binary JAR sebagai tracked artifact:

```text
source
   ↓
build
   ↓
validation
   ↓
JAR
   ↓
git diff
   ↓
commit
```

Jangan commit JAR yang berasal dari source state yang tidak sesuai dengan commit.

Jangan menyalin JAR ke server eksternal sebagai bagian dari repository workflow kecuali user secara eksplisit meminta deployment.

---

# 34. Documentation & Release Checklist

Sebelum release:

```text
[ ] Source code updated
[ ] Relevant configuration updated
[ ] Relevant documentation updated
[ ] Targeted plugin build successful
[ ] Runtime validation completed
[ ] Generated JAR verified
[ ] Git diff reviewed
[ ] No unintended files changed
[ ] Remote checked again
[ ] Commit created
[ ] Push authorized
[ ] Push successful
```

---

# 35. Final Pre-Push Safety Check

Sebelum push:

```powershell
git fetch origin
git status --short
git log HEAD..origin/main --oneline
git diff
git diff --stat
```

Pastikan:

```text
No unexpected files
No secrets
No unrelated changes
No accidental deletions
No stale remote commits
No debug artifacts
No generated junk
```

Jika terdapat perubahan remote:

```text
STOP
↓
Synchronize safely
↓
Rebuild
↓
Retest
↓
Review diff again
```

---

# 36. Never Assume

Agent wajib memverifikasi informasi yang dapat diverifikasi.

Jangan mengasumsikan:

```text
class exists
command exists
API exists
dependency exists
database schema
configuration key
plugin version
event behavior
thread safety
```

Gunakan:

```text
Inspect → Verify → Modify
```

Jika informasi tersedia di repository, repository adalah sumber kebenaran utama.

---

# 37. Git History Awareness

Untuk perubahan kompleks, gunakan Git history bila diperlukan:

```powershell
git log
git log -- <file>
git show <commit>
git blame <file>
```

Tujuan:

```text
Understand previous decisions
Find regression origin
Understand ownership/context
Avoid reverting intentional behavior
```

Jangan menghapus atau membatalkan perubahan developer lain hanya karena implementation terlihat tidak ideal.

---

# 38. Runtime Validation

Compilation bukan validation lengkap.

Jika environment memungkinkan, lakukan:

```text
Build
↓
Start Paper
↓
Inspect startup logs
↓
Test affected feature
↓
Test integration
↓
Restart server
↓
Verify persistence
```

Untuk perubahan cross-plugin:

```text
Core
↓
API
↓
Consumer plugin
↓
Runtime behavior
```

harus divalidasi.

---

# 39. Change Impact Analysis

Sebelum perubahan besar, identifikasi:

```text
Affected plugin
Affected packages
Affected APIs
Affected events
Affected configuration
Affected database tables
Affected commands
Affected permissions
Affected integrations
Affected documentation
```

Contoh:

```text
Changing Rank API
        ↓
Core
        ↓
Chat
        ↓
TAB / PlaceholderAPI
        ↓
Documentation
```

Jangan menganggap perubahan pada public API hanya berdampak pada satu class.

---

# 40. Compatibility

Pertahankan compatibility jika memungkinkan.

Sebelum melakukan breaking change:

1. cari semua consumer;
2. identifikasi API usage;
3. tentukan migration strategy;
4. update consumer;
5. build seluruh affected modules;
6. dokumentasikan breaking change.

Jangan menghapus API publik hanya karena implementation baru terasa lebih rapi.

---

# 41. Anti-Overengineering

Gunakan solusi paling sederhana yang memenuhi:

```text
Correctness
Security
Maintainability
Performance
Compatibility
```

Jangan menambahkan:

```text
framework
abstraction
dependency
service layer
event
cache
thread
database table
```

tanpa kebutuhan yang jelas.

Complexity adalah hutang yang harus dibayar developer berikutnya.

---

# 42. Performance Rules

Hindari operasi mahal di Main Thread.

Waspadai:

```text
Database query
Network request
Large file parsing
Repeated serialization
Large player iteration
Expensive string processing
Unbounded loops
```

Untuk high-frequency systems seperti:

```text
scoreboard
nametag
chat
combat
player movement
```

gunakan caching/delta updates bila memang diperlukan.

Target:

```text
Minimal unnecessary allocations
Minimal repeated calculation
Minimal main-thread work
```

Jangan melakukan premature optimization yang membuat code lebih sulit dipahami tanpa measurable benefit.

---

# 43. Scoreboard & Nametag

Jika sistem menggunakan animated RGB/gradient:

```text
Multi-phase shifting RGB gradient
Multi-scoreboard synchronization
Smart delta-frame caching
```

Animation harus menghindari update penuh apabila tidak diperlukan.

Gunakan delta updates:

```text
Previous frame
      ↓
Compare
      ↓
Only changed components
      ↓
Apply
```

Tujuan:

```text
Zero unnecessary TPS impact
```

---

# 44. Definition of Done

Task hanya dianggap selesai apabila:

```text
[ ] Requirement terpenuhi
[ ] Existing implementation telah diperiksa
[ ] Tidak ada duplicate implementation
[ ] Scope tetap terkendali
[ ] Dependency impact diperiksa
[ ] API impact diperiksa
[ ] Configuration diperiksa
[ ] Database impact diperiksa
[ ] Security impact diperiksa
[ ] Relevant documentation diperbarui
[ ] Affected plugin berhasil dikompilasi
[ ] Relevant tests berhasil
[ ] Runtime validation dilakukan jika memungkinkan
[ ] git diff diperiksa
[ ] git status diperiksa
[ ] Remote diperiksa kembali
[ ] Tidak ada perubahan tidak disengaja
[ ] Commit dibuat jika diperlukan
[ ] Push hanya dilakukan jika authorized
```

---

# 45. Agent Final Report

Setelah pekerjaan selesai, agent harus melaporkan:

```text
## Summary

What changed?

## Files Changed

Which files were created/modified/deleted?

## Plugins Affected

Which plugins were affected?

## Architecture Impact

Which APIs, events, services, or dependencies changed?

## Database Impact

Were schemas/migrations changed?

## Configuration

Which configuration files changed?

## Build

Which build command was executed?

## Tests

Which tests/validation were performed?

## Git

Commit:
Branch:
Remote synchronization:
Push status:

## Known Issues

What remains unresolved?

## Risk

Any compatibility/security/performance concerns?
```

Jangan mengatakan:

```text
"Everything works."
```

tanpa evidence.

Gunakan hasil validation yang benar-benar dilakukan.

---

# 46. Emergency Stop Conditions

Agent harus berhenti dan tidak melakukan perubahan lebih lanjut jika menemukan:

```text
Potential data loss
Unresolved merge conflict
Unknown destructive migration
Credential/secret exposure
Unexpected repository-wide modification
Circular dependency
Unclear ownership of conflicting changes
Potential economy duplication exploit
Potential permission escalation
Production data modification requirement
```

Dalam kondisi tersebut:

```text
STOP
↓
Explain the risk
↓
Show evidence
↓
Do not destroy or overwrite existing work
```

---

# 47. Golden Rules

Seluruh agent Apexsions wajib mengingat:

```text
1. Inspect before modifying.
2. Sync with origin before working.
3. Never overwrite another developer's work.
4. Search existing code before creating new code.
5. Keep scope controlled.
6. Use APIs instead of direct database coupling.
7. Keep optional integrations optional.
8. Never block the Main Thread with database/network work.
9. Protect economy and item transactions against race conditions and duplication.
10. Never silently destroy data.
11. Validate configuration.
12. Compilation is not runtime validation.
13. Review git diff before commit.
14. Fetch origin again before push.
15. Never force-push shared branches.
16. Commit and push are separate operations.
17. Update documentation when behavior or architecture changes.
18. Prefer simple, correct, maintainable solutions.
19. Never assume when the repository can prove it.
20. When safety and speed conflict, choose safety.
```

---

# 48. Final Workflow

The canonical Apexsions development workflow is:

```text
┌──────────────────────────────┐
│        Receive Task          │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Read GEMINI.md / AGENTS.md   │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ git status + fetch + inspect │
└──────────────┬───────────────┘
               ↓
       Remote Changes?
          /         \
        YES          NO
         ↓            ↓
  Sync Safely      Continue
         \            /
          ↓          ↓
┌──────────────────────────────┐
│ Inspect Existing Code        │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Analyze Scope & Dependencies │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Plan Minimal Change          │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Implement                    │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Build Affected Plugins       │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Test / Runtime Validation    │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Review git diff/status       │
└──────────────┬───────────────┘
               ↓
┌──────────────────────────────┐
│ Fetch origin again           │
└──────────────┬───────────────┘
               ↓
       Remote Changes?
          /         \
        YES          NO
         ↓            ↓
   Sync + Rebuild   Continue
         \            /
          ↓          ↓
┌──────────────────────────────┐
│ Commit                       │
└──────────────┬───────────────┘
               ↓
      Push Authorized?
          /         \
        YES          NO
         ↓            ↓
       Push       Stop at commit
         ↓
┌──────────────────────────────┐
│ Final Report                 │
└──────────────────────────────┘
```

**This workflow is mandatory unless the user explicitly instructs otherwise and the requested deviation does not violate repository safety or data integrity.**
