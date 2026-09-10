<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\User;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class RankPurchase extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_rank_purchases';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'user_id',
        'minecraft_account_id',
        'minecraft_uuid',
        'minecraft_username',
        'rank',
        'rank_type',
        'duration_days',
        'price_paid',
        'status',
        'started_at',
        'expires_at',
        'source',
        'notes',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'duration_days' => 'integer',
        'price_paid' => 'double',
        'started_at' => 'datetime',
        'expires_at' => 'datetime',
    ];

    /**
     * Get the associated user.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Get the associated Minecraft account.
     */
    public function minecraftAccount(): BelongsTo
    {
        return $this->belongsTo(MinecraftAccount::class, 'minecraft_account_id');
    }

    /**
     * Determine if this purchase is an active trial.
     */
    public function isTrial(): bool
    {
        return strtoupper($this->rank_type) === 'TRIAL';
    }

    /**
     * Determine if this purchase is permanent.
     */
    public function isPermanent(): bool
    {
        return strtoupper($this->rank_type) === 'PERMANENT';
    }

    /**
     * Check if trial has expired.
     */
    public function isExpired(): bool
    {
        if ($this->isPermanent()) {
            return false;
        }

        return $this->expires_at && $this->expires_at->isPast();
    }
}
