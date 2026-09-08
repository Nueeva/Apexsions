<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Plugin\ApexsionsBridge\Models\AuditLog;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Request;

class AuditService
{
    /**
     * List of sensitive keys to redact from logs and metadata.
     */
    protected static array $redactedKeys = [
        'password',
        'password_confirmation',
        'new_password',
        'old_password',
        'secret',
        'api_key',
        'token',
        'auth_token',
        'key',
    ];

    /**
     * Record a direct complete audit log entry.
     */
    public static function log(array $data): AuditLog
    {
        $actorUser = Auth::user();
        $actorType = $data['actor_type'] ?? ($actorUser ? 'USER' : 'SYSTEM');
        $actorId = $data['actor_id'] ?? ($actorUser ? (string) $actorUser->id : null);
        $actorName = $data['actor_name'] ?? ($actorUser ? $actorUser->name : 'System');

        $metadata = self::sanitize($data['metadata'] ?? []);
        if (is_array($metadata) && Request::ip()) {
            $metadata['ip'] = Request::ip();
            $metadata['user_agent'] = substr((string) Request::userAgent(), 0, 150);
        }

        $actionId = $data['action_id'] ?? ($metadata['action_id'] ?? null);

        return AuditLog::create([
            'action_id' => $actionId,
            'actor_type' => $actorType,
            'actor_id' => $actorId,
            'actor_name' => $actorName,
            'action' => strtoupper($data['action'] ?? 'UNKNOWN_ACTION'),
            'target_type' => $data['target_type'] ?? null,
            'target_id' => $data['target_id'] ?? null,
            'target_name' => $data['target_name'] ?? null,
            'old_value' => self::formatValue($data['old_value'] ?? null),
            'new_value' => self::formatValue($data['new_value'] ?? null),
            'reason' => $data['reason'] ?? null,
            'source' => strtoupper($data['source'] ?? 'WEB'),
            'status' => strtoupper($data['status'] ?? 'SUCCESS'),
            'metadata' => $metadata,
        ]);
    }

    /**
     * Begin an administrative action with status PENDING.
     */
    public static function start(
        string $action,
        ?string $targetType = null,
        ?string $targetId = null,
        ?string $targetName = null,
        ?string $reason = null,
        mixed $oldValue = null,
        array $metadata = [],
        string $source = 'WEB'
    ): AuditLog {
        if (empty($metadata['action_id'])) {
            $metadata['action_id'] = (string) \Illuminate\Support\Str::uuid();
        }

        return self::log([
            'actor_type' => !empty($metadata['staff_id']) ? 'STAFF' : null,
            'actor_id' => !empty($metadata['staff_id']) ? (string) $metadata['staff_id'] : null,
            'actor_name' => $metadata['staff'] ?? null,
            'action' => $action,
            'target_type' => $targetType,
            'target_id' => $targetId,
            'target_name' => $targetName,
            'old_value' => $oldValue,
            'new_value' => null,
            'reason' => $reason,
            'source' => $source,
            'status' => 'PENDING',
            'metadata' => $metadata,
        ]);
    }

    /**
     * Mark an action as SUCCESS with optional new value.
     */
    public static function success(AuditLog|int $log, mixed $newValue = null, ?array $additionalMeta = null): void
    {
        $instance = is_numeric($log) ? AuditLog::find($log) : $log;
        if (!$instance) {
            return;
        }

        $meta = $instance->metadata ?? [];
        if (!empty($additionalMeta)) {
            $meta = array_merge($meta, self::sanitize($additionalMeta));
        }

        $updates = [
            'status' => 'SUCCESS',
            'metadata' => $meta,
        ];

        if ($newValue !== null) {
            $updates['new_value'] = self::formatValue($newValue);
        }

        $instance->update($updates);
    }

    /**
     * Mark an action as FAILED with error diagnostic.
     */
    public static function failed(AuditLog|int $log, string $errorMessage, ?array $additionalMeta = null): void
    {
        $instance = is_numeric($log) ? AuditLog::find($log) : $log;
        if (!$instance) {
            return;
        }

        $meta = $instance->metadata ?? [];
        $meta['error'] = substr($errorMessage, 0, 500);
        if (!empty($additionalMeta)) {
            $meta = array_merge($meta, self::sanitize($additionalMeta));
        }

        $instance->update([
            'status' => 'FAILED',
            'metadata' => $meta,
        ]);
    }

    /**
     * Format old/new values safely into text/json string without secret leakage.
     */
    protected static function formatValue(mixed $value): ?string
    {
        if ($value === null) {
            return null;
        }
        if (is_array($value) || is_object($value)) {
            return json_encode(self::sanitize((array) $value), JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
        }
        return (string) $value;
    }

    /**
     * Recursively sanitize data array removing sensitive credential values.
     */
    public static function sanitize(mixed $data): mixed
    {
        if (!is_array($data)) {
            return $data;
        }

        $sanitized = [];
        foreach ($data as $key => $val) {
            $lowerKey = strtolower((string) $key);
            if (in_array($lowerKey, self::$redactedKeys, true) || str_contains($lowerKey, 'password') || str_contains($lowerKey, 'secret')) {
                $sanitized[$key] = '[REDACTED]';
            } elseif (is_array($val)) {
                $sanitized[$key] = self::sanitize($val);
            } else {
                $sanitized[$key] = $val;
            }
        }
        return $sanitized;
    }
}
