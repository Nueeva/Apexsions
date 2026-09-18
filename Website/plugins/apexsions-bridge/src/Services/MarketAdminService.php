<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\Setting;
use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Carbon\Carbon;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;

class MarketAdminService
{
    public const CACHE_KEY_CONFIG = 'apexsions.market.config';
    public const CACHE_KEY_ITEMS = 'apexsions.market.items';

    /**
     * Default kingdom modifiers, tax percentages, and dynamic market parameters.
     */
    public static function getDefaultSettings(): array
    {
        return [
            'kingdoms' => [
                'SOLTERRA' => [
                    'name' => 'Solterra',
                    'tax_percent' => 20.0,
                    'ores_buy_multiplier' => 1.15,
                    'ores_sell_ratio' => 0.30,
                    'ores_stability' => true,
                    'food_buy_multiplier' => 0.90,
                    'farming_buy_multiplier' => 0.90,
                    'blocks_buy_multiplier' => 1.05,
                    'dyes_buy_multiplier' => 1.00,
                    'mob_drops_buy_multiplier' => 1.00,
                ],
                'ZENITHAR' => [
                    'name' => 'Zenithar',
                    'tax_percent' => 18.0,
                    'volatility_multiplier' => 1.05,
                    'blocks_buy_multiplier' => 0.85,
                    'dyes_buy_multiplier' => 0.90,
                    'ores_buy_multiplier' => 1.05,
                    'ores_sell_multiplier' => 1.00,
                    'food_buy_multiplier' => 1.05,
                    'food_sell_multiplier' => 1.00,
                    'farming_buy_multiplier' => 1.05,
                    'farming_sell_multiplier' => 1.00,
                    'mob_drops_buy_multiplier' => 1.05,
                    'mob_drops_sell_multiplier' => 1.00,
                ],
                'SYLVAMOOR' => [
                    'name' => 'Sylvamoor',
                    'tax_percent' => 15.0,
                    'dyes_buy_multiplier' => 0.85,
                    'farming_buy_multiplier' => 0.90,
                    'food_buy_multiplier' => 0.95,
                    'ores_buy_multiplier' => 1.10,
                    'mob_drops_buy_multiplier' => 0.90,
                    'blocks_buy_multiplier' => 1.00,
                ],
            ],
            'weather' => [
                'enabled' => true,
                'clear' => [
                    'farming_sell_multiplier' => 1.10,
                    'mob_sell_multiplier' => 1.00,
                ],
                'rain' => [
                    'farming_sell_multiplier' => 0.98,
                    'mob_sell_multiplier' => 1.05,
                ],
                'thunder' => [
                    'farming_sell_multiplier' => 0.95,
                    'mob_sell_multiplier' => 1.15,
                ],
            ],
            'supply_market' => [
                'enabled' => true,
                'sensitivity' => 0.025,
                'base_volume_threshold' => 2304,
                'max_saturation_drop' => 0.10,
                'min_sell_multiplier' => 0.90,
                'recovery_interval_minutes' => 10,
                'recovery_percent_per_interval' => 25.0,
            ],
            'clamping' => [
                'min_buy_ratio' => 0.85,
                'max_buy_ratio' => 1.20,
                'min_sell_ratio' => 0.85,
                'max_sell_ratio' => 1.20,
            ],
            'economy' => [
                'currency_name' => 'Rupiah',
                'currency_symbol' => 'Rp. ',
                'default_sell_ratio' => 0.20,
            ],
        ];
    }

    /**
     * Default shop items catalog across all 6 official categories.
     */
    public static function getDefaultItems(): array
    {
        return [
            // 1. ORES & MINERALS
            'coal' => ['category' => 'ores', 'material' => 'COAL', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Coal'],
            'raw_copper' => ['category' => 'ores', 'material' => 'RAW_COPPER', 'buy_price' => 20.0, 'sell_price' => 4.0, 'buy_enabled' => true, 'display_name' => 'Raw Copper'],
            'copper_ingot' => ['category' => 'ores', 'material' => 'COPPER_INGOT', 'buy_price' => 30.0, 'sell_price' => 6.0, 'buy_enabled' => true, 'display_name' => 'Copper Ingot'],
            'raw_iron' => ['category' => 'ores', 'material' => 'RAW_IRON', 'buy_price' => 50.0, 'sell_price' => 10.0, 'buy_enabled' => true, 'display_name' => 'Raw Iron'],
            'iron_ingot' => ['category' => 'ores', 'material' => 'IRON_INGOT', 'buy_price' => 75.0, 'sell_price' => 15.0, 'buy_enabled' => true, 'display_name' => 'Iron Ingot'],
            'raw_gold' => ['category' => 'ores', 'material' => 'RAW_GOLD', 'buy_price' => 80.0, 'sell_price' => 16.0, 'buy_enabled' => true, 'display_name' => 'Raw Gold'],
            'gold_ingot' => ['category' => 'ores', 'material' => 'GOLD_INGOT', 'buy_price' => 120.0, 'sell_price' => 24.0, 'buy_enabled' => true, 'display_name' => 'Gold Ingot'],
            'redstone' => ['category' => 'ores', 'material' => 'REDSTONE', 'buy_price' => 25.0, 'sell_price' => 5.0, 'buy_enabled' => true, 'display_name' => 'Redstone Dust'],
            'lapis_lazuli' => ['category' => 'ores', 'material' => 'LAPIS_LAZULI', 'buy_price' => 30.0, 'sell_price' => 6.0, 'buy_enabled' => true, 'display_name' => 'Lapis Lazuli'],
            'amethyst_shard' => ['category' => 'ores', 'material' => 'AMETHYST_SHARD', 'buy_price' => 35.0, 'sell_price' => 7.0, 'buy_enabled' => true, 'display_name' => 'Amethyst Shard'],
            'nether_quartz' => ['category' => 'ores', 'material' => 'QUARTZ', 'buy_price' => 40.0, 'sell_price' => 8.0, 'buy_enabled' => true, 'display_name' => 'Nether Quartz'],
            'diamond' => ['category' => 'ores', 'material' => 'DIAMOND', 'buy_price' => 500.0, 'sell_price' => 100.0, 'buy_enabled' => true, 'display_name' => 'Diamond'],
            'emerald' => ['category' => 'ores', 'material' => 'EMERALD', 'buy_price' => 350.0, 'sell_price' => 70.0, 'buy_enabled' => true, 'display_name' => 'Emerald'],
            'ancient_debris' => ['category' => 'ores', 'material' => 'ANCIENT_DEBRIS', 'buy_price' => 2500.0, 'sell_price' => 500.0, 'buy_enabled' => true, 'display_name' => 'Ancient Debris'],
            'netherite_ingot' => ['category' => 'ores', 'material' => 'NETHERITE_INGOT', 'buy_price' => 10000.0, 'sell_price' => 2000.0, 'buy_enabled' => true, 'display_name' => 'Netherite Ingot'],

            // 2. FARMING & AGRICULTURE
            'wheat' => ['category' => 'farming', 'material' => 'WHEAT', 'buy_price' => 12.0, 'sell_price' => 2.5, 'buy_enabled' => true, 'display_name' => 'Wheat'],
            'carrot' => ['category' => 'farming', 'material' => 'CARROT', 'buy_price' => 10.0, 'sell_price' => 2.0, 'buy_enabled' => true, 'display_name' => 'Carrot'],
            'potato' => ['category' => 'farming', 'material' => 'POTATO', 'buy_price' => 10.0, 'sell_price' => 2.0, 'buy_enabled' => true, 'display_name' => 'Potato'],
            'beetroot' => ['category' => 'farming', 'material' => 'BEETROOT', 'buy_price' => 10.0, 'sell_price' => 2.0, 'buy_enabled' => true, 'display_name' => 'Beetroot'],
            'melon_slice' => ['category' => 'farming', 'material' => 'MELON_SLICE', 'buy_price' => 5.0, 'sell_price' => 1.0, 'buy_enabled' => true, 'display_name' => 'Melon Slice'],
            'pumpkin' => ['category' => 'farming', 'material' => 'PUMPKIN', 'buy_price' => 20.0, 'sell_price' => 4.0, 'buy_enabled' => true, 'display_name' => 'Pumpkin'],
            'sugar_cane' => ['category' => 'farming', 'material' => 'SUGAR_CANE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Sugar Cane'],
            'bamboo' => ['category' => 'farming', 'material' => 'BAMBOO', 'buy_price' => 8.0, 'sell_price' => 1.5, 'buy_enabled' => true, 'display_name' => 'Bamboo'],
            'cactus' => ['category' => 'farming', 'material' => 'CACTUS', 'buy_price' => 12.0, 'sell_price' => 2.5, 'buy_enabled' => true, 'display_name' => 'Cactus'],
            'kelp' => ['category' => 'farming', 'material' => 'KELP', 'buy_price' => 6.0, 'sell_price' => 1.2, 'buy_enabled' => true, 'display_name' => 'Kelp'],
            'cocoa_beans' => ['category' => 'farming', 'material' => 'COCOA_BEANS', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'Cocoa Beans'],
            'sweet_berries' => ['category' => 'farming', 'material' => 'SWEET_BERRIES', 'buy_price' => 8.0, 'sell_price' => 1.6, 'buy_enabled' => true, 'display_name' => 'Sweet Berries'],
            'glow_berries' => ['category' => 'farming', 'material' => 'GLOW_BERRIES', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Glow Berries'],
            'nether_wart' => ['category' => 'farming', 'material' => 'NETHER_WART', 'buy_price' => 25.0, 'sell_price' => 5.0, 'buy_enabled' => true, 'display_name' => 'Nether Wart'],

            // 3. FOOD
            'bread' => ['category' => 'food', 'material' => 'BREAD', 'buy_price' => 20.0, 'sell_price' => 4.0, 'buy_enabled' => true, 'display_name' => 'Bread'],
            'baked_potato' => ['category' => 'food', 'material' => 'BAKED_POTATO', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'Baked Potato'],
            'cooked_beef' => ['category' => 'food', 'material' => 'COOKED_BEEF', 'buy_price' => 35.0, 'sell_price' => 7.0, 'buy_enabled' => true, 'display_name' => 'Steak'],
            'cooked_porkchop' => ['category' => 'food', 'material' => 'COOKED_PORKCHOP', 'buy_price' => 35.0, 'sell_price' => 7.0, 'buy_enabled' => true, 'display_name' => 'Cooked Porkchop'],
            'cooked_mutton' => ['category' => 'food', 'material' => 'COOKED_MUTTON', 'buy_price' => 30.0, 'sell_price' => 6.0, 'buy_enabled' => true, 'display_name' => 'Cooked Mutton'],
            'cooked_chicken' => ['category' => 'food', 'material' => 'COOKED_CHICKEN', 'buy_price' => 28.0, 'sell_price' => 5.5, 'buy_enabled' => true, 'display_name' => 'Cooked Chicken'],
            'cooked_cod' => ['category' => 'food', 'material' => 'COOKED_COD', 'buy_price' => 25.0, 'sell_price' => 5.0, 'buy_enabled' => true, 'display_name' => 'Cooked Cod'],
            'cooked_salmon' => ['category' => 'food', 'material' => 'COOKED_SALMON', 'buy_price' => 30.0, 'sell_price' => 6.0, 'buy_enabled' => true, 'display_name' => 'Cooked Salmon'],
            'golden_carrot' => ['category' => 'food', 'material' => 'GOLDEN_CARROT', 'buy_price' => 120.0, 'sell_price' => 24.0, 'buy_enabled' => true, 'display_name' => 'Golden Carrot'],
            'golden_apple' => ['category' => 'food', 'material' => 'GOLDEN_APPLE', 'buy_price' => 500.0, 'sell_price' => 100.0, 'buy_enabled' => true, 'display_name' => 'Golden Apple'],
            'enchanted_golden_apple' => ['category' => 'food', 'material' => 'ENCHANTED_GOLDEN_APPLE', 'buy_price' => 15000.0, 'sell_price' => 3000.0, 'buy_enabled' => true, 'display_name' => 'Enchanted Golden Apple'],
            'cake' => ['category' => 'food', 'material' => 'CAKE', 'buy_price' => 150.0, 'sell_price' => 30.0, 'buy_enabled' => true, 'display_name' => 'Cake'],
            'pumpkin_pie' => ['category' => 'food', 'material' => 'PUMPKIN_PIE', 'buy_price' => 60.0, 'sell_price' => 12.0, 'buy_enabled' => true, 'display_name' => 'Pumpkin Pie'],

            // 4. MOB DROPS
            'rotten_flesh' => ['category' => 'mob_drops', 'material' => 'ROTTEN_FLESH', 'buy_price' => 8.0, 'sell_price' => 1.5, 'buy_enabled' => true, 'display_name' => 'Rotten Flesh'],
            'bone' => ['category' => 'mob_drops', 'material' => 'BONE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Bone'],
            'string' => ['category' => 'mob_drops', 'material' => 'STRING', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'String'],
            'spider_eye' => ['category' => 'mob_drops', 'material' => 'SPIDER_EYE', 'buy_price' => 20.0, 'sell_price' => 4.0, 'buy_enabled' => true, 'display_name' => 'Spider Eye'],
            'gunpowder' => ['category' => 'mob_drops', 'material' => 'GUNPOWDER', 'buy_price' => 45.0, 'sell_price' => 9.0, 'buy_enabled' => true, 'display_name' => 'Gunpowder'],
            'ender_pearl' => ['category' => 'mob_drops', 'material' => 'ENDER_PEARL', 'buy_price' => 100.0, 'sell_price' => 20.0, 'buy_enabled' => true, 'display_name' => 'Ender Pearl'],
            'blaze_rod' => ['category' => 'mob_drops', 'material' => 'BLAZE_ROD', 'buy_price' => 120.0, 'sell_price' => 25.0, 'buy_enabled' => true, 'display_name' => 'Blaze Rod'],
            'ghast_tear' => ['category' => 'mob_drops', 'material' => 'GHAST_TEAR', 'buy_price' => 250.0, 'sell_price' => 50.0, 'buy_enabled' => true, 'display_name' => 'Ghast Tear'],
            'magma_cream' => ['category' => 'mob_drops', 'material' => 'MAGMA_CREAM', 'buy_price' => 60.0, 'sell_price' => 12.0, 'buy_enabled' => true, 'display_name' => 'Magma Cream'],
            'slime_ball' => ['category' => 'mob_drops', 'material' => 'SLIME_BALL', 'buy_price' => 50.0, 'sell_price' => 10.0, 'buy_enabled' => true, 'display_name' => 'Slimeball'],
            'phantom_membrane' => ['category' => 'mob_drops', 'material' => 'PHANTOM_MEMBRANE', 'buy_price' => 80.0, 'sell_price' => 16.0, 'buy_enabled' => true, 'display_name' => 'Phantom Membrane'],
            'shulker_shell' => ['category' => 'mob_drops', 'material' => 'SHULKER_SHELL', 'buy_price' => 1200.0, 'sell_price' => 250.0, 'buy_enabled' => true, 'display_name' => 'Shulker Shell'],
            'wither_skeleton_skull' => ['category' => 'mob_drops', 'material' => 'WITHER_SKELETON_SKULL', 'buy_price' => 3500.0, 'sell_price' => 700.0, 'buy_enabled' => true, 'display_name' => 'Wither Skeleton Skull'],
            'nether_star' => ['category' => 'mob_drops', 'material' => 'NETHER_STAR', 'buy_price' => 18000.0, 'sell_price' => 3600.0, 'buy_enabled' => true, 'display_name' => 'Nether Star'],

            // 5. BLOCKS & WOOD
            'stone' => ['category' => 'blocks', 'material' => 'STONE', 'buy_price' => 8.0, 'sell_price' => 1.5, 'buy_enabled' => true, 'display_name' => 'Stone'],
            'cobblestone' => ['category' => 'blocks', 'material' => 'COBBLESTONE', 'buy_price' => 5.0, 'sell_price' => 1.0, 'buy_enabled' => true, 'display_name' => 'Cobblestone'],
            'deepslate' => ['category' => 'blocks', 'material' => 'DEEPSLATE', 'buy_price' => 10.0, 'sell_price' => 2.0, 'buy_enabled' => true, 'display_name' => 'Deepslate'],
            'cobbled_deepslate' => ['category' => 'blocks', 'material' => 'COBBLED_DEEPSLATE', 'buy_price' => 7.0, 'sell_price' => 1.4, 'buy_enabled' => true, 'display_name' => 'Cobbled Deepslate'],
            'oak_log' => ['category' => 'blocks', 'material' => 'OAK_LOG', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Oak Log'],
            'spruce_log' => ['category' => 'blocks', 'material' => 'SPRUCE_LOG', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Spruce Log'],
            'birch_log' => ['category' => 'blocks', 'material' => 'BIRCH_LOG', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Birch Log'],
            'jungle_log' => ['category' => 'blocks', 'material' => 'JUNGLE_LOG', 'buy_price' => 16.0, 'sell_price' => 3.2, 'buy_enabled' => true, 'display_name' => 'Jungle Log'],
            'acacia_log' => ['category' => 'blocks', 'material' => 'ACACIA_LOG', 'buy_price' => 16.0, 'sell_price' => 3.2, 'buy_enabled' => true, 'display_name' => 'Acacia Log'],
            'dark_oak_log' => ['category' => 'blocks', 'material' => 'DARK_OAK_LOG', 'buy_price' => 16.0, 'sell_price' => 3.2, 'buy_enabled' => true, 'display_name' => 'Dark Oak Log'],
            'mangrove_log' => ['category' => 'blocks', 'material' => 'MANGROVE_LOG', 'buy_price' => 18.0, 'sell_price' => 3.6, 'buy_enabled' => true, 'display_name' => 'Mangrove Log'],
            'cherry_log' => ['category' => 'blocks', 'material' => 'CHERRY_LOG', 'buy_price' => 20.0, 'sell_price' => 4.0, 'buy_enabled' => true, 'display_name' => 'Cherry Log'],
            'glass' => ['category' => 'blocks', 'material' => 'GLASS', 'buy_price' => 12.0, 'sell_price' => 2.4, 'buy_enabled' => true, 'display_name' => 'Glass'],
            'obsidian' => ['category' => 'blocks', 'material' => 'OBSIDIAN', 'buy_price' => 150.0, 'sell_price' => 30.0, 'buy_enabled' => true, 'display_name' => 'Obsidian'],
            'crying_obsidian' => ['category' => 'blocks', 'material' => 'CRYING_OBSIDIAN', 'buy_price' => 250.0, 'sell_price' => 50.0, 'buy_enabled' => true, 'display_name' => 'Crying Obsidian'],
            'sea_lantern' => ['category' => 'blocks', 'material' => 'SEA_LANTERN', 'buy_price' => 180.0, 'sell_price' => 36.0, 'buy_enabled' => true, 'display_name' => 'Sea Lantern'],
            'glowstone' => ['category' => 'blocks', 'material' => 'GLOWSTONE', 'buy_price' => 100.0, 'sell_price' => 20.0, 'buy_enabled' => true, 'display_name' => 'Glowstone'],

            // 6. DYES & COLORANTS
            'white_dye' => ['category' => 'dyes', 'material' => 'WHITE_DYE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'White Dye'],
            'red_dye' => ['category' => 'dyes', 'material' => 'RED_DYE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Red Dye'],
            'blue_dye' => ['category' => 'dyes', 'material' => 'BLUE_DYE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Blue Dye'],
            'yellow_dye' => ['category' => 'dyes', 'material' => 'YELLOW_DYE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Yellow Dye'],
            'green_dye' => ['category' => 'dyes', 'material' => 'GREEN_DYE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Green Dye'],
            'black_dye' => ['category' => 'dyes', 'material' => 'BLACK_DYE', 'buy_price' => 15.0, 'sell_price' => 3.0, 'buy_enabled' => true, 'display_name' => 'Black Dye'],
            'purple_dye' => ['category' => 'dyes', 'material' => 'PURPLE_DYE', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'Purple Dye'],
            'orange_dye' => ['category' => 'dyes', 'material' => 'ORANGE_DYE', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'Orange Dye'],
            'cyan_dye' => ['category' => 'dyes', 'material' => 'CYAN_DYE', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'Cyan Dye'],
            'pink_dye' => ['category' => 'dyes', 'material' => 'PINK_DYE', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'Pink Dye'],
            'lime_dye' => ['category' => 'dyes', 'material' => 'LIME_DYE', 'buy_price' => 18.0, 'sell_price' => 3.5, 'buy_enabled' => true, 'display_name' => 'Lime Dye'],
        ];
    }

    /**
     * Retrieve all saved market settings, with fallback to defaults.
     */
    public static function getSettings(): array
    {
        $raw = setting('apexsions.market.settings');
        if (empty($raw)) {
            return self::getDefaultSettings();
        }

        $decoded = is_array($raw) ? $raw : json_decode($raw, true);
        if (!is_array($decoded)) {
            return self::getDefaultSettings();
        }

        return array_replace_recursive(self::getDefaultSettings(), $decoded);
    }

    /**
     * Retrieve all shop items, with fallback to defaults.
     */
    public static function getItems(): array
    {
        $raw = setting('apexsions.market.items');
        if (empty($raw)) {
            return self::getDefaultItems();
        }

        $decoded = is_array($raw) ? $raw : json_decode($raw, true);
        if (!is_array($decoded)) {
            return self::getDefaultItems();
        }

        return array_replace(self::getDefaultItems(), $decoded);
    }

    /**
     * Get items grouped by category.
     */
    public static function getItemsByCategory(): array
    {
        $items = self::getItems();
        $grouped = [
            'ores' => [],
            'farming' => [],
            'food' => [],
            'mob_drops' => [],
            'blocks' => [],
            'dyes' => [],
        ];

        foreach ($items as $key => $data) {
            $cat = $data['category'] ?? 'ores';
            if (!isset($grouped[$cat])) {
                $grouped[$cat] = [];
            }
            $data['id'] = $key;
            $data['sell_ratio'] = $data['buy_price'] > 0 ? round(($data['sell_price'] / $data['buy_price']) * 100, 1) : 0;
            $grouped[$cat][$key] = $data;
        }

        return $grouped;
    }

    /**
     * Save kingdom settings and trigger audit log.
     */
    public static function updateKingdoms(array $kingdomData, ?User $actor = null): array
    {
        $settings = self::getSettings();
        $oldKingdoms = $settings['kingdoms'];

        foreach ($kingdomData as $kKey => $fields) {
            $upper = strtoupper($kKey);
            if (isset($settings['kingdoms'][$upper])) {
                foreach ($fields as $fKey => $val) {
                    $settings['kingdoms'][$upper][$fKey] = is_numeric($val) ? (float) $val : (bool) $val;
                }
            }
        }

        Setting::updateSettings(['apexsions.market.settings' => json_encode($settings)]);
        Cache::forget(self::CACHE_KEY_CONFIG);

        AuditService::log([
            'action' => 'MARKET_KINGDOMS_UPDATE',
            'actor_type' => $actor ? 'USER' : 'SYSTEM',
            'actor_id' => $actor ? (string) $actor->id : null,
            'actor_name' => $actor ? $actor->name : 'Web Administrator',
            'target_type' => 'MARKET',
            'target_id' => 'KINGDOM_MODIFIERS',
            'target_name' => 'Kingdom Market Parameters',
            'old_value' => $oldKingdoms,
            'new_value' => $settings['kingdoms'],
            'reason' => 'Pembaruan parameter pasar & pajak tiga kerajaan dari Web Admin',
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);

        self::dispatchServerReload('Perubahan parameter pasar 3 kerajaan diterapkan dari Web Admin.');

        return ['success' => true, 'message' => 'Parameter pasar kerajaan berhasil diperbarui dan disinkronkan ke server.'];
    }

    /**
     * Save dynamic market & weather settings.
     */
    public static function updateDynamics(array $data, ?User $actor = null): array
    {
        $settings = self::getSettings();
        $oldSettings = [
            'weather' => $settings['weather'],
            'supply_market' => $settings['supply_market'],
            'clamping' => $settings['clamping'],
        ];

        if (isset($data['weather'])) {
            $settings['weather'] = array_replace_recursive($settings['weather'], $data['weather']);
        }
        if (isset($data['supply_market'])) {
            $settings['supply_market'] = array_replace_recursive($settings['supply_market'], $data['supply_market']);
        }
        if (isset($data['clamping'])) {
            $settings['clamping'] = array_replace_recursive($settings['clamping'], $data['clamping']);
        }

        Setting::updateSettings(['apexsions.market.settings' => json_encode($settings)]);
        Cache::forget(self::CACHE_KEY_CONFIG);

        AuditService::log([
            'action' => 'MARKET_DYNAMICS_UPDATE',
            'actor_type' => $actor ? 'USER' : 'SYSTEM',
            'actor_id' => $actor ? (string) $actor->id : null,
            'actor_name' => $actor ? $actor->name : 'Web Administrator',
            'target_type' => 'MARKET',
            'target_id' => 'DYNAMIC_PARAMETERS',
            'target_name' => 'Market Clamping & Weather Curves',
            'old_value' => $oldSettings,
            'new_value' => [
                'weather' => $settings['weather'],
                'supply_market' => $settings['supply_market'],
                'clamping' => $settings['clamping'],
            ],
            'reason' => 'Pembaruan kurva pasar dinamis, saturasi pasokan, dan cuaca dari Web Admin',
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);

        self::dispatchServerReload('Pembaruan dinamika pasar & cuaca dari Web Admin.');

        return ['success' => true, 'message' => 'Pengaturan pasar dinamis berhasil disimpan dan disinkronkan.'];
    }

    /**
     * Update a single shop item.
     */
    public static function updateItem(string $itemId, array $data, ?User $actor = null): array
    {
        $items = self::getItems();
        $normId = strtolower(trim($itemId));

        if (!isset($items[$normId])) {
            return ['success' => false, 'message' => "Item '{$itemId}' tidak ditemukan dalam katalog pasar."];
        }

        $oldItem = $items[$normId];
        if (isset($data['buy_price'])) {
            $items[$normId]['buy_price'] = max(0.1, (float) $data['buy_price']);
        }
        if (isset($data['sell_price'])) {
            $items[$normId]['sell_price'] = max(0.05, (float) $data['sell_price']);
        }
        if (isset($data['buy_enabled'])) {
            $items[$normId]['buy_enabled'] = (bool) $data['buy_enabled'];
        }
        if (isset($data['display_name']) && !empty(trim($data['display_name']))) {
            $items[$normId]['display_name'] = trim($data['display_name']);
        }

        Setting::updateSettings(['apexsions.market.items' => json_encode($items)]);
        Cache::forget(self::CACHE_KEY_ITEMS);

        AuditService::log([
            'action' => 'MARKET_ITEM_UPDATE',
            'actor_type' => $actor ? 'USER' : 'SYSTEM',
            'actor_id' => $actor ? (string) $actor->id : null,
            'actor_name' => $actor ? $actor->name : 'Web Administrator',
            'target_type' => 'ITEM',
            'target_id' => $normId,
            'target_name' => $items[$normId]['display_name'],
            'old_value' => $oldItem,
            'new_value' => $items[$normId],
            'reason' => "Penyesuaian harga item '{$normId}' dari Web Admin",
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);

        self::dispatchServerReload("Penyesuaian harga item '{$normId}'.");

        return ['success' => true, 'message' => "Item '{$items[$normId]['display_name']}' berhasil diperbarui."];
    }

    /**
     * Batch update prices or sell ratios across a category or entire catalog.
     */
    public static function batchUpdateItems(array $options, ?User $actor = null): array
    {
        $items = self::getItems();
        $category = $options['category'] ?? 'all';
        $type = $options['type'] ?? 'ratio'; // 'ratio' or 'percent'
        $value = (float) ($options['value'] ?? 0);

        $affected = 0;
        foreach ($items as $id => &$item) {
            if ($category !== 'all' && ($item['category'] ?? '') !== $category) {
                continue;
            }

            if ($type === 'ratio') {
                // Set sell price as fraction of buy price (e.g. 0.20 = 20%)
                $ratio = max(0.01, min(1.0, $value));
                $item['sell_price'] = round($item['buy_price'] * $ratio, 2);
                $affected++;
            } elseif ($type === 'percent') {
                // Adjust both buy & sell price by percentage (e.g. +10% or -10%)
                $multiplier = 1.0 + ($value / 100.0);
                $multiplier = max(0.1, $multiplier);
                $item['buy_price'] = round($item['buy_price'] * $multiplier, 2);
                $item['sell_price'] = round($item['sell_price'] * $multiplier, 2);
                $affected++;
            }
        }
        unset($item);

        Setting::updateSettings(['apexsions.market.items' => json_encode($items)]);
        Cache::forget(self::CACHE_KEY_ITEMS);

        AuditService::log([
            'action' => 'MARKET_BATCH_UPDATE',
            'actor_type' => $actor ? 'USER' : 'SYSTEM',
            'actor_id' => $actor ? (string) $actor->id : null,
            'actor_name' => $actor ? $actor->name : 'Web Administrator',
            'target_type' => 'MARKET',
            'target_id' => strtoupper($category),
            'target_name' => "Batch Update [{$category}]",
            'old_value' => null,
            'new_value' => ['type' => $type, 'value' => $value, 'affected' => $affected],
            'reason' => "Penyesuaian massal ({$type}: {$value}) untuk kategori '{$category}'",
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);

        self::dispatchServerReload("Penyesuaian massal harga kategori '{$category}'.");

        return [
            'success' => true,
            'message' => "Berhasil memperbarui {$affected} item dalam kategori '{$category}'.",
            'affected' => $affected,
        ];
    }

    /**
     * Reset market settings and item prices to official defaults.
     */
    public static function resetDefaults(?User $actor = null): array
    {
        Setting::updateSettings([
            'apexsions.market.settings' => json_encode(self::getDefaultSettings()),
            'apexsions.market.items' => json_encode(self::getDefaultItems()),
        ]);

        Cache::forget(self::CACHE_KEY_CONFIG);
        Cache::forget(self::CACHE_KEY_ITEMS);

        AuditService::log([
            'action' => 'MARKET_RESET_DEFAULTS',
            'actor_type' => $actor ? 'USER' : 'SYSTEM',
            'actor_id' => $actor ? (string) $actor->id : null,
            'actor_name' => $actor ? $actor->name : 'Web Administrator',
            'target_type' => 'MARKET',
            'target_id' => 'ALL',
            'target_name' => 'All Market Configurations & Catalog',
            'old_value' => null,
            'new_value' => 'DEFAULT_BASELINE',
            'reason' => 'Reset total seluruh parameter pasar dan harga komoditas ke standar dasar seimbang.',
            'source' => 'WEB',
            'status' => 'SUCCESS',
        ]);

        self::dispatchServerReload('Reset total konfigurasi pasar ke baseline seimbang.');

        return ['success' => true, 'message' => 'Konfigurasi pasar dan katalog item berhasil di-reset ke standar seimbang.'];
    }

    /**
     * Compile complete shop configuration for API delivery to game server.
     */
    public static function compileFullConfig(): array
    {
        $settings = self::getSettings();
        $items = self::getItems();
        $grouped = self::getItemsByCategory();

        return [
            'status' => 'success',
            'server_brand' => 'Apexsions',
            'version' => '1.0.0',
            'updated_at' => Carbon::now()->toIso8601String(),
            'economy' => $settings['economy'],
            'kingdoms' => $settings['kingdoms'],
            'weather' => $settings['weather'],
            'supply_market' => $settings['supply_market'],
            'clamping' => $settings['clamping'],
            'categories' => $grouped,
            'items' => $items,
        ];
    }

    /**
     * Dispatch reload delivery to the Minecraft server via WebBridge queue.
     */
    public static function dispatchServerReload(string $reason = ''): void
    {
        try {
            Delivery::create([
                'action_id' => (string) Str::uuid(),
                'idempotency_key' => 'market-reload-' . time(),
                'server_id' => 1,
                'command' => 'shop reload',
                'status' => 'PENDING',
                'error_message' => $reason,
            ]);
        } catch (\Throwable $e) {
            Log::warning('[MarketAdminService] Gagal menyusun antrean delivery reload: ' . $e->getMessage());
        }
    }
}
