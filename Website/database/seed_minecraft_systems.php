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

$driver = DB::getDriverName();
if ($driver === 'sqlite') {
    DB::statement('PRAGMA foreign_keys = OFF;');
} else {
    DB::statement('SET FOREIGN_KEY_CHECKS=0;');
}

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
        'name' => '💎 Diamond',
        'slug' => 'diamond',
        'icon' => 'bi bi-gem',
        'description' => 'Mata uang premium resmi peradaban Apexsions untuk transaksi eksklusif, kosmetik langka, dan item bergengsi (Rp 375/Diamond).',
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
    // =========================================================================
    // 1. ASCENDANT
    // =========================================================================
    [
        'category_id' => 1,
        'name' => 'Ascendant (Trial 30 Hari)',
        'short_description' => 'Awal pendakian peradaban. Paket Trial 30 Hari dengan bonus ekonomi dan limit esensial.',
        'description' => "### Hak Istimewa Kasta Ascendant (Trial 30 Hari)\n" .
            "- **Prefix Chat & Tab:** `[☘ ASCENDANT]` dengan warna hijau zamrud berkilau.\n" .
            "- **Maksimal Homes:** 3 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 4 Barang aktif di Auction House (`/ah` / `/lelang`).\n" .
            "- **Batas Custom Enchants:** Maksimal 5 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 2 Menit 15 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +3% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +5% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Masa Aktif:** 30 Hari (Berbatas Waktu).\n" .
            "- *Catatan:* Paket Trial tidak memperoleh Kit Permanen atau Uang Reward Satu Kali.",
        'position' => 1,
        'image' => 'package-ascendant.jpg',
        'price' => 20000,
        'commands' => json_encode(['lp user {player} parent addtemp ascendant 30d', 'lp user {player} permission settemp apexsions.rank.trial true 30d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Ascendant (Trial 90 Hari)',
        'short_description' => 'Paket Trial 90 Hari Ascendant dengan diskon hemat untuk warga aktif peradaban.',
        'description' => "### Hak Istimewa Kasta Ascendant (Trial 90 Hari)\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Ascendant Trial.\n" .
            "- **Maksimal Homes:** 3 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 4 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 5 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 2 Menit 15 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +3% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +5% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Masa Aktif:** 90 Hari (3 Bulan Penuh).",
        'position' => 2,
        'image' => 'package-ascendant.jpg',
        'price' => 45000,
        'commands' => json_encode(['lp user {player} parent addtemp ascendant 90d', 'lp user {player} permission settemp apexsions.rank.trial true 90d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Ascendant (Permanen)',
        'short_description' => 'Kasta Ascendant seumur hidup dengan Kit Ascendant eksklusif dan saldo awal Rp 50.000.',
        'description' => "### Hak Istimewa Kasta Ascendant (PERMANEN)\n" .
            "- **Durasi:** Permanen Seumur Hidup (Tanpa Batas Waktu).\n" .
            "- **Mencakup:** SELURUH benefit Ascendant Trial.\n" .
            "- **Bonus Uang Satu Kali:** Rp 50.000 Saldo Server (Diberikan sekali, anti-duplikasi).\n" .
            "- **Akses Kit Eksklusif:** Ascendant Kit berkala (`/kits`).\n" .
            "- **Maksimal Homes:** 3 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 4 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 5 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 2 Menit 15 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +3% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +5% Pengganda kenaikan EXP Level Karakter.",
        'position' => 3,
        'image' => 'package-ascendant.jpg',
        'price' => 65000,
        'commands' => json_encode(['lp user {player} parent set ascendant', 'lp user {player} permission set apexsions.rank.permanent true', 'eco give {player} 50000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // 2. ARCHON
    // =========================================================================
    [
        'category_id' => 1,
        'name' => 'Archon (Trial 30 Hari)',
        'short_description' => 'Kasta bangsawan agung Trial 30 Hari dengan perintah utilitas /craft, /enderchest, dan multiplier bank.',
        'description' => "### Hak Istimewa Kasta Archon (Trial 30 Hari)\n" .
            "- **Prefix Chat & Tab:** `[💎 ARCHON]` dengan gradien cyan kristal berkilau.\n" .
            "- **Maksimal Homes:** 4 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 7 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 6 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 2 Menit (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +5% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +8% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 1.2x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft` (Portable Workbench), `/enderchest` (`/ec`).\n" .
            "- **Masa Aktif:** 30 Hari.",
        'position' => 4,
        'image' => 'package-archon.jpg',
        'price' => 40000,
        'commands' => json_encode(['lp user {player} parent addtemp archon 30d', 'lp user {player} permission settemp apexsions.rank.trial true 30d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Archon (Trial 90 Hari)',
        'short_description' => 'Paket 90 Hari Archon dengan seluruh utilitas craft, enderchest, dan bunga bank 1.2x.',
        'description' => "### Hak Istimewa Kasta Archon (Trial 90 Hari)\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Archon Trial.\n" .
            "- **Maksimal Homes:** 4 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 7 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 6 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 2 Menit (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +5% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +8% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 1.2x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/enderchest`.\n" .
            "- **Masa Aktif:** 90 Hari.",
        'position' => 5,
        'image' => 'package-archon.jpg',
        'price' => 95000,
        'commands' => json_encode(['lp user {player} parent addtemp archon 90d', 'lp user {player} permission settemp apexsions.rank.trial true 90d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Archon (Permanen)',
        'short_description' => 'Archon Seumur Hidup! Termasuk Kit Archon, Kit Ascendant, dan bonus uang Rp 80.000.',
        'description' => "### Hak Istimewa Kasta Archon (PERMANEN)\n" .
            "- **Durasi:** Permanen Seumur Hidup.\n" .
            "- **Mencakup:** SELURUH keuntungan Archon Trial.\n" .
            "- **Bonus Uang Satu Kali:** Rp 80.000 Saldo Server (Diberikan sekali, anti-duplikasi).\n" .
            "- **Hierarki Kit:** Akses ke Archon Kit + Ascendant Kit (`/kits`).\n" .
            "- **Maksimal Homes:** 4 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 7 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 6 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 2 Menit (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +5% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +8% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 1.2x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/enderchest`.",
        'position' => 6,
        'image' => 'package-archon.jpg',
        'price' => 135000,
        'commands' => json_encode(['lp user {player} parent set archon', 'lp user {player} permission set apexsions.rank.permanent true', 'eco give {player} 80000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // 3. SOVEREIGN
    // =========================================================================
    [
        'category_id' => 1,
        'name' => 'Sovereign (Trial 30 Hari)',
        'short_description' => 'Penguasa kerajaan Trial 30 Hari dengan /anvil, /smithing, dan bunga bank 1.5x.',
        'description' => "### Hak Istimewa Kasta Sovereign (Trial 30 Hari)\n" .
            "- **Prefix Chat & Tab:** `[⚜ SOVEREIGN]` dengan gradien emas kemilau kerajaan.\n" .
            "- **Maksimal Homes:** 5 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 10 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 8 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 1 Menit 35 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +8% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +10% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 1.5x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/enderchest`.\n" .
            "- **Masa Aktif:** 30 Hari.",
        'position' => 7,
        'image' => 'package-sovereign.jpg',
        'price' => 75000,
        'commands' => json_encode(['lp user {player} parent addtemp sovereign 30d', 'lp user {player} permission settemp apexsions.rank.trial true 30d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Sovereign (Trial 90 Hari)',
        'short_description' => 'Paket 90 Hari Sovereign dengan batas 5 homes, 10 lelang, dan 8 custom enchants.',
        'description' => "### Hak Istimewa Kasta Sovereign (Trial 90 Hari)\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Sovereign Trial.\n" .
            "- **Maksimal Homes:** 5 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 10 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 8 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 1 Menit 35 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +8% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +10% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 1.5x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/enderchest`.\n" .
            "- **Masa Aktif:** 90 Hari.",
        'position' => 8,
        'image' => 'package-sovereign.jpg',
        'price' => 180000,
        'commands' => json_encode(['lp user {player} parent addtemp sovereign 90d', 'lp user {player} permission settemp apexsions.rank.trial true 90d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Sovereign (Permanen)',
        'short_description' => 'Sovereign Seumur Hidup! Termasuk Nickname GUI, Kit Sovereign/Archon/Ascendant, dan uang Rp 120.000.',
        'description' => "### Hak Istimewa Kasta Sovereign (PERMANEN)\n" .
            "- **Durasi:** Permanen Seumur Hidup.\n" .
            "- **Mencakup:** SELURUH keuntungan Sovereign Trial.\n" .
            "- **Bonus Uang Satu Kali:** Rp 120.000 Saldo Server (Diberikan sekali, anti-duplikasi).\n" .
            "- **Fitur Nickname:** Akses Nickname GUI (`/nick`). Dapat menggunakan nama samaran (Tanpa edit warna).\n" .
            "- **Hierarki Kit:** Akses ke Sovereign Kit + Archon Kit + Ascendant Kit (`/kits`).\n" .
            "- **Maksimal Homes:** 5 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 10 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 8 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 1 Menit 35 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +8% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +10% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 1.5x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/enderchest`.",
        'position' => 9,
        'image' => 'package-sovereign.jpg',
        'price' => 250000,
        'commands' => json_encode(['lp user {player} parent set sovereign', 'lp user {player} permission set apexsions.rank.permanent true', 'eco give {player} 120000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // 4. EMPEROR
    // =========================================================================
    [
        'category_id' => 1,
        'name' => 'Emperor (Trial 30 Hari)',
        'short_description' => 'Kaisar perang agung Trial 30 Hari dengan /repair, /feed (5m cd), /hat, dan bunga bank 2.0x.',
        'description' => "### Hak Istimewa Kasta Emperor (Trial 30 Hari)\n" .
            "- **Prefix Chat & Tab:** `[⚔ EMPEROR]` dengan gradien merah rubi membara.\n" .
            "- **Maksimal Homes:** 7 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 14 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 11 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 1 Menit 10 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +12% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +14% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 2.0x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/repair`, `/feed` (Cooldown 5 Menit), `/hat`, `/enderchest`.\n" .
            "- **Masa Aktif:** 30 Hari.",
        'position' => 10,
        'image' => 'package-emperor.jpg',
        'price' => 135000,
        'commands' => json_encode(['lp user {player} parent addtemp emperor 30d', 'lp user {player} permission settemp apexsions.rank.trial true 30d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Emperor (Trial 90 Hari)',
        'short_description' => 'Paket 90 Hari Emperor dengan 7 homes, 14 lelang, 11 enchants, dan bunga bank 2.0x.',
        'description' => "### Hak Istimewa Kasta Emperor (Trial 90 Hari)\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Emperor Trial.\n" .
            "- **Maksimal Homes:** 7 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 14 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 11 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 1 Menit 10 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +12% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +14% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 2.0x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/repair`, `/feed`, `/hat`, `/enderchest`.\n" .
            "- **Masa Aktif:** 90 Hari.",
        'position' => 11,
        'image' => 'package-emperor.jpg',
        'price' => 320000,
        'commands' => json_encode(['lp user {player} parent addtemp emperor 90d', 'lp user {player} permission settemp apexsions.rank.trial true 90d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Emperor (Permanen)',
        'short_description' => 'Emperor Seumur Hidup! Gratis Sio Pass musim ini, diskon 10% musim depan, Kit Emperor s/d Ascendant, dan uang Rp 180.000.',
        'description' => "### Hak Istimewa Kasta Emperor (PERMANEN)\n" .
            "- **Durasi:** Permanen Seumur Hidup.\n" .
            "- **Mencakup:** SELURUH keuntungan Emperor Trial.\n" .
            "- **Bonus Uang Satu Kali:** Rp 180.000 Saldo Server (Diberikan sekali, anti-duplikasi).\n" .
            "- **Keistimewaan BattlePass:**\n" .
            "  - **Musim Berjalan:** Gratis Membuka Akses **Sio Pass** di musim aktif saat ini!\n" .
            "  - **Musim Mendatang:** Diskon Khusus **10%** untuk setiap pembelian Sio Pass musim baru.\n" .
            "- **Fitur Nickname:** Akses Nickname GUI (`/nick`) dengan kustomisasi warna solid (Gradien tidak diizinkan).\n" .
            "- **Hierarki Kit:** Akses ke Emperor Kit + Sovereign Kit + Archon Kit + Ascendant Kit (`/kits`).\n" .
            "- **Maksimal Homes:** 7 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 14 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 11 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** 1 Menit 10 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +12% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +14% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 2.0x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/repair`, `/feed` (Cooldown 5 Menit), `/hat`, `/enderchest`.",
        'position' => 12,
        'image' => 'package-emperor.jpg',
        'price' => 450000,
        'commands' => json_encode(['lp user {player} parent set emperor', 'lp user {player} permission set apexsions.rank.permanent true', 'abp pass grant {player} sio', 'eco give {player} 180000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // 5. SIONS
    // =========================================================================
    [
        'category_id' => 1,
        'name' => 'Sions (Trial 30 Hari)',
        'short_description' => 'Puncak peradaban Trial 30 Hari dengan 10 homes, 20 lelang, 15 enchants, RTP 50s, dan bunga bank 3.0x.',
        'description' => "### Hak Istimewa Kasta Sions (Trial 30 Hari)\n" .
            "- **Prefix Chat & Tab:** `[✦ SIONS ✦]` dengan gradien Cyan-Emas Ultra Elegan.\n" .
            "- **Maksimal Homes:** 10 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 20 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 15 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** Hanya 50 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +17% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +20% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 3.0x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/repair`, `/feed` (Cooldown 3 Menit), `/hat`, `/enderchest`.\n" .
            "- **Masa Aktif:** 30 Hari.",
        'position' => 13,
        'image' => 'package-sions.jpg',
        'price' => 225000,
        'commands' => json_encode(['lp user {player} parent addtemp sions 30d', 'lp user {player} permission settemp apexsions.rank.trial true 30d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Sions (Trial 90 Hari)',
        'short_description' => 'Paket 90 Hari Sions: Pengalaman puncak kasta peradaban Apexsions.',
        'description' => "### Hak Istimewa Kasta Sions (Trial 90 Hari)\n" .
            "- **Mencakup:** Seluruh keuntungan kasta Sions Trial.\n" .
            "- **Maksimal Homes:** 10 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 20 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 15 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** Hanya 50 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +17% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +20% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 3.0x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/repair`, `/feed` (Cooldown 3 Menit), `/hat`, `/enderchest`.\n" .
            "- **Masa Aktif:** 90 Hari.",
        'position' => 14,
        'image' => 'package-sions.jpg',
        'price' => 550000,
        'commands' => json_encode(['lp user {player} parent addtemp sions 90d', 'lp user {player} permission settemp apexsions.rank.trial true 90d']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'name' => 'Sions (Permanen)',
        'short_description' => 'Status Puncak Apexsions! Buka Sio & Exsio Pass musim ini, diskon 15% musim depan, seluruh Kit, dan uang Rp 300.000.',
        'description' => "### Hak Istimewa Kasta Sions (PERMANEN - PUNCAK DONATUR)\n" .
            "- **Durasi:** Permanen Seumur Hidup.\n" .
            "- **Mencakup:** SELURUH keuntungan Sions Trial dan semua tingkatan kasta di bawahnya.\n" .
            "- **Bonus Uang Satu Kali:** Rp 300.000 Saldo Server (Diberikan sekali, anti-duplikasi).\n" .
            "- **Keistimewaan BattlePass:**\n" .
            "  - **Musim Berjalan:** Gratis Membuka Akses **Sio Pass + Exsio Pass** musim ini!\n" .
            "  - **Musim Mendatang:** Diskon Eksklusif **15%** untuk pembelian Sio Pass dan Exsio Pass setiap musim baru.\n" .
            "- **Fitur Nickname:** Akses Penuh Nickname GUI (`/nick`) dengan seluruh pilihan warna dan gradien animasi.\n" .
            "- **Hierarki Kit Penuh:** Akses ke Sions Kit + Emperor Kit + Sovereign Kit + Archon Kit + Ascendant Kit (`/kits`).\n" .
            "- **Maksimal Homes:** 10 Homes (`/sethome`).\n" .
            "- **Batas Listing Lelang:** 20 Barang aktif di Auction House (`/ah`).\n" .
            "- **Batas Custom Enchants:** Maksimal 15 Sihir Kustom per item.\n" .
            "- **Cooldown RTP:** Hanya 50 Detik (`/rtp` / `/tpr`).\n" .
            "- **Bonus Jual Shop:** +17% Harga jual komoditas di Toko Kerajaan (`/shop`).\n" .
            "- **Bonus EXP Level:** +20% Pengganda kenaikan EXP Level Karakter.\n" .
            "- **Imbal Hasil Deposito Bank:** 3.0x Multiplier Bunga Bank (`/bank`).\n" .
            "- **Perintah Utilitas:** `/craft`, `/anvil`, `/smithing`, `/repair`, `/feed` (Cooldown 3 Menit), `/hat`, `/enderchest`.",
        'position' => 15,
        'image' => 'package-sions.jpg',
        'price' => 800000,
        'commands' => json_encode(['lp user {player} parent set sions', 'lp user {player} permission set apexsions.rank.permanent true', 'abp pass grant {player} exsio', 'abp pass grant {player} sio', 'eco give {player} 300000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // 6. BATTLEPASS MUSIMAN
    // =========================================================================
    [
        'category_id' => 2,
        'name' => 'Sio Pass',
        'short_description' => 'Akses 100 level jalur hadiah musiman Sio Pass dan quests mingguan eksklusif.',
        'description' => "### Hak Istimewa Sio Pass (Civilization Battlepass Season)\n" .
            "- **Akses 100 Level Premium:** Membuka seluruh tier hadiah jalur emas (Gold Track) level 1 s/d 100.\n" .
            "- **Quests Eksklusif:** Akses ke Quests Harian & Mingguan berhadiah Apex Coins serta material langka.\n" .
            "- **EXP Boost:** Pengganda perolehan EXP Pass sebesar +25% dari setiap aktivitas peradaban.\n" .
            "- **Akses EXP Shop:** Hak berbelanja di Rotating EXP Shop dengan penawaran diskon musiman (`/abp`).\n" .
            "- **Perlindungan Akun:** Diskon otomatis server-side untuk pemegang rank Emperor (10%) & Sions (15%).",
        'position' => 1,
        'image' => 'package-sio-pass.jpg',
        'price' => 45000,
        'commands' => json_encode(['abp pass grant {player} sio']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 2,
        'name' => 'Exsio Pass',
        'short_description' => 'Tier tertinggi! Membeli Exsio Pass otomatis membuka Sio Pass + 20 Level Skip dan Kosmetik Mitos.',
        'description' => "### Hak Istimewa Exsio Pass (Ultimate Battlepass Season)\n" .
            "> **PENTING:** Membeli Exsio Pass otomatis membuka **Sio Pass** dan seluruh keuntungannya!\n\n" .
            "- **Termasuk Sio Pass Penuh:** Membuka seluruh 100 tier hadiah jalur emas Sio Pass.\n" .
            "- **Instan Level Skip:** Langsung melompat +20 Level BattlePass awal secara instan.\n" .
            "- **Kosmetik Mitos Musiman:** Sayap Kosmetik Eksklusif, Partikel Aura Mitos, dan Gelar Chat Unik.\n" .
            "- **Bonus Tunai Langsung:** Tambahan Rp 50.000 saldo in-game dan Crate Keys langsung ke inventory.\n" .
            "- **Perlindungan Akun:** Diskon otomatis server-side 15% untuk pemegang rank Sions Permanen.",
        'position' => 2,
        'image' => 'package-exsio-pass.jpg',
        'price' => 85000,
        'commands' => json_encode(['abp pass grant {player} exsio', 'abp pass grant {player} sio', 'abp level add {player} 20', 'eco give {player} 50000']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // 7. PAKET DIAMOND PREMIUM RESMI (1 Diamond = Rp 375)
    // =========================================================================
    [
        'category_id' => 3,
        'name' => '40 Diamond 💎',
        'short_description' => 'Paket Starter 40 Diamond premium untuk peradaban Apexsions.',
        'description' => "### 💎 40 Diamond Premium Apexsions\n" .
            "- **Mata Uang:** Diamond resmi server Apexsions (ApexsionsEconomy).\n" .
            "- **Jumlah:** 40 💎 Diamond.\n" .
            "- **Rasio Resmi:** Rp 375 per Diamond.\n" .
            "- **Penggunaan:** Transaksi premium, kosmetik eksklusif, auction premium, dan lelang pasar bebas.\n" .
            "- **Aktivasi:** Otomatis ditambahkan ke saldo in-game via delivery daemon.",
        'position' => 1,
        'image' => 'package-diamond-40.jpg',
        'price' => 15000,
        'commands' => json_encode(['ecoadmin give {player} 40 diamond']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => '80 Diamond 💎',
        'short_description' => 'Paket Hemat 80 Diamond untuk transaksi peradaban dan perlengkapan.',
        'description' => "### 💎 80 Diamond Premium Apexsions\n" .
            "- **Mata Uang:** Diamond resmi server Apexsions (ApexsionsEconomy).\n" .
            "- **Jumlah:** 80 💎 Diamond.\n" .
            "- **Rasio Resmi:** Rp 375 per Diamond.\n" .
            "- **Penggunaan:** Transaksi premium, kosmetik eksklusif, auction premium, dan lelang pasar bebas.\n" .
            "- **Aktivasi:** Otomatis ditambahkan ke saldo in-game via delivery daemon.",
        'position' => 2,
        'image' => 'package-diamond-80.jpg',
        'price' => 30000,
        'commands' => json_encode(['ecoadmin give {player} 80 diamond']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => '160 Diamond 💎',
        'short_description' => 'Paket Ksatria 160 Diamond untuk dominasi ekonomi peradaban.',
        'description' => "### 💎 160 Diamond Premium Apexsions\n" .
            "- **Mata Uang:** Diamond resmi server Apexsions (ApexsionsEconomy).\n" .
            "- **Jumlah:** 160 💎 Diamond.\n" .
            "- **Rasio Resmi:** Rp 375 per Diamond.\n" .
            "- **Penggunaan:** Transaksi premium, kosmetik eksklusif, auction premium, dan lelang pasar bebas.\n" .
            "- **Aktivasi:** Otomatis ditambahkan ke saldo in-game via delivery daemon.",
        'position' => 3,
        'image' => 'package-diamond-160.jpg',
        'price' => 60000,
        'commands' => json_encode(['ecoadmin give {player} 160 diamond']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => '320 Diamond 💎',
        'short_description' => 'Paket Bangsawan 320 Diamond untuk ekspansi kerajaan dan lelang.',
        'description' => "### 💎 320 Diamond Premium Apexsions\n" .
            "- **Mata Uang:** Diamond resmi server Apexsions (ApexsionsEconomy).\n" .
            "- **Jumlah:** 320 💎 Diamond.\n" .
            "- **Rasio Resmi:** Rp 375 per Diamond.\n" .
            "- **Penggunaan:** Transaksi premium, kosmetik eksklusif, auction premium, dan lelang pasar bebas.\n" .
            "- **Aktivasi:** Otomatis ditambahkan ke saldo in-game via delivery daemon.",
        'position' => 4,
        'image' => 'package-diamond-320.jpg',
        'price' => 120000,
        'commands' => json_encode(['ecoadmin give {player} 320 diamond']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => '640 Diamond 💎',
        'short_description' => 'Paket Kaisar 640 Diamond untuk dominasi ekonomi dan prestise tinggi.',
        'description' => "### 💎 640 Diamond Premium Apexsions\n" .
            "- **Mata Uang:** Diamond resmi server Apexsions (ApexsionsEconomy).\n" .
            "- **Jumlah:** 640 💎 Diamond.\n" .
            "- **Rasio Resmi:** Rp 375 per Diamond.\n" .
            "- **Penggunaan:** Transaksi premium, kosmetik eksklusif, auction premium, dan lelang pasar bebas.\n" .
            "- **Aktivasi:** Otomatis ditambahkan ke saldo in-game via delivery daemon.",
        'position' => 5,
        'image' => 'package-diamond-640.jpg',
        'price' => 240000,
        'commands' => json_encode(['ecoadmin give {player} 640 diamond']),
        'has_quantity' => false,
        'is_enabled' => true,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 3,
        'name' => '1.200 Diamond 💎',
        'short_description' => 'Paket Sultan 1.200 Diamond terlengkap untuk menguasai pasar ekonomi Apexsions.',
        'description' => "### 💎 1.200 Diamond Premium Apexsions\n" .
            "- **Mata Uang:** Diamond resmi server Apexsions (ApexsionsEconomy).\n" .
            "- **Jumlah:** 1.200 💎 Diamond.\n" .
            "- **Rasio Resmi:** Rp 375 per Diamond.\n" .
            "- **Penggunaan:** Transaksi premium, kosmetik eksklusif, auction premium, dan lelang pasar bebas.\n" .
            "- **Aktivasi:** Otomatis ditambahkan ke saldo in-game via delivery daemon.",
        'position' => 6,
        'image' => 'package-diamond-1200.jpg',
        'price' => 450000,
        'commands' => json_encode(['ecoadmin give {player} 1200 diamond']),
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
        'position' => 7,
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
    // =========================================================================
    // KATEGORI 1: PANDUAN PEMULA & PERINTAH (Cat ID: 1)
    // =========================================================================
    [
        'category_id' => 1,
        'position' => 1,
        'title' => 'Panduan 15 Menit Pertama Warga Baru (Zero-to-Hero Roadmap)',
        'slug' => 'panduan-15-menit-pertama',
        'content' => <<<MARKDOWN
# Panduan 15 Menit Pertama: Dari Pengelana Menjadi Warga Berdaulat

Selamat datang di **Apexsions: The Peak Civilizations**! Anda mungkin merasa semesta kerajaan ini begitu luas dan megah hingga bingung harus mulai dari mana. Jangan khawatir! Cukup ikuti peta jalan 15 menit berikut untuk langsung mandiri, memiliki rumah, dan mulai menghasilkan pundi-pundi Rupiah pertama Anda:

---

### ⏱ Menit 0–2: Tiba di Spawn & Ambil Bekal Perintis
1. Anda akan mendarat di pelataran agung **Spawn Nexus**.
2. Segera ketik perintah:
   ```text
   /kit starter
   ```
3. Anda akan menerima satu set perkakas awal, obor, roti makanan bergizi, dan buku panduan kerajaan.
4. *Tip:* Jangan membuang buku panduan; simpan di inventory Anda sebagai kompas awal!

---

### ⏱ Menit 2–5: Menuju Alam Bebas dengan Teleportasi Acak (/rtp)
1. Jangan menghabiskan waktu berjalan kaki ratusan blok untuk keluar dari area spawn!
2. Ketik perintah:
   ```text
   /rtp
   ```
3. Sistem `ApexsionsCore` akan secara otomatis mencari koordinat alam liar yang aman (bebas tebing curam dan genangan lahar) dalam hitungan detik.

---

### ⏱ Menit 5–8: Menentukan Sumpah Kerajaan (/k)
Di Apexsions, kekuatan Anda berlipat ganda saat bergabung dengan salah satu dari Tiga Kerajaan Berdaulat. Ketik `/k` untuk membuka antarmuka pemilihan:
- ☀️ **Zenithar (Puncak Cakrawala):** Cocok untuk penambang & pembangun kastil. Memiliki buff `+15%` Kecepatan Menambang & Experience.
- 🔥 **Solterra (Bara Api & Pasir):** Cocok untuk pejuang duel PvP & penakluk. Memiliki buff `+15%` Melee Damage & Kebal Api di wilayah sendiri.
- 🌿 **Sylvamoor (Hutan Hayat & Laut):** Cocok untuk petani, peternak, dan saudagar. Memiliki buff `+20%` Hasil Panen & Regenerasi Nyawa.

*Pilihlah kerajaan yang paling sesuai dengan gaya bermain favorit Anda!*

---

### ⏱ Menit 8–12: Tancapkan Panji Rumah Pertama Anda (/sethome)
1. Kumpulkan beberapa blok kayu pohon dan buatlah meja kerja (*Crafting Table*).
2. Bangun tempat berteduh sementara untuk melindungi diri dari monster malam.
3. Kunci koordinat markas Anda dengan mengetik:
   ```text
   /sethome rumah
   ```
4. Kapan pun Anda tersesat di alam liar atau selesai berpetualang, cukup ketik `/home rumah` untuk kembali seketika.

---

### ⏱ Menit 12–15: Menghasilkan Rupiah Pertama di Pasar (/sell)
1. Tebang kayu ekstra atau tambang batubara/besi di sekitar Anda.
2. Buka antarmuka penjualan kilat dengan perintah:
   ```text
   /sell
   ```
3. Masukkan item yang ingin dijual ke dalam keranjang. Saldo Rupiah (`Rp`) Anda akan bertambah secara instan!
4. Cek kekayaan Anda kapan saja dengan mengetik `/balance` atau `/money`.

---

### 🚀 Langkah Selanjutnya: Menuju Puncak Kejayaan
- **Tingkatkan Level Karakter:** Tambang bijih mulia dan lawan monster untuk menaikkan level 1–100 dan membuka **Gelar Sequence** bergengsi.
- **Klaim Wilayah Permanen:** Setelah kas Anda cukup, gunakan `/k claim` untuk mengunci tanah peradaban Anda dari segala bentuk penjarahan.
- **Jelajahi Altar Sihir:** Buka `/ce` atau `/enchanter` untuk memperkuat senjata Anda dengan 182 sihir Custom Enchantments!
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'position' => 2,
        'title' => 'Cara Bergabung ke Server Apexsions',
        'slug' => 'cara-bergabung',
        'content' => <<<MARKDOWN
# Cara Bergabung ke Server Apexsions

Apexsions adalah server peradaban Minecraft modular berarsitektur tinggi yang mendukung pemain **Java Edition** dan **Bedrock Edition** secara bersamaan (*Cross-Platform Geyser*).

---

### Informasi Alamat Server (Koneksi)

| Platform | Alamat IP / Host | Port | Versi Minecraft |
| :--- | :--- | :--- | :--- |
| **Java Edition** (PC/Mac/Linux) | `apexsions.my.id` | `32348` | **26.2** (Paper API) |
| **Bedrock Edition** (Android/iOS/Win10/Console) | `apexsions.my.id` | **`32348`** | Versi Terbaru (Bedrock) |

---

### Langkah Mudah Menghubungkan Client

1. **Buka Minecraft Client:** Pastikan Anda menggunakan versi Minecraft **26.2** (Java Edition) atau Bedrock versi terbaru.
2. **Pilih Menu Multiplayer:** Klik tombol **Add Server** (Tambah Server).
3. **Masukkan Data Server:**
   - **Server Name:** Apexsions
   - **Server Address:** `apexsions.my.id:32348`
   - *(Khusus Bedrock, pastikan Port diisi `32348`)*
4. **Masuk ke Dunia:** Klik **Join Server**. Anda akan disambut di lobi utama peradaban.
5. **Klaim Bekal Awal:** Gunakan perintah `/kit starter` untuk langsung memulai petualangan Anda.

---

### Integrasi Akun Web & Server
Untuk keamanan transaksi, riwayat lelang, dan klaim hadiah musiman:
- Kunjungi portal web resmi: [apexsions.my.id](https://apexsions.my.id).
- Gunakan perintah `/link` di dalam server jika diminta untuk menyinkronkan status keamanan profil Anda dengan portal web.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'position' => 3,
        'title' => 'Daftar Perintah Resmi Server (Commands Cheat Sheet)',
        'slug' => 'daftar-perintah-resmi',
        'content' => <<<MARKDOWN
# Daftar Perintah Resmi Server (Commands Cheat Sheet)

Seluruh perintah resmi terdaftar aktif dan aman di bawah ekosistem plugin Apexsions. Gunakan panduan cepat ini sebagai referensi navigasi Anda:

---

### 1. Navigasi & Eksplorasi Dasar
- `/spawn` — Teleportasi kembali ke titik pusat peradaban utama.
- `/rtp` — Teleportasi acak ke alam liar yang aman untuk mendirikan pemukiman baru.
- `/sethome <nama>` — Menandai koordinat markas pribadi Anda.
- `/home <nama>` — Teleportasi kembali ke titik rumah yang telah ditandai.
- `/tpa <player>` — Mengirimkan permintaan teleportasi ramah ke pemain lain.
- `/tpaccept` — Menerima permintaan teleportasi yang masuk.

---

### 2. Kerajaan & Kedaulatan Wilayah (`ApexsionsCore`)
- `/kingdom` atau `/k` — Membuka GUI pemilihan dan status Tiga Kerajaan Berdaulat.
- `/k info [nama]` — Melihat status ibukota, buff aktif, dan raja kerajaan.
- `/k claim` — Mengklaim chunk wilayah (16x16 blok) atas nama kerajaan Anda.
- `/k map` — Melihat radar wilayah dan perbatasan kerajaan di sekeliling Anda.
- `/k deposit <jumlah>` — Menyetorkan Rupiah ke kas perbendaharaan nexus kerajaan.

---

### 3. Ekonomi & Perdagangan (`ApexsionsEconomy` & `ApexsionsShop`)
- `/money` atau `/balance` atau `/bal` — Memeriksa saldo dompet ganda (Rupiah `Rp` & Diamond `💎`).
- `/pay <player> <jumlah>` — Mentransfer Rupiah secara instan dan aman ke pemain lain.
- `/trade <player>` — Membuka antarmuka barter dua arah yang dilindungi sistem escrow.
- `/ah` — Membuka Pasar Lelang (Auction House) 24 jam.
- `/ah sell <harga>` — Mendaftarkan item yang sedang dipegang ke pasar lelang.
- `/shop` — Membuka katalog Toko Pasar Dinamis (*Dynamic Market*).
- `/sell` — Menjual hasil tambang, panen, atau mob drop secara instan.

---

### 4. Perlengkapan, Kit & Sihir (`ApexsionsCore` & `ApexsionsCustomEnchants`)
- `/kits` atau `/kit` — Membuka GUI daftar kit perlengkapan berkala Anda.
- `/kit preview <nama>` — Melihat pratinjau isi perlengkapan dan set bonus armor.
- `/enchanter` atau `/ce` — Membuka altar penempaan 182 Custom Enchantments.

---

### 5. Battlepass & Komunikasi (`ApexsionsBattlepass` & `ApexsionsChat`)
- `/abp` — Membuka antarmuka utama progres Battlepass musiman.
- `/abp quests` — Memeriksa daftar misi harian dan mingguan Anda.
- `/ch g` — Beralih ke Kanal Chat Global.
- `/ch k` — Beralih ke Kanal Chat Kerajaan (rahasia internal sesama warga).
- `/mail send <player> <pesan>` — Mengirimkan surat offline ke pemain lain.
- `/report <player> <alasan>` — Melaporkan indikasi kecurangan ke meja piket staf.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 1,
        'position' => 4,
        'title' => 'Sistem Progresi Level 1–100 & Gelar Peradaban',
        'slug' => 'progresi-level-dan-gelar',
        'content' => <<<MARKDOWN
# Sistem Progresi Level 1–100 & Gelar Peradaban

Sistem progresi level di Apexsions (`ApexsionsCore`) dirancang untuk menghargai setiap dedikasi pemain dalam membangun peradaban. Level pemain berjalan dari **Level 1 hingga Level 100**, dilengkapi dengan sistem gelar dinamis yang terikat pada kerajaan yang Anda bela.

---

### Sumber Perolehan Experience (XP)
1. **Pertambangan (Mining):** Menambang batu bara, emas murni, diamond, dan ancient debris memberikan limpahan XP peradaban.
2. **Pertempuran (Slaying):** Mengalahkan monster malam, raid pillager, serta boss monster.
3. **Agraris (Farming):** Memanen gandum, wortel, tebu, dan nether wart dalam skala kerajaan.
4. **Pembangunan & Kedaulatan:** Menyetorkan sumber daya ke nexus kerajaan dan berpartisipasi dalam pertempuran wilayah.

---

### Gelar Kehormatan Berdasarkan Kerajaan (Tiap 10 Level)

Setiap mencapai tonggak level baru, gelar kehormatan pada chat prefix Anda akan berubah secara otomatis:

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

### Hadiah Milestone Level (/rewards)
Setiap kelipatan 10 level, Anda dapat mengklaim peti pusaka eksklusif melalui perintah `/rewards` yang berisi:
- Koin Rupiah Server dan Diamond murni.
- Buku Custom Enchantment tingkat Legendary & Fabled.
- Crate Keys langka & Voucher potongan pasar lelang.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // KATEGORI 2: TIGA KERAJAAN & KEDAULATAN (Cat ID: 2)
    // =========================================================================
    [
        'category_id' => 2,
        'position' => 1,
        'title' => 'Babad Sejarah: Runtuhnya Kekaisaran Sions & Eksodus Akbar (The Fall of Sions)',
        'slug' => 'babad-sejarah-tiga-mahkota',
        'content' => <<<MARKDOWN
# Babad Sejarah: Runtuhnya Kekaisaran Sions & Eksodus Akbar

> *"Dahulu kala, satu panji menaungi seluruh cakrawala. Namun ketika kecongkakan membuka gerbang Dimensi Kegelapan demi menciptakan pasukan yang tak terkalahkan, tanah leluhur runtuh ke dalam jurang kehampaan."*<br>
> — **Arsip Suci Kerajaan, Kitab Babad Sions, Bab I: Bait Kehancuran**

---

### Era Keemasan Kekaisaran Sions (The Ancient Empire of Sions)
Berabad-abad silam, seluruh bentang alam benua Apexsions disatukan di bawah satu imperium tunggal yang mahaluas: **Kekaisaran Sions**. Tidak ada tembok pembatas antar-provinsi; peradaban berada pada puncak kejayaan, kemakmuran ekonomi, kemegahan arsitektur istana, dan ketertiban hukum yang harmonis.

Sepanjang sejarah berdirinya, Kekaisaran Sions memegang teguh satu hukum suci yang diwariskan turun-temurun: **dilarang keras menyentuh ilmu hitam maupun kekuatan gelap**. Bangsa Sions hidup murni mengandalkan ilmu keteknikan, sihir elemen alam, dan disiplin ksatria.

---

### Ambisi Terlarang & Bencana Dimensi Kegelapan (The Dark Dimension Cataclysm)
Namun, kedamaian abadi tersebut dirusak oleh ketamakan sang kaisar terakhir. Terobsesi memperluas kedaulatan hingga ke batas tak terhingga, sang pemimpin secara rahasia memerintahkan para ilmuwan istana untuk menembus batas realitas dan membuka portal ke **Dimensi Kegelapan (Dark Dimension)**. Tujuannya adalah menyuntikkan energi hitam purba ke dalam tubuh seluruh pasukan kekaisaran demi menciptakan legiun petarung abadi.

Karena bangsa Sions sama sekali tidak pernah beradaptasi atau menggunakan kekuatan gelap, energi Dimensi Kegelapan tersebut bergolak liar tak terkendali. Celah dimensi meledak menjadi badai kosmik hitam (*The Dark Rift Cataclysm*). Langit terbelah, ibukota agung kekaisaran hancur berkeping-keping, dan tahta tunggal Sions runtuh seketika dalam satu malam kelam.

---

### Eksodus Akbar ke Tiga Penjuru Mata Angin (The Great Exodus)
Dari kepulan abu malapetaka itu, para penyintas yang tercerai-berai mengorganisasi diri dan melarikan diri ke tiga penjuru mata angin, melahirkan **Tiga Kerajaan Berdaulat** yang tegak hingga hari ini:

#### 1. Zenithar (Arah Timur / Zenith) — Dinasti Kerajaan & Kavaleri Elit
Pihak yang berhasil mempertahankan diri di pusat istana adalah **keluarga dinasti kerajaan (Royal Bloodline)** bersama korps pengawal kehormatan kekaisaran. Membawa serta pusaka mahkota, kitab hukum tata krama, dan emas perbendaharaan, mereka bergerak ke **Arah Timur** menuju dataran tinggi berbatu dan puncak pegunungan cakrawala (*Zenith*). Di sana, mereka mendirikan *Solarium Spire Citadel*, memulihkan martabat kekaisaran, dan bersumpah menjaga kemurnian tata krama istana.

#### 2. Sylvamoor (Arah Barat) — Kaum Pekerja, Petani & Pembela Hayati
Penyintas yang bergerak ke **Arah Barat** adalah golongan rakyat pekerja keras: para buruh konstruksi, pengrajin batu, arsitek lapangan, dan petani lumbung kekaisaran, didampingi oleh prajurit garda rakyat (gabungan prajurit yang menguasai sihir alam dasar dan prajurit non-sihir yang tangguh). Menolak ambisi takhta yang telah menghancurkan tanah air mereka, mereka memasuki rimba kanopi purba dan pesisir samudra kristal, mendirikan *Eldergrove Sanctuary* untuk hidup berdampingan selaras dengan alam dan Pohon Dunia.

#### 3. Solterra (Arah Selatan) — Pesulap Tempur & Veteran Garis Depan
Kelompok yang mengarah ke **Arah Selatan** adalah para **pesulap tempur agung (Magicians/Arcanists)** serta para prajurit veteran garis depan terkuat dan paling tangguh dari bekas angkatan bersenjata Sions. Mereka sengaja menantang wilayah ekstrem: kawah vulkanik membara, tanah cadas, dan gurun pasir terik. Di sana mereka membangun benteng obsidian *Ignis Bastion Fortress*, memadukan kedahsyatan sihir elemen api dengan kekuatan fisik brutal tanpa ampun untuk mendominasi lingkungan keras.

---

### Warisan Sejarah: Pembentukan Buff & Debuff
Kondisi masa lalu para pendiri serta adaptasi geografis selama ratusan tahun membentuk fisiologi unik pada warga masing-masing kerajaan:
- **Warga Zenithar** memiliki kelincahan kavaleri (+5% Speed) dan pertahanan perisai elit, namun fisik aristokrat mereka rentan terhadap racun liar (+7% Poison) dan membutuhkan nutrisi makanan berkualitas.
- **Warga Solterra** dianugerahi daya hancur luar biasa (+15% Damage & +10% Crit), namun gaya bertarung agresif tanpa pelindung tebal membuat mereka lebih rapuh (-2 HP Max Health & +8% Damage Masuk).
- **Warga Sylvamoor** memiliki fisik pekerja yang sangat bugar (+2 HP Max Health / 11 Hati & Ketahanan Hayati Tinggi), namun jiwa agraris mereka lemah terhadap api (+15% Fire Damage) dan mengalami mabuk ketinggian di tebing tinggi (Y > 110).

---

### Misteri Reruntuhan Episentrum di Jantung Wilderness (Terra Interdicta)
Tepat di titik tengah alam liar (*Wilderness*) yang memisahkan perbatasan ketiga kerajaan, tersembunyi puing-puing raksasa dari istana pusat Kekaisaran Sions yang telah runtuh. Wilayah ini sengaja tidak dicantumkan dalam kartografi resmi kerajaan mana pun karena distorsi dimensi purba serta kabut anomali kegelapan yang menolak pemetaan biasa.

Para penjelajah dan pemburu harta karun membisikkan bahwa di dalam reruntuhan terkutuk ini tersimpan peninggalan pusaka kaisar, peti perbendaharaan emas kuno, dan gulungan mantra terlarang. Namun, tempat ini dijaga oleh **monster-monster berlevel tinggi dan sisa-sisa legiun prajurit Sions yang telah bermutasi** akibat paparan energi Dimensi Kegelapan abadi—menjadikannya zona ekspedisi paling berbahaya sekaligus paling berharga di seluruh jagat Apexsions.

---

### Ikrar Sumpah Warga Baru
Kini, setiap pengelana baru (*Wanderer*) yang menginjakkan kaki di tanah Apexsions harus memilih jalannya: Apakah Anda akan menjunjung martabat dinasti di **Zenithar**, menaklukkan cadas api di **Solterra**, atau merengkuh kedamaian rimba di **Sylvamoor**? Pilihan Anda adalah takdir peradaban Anda!
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 2,
        'position' => 2,
        'title' => 'Ensiklopedia 3 Kerajaan Berdaulat & Kondisi Wilayah',
        'slug' => 'tiga-kerajaan-berdaulat',
        'content' => <<<MARKDOWN
# Ensiklopedia Tiga Kerajaan Berdaulat & Kondisi Wilayah

<div class="fandom-infobox">
    <div class="fandom-infobox-header">
        <h4 class="fandom-infobox-title">TIGA KERAJAAN BERDAULAT</h4>
        <div class="fandom-infobox-subtitle">Penerus Eksodus Kekaisaran Sions</div>
    </div>
    <div class="fandom-infobox-image">
        <img src="/assets/themes/apexsions/img/realm-showcase.jpg" alt="Bentang Alam Tiga Kerajaan Apexsions">
    </div>
    <table class="fandom-infobox-table">
        <tr><td class="fandom-infobox-label">Kerajaan 1</td><td class="fandom-infobox-value"><strong style="color: #fde047;">Zenithar</strong> (Arah Timur • Dinasti Kerajaan Sions)</td></tr>
        <tr><td class="fandom-infobox-label">Kerajaan 2</td><td class="fandom-infobox-value"><strong style="color: #f87171;">Solterra</strong> (Arah Selatan • Magician &amp; Veteran Perang)</td></tr>
        <tr><td class="fandom-infobox-label">Kerajaan 3</td><td class="fandom-infobox-value"><strong style="color: #4ade80;">Sylvamoor</strong> (Arah Barat • Pekerja &amp; Pejuang Rimba)</td></tr>
        <tr><td class="fandom-infobox-label">Perintah Pilih</td><td class="fandom-infobox-value"><code>/k</code> atau <code>/kingdom choose</code></td></tr>
        <tr><td class="fandom-infobox-label">Pajak Wilayah</td><td class="fandom-infobox-value">Zenithar: 25% • Solterra: 20% • Sylvamoor: 15%</td></tr>
        <tr><td class="fandom-infobox-label">Biaya Perdagangan</td><td class="fandom-infobox-value">Internal: Bebas Pajak • Lintas Kerajaan: Dikenakan Tarif Transportasi</td></tr>
    </table>
</div>

Di Apexsions, tanah air terbagi menjadi tiga kerajaan otonom yang lahir dari Eksodus Akbar pasca runtuhnya Kekaisaran Sions. Setiap kerajaan memiliki fisiologi wilayah, filosofi peradaban, serta **Buff dan Debuff** resmi yang aktif secara otomatis di dalam game (`ApexsionsCore`).

---

### 1. Zenithar (Celestial & Solar Realm — Teritori Timur)
*Penerus garis keturunan dinasti kekaisaran yang mempertahankan relik suci dan menara peradaban di puncak langit cakrawala.*

- **Asal-Usul Eksodus:** Dinasti & keluarga kerajaan Sions serta kavaleri pengawal kehormatan istana yang melarikan diri ke arah Timur.
- **Ibukota:** Solarium Spire Citadel
- **Koordinat Ibukota:** `world (-3028, 64, -5597)`
- **Bioma Khas:** Sky Plains, High Peaks, Jagged Peaks
- **Gelar Raja Tertinggi:** *Monarch of the Sun* (Gelar Tertinggi Pemain: *✦ EMPEROR OF ZENITHAR ✦*)
- **Pajak Kas Wilayah:** 25% (Pemeliharaan istana megah & pertahanan perisai nexus)
- **Karakteristik & Buff Resmi (In-Game):**
  - `+5%` Kecepatan Gerak (*Speed Boost*) — Kelincahan formasi kavaleri elit.
  - `+7%` Keberuntungan (*Luck Boost*) — Berkah kemakmuran dinasti matahari.
  - `+6%` Total Serangan (*All Damage Boost*) — Disiplin taktik pedang kerajaan.
  - `+6%` Pertahanan Diri (*Defense*) — Kokohnya zirah lapis emas istana.
  - `-5%` Reduksi Serangan Critical Musuh — Tangguhnya perisai kehormatan.
- **Kelemahan & Debuff Fisik (In-Game):**
  - `+7%` Damage & Durasi Efek Racun (*Poison Vulnerability*) — Fisik bangsawan yang steril tidak kebal terhadap racun liar.
  - Makanan Memulihkan Hunger Bar Lebih Sedikit (-1 Point) — Terbiasa dengan santapan jamuan istana steril.
  - Harga pasar fluktuatif di pasar dinamis.

---

### 2. Solterra (Crimson Earth & Fire Empire — Teritori Selatan)
*Kekaisaran perang perkasa bermahkotakan kawah lahar membara dan bukit pasir cadas.*

- **Asal-Usul Eksodus:** Para pesulap tempur agung (arcanists) serta prajurit veteran garis depan terkuat bekas legiun Sions yang bermigrasi ke arah Selatan.
- **Ibukota:** Ignis Bastion Fortress
- **Koordinat Ibukota:** `world (-5843, 65, 889)`
- **Bioma Khas:** Desert, Badlands / Mesa, Savanna Plateau
- **Gelar Raja Tertinggi:** *Warlord of the Dunes* (Gelar Tertinggi Pemain: *✦ LORD OF SOLTERRA ✦*)
- **Pajak Kas Wilayah:** 20%
- **Karakteristik & Buff Resmi (In-Game):**
  - `+15%` Total Serangan (*All Damage Boost*) — Kedahsyatan sihir penghancur dan kekuatan fisik brutal.
  - `+10%` Serangan Kritis (*Critical Damage Boost*) — Tebasan mematikan tak kenal ampun.
  - `+10%` Kecepatan Menambang (*Mining Speed Boost*) — Pengalaman memecah cadas vulkanik dan obsidian.
  - `+2%` Pertahanan Diri (*Defense*).
  - Nilai Jual Ore Tinggi (Rasio jual bijih tambang stabil di 65% harga beli) — Pusat penempaan senjata berat.
- **Kelemahan & Debuff Fisik (In-Game):**
  - `-2 HP` Maksimal Darah (Total 9 Hati) — Efek samping luka sihir panas dan kebiasaan bertarung tanpa armor pelindung penuh.
  - `+8%` Total Damage Diterima (*Damage Vulnerability*) — Kecerobohan agresif dalam duel.
  - `+7%` Laju Pengurangan Hunger Bar (*Exhaustion Rate*) — Suhu gurun yang membakar menguras kalori lebih cepat.
  - Tanaman & lahan pertanian lebih cepat mengering di tanah cadas.

---

### 3. Sylvamoor (Azure Crystal & Ocean Realm — Teritori Barat)
*Kerajaan suaka mandiri di tengah belantara kanopi purba, pohon dunia mistis, dan samudra kristal biru.*

- **Asal-Usul Eksodus:** Kaum buruh pekerja, pembangun, petani lumbung, serta prajurit garda rakyat (sihir alam dan non-sihir) yang mengungsi ke arah Barat.
- **Ibukota:** Eldergrove Sanctuary
- **Koordinat Ibukota:** `world (-9666, 64, -4812)`
- **Bioma Khas:** Old Growth Taiga, Jungle, Warm Ocean
- **Gelar Raja Tertinggi:** *Guardian of the World Tree* (Gelar Tertinggi Pemain: *✦ AVATAR OF SYLVAMOOR ✦*)
- **Pajak Kas Wilayah:** 15% (Sistem gotong-royong swadaya masyarakat)
- **Karakteristik & Buff Resmi (In-Game):**
  - `+2 HP` Maksimal Darah (Total 11 Hati) — Daya tahan fisik prima dari kaum pekerja keras.
  - `+12%` Keberuntungan (*Luck Boost*) — Berkah hayati keselarasan dengan alam.
  - `+7%` Peluang Ekstra Drop Monster (*Mob Drop Rate*) — Keahlian membedah anatomi hasil buruan rimba.
  - `+8% Defense & 5% Reduksi Damage Masuk (~12.6% Total Defense)` — Perlindungan kulit pohon purba.
  - `-5%` Damage & Durasi Racun (*Poison Resistance*) — Penguasaan alkemi herbal dan penawar alami.
  - Kelembapan lahan pertanian dan tanaman selalu stabil (tidak pernah mengering).
- **Kelemahan & Debuff Fisik (In-Game):**
  - Mabuk Ketinggian di `Y > 110` (Efek Hunger & Weakness Ringan) — Terbiasa berdiam di bawah kanopi rindang lembah.
  - `+15%` Kerusakan Terbakar (Api, Lava, Magma) — Kelemahan mutlak serat alam terhadap elemen panas.
  - `-10%` Kecepatan Menambang — Kurang terlatih mengekstraksi batuan keras bawah tanah.
  - `-10%` Serangan ke Pemain (PvP) & `-5%` Serangan ke Monster (PvE) — Jiwa komunal damai dan persenjataan bertani.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 2,
        'position' => 3,
        'title' => 'Klaim Wilayah Kerajaan & Proteksi Nexus',
        'slug' => 'klaim-wilayah-dan-nexus',
        'content' => <<<MARKDOWN
# Klaim Wilayah Kerajaan & Proteksi Nexus

Setiap jengkal tanah di Apexsions dilindungi oleh sistem kedaulatan tanah (`ApexsionsCore`). Pemain dapat memperluas wilayah pemukiman mereka dan melindunginya dari kehancuran maupun penjarahan.

---

### Cara Mengklaim Wilayah Baru
1. Berdirilah di chunk (area 16x16 blok) yang belum memiliki pemilik.
2. Gunakan perintah:
   ```text
   /k claim
   ```
3. Pastikan kas pribadi Anda memiliki saldo Rupiah yang mencukupi untuk biaya pemeliharaan awal.
4. Periksa batas wilayah di sekeliling Anda dengan perintah `/k map`.

---

### Sistem Proteksi & Otoritas Chunk
Setelah chunk diklaim atas nama kerajaan Anda:
- Pemain dari kerajaan lain **tidak dapat** menghancurkan blok, menaruh blok, membuka peti (*chest*), ataupun menggunakan pintu dan tombol.
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
        'position' => 4,
        'title' => 'Perang Kerajaan, Siege & Combat Tag',
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
- **Hukuman Disconnect:** Jika Anda sengaja keluar dari permainan (*Force Close / Alt+F4 / Disconnect*) saat Combat Tag masih aktif, karakter Anda akan **mati seketika**, seluruh inventory Anda berhamburan di tanah, dan musuh dinyatakan sebagai pemenang duel!

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

    // =========================================================================
    // KATEGORI 3: EKONOMI & PERDAGANGAN (Cat ID: 3)
    // =========================================================================
    [
        'category_id' => 3,
        'position' => 1,
        'title' => 'Sistem Mata Uang: Rupiah (Rp) & Diamond (💎)',
        'slug' => 'sistem-mata-uang',
        'content' => <<<MARKDOWN
# Sistem Mata Uang: Rupiah (Rp) & Diamond (💎)

Ekosistem ekonomi di Apexsions (`ApexsionsEconomy`) menggunakan arsitektur **Dual-Currency** dengan proteksi transaksi ACID (*Atomic, Consistent, Isolated, Durable*). Hal ini menjamin tidak akan pernah terjadi duplikasi uang, kehilangan saldo, maupun saldo bernilai negatif.

---

### Dua Mata Uang Resmi Server

| Mata Uang | Simbol | Sifat Mata Uang | Cara Memperoleh | Penggunaan Utama |
| :--- | :---: | :--- | :--- | :--- |
| **Rupiah** | `Rp` | Uang sirkulasi fiat harian | Menjual hasil tambang/panen di `/shop`, reward misi, gaji kerajaan | Pembelian item pasar dinamis, klaim wilayah, pajak nexus, barter |
| **Diamond** | `💎` | Logam mulia bernilai tinggi | Menambang diamond ore di kedalaman Y < 0, reward event khusus | Membeli Custom Enchants kasta atas (Legendary & Fabled), item langka |

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

Toko server di Apexsions (`ApexsionsShop`) digerakkan oleh algoritma **Supply and Demand** otomatis. Harga komoditas tidak bersifat statis, melainkan bergerak dinamis sesuai volume transaksi seluruh pemain di server.

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
- Jika banyak pemain menjual satu komoditas secara massal (contoh: puluhan ribu kentang), harga beli server untuk komoditas tersebut akan mengalami depresiasi (turun perlahan).
- Jika pasar kekurangan stok dan banyak pemain membelinya, harga akan mengalami apresiasi (naik secara wajar).
- Manfaatkan fluktuasi ini untuk menjadi pedagang peradaban yang jeli!
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

### 1. Pasar Lelang (Auction House /ah)
- Buka antarmuka lelang dengan perintah `/ah`.
- Untuk menjual item yang sedang Anda pegang: ketik `/ah sell <harga>`.
- Item akan tayang di etalase pasar selama **24 Jam**.
- Server mengenakan biaya administrasi pendaftaran lelang sebesar **2%** yang dipotong otomatis saat barang laku terjual.
- Setiap pemain memiliki kuota maksimal hingga **10 slot penjualan aktif** secara bersamaan.

---

### 2. Barter Window Escrow (/trade <player>)
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
    [
        'category_id' => 3,
        'position' => 4,
        'title' => 'Strategi Kemakmuran: Panduan Menjadi Saudagar Sukses',
        'slug' => 'strategi-kemakmuran',
        'content' => <<<MARKDOWN
# Strategi Kemakmuran: Panduan Menjadi Saudagar Sukses

Menjadi kaya raya di Apexsions bukanlah mimpi kosong. Banyak pemain pemula terjebak hanya dengan menggali batu biasa. Berikut adalah 4 rahasia para taipan ekonomi Apexsions dalam melipatgandakan kekayaan dari nol:

---

### 1. Eksploitasi Rute Tambang Dalam (Y: -58)
- Gali hingga kedalaman `Y: -53` hingga `Y: -58` untuk memaksimalkan peluang menemukan Diamond Ore dan Deepslate Gold Ore.
- **Wajib Gunakan Sihir:** Usahakan segera memiliki beliung ber-enchant `Fortune III` atau sihir kustom `Trench` & `Telepathy` agar hasil tambang berlipat 3x dan langsung masuk ke kantong.

---

### 2. Sinergi Pertanian Otomatis Sylvamoor
- Jika Anda memilih kerajaan **Sylvamoor**, Anda memiliki buff alami `+20%` hasil panen.
- Buat ladang tebu atau labu otomatis berskala 4 chunk.
- Jual hasil panen secara berkala menggunakan perintah `/sell`. Ini adalah sumber pendapatan pasif paling stabil!

---

### 3. Seni "Market Timing" di Pasar Dinamis (/shop)
- **Jangan Jual Massal saat Harga Jeblok:** Jika harga gandum sedang jatuh karena ada pemain lain yang baru menjual 50 peti gandum, simpan stok Anda di dalam peti.
- Tunggu beberapa jam hingga algoritma pasar kembali menaikkan harga dasar, barulah jual seluruh stok simpanan Anda untuk keuntungan maksimal!

---

### 4. Flipping Buku Sihir di Auction House (/ah)
- Banyak pemain baru yang membutuhkan uang cepat menjual buku kustom `Ultimate` atau `Legendary` di bawah harga pasar di `/ah`.
- Beli buku murah tersebut, lalu jual kembali dengan harga normal atau simpan untuk meracik senjata dewa Anda sendiri. Selisih keuntungan bisa mencapai ratusan ribu Rupiah!
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // KATEGORI 4: CUSTOM ENCHANTS & KITS (Cat ID: 4)
    // =========================================================================
    [
        'category_id' => 4,
        'position' => 1,
        'title' => '182 Custom Enchantments & 7 Tingkatan Tier',
        'slug' => 'custom-enchantments',
        'content' => <<<MARKDOWN
# 182 Custom Enchantments & 7 Tingkatan Tier

<div class="fandom-infobox">
    <div class="fandom-infobox-header">
        <h4 class="fandom-infobox-title">ALTAR PENEMPAAN</h4>
        <div class="fandom-infobox-subtitle">ApexsionsCustomEnchants</div>
    </div>
    <div class="fandom-infobox-image">
        <img src="/assets/themes/apexsions/img/package-sovereign.jpg" alt="Altar Sihir Apexsions">
    </div>
    <table class="fandom-infobox-table">
        <tr><td class="fandom-infobox-label">Total Sihir</td><td class="fandom-infobox-value">182 Enchantments Kustom</td></tr>
        <tr><td class="fandom-infobox-label">Tingkatan Tier</td><td class="fandom-infobox-value">7 Kasta (Simple s/d Heroic)</td></tr>
        <tr><td class="fandom-infobox-label">Akses Altar</td><td class="fandom-infobox-value"><code>/enchanter</code> atau <code>/ce</code></td></tr>
        <tr><td class="fandom-infobox-label">Editor Native</td><td class="fandom-infobox-value"><code>/ace create</code> (Paper Dialog)</td></tr>
        <tr><td class="fandom-infobox-label">Perlindungan</td><td class="fandom-infobox-value">White Scroll, Magic Dust</td></tr>
        <tr><td class="fandom-infobox-label">Target Gear</td><td class="fandom-infobox-value">Pedang, Armor, Busur, Alat Tambang</td></tr>
    </table>
</div>

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
- **Fungsi:** Mencegah kehancuran item berharga!
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
    [
        'category_id' => 4,
        'position' => 4,
        'title' => 'Panduan Meta Build Sihir & Sinergi Persenjataan',
        'slug' => 'panduan-meta-build-sihir',
        'content' => <<<MARKDOWN
# Panduan Meta Build Sihir & Sinergi Persenjataan

Memasuki kancah pertempuran peradaban tanpa strategi penempaan sihir adalah tindakan ceroboh. Berikut adalah 4 racikan build sihir (*custom enchantment metas*) terkuat yang telah teruji di arena perang Apexsions:

---

### 🛡 Build 1: "The Immortal Citadel" (Tank Utama / Pelindung Nexus)
- **Fokus:** Daya tahan ekstrem terhadap serangan fisik keroyokan dan ledakan siege.
- **Komposisi Armor:** Full Netherite dengan bonus set **Damage Reduction (-25%)**.
- **Kombinasi Enchant Wajib:**
  - `Overload III` — Menambahkan 4 bar hati ekstra permanen.
  - `Aegis IV` — Menyerap 30% burst damage pertama yang masuk.
  - `Enlighted III` — Meregenerasi darah saat terkena pukulan musuh.
  - `Armored IV` — Mengurangi damage dari pedang ber-enchant tajam.

---

### ⚔ Build 2: "The Crimson Executioner" (PvP Duelist / High Burst)
- **Fokus:** Melumpuhkan musuh dalam hitungan detik melalui kerusakan kritikal beruntun.
- **Komposisi Armor:** Attack Boost Set (+35%).
- **Kombinasi Enchant Senjata (Pedang Netherite):**
  - `Rage VI` — Setiap serangan berturut-turut meningkatkan damage hingga +50%.
  - `Double Strike IV` — Peluang 25% memicu dua tebasan dalam satu klik.
  - `Bleed V` — Memberikan efek pendarahan yang mengabaikan armor lawan.
  - `Lifesteal III` — Menyerap darah musuh untuk menyembuhkan diri sendiri.

---

### 💨 Build 3: "The Phantom Striker" (Assassin / Speed & Evasion)
- **Fokus:** Kecepatan manuver tinggi, menghindar dari serangan, dan membutakan lawan.
- **Komposisi Armor:** Dodge Chance Set (+20%) + Speed Boost Boots.
- **Kombinasi Enchant Wajib:**
  - `Gears III` & `Springs III` — Kecepatan lari dan lompatan ekstra tinggi.
  - `Blind IV` — Peluang membutakan pandangan musuh saat menyerang.
  - `Inquisitive IV` — Mendapatkan bonus XP berlipat saat menumbangkan lawan.

---

### ⛏ Build 4: "The Deep Excavator" (Pencetak Kekayaan Tambang)
- **Fokus:** Menggali ribuan blok dalam hitungan menit untuk mendominasi pasar tambang.
- **Komposisi Beliung:** Netherite Pickaxe.
- **Kombinasi Enchant Wajib:**
  - `Trench III` — Menggali area 3x3 blok sekaligus dalam satu ayunan!
  - `Explosive III` — Memicu ledakan mikro yang merontokkan batuan tanpa merusak ore.
  - `Telepathy I` — Seluruh hasil tambang langsung masuk ke inventory (anti-curi).
  - `Experience V` — Menggandakan perolehan orb XP dari setiap batu yang hancur.

*Peringatan Alkemis: Selalu tempelkan White Scroll pada perkakas utama Anda sebelum mengaplikasikan sihir tingkat Fabled!*
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // KATEGORI 5: BATTLEPASS & KOMUNIKASI (Cat ID: 5)
    // =========================================================================
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
- **Staff Channel (`/ch s`):** Kanal tertutup khusus para penjaga ketertiban (Herald, Warden, Overseer, Architect, dan Ancestor).

---

### Format Tampilan Chat Modern
Setiap pesan di chatroom menampilkan identitas lengkap peradaban Anda:
```text
[G] [Lv. 45 Apex Templar] [⚜ SOVEREIGN] [✦ ZENITHAR] PlayerName ➔ Pesan Anda
```

---

### Layanan Surat Pribadi & Laporan Kecurangan
- **Kirim Surat Offline:** `/mail send <player> <pesan>` — Mengirimkan pesan kepada teman yang sedang offline. Surat akan terbaca saat mereka login berikutnya.
- **Laporkan Pelanggaran:** `/report <player> <alasan>` — Mengirimkan laporan instan ke meja piket staf jika menemukan indikasi penggunaan cheat ilegal, griefing di luar aturan war, atau pelanggaran tata tertib server.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 5,
        'position' => 3,
        'title' => 'Etika Komunitas, Roleplay & Kode Kehormatan Peradaban',
        'slug' => 'etika-dan-kode-kehormatan',
        'content' => <<<MARKDOWN
# Etika Komunitas, Roleplay & Kode Kehormatan Peradaban

Apexsions dibangun di atas fondasi kehormatan, sportivitas, dan persahabatan antar-pemain. Untuk menjaga agar suasana permainan tetap seru dan beradab, setiap warga wajib mematuhi kode kehormatan berikut:

---

### 1. Integritas Permainan & Larangan Cheat
- Dilarang keras menggunakan client pihak ketiga yang memberikan keuntungan curang (*X-Ray, Auto-Clicker, Fly Hack, Speed Hack, Killaura, Baritone*).
- Sistem pendeteksi otomatis dan staf pengawas (*Overseer*) berhak memberikan sanksi blokir permanen tanpa peringatan bagi pelaku kecurangan.

---

### 2. Aturan Kedaulatan & Anti-Griefing
- Bangunan di dalam chunk terproteksi haram diganggu.
- Penyerbuan wilayah hanya sah jika dilakukan dalam kerangka mekanisme resmi **Kingdom War**.
- Dilarang membuat jebakan portal (*portal trap*) atau jebakan teleportasi mematikan (*TP-trapping*).

---

### 3. Etika Berkomunikasi & Bebas Toksisitas
- Kanal publik adalah ruang bersama. Dilarang menyebarkan ujaran kebencian, pelecehan personal, pornografi, maupun provokasi berbau SARA.
- Bersikaplah ramah dan bantu pemain baru yang baru tiba di server.

---

### 4. Semangat Roleplay Kerajaan
- Rivalitas antar-kerajaan (Zenithar vs Solterra vs Sylvamoor) adalah bumbu roleplay yang seru di dalam game.
- Nikmati persaingan ini dengan kepala dingin dan jiwa ksatria. Jadikan perang sebagai ajang unjuk ketangkasan, bukan permusuhan di dunia nyata!
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],

    // =========================================================================
    // KATEGORI 6: HIERARKI KASTA RESMI (Cat ID: 6)
    // =========================================================================
    [
        'category_id' => 6,
        'position' => 1,
        'title' => 'Struktur 5 Tingkat & 11 Kasta Resmi Apexsions',
        'slug' => 'hierarki-kasta',
        'content' => <<<MARKDOWN
# Struktur 5 Tingkat & 11 Kasta Resmi Apexsions

<div class="fandom-infobox">
    <div class="fandom-infobox-header">
        <h4 class="fandom-infobox-title">HIERARKI KASTA</h4>
        <div class="fandom-infobox-subtitle">ranks.yml • Sumber Kebenaran Resmi</div>
    </div>
    <div class="fandom-infobox-image">
        <img src="/assets/themes/apexsions/img/package-archon.jpg" alt="Hierarki Kasta Apexsions">
    </div>
    <table class="fandom-infobox-table">
        <tr><td class="fandom-infobox-label">Tier Tertinggi</td><td class="fandom-infobox-value"><strong style="color: #fde047;">Tier V: Ancestor</strong> (Weight 100)</td></tr>
        <tr><td class="fandom-infobox-label">Tier Otoritas</td><td class="fandom-infobox-value">Architect &amp; Overseer (Weight 95)</td></tr>
        <tr><td class="fandom-infobox-label">Tier Administrasi</td><td class="fandom-infobox-value">Warden (90) &amp; Herald (80)</td></tr>
        <tr><td class="fandom-infobox-label">Tier Donatur</td><td class="fandom-infobox-value">Sions, Emperor, Sovereign, Archon, Ascendant</td></tr>
        <tr><td class="fandom-infobox-label">Kasta Default</td><td class="fandom-infobox-value">Wanderer (Weight 10)</td></tr>
        <tr><td class="fandom-infobox-label">Sistem Izin</td><td class="fandom-infobox-value">LuckPerms Native Vault Sync</td></tr>
    </table>
</div>

Sumber kebenaran hierarki (`ranks.yml`) membagi peradaban Apexsions ke dalam **5 Tingkat & 11 Kasta Resmi** yang memiliki bobot (*weight*), wewenang, dan kehormatan masing-masing:

---

| Tingkat Hierarki | Kasta | Bobot (Weight) | Peran & Hak Istimewa Utama | Status Hak |
| :--- | :--- | :---: | :--- | :--- |
| **Tingkat V: Puncak** | **👑 THE ANCESTOR** | **100** | Pemilik, pencipta semesta, dan pelindung peradaban Apexsions. | Pemilik Tunggal (Owner) |
| **Tingkat IV: Otoritas** | **📐 ARCHITECT** | **95** | Perancang tata ruang, pembangunan realm, dan estetika arsitektur peradaban. | Dewan Otoritas (Setara) |
| **Tingkat IV: Otoritas** | **👁 OVERSEER** | **95** | Pengawas kedaulatan, audit integritas ekonomi pasar, dan stabilitas peradaban. | Dewan Otoritas (Setara) |
| **Tingkat III: Administrasi** | **🛡 WARDEN** | **90** | Kepala staf administrasi, pengawas keadilan perang, dan penegak hukum server. | Staf Administrator |
| **Tingkat III: Administrasi** | **📜 HERALD** | **80** | Pemandu warga, penegak etika komunitas, dan penengah sengketa wilayah. | Staf Helper / Moderator |
| **Tingkat II: Bangsawan** | **✦ SIONS ✦** | **70** | Puncak kasta donatur tertinggi dengan seluruh hak istimewa, broadcast kemegahan, dan aura mahkota emas. | Kasta Donatur Sultan (Apex Tier) |
| **Tingkat II: Bangsawan** | **⚔ EMPEROR** | **60** | Kasta kaisar perang dengan hak terbang di wilayah, kit bulanan ber-set bonus, dan prioritas antrean. | Kasta Donatur Tier 4 |
| **Tingkat II: Bangsawan** | **⚜ SOVEREIGN** | **50** | Kasta penguasa tanah dengan sayap partikel, kit 14 harian, dan bebas tarif dagang wilayah. | Kasta Donatur Tier 3 |
| **Tingkat II: Bangsawan** | **💎 ARCHON** | **40** | Kasta perajin kristal dengan kit mingguan, utilitas portabel, dan akses enderchest. | Kasta Donatur Tier 2 |
| **Tingkat II: Bangsawan** | **☘ ASCENDANT** | **30** | Kasta warga terhormat dengan kit harian, hak bypass antrean server, dan tambahan klaim wilayah. | Kasta Donatur Tier 1 |
| **Tingkat I: Fondasi** | **WANDERER** | **10** | Warga perintis baru yang memulai perjalanan peradaban di alam liar Apexsions. | Kasta Default (Semua Pemain) |

---

### Integritas Kasta & Sinkronisasi LuckPerms
Seluruh kasta donatur dan staf dikelola secara terpusat dan otomatis melalui sinkronisasi native **LuckPerms**, sehingga setiap pembelian paket di Webstore akan mengaktifkan kasta Anda di server dalam hitungan detik tanpa perlu restart server.
MARKDOWN
        ,
        'created_at' => $now,
        'updated_at' => $now,
    ],
    [
        'category_id' => 6,
        'position' => 2,
        'title' => 'Jalur Kenaikan Spiritual: 10 Urutan Kehormatan (Sequence Pathways)',
        'slug' => 'jalur-kenaikan-spiritual',
        'content' => <<<MARKDOWN
# Jalur Kenaikan Spiritual: 10 Urutan Kehormatan (Sequence Pathways)

> *"Tingkat kekuatan bukanlah sekadar angka di atas kepalamu, melainkan tahapan metamorfosis jiwa dari debu fana menuju kemuliaan Ilahi."*<br>
> — **Kanon Kenaikan Spiritual Apexsions**

Di Apexsions, sistem level 1–100 mengadopsi konsep **Sequence Pathways** yang terinspirasi dari sastra fantasi peradaban. Grinding XP bukan sekadar menaikkan stat, melainkan perjalanan mendaki 10 Urutan Spiritual yang mengubah identitas, gelar prefix, dan takdir peran Anda di medan peradaban:

---

### ☀️ Jalur Penguasa Surya (Zenithar Pathway — The Sun & Heavens)
Bagi mereka yang meniti jalan ketertiban arcanum dan kejayaan langit:
- **Urutan 10: Acolyte of Zenith (Lv. 1–10)** — Murid perintis yang baru merasakan sentuhan pertama kehangatan surya.
- **Urutan 9: Celestial Scout (Lv. 11–20)** — Pengintai langit yang memetakan jurang dan tebing puncak tertinggi.
- **Urutan 8: Sky Warden (Lv. 21–30)** — Penjaga menara pengawas cakrawala dari serbuan makhluk kegelapan.
- **Urutan 7: Astral Knight (Lv. 31–40)** — Ksatria berpedang cahaya bintang pelindung kaum tertindas.
- **Urutan 6: Apex Templar (Lv. 41–50)** — Pendekar suci penjaga kuil kristal matahari yang kebal racun batin.
- **Urutan 5: Star Commander (Lv. 51–60)** — Panglima armada langit yang memegang panji cahaya matahari terbit.
- **Urutan 4: Solaris Archon (Lv. 61–70)** — Wujud setengah dewa yang mampu mengalirkan radiasi surya ke dalam senjatanya.
- **Urutan 3: ⚡ High Celestial ⚡ (Lv. 71–80)** — Pengendali petir dan badai langit yang mampu mengubah arah pertempuran.
- **Urutan 2: 👑 Zenith Paragon 👑 (Lv. 81–90)** — Teladan sempurna peradaban surya bertahtakan cahaya keemasan murni.
- **Urutan 1: ✦ EMPEROR OF ZENITHAR ✦ (Lv. 91–100)** — Sang Kaisar Dirgantara, penguasa mutlak seluruh cakrawala Apexsions!

---

### 🔥 Jalur Panglima Bara (Solterra Pathway — The Fire & Iron)
Bagi mereka yang memilih jalan darah, disiplin besi, dan pengorbanan tanpa batas:
- **Urutan 10: Dune Wanderer (Lv. 1–10)** — Pengelana pasir bertelanjang kaki yang tahan haus di padang tandus.
- **Urutan 9: Sun Scout (Lv. 11–20)** — Penjejak jejak musuh di bawah terik matahari yang membakar kulit.
- **Urutan 8: Terra Blade (Lv. 21–30)** — Pendekar tangguh bersenjatakan pedang tempaan batu vulkanik purba.
- **Urutan 7: Solar Knight (Lv. 31–40)** — Ksatria lapis baja merah yang kebal terhadap siraman magma cair.
- **Urutan 6: Flame Vanguard (Lv. 41–50)** — Pasukan pelopor yang mendobrak gerbang benteng musuh dengan amukan api.
- **Urutan 5: Dune Warlord (Lv. 51–60)** — Jenderal padang pasir yang disegani, penguasa tambang obsidian merah.
- **Urutan 4: Solaris Champion (Lv. 61–70)** — Juara arena tanding yang mampu membelah kobaran api dengan tangan kosong.
- **Urutan 3: 🔥 Sun Sovereign 🔥 (Lv. 71–80)** — Penguasa lahar dan letusan gunung yang menyulap pasir menjadi kaca.
- **Urutan 2: ⚔ Solterra Overlord ⚔* (Lv. 81–90)** — Panglima perang agung yang memegang komando mutlak legiun besi.
- **Urutan 1: ✦ LORD OF SOLTERRA ✦ (Lv. 91–100)** — Penguasa Abadi Tanah Merah, perwujudan kekuatan api primordial!

---

### 🌿 Jalur Penjaga Hayati (Sylvamoor Pathway — The Emerald Life)
Bagi mereka yang menyatu dengan denyut pohon purba, kesembuhan, dan samudra:
- **Urutan 10: Sylvan Citizen (Lv. 1–10)** — Warga rimba pemetik daun pertama dan penabur benih kehidupan.
- **Urutan 9: Grove Keeper (Lv. 11–20)** — Penjaga kebun obat dan perawat bibit pohon dunia yang rapuh.
- **Urutan 8: Forest Warden (Lv. 21–30)** — Pembela satwa liar dan penjaga tapal batas hutan dari penebangan liar.
- **Urutan 7: Wild Knight (Lv. 31–40)** — Ksatria berzirah kayu besi purba yang bersenjatakan duri kristal laut.
- **Urutan 6: Nature Commander (Lv. 41–50)** — Komandan pasukan rimba dan pawang binatang purba alam liar.
- **Urutan 5: Druidic Lord (Lv. 51–60)** — Tetua peramu sihir kehidupan yang mampu menyembuhkan luka fatal kawan.
- **Urutan 4: Verdant Archon (Lv. 61–70)** — Wujud mistis yang mampu memanggil akar bumi raksasa untuk membelenggu musuh.
- **Urutan 3: 🌿 Elder Guardian 🌿 (Lv. 71–80)** — Pelindung suci jantung Pohon Dunia (*The World Tree*).
- **Urutan 2: ⚜ Sylvan Sovereign ⚜ (Lv. 81–90)** — Penguasa seluruh hutan zamrud dan samudra kristal tak bertepi.
- **Urutan 1: ✦ AVATAR OF SYLVAMOOR ✦ (Lv. 91–100)** — Inkarnasi Dewi Kehidupan, penguasa denyut nadi semesta ciptaan!

---

### Upacara Kenaikan Urutan & Hadiah (/rewards)
Setiap kali Anda menembus batas urutan berikutnya (kelipatan 10 level):
1. Prefix kehormatan Anda di obrolan chat akan berevolusi secara otomatis.
2. Segera ketik `/rewards` untuk membuka peti pusaka yang berisi Rupiah, Diamond, dan gulungan sihir langka sebagai bekal pendakian spiritual Anda berikutnya!
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

if ($driver === 'sqlite') {
    DB::statement('PRAGMA foreign_keys = ON;');
} else {
    DB::statement('SET FOREIGN_KEY_CHECKS=1;');
}

echo "\n==========================================================\n";
echo "   SYNCHRONIZATION COMPLETED SUCCESSFULLY! \n";
echo "==========================================================\n";
