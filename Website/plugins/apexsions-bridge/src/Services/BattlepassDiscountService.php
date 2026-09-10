<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\MinecraftAccount;
use Azuriom\PluginShop\Models\Package;

class BattlepassDiscountService
{
    /**
     * Get the active primary WhatsApp phone number from admin configuration.
     */
    public static function getWhatsAppNumber(): string
    {
        $numbers = setting('apexsions.whatsapp.admin_numbers', '6281212994597,6285883161047,6287729112281');
        $list = array_filter(array_map('trim', explode(',', $numbers)));
        return !empty($list) ? reset($list) : '6281212994597';
    }

    /**
     * Build WhatsApp Click-to-Chat URL for standard product.
     */
    public static function generateStandardWhatsAppUrl(string $productName, float $price): string
    {
        $number = self::getWhatsAppNumber();
        $template = setting('apexsions.whatsapp.template_standard', 'Min, aku mau beli {nama produk} yang harganya Rp.{harga produk}');
        $formattedPrice = number_format($price, 0, ',', '.');

        $message = str_replace(
            ['{nama produk}', '{harga produk}'],
            [$productName, $formattedPrice],
            $template
        );

        return "https://wa.me/{$number}?text=" . rawurlencode($message);
    }

    /**
     * Build WhatsApp Click-to-Chat URL for BattlePass with rank discount.
     */
    public static function generateBattlepassWhatsAppUrl(string $passName, float $price, string $rankName): string
    {
        $number = self::getWhatsAppNumber();
        $template = setting('apexsions.whatsapp.template_battlepass_discount', 'Min, aku mau beli battlepass {nama pass} yang harganya Rp.{harga produk} karna aku udah beli rank {rank} jadi harganya segitu');
        $formattedPrice = number_format($price, 0, ',', '.');

        $message = str_replace(
            ['{nama pass}', '{harga produk}', '{rank}'],
            [$passName, $formattedPrice, ucfirst($rankName)],
            $template
        );

        return "https://wa.me/{$number}?text=" . rawurlencode($message);
    }

    /**
     * Build WhatsApp Click-to-Chat URL for Rank Upgrade.
     */
    public static function generateUpgradeWhatsAppUrl(string $oldRank, string $newRank, float $upgradePrice): string
    {
        $number = self::getWhatsAppNumber();
        $template = setting('apexsions.whatsapp.template_upgrade', 'Min, aku mau upgrade rank dari {rank lama} ke {rank baru} yang harganya Rp.{harga upgrade}');
        $formattedPrice = number_format($upgradePrice, 0, ',', '.');

        $message = str_replace(
            ['{rank lama}', '{rank baru}', '{harga upgrade}'],
            [ucfirst($oldRank), ucfirst($newRank), $formattedPrice],
            $template
        );

        return "https://wa.me/{$number}?text=" . rawurlencode($message);
    }

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
     *   whatsapp_message: string,
     *   whatsapp_url: string
     * }
     */
    public static function calculateDiscount(?MinecraftAccount $account, $package, float $basePrice): array
    {
        $packageName = is_string($package) ? $package : ($package->name ?? '');
        $isSioPass = (stripos($packageName, 'sio pass') !== false || stripos($packageName, 'battlepass') !== false) && stripos($packageName, 'exsio') === false;
        $isExsioPass = stripos($packageName, 'exsio pass') !== false;

        $standardUrl = self::generateStandardWhatsAppUrl($packageName, $basePrice);
        $defaultResult = [
            'has_discount' => false,
            'discount_percent' => 0,
            'eligible_rank' => null,
            'is_free_current_season' => false,
            'original_price' => $basePrice,
            'discounted_price' => $basePrice,
            'savings' => 0.0,
            'whatsapp_message' => "Min, aku mau beli {$packageName} yang harganya Rp." . number_format($basePrice, 0, ',', '.'),
            'whatsapp_url' => $standardUrl,
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

        $sionsDiscount = (int) setting('apexsions.battlepass.sions_discount', 15);
        $emperorDiscount = (int) setting('apexsions.battlepass.emperor_discount', 10);

        // 1. SIONS PERMANENT: 15% discount for both Sio Pass and Exsio Pass
        if ($rank === 'sions') {
            $discountPercent = $sionsDiscount;
            $discountMultiplier = (100 - $discountPercent) / 100.0;
            $discountedPrice = round($basePrice * $discountMultiplier);
            $savings = $basePrice - $discountedPrice;
            $formattedPrice = number_format($discountedPrice, 0, ',', '.');
            $url = self::generateBattlepassWhatsAppUrl($packageName, $discountedPrice, 'Sions');

            return [
                'has_discount' => true,
                'discount_percent' => $discountPercent,
                'eligible_rank' => 'Sions',
                'is_free_current_season' => true,
                'original_price' => $basePrice,
                'discounted_price' => $discountedPrice,
                'savings' => $savings,
                'whatsapp_message' => "Min, aku mau beli battlepass {$packageName} yang harganya Rp.{$formattedPrice} karna aku udah beli rank Sions jadi harganya segitu",
                'whatsapp_url' => $url,
            ];
        }

        // 2. EMPEROR PERMANENT: 10% discount for Sio Pass
        if ($rank === 'emperor' && $isSioPass) {
            $discountPercent = $emperorDiscount;
            $discountMultiplier = (100 - $discountPercent) / 100.0;
            $discountedPrice = round($basePrice * $discountMultiplier);
            $savings = $basePrice - $discountedPrice;
            $formattedPrice = number_format($discountedPrice, 0, ',', '.');
            $url = self::generateBattlepassWhatsAppUrl($packageName, $discountedPrice, 'Emperor');

            return [
                'has_discount' => true,
                'discount_percent' => $discountPercent,
                'eligible_rank' => 'Emperor',
                'is_free_current_season' => true,
                'original_price' => $basePrice,
                'discounted_price' => $discountedPrice,
                'savings' => $savings,
                'whatsapp_message' => "Min, aku mau beli battlepass {$packageName} yang harganya Rp.{$formattedPrice} karna aku udah beli rank Emperor jadi harganya segitu",
                'whatsapp_url' => $url,
            ];
        }

        return $defaultResult;
    }
}
