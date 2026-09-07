# Dokumentasi Master Apexsions Plugin Suite — Minecraft 26.2 (The Peak Civilizations)

Dokumentasi resmi yang merangkum arsitektur menyeluruh, interaksi antar-plugin, matriks izin & perintah, konfigurasi modular, serta integrasi gameplay untuk 7 plugin utama di ekosistem **Apexsions**.

---

## 🏛️ 1. Ikhtisar Arsitektur 7 Plugin (Plugin Ecosystem Matrix)

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
                    ┌───────────────────┼───────────────────┐
                    ▼                   ▼                   ▼
          ┌───────────────────┐┌───────────────────┐┌───────────────────────┐
          │  ApexsionsShop    ││  ApexsionsMedia   ││ApexsionsCustomEnchants│
          │ (Dynamic Markets) ││(Interactive Visual││ (Enchanter & Sets)    │
          └───────────────────┘└───────────────────┘└───────────────────────┘
```

1. **`ApexsionsCore`** (`com.apexsions.core.*`): Otoritas wilayah 3 Kerajaan (`Zenithar`, `Solterra`, `Sylvamoor`), progresi level (1-100) & 13 sumber XP, BlueMap polygon rendering, sistem `/rtp` terikat kerajaan, Kingdom War Manager, PvP Combat Tag (15s), proteksi PvP teritorial kerajaan, Title Vault GUI, Particle Cosmetics GUI, sistem Warp GUI & Admin Warp Manager, Player Inspector GUI, kit kerajaan terintegrasi (`/kits`), WebBridge asynchronous delivery queue (Online & Offline), dan NightCore Native Dialog Input GUI (`CustomInputTextGUI`) untuk Paper 26.2.
2. **`ApexsionsChat`** (`com.apexsions.chat.*`): Sistem komunikasi Adventure/MiniMessage dengan channel (`Global`, `Kingdom`, `Staff`), preferensi obrolan GUI (`/channel settings`), ID-Card sosial (`/channel profile <p>`), pamer item (`/showitem`), surat offline (`/mail`), chat games, pengumuman otomatis, sistem nickname kustom & token rename (`/nick`, `/realname`), dan sistem moderasi lapis tiga dengan Staff Reports Investigation Desk 54-slot (`/reports`).
3. **`ApexsionsEconomy`** (`com.apexsions.economy.*`): Multi-Currency atomic (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, Sistem Barter/Trade 12-Slot terintegrasi kerajaan & pajak transportasi lintas-kerajaan, serta dukungan penuh console / web delivery (`/eco give/take/set/reload`).
4. **`ApexsionsBattlepass`** (`com.apexsions.battlepass.*`): Season battlepass 200 level, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`FREE`, `PREMIUM`, `PREMIUM+`, `ULTIMATE`), Toko Rotasi (*Dynamic Shop*), dan Editor Admin GUI 54-Slot (`/abp`).
5. **`ApexsionsShop`** (`com.apexsions.shop.*`): Pasar & toko dinamis 6 kategori (`blocks`, `farming`, `food`, `ores`, `mob_drops`, `dyes`), rasio jual dasar **20%**, formula multiplier cuaca & bioma kerajaan, price clamping (50%-200%), siaran tren pasar berkala, dashboard tren `/shop trends`, pajak wilayah 10%, UI ramah sentuh/Bedrock, dan GUI jual cepat 45-slot (`/sell`).
6. **`ApexsionsMedia`** (`com.apexsions.media.*`): Sistem render banner/logo gambar multi-tile asinkron (PNG/JPG/URL), raytrace line-of-sight hover glowing & actionbar tooltip, serta aksi interaksi tautan URL web/salin clipboard terkonfirmasi (100% vanilla & Bedrock compatible).
7. **`ApexsionsCustomEnchants`** (`com.apexsions.customenchants.*`): Dual-Currency Enchanter Gacha GUI (`/ce`), Toko Buku Sihir Spesifik 54-Slot (`/ce shop`), 28 Custom Enchantments berkekuatan tinggi, Mystery & Magic Dust, White & Black Scrolls, Central Admin Hub (`/ace`), Katalog `/ace enchants`, dan Interactive Armor Set Builder (`/ace create`) dengan sinkronisasi ID otomatis.

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
| `/kingdom setspawn <k>`| `/k setspawn` | Menetapkan titik spawn ibukota kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/kingdom setking <k> <p>` | - | Mengangkat pemain menjadi Raja kerajaan | `apexsionscore.admin` | `op` |
| `/kingdom unsetking <k>` | `/kingdom removeking` | Mencabut gelar Raja dari kerajaan | `apexsionscore.admin` | `op` |
| `/level` | `/lvl`, `/profile`, `/exp`, `/rewards` | Membuka GUI progress bar level (1-100) & hadiah | `apexsionscore.command.level` | `true` |
| `/xpguide` | - | Panduan detail 13 sumber perolehan XP | `apexsionscore.command.level` | `true` |
| `/titles` | `/tags`, `/title`, `/tag` | Membuka Title Vault GUI untuk memasang gelar & badge prestise | `apexsionscore.command.titles` | `true` |
| `/cosmetics` | `/auras`, `/trails`, `/aura`, `/trail` | Membuka Particle Cosmetics GUI (Head Auras, Trails, Kill Effects)| `apexsionscore.command.cosmetics` | `true` |
| `/rtp` | `/wild`, `/wilderness`, `/krtp` | Teleportasi acak aman di dalam wilayah kerajaan sendiri | `apexsionscore.command.rtp` | `true` |
| `/ac reload` | `/apexsionscore reload`, `/kc reload` | Reload modular configs, LuckPerms ranks, BlueMap, & rewards | `apexsionscore.admin` | `op` |
| `/ac setspawn <kingdom>`| `/ac setcapital` | Menetapkan koordinat spawn ibukota kerajaan di lokasi berdiri | `apexsionscore.admin` | `op` |
| `/ac war start <K1> <K2> [m]`| - | Memulai perang resmi antar-kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/ac war stop` | - | Menghentikan paksa perang kerajaan aktif (Admin) | `apexsionscore.admin` | `op` |
| `/ac war status` | - | Memeriksa status dan sisa waktu perang kerajaan aktif | `apexsionscore.admin` | `op` |
| `/ac setlevel <player> <1-100>`| `/kc setlevel` | Mengatur level pemain secara langsung (Online & Offline) | `apexsionscore.admin` | `op` |
| `/ac addxp <player> <amount>`| `/kc addxp` | Menambahkan XP progresi ke pemain (Online & Offline) | `apexsionscore.admin` | `op` |
| `/ac setkingdom <player> <kingdom>`| `/kc setk` | Memindahkan kerajaan pemain seketika | `apexsionscore.admin` | `op` |
| `/ac setlobby` | `/kc setlobby` | Menetapkan koordinat lobby/spawn di lokasi berdiri | `apexsionscore.admin` | `op` |
| `/ac info <player>` | `/kc info` | Memeriksa rincian level, XP, kerajaan, dan klaim reward pemain | `apexsionscore.admin` | `op` |
| `/link [pin]` | `/tautkan` | Menautkan akun in-game dengan portal web Azuriom | `apexsionscore.link` | `true` |

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
| `/economy` | `/eco`, `/uang`, `/bal` | Membuka menu saldo. Mendukung `/eco <give\|take\|set>` via Player & Console | `apexsionseconomy.use` | `true` |
| `/baltop` | `/topbal` | Menampilkan peringkat kekayaan server | `apexsionseconomy.use` | `true` |
| `/pay <p> <amt> [curr]` | `/transfer`, `/kirimuang` | Mentransfer uang ke pemain lain | `apexsionseconomy.pay` | `true` |
| `/ah` | `/lelang`, `/auction` | Membuka pasar lelang & brankas klaim escrow | `apexsionseconomy.ah` | `true` |
| `/trade [pemain]` | `/barter`, `/tukar` | Membuka menu barter item & saldo | `apexsionseconomy.trade` | `true` |
| `/trade toggle` | - | Toggle mengaktifkan / menonaktifkan ajakan trade | `apexsionseconomy.trade` | `true` |
| `/ecoadmin reload` | `/apexeconomy reload`, `/adminpay reload` | Reload konfigurasi ekonomi, mata uang, & tarif trade/ah | `apexsionseconomy.admin` | `op` |
| `/ecoadmin give <p> <amt> [curr]`| `/eco give` | Menambahkan saldo Rupiah / Diamond ke pemain (Player & Console) | `apexsionseconomy.admin` | `op` |
| `/ecoadmin take <p> <amt> [curr]`| `/eco take` | Mengurangi saldo Rupiah / Diamond dari pemain (Player & Console) | `apexsionseconomy.admin` | `op` |
| `/ecoadmin set <p> <amt> [curr]` | `/eco set` | Menyetel saldo Rupiah / Diamond pemain secara langsung (Player & Console) | `apexsionseconomy.admin` | `op` |

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

### ⚡ ApexsionsCustomEnchants
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/ce` | `/enchanter`, `/customenchants` | Membuka Enchanter Gacha Dual-Currency GUI | `apexsionscustomenchants.use` | `true` |
| `/ce shop` | `/ceshop` | Membuka Toko Buku Sihir Spesifik 54-Slot | `apexsionscustomenchants.use` | `true` |
| `/ce tinkerer` | `/tinkerer` | Membuka antarmuka Tinkerer Kerajaan (Coming Soon) | `apexsionscustomenchants.use` | `true` |
| `/ace` | `/customenchantsadmin` | Central Admin Hub GUI (45-Slot) | `apexsionscustomenchants.admin` | `op` |
| `/ace enchants` | `/ae admin` | Katalog interaktif replika AdvancedEnchantments | `apexsionscustomenchants.admin` | `op` |
| `/ace create` | - | Interactive Item & Armor Set Builder dengan sinkronisasi ID | `apexsionscustomenchants.admin` | `op` |
| `/ace pricing` | - | Konfigurasi harga gacha, rate, dan multiplier | `apexsionscustomenchants.admin` | `op` |
| `/ace reload` | - | Reload konfigurasi custom enchants, tiers, dan sets | `apexsionscustomenchants.admin` | `op` |

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
powershell -ExecutionPolicy Bypass -File .\build.ps1 CustomEnchants

# 2. Kompilasi Seluruh Suite (Gunakan HANYA jika semua 7 modul berubah):
powershell -ExecutionPolicy Bypass -File .\build.ps1 -all
```

---

## 📜 5. Kesinambungan Lore & Mekanik Gameplay (The Narrative Mechanics Matrix)

Ekosistem Apexsions tidak sekadar kumpulan plugin teknis terpisah, melainkan perwujudan langsung dari narasi kanonik dunia **Apexsions — The Peak Civilizations**:

### A. Kisah Agung: Kejatuhan Sions & Lahirnya 3 Peradaban
Dahulu kala, benua ini dipersatukan di bawah satu imperium agung yang membentang tanpa batas: **Kekaisaran Kuno Sions**. Namun, ambisi pemimpin terakhirnya untuk melipatgandakan kekuatan pasukan dengan menyerap energi terlarang dari dimensi kegelapan (*Dark Dimension*) memicu malapetaka dahsyat (*The Great Rupture*). Kekaisaran runtuh dalam kehancuran kosmis, memaksa rakyatnya tercerai-berai:
1. **Zenithar (Arah Timur / Zenith Cakrawala)**:
   - *Latar Belakang*: Keluarga dinasti kerajaan, bangsawan berdarah murni, dan kavaleri suci yang berhasil mempertahankan diri dan hijrah ke arah timur pegunungan kristal.
   - *Karakteristik & Buff*: Menjunjung tinggi kehormatan dan pertahanan suci (*Buff: Speed, Luck, Damage Reduction; Debuff: Kerentanan Racun, Porsi Makan*).
2. **Solterra (Arah Selatan / Kawah Emas Vulkanik)**:
   - *Latar Belakang*: Para ahli sihir tempur (Magicians), alkemis, dan tentara tangguh berpengalaman yang memisahkan diri ke tanah tandus dan lembah cadas selatan.
   - *Karakteristik & Buff*: Menguasai peleburan bijih logam, kekuatan fisik destruktif, dan api (*Buff: Serangan Tinggi, Critical Hit, Mining Haste; Debuff: -2 HP Darah, Rentan Kerusakan, Cepat Lapar*).
3. **Sylvamoor (Arah Barat / Belantara Kanopi Purba)**:
   - *Latar Belakang*: Kaum pekerja, pemburu, petani, serta tentara non-magis yang bersatu dan bermigrasi ke hutan rimba raksasa barat.
   - *Karakteristik & Buff*: Mengembangkan keahlian hidup berdampingan dengan alam, foraging, dan kelincahan berburu (*Buff: +2 HP Darah, Pertahanan Tinggi, Drop Rate Melimpah; Debuff: Mabuk Ketinggian, Kerentanan Api*).

### B. Matriks Kesinambungan Fitur Plugin dengan Lore:
- **Auto-Respawn Ibukota Kerajaan (`ApexsionsCore`)**: Saat gugur, jiwa prajurit ditarik kembali ke altar suci ibukota peradaban masing-masing (sinkron dengan peta teritorial BlueMap).
- **Perlindungan Teritorial & Kingdom War (`ApexsionsCore`)**: Larangan friendly-fire di dalam wilayah melindungi warga dari perang saudara, sedangkan mode perang resmi (*War Mode*) mencerminkan perebutan hegemoni klaim tahta Sions.
- **Pajak Transportasi Antar-Kerajaan (`ApexsionsEconomy`)**: Perbedaan faksi dan jarak geografis mewajibkan adanya bea cukai saat bertransaksi dengan kerajaan lain via `/trade`.
- **Fluktuasi Pasar Dinamis (`ApexsionsShop`)**: Harga beli dan jual bergantung pada komoditas unggulan masing-masing peradaban serta kondisi cuaca dunia.
- **Quest Sejarah & Ekspedisi (`ApexsionsBattlepass`)**: Misi perburuan dan eksplorasi memandu petualang menyingkap rahasia masa lalu.

---

## ⚔️ 6. Ekosistem PVE: Reruntuhan Kuno Sions & Integrasi MythicMobs

Di titik pusat alam liar (Wilderness) di antara ketiga kerajaan, berdiri **Reruntuhan Kerajaan Kuno Sions** (*The Forbidden Sanctum of Sions*). Area ini terkontaminasi radiasi energi Dark Dimension:

### A. Hierarki Monster Berlevel (PVE Level Scaling):
1. **Wilderness Umum (Lv. 10–25)**: Monster alam liar berkeliaran dengan statistik HP & Damage yang terkalibrasi, memberi XP leveling untuk `ApexsionsCore`.
2. **Pinggiran Reruntuhan (Lv. 30–55)**:
   - **`SionsVoidAssassin`**: Pembunuh bayangan yang dapat menghilang (*ShadowStep*) dan teleport ke belakang punggung pemain.
   - **`SionsRuinSentinel`**: Golem pertahanan kuno dengan serangan hentakan tanah (*GroundShatter*) dan meriam peledak (*VoidMortar*).
   - **`SionsCultistPriest`**: Rahib terkorupsi yang memulihkan darah pasukan Sions di sekitarnya.
3. **Raid World Boss: Emperor Valerius, The Void-Touched (Lv. 100)**:
   - Bos 3-Fase dengan indikator bahaya telegraphed di tanah sebelum ledakan void.
   - Fase kebal perisai dimensi (*Invulnerability Shield*) yang memanggil minion penjaga tahta.
   - Fase amarah (*Enrage*) saat HP < 25% yang memicu hujan meteor kegelapan (*Void Cataclysm*).

### B. Siklus Relik & Hadiah Legendaris:
- **`SionsAncientRelic` (Pecahan Relik Kuno)**: Komoditas ekonomi bernilai tinggi untuk diperdagangkan di pasar lelang (`/ah`) atau ditukar koin.
- **`CorruptedDarkCore` (Inti Dimensi Gelap)**: Material langka untuk penempaan sihir tingkat tinggi di masa depan.
- **`ValeriusVoidblade` & `CrownOfSions`**: Senjata dan mahkota peninggalan kaisar dengan efek visual custom serta bonus stat tempur.

---

## 🗺️ 7. Master Roadmap & Visi Jangka Panjang Ekosistem Apexsions

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                           APEXSIONS MASTER ROADMAP                               │
└──────────────────────────────────────────────────────────────────────────────────┘
  [Fase 1: Kedaulatan Teritorial] (SELESAI / AKTIF)
    ├── 7 Plugin Suite Utama Modular & Terintegrasi
    ├── Sistem 3 Kerajaan (Zenithar, Solterra, Sylvamoor) & Poligon BlueMap
    ├── Progresi Karakter 1-100 & 13 Sumber XP
    ├── Ekonomi Multi-Mata Uang Atomic & Pasar Lelang Escrow
    └── Auto-Respawn Ibukota Kerajaan & Integrasi In-Game Spawn Manager

  [Fase 2: Bencana Dimensi Gelap & World Raids] (SAAT INI / EXPANSION)
    ├── Integrasi Dungeon PVE Reruntuhan Kuno Sions via MythicMobs
    ├── Mekanik Bos Multi-Fase dengan Telegraphed Floor Indicators
    ├── Peredaran Relik Sions di Pasar Lelang & Toko Dinamis
    └── Penyesuaian Webstore Checkout Instan WhatsApp Founder/Admin

  [Fase 3: Pengepungan Benteng & Perang Teritorial] (RENCANA JANGKA MENENGAH)
    ├── Fitur Kingdom Castle Siege: Perebutan benteng terluar di Wilderness
    ├── Pajak Teritorial Wilayah Taklukan untuk Kas Kerajaan
    ├── Kit Perang Khusus Siege & Perlengkapan Tempur Artileri
    └── Sistem Aliansi Diplomasi Sementara Antar-Dua Kerajaan

  [Fase 4: Sinkronisasi Jaringan Web Terpadu] (STATUS: AKTIF / TEREALISASI)
    ├── WebBridge v1: Sinkronisasi REST API Dua Arah (Minecraft <-> Azuriom Web)
    ├── Sistem Terjemahan Bilingual Menyeluruh (ID 🇮🇩 & EN 🇬🇧) via Engine APX_I18N
    ├── Papan Peringkat Tiga Kerajaan & Dewan Kehormatan Realm (/leaderboard)
    ├── URL Profil Publik Berbasis ID Unik (/player/{uuid}) & 301 Canonical Redirect
    └── Penautan Akun In-Game /link, Klaim Hadiah Harian & Self-Service Profil Web
```

---

## 🌐 8. Arsitektur Jembatan Web & Portal Ekosistem (`Website/` & `apexsions-bridge`)

Ekosistem Apexsions mengintegrasikan server Minecraft (Paper 26.2) dengan portal web produksi Azuriom secara mulus menggunakan plugin jembatan **`apexsions-bridge`** dan arsitektur tema kustom **`themes/apexsions`**.

### A. Sinkronisasi Data Dua Arah (`ApexsionsCore` $\leftrightarrow$ `apexsions-bridge`):
1. **Endpoint REST API Terproteksi**:
   - `POST http://web.apexsions.my.id/api/apexsions-bridge/sync-player`
   - Diproteksi menggunakan secret key live: `apexsions_bridge_key_live_2026`.
   - Mengirimkan payload JSON asinkron dari `WebBridgeService` di `ApexsionsCore` saat pemain bergabung (*Join*), keluar (*Quit*), naik level (*LevelUp*), ubah kerajaan, ubah saldo Rupiah/Diamond, atau ubah gelar aktif.
2. **Entitas Model `MinecraftAccount` di Database Web**:
   - Menyimpan `minecraft_uuid`, `minecraft_username`, `edition` (JAVA / BEDROCK), `level`, `xp`, `rank`, `kingdom`, `balance_rupiah`, `balance_diamond`, `unlocked_titles`, `active_title`, `verified_at`, dan `last_daily_reward_at`.
3. **Penautan Akun Mandiri (`/link`)**:
   - Pemain menjalankan `/link` di Minecraft untuk mendapatkan 6-digit PIN acak berbatas waktu (15 menit).
   - Memasukkan PIN pada form web `/link` memverifikasi kepemilikan akun Minecraft secara aman.

### B. Dewan Kehormatan & Papan Peringkat (`/leaderboard`):
1. **Dominasi Tiga Kerajaan**:
   - Menghitung agregat total populasi dan akumulasi kekuatan level peradaban untuk *Zenithar*, *Solterra*, dan *Sylvamoor*.
2. **Top 10 Pengelana & Konglomerat**:
   - Tabel 1: 10 Pemain dengan Level & XP tertinggi (badge rank donatur/kasta resmi).
   - Tabel 2: 10 Pemain terkaya dengan saldo Rupiah (Rp) dan Diamond murni (💎).
   - Seluruh baris pemain memiliki tautan menuju profil unik `/player/{uuid}`.

### C. Keamanan Profil Publik & URL Berbasis ID Unik (`/player/{identifier}`):
1. **Perlindungan Privasi Standar Web Modern**:
   - Menghapus ketergantungan pada username mentah di URL profil publik untuk mencegah *user enumeration* dan penargetan bot pihak ketiga.
   - Menggunakan **UUID resmi Mojang/Bedrock** (atau primary key ID unik akun) sebagai slug resmi: `/player/7b153d42-48d3-3192-a404-f98405035c0e`.
2. **Automatic 301 Canonical Redirect**:
   - Permintaan ke URL username lama (`/player/NamaPemain`) secara otomatis dialihkan dengan status **HTTP 301 Moved Permanently** ke URL ID unik.

### D. Sistem Terjemahan Bilingual Menyeluruh (ID 🇮🇩 & EN 🇬🇧):
1. **Engine `APX_I18N` Klien**:
   - Terintegrasi di `Website/themes/apexsions/assets/js/app.js` (mirrored ke public assets).
   - Beroperasi instan tanpa reload browser, mempertahankan posisi scroll dan state halaman.
   - Membaca preferensi bahasa dari `localStorage` (default `id`).
2. **Cakupan Kamus Lengkap**:
   - Navigasi & Hero
   - 11 Kasta Sosial (Ancestor s/d Wanderer)
   - Profil Akun & Hadiah Harian
   - Penautan Minecraft (`/link`)
   - Webstore Donasi, Keranjang & Riwayat Transaksi
   - Leaderboard & Kartu Profil 3D Karakter
   - Aturan Server (4 Pilar & Pencegahan Sistem)
   - Kebijakan Privasi & Syarat Ketentuan
   - Vote Server & Warta Berita Komunitas
   - Ensiklopedia Wiki & Hasil Pencarian


