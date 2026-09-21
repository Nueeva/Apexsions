# Dokumentasi Master Apexsions Plugin Suite — Minecraft 26.2 (The Peak Civilizations)

Dokumentasi resmi yang merangkum arsitektur menyeluruh, interaksi antar-plugin, matriks izin & perintah, konfigurasi modular, serta integrasi gameplay untuk 9 plugin utama di ekosistem **Apexsions**.

---

## 🏛️ 1. Ikhtisar Arsitektur 9 Plugin (Plugin Ecosystem Matrix)

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
          └───────────────────┘└───────────────────┘└───────────┬───────────┘
                                                                │
                                                    ┌───────────▼───────────┐
                                                    │   ApexsionsCrates     │
                                                    │ (Keys, Pity & Opening)│
                                                    └───────────┬───────────┘
                                                                │
                                                    ┌───────────▼───────────┐
                                                    │   ApexsionsFishing    │
                                                    │(AFK, Vaults & Anglers)│
                                                    └───────────────────────┘
```

1. **`ApexsionsCore`** (`com.apexsions.core.*`): Otoritas wilayah 3 Kerajaan (`Zenithar`, `Solterra`, `Sylvamoor`), progresi level (1-100) & 16 sumber XP, BlueMap polygon rendering, sistem `/rtp` terikat kerajaan, Kingdom War Manager, PvP Combat Tag (15s), proteksi PvP teritorial kerajaan, Title Vault GUI, Particle Cosmetics GUI, sistem Warp GUI & Admin Warp Manager, Player Inspector GUI, kit kerajaan terintegrasi (`/kits`), WebBridge asynchronous delivery queue (Online & Offline), dan NightCore Native Dialog Input GUI (`CustomInputTextGUI`) untuk Paper 26.2.
2. **`ApexsionsChat`** (`com.apexsions.chat.*`): Sistem komunikasi Adventure/MiniMessage dengan channel (`Global`, `Kingdom`, `Staff`), preferensi obrolan GUI (`/channel settings`), ID-Card sosial (`/channel profile <p>`), pamer item (`/showitem`), surat offline (`/mail`), chat games, pengumuman otomatis, sistem nickname kustom & token rename (`/nick`, `/realname`), dan sistem moderasi lapis tiga dengan Staff Reports Investigation Desk 54-slot (`/reports`).
3. **`ApexsionsEconomy`** (`com.apexsions.economy.*`): Multi-Currency atomic (`Rupiah`, `Diamond`), Transfer (`/pay`), Pasar Lelang (`/ah`) dengan Escrow Claim, Sistem Barter/Trade 12-Slot terintegrasi kerajaan & pajak transportasi lintas-kerajaan, serta dukungan penuh console / web delivery (`/eco give/take/set/reload`).
4. **`ApexsionsBattlepass`** (`com.apexsions.battlepass.*`): Season battlepass 200 level, Quests (Daily, Weekly, Monthly), Tingkatan Pass (`Citizen`, `Sio`, `Exsio`), Toko Rotasi (*Dynamic Shop*), dan Editor Admin GUI 54-Slot (`/abp`).
5. **`ApexsionsShop`** (`com.apexsions.shop.*`): Pasar & toko dinamis 6 kategori (`blocks`, `farming`, `food`, `ores`, `mob_drops`, `dyes`), rasio jual dasar **20%**, formula multiplier cuaca & bioma kerajaan, price clamping (85%-120%), siaran tren pasar berkala, dashboard tren `/shop trends`, pajak wilayah per-kerajaan (18%/20%/15%), UI ramah sentuh/Bedrock, dan GUI jual cepat 45-slot (`/sell`).
6. **`ApexsionsMedia`** (`com.apexsions.media.*`): Sistem render banner/logo gambar multi-tile asinkron (PNG/JPG/URL), raytrace line-of-sight hover glowing & actionbar tooltip, serta aksi interaksi tautan URL web/salin clipboard terkonfirmasi (100% vanilla & Bedrock compatible).
7. **`ApexsionsCustomEnchants`** (`com.apexsions.customenchants.*`): Dual-Currency Enchanter Gacha GUI (`/ce`), Toko Buku Sihir Spesifik 54-Slot (`/ce shop`), 182 Custom Enchantments lintas 7 tier kekuatan, Mystery & Magic Dust, White & Black Scrolls, Central Admin Hub (`/ace`), Katalog `/ace enchants`, dan Interactive Armor Set Builder (`/ace create`) dengan sinkronisasi ID otomatis.
8. **`ApexsionsCrates`** (`com.apexsions.crates.*`): Toko Kunci Crate (`/crateshop`), sistem animasi pembukaan berbasis paket (PacketEvents/ProtocolLib), milestone progression, unified tiered effective weight chance formula, dan integrasi hadiah ekonomi / kit.
9. **`ApexsionsFishing`** (`com.apexsions.fishing.*`): Sistem AFK Fishing interaktif & Active Reel Engine, Rarity & Weight Engine 6-tier (`COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`, `SECRET`), Fishing Vault brankas penyimpanan (`/vault`), Toko Penjualan Ikan & Delivery Market (`/fish sell`), Auto-Catch Rods Creator & Upgrade Engine (`/fish shop`), dan Leaderboard Top Angler (`/fish top`) terintegrasi kebijakan pengecualian staf.

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

### E. Integrasi Citizens NPC & Native Command Binding (`/k`)
- **Penanganan Bebas Konflik**: Seluruh listener dan custom traits hardcoded telah ditiadakan dari `CitizensHook` agar tidak mencegat interaksi klik pemain.
- **Native Command Execution**: Admin mengaitkan aksi NPC menggunakan mekanisme resmi bawaan Citizens:
  ```bash
  /npc sel <id>
  /npc cmd add -p k
  ```
- **Alur Cerdas `/k`**: Pemain yang belum berikrar otomatis diarahkan ke menu pemilihan kerajaan (`RegionSelectionGUI`), sedangkan pemain yang telah bersumpah setia langsung dipindahkan ke ibukota kerajaannya via `RegionTeleportService`.

### F. Ekosistem Pemancingan & Integrasi Ekonomi/Core (`ApexsionsFishing` ↔ `ApexsionsEconomy`, `ApexsionsCore`, & `ApexsionsCustomEnchants`)
- **Dual-Currency Fish Market:** Penjualan tangkapan ikan (`/fish sell`) langsung mentransfer saldo Rupiah atau Diamond ke akun pemain secara atomic via `ApexsionsEconomyAPI`.
- **Top Angler Leaderboard Exemption:** Papan peringkat nelayan terbaik (`/fish top`) menyaring akun staf, OP, dan entitas Aetherion menggunakan `ApexsionsCoreAPI.isLeaderboardExempt(uuid)` guna memastikan supremasi kompetisi dipegang oleh warga fana.
- **Custom Rods & Enchantment Compatibility:** Pancingan dari `AutoCatchRodManager` terintegrasi harmonis dengan enchantments dari `ApexsionsCustomEnchants` serta progression hooks.

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
| `/kingdom` | `/k`, `/region` | Teleportasi ke ibukota kerajaan (jika sudah berikrar) atau membuka menu pemilihan (jika belum berikrar) | `apexsionscore.command.region` | `true` |
| `/kingdom info` | `/k info`, `/k profile` | Membuka profil dan status statistik kerajaan pemain | `apexsionscore.command.level` | `true` |
| `/kingdom choose` | `/k select` | Membuka menu pemilihan 3 kerajaan | `apexsionscore.command.region` | `true` |
| `/kingdom top` | `/k leaderboard`| Membuka Hall of Fame & Leaderboard GUI | `apexsionscore.command.level` | `true` |
| `/kingdom setspawn <k>`| `/k setspawn` | Menetapkan titik spawn ibukota kerajaan (Admin) | `apexsionscore.admin` | `op` |
| `/kingdom setking <k> <p>` | - | Mengangkat pemain menjadi Raja kerajaan | `apexsionscore.admin` | `op` |
| `/kingdom unsetking <k>` | `/kingdom removeking` | Mencabut gelar Raja dari kerajaan | `apexsionscore.admin` | `op` |
| `/level` | `/lvl`, `/profile`, `/exp`, `/rewards` | Membuka GUI progress bar level (1-100) & hadiah | `apexsionscore.command.level` | `true` |
| `/xpguide` | - | Panduan detail 16 sumber perolehan XP | `apexsionscore.command.level` | `true` |
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
| `/link [pin]` | `/tautkan` | Menautkan akun in-game dengan portal web Azuriom | `apexsionscore.command.link` | `true` |
| `/deathcoords` | `/lastdeath`, `/kor`, `/cor` | Menampilkan koordinat kematian terakhir & kompas navigasi | `apexsions.core.deathcoords` | `true` |
| `/deathcoords compass` | - | Mengarahkan jarum kompas held-item ke titik kematian | `apexsions.core.deathcoords` | `true` |
| `/deathcoords <player>`| - | Memeriksa koordinat kematian pemain lain (Staf/Admin) | `apexsions.core.deathcoords.others` | `op` |
| `/deathcoords tp <p>` | - | Teleportasi cepat staf ke koordinat kematian pemain | `apexsions.core.deathcoords.tp` | `op` |

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
| `/economy top` | `/bal top`, `/eco leaderboard` | Menampilkan peringkat kekayaan server | `apexsionseconomy.use` | `true` |
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
| `/bp info` | `/bp stats`, `/bp level`, `/bp progress`, `/bp status` | Menampilkan level, pass tertinggi, dan progress XP | `apexsionsbattlepass.use` | `true` |
| `/bp quests` | `/bp quest` | Membuka daftar misi harian/mingguan/bulanan | `apexsionsbattlepass.use` | `true` |
| `/bp rewards` | `/bp pass`, `/bp passes` | Membuka menu klaim hadiah & tingkatan pass | `apexsionsbattlepass.use` | `true` |
| `/bp shop` | `/bp store` | Membuka toko rotasi penukaran BP-XP | `apexsionsbattlepass.use` | `true` |
| `/bp season` | - | Memeriksa status, waktu tersisa, dan periode season | `apexsionsbattlepass.use` | `true` |
| `/abp` | `/bpadmin`, `/adminbp` | Membuka panel kontrol visual editor 54-slot | `apexsionsbattlepass.admin` | `op` |
| `/abp reload` | - | Reload seluruh konfigurasi pass, quests, rewards, & season | `apexsionsbattlepass.admin` | `op` |
| `/abp givepass <p> <pass>` | - | Memberikan pass (`citizen`, `sio`, `exsio`)| `apexsionsbattlepass.admin` | `op` |
| `/abp setlevel <p> <lvl>`| - | Menyetel level BattlePass pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp addxp <p> <amount>`| - | Menambahkan XP BattlePass pemain | `apexsionsbattlepass.admin` | `op` |
| `/abp currency <p> <add\|set\|take> <amt>` | `/abp resetrefresh` | Mengelola Battle Coins & refresh kuota toko | `apexsionsbattlepass.admin` | `op` |
| `/abp season <...>` | - | Mengelola status & periode season aktif | `apexsionsbattlepass.admin` | `op` |
| `/abp reset <p>` | - | Mereset total seluruh data progresi BattlePass pemain | `apexsionsbattlepass.admin` | `op` |

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
| `/ce` | `/enchanter`, `/customenchants` | Membuka Enchanter Gacha Dual-Currency GUI | `apexsions.customenchants.use` | `true` |
| `/ce shop` | `/ce toko` | Membuka Toko Buku Sihir Spesifik 54-Slot | `apexsions.customenchants.use` | `true` |
| `/ce tinkerer` | - | Membuka antarmuka Tinkerer Kerajaan (Coming Soon) | `apexsions.customenchants.use` | `true` |
| `/ace` | `/apexsionscustomenchants`, `/aceadmin` | Central Admin Hub GUI (45-Slot) | `apexsions.admin` | `op` |
| `/ace enchants` | - | Katalog interaktif replika AdvancedEnchantments | `apexsions.admin` | `op` |
| `/ace create` | - | Interactive Item & Armor Set Builder dengan sinkronisasi ID | `apexsions.admin` | `op` |
| `/ace pricing` | - | Konfigurasi harga gacha, rate, dan multiplier | `apexsions.admin` | `op` |
| `/ace reload` | - | Reload konfigurasi custom enchants, tiers, dan sets | `apexsions.admin` | `op` |
| `/presets` | `/preset`, `/acepresets` | Membuka menu preset armor & tool set tersimpan | `apexsions.admin` | `op` |

---

### 🎁 ApexsionsCrates
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `/crateshop` | `/keyshop`, `/cratekeyshop` | Membuka Toko Pembelian Kunci Peti Dual-Currency GUI | `apexsionscrates.command.menu` | `true` |
| `/crate` | `/crates` | Menampilkan antarmuka daftar peti hadiah dan milestone | `apexsionscrates.command.menu` | `true` |
| `/crate open <id>` | - | Membuka peti hadiah tertentu | `apexsionscrates.command.open` | `true` |
| `/crate preview <id>` | - | Pratinjau daftar hadiah peti | `apexsionscrates.command.preview` | `true` |
| `/crate key <give\|take\|set\|show> <p> <crate> <amt>` | - | Mengelola jumlah kunci peti pemain (Admin) | `apexsionscrates.command.key` | `op` |
| `/crate give <p> <crate> [amt]` | - | Memberikan item/kunci peti kepada pemain (Admin) | `apexsionscrates.command.give` | `op` |
| `/crate editor` | - | Editor interaktif peti, hadiah, dan milestone (Admin) | `apexsionscrates.command.editor` | `op` |
| `/crate reload` | - | Memuat ulang seluruh konfigurasi peti dan probabilitas | `apexsionscrates.command.reload` | `op` |

---

### 🎣 ApexsionsFishing
| Perintah | Alias | Deskripsi | Hak Akses | Default |
| :--- | :--- | :--- | :--- | :---: |
| `/fish` | `/fishing`, `/mancing` | Membuka Menu Utama Peradaban Memancing Apexsions | `apexsions.fishing.use` | `true` |
| `/vault` | `/fishvault`, `/fvault` | Membuka Fishing Vault brankas penyimpanan hasil pancingan 54-slot | `apexsions.fishing.vault` | `true` |
| `/fish shop` | `/fish toko` | Membuka Toko Joran Spesial & Auto-Catch Rods (`RodShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish vaultshop` | `/fish belibrankas` | Membuka Toko Peningkatan Kapasitas Brankas Ikan (`VaultShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish sell` | `/fish jual` | Membuka Antarmuka Penjualan Ikan & Delivery Market (Dual-Currency) | `apexsions.fishing.use` | `true` |
| `/fish top` | `/fish leaderboard`, `/fish peringkat` | Papan peringkat Top Angler | `apexsions.fishing.use` | `true` |
| `/fish journal` | `/fish pedia`, `/fish jurnal` | Ensiklopedia spesies ikan & hasil tangkapan | `apexsions.fishing.use` | `true` |
| `/fish bait` | `/fish umpan` | Membuka Toko Kuota Umpan Virtual (`BaitShopGUI`) | `apexsions.fishing.use` | `true` |
| `/fish admin` | - | Panel Administrasi Nelayan (Admin Hub GUI) | `apexsions.fishing.admin` | `op` |
| `/fish creator` | `/fish create` | Native Dialog Admin Rod Creator GUI | `apexsions.fishing.admin` | `op` |
| `/fish reload`| - | Memuat ulang konfigurasi ikan, rarity, bioma, dan bobot tangkapan | `apexsions.fishing.admin` | `op` |

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
powershell -ExecutionPolicy Bypass -File .\build.ps1 Crates
powershell -ExecutionPolicy Bypass -File .\build.ps1 Fishing

# 2. Kompilasi Seluruh Suite (Gunakan HANYA jika semua modul berubah):
powershell -ExecutionPolicy Bypass -File .\build.ps1 -all
```

---

## 📜 5. Kesinambungan Lore & Mekanik Gameplay (The Narrative Mechanics Matrix)

Ekosistem Apexsions tidak sekadar kumpulan plugin teknis terpisah, melainkan perwujudan langsung dari narasi kanonik dunia **Apexsions — The Peak Civilizations**:

### A. Kisah Agung: Kejatuhan Sions & Lahirnya 3 Peradaban
Dahulu kala, benua ini dipersatukan di bawah satu imperium agung yang membentang tanpa batas: **Kekaisaran Kuno Sions**. Namun, ambisi pemimpin terakhirnya untuk melipatgandakan kekuatan pasukan dengan menyerap energi terlarang dari dimensi kegelapan (*Dark Dimension*) memicu malapetaka dahsyat (*The Great Rupture*). Kekaisaran runtuh dalam kehancuran kosmis, memaksa rakyatnya tercerai-berai:
1. **Zenithar (Arah Timur / Zenith Cakrawala)**:
   - *Latar Belakang*: Keluarga dinasti kerajaan, bangsawan berdarah murni, dan kavaleri suci yang berhasil mempertahankan diri dan hijrah ke arah timur pegunungan kristal.
   - *Karakteristik & Buff*: Pusat perbankan, pasar lelang, kavaleri istana, dan pertahanan reduksi (*Buff: Speed +8%, Luck +15%, [Royal Discipline] Damage +6%, [Royal Aegis] 20% Reduksi Damage Masuk, Diskon 30% Pajak Lelang, Bunga Bank +25%, Diskon 15% Blok Toko; Debuff: Kerentanan Racun & Wither +15%, +12% Cepat Lapar, -10% Kecepatan Menambang, Biaya Tempa Anvil +1 EXP*).
2. **Solterra (Arah Selatan / Kawah Emas Vulkanik)**:
   - *Latar Belakang*: Para ahli sihir tempur (Magicians), alkemis, dan tentara tangguh berpengalaman yang memisahkan diri ke tanah tandus dan lembah cadas selatan.
   - *Karakteristik & Buff*: Menguasai peleburan bijih logam, kekuatan fisik destruktif, dan api (*Buff: Serangan Tinggi, Critical Hit, Mining Haste; Debuff: -2 HP Darah, Rentan Kerusakan, Cepat Lapar*).
3. **Sylvamoor (Arah Barat / Belantara Kanopi Purba)**:
   - *Latar Belakang*: Kaum pekerja, pemburu, petani, serta tentara non-magis yang bersatu dan bermigrasi ke hutan rimba raksasa barat.
   - *Karakteristik & Buff*: Mengembangkan keahlian hidup berdampingan dengan alam, foraging, dan kelincahan berburu (*Buff: +2 HP Darah, Pertahanan Rimba +15%, Racun Stop di 3 Hati, Drop Rate Melimpah; Debuff: Mabuk Ketinggian, Kerentanan Api*).

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
    ├── 9 Plugin Suite Utama Modular & Terintegrasi
    ├── Sistem 3 Kerajaan (Zenithar, Solterra, Sylvamoor) & Poligon BlueMap
    ├── Progresi Karakter 1-100 & 16 Sumber XP
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
   - Kolom `user_id` bersifat nullable sehingga setiap pemain in-game langsung tercatat profil statistiknya di database web meskipun belum membuat/menautkan akun website.
3. **Penautan Akun Mandiri (`/link`)**:
   - Pemain menjalankan `/link` di Minecraft untuk mendapatkan 6-digit PIN acak berbatas waktu (15 menit).
   - Memasukkan PIN pada form web `/link` memverifikasi kepemilikan akun Minecraft secara aman dan menautkannya dengan akun web pengguna.
4. **Siaran Pengumuman Global In-Game (`broadcast` / `bc`)**:
   - Admin dapat mengirimkan siaran langsung ke server Minecraft melalui Web Dashboard (`POST /admin/apexsions/broadcast`).
   - Dispatched melalui antrean `deliveries` dengan target `GLOBAL` / `ALL_PLAYERS` (kolom UUID nullable).
   - Server memproses pengumuman menggunakan parser native Kyori Adventure `MiniMessage` dan membunyikan efek audio lonceng (`BLOCK_NOTE_BLOCK_BELL`) ke seluruh pemain online.

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

### E. Apexsions Unified Admin Dashboard & Safe Plugin Control:
1. **10 Modul Pusat Operasi Server (`/admin/*`)**:
   - **Dashboard Eksekutif (`/admin`)**: Status kesehatan Paper 26.2, TPS, RAM, player count, dan insiden aktif.
   - **Player Management 360 (`/admin/players`)**: Investigasi profil menyeluruh (UUID, level, rank, kingdom, dual balance, punishment history, transaction history) dan aksi administratif terotorisasi.
   - **Moderation Desk (`/admin/moderation`)**: Penindakan sanksi (Ban, Mute, Warn, Kick) dan pelacakan status penyelesaian laporan pemain.
   - **Economy Operations (`/admin/economy`)**: Ledger transaksi atomic (Rupiah & Diamonds), audit pasar lelang (`/ah`), dan saldo perbendaharaan Kingdom.
   - **Server Operations (`/admin/server`)**: Telemetri runtime real-time, Safe Server Actions, dan pengelolaan Maintenance Mode.
   - **Custom Plugin Suite (`/admin/custom-plugins`)**: Registry 9 plugin custom Apexsions, monitoring health status, dan eksekusi aksi aman (*Safe Actions*).
   - **Intelligence & Incident Center (`/admin/incidents`)**: Deteksi anomali berbasis aturan (Rule-based), korelasi event otomatis, dan berkas investigasi staf.
   - **Notifications Hub (`/admin/notifications`)**: Pengiriman alert insiden kritis dengan proteksi deduplikasi anti-spam dan cooldown.
   - **Safe Automation Hub (`/admin/automation`)**: Orkestrasi kebijakan otomatis dengan *Approval Gate* wajib untuk tindakan sensitif (reload, dsb.).
   - **Unified Audit Log (`/admin/audit-logs`)**: Rekam jejak immutable dari seluruh aksi administratif web maupun in-game.
2. **Arsitektur Antrean Terpercaya (Bridge Action Reliability)**:
   - Setiap aksi dari dashboard diterbitkan dengan `action_id` unik server-generated.
   - Whitelisted command templates (bukan raw terminal arbitrary) mencegah injeksi perintah berbahaya.
   - **Multi-Command Execution Engine**: Antrean mendukung compound commands (seperti penetapan rank LuckPerms dan permission flags) dengan splitting regex `[;\n]+`. Setiap baris perintah dieksekusi secara sekuensial di Bukkit main-thread, menghilangkan kegagalan konsol Minecraft akibat karakter titik koma (`;`).
   - **Native Tellraw & Alert Interceptor**: Mengintersepsi perintah `tellraw <player> <json>` dan `minecraft:tellraw <player> <json>` via Kyori Adventure `GsonComponentSerializer`. Untuk pemain online, pesan dikirimkan ke chat dengan efek suara `BLOCK_NOTE_BLOCK_CHIME`. Untuk pemain offline, delivery ditandai `DELIVERED` secara anggun tanpa menimbulkan deadlock antrean.
   - Daemon `WebBridgeService` in-game mem-poll antrean `/api/apexsions-bridge/deliveries/pending` dan melaporkan status keberhasilan eksekusi (`DELIVERED` / `FAILED`) secara asinkron.
3. **Ingestion Audit Log In-Game**:
   - Aksi staf via in-game `PlayerInspectorGUI` secara otomatis di-push ke endpoint `/api/apexsions-bridge/audit/log` untuk tercatat di database sentral.

### F. Sistem Kasta (Rank), Upgrade Engine & Webstore Integration:
1. **Hierarki 5 Tingkat Donatur Resmi**:
   - `Ascendant` (Tier 1, Weight 30)
   - `Archon` (Tier 2, Weight 40)
   - `Sovereign` (Tier 3, Weight 50)
   - `Emperor` (Tier 4, Weight 60)
   - `Sions` (Tier 5, Weight 70)
2. **Pemisahan Ketat Trial vs. Permanent**:
   - **Trial (30 Hari / 120 Hari)**: Mendapatkan seluruh benefit batas limit & command, tetapi tanpa bonus uang server permanent. Expire otomatis dan mengembalikan pemain ke rank permanent sebelumnya (*Rank Retention*).
   - **Permanent**: Berlaku selamanya, mendapat hadiah satu kali server money ledger, dan memiliki hak upgrade rank.
   - **Isolasi Reward Uang Tunai Permanen**: Bersifat ketat non-kumulatif (Sions menerima Rp 300.000, Emperor Rp 180.000, tanpa penambahan kumulatif rank di bawahnya). Anti-duplikasi dijamin melalui constraint tabel `apexsions_rank_rewards_claimed`.
3. **Rank Upgrade Engine**:
   - Upgrade eksklusif untuk rank Permanent ke tingkat lebih tinggi.
   - Perhitungan harga upgrade dinamis ($Target - Current$) dengan kemampuan admin override di `RankConfig`.
   - Otomatis memperbarui grup LuckPerms in-game dan menyerahkan selisih bonus uang permanent tanpa duplikasi.
4. **Diskon BattlePass & Pass Hierarchy**:
   - Terintegrasi otomatis server-side: Emperor Permanent mendapat diskon 10% (Sio Pass masa depan), Sions Permanent mendapat diskon 15% (Sio & Exsio Pass masa depan).
   - **Pass Hierarchy**: Pemain dengan `Exsio Pass` otomatis memiliki akses penuh ke seluruh reward dan fungsionalitas `Sio Pass` dan `Citizen Pass` (`PlayerData.java`).
   - WhatsApp order click-to-chat menggunakan template dinamis terkonfigurasi dengan format harga Rupiah presisi.
5. **Sistem Vote & Civic Rewards End-to-End**:
   - Setiap vote valid memberikan 3x Vote Crate Keys (`crates key give <p> vote 3`), uang tunai Rp 1.000 (`ecoadmin give <p> 1000 rupiah`), dan alert apresiasi tellraw.
   - Transaksi diproteksi oleh SHA-256 idempotency hash berbasis tanggal/jam untuk mencegah eksploitasi reward ganda.
6. **Pusat Manajemen Rank Admin (`/admin/ranks`)**:
   - Konfigurasi lengkap harga, batas benefit, template pesan WhatsApp, dan pelacakan riwayat transaksi/upgrade di `/admin/ranks/purchases`.

### G. Kebijakan Pengecualian Leaderboard 6-Lapis (Leaderboard Exemption Contract):
1. **Penyaringan Otomatis Multi-Layer**:
   - Menjamin bahwa seluruh posisi teratas papan peringkat murni diperebutkan oleh warga fana biasa (*Citizens* & *Donators*).
   - Akun staf, OP, dan entitas Aetherion disaring keluar dari seluruh leaderboard publik melalui 6 lapisan verifikasi:
     - **Bukkit Operator (OP)**: `player.isOp()` / `ops.json`.
     - **Staff Rank Weight $\ge 80$**: `ancestor` (100), `architect` (95), `overseer` (95), `warden` (90), `herald` (80).
     - **Afiliasi Kerajaan Transenden Aetherion**: `kingdom_id = 'AETHERION'` (The Aetherial Conclave).
     - **Permission Nodes**: `apexsions.admin`, `apexsions.staff`, `apexsionscore.admin`, `apexsions.conclave`, `apexsions.leaderboard.exempt`.
     - **Azuriom Web Administrator**: `role->is_admin == true`.
     - **Blacklist Akun Staf/Founder**: `nueeva`, `nuevaid`, `rifqi`, `friell`, `favian`, `fanerf`, `kazrienvall`.
2. **Cakupan Penyaringan In-Game & Web**:
   - In-Game: `/kingdom top` (`ApexsionsCore`), `/economy top` (`ApexsionsEconomy`), `/abp top` (`ApexsionsBattlepass`), `/fish top` (`ApexsionsFishing`).
   - Web Platform (`https://web.apexsions.my.id/leaderboard`): Menampilkan secara ketat **2 Tabel Utama** (Level & Saldo Rupiah). Leaderboard BattlePass ditiadakan dari portal web (eksklusif in-game) demi menjaga kesederhanaan, performa, dan fokus antarmuka web.

---

## 🔐 11. Integrasi Autentikasi Lintas Platform: FastLogin, Floodgate & AuthMeReloaded

Mengintegrasikan ekosistem autentikasi aman tanpa hambatan (*zero-friction*) bagi pemain Java Original dan Bedrock, sekaligus memproteksi akun pemain crack dari pembajakan nama:

### A. Matriks Kompatibilitas Runtime
- **Server Engine:** Paper version 26.2-92-main (Minecraft 26.2, Java 21 LTS).
- **Packet Interceptor:** ProtocolLib v5.4.0.
- **Cross-Platform Bridge:** Geyser-Spigot v2.11.3 + Floodgate v2.2.5 (Prefix: `.`).
- **Auth Core:** AuthMeReloaded v6.0.1-b2770 (`plugins/AuthMe/authme.db`).
- **Auto-Login Layer:** FastLogin v1.12-kick-toggle (`plugins/FastLogin/FastLogin.db`).

### B. Konfigurasi Otoritatif (`plugins/FastLogin/config.yml`)
- `autoLoginFloodgate: true`: Pemain Bedrock login otomatis via enkripsi sesi Xbox Live Floodgate setelah registrasi awal.
- `autoRegister: false`: Melindungi pemain crack veteran yang memakai nickname Mojang agar tidak terkunci dengan password acak.
- `premiumUuid: false`: Menjaga seluruh data pemain dan link profil web `/player/{uuid}` tetap konsisten pada offline UUID (`OfflinePlayer:<name>`).
- `allowFloodgateNameConflict: false`: Mengamankan namespace Bedrock dari potensi tabrakan nama dengan pemain Java.
- `prevent-proxy-connections=false` di `server.properties` $\leftrightarrow$ `useProxyAgnosticResolver: true` harmonis.

### C. Daftar Perintah & Izin Autentikasi
| Perintah | Alias | Izin Default | Target Pengguna | Fungsi |
|---|---|---|---|---|
| `/premium` | `/prem`, `/loginfast` | `fastlogin.bukkit.command.premium` | Java Premium | Menandai akun sebagai akun berbayar Mojang (konfirmasi 2x + 1x kick by-design). |
| `/cracked <player>` | `/unpremium` | `fastlogin.bukkit.command.cracked` | Staf (Warden+) | Mencabut status premium dan memulihkan akun ke login kata sandi AuthMe. |
| `/fldelete <player>` | - | `fastlogin.bukkit.command.delete` | Admin (OP) | Menghapus entri profil pemain dari `FastLogin.db`. |

### D. Konfigurasi Izin LuckPerms
```powershell
# Blokir pemain Bedrock agar tidak memicu verifikasi Java Mojang
lp group default permission set fastlogin.bukkit.command.premium false context[origin=bedrock]

# Berikan izin recovery akun ke jajaran staf Tier III (Warden) & Tier IV (Overseer)
lp group warden permission set fastlogin.bukkit.command.cracked true
lp group overseer permission set fastlogin.bukkit.command.cracked true
```

### E. Integrasi PlaceholderAPI
- `%fastlogin_status%`: Mengembalikan nilai `Premium` atau `Cracked`. Terintegrasi dengan TAB scoreboard 6.1.2 dan Staff Player Inspector.

---

## 🛠️ 19. Perkakas Pengembang & Knowledge Graph Plugin Suite (`graphify`)

Untuk memudahkan penelusuran arsitektur 9 plugin dan ratusan kelas internal Paper API, repositori telah dilengkapi dengan database **Knowledge Graph Graphify** (`graphify-out/`):

1. **Perintah Cepat CLI untuk Developer Plugin:**
   - Menelusuri seluruh kelas dan listener yang mengonsumsi suatu API:
     ```bash
     graphify query "ApexsionsCoreAPI"
     graphify query "PlayerListener"
     ```
   - Menemukan dependensi antar-plugin:
     ```bash
     graphify path "ApexsionsEconomyAPI" "DynamicMarketService"
     ```
   - Penjelasan struktur kelas/modul:
     ```bash
     graphify explain "ApexsionsCorePlugin"
     ```
2. **Sinkronisasi Pasca-Kompilasi Maven:**
   - Setelah menambahkan listener, event, atau service baru di plugin Minecraft, jalankan:
     ```powershell
     graphify update .
     ```
   - Pembaruan graf berjalan seketika (AST Tree-Sitter lokal) tanpa biaya token API.
3. **Visualisasi Arsitektur Interaktif:**
   - Buka `graphify-out/graph.html` di browser untuk melihat peta klaster relasi 9 plugin secara visual.

---

## 🛡️ 20. Apexsions Security & Anti-Cheat Suite (Mitigasi Fly Hack, Auth Bypass, Combat & Exploits)

Sistem keamanan terpusat di `ApexsionsCore` (`com.apexsions.core.security.*`) yang dirancang khusus untuk memitigasi cheat client modern (seperti mod Fly Hack CurseForge, Meteor, Wurst, LiquidBounce, Aristois) serta celah eksploitasi otentikasi.

### A. Sub-Sistem Keamanan & Mitigasi Cheat
| Komponen | Kelas Sumber | Tipe Ancaman / Cheat | Metode Mitigasi & Aksi |
|---|---|---|---|
| **Movement Security** | `MovementSecurityListener` | **Fly Hack**, AirWalk, Hovering, Glide, Creative-Fly | Deteksi pergerakan vertikal $\Delta y \ge 0$ di udara $> 6$ tick tanpa status flight yang sah. Aksi: *Rubberband* ke lokasi aman di tanah + notifikasi staf jika berulang. |
| | | **Horizontal Speed Hack**, Timer | Memantau $(\Delta x^2 + \Delta z^2)$ dengan memperhitungkan efek ramuan Speed, Soul Speed, dan knockback combat. |
| | | **Jesus / WaterWalk** | Mendeteksi pemain yang berjalan di atas permukaan air/lava dengan `onGround = true` tanpa sepatu Frost Walker. |
| | | **NoFall (Spoofed Packets)** | Server melacak jarak jatuh nyata; memberikan damage jatuh independen saat mendarat meskipun paket klien memalsukan `fallDistance = 0`. |
| **Auth Gatekeeper** | `AuthSecurityGateKeeper` | **Auth Bypass** (Command & Event Glitch) | Berjalan pada `LOWEST` priority: memblokir seluruh perintah non-auth, interaksi inventaris, buka peti, lempar/ambil item, dan serangan entitas sebelum pemain terotentikasi. |
| | | **Session Hijacking** (IP Bersama) | Penonaktifan `sessions.enabled` di AuthMe untuk menghapus celah auto-login pada WiFi publik/CGNAT. |
| | | **Staff Brute-Force & Takeover** | Mengunci akun jajaran Staf (`ancestor`, `architect`, `overseer`, `warden`, `herald`). Percobaan login salah $\ge 3$ kali memutus koneksi, memblokir IP 10 menit, dan menyiarkan peringatan darurat ke staf & konsol. |
| **Combat Guard** | `CombatSecurityListener` | **KillAura (Angle Check)** | Menghitung sudut vektor antara arah mata penyerang dan korban. Serangan dengan sudut $> 95^\circ$ (memukul ke samping/belakang) otomatis dibatalkan. |
| | | **Wall-Hit (Phase Strike)** | Raycast oklusi blok padat di antara mata penyerang dan hitbox korban. Mencegah memukul menembus dinding/pintu. |
| | | **Combat Reach Hack** | Membatasi jarak jangkauan serangan maksimal $4.2$ blok di mode Survival (dengan kompensasi latency & bounding box). |
| | | **Auto-Clicker** | Membatasi frekuensi serangan maksimal 20 CPS per detik. |
| **Packet Sanitizer** | `PacketExploitListener` | **BadPackets (Crash Exploits)** | Menolak dan mengoreksi nilai pitch di luar rentang fisik $[-90.0^\circ, +90.0^\circ]$. Menendang pemain yang mengirim koordinat `NaN` atau `Infinity`. |
| | | **Scaffold / FastPlace** | Membatasi penempatan blok maksimal 14 blok/detik di mode Survival. |
| | | **ChestStealer** | Membatasi interaksi pemindahan item dari wadah/peti maksimal 12 klik/detik. |

### B. Matriks Hak Izin Bypass Anti-Cheat (Khusus Pengujian / Admin)
| Permission Node | Penerima Default | Deskripsi |
|---|---|---|
| `apexsions.bypass.movement` | Admin / OP | Mengecualikan staf dari pemeriksaan Fly Hack, Speed, dan Jesus (misal saat moderasi noclip). |
| `apexsions.bypass.combat` | Admin / OP | Mengecualikan staf dari batas Reach dan KillAura Angle. |
| `apexsions.bypass.scaffold` | Admin / OP | Mengecualikan dari batas kecepatan penempatan blok. |
| `apexsions.bypass.cheststealer` | Admin / OP | Mengecualikan dari batas kecepatan klik kontainer. |

