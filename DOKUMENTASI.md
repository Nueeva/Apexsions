# Dokumentasi Master Apexsions Plugin Suite — Minecraft 1.21.4 / Paper 26.2

Dokumentasi resmi yang merangkum arsitektur menyeluruh, interaksi antar-plugin, matriks izin & perintah, konfigurasi modular, serta integrasi gameplay untuk 6 plugin utama di ekosistem **Apexsions**.

---

## 🏛️ 1. Ikhtisar Arsitektur 6 Plugin (Plugin Ecosystem Matrix)

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

1. **`ApexsionsCore`** (`com.apexsions.core.*`): Otoritas wilayah 3 Kerajaan (`Zenithar`, `Solterra`, `Sylvamoor`), progresi level (1-100) & 13 sumber XP, BlueMap polygon rendering, sistem `/rtp` terikat kerajaan, Kingdom War Manager, PvP Combat Tag (15s), proteksi PvP teritorial kerajaan, Title Vault GUI, Particle Cosmetics GUI, sistem Warp GUI & Admin Warp Manager, Player Inspector GUI, dan pencegahan TPA lintas-wilayah EssentialsX.
2. **`ApexsionsChat`** (`com.apexsions.chat.*`): Sistem komunikasi Adventure/MiniMessage dengan channel (`Global`, `Kingdom`, `Staff`), preferensi obrolan GUI (`/channel settings`), ID-Card sosial (`/channel profile <p>`), pamer item (`/showitem`), surat offline (`/mail`), chat games, pengumuman otomatis, sistem nickname kustom & token rename (`/nick`, `/realname`), dan sistem moderasi lapis tiga dengan Staff Reports Investigation Desk 54-slot (`/reports`).
3. **`ApexsionsEconomy`** (`com.apexsions.economy.*`): Multi-Currency atomic (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, dan Sistem Barter/Trade 12-Slot terintegrasi kerajaan & pajak transportasi lintas-kerajaan.
4. **`ApexsionsBattlepass`** (`com.apexsions.battlepass.*`): Season battlepass 200 level, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), Toko Rotasi (*Dynamic Shop*), dan Editor Admin GUI 54-Slot (`/abp`).
5. **`ApexsionsShop`** (`com.apexsions.shop.*`): Pasar & toko dinamis 6 kategori (`blocks`, `farming`, `food`, `ores`, `mob_drops`, `dyes`), rasio jual dasar **20%**, formula multiplier cuaca & bioma kerajaan, price clamping (50%-200%), siaran tren pasar berkala, dashboard tren `/shop trends`, pajak wilayah 10%, UI ramah sentuh/Bedrock, dan GUI jual cepat 45-slot (`/sell`).
6. **`ApexsionsMedia`** (`com.apexsions.media.*`): Sistem render banner/logo gambar multi-tile asinkron (PNG/JPG/URL), raytrace line-of-sight hover glowing & actionbar tooltip, serta aksi interaksi tautan URL web/salin clipboard terkonfirmasi (100% vanilla & Bedrock compatible).

---

## 🔗 2. Integrasi & Komunikasi Antar-Plugin

### A. Toko Dinamis & Ekonomi (`ApexsionsShop` $\leftrightarrow$ `ApexsionsEconomy` & `ApexsionsCore`)
- **Multi-Currency:** Mendukung transaksi berbasis `Rupiah` (Rp) dan `Diamond` secara atomic lewat `ApexsionsEconomyAPI`.
- **Spesialisasi Pasar Kerajaan:** Mengambil status kerajaan pembeli dari `ApexsionsCoreAPI`. Anggota kerajaan mendapat diskon khusus untuk komoditas unggulan wilayahnya.
- **Pajak & Multiplier Cuaca:** Harga beli & jual berfluktuasi dinamis berdasarkan kondisi cuaca dunia (hujan/badai/cerah) dan ketersediaan pasokan.

### B. Barter / Trade Terintegrasi Kerajaan (`ApexsionsEconomy` $\leftrightarrow$ `ApexsionsCore`)
- **Penyaringan Pemain Kerajaan:** Menu `/trade` secara default menyaring hanya anggota satu kerajaan. Pemain dapat menekan tombol filter di Slot 8 untuk melihat seluruh pemain server (Global).
- **Pajak Transportasi Lintas-Kerajaan:** Jika bertransaksi dengan anggota kerajaan lain, kedua belah pihak dikenakan biaya transportasi saat konfirmasi (default `Rp 5.000`). Transaksi sesama kerajaan adalah **Rp 0 (GRATIS)**.

### C. Enforcer TPA EssentialsX (`ApexsionsCore` $\leftrightarrow$ `EssentialsX`)
- **Pengecekan Kerajaan:** Teleportasi `/tpa` dan `/tpahere` hanya diizinkan untuk pemain dalam kerajaan yang sama.
- **Pengecekan Wilayah Teritorial:** Kedua pemain (pengirim & penerima) **wajib berada di dalam batas poligon kerajaan mereka**. Jika salah satu berada di luar wilayah teritorial (misal di wilderness atau kerajaan lawan), TPA dibatalkan secara otomatis.

### D. Exp-Shop & Progresi (`ApexsionsBattlepass` $\leftrightarrow$ `ApexsionsEconomy` & `ApexsionsCore`)
- Toko EXP Battlepass dapat menggunakan mata uang `Rupiah` dan `Diamond` melalui `ApexsionsEconomyAPI`.
- Menyinkronkan progres quest dengan 13 aksi gameplay `ApexsionsCore`.

---

## 📜 3. Matriks Perintah & Hak Akses (Commands & Permissions)

### 👑 ApexsionsCore
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/admingui` | `/apexadmin`, `/aadmin`, `/aa` | Master Admin Control Hub (Dashboard 54-slot seluruh suite) | `apexsions.admin.gui` | `op` |
| `/admingui player <p>`| - | Membuka Deep Player Inspector untuk mengatur saldo, level, kerajaan, monarch, inv | `apexsions.admin.gui` | `op` |
| `/admingui warp` | - | Pintasan langsung ke Admin Warp Management GUI | `apexsions.admin.gui` | `op` |
| `/admingui economy` | - | Pintasan langsung ke Admin Economy Controls | `apexsions.admin.gui` | `op` |
| `/admingui pass` | - | Pintasan langsung ke BattlePass Visual Editor | `apexsions.admin.gui` | `op` |
| `/admingui shop` | - | Pintasan langsung ke Dashboard Tren Pasar Toko | `apexsions.admin.gui` | `op` |
| `/admingui media` | - | Pintasan langsung ke Media Banner Admin GUI | `apexsions.admin.gui` | `op` |
| `/admingui reload` | - | Me-reload konfigurasi terpusat seluruh suite | `apexsions.admin.gui` | `op` |
| `/lobby` | `/hub` | Teleportasi ke lobi utama server (Multiverse-ready) | `apexsionscore.command.lobby` | `true` |
| `/warp [nama]` | `/warps` | Membuka GUI navigasi warp 54-slot atau teleport ke lokasi | `apexsionscore.command.warp` | `true` |
| `/warpmgr` | `/warpadmin`, `/warp admin` | Membuka Interactive Admin Warp Management GUI | `apexsionscore.warp.admin` | `op` |
| `/warp set <nama> [kat]` | - | Membuat warp baru di lokasi berdiri | `apexsionscore.warp.admin` | `op` |
| `/warp delete <nama>` | `/warp del` | Menghapus warp dari database server | `apexsionscore.warp.admin` | `op` |
| `/kingdom` | `/k`, `/region` | Membuka profil dan status kerajaan pemain | `apexsionscore.command.region` | `true` |
| `/kingdom choose` | `/k select` | Membuka menu pemilihan 3 kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard`| Membuka Hall of Fame & Leaderboard GUI | `apexsionscore.command.level` | `true` |
| `/kingdom setking <k> <p>` | - | Mengangkat pemain menjadi Raja kerajaan | `apexsionscore.admin` | `op` |
| `/kingdom unsetking <k>` | `/kingdom removeking` | Mencabut gelar Raja dari kerajaan | `apexsionscore.admin` | `op` |
| `/level` | `/lvl`, `/profile`, `/exp`, `/rewards` | Membuka GUI progress bar level (1-100) & hadiah | `apexsionscore.command.level` | `true` |
| `/xpguide` | - | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/titles` | `/tags`, `/title`, `/tag` | Membuka Title Vault GUI untuk memasang gelar & badge prestise | `apexsionscore.command.titles` | `true` |
| `/cosmetics` | `/auras`, `/trails`, `/aura`, `/trail` | Membuka Particle Cosmetics GUI (Head Auras, Trails, Kill Effects)| `apexsionscore.command.cosmetics` | `true` |
| `/rtp` | `/wild`, `/wilderness`, `/krtp` | Teleportasi acak aman di dalam wilayah kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/ac reload` | `/apexsionscore reload`, `/kc reload` | Reload modular configs, LuckPerms ranks, BlueMap, & rewards | `apexsionscore.admin` | `op` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang resmi antar-kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan paksa perang kerajaan aktif (Admin) | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status dan sisa waktu perang kerajaan aktif | `apexsionscore.admin` | `op` |
| `/ac setlevel <player> <1-100>`| - | Mengatur level pemain secara langsung | `apexsionscore.admin` | `op` |
| `/ac addxp <player> <amount>`| - | Menambahkan XP progresi ke pemain | `apexsionscore.admin` | `op` |
| `/ac setkingdom <player> <kingdom>`| - | Memindahkan kerajaan pemain seketika | `apexsionscore.admin` | `op` |
| `/ac setlobby` | - | Menetapkan koordinat lobby/spawn di lokasi berdiri | `apexsionscore.admin` | `op` |
| `/ac info <player>` | - | Memeriksa rincian level, XP, kerajaan, dan klaim reward pemain | `apexsionscore.admin` | `op` |

---

### 💬 ApexsionsChat
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/channel [settings]` | `/ch` | Mengganti channel atau buka pengaturan GUI | `apexsionschat.channel` | `true` |
| `/channel profile <p>`| - | Membuka antarmuka interaksi profil sosial pemain (ID-Card) | `apexsionschat.channel` | `true` |
| `/g [pesan]` | `/global` | Berbicara di obrolan Global | `apexsionschat.channel.global` | `true` |
| `/kc [pesan]` | `/kchat`, `/kingdomchat` | Berbicara di obrolan Kerajaan | `apexsionschat.channel.kingdom` | `true` |
| `/sc [pesan]` | `/staffchat` | Berbicara di obrolan Staf | `apexsionschat.channel.staff` | `op` |
| `/showitem` | `/item`, `/i`, `/hand` | Memamerkan item di tangan ke obrolan | `apexsionschat.showitem` | `true` |
| `/report <p> <alasan>`| - | Melaporkan pemain yang melanggar aturan server | `apexsionschat.report` | `true` |
| `/reports` | `/reportlist` | Membuka antarmuka resolusi laporan & meja staf 54-slot | `apexsionschat.staff.reports` | `op` |
| `/mail send <p> <msg>`| - | Mengirim surat offline ke pemain | `apexsionschat.mail` | `true` |
| `/mail read` | `/inbox` | Membaca kotak masuk surat offline | `apexsionschat.mail` | `true` |
| `/mail clear` | - | Menghapus seluruh pesan di kotak masuk | `apexsionschat.mail` | `true` |
| `/nick [nama\|color\|reset]` | `/nickname` | Mengatur nickname kustom menggunakan token atau buka GUI warna | `apexsions.nick` | `true` |
| `/nick color` | - | Membuka antarmuka pemilihan warna & gradasi nickname | `apexsions.nick.color` | `op` |
| `/realname <nickname>` | - | Mengetahui akun / nama asli pemain di balik nickname aktif | - | `true` |
| `/apexsionschat reload`| `/chatadmin reload`, `/acchat reload` | Reload konfigurasi obrolan, chat games, & pengumuman | `apexsionschat.admin` | `op` |
| `/apexsionschat mute` | `/acchat lock` | Toggle kunci/mute obrolan global server | `apexsionschat.admin` | `op` |
| `/apexsionschat clear`| - | Membersihkan layar obrolan server (100 baris kosong) | `apexsionschat.admin` | `op` |
| `/apexsionschat game start`| - | Memulai paksa chat game mini-event | `apexsionschat.admin` | `op` |
| `/apexsionschat announce`| - | Menyiarkan pengumuman berkala berikutnya secara instan | `apexsionschat.admin` | `op` |

---

### 💰 ApexsionsEconomy
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/economy` | `/eco`, `/uang`, `/bal` | Membuka menu utama saldo pemain | `apexsionseconomy.use` | `true` |
| `/baltop` | `/topbal` | Menampilkan peringkat kekayaan server | `apexsionseconomy.use` | `true` |
| `/pay <p> <amt> [curr]` | `/transfer`, `/kirimuang` | Mentransfer uang ke pemain lain | `apexsionseconomy.pay` | `true` |
| `/ah` | `/lelang`, `/auction` | Membuka pasar lelang & brankas klaim escrow | `apexsionseconomy.ah` | `true` |
| `/trade [pemain]` | `/barter`, `/tukar` | Membuka menu barter item & saldo | `apexsionseconomy.trade` | `true` |
| `/trade toggle` | - | Toggle mengaktifkan / menonaktifkan ajakan trade | `apexsionseconomy.trade` | `true` |
| `/ecoadmin reload` | `/apexeconomy reload`, `/adminpay reload` | Reload konfigurasi ekonomi, mata uang, & tarif trade/ah | `apexsionseconomy.admin` | `op` |
| `/ecoadmin give <p> <amt> [curr]`| - | Menambahkan saldo Rupiah / Diamond ke pemain | `apexsionseconomy.admin` | `op` |
| `/ecoadmin take <p> <amt> [curr]`| - | Mengurangi saldo Rupiah / Diamond dari pemain | `apexsionseconomy.admin` | `op` |
| `/ecoadmin set <p> <amt> [curr]` | - | Menyetel saldo Rupiah / Diamond pemain secara langsung | `apexsionseconomy.admin` | `op` |

---

### 🎫 ApexsionsBattlepass
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/bp` | `/battlepass` | Membuka antarmuka utama 200 level Season BattlePass | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp misi` | Membuka daftar misi harian/mingguan/bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp toko` | Membuka toko rotasi penukaran BP-XP | `apexsionsbattlepass.use` | `true` |
| `/bp pass` | - | Membuka menu pembelian/peningkatan tier pass | `apexsionsbattlepass.use` | `true` |
| `/bp season` | - | Memeriksa status, waktu tersisa, dan periode season | `apexsionsbattlepass.use` | `true` |
| `/bp claim [level]` | - | Mengklaim hadiah level BattlePass yang telah tercapai | `apexsionsbattlepass.use` | `true` |
| `/bp level` | - | Menampilkan level dan sisa XP BattlePass saat ini | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |
| `/abp reload` | - | Reload seluruh konfigurasi pass, quests, rewards, & season | `apexsionsbattlepass.admin` | `op` |
| `/abp givepass <p> <tier>` | - | Memberikan tier pass (`free`, `premium`, `premium_plus`, `ultimate`)| `apexsionsbattlepass.admin` | `op` |
| `/abp setlevel <p> <lvl>`| - | Menyetel level BattlePass pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp addxp <p> <amount>`| - | Menambahkan XP BattlePass pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp reset <p>` | - | Mereset total seluruh data progresi BattlePass pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp editor` | - | Membuka GUI visual editor hadiah & toko | `apexsionsbattlepass.admin` | `op` |

---

### 🛒 ApexsionsShop
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/shop` | `/pasar`, `/toko`, `/store`, `/bazar` | Membuka menu utama 6 kategori toko | `apexsionsshop.use` | `true` |
| `/shop trends` | - | Membuka dashboard visual tren pasar & fluktuasi harga | `apexsionsshop.use` | `true` |
| `/shop <kategori>` | - | Membuka langsung kategori toko tertentu | `apexsionsshop.use` | `true` |
| `/shop reload` | - | Reload konfigurasi toko, formula dinamis, & kategori | `apexsionsshop.admin` | `op` |
| `/sell` | `/sellgui`, `/jual` | Membuka GUI jual cepat 45-slot drag-and-drop | `apexsionsshop.sell` | `true` |
| `/sellall` | `/jualsemua` | Menjual seluruh item cocok di inventaris | `apexsionsshop.sell` | `true` |
| `/sellhand` | `/jualtangan` | Menjual item yang sedang dipegang di tangan utama | `apexsionsshop.sell` | `true` |

---

### 🖼️ ApexsionsMedia (Banner & Creator Suite)
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/media create <id> <src> [w] [h] [url] [mode]` | `/banner create` | Memasang banner baru dengan auto-dimensi & link | `apexsionsmedia.admin` | `op` |
| `/media place <id>` | `/banner place`, `/media paste` | Memindahkan banner ke dinding target via Raytracing | `apexsionsmedia.admin` | `op` |
| `/media copy <idAsal> <idBaru>` | `/banner copy`, `/media clone` | Menduplikasi template konfigurasi banner | `apexsionsmedia.admin` | `op` |
| `/media move <id>` | `/banner move`, `/media moveto` | Memindahkan lokasi banner yang ada | `apexsionsmedia.admin` | `op` |
| `/media delete <id>` | `/banner delete`, `/media remove` | Menghapus banner dan entity item frame terkait | `apexsionsmedia.admin` | `op` |
| `/media list` | `/banner list` | Menampilkan daftar seluruh banner aktif di server | `apexsionsmedia.admin` | `op` |
| `/media setlink <id> <url> [mode]` | `/banner setlink` | Mengubah tautan URL interaktif banner | `apexsionsmedia.admin` | `op` |
| `/media resize <id> <w> <h>` | `/banner resize` | Mengubah dimensi ukuran banner secara langsung | `apexsionsmedia.admin` | `op` |
| `/media reload` | `/banner reload` | Reload konfigurasi & render ulang seluruh banner aktif | `apexsionsmedia.admin` | `op` |
| `/media gui` | `/media admin`, `/banner gui` | Membuka Interactive Media Admin Management GUI | `apexsionsmedia.admin` | `op` |
| `/creator` | `/kreator`, `/creator menu` | Membuka Interactive Creator Hub GUI (54-slot) | `apexsionsmedia.creator` | `true` |
| `/creator submit <url>` | `/creator claim <url>` | Submit URL video YouTube/TikTok untuk verifikasi & klaim | `apexsionsmedia.creator` | `true` |
| `/creator link <yt\|tt> <id>` | `/creator link yt/tt` | Memulai penautan akun YouTube / TikTok | `apexsionsmedia.creator` | `true` |
| `/creator verify youtube` | `/creator verify` | Verifikasi kode linking deskripsi YouTube | `apexsionsmedia.creator` | `true` |
| `/creator unlink <yt\|tt>` | `/creator unbind` | Memutuskan tautan akun kreator | `apexsionsmedia.creator` | `true` |
| `/creator tiers` | `/creator rewards` | Membuka GUI daftar tingkatan tier & hadiah | `apexsionsmedia.creator` | `true` |
| `/creator admin <reload\|info\|reset>` | `/creator admin` | Manajemen administrasi data & reload kreator | `apexsionsmedia.creator.admin` | `op` |

---

## ⚡ 4. Panduan Kompilasi Multi-Compiler (`build.ps1`)

Untuk efisiensi dan kecepatan pengembangan, **HANYA** kompilasi plugin yang mengalami perubahan kode:

```powershell
# 1. Kompilasi Terarah per Modul (Hanya ~15-20 detik):
powershell -ExecutionPolicy Bypass -File .\build.ps1 Core
powershell -ExecutionPolicy Bypass -File .\build.ps1 Chat
powershell -ExecutionPolicy Bypass -File .\build.ps1 Economy
powershell -ExecutionPolicy Bypass -File .\build.ps1 Battlepass
powershell -ExecutionPolicy Bypass -File .\build.ps1 Shop
powershell -ExecutionPolicy Bypass -File .\build.ps1 Media

# 2. Kompilasi Seluruh Suite (Gunakan HANYA jika semua 6 modul berubah):
powershell -ExecutionPolicy Bypass -File .\build.ps1 -all
```
