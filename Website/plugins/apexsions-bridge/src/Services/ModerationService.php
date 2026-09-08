<?php

namespace Azuriom\Plugin\ApexsionsBridge\Services;

use Azuriom\Models\User;
use Azuriom\Plugin\ApexsionsBridge\Models\Delivery;
use Azuriom\Plugin\ApexsionsBridge\Models\Punishment;
use Carbon\Carbon;
use Illuminate\Support\Str;

class ModerationService
{
    /**
     * Issue an official staff warning to a player.
     */
    public static function warn(string $playerUuid, string $playerName, string $reason, ?User $staff = null): Punishment
    {
        $actionId = (string) Str::uuid();
        $staffId = $staff?->id;
        $staffName = $staff?->name ?? 'System';

        // 1. Audit pending
        $audit = AuditService::start(
            'PLAYER_WARNED',
            'PLAYER',
            $playerUuid,
            $playerName,
            $reason,
            null,
            ['action_id' => $actionId, 'staff' => $staffName, 'staff_id' => $staffId],
            'WEB'
        );

        // 2. Create punishment record
        $punishment = Punishment::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_name' => $playerName,
            'type' => 'WARN',
            'reason' => $reason,
            'staff_id' => $staffId,
            'staff_name' => $staffName,
            'status' => 'ACTIVE',
            'source' => 'WEB',
        ]);

        // 3. Queue command to Minecraft server
        $sanitizedReason = str_replace('"', '\\"', $reason);
        $cmd = "minecraft:tellraw {$playerName} [{\"text\":\"[PERINGATAN STAF] \",\"color\":\"gold\",\"bold\":true},{\"text\":\"{$sanitizedReason}\",\"color\":\"yellow\"}]";

        Delivery::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_username' => $playerName,
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        return $punishment;
    }

    /**
     * Mute a player for a given duration (in minutes, 0 or null = permanent).
     */
    public static function mute(string $playerUuid, string $playerName, ?int $durationMinutes, string $reason, ?User $staff = null): Punishment
    {
        $actionId = (string) Str::uuid();
        $staffId = $staff?->id;
        $staffName = $staff?->name ?? 'System';

        $durationSeconds = ($durationMinutes !== null && $durationMinutes > 0) ? ($durationMinutes * 60) : null;
        $expiresAt = $durationSeconds ? Carbon::now()->addSeconds($durationSeconds) : null;

        // 1. Audit pending
        AuditService::start(
            'PLAYER_MUTED',
            'PLAYER',
            $playerUuid,
            $playerName,
            $reason,
            null,
            [
                'action_id' => $actionId,
                'staff' => $staffName,
                'staff_id' => $staffId,
                'duration_minutes' => $durationMinutes,
                'expires_at' => $expiresAt?->toIso8601String(),
            ],
            'WEB'
        );

        // 2. Create punishment record
        $punishment = Punishment::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_name' => $playerName,
            'type' => 'MUTE',
            'reason' => $reason,
            'staff_id' => $staffId,
            'staff_name' => $staffName,
            'duration_seconds' => $durationSeconds,
            'expires_at' => $expiresAt,
            'status' => 'ACTIVE',
            'source' => 'WEB',
        ]);

        // 3. Queue command to Minecraft server
        $sanitizedReason = str_replace('"', '', $reason);
        $durationArg = $durationMinutes ? "{$durationMinutes}m" : "";
        $cmd = trim("mute {$playerName} {$durationArg} {$sanitizedReason}");

        Delivery::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_username' => $playerName,
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        return $punishment;
    }

    /**
     * Unmute a player and pardon active mute punishment.
     */
    public static function unmute(string $playerUuid, string $playerName, string $reason, ?User $staff = null): void
    {
        $actionId = (string) Str::uuid();
        $staffId = $staff?->id;
        $staffName = $staff?->name ?? 'System';

        AuditService::start(
            'PLAYER_UNMUTED',
            'PLAYER',
            $playerUuid,
            $playerName,
            $reason,
            null,
            ['action_id' => $actionId, 'staff' => $staffName, 'staff_id' => $staffId],
            'WEB'
        );

        // Mark active mutes as PARDONED
        Punishment::where('player_uuid', $playerUuid)
            ->where('type', 'MUTE')
            ->where('status', 'ACTIVE')
            ->update([
                'status' => 'PARDONED',
                'pardon_reason' => $reason,
                'pardoned_by' => $staffName,
                'pardoned_at' => now(),
            ]);

        Delivery::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_username' => $playerName,
            'command' => "unmute {$playerName}",
            'status' => 'PENDING',
        ]);
    }

    /**
     * Kick a player from the server immediately.
     */
    public static function kick(string $playerUuid, string $playerName, string $reason, ?User $staff = null): Punishment
    {
        $actionId = (string) Str::uuid();
        $staffId = $staff?->id;
        $staffName = $staff?->name ?? 'System';

        AuditService::start(
            'PLAYER_KICKED',
            'PLAYER',
            $playerUuid,
            $playerName,
            $reason,
            null,
            ['action_id' => $actionId, 'staff' => $staffName, 'staff_id' => $staffId],
            'WEB'
        );

        $punishment = Punishment::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_name' => $playerName,
            'type' => 'KICK',
            'reason' => $reason,
            'staff_id' => $staffId,
            'staff_name' => $staffName,
            'status' => 'ACTIVE',
            'source' => 'WEB',
        ]);

        $sanitizedReason = str_replace('"', '', $reason);
        Delivery::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_username' => $playerName,
            'command' => "kick {$playerName} {$sanitizedReason}",
            'status' => 'PENDING',
        ]);

        return $punishment;
    }

    /**
     * Ban a player from the server (hours duration, null = permanent).
     */
    public static function ban(string $playerUuid, string $playerName, ?int $durationHours, string $reason, ?User $staff = null): Punishment
    {
        $actionId = (string) Str::uuid();
        $staffId = $staff?->id;
        $staffName = $staff?->name ?? 'System';

        $durationSeconds = ($durationHours !== null && $durationHours > 0) ? ($durationHours * 3600) : null;
        $expiresAt = $durationSeconds ? Carbon::now()->addSeconds($durationSeconds) : null;

        AuditService::start(
            'PLAYER_BANNED',
            'PLAYER',
            $playerUuid,
            $playerName,
            $reason,
            null,
            [
                'action_id' => $actionId,
                'staff' => $staffName,
                'staff_id' => $staffId,
                'duration_hours' => $durationHours,
                'expires_at' => $expiresAt?->toIso8601String(),
            ],
            'WEB'
        );

        $punishment = Punishment::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_name' => $playerName,
            'type' => 'BAN',
            'reason' => $reason,
            'staff_id' => $staffId,
            'staff_name' => $staffName,
            'duration_seconds' => $durationSeconds,
            'expires_at' => $expiresAt,
            'status' => 'ACTIVE',
            'source' => 'WEB',
        ]);

        $sanitizedReason = str_replace('"', '', $reason);
        $durationArg = $durationHours ? "{$durationHours}h" : "";
        $cmd = trim("ban {$playerName} {$durationArg} {$sanitizedReason}");

        Delivery::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_username' => $playerName,
            'command' => $cmd,
            'status' => 'PENDING',
        ]);

        return $punishment;
    }

    /**
     * Unban a player and pardon active ban.
     */
    public static function unban(string $playerUuid, string $playerName, string $reason, ?User $staff = null): void
    {
        $actionId = (string) Str::uuid();
        $staffId = $staff?->id;
        $staffName = $staff?->name ?? 'System';

        AuditService::start(
            'PLAYER_UNBANNED',
            'PLAYER',
            $playerUuid,
            $playerName,
            $reason,
            null,
            ['action_id' => $actionId, 'staff' => $staffName, 'staff_id' => $staffId],
            'WEB'
        );

        Punishment::where('player_uuid', $playerUuid)
            ->where('type', 'BAN')
            ->where('status', 'ACTIVE')
            ->update([
                'status' => 'PARDONED',
                'pardon_reason' => $reason,
                'pardoned_by' => $staffName,
                'pardoned_at' => now(),
            ]);

        Delivery::create([
            'action_id' => $actionId,
            'player_uuid' => $playerUuid,
            'player_username' => $playerName,
            'command' => "pardon {$playerName}",
            'status' => 'PENDING',
        ]);
    }

    /**
     * Pardon an active punishment.
     */
    public static function pardon(Punishment $punishment, string $reason, ?User $staff = null): void
    {
        if ($punishment->type === 'BAN') {
            self::unban($punishment->player_uuid, $punishment->player_name, $reason, $staff);
        } elseif ($punishment->type === 'MUTE') {
            self::unmute($punishment->player_uuid, $punishment->player_name, $reason, $staff);
        } else {
            $staffName = $staff?->name ?? 'System';
            $punishment->update([
                'status' => 'PARDONED',
                'pardon_reason' => $reason,
                'pardoned_by' => $staffName,
                'pardoned_at' => now(),
            ]);
            AuditService::log([
                'actor_type' => 'STAFF',
                'actor_id' => $staff ? (string) $staff->id : null,
                'actor_name' => $staffName,
                'action' => 'PUNISHMENT_PARDONED',
                'target_type' => 'PLAYER',
                'target_id' => $punishment->player_uuid,
                'target_name' => $punishment->player_name,
                'reason' => $reason,
                'source' => 'WEB',
                'status' => 'SUCCESS',
            ]);
        }
    }
}
