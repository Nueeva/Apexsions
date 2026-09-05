<?php

/**
 * Apexsions Website — Master Minecraft Systems Seeder
 * Synchronizes Wiki categories, Wiki articles, Shop categories, and Shop packages
 * with the authoritative configuration of the Apexsions Minecraft server suite.
 */

require __DIR__ . '/../vendor/autoload.php';
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Illuminate\Support\Facades\DB;
use Illuminate\Support\Carbon;

echo "==========================================================\n";
echo "   APEXSIONS — SYNCHRONIZING WEBSITE WITH MINECRAFT SERVER \n";
echo "==========================================================\n\n";

$now = Carbon::now();

// --------------------------------------------------------------------------
// 1. SHOP CATEGORIES & PACKAGES
// --------------------------------------------------------------------------
echo "[1/4] Synchronizing Shop Categories...\n";

DB::statement('PRAGMA foreign_keys = OFF;');

DB::table('shop_packages')->truncate();
DB::table('shop_categories')->truncate();

$shopCategories = [
    [
        'id' => 1,
        'name' => 'Rank Kasta Donatur',
        'slug' => 'rank-donatur',
        'icon' => 'bi bi-crown',
        'description' => 'Tingkatkan kasta dan kejayaan peradabanmu di realm Apexsions dengan benefit eksklusif, kit berkala, dan kasta permanen.',
        'position' => 1,
        'parent_id' => null,
        'cumulate_purchases' => false,
        'is_enabled' => true,
        'single_purchase' => false,
        'cumulate_strict' => false,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'id' => 2,
        'name' => 'Battlepass Musiman',
        'slug' => 'battlepass',
        'icon' => 'bi bi-trophy',
        'description' => 'Buka akses penuh ke 100 level jalur hadiah musiman, quests mingguan berlimpah, dan kosmetik langka.',
        'position' => 2,
        'parent_id' => null,
        'cumulate_purchases' => false,
        'is_enabled' => true,
        'single_purchase' => false,
        'cumulate_strict' => false,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'id' => 3,
        'name' => 'Pundi Koin & Booster',
        'slug' => 'coins-booster',
        'icon' => 'bi bi-gem',
        'description' => 'Mata uang server tambahan dan pengganda pengalaman (XP Booster & Economy Booster) untuk mempercepat dominasi peradabanmu.',
        'position' => 3,
        'parent_id' => null,
        'cumulate_purchases' => false,
        'is_enabled' => true,
        'single_purchase' => false,
        'cumulate_strict' => false,
        'created_at' => $now,
        'updated_at' => $now,
    ],
];

foreach ($shopCategories as $cat) {
    DB::table('shop_categories')->insert($cat);
    echo "  -> Category added: {$cat['name']}\n";
}

echo "\n[2/4] Synchronizing Shop Packages...\n";

$shopPackages = [
    // --- RANK KASTA DONATUR ---
    [
        'category_id' => 1,
        'name' => 'Ascendant Rank',
        'short_description' => 'Kasta donatur perintis dengan hak akses kit dasar dan prioritas antrean.',
        'description' => "### Hak Istimewa Kasta Ascendant\n" .
            "- **Prefix Chat & Tab:** `[☘ ASCENDANT]` dengan warna hijau zamrud berkilau.\n" .
            "- **Akses Kit Khusus:** Akses ke Kit Ascendant harian (`/kit ascendant`).\n" .
            "- **Ekspansi Wilayah:** +2 Batas klaim wilayah kerajaan (`/k claim`).\n" .
            "- **Prioritas Masuk:** Bypass antrean saat server penuh.\n" .
            "- **Perintah Utilitas:** `/hat`, `/near`, `/craft`, `/workbench`.\n" .
            "- **Durasi:** Permanen seumur hidup.",
        'position' => 1,
        'image' => null,
        'price' => 35000,
        'commands' => json_encode(['lp user {player} parent add ascendant']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Archon Rank',
        'short_description' => 'Kasta ksatria agung dengan hak kit tempur dan kosmetik bercahaya.',
        'description' => "### Hak Istimewa Kasta Archon\n" .
            "- **Prefix Chat & Tab:** `[💎 ARCHON]` dengan gradien cyan kristal.\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Ascendant.\n" .
            "- **Akses Kit Khusus:** Akses ke Kit Archon mingguan (`/kit archon`).\n" .
            "- **Ekspansi Wilayah:** +4 Batas klaim wilayah kerajaan.\n" .
            "- **Perintah Utilitas:** `/anvil`, `/smithing`, `/enderchest` (`/ec`).\n" .
            "- **Kosmetik Bercahaya:** Akses glow warna biru kristal (`/glow`).\n" .
            "- **Diskon Barter:** Diskon 5% biaya administrasi pasar lelang.",
        'position' => 2,
        'image' => null,
        'price' => 75000,
        'commands' => json_encode(['lp user {player} parent add archon']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Sovereign Rank',
        'short_description' => 'Kasta penguasa tanah dengan sayap partikel dan hak wilayah luas.',
        'description' => "### Hak Istimewa Kasta Sovereign\n" .
            "- **Prefix Chat & Tab:** `[⚜ SOVEREIGN]` dengan gradien emas kemilau.\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Archon & Ascendant.\n" .
            "- **Akses Kit Khusus:** Akses ke Kit Sovereign 14 harian (Gear ber-set bonus).\n" .
            "- **Ekspansi Wilayah:** +7 Batas klaim wilayah kerajaan.\n" .
            "- **Kosmetik Sayap:** Efek partikel sayap emas eksklusif (`/cosmetics`).\n" .
            "- **Perintah Utilitas:** `/feed`, `/condense`, `/disposal`.\n" .
            "- **Bebas Tarif Dagang:** Bebas biaya transportasi perdagangan lintas kerajaan (Hemat Rp 5.000 per trade).",
        'position' => 3,
        'image' => null,
        'price' => 150000,
        'commands' => json_encode(['lp user {player} parent add sovereign']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Emperor Rank',
        'short_description' => 'Kasta kaisar perang agung dengan hak terbang di ibukota dan kit legendaris.',
        'description' => "### Hak Istimewa Kasta Emperor\n" .
            "- **Prefix Chat & Tab:** `[⚔ EMPEROR]` dengan gradien merah rubi membara.\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Sovereign, Archon & Ascendant.\n" .
            "- **Akses Kit Khusus:** Akses ke Kit Emperor bulanan (Armor Set Bonus Attack & Critical Damage).\n" .
            "- **Ekspansi Wilayah:** +10 Batas klaim wilayah kerajaan.\n" .
            "- **Hak Terbang:** Hak terbang (`/fly`) di seluruh wilayah klaim pribadi dan ibukota kerajaan.\n" .
            "- **Perintah Utilitas:** `/repair all`, `/extinguish`, `/ptime` (Personal Time).\n" .
            "- **Antrean:** Prioritas puncak di seluruh server.",
        'position' => 4,
        'image' => null,
        'price' => 275000,
        'commands' => json_encode(['lp user {player} parent add emperor']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Sions Rank',
        'short_description' => 'Kasta tertinggi dan paling prestisius di seluruh jagat peradaban Apexsions.',
        'description' => "### Hak Istimewa Kasta Sions (Puncak Donatur)\n" .
            "- **Prefix Chat & Tab:** `[✦ SIONS ✦]` dengan gradien Cyan-Emas Ultra Elegan.\n" .
            "- **Mencakup:** SELURUH hak istimewa semua kasta di bawahnya.\n" .
            "- **Akses Kit Tertinggi:** Akses ke seluruh Kit Donatur + Kit Sions Eksklusif (Ultimate Armor Set).\n" .
            "- **Ekspansi Wilayah:** +15 Batas klaim wilayah kerajaan.\n" .
            "- **Join Broadcast:** Pesan megah broadcast ke seluruh server setiap kali Anda masuk.\n" .
            "- **Aura Mahkota:** Efek mahkota bercahaya dan jejak partikel legenda.\n" .
            "- **Lounge Donatur:** Akses ke ruang VIP eksklusif di Discord & in-game.\n" .
            "- **Bonus Awal:** Bonus 1.000 Apex Coins & 1x Golden Crate Key instan.",
        'position' => 5,
        'image' => null,
        'price' => 500000,
        'commands' => json_encode(['lp user {player} parent add sions', 'abp exp give {player} 1000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // --- BATTLEPASS MUSIMAN ---
    [
        'category_id' => 2,
        'name' => 'Premium Pass Musiman',
        'short_description' => 'Buka 100 level jalur hadiah Premium dan quests mingguan eksklusif.',
        'description' => "### Benefit Premium Pass\n" .
            "- **Akses 100 Level Premium:** Membuka seluruh tier hadiah jalur emas (Gold Track) level 1 s/d 100.\n" .
            "- **Quests Eksklusif:** Akses ke Quests Harian & Mingguan berhadiah koin serta material langka.\n" .
            "- **EXP Boost:** Pengganda perolehan EXP Pass sebesar +25% dari setiap aktivitas.\n" .
            "- **Akses EXP Shop:** Hak berbelanja di Rotating EXP Shop dengan penawaran diskon khusus (`/abp`).",
        'position' => 1,
        'image' => null,
        'price' => 45000,
        'commands' => json_encode(['abp pass grant {player} premium']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 2,
        'name' => 'VIP Pass Musiman',
        'short_description' => 'Jalur tertinggi dengan instan +20 level pass skip dan kosmetik eksklusif.',
        'description' => "### Benefit VIP Pass (Tingkat Tertinggi)\n" .
            "- **Mencakup:** Seluruh keuntungan Premium Pass.\n" .
            "- **Instan Level Skip:** Langsung melompat 20 Level awal Battlepass secara instan.\n" .
            "- **Kosmetik Musiman:** Jubah Sayap Musiman Eksklusif & Gelar Khusus Chat.\n" .
            "- **Bonus Tunai:** Tambahan 50.000 Rupiah server & 5x Magic Dust langsung ke inventory.",
        'position' => 2,
        'image' => null,
        'price' => 85000,
        'commands' => json_encode(['abp pass grant {player} vip', 'abp level add {player} 20', 'eco give {player} rupiah 50000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // --- PUNDI KOIN & BOOSTER ---
    [
        'category_id' => 3,
        'name' => 'Pundi 500 Apex Coins',
        'short_description' => '500 Koin peradaban untuk berbelanja kosmetik dan crate keys.',
        'description' => "Mendapatkan 500 Koin Apex Coins yang dapat digunakan di toko kosmetik `/cosmetics` dan pembelian item crate.",
        'position' => 1,
        'image' => null,
        'price' => 25000,
        'commands' => json_encode(['abp exp give {player} 500']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => 'Pundi 1.200 Apex Coins',
        'short_description' => '1.200 Koin peradaban dengan bonus ekstra 200 koin.',
        'description' => "Mendapatkan 1.200 Koin Apex Coins (Termasuk bonus 200 koin) untuk berbelanja kebutuhan kosmetik dan title eksklusif.",
        'position' => 2,
        'image' => null,
        'price' => 50000,
        'commands' => json_encode(['abp exp give {player} 1200']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => 'Pundi 2.500 Apex Coins',
        'short_description' => 'Paket sultan 2.500 Koin peradaban dengan bonus ekstra 500 koin.',
        'description' => "Mendapatkan 2.500 Koin Apex Coins (Termasuk bonus 500 koin) untuk membuka berbagai kosmetik legendaris dan seasonal pass.",
        'position' => 3,
        'image' => null,
        'price' => 100000,
        'commands' => json_encode(['abp exp give {player} 2500']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => 'XP Booster 2x (3 Hari)',
        'short_description' => 'Pengganda perolehan XP 2x lipat selama 72 jam penuh.',
        'description' => "Menggandakan seluruh perolehan XP (Leveling dan Battlepass) sebesar 200% selama 72 jam berturut-turut di seluruh server.",
        'position' => 4,
        'image' => null,
        'price' => 15000,
        'commands' => json_encode(['apexbooster give {player} xp 2 72h']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => 'Economy Booster 1.5x (3 Hari)',
        'short_description' => 'Tingkatkan penghasilan Rupiah penjualan barang sebesar +50% selama 3 hari.',
        'description' => "Meningkatkan perolehan Rupiah dari hasil penjualan barang di `/sell` dan reward aktivitas sebesar +50% selama 72 jam.",
        'position' => 5,
        'image' => null,
        'price' => 20000,
        'commands' => json_encode(['apexbooster give {player} eco 1.5 72h']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
];

foreach ($shopPackages as $pkg) {
    DB::table('shop_packages')->insert($pkg);
    echo "  -> Package added: {$pkg['name']} (Rp " . number_format($pkg['price'], 0, ',', '.') . ")\n";
}

// --------------------------------------------------------------------------
// 2. WIKI CATEGORIES & PAGES
// --------------------------------------------------------------------------
echo "\n[3/4] Synchronizing Wiki Categories...\n";

DB::table('wiki_pages')->truncate();
DB::table('wiki_categories')->truncate();

$wikiCategories = [
    [
        'id' => 1,
        'icon' => 'bi bi-compass',
        'name' => 'Panduan Pemula & Perintah',
        'slug' => 'panduan-pemula',
        'position' => 1,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'id' => 2,
        'icon' => 'bi bi-shield-shaded',
        'name' => 'Tiga Kerajaan & Kedaulatan',
        'slug' => 'tiga-kerajaan',
        'position' => 2,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'id' => 3,
        'icon' => 'bi bi-coin',
        'name' => 'Ekonomi & Perdagangan',
        'slug' => 'ekonomi-perdagangan',
        'position' => 3,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'id' => 4,
        'icon' => 'bi bi-magic',
        'name' => 'Custom Enchants & Kits',
        'slug' => 'enchants-dan-kits',
        'position' => 4,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'id' => 5,
        'icon' => 'bi bi-trophy',
        'name' => 'Battlepass & Komunikasi',
        'slug' => 'battlepass-komunikasi',
        'position' => 5,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'id' => 6,
        'icon' => 'bi bi-crown',
        'name' => 'Hierarki Kasta Resmi',
        'slug' => 'hierarki-kasta',
        'position' => 6,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
];

foreach ($wikiCategories as $cat) {
    DB::table('wiki_categories')->insert($cat);
    echo "  -> Category added: {$cat['name']}\n";
}

echo "\n[4/4] Synchronizing Wiki Detailed Articles...\n";

$wikiPages = [
    // --- KATEGORI 1: PANDUAN PEMULA & PERINTAH ---
    [
        'category_id' => 1,
        'position' => 1,
        'title' => 'Cara Bergabung ke Server Apexsions',
        'slug' => 'cara-bergabung',
        'content' => <<<MARKDOWN
# Cara Bergabung ke Server Apexsions

Apexsions adalah server peradaban Minecraft modular berarsitektur tinggi yang mendukung pemain **Java Edition** dan **Bedrock Edition** secara bersamaan.

---

### Informasi Alamat Server (Koneksi)

| Platform | Alamat IP / Host | Port | Versi Minecraft |
| :--- | :--- | :--- | :--- |
| **Java Edition** | `apexsions.my.id` | `25565` (Default) | **1.21.4** |
| **Bedrock Edition** (HP/Win10/Console) | `apexsions.my.id` | **`19132`** | Versi Terbaru (Bedrock) |

---

### Langkah Mudah Memulai Bermain

1. **Buka Minecraft Client:** Pastikan Anda menggunakan versi Minecraft **1.21.4**.
2. **Pilih Menu Multiplayer:** Klik tombol **Add Server** (Tambah Server).
3. **Masukkan Data Server:**
   - **Server Name:** Apexsions
   - **Server Address:** `apexsions.my.id`
   - *(Untuk Bedrock, masukkan Port `19132`)*
4. **Masuk ke Dunia:** Klik **Join Server**. Anda akan mendarat di pusat peradaban agung Apexsions.
5. **Pilih Kerajaan Pertama Anda:** Ketik perintah `/kingdom` atau `/k` untuk memilih salah satu dari 3 Kerajaan berdaulat: **Zenithar**, **Solterra**, atau **Sylvamoor**.

---

### Integrasi Akun Web & Server

Untuk keamanan transaksi dan mengklaim hadiah starter pack:
- Daftarkan akun Anda di portal web resmi: [apexsions.my.id](https://apexsions.my.id).
- Gunakan perintah `/link` di dalam server jika diminta untuk menyinkronkan status keamanan profil Anda.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'position' => 2,
        'title' => 'Daftar Perintah Resmi Server (Commands Guide)',
        'slug' => 'daftar-perintah-resmi',
        'content' => <<<MARKDOWN
# Daftar Perintah Resmi Server (Commands Guide)

Berikut adalah daftar perintah resmi yang terdaftar aktif di ekosistem plugin Apexsions.

---

### 1. Navigasi & Eksplorasi Dasar
- `/spawn` — Teleportasi kembali ke titik pusat peradaban utama.
- `/rtp` — Teleportasi acak ke alam liar yang aman untuk mendirikan pemukiman baru.
- `/sethome <nama>` — Menandai koordinat rumah atau markas pribadi Anda.
- `/home <nama>` — Teleportasi kembali ke titik rumah yang telah ditandai.
- `/tpa <player>` — Mengirimkan permintaan teleportasi ramah ke pemain lain.
- `/tpaccept` — Menerima permintaan teleportasi yang masuk.

---

### 2. Kerajaan & Kedaulatan Wilayah (`ApexsionsCore`)
- `/kingdom` atau `/k` — Membuka GUI pemilihan dan informasi 3 Kerajaan Berdaulat.
- `/k info [nama]` — Melihat status ibukota, buff aktif, dan raja kerajaan.
- `/k claim` — Mengklaim chunk wilayah atas nama kerajaan Anda.
- `/k map` — Melihat radar wilayah dan perbatasan kerajaan di sekeliling Anda.
- `/k deposit <jumlah>` — Menyetorkan Rupiah ke kas perbendaharaan kerajaan.

---

### 3. Ekonomi & Perdagangan (`ApexsionsEconomy` & `ApexsionsShop`)
- `/money` atau `/balance` atau `/bal` — Memeriksa saldo dompet atomic Anda (Rupiah & Diamond).
- `/pay <player> <jumlah>` — Mentransfer Rupiah secara instan dan aman ke pemain lain.
- `/trade <player>` — Membuka antarmuka barter dua arah yang dilindungi sistem escrow.
- `/ah` — Membuka Pasar Lelang (Auction House) 24 jam.
- `/ah sell <harga>` — Mendaftarkan item yang sedang dipegang ke pasar lelang.
- `/shop` — Membuka Toko Pasar Dinamis (Dynamic Market).
- `/sell` — Menjual hasil tambang, panen, atau mob drop ke pasar dinamis.

---

### 4. Perlengkapan, Kit & Sihir (`ApexsionsCore` & `ApexsionsCustomEnchants`)
- `/kits` atau `/kit` — Membuka GUI daftar kit perlengkapan berkala Anda.
- `/kit preview <nama>` — Melihat pratinjau isi perlengkapan dan set bonus armor.
- `/enchanter` atau `/ce` — Membuka altar 182 Custom Enchantments.

---

### 5. Battlepass & Komunikasi (`ApexsionsBattlepass` & `ApexsionsChat`)
- `/abp` — Membuka antarmuka utama progres Battlepass musiman.
- `/abp quests` — Memeriksa daftar misi harian dan mingguan Anda.
- `/ch g` — Beralih ke Kanal Chat Global.
- `/ch k` — Beralih ke Kanal Chat Kerajaan (hanya terbaca sesama warga).
- `/mail send <player> <pesan>` — Mengirimkan surat offline ke pemain lain.
- `/report <player> <alasan>` — Melaporkan indikasi kecurangan ke meja piket staf.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'position' => 3,
        'title' => 'Sistem Progresi Level 1–100 & Gelar Peradaban',
        'slug' => 'progresi-level-dan-gelar',
        'content' => <<<MARKDOWN
# Sistem Progresi Level 1–100 & Gelar Peradaban

Sistem progresi level di Apexsions (`ApexsionsCore`) dirancang untuk menghargai setiap dedikasi pemain dalam membangun peradaban. Level pemain berjalan dari **Level 1 hingga Level 100**, dilengkapi dengan sistem gelar dinamis yang terikat pada kerajaan yang Anda pilih.

---

### Sumber Perolehan Experience (XP)

1. **Pertambangan (Mining):** Menambang emas murni, diamond, netherite, dan ancient debris memberikan limpahan XP peradaban.
2. **Pertempuran (Slaying):** Mengalahkan monster malam, raid pillager, serta boss monster.
3. **Agraris (Farming):** Memanen gandum, wortel, tebu, dan nether wart dalam skala kerajaan.
4. **Pembangunan & Kedaulatan:** Menyetorkan sumber daya ke nexus kerajaan dan berpartisipasi dalam pertempuran wilayah.

---

### Gelar Resmi Berdasarkan Kerajaan (Tiap 10 Level)

Setiap mencapai milestone level baru, gelar kehormatan pada chat prefix Anda akan berubah secara otomatis:

| Level Tier | Gelar Zenithar (Solar) | Gelar Solterra (Crimson) | Gelar Sylvamoor (Azure) |
| :--- | :--- | :--- | :--- |
| **Lv. 1–10** | Acolyte of Zenith | Dune Wanderer | Sylvan Citizen |
| **Lv. 11–20** | Celestial Scout | Sun Scout | Grove Keeper |
| **Lv. 21–30** | Sky Warden | Terra Blade | Forest Warden |
| **Lv. 31–40** | Astral Knight | Solar Knight | Wild Knight |
| **Lv. 41–50** | Apex Templar | Flame Vanguard | Nature Commander |
| **Lv. 51–60** | Star Commander | Dune Warlord | Druidic Lord |
| **Lv. 61–70** | Solaris Archon | Solaris Champion | Verdant Archon |
| **Lv. 71–80** | ⚡ High Celestial ⚡ | 🔥 Sun Sovereign 🔥 | 🌿 Elder Guardian 🌿 |
| **Lv. 81–90** | 👑 Zenith Paragon 👑 | ⚔ Solterra Overlord ⚔ | ⚜ Sylvan Sovereign ⚜ |
| **Lv. 91–100** | ✦ EMPEROR OF ZENITHAR ✦ | ✦ LORD OF SOLTERRA ✦ | ✦ AVATAR OF SYLVAMOOR ✦ |

---

### Hadiah Milestone Level

Setiap kelipatan 10 level, Anda dapat mengklaim hadiah eksklusif melalui GUI `/rewards` yang berisi:
- Koin Rupiah Server dan Diamond murni.
- Buku Custom Enchantment tingkat tinggi.
- Crate Keys langka & Voucher diskon pasar lelang.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // --- KATEGORI 2: TIGA KERAJAAN & KEDAULATAN ---
    [
        'category_id' => 2,
        'position' => 1,
        'title' => 'Ensiklopedia 3 Kerajaan Berdaulat',
        'slug' => 'tiga-kerajaan-berdaulat',
        'content' => <<<MARKDOWN
# Ensiklopedia Tiga Kerajaan Berdaulat

Di Apexsions, dunia terbagi menjadi 3 kerajaan besar berdaulat yang memiliki sejarah, fisiologi wilayah, dan kekuatan mistis yang unik. Setiap pemain wajib memilih satu kerajaan sebagai tanah tumpah darahnya.

---

### 1. Zenithar (Celestial & Solar Realm)
*Sebuah kerajaan megah bertahtakan cahaya keemasan, istana awan menjulang tinggi, dan kuil arcanum langit.*

- **Ibukota:** Solarium Spire Citadel
- **Koordinat Ibukota:** `world (-3028, 64, -5597)`
- **Bioma Khas:** Sky Plains, High Peaks, Jagged Peaks
- **Gelar Raja Tertinggi:** *Monarch of the Sun*
- **Pajak Kerajaan:** 10%
- **Karakteristik & Buff Khusus:**
  - `+15%` Kecepatan Menambang & Perolehan Experience.
  - `+10%` Kecepatan Gerak saat berada di ketinggian (`Y > 80`).
  - Efek Aura Cahaya Abadi di seluruh wilayah ibukota.
- **Kelemahan (Nerf):** `-10%` Kecepatan berenang di air dalam; rentan terhadap sambaran petir saat badai.

---

### 2. Solterra (Crimson Earth & Fire Empire)
*Kekaisaran perang perkasa bermahkotakan lautan magma, benteng obsidian merah, dan bukit pasir membara.*

- **Ibukota:** Ignis Bastion Fortress
- **Koordinat Ibukota:** `world (-5843, 65, 889)`
- **Bioma Khas:** Desert, Badlands / Mesa, Savanna Plateau
- **Gelar Raja Tertinggi:** *Warlord of the Dunes*
- **Pajak Kerajaan:** 10%
- **Karakteristik & Buff Khusus:**
  - `+15%` Serangan Melee & Bonus Kerusakan Api (Fire Damage).
  - Kebal mutlak terhadap efek terbakar di seluruh wilayah kedaulatan Solterra.
  - `+10%` Ketahanan terhadap knockback (Knockback Resistance).
- **Kelemahan (Nerf):** `-10%` Ketahanan terhadap Fall Damage; efisiensi bercocok tanam tanaman dingin berkurang.

---

### 3. Sylvamoor (Azure Crystal & Ocean Realm)
*Kerajaan kuno penuh kedamaian di tengah hutan lebat, pohon dunia mistis, dan samudra kristal biru berkilau.*

- **Ibukota:** Eldergrove Sanctuary
- **Koordinat Ibukota:** `world (-9666, 64, -4812)`
- **Bioma Khas:** Old Growth Taiga, Jungle, Warm Ocean
- **Gelar Raja Tertinggi:** *Protector of the World Tree*
- **Pajak Kerajaan:** 10%
- **Karakteristik & Buff Khusus:**
  - `+20%` Efisiensi Hasil Panen dan Regenerasi Nyawa Alami.
  - Kecepatan berenang ekstra dan pernapasan air tanpa batas di samudra Sylvamoor.
  - Daya tahan perisai dan armor meningkat saat berada di dalam bioma hutan.
- **Kelemahan (Nerf):** `-15%` Kerentanan terhadap serangan api di luar wilayah kerajaan.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 2,
        'position' => 2,
        'title' => 'Klaim Wilayah Kerajaan & Proteksi Nexus',
        'slug' => 'klaim-wilayah-dan-nexus',
        'content' => <<<MARKDOWN
# Klaim Wilayah Kerajaan & Proteksi Nexus

Setiap inci tanah di Apexsions dilindungi oleh sistem kedaulatan tanah (`ApexsionsCore`). Pemain dapat memperluas wilayah pemukiman mereka dan melindunginya dari kehancuran maupun penjarahan.

---

### Cara Mengklaim Wilayah Baru

1. Berdirilah di chunk (area 16x16 blok) yang belum memiliki pemilik.
2. Gunakan perintah `/k claim`.
3. Pastikan kas pribadi Anda memiliki saldo Rupiah yang mencukupi untuk biaya pemeliharaan awal.
4. Periksa batas wilayah di sekeliling Anda dengan perintah `/k map`.

---

### Sistem Proteksi & Otoritas Chunk

Setelah chunk diklaim atas nama kerajaan Anda:
- Pemain dari kerajaan lain **tidak dapat** menghancurkan blok, menaruh blok, membuka peti (chest), ataupun menggunakan pintu dan tombol.
- Seluruh ledakan Creeper dan TNT di alam liar tidak akan merusak bangunan di dalam chunk yang terproteksi.
- Hewan ternak dan tanaman Anda aman dari pencurian.

---

### Kas Nexus & Pajak Wilayah

Setiap kerajaan memiliki **Nexus Inti**. Jika kas perbendaharaan kerajaan habis akibat tidak ada warga yang membayar pajak atau menyetor donasi melalui `/k deposit`, proteksi chunk dapat melemah dan masuk ke dalam status *Vulnerable* (Rentan Penaklukan).
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 2,
        'position' => 3,
        'title' => 'Perang Kerajaan (Kingdom War), Siege & Combat Tag',
        'slug' => 'perang-kerajaan-dan-combat-tag',
        'content' => <<<MARKDOWN
# Perang Kerajaan, Siege & Combat Tag

Untuk menjaga keadilan dan mencegah kecurangan saat pertempuran PvP antar-peradaban, server memberlakukan sistem **Kingdom War** dan **Combat Tag** yang ketat.

---

### Sistem Combat Tag (Anti-Combat Log)

Setiap kali Anda memberikan serangan atau menerima serangan dari pemain lain:
- Status **COMBAT TAG AKTIF** selama **15 Detik**.
- Anda **DILARANG** melakukan perintah teleportasi (`/spawn`, `/home`, `/tpa`).
- Anda **DILARANG** membuka peti lelang (`/ah`) atau toko (`/shop`).
- **Hukuman Disconnect:** Jika Anda sengaja keluar dari permainan (Force Close / Alt+F4 / Disconnect) saat Combat Tag masih aktif, karakter Anda akan **mati seketika**, seluruh inventory Anda terjatuh di tanah, dan musuh dinyatakan sebagai pemenang duel!

---

### Siege & Penaklukan Wilayah (Kingdom War)

1. **Jadwal Siege Terjadwal:** Perang perebutan wilayah hanya dapat dideklarasikan pada jadwal server resmi demi memastikan kedua pihak memiliki pasukan pembela.
2. **Penyerbuan Nexus:** Pasukan penyerang harus menghancurkan Crystal Shield di titik klaim musuh sebelum dapat mengklaim hak atas tanah tersebut.
3. **Pampasan Perang:** Kerajaan pemenang akan memperoleh 25% dari saldo kas nexus kerajaan yang kalah.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // --- KATEGORI 3: EKONOMI & PERDAGANGAN ---
    [
        'category_id' => 3,
        'position' => 1,
        'title' => 'Sistem Mata Uang: Rupiah (Rp) & Diamond (💎)',
        'slug' => 'sistem-mata-uang',
        'content' => <<<MARKDOWN
# Sistem Mata Uang: Rupiah (Rp) & Diamond (💎)

Ekosistem ekonomi di Apexsions (`ApexsionsEconomy`) menggunakan arsitektur **Dual-Currency** dengan proteksi transaksi ACID (Atomic, Consistent, Isolated, Durable). Hal ini menjamin tidak akan pernah terjadi duplikasi uang, kehilangan saldo, maupun nilai negatif.

---

### Dua Mata Uang Resmi Server

| Mata Uang | Simbol | Sifat Mata Uang | Cara Memperoleh | Penggunaan Utama |
| :--- | :---: | :--- | :--- | :--- |
| **Rupiah** | `Rp` | Uang sirkulasi fiat harian | Menjual hasil tambang/panen di `/shop`, reward misi, gaji kerajaan | Pembelian item pasar dinamis, klaim wilayah, pajak nexus, barter |
| **Diamond** | `💎` | Komoditas logam mulia bernilai tinggi | Menambang diamond ore di kedalaman Y < 0, reward event khusus | Membeli Custom Enchants kasta atas (Legendary & Fabled), item langka |

---

### Saldo Awal Pemain Baru
Setiap warga perintis yang baru pertama kali bergabung secara otomatis menerima:
- **Rp 10.000 (Rupiah)** sebagai modal awal membeli perkakas dan bibit pertanian.
- Saldo dapat dicek kapan saja dengan perintah `/money` atau `/balance`.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'position' => 2,
        'title' => 'Pasar Dinamis (Dynamic Market /shop & /sell)',
        'slug' => 'pasar-dinamis',
        'content' => <<<MARKDOWN
# Pasar Dinamis (Dynamic Market /shop & /sell)

Toko server di Apexsions (`ApexsionsShop`) digerakkan oleh algoritma **Supply and Demand** otomatis. Harga komoditas tidak bersifat statis, melainkan bergerak dinamis sesuai volume transaksi pemain.

---

### Kategori Komoditas Pasar Dinamis
1. **Blocks (Bahan Bangunan):** Batu, deepslate, kayu, kaca, terracotta.
2. **Farming (Hasil Pertanian):** Gandum, kentang, wortel, melon, labu, tebu.
3. **Mob Drops (Hasil Berburu):** Rotten flesh, tulang, benang, gunpowder, ender pearl.
4. **Ores (Hasil Tambang):** Batubara, besi, tembaga, emas murni, redstone, lapis lazuli.
5. **Dyes (Pewarna):** Aneka ragam pewarna alami untuk kerajinan.
6. **Food (Bahan Pangan):** Roti, daging matang, golden carrot.

---

### Mekanisme Fluktuasi Harga
- Jika banyak pemain menjual satu komoditas secara massal (contoh: ribuan kentang), harga jual komoditas tersebut akan mengalami depresiasi (turun perlahan).
- Jika pasar kekurangan stok dan banyak pemain membelinya, harga akan mengalami apresiasi (naik secara wajar).
- Manfaatkan fluktuasi ini untuk menjadi pedagang peradaban yang sukses!
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'position' => 3,
        'title' => 'Pasar Lelang (/ah) & Barter Escrow (/trade)',
        'slug' => 'pasar-lelang-dan-barter',
        'content' => <<<MARKDOWN
# Pasar Lelang (/ah) & Barter Escrow (/trade)

Perdagangan antar-pemain di Apexsions difasilitasi oleh dua sistem mandiri yang aman dan terlindungi dari segala bentuk penipuan (*scam*).

---

### 1. Pasar Lelang (Auction House `/ah`)
- Buka antarmuka lelang dengan perintah `/ah`.
- Untuk menjual item yang sedang Anda pegang: ketik `/ah sell <harga>`.
- Item akan tayang di etalase pasar selama **24 Jam**.
- Server mengenakan biaya administrasi pendaftaran lelang sebesar **2%** yang dipotong otomatis saat barang laku terjual.
- Setiap pemain memiliki kuota maksimal hingga **10 slot penjualan aktif** secara bersamaan.

---

### 2. Barter Window Escrow (`/trade <player>`)
- Kirimkan ajakan barter ke pemain yang berada dalam jarak pandang: `/trade <nama_pemain>`.
- Antarmuka GUI dua sisi akan terbuka. Masukkan item atau ketik nominal Rupiah yang ingin ditukarkan.
- Kedua belah pihak wajib mengklik tombol **KUNCI TAWARAN**, dilanjutkan dengan tombol **SETUJU FINAL**.
- Sistem escrow menjamin item dan uang bertukar secara instan pada detik yang sama tanpa ada risiko barang dicuri.

---

### Kebijakan Tarif Transportasi Lintas Kerajaan
- **Sesama Warga Kerajaan (Same-Kingdom):** Bebas biaya administrasi perdagangan (**GRATIS / Rp 0**).
- **Perdagangan Lintas Kerajaan Berbeda (Cross-Kingdom):** Dikenakan tarif transportasi resmi sebesar **Rp 5.000** kepada kedua belah pihak sebagai kompensasi bea cukai perbatasan peradaban.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // --- KATEGORI 4: CUSTOM ENCHANTS & KITS ---
    [
        'category_id' => 4,
        'position' => 1,
        'title' => '182 Custom Enchantments & 7 Tingkatan Tier',
        'slug' => 'custom-enchantments',
        'content' => <<<MARKDOWN
# 182 Custom Enchantments & 7 Tingkatan Tier

`ApexsionsCustomEnchants` menghadirkan sistem sihir persenjataan tercanggih dengan **182 Enchantments kustom** yang terbagi ke dalam **7 Tingkatan Kasta Sihir**.

---

### Daftar 7 Tingkatan Tier Sihir & Biaya

| Tier Sihir | Ikon Representasi | Mata Uang | Biaya Per Buku | Status |
| :--- | :---: | :---: | :---: | :---: |
| **SIMPLE** | Coal (Batubara) | Rupiah | **Rp 15.000** | Aktif |
| **UNIQUE** | Copper Ingot | Rupiah | **Rp 35.000** | Aktif |
| **ELITE** | Iron Ingot | Rupiah | **Rp 75.000** | Aktif |
| **ULTIMATE** | Gold Ingot | Rupiah | **Rp 150.000** | Aktif |
| **LEGENDARY** | Emerald | Diamond | **25 💎** | Aktif |
| **FABLED** | Netherite Ingot | Diamond | **50 💎** | Aktif |
| **HEROIC (God Tier)** | Nether Star | Diamond | **100 💎** | Segera Hadir |

---

### Cara Mendapatkan & Menerapkan Buku Sihir
1. Buka antarmuka altar sihir dengan perintah `/enchanter` atau `/ce`.
2. Pilih tier buku yang Anda inginkan sesuai kesiapan kas Rupiah atau Diamond Anda.
3. Anda akan menerima sebuah **Enchantment Book** dengan dua persentase krusial:
   - **Success Rate (%):** Peluang sihir berhasil menyatu dengan perkakas Anda.
   - **Destroy Rate (%):** Risiko perkakas hancur berkeping-keping jika proses penempaan gagal!
4. Untuk menerapkan sihir: seret dan jatuhkan (*drag and drop*) buku tersebut ke atas senjata atau armor Anda di dalam inventory.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 4,
        'position' => 2,
        'title' => 'Gulungan Sihir (Scrolls), Magic Dust & Scrambler',
        'slug' => 'scrolls-dan-magic-dust',
        'content' => <<<MARKDOWN
# Gulungan Sihir (Scrolls), Magic Dust & Scrambler

Untuk meminimalkan risiko kehancuran perlengkapan berharga saat penempaan sihir, para alkemis Apexsions menciptakan aneka gulungan pelindung dan serbuk sihir:

---

### 1. Magic Dust (Serbuk Sihir)
- **Fungsi:** Meningkatkan **Success Rate** buku sihir Anda.
- **Cara Pakai:** Seret serbuk Magic Dust dan jatuhkan ke atas buku sihir yang ingin Anda tingkatkan peluang keberhasilannya sebelum ditempa.

---

### 2. White Scroll (Gulungan Suci Pelindung)
- **Fungsi:** Mencegah kehancuran item!
- **Mekanisme:** Jika Anda menempelkan White Scroll pada senjata atau armor, item tersebut akan mendapatkan tanda `[PROTECTED]`. Jika proses enchant di kemudian hari gagal, item Anda **tidak akan hancur**, melainkan hanya White Scroll-nya saja yang terpakai.

---

### 3. Black Scroll (Gulungan Pencabut Sihir)
- **Fungsi:** Mencabut satu enchant acak dari perlengkapan Anda dan mengembalikannya ke dalam bentuk buku sihir dengan tingkat keberhasilan 100%.

---

### 4. Transmog Scroll (Gulungan Penata Lore)
- **Fungsi:** Mengatur ulang tata letak tulisan enchant pada perkakas Anda menjadi rapi berurutan angka Romawi, serta menambahkan penghitung korban (*Kill Tracker*) yang prestisius.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 4,
        'position' => 3,
        'title' => 'Sistem Native Kits & Bonus Set Armor Berbasis Stat',
        'slug' => 'kits-dan-set-bonuses',
        'content' => <<<MARKDOWN
# Sistem Native Kits & Bonus Set Armor Berbasis Stat

Di samping sihir individual, sistem perlengkapan Apexsions (`ApexsionsCore`) dilengkapi dengan **Native Kits Engine** dan **Stat-based Armor Set Bonuses**.

---

### Perintah Akses Perlengkapan
- `/kits` atau `/kit` — Membuka antarmuka visual daftar kit Anda.
- `/kit preview <nama>` — Memeriksa isi peti, senjata, dan efek set armor sebelum mengklaim.
- Setiap kit memiliki waktu cooldown berkala (harian, mingguan, hingga bulanan).

---

### 6 Tipe Bonus Set Armor Peradaban

Saat Anda mengenakan 4 potong armor (Helm, Chestplate, Leggings, Boots) dari satu set perlengkapan yang sama, kekuatan pasif set bonus akan aktif secara otomatis:

1. **🛡 DAMAGE REDUCTION (-15% s/d -25%):** Mengurangi persentase seluruh kerusakan yang Anda terima dari serangan fisik dan proyektil musuh.
2. **⚔ ATTACK BOOST (+20% s/d +35%):** Meningkatkan daya hancur serangan senjata jarak dekat Anda secara signifikan.
3. **💨 DODGE CHANCE (+10% s/d +20%):** Peluang alami untuk menghindar sepenuhnya dari serangan musuh tanpa menerima damage sedikitpun.
4. **⚡ CRITICAL STRIKE (+25% s/d +50%):** Melipatgandakan kerusakan saat Anda mendaratkan serangan kritikal.
5. **❤ EXTRA MAX HEALTH (+6 HP s/d +14 HP):** Menambahkan bar nyawa ekstra (3 hingga 7 hati tambahan) secara permanen selama set armor dikenakan.
6. **⚡ SPEED BOOST (+15% s/d +30%):** Meningkatkan kelincahan manuver dan kecepatan gerak berlari Anda di medan pertempuran.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // --- KATEGORI 5: BATTLEPASS & KOMUNIKASI ---
    [
        'category_id' => 5,
        'position' => 1,
        'title' => 'Battlepass Musiman: Quests & EXP Shop',
        'slug' => 'battlepass-musiman',
        'content' => <<<MARKDOWN
# Battlepass Musiman: Quests & EXP Shop

`ApexsionsBattlepass` adalah sistem musim petualangan yang menghadirkan tantangan berhadiah sepanjang 100 tingkatan level reward.

---

### Tiga Tingkatan Pass Musiman
1. **Free Pass:** Terbuka secara otomatis dan gratis untuk seluruh pemain baru (`Wanderer`). Berisi suplai makanan, uang saku Rupiah, dan material tambang standar.
2. **Premium Pass:** Tersedia di Webstore atau dibeli in-game (`/abp buy`). Membuka akses ke jalur emas dengan reward senjata tajam, koin Apex Coins, dan enchant books tingkat tinggi.
3. **VIP Pass:** Jalur donatur tertinggi dengan bonus instan 20 level skip, jubah kosmetik eksklusif musim, serta akses prioritas penawaran toko.

---

### Misi Harian & Mingguan (Quests)
Buka menu misi dengan perintah `/abp quests`:
- **Daily Quests:** Misi ringan harian seperti menebang 100 kayu, menambang 50 besi, atau membunuh 20 zombie.
- **Weekly Quests:** Tantangan komunal mingguan berskala besar seperti mengalahkan Boss Warden, memenangkan perang wilayah, atau menyetor hasil panen ke kas kerajaan.

---

### Toko EXP Berputar (Rotating EXP Shop)
EXP ekstra yang Anda kumpulkan dari misi Battlepass dapat dibelanjakan di menu `/abp shop`. Barang yang ditawarkan berganti secara otomatis setiap pekan, mulai dari serbuk sihir hingga voucher diskon toko peradaban.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 5,
        'position' => 2,
        'title' => 'Kanal Chat, Kingdom Tags & Layanan Pelaporan',
        'slug' => 'kanal-chat-dan-laporan',
        'content' => <<<MARKDOWN
# Kanal Chat, Kingdom Tags & Layanan Pelaporan

Sistem komunikasi di Apexsions (`ApexsionsChat`) menggunakan format komponen Adventure MiniMessage modern yang rapi, informatif, dan bebas spam.

---

### Tiga Saluran Kanal Chat Resmi
- **Global Channel (`/ch g`):** Pesan Anda terbaca oleh seluruh pemain yang sedang online di seluruh dunia server.
- **Kingdom Channel (`/ch k`):** Saluran khusus internal kerajaan. Pesan hanya dapat dibaca oleh rekan satu kerajaan Anda untuk merancang strategi perang atau diplomasi.
- **Staff Channel (`/ch s`):** Kanal tertutup khusus para penjaga ketertiban (Herald, Warden, dan Ancestor).

---

### Format Tampilan Chat Modern
Setiap pesan di chatroom menampilkan identitas lengkap peradaban Anda:
`[G] [Lv. 45 Apex Templar] [⚜ SOVEREIGN] [✦ ZENITHAR] PlayerName ➔ Pesan Anda`

---

### Layanan Surat Pribadi & Laporan Kecurangan
- **Kirim Surat Offline:** `/mail send <player> <pesan>` — Mengirimkan pesan kepada teman yang sedang offline. Surat akan terbaca saat mereka login berikutnya.
- **Laporkan Pelanggaran:** `/report <player> <alasan>` — Mengirimkan laporan instan ke meja piket staf jika menemukan indikasi penggunaan cheat ilegal, griefing di luar aturan war, atau pelanggaran tata tertib server.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // --- KATEGORI 6: HIERARKI KASTA RESMI ---
    [
        'category_id' => 6,
        'position' => 1,
        'title' => 'Struktur 9 Tingkatan Kasta Resmi Apexsions',
        'slug' => 'struktur-sembilan-kasta',
        'content' => <<<MARKDOWN
# Struktur Sembilan Tingkatan Kasta Resmi Apexsions

Sumber kebenaran hierarki (`ranks.yml`) membagi peradaban Apexsions ke dalam **9 Kasta Resmi** yang memiliki bobot (*weight*), wewenang, dan kehormatan masing-masing:

---

| Tingkat Kasta | Bobot (Weight) | Peran & Hak Istimewa Utama | Status Hak |
| :--- | :---: | :--- | :--- |
| **👑 THE ANCESTOR** | **100** | Pemilik, pencipta semesta, dan pelindung peradaban Apexsions. | Pemilik Tunggal (Owner) |
| **🛡 WARDEN** | **90** | Kepala staf administrasi, pengawas keadilan perang, dan penegak hukum server. | Staf Administrator |
| **📜 HERALD** | **80** | Moderator komunitas, pemandu warga baru, dan penengah sengketa wilayah. | Staf Moderator |
| **✦ SIONS ✦** | **70** | Puncak kasta donatur tertinggi dengan seluruh hak istimewa, broadcast kemegahan, dan aura mahkota emas. | Kasta Donatur Sultan (Apex Tier) |
| **⚔ EMPEROR** | **60** | Kasta kaisar perang dengan hak terbang di ibukota, kit bulanan ber-set bonus, dan prioritas antrean. | Kasta Donatur Tier 4 |
| **⚜ SOVEREIGN** | **50** | Kasta penguasa tanah dengan sayap partikel, kit 14 harian, dan bebas tarif dagang lintas kerajaan. | Kasta Donatur Tier 3 |
| **💎 ARCHON** | **40** | Kasta ksatria agung dengan kit mingguan, efek glow kristal, dan akses enderchest portable. | Kasta Donatur Tier 2 |
| **☘ ASCENDANT** | **30** | Kasta perintis dengan kit harian, hak bypass antrean server, dan tambahan klaim wilayah. | Kasta Donatur Tier 1 |
| **WANDERER** | **10** | Warga perintis baru yang baru mendarat di alam peradaban Apexsions. | Kasta Default (Semua Pemain) |

---

### Integritas Kasta & LuckPerms
Seluruh kasta donatur dan staf dikelola secara terpusat dan otomatis melalui sinkronisasi native **LuckPerms**, sehingga setiap pembelian paket di Webstore akan mengaktifkan kasta Anda di server dalam hitungan detik tanpa perlu restart server.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
];

foreach ($wikiPages as $p) {
    DB::table('wiki_pages')->insert($p);
    echo "  -> Article added: {$p['title']} (Cat ID: {$p['category_id']})\n";
}

echo "\n==========================================================\n";
echo "   SYNCHRONIZATION COMPLETED SUCCESSFULLY! \n";
echo "==========================================================\n";
