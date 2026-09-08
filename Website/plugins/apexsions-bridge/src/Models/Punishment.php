<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\User;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Punishment extends Model
{
    /**
     * The table associated with the model.
     */
    protected $table = 'apexsions_punishments';

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'action_id',
        'player_uuid',
        'player_name',
        'type',
        'reason',
        'staff_id',
        'staff_name',
        'duration_seconds',
        'expires_at',
        'status',
        'pardon_reason',
        'pardoned_by',
        'pardoned_at',
        'source',
        'metadata',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'staff_id' => 'integer',
        'duration_seconds' => 'integer',
        'expires_at' => 'datetime',
        'pardoned_at' => 'datetime',
        'metadata' => 'array',
    ];

    /**
     * Staff member who issued the punishment.
     */
    public function staff(): BelongsTo
    {
        return $this->belongsTo(User::class, 'staff_id');
    }

    /**
     * Player account record.
     */
    public function playerAccount(): BelongsTo
    {
        return $this->belongsTo(MinecraftAccount::class, 'player_uuid', 'minecraft_uuid');
    }

    /**
     * Scope query to active punishments.
     */
    public function scopeActive(Builder $query): Builder
    {
        return $query->where('status', 'ACTIVE')
            ->where(function ($q) {
                $q->whereNull('expires_at')
                  ->orWhere('expires_at', '>', now());
            });
    }

    /**
     * Scope query by type (WARN, MUTE, KICK, BAN).
     */
    public function scopeFilterType(Builder $query, ?string $type): Builder
    {
        if (!empty($type) && $type !== 'all') {
            return $query->where('type', strtoupper($type));
        }
        return $query;
    }

    /**
     * Scope query for a specific player (UUID or username).
     */
    public function scopeForPlayer(Builder $query, string $identifier): Builder
    {
        return $query->where('player_uuid', $identifier)
            ->orWhere('player_name', $identifier);
    }

    /**
     * Check if punishment is active.
     */
    public function isActive(): bool
    {
        if ($this->status !== 'ACTIVE') {
            return false;
        }

        if ($this->expires_at !== null && $this->expires_at->isPast()) {
            return false;
        }

        return true;
    }

    /**
     * Check if punishment is permanent.
     */
    public function isPermanent(): bool
    {
        return $this->duration_seconds === null && $this->expires_at === null;
    }
}
