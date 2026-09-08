<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsNotification;
use Azuriom\Plugin\ApexsionsBridge\Models\NotificationDelivery;
use Carbon\Carbon;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Str;

class NotificationDispatcherService
{
    /**
     * Dispatch notification to designated channels.
     */
    public static function dispatch(ApexsionsNotification $notification, array $channels, bool $isDuplicate = false): array
    {
        $deliveries = [];

        foreach ($channels as $channel) {
            $deliveryId = 'deliv_' . Str::uuid();

            // If it is a duplicate event and not IN_APP, suppress to prevent spamming external channels
            if ($isDuplicate && $channel !== 'IN_APP') {
                $delivery = NotificationDelivery::create([
                    'delivery_id' => $deliveryId,
                    'notification_id' => $notification->notification_id,
                    'channel' => $channel,
                    'status' => 'SUPPRESSED',
                    'attempt_count' => 0,
                    'error_summary' => 'Suppressed by anti-spam deduplication policy',
                    'payload' => self::buildPayload($notification),
                ]);
                $deliveries[] = $delivery;
                continue;
            }

            if ($channel === 'IN_APP') {
                $delivery = NotificationDelivery::create([
                    'delivery_id' => $deliveryId,
                    'notification_id' => $notification->notification_id,
                    'channel' => 'IN_APP',
                    'status' => 'DELIVERED',
                    'attempt_count' => 1,
                    'last_attempt_at' => Carbon::now(),
                    'payload' => self::buildPayload($notification),
                ]);
                $deliveries[] = $delivery;
            } elseif ($channel === 'DISCORD_WEBHOOK') {
                $delivery = self::dispatchDiscordWebhook($notification, $deliveryId);
                $deliveries[] = $delivery;
            }
        }

        return $deliveries;
    }

    /**
     * Dispatch to Discord Webhook with strict SSRF checks and sensitive redaction.
     */
    protected static function dispatchDiscordWebhook(ApexsionsNotification $notification, string $deliveryId): NotificationDelivery
    {
        $webhookUrl = function_exists('setting') ? setting('apexsions.discord_webhook_url') : null;
        if (empty($webhookUrl)) {
            $webhookUrl = env('DISCORD_WEBHOOK_URL');
        }

        $payload = self::buildPayload($notification);

        if (empty($webhookUrl)) {
            return NotificationDelivery::create([
                'delivery_id' => $deliveryId,
                'notification_id' => $notification->notification_id,
                'channel' => 'DISCORD_WEBHOOK',
                'status' => 'SUPPRESSED',
                'attempt_count' => 0,
                'error_summary' => 'Discord Webhook URL not configured (Safe fallback)',
                'payload' => $payload,
            ]);
        }

        // SSRF Validation: destination domain MUST be discord.com or discordapp.com
        if (!self::isValidDiscordUrl($webhookUrl)) {
            return NotificationDelivery::create([
                'delivery_id' => $deliveryId,
                'notification_id' => $notification->notification_id,
                'channel' => 'DISCORD_WEBHOOK',
                'status' => 'FAILED',
                'attempt_count' => 1,
                'last_attempt_at' => Carbon::now(),
                'error_summary' => 'Blocked insecure webhook destination (SSRF Protection: Domain not allowed)',
                'payload' => $payload,
            ]);
        }

        try {
            $response = Http::timeout(3)->post($webhookUrl, $payload);

            if ($response->successful()) {
                return NotificationDelivery::create([
                    'delivery_id' => $deliveryId,
                    'notification_id' => $notification->notification_id,
                    'channel' => 'DISCORD_WEBHOOK',
                    'status' => 'DELIVERED',
                    'attempt_count' => 1,
                    'last_attempt_at' => Carbon::now(),
                    'payload' => $payload,
                ]);
            }

            return NotificationDelivery::create([
                'delivery_id' => $deliveryId,
                'notification_id' => $notification->notification_id,
                'channel' => 'DISCORD_WEBHOOK',
                'status' => 'RETRYING',
                'attempt_count' => 1,
                'last_attempt_at' => Carbon::now(),
                'next_retry_at' => Carbon::now()->addMinutes(5),
                'error_summary' => 'HTTP Status: ' . $response->status(),
                'payload' => $payload,
            ]);
        } catch (\Throwable $e) {
            // Notification failure MUST NEVER throw unhandled exception or recursively notify
            return NotificationDelivery::create([
                'delivery_id' => $deliveryId,
                'notification_id' => $notification->notification_id,
                'channel' => 'DISCORD_WEBHOOK',
                'status' => 'RETRYING',
                'attempt_count' => 1,
                'last_attempt_at' => Carbon::now(),
                'next_retry_at' => Carbon::now()->addMinutes(5),
                'error_summary' => Str::limit($e->getMessage(), 250),
                'payload' => $payload,
            ]);
        }
    }

    /**
     * Validate Discord Webhook URL to prevent SSRF against internal services.
     */
    public static function isValidDiscordUrl(string $url): bool
    {
        $parts = parse_url($url);
        if (!$parts || empty($parts['scheme']) || empty($parts['host'])) {
            return false;
        }

        if (strtolower($parts['scheme']) !== 'https') {
            return false;
        }

        $host = strtolower($parts['host']);

        return $host === 'discord.com' ||
               $host === 'discordapp.com' ||
               str_ends_with($host, '.discord.com') ||
               str_ends_with($host, '.discordapp.com');
    }

    /**
     * Build Discord-compatible JSON payload with sanitized fields and zero secrets.
     */
    public static function buildPayload(ApexsionsNotification $notification): array
    {
        $color = match (strtoupper($notification->severity)) {
            'CRITICAL' => 0xE74C3C, // Red
            'HIGH' => 0xE67E22,     // Orange
            'MEDIUM' => 0xF1C40F,   // Yellow
            default => 0x3498DB,    // Blue
        };

        $sanitizedMetadata = AuditService::sanitize($notification->metadata ?? []);

        return [
            'username' => 'Apexsions Realm Guard',
            'embeds' => [
                [
                    'title' => sprintf("[%s] %s", $notification->severity, Str::limit($notification->title, 100)),
                    'description' => Str::limit(self::sanitizeString($notification->message), 1000),
                    'color' => $color,
                    'fields' => [
                        [
                            'name' => 'Source / Type',
                            'value' => "{$notification->source} / {$notification->type}",
                            'inline' => true,
                        ],
                        [
                            'name' => 'Entity',
                            'value' => "{$notification->entity_type}: {$notification->entity_id}",
                            'inline' => true,
                        ],
                        [
                            'name' => 'Occurrences',
                            'value' => (string) $notification->occurrence_count,
                            'inline' => true,
                        ],
                    ],
                    'footer' => [
                        'text' => "Notification ID: {$notification->notification_id}",
                    ],
                    'timestamp' => $notification->last_occurred_at ? $notification->last_occurred_at->toIso8601String() : Carbon::now()->toIso8601String(),
                ],
            ],
            'meta' => $sanitizedMetadata,
        ];
    }

    /**
     * Sanitize string values to ensure passwords or keys do not leak.
     */
    protected static function sanitizeString(string $text): string
    {
        $patterns = [
            '/api[_-]?key[:=]\s*["\']?[a-zA-Z0-9_\-]+["\']?/i' => 'api_key=[REDACTED]',
            '/token[:=]\s*["\']?[a-zA-Z0-9_\-]+["\']?/i' => 'token=[REDACTED]',
            '/password[:=]\s*["\']?[^\s"\'&]+["\']?/i' => 'password=[REDACTED]',
            '/secret[:=]\s*["\']?[a-zA-Z0-9_\-]+["\']?/i' => 'secret=[REDACTED]',
        ];

        return preg_replace(array_keys($patterns), array_values($patterns), $text) ?? $text;
    }
}
