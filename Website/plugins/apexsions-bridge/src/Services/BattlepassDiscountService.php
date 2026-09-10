<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\PluginShop\Models\Package;

class BattlepassDiscountService
{
    /**
     * Get discount rate for a specific player and package.
     *
     * @param MinecraftAccount|null $account
     * @param Package|string $package
     * @return array{
     *   has_discount: bool,
     *   discount_percent: int,
     *   eligible_rank: string|null,
     *   is_free_current_season: bool,
     *   original_price: float,
     *   discounted_price: float,
     *   savings: float,
     *   whatsapp_message: string
     * }
     */
    public static function calculateDiscount(?MinecraftAccount $account, $package, float $basePrice): array
    {
        $packageName = is_string($package) ? $package : ($package->name ?? '');
        $isSioPass = (stripos($packageName, 'sio pass') !== false || stripos($packageName, 'battlepass') !== false) && stripos($packageName, 'exsio') === false;
        $isExsioPass = stripos($packageName, 'exsio pass') !== false;

        $defaultResult = [
            'has_discount' => false,
            'discount_percent' => 0,
            'eligible_rank' => null,
            'is_free_current_season' => false,
            'original_price' => $basePrice,
            'discounted_price' => $basePrice,
            'savings' => 0.0,
            'whatsapp_message' => "Min, aku mau beli {$packageName} yang harganya Rp." . number_format($basePrice, 0, ',', '.'),
        ];

        // If not a BattlePass package or no linked account, return default
        if ((!$isSioPass && !$isExsioPass) || !$account) {
            return $defaultResult;
        }

        $rank = strtolower(trim($account->rank ?? 'wanderer'));
        $isPermanent = $account->isPermanentRank();

        // Must be permanent rank to receive battlepass perks
        if (!$isPermanent) {
            return $defaultResult;
        }

        // 1. SIONS PERMANENT: 15% discount for both Sio Pass and Exsio Pass
        if ($rank === 'sions') {
            $discountPercent = 15;
            $discountedPrice = round($basePrice * 0.85);
            $savings = $basePrice - $discountedPrice;
            $formattedPrice = number_format($discountedPrice, 0, ',', '.');

            return [
                'has_discount' => true,
                'discount_percent' => $discountPercent,
                'eligible_rank' => 'Sions',
                'is_free_current_season' => true,
                'original_price' => $basePrice,
                'discounted_price' => $discountedPrice,
                'savings' => $savings,
                'whatsapp_message' => "Min, aku mau beli battlepass {$packageName} yang harganya Rp.{$formattedPrice} karna aku udah beli rank Sions jadi harganya segitu",
            ];
        }

        // 2. EMPEROR PERMANENT: 10% discount for Sio Pass
        if ($rank === 'emperor' && $isSioPass) {
            $discountPercent = 10;
            $discountedPrice = round($basePrice * 0.90);
            $savings = $basePrice - $discountedPrice;
            $formattedPrice = number_format($discountedPrice, 0, ',', '.');

            return [
                'has_discount' => true,
                'discount_percent' => $discountPercent,
                'eligible_rank' => 'Emperor',
                'is_free_current_season' => true,
                'original_price' => $basePrice,
                'discounted_price' => $discountedPrice,
                'savings' => $savings,
                'whatsapp_message' => "Min, aku mau beli battlepass {$packageName} yang harganya Rp.{$formattedPrice} karna aku udah beli rank Emperor jadi harganya segitu",
            ];
        }

        return $defaultResult;
    }
}
