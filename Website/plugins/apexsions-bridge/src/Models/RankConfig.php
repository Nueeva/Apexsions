<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Model;

class RankConfig extends Model
{
    protected $table = 'apexsions_rank_configs';

    protected $fillable = [
        'rank_key',
        'display_name',
        'badge',
        'prefix',
        'color',
        'tier',
        'weight',
        'order_index',
        'is_active',
        'is_buyable',
        'banner_image',
        'card_image',
        'description',
        'price_trial_30',
        'price_trial_90',
        'price_permanent',
        'price_upgrade_override',
        'discount_percent',
        'money_reward_permanent',
        'benefit_max_homes',
        'benefit_max_auctions',
        'benefit_max_enchants',
        'benefit_rtp_cooldown',
        'benefit_shop_sell_bonus',
        'benefit_xp_bonus',
        'benefit_bank_multiplier',
        'benefit_feed_cooldown',
        'benefit_commands',
        'benefit_kits',
        'benefit_nick_permission',
        'benefit_battlepass_unlock',
        'benefit_battlepass_discount',
    ];

    protected $casts = [
        'is_active' => 'boolean',
        'is_buyable' => 'boolean',
        'price_trial_30' => 'float',
        'price_trial_90' => 'float',
        'price_permanent' => 'float',
        'price_upgrade_override' => 'float',
        'discount_percent' => 'float',
        'money_reward_permanent' => 'float',
        'benefit_max_homes' => 'integer',
        'benefit_max_auctions' => 'integer',
        'benefit_max_enchants' => 'integer',
        'benefit_rtp_cooldown' => 'integer',
        'benefit_shop_sell_bonus' => 'float',
        'benefit_xp_bonus' => 'float',
        'benefit_bank_multiplier' => 'float',
        'benefit_feed_cooldown' => 'integer',
        'benefit_commands' => 'array',
        'benefit_kits' => 'array',
        'benefit_battlepass_discount' => 'float',
    ];

    /**
     * Seed or return default rank configurations if table is empty.
    /**
     * Complete master default matrix for all 11 official server ranks.
     */
    public static function getDefaultRanksMatrix(): array
    {
        return [
            'wanderer' => [
                'rank_key' => 'wanderer',
                'display_name' => 'Wanderer',
                'badge' => 'Wanderer',
                'prefix' => '[Wanderer] ',
                'color' => '#95a5a6',
                'tier' => 'Tier I',
                'weight' => 10,
                'order_index' => 1,
                'is_active' => true,
                'is_buyable' => false,
                'banner_image' => 'assets/themes/apexsions/img/package-ascendant.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-ascendant.jpg',
                'description' => 'Fondasi peradaban Apexsions. Rank standar bagi setiap warga baru yang menjejakkan kaki di realm.',
                'price_trial_30' => 0.0,
                'price_trial_90' => 0.0,
                'price_permanent' => 0.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 0.0,
                'benefit_max_homes' => 2,
                'benefit_max_auctions' => 3,
                'benefit_max_enchants' => 4,
                'benefit_rtp_cooldown' => 150, // 2m 30s
                'benefit_shop_sell_bonus' => 0.0,
                'benefit_xp_bonus' => 0.0,
                'benefit_bank_multiplier' => 1.0,
                'benefit_feed_cooldown' => null,
                'benefit_commands' => [],
                'benefit_kits' => ['Wanderer Kit'],
                'benefit_nick_permission' => 'none',
                'benefit_battlepass_unlock' => null,
                'benefit_battlepass_discount' => 0.0,
            ],
            'ascendant' => [
                'rank_key' => 'ascendant',
                'display_name' => 'Ascendant',
                'badge' => '☘ ASCENDANT',
                'prefix' => '[☘ ASCENDANT] ',
                'color' => '#38ef7d',
                'tier' => 'Tier II',
                'weight' => 30,
                'order_index' => 2,
                'is_active' => true,
                'is_buyable' => true,
                'banner_image' => 'assets/themes/apexsions/img/package-ascendant.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-ascendant.jpg',
                'description' => 'Awal kebangkitan peradaban. Tingkat pertama menuju strata kekuasaan yang lebih tinggi di Apexsions.',
                'price_trial_30' => 20000.0,
                'price_trial_90' => 45000.0,
                'price_permanent' => 65000.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 50000.0,
                'benefit_max_homes' => 3,
                'benefit_max_auctions' => 4,
                'benefit_max_enchants' => 5,
                'benefit_rtp_cooldown' => 135, // 2m 15s
                'benefit_shop_sell_bonus' => 3.0,
                'benefit_xp_bonus' => 5.0,
                'benefit_bank_multiplier' => 1.0,
                'benefit_feed_cooldown' => null,
                'benefit_commands' => [],
                'benefit_kits' => ['Ascendant Kit'],
                'benefit_nick_permission' => 'none',
                'benefit_battlepass_unlock' => null,
                'benefit_battlepass_discount' => 0.0,
            ],
            'archon' => [
                'rank_key' => 'archon',
                'display_name' => 'Archon',
                'badge' => '💎 ARCHON',
                'prefix' => '[💎 ARCHON] ',
                'color' => '#00c6ff',
                'tier' => 'Tier II',
                'weight' => 40,
                'order_index' => 3,
                'is_active' => true,
                'is_buyable' => true,
                'banner_image' => 'assets/themes/apexsions/img/package-archon.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-archon.jpg',
                'description' => 'Otoritas dan kepemimpinan peradaban tingkat lanjut dengan kendali arsitektur dan kemudahan portabel.',
                'price_trial_30' => 40000.0,
                'price_trial_90' => 95000.0,
                'price_permanent' => 135000.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 80000.0,
                'benefit_max_homes' => 4,
                'benefit_max_auctions' => 7,
                'benefit_max_enchants' => 6,
                'benefit_rtp_cooldown' => 120, // 2m
                'benefit_shop_sell_bonus' => 5.0,
                'benefit_xp_bonus' => 8.0,
                'benefit_bank_multiplier' => 1.2,
                'benefit_feed_cooldown' => null,
                'benefit_commands' => ['/craft', '/enderchest'],
                'benefit_kits' => ['Archon Kit'],
                'benefit_nick_permission' => 'none',
                'benefit_battlepass_unlock' => null,
                'benefit_battlepass_discount' => 0.0,
            ],
            'sovereign' => [
                'rank_key' => 'sovereign',
                'display_name' => 'Sovereign',
                'badge' => '⚜ SOVEREIGN',
                'prefix' => '[⚜ SOVEREIGN] ',
                'color' => '#f1c40f',
                'tier' => 'Tier II',
                'weight' => 50,
                'order_index' => 4,
                'is_active' => true,
                'is_buyable' => true,
                'banner_image' => 'assets/themes/apexsions/img/package-sovereign.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-sovereign.jpg',
                'description' => 'Kekuasaan ningrat dan kepemimpinan kerajaan penuh wibawa dengan kuasa menempa dan identitas nama.',
                'price_trial_30' => 75000.0,
                'price_trial_90' => 180000.0,
                'price_permanent' => 250000.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 120000.0,
                'benefit_max_homes' => 5,
                'benefit_max_auctions' => 10,
                'benefit_max_enchants' => 8,
                'benefit_rtp_cooldown' => 95, // 1m 35s
                'benefit_shop_sell_bonus' => 8.0,
                'benefit_xp_bonus' => 10.0,
                'benefit_bank_multiplier' => 1.5,
                'benefit_feed_cooldown' => null,
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/enderchest'],
                'benefit_kits' => ['Sovereign Kit'],
                'benefit_nick_permission' => 'no_color', // /nick tanpa edit warna
                'benefit_battlepass_unlock' => null,
                'benefit_battlepass_discount' => 0.0,
            ],
            'emperor' => [
                'rank_key' => 'emperor',
                'display_name' => 'Emperor',
                'badge' => '⚔ EMPEROR',
                'prefix' => '[⚔ EMPEROR] ',
                'color' => '#e52d27',
                'tier' => 'Tier II',
                'weight' => 60,
                'order_index' => 5,
                'is_active' => true,
                'is_buyable' => true,
                'banner_image' => 'assets/themes/apexsions/img/package-emperor.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-emperor.jpg',
                'description' => 'Kaisar terhormat dengan supremasi tak terbantahkan, hak reparasi peradaban, dan akses langsung Sio Pass.',
                'price_trial_30' => 135000.0,
                'price_trial_90' => 320000.0,
                'price_permanent' => 450000.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 180000.0,
                'benefit_max_homes' => 7,
                'benefit_max_auctions' => 14,
                'benefit_max_enchants' => 11,
                'benefit_rtp_cooldown' => 70, // 1m 10s
                'benefit_shop_sell_bonus' => 12.0,
                'benefit_xp_bonus' => 14.0,
                'benefit_bank_multiplier' => 2.0,
                'benefit_feed_cooldown' => 300, // 5m
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/repair', '/feed', '/hat', '/enderchest'],
                'benefit_kits' => ['Emperor Kit'],
                'benefit_nick_permission' => 'solid_color', // Warna solid, tanpa gradient
                'benefit_battlepass_unlock' => 'sio',
                'benefit_battlepass_discount' => 10.0,
            ],
            'sions' => [
                'rank_key' => 'sions',
                'display_name' => 'Sions',
                'badge' => '✦ SIONS ✦',
                'prefix' => '[✦ SIONS] ',
                'color' => '#00FFFF',
                'tier' => 'Tier II',
                'weight' => 70,
                'order_index' => 6,
                'is_active' => true,
                'is_buyable' => true,
                'banner_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'description' => 'Puncak kedaulatan peradaban Apexsions. Privilese mutlak, RTP kilat, seluruh warna nama, serta seluruh season pass terbuka.',
                'price_trial_30' => 225000.0,
                'price_trial_90' => 550000.0,
                'price_permanent' => 800000.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 300000.0,
                'benefit_max_homes' => 10,
                'benefit_max_auctions' => 20,
                'benefit_max_enchants' => 15,
                'benefit_rtp_cooldown' => 50, // 50s
                'benefit_shop_sell_bonus' => 17.0,
                'benefit_xp_bonus' => 20.0,
                'benefit_bank_multiplier' => 3.0,
                'benefit_feed_cooldown' => 180, // 3m
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/repair', '/feed', '/hat', '/enderchest'],
                'benefit_kits' => ['Sions Kit'],
                'benefit_nick_permission' => 'all_colors', // Semua warna & gradient
                'benefit_battlepass_unlock' => 'exsio', // Membuka Exsio dan Sio Pass
                'benefit_battlepass_discount' => 15.0,
            ],
            'herald' => [
                'rank_key' => 'herald',
                'display_name' => 'Herald',
                'badge' => '📜 HERALD',
                'prefix' => '[📜 HERALD] ',
                'color' => '#ff5858',
                'tier' => 'Tier III',
                'weight' => 80,
                'order_index' => 7,
                'is_active' => true,
                'is_buyable' => false,
                'banner_image' => 'assets/themes/apexsions/img/package-sovereign.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-sovereign.jpg',
                'description' => 'Pemberita kerajaan, fasilitator komunitas, dan penegak moderasi lapangan.',
                'price_trial_30' => 0.0,
                'price_trial_90' => 0.0,
                'price_permanent' => 0.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 0.0,
                'benefit_max_homes' => 999, // Uncapped operator authority
                'benefit_max_auctions' => 999, // Uncapped operator authority
                'benefit_max_enchants' => 15,
                'benefit_rtp_cooldown' => 0, // Bypass cooldown
                'benefit_shop_sell_bonus' => 25.0,
                'benefit_xp_bonus' => 30.0,
                'benefit_bank_multiplier' => 3.0,
                'benefit_feed_cooldown' => 0,
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/repair', '/feed', '/hat', '/enderchest'],
                'benefit_kits' => ['Herald Kit', 'Sovereign Kit'],
                'benefit_nick_permission' => 'solid_color',
                'benefit_battlepass_unlock' => 'sio',
                'benefit_battlepass_discount' => 25.0,
            ],
            'warden' => [
                'rank_key' => 'warden',
                'display_name' => 'Warden',
                'badge' => '🛡 WARDEN',
                'prefix' => '[🛡 WARDEN] ',
                'color' => '#2a5298',
                'tier' => 'Tier III',
                'weight' => 90,
                'order_index' => 8,
                'is_active' => true,
                'is_buyable' => false,
                'banner_image' => 'assets/themes/apexsions/img/package-emperor.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-emperor.jpg',
                'description' => 'Komandan staf dan penegak keamanan sentral realm Apexsions.',
                'price_trial_30' => 0.0,
                'price_trial_90' => 0.0,
                'price_permanent' => 0.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 0.0,
                'benefit_max_homes' => 999, // Uncapped operator authority
                'benefit_max_auctions' => 999, // Uncapped operator authority
                'benefit_max_enchants' => 15,
                'benefit_rtp_cooldown' => 0, // Bypass cooldown
                'benefit_shop_sell_bonus' => 25.0,
                'benefit_xp_bonus' => 30.0,
                'benefit_bank_multiplier' => 3.0,
                'benefit_feed_cooldown' => 0,
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/repair', '/feed', '/hat', '/enderchest', '/vanish'],
                'benefit_kits' => ['Warden Kit', 'Emperor Kit'],
                'benefit_nick_permission' => 'all_colors',
                'benefit_battlepass_unlock' => 'sio',
                'benefit_battlepass_discount' => 50.0,
            ],
            'overseer' => [
                'rank_key' => 'overseer',
                'display_name' => 'Overseer',
                'badge' => '👁 OVERSEER',
                'prefix' => '[👁 OVERSEER] ',
                'color' => '#FFD700',
                'tier' => 'Tier IV',
                'weight' => 95,
                'order_index' => 9,
                'is_active' => true,
                'is_buyable' => false,
                'banner_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'description' => 'Pengawas integritas, keadilan aturan, dan stabilitas makro peradaban.',
                'price_trial_30' => 0.0,
                'price_trial_90' => 0.0,
                'price_permanent' => 0.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 0.0,
                'benefit_max_homes' => 999, // Uncapped operator authority
                'benefit_max_auctions' => 999, // Uncapped operator authority
                'benefit_max_enchants' => 15,
                'benefit_rtp_cooldown' => 0, // Bypass cooldown
                'benefit_shop_sell_bonus' => 25.0,
                'benefit_xp_bonus' => 30.0,
                'benefit_bank_multiplier' => 3.0,
                'benefit_feed_cooldown' => 0,
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/repair', '/feed', '/hat', '/enderchest', '/fly', '/vanish'],
                'benefit_kits' => ['Overseer Kit', 'Sions Kit'],
                'benefit_nick_permission' => 'all_colors',
                'benefit_battlepass_unlock' => 'exsio',
                'benefit_battlepass_discount' => 100.0,
            ],
            'architect' => [
                'rank_key' => 'architect',
                'display_name' => 'Architect',
                'badge' => '📐 ARCHITECT',
                'prefix' => '[📐 ARCHITECT] ',
                'color' => '#8E2DE2',
                'tier' => 'Tier IV',
                'weight' => 95,
                'order_index' => 10,
                'is_active' => true,
                'is_buyable' => false,
                'banner_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'description' => 'Arsitek dan pembangun tatanan peradaban realm dengan wewenang sistem penuh.',
                'price_trial_30' => 0.0,
                'price_trial_90' => 0.0,
                'price_permanent' => 0.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 0.0,
                'benefit_max_homes' => 999, // Uncapped operator authority
                'benefit_max_auctions' => 999, // Uncapped operator authority
                'benefit_max_enchants' => 15,
                'benefit_rtp_cooldown' => 0, // Bypass cooldown
                'benefit_shop_sell_bonus' => 25.0,
                'benefit_xp_bonus' => 30.0,
                'benefit_bank_multiplier' => 3.0,
                'benefit_feed_cooldown' => 0,
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/repair', '/feed', '/hat', '/enderchest', '/fly', '/vanish'],
                'benefit_kits' => ['Architect Kit', 'Sions Kit'],
                'benefit_nick_permission' => 'all_colors',
                'benefit_battlepass_unlock' => 'exsio',
                'benefit_battlepass_discount' => 100.0,
            ],
            'ancestor' => [
                'rank_key' => 'ancestor',
                'display_name' => 'The Ancestor',
                'badge' => '👑 ANCESTOR',
                'prefix' => '[👑 ANCESTOR] ',
                'color' => '#8B0000',
                'tier' => 'Tier V',
                'weight' => 100,
                'order_index' => 11,
                'is_active' => true,
                'is_buyable' => false,
                'banner_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'card_image' => 'assets/themes/apexsions/img/package-sions.jpg',
                'description' => 'Gelar kehormatan tertinggi dan otoritas pendiri realm Apexsions. Terkunci khusus untuk Founder/Owner.',
                'price_trial_30' => 0.0,
                'price_trial_90' => 0.0,
                'price_permanent' => 0.0,
                'price_upgrade_override' => null,
                'discount_percent' => 0.0,
                'money_reward_permanent' => 0.0,
                'benefit_max_homes' => 999, // Uncapped operator authority
                'benefit_max_auctions' => 999, // Uncapped operator authority
                'benefit_max_enchants' => 15,
                'benefit_rtp_cooldown' => 0, // Bypass cooldown
                'benefit_shop_sell_bonus' => 25.0,
                'benefit_xp_bonus' => 30.0,
                'benefit_bank_multiplier' => 3.5,
                'benefit_feed_cooldown' => 0,
                'benefit_commands' => ['/craft', '/anvil', '/smithing', '/repair', '/feed', '/hat', '/enderchest', '/fly', '/god', '/heal', '/vanish'],
                'benefit_kits' => ['Ancestor Kit', 'Sions Kit', 'Emperor Kit', 'Sovereign Kit', 'Archon Kit', 'Ascendant Kit'],
                'benefit_nick_permission' => 'all_colors',
                'benefit_battlepass_unlock' => 'exsio',
                'benefit_battlepass_discount' => 100.0,
            ],
        ];
    }

    /**
     * Get default configuration array for a specific rank key.
     */
    public static function getDefaultsForRank(string $rankKey): ?array
    {
        $normalized = strtolower(trim($rankKey));
        $matrix = self::getDefaultRanksMatrix();
        return $matrix[$normalized] ?? null;
    }

    /**
     * Seed or populate default rank configurations for all 11 ranks.
     */
    public static function seedDefaultsIfEmpty(bool $forceAll = false): void
    {
        $defaults = self::getDefaultRanksMatrix();

        foreach ($defaults as $data) {
            $existing = self::where('rank_key', $data['rank_key'])->first();
            if (!$existing) {
                self::create($data);
            } elseif ($forceAll) {
                $existing->update($data);
            } else {
                $toUpdate = [];
                foreach ($data as $field => $val) {
                    if (is_null($existing->{$field})) {
                        $toUpdate[$field] = $val;
                    }
                }
                if (!empty($toUpdate)) {
                    $existing->update($toUpdate);
                }
            }
        }
    }
}
