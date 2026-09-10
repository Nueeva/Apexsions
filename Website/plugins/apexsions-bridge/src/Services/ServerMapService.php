<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\Setting;
use Illuminate\Support\Facades\Cache;

class ServerMapService
{
    /**
     * Get the live BlueMap URL.
     */
    public static function getMapUrl(): string
    {
        return setting('apexsions.map_url', 'http://apexsions.my.id:32076/');
    }

    /**
     * Check if the map feature is enabled by admin.
     */
    public static function isMapEnabled(): bool
    {
        return (bool) setting('apexsions.map_enabled', true);
    }

    /**
     * Check if navigation links (navbar & mobile drawer) are visible.
     */
    public static function isNavigationVisible(): bool
    {
        return (bool) setting('apexsions.map_nav_visible', true);
    }

    /**
     * Check if background health check is enabled.
     */
    public static function isHealthCheckEnabled(): bool
    {
        return (bool) setting('apexsions.map_health_check', true);
    }

    /**
     * Check if BlueMap server is currently online (cached for 60 seconds).
     */
    public static function isMapOnline(): bool
    {
        if (!self::isHealthCheckEnabled()) {
            return true;
        }

        return Cache::remember('apexsions.map_online_status', 60, function () {
            $url = self::getMapUrl();
            $parts = parse_url($url);
            $host = $parts['host'] ?? 'apexsions.my.id';
            $port = (int) ($parts['port'] ?? 32076);

            $connection = @fsockopen($host, $port, $errno, $errstr, 1.5);
            if (is_resource($connection)) {
                fclose($connection);
                return true;
            }

            // Fallback: check loopback on VPS
            $localConn = @fsockopen('127.0.0.1', 32076, $errno, $errstr, 1.0);
            if (is_resource($localConn)) {
                fclose($localConn);
                return true;
            }

            return false;
        });
    }

    /**
     * Update Server Map settings in Azuriom database.
     */
    public static function updateSettings(array $data): void
    {
        Setting::updateSettings([
            'apexsions.map_enabled' => !empty($data['map_enabled']),
            'apexsions.map_url' => $data['map_url'] ?? 'http://apexsions.my.id:32076/',
            'apexsions.map_health_check' => !empty($data['map_health_check']),
            'apexsions.map_nav_visible' => !empty($data['map_nav_visible']),
        ]);
        Cache::forget('apexsions.map_online_status');
    }
}
