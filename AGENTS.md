# Agent Developer Guide — Apexsions Plugin Suite

Dokumen ini berisi panduan instruksi dan standar operasional bagi seluruh agen AI / developer yang berkontribusi pada repository **Apexsions**.

---

## 🏛️ Arsitektur & Aturan Ekosistem

### 1. Nama Resmi & 6 Modul Inti
- **`ApexsionsCore`**: Kerajaan (Zenithar, Solterra, Sylvamoor), leveling progresi, combat tag, BlueMap, warp GUI, rank animated engine, dan player inspector control hub.
- **`ApexsionsChat`**: Obrolan saluran (Global, Kingdom, Staff), preferensi GUI, ID-Card sosial, dan meja laporan investigasi staf.
- **`ApexsionsEconomy`**: Multi-Currency atomic (`Rupiah` & `Diamond`), lelang escrow `/ah`, dan barter/trade kerajaan.
- **`ApexsionsBattlepass`**: 200 Level Pass, quest pool, toko rotasi BP-XP, dan visual editor `/abp`.
- **`ApexsionsShop`**: Pasar dinamis 6 kategori, rasio jual 20%, pengaruh cuaca/kerajaan, tren pasar `/shop trends`, dan `/sellgui`.
- **`ApexsionsMedia`**: Multi-tile Async Image Banner, raytrace line-of-sight hover glow, actionbar tooltip, dan web confirmation.

### 2. Standar 9 Rank Resmi (`ranks.yml`)
1. `ancestor` (The Ancestor — Owner / Founder, Weight: 100)
2. `warden` (Warden — Head Staff / Admin, Weight: 90)
3. `herald` (Herald — Staff / Moderator, Weight: 80)
4. `sions` (Sions — Apex Donator Tier, Weight: 70)
5. `emperor` (Emperor — Donator Tier 4, Weight: 60)
6. `sovereign` (Sovereign — Donator Tier 3, Weight: 50)
7. `archon` (Archon — Donator Tier 2, Weight: 40)
8. `ascendant` (Ascendant — Donator Tier 1, Weight: 30)
9. `wanderer` (Wanderer — Default Rank Semua Pemain, Weight: 10)

### 3. Kompilasi & Git Flow
- **Aturan Kompilasi Terarah (Targeted Build Rule)**:
  - Jika yang diubah hanya 1, 2, atau 3 plugin, **HANYA** kompilasi plugin yang dimodifikasi (JANGAN gunakan `-all`):
    - `powershell -ExecutionPolicy Bypass -File .\build.ps1 Core`
    - `powershell -ExecutionPolicy Bypass -File .\build.ps1 Chat`
    - `powershell -ExecutionPolicy Bypass -File .\build.ps1 Economy`
    - `powershell -ExecutionPolicy Bypass -File .\build.ps1 Battlepass`
    - `powershell -ExecutionPolicy Bypass -File .\build.ps1 Shop`
    - `powershell -ExecutionPolicy Bypass -File .\build.ps1 Media`
  - Gunakan `.\build.ps1 -all` **HANYA** jika seluruh 6 plugin memang mengalami perubahan arsitektural secara serentak.
- Sebelum memulai dan sebelum push, selalu jalankan `git fetch origin` untuk mendeteksi perubahan remote.
- Commit dan push seluruh source code, file build, dan binary `.jar` ke branch `main`.
