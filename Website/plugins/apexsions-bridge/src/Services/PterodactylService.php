<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\Setting;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class PterodactylService
{
    /**
     * Get the base panel URL.
     */
    public static function getPanelUrl(): string
    {
        return rtrim((string) setting('apexsions.pterodactyl_url', env('PTERODACTYL_URL', 'https://stellar.jagoanhosting.id')), '/');
    }

    /**
     * Get the client API key.
     */
    public static function getApiKey(): string
    {
        return (string) setting('apexsions.pterodactyl_api_key', env('PTERODACTYL_API_KEY', ''));
    }

    /**
     * Get the target Server ID.
     */
    public static function getServerId(): string
    {
        return (string) setting('apexsions.pterodactyl_server_id', env('PTERODACTYL_SERVER_ID', '27e4a2f6'));
    }

    /**
     * Check if Pterodactyl credentials are configured.
     */
    public static function isConfigured(): bool
    {
        return !empty(self::getApiKey()) && !empty(self::getServerId());
    }

    /**
     * Build an authorized HTTP client for Pterodactyl Client API.
     */
    protected static function client(int $timeout = 15)
    {
        return Http::withHeaders([
            'Authorization' => 'Bearer ' . self::getApiKey(),
            'Accept' => 'application/json',
            'Content-Type' => 'application/json',
        ])->withoutVerifying()->timeout($timeout);
    }

    /**
     * Get server details (name, limits, allocations).
     */
    public static function getServerDetails(): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId();
            $res = self::client()->get($url);

            if ($res->successful()) {
                return ['success' => true, 'data' => $res->json('attributes') ?? []];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Get live resource stats (state, CPU, RAM, Disk, Uptime).
     */
    public static function getResources(): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/resources';
            $res = self::client(8)->get($url);

            if ($res->successful()) {
                $attrs = $res->json('attributes') ?? [];
                return [
                    'success' => true,
                    'current_state' => $attrs['current_state'] ?? 'offline',
                    'is_suspended' => $attrs['is_suspended'] ?? false,
                    'resources' => $attrs['resources'] ?? [],
                ];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Send power signal ('start', 'stop', 'restart', 'kill').
     */
    public static function sendPowerSignal(string $signal): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        $signal = strtolower(trim($signal));
        if (!in_array($signal, ['start', 'stop', 'restart', 'kill'], true)) {
            return ['success' => false, 'error' => 'Signal power tidak valid.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/power';
            $res = self::client(15)->post($url, ['signal' => $signal]);

            if ($res->successful() || $res->status() === 204) {
                return ['success' => true, 'signal' => $signal];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Send a raw console command to the Minecraft server.
     */
    public static function sendCommand(string $command): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        $command = trim($command);
        if (empty($command)) {
            return ['success' => false, 'error' => 'Perintah tidak boleh kosong.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/command';
            $res = self::client(10)->post($url, ['command' => $command]);

            if ($res->successful() || $res->status() === 204) {
                return ['success' => true, 'command' => $command];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Get WebSocket token and URL for real-time console streaming.
     */
    public static function getWebsocketCredentials(): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/websocket';
            $res = self::client(10)->get($url);

            if ($res->successful()) {
                $data = $res->json('data') ?? [];
                return [
                    'success' => true,
                    'token' => $data['token'] ?? null,
                    'socket' => $data['socket'] ?? null,
                    'origin' => self::getPanelUrl(),
                ];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * List files in a directory.
     */
    public static function listFiles(string $directory = '/'): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/files/list?directory=' . urlencode($directory);
            $res = self::client(15)->get($url);

            if ($res->successful()) {
                $rawList = $res->json('data') ?? [];
                $files = [];
                foreach ($rawList as $item) {
                    $files[] = $item['attributes'] ?? $item;
                }
                return ['success' => true, 'directory' => $directory, 'files' => $files];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Get file contents as plain text.
     */
    public static function getFileContents(string $filePath): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/files/contents?file=' . urlencode($filePath);
            $res = Http::withHeaders([
                'Authorization' => 'Bearer ' . self::getApiKey(),
                'Accept' => 'text/plain',
            ])->withoutVerifying()->timeout(15)->get($url);

            if ($res->successful()) {
                return ['success' => true, 'file' => $filePath, 'content' => $res->body()];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Write plain text to a file.
     */
    public static function writeFileContents(string $filePath, string $content): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/files/write?file=' . urlencode($filePath);
            $res = Http::withHeaders([
                'Authorization' => 'Bearer ' . self::getApiKey(),
                'Content-Type' => 'text/plain',
            ])->withBody($content, 'text/plain')->withoutVerifying()->timeout(20)->post($url);

            if ($res->successful() || $res->status() === 204) {
                return ['success' => true, 'file' => $filePath];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Create a folder in the specified root path.
     */
    public static function createFolder(string $root, string $name): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/files/create-folder';
            $res = self::client()->post($url, ['root' => $root, 'name' => $name]);

            if ($res->successful() || $res->status() === 204) {
                return ['success' => true];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Delete files or folders.
     */
    public static function deleteFiles(string $root, array $files): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/files/delete';
            $res = self::client()->post($url, ['root' => $root, 'files' => $files]);

            if ($res->successful() || $res->status() === 204) {
                return ['success' => true];
            }

            return ['success' => false, 'error' => $res->body(), 'status' => $res->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }

    /**
     * Get a signed download URL.
     */
    public static function getDownloadUrl(string $filePath): ?string
    {
        if (!self::isConfigured()) {
            return null;
        }

        try {
            $url = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/files/download?file=' . urlencode($filePath);
            $res = self::client()->get($url);

            if ($res->successful()) {
                return $res->json('attributes.url');
            }
        } catch (\Throwable $e) {
            Log::warning("Pterodactyl getDownloadUrl error: " . $e->getMessage());
        }
        return null;
    }

    /**
     * Upload an uploaded file to Pterodactyl.
     */
    public static function uploadFile(string $directory, string $filename, string $fileContent): array
    {
        if (!self::isConfigured()) {
            return ['success' => false, 'error' => 'Pterodactyl API Key belum dikonfigurasi.'];
        }

        try {
            $uploadUrlEndpoint = self::getPanelUrl() . '/api/client/servers/' . self::getServerId() . '/files/upload';
            $res = self::client()->get($uploadUrlEndpoint);

            if (!$res->successful()) {
                return ['success' => false, 'error' => 'Gagal mendapatkan signed upload URL dari panel.'];
            }

            $signedUrl = $res->json('attributes.url');
            if (empty($signedUrl)) {
                return ['success' => false, 'error' => 'Signed upload URL kosong.'];
            }

            $targetUrl = $signedUrl . '&directory=' . urlencode($directory);

            $uploadRes = Http::withoutVerifying()->timeout(120)->attach(
                'files',
                $fileContent,
                $filename
            )->post($targetUrl);

            if ($uploadRes->successful() || $uploadRes->status() === 204) {
                return ['success' => true, 'filename' => $filename, 'directory' => $directory];
            }

            return ['success' => false, 'error' => $uploadRes->body(), 'status' => $uploadRes->status()];
        } catch (\Throwable $e) {
            return ['success' => false, 'error' => $e->getMessage()];
        }
    }
}
