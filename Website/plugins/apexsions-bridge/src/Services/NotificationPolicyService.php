<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\ApexsionsNotification;
use Carbon\Carbon;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Str;

class NotificationPolicyService
{
    /**
     * Resolve routing channels for a given severity.
     */
    public static function resolveChannels(string $severity): array
    {
        $severity = strtoupper($severity);

        return match ($severity) {
            'CRITICAL', 'HIGH' => ['IN_APP', 'DISCORD_WEBHOOK'],
            'MEDIUM', 'LOW' => ['IN_APP'],
            default => ['IN_APP'],
        };
    }

    /**
     * Resolve deduplication window in minutes based on severity.
     */
    public static function getDedupWindowMinutes(string $severity): int
    {
        return match (strtoupper($severity)) {
            'CRITICAL' => 15,
            'HIGH' => 30,
            'MEDIUM' => 60,
            default => 120,
        };
    }

    /**
     * Resolve cooldown duration in minutes.
     */
    public static function getCooldownMinutes(string $severity): int
    {
        return match (strtoupper($severity)) {
            'CRITICAL' => 5,
            'HIGH' => 15,
            'MEDIUM' => 30,
            default => 60,
        };
    }

    /**
     * Ingest or deduplicate a notification.
     * Returns an array with ['notification' => ApexsionsNotification, 'is_duplicate' => bool, 'channels' => array]
     */
    public static function process(array $data): array
    {
        $type = strtoupper($data['type'] ?? 'SYSTEM_ALERT');
        $severity = strtoupper($data['severity'] ?? 'MEDIUM');
        $entityType = strtoupper($data['entity_type'] ?? 'SYSTEM');
        $entityId = (string) ($data['entity_id'] ?? 'SYSTEM');
        $source = strtoupper($data['source'] ?? 'SYSTEM');
        $title = $data['title'] ?? 'Apexsions System Alert';
        $message = $data['message'] ?? '';

        $dedupKey = sprintf('%s_%s_%s', $type, $entityType, $entityId);
        $windowMinutes = self::getDedupWindowMinutes($severity);

        // 1. Check for an active unacknowledged notification within the deduplication window
        $cutoff = Carbon::now()->subMinutes($windowMinutes);
        $existing = ApexsionsNotification::where('dedup_key', $dedupKey)
            ->where('status', 'UNACKNOWLEDGED')
            ->where('last_occurred_at', '>=', $cutoff)
            ->latest('last_occurred_at')
            ->first();

        if ($existing) {
            $existing->increment('occurrence_count');
            $existing->update([
                'last_occurred_at' => Carbon::now(),
                'message' => !empty($message) ? $message : $existing->message,
            ]);

            // Check if still inside notification cooldown to prevent external webhook spam
            $cooldownCacheKey = "notif_cd_{$dedupKey}";
            $inCooldown = Cache::has($cooldownCacheKey);

            return [
                'notification' => $existing,
                'is_duplicate' => true,
                'channels' => $inCooldown ? ['IN_APP'] : self::resolveChannels($severity),
            ];
        }

        // 2. Create fresh notification
        $notificationId = 'notif_' . Str::uuid();
        $notification = ApexsionsNotification::create([
            'notification_id' => $notificationId,
            'dedup_key' => $dedupKey,
            'type' => $type,
            'severity' => $severity,
            'title' => $title,
            'message' => $message,
            'source' => $source,
            'entity_type' => $entityType,
            'entity_id' => $entityId,
            'event_id' => $data['event_id'] ?? null,
            'incident_id' => $data['incident_id'] ?? null,
            'action_id' => $data['action_id'] ?? null,
            'correlation_id' => $data['correlation_id'] ?? null,
            'occurrence_count' => 1,
            'last_occurred_at' => Carbon::now(),
            'status' => 'UNACKNOWLEDGED',
            'metadata' => $data['metadata'] ?? [],
        ]);

        // Put cooldown key in cache
        $cooldownMinutes = self::getCooldownMinutes($severity);
        Cache::put("notif_cd_{$dedupKey}", true, Carbon::now()->addMinutes($cooldownMinutes));

        return [
            'notification' => $notification,
            'is_duplicate' => false,
            'channels' => self::resolveChannels($severity),
        ];
    }
}
