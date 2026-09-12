<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Azuriom\Models\User;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class MinecraftAccount extends Model
{
    use HasTablePrefix;
    /**
     * The table associated with the model.
     */
    protected $table = 'minecraft_accounts';

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'user_id',
        'edition',
        'auth_mode',
        'minecraft_uuid',
        'minecraft_username',
        'rank',
        'rank_display',
        'rank_type',
        'rank_expires_at',
        'kingdom',
        'kingdom_display',
        'level',
        'xp',
        'required_xp',
        'level_title',
        'active_title',
        'balance_rupiah',
        'balance_diamond',
        'battlepass_tier',
        'battlepass_xp',
        'battlepass_required_xp',
        'battlepass_has_premium',
        'battlepass_pass_name',
        'apex_coins',
        'unlocked_titles',
        'floodgate_uuid',
        'verification_code',
        'verification_expires_at',
        'verified_at',
        'last_seen_at',
        'last_daily_reward_at',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'level' => 'integer',
        'xp' => 'integer',
        'required_xp' => 'integer',
        'balance_rupiah' => 'double',
        'balance_diamond' => 'double',
        'battlepass_tier' => 'integer',
        'battlepass_xp' => 'integer',
        'battlepass_required_xp' => 'integer',
        'battlepass_has_premium' => 'boolean',
        'apex_coins' => 'integer',
        'unlocked_titles' => 'array',
        'rank_expires_at' => 'datetime',
        'verification_expires_at' => 'datetime',
        'verified_at' => 'datetime',
        'last_seen_at' => 'datetime',
        'last_daily_reward_at' => 'datetime',
    ];

    /**
     * The user that owns this linked account.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Rank purchases history.
     */
    public function rankPurchases()
    {
        return $this->hasMany(RankPurchase::class, 'minecraft_account_id');
    }

    /**
     * Claimed rank rewards.
     */
    public function rankRewardClaims()
    {
        return $this->hasMany(RankRewardClaim::class, 'minecraft_uuid', 'minecraft_uuid');
    }

    /**
     * Check if account has an active trial rank.
     */
    public function isTrialRank(): bool
    {
        return strtoupper($this->rank_type ?? '') === 'TRIAL';
    }

    /**
     * Check if account has a permanent rank.
     */
    public function isPermanentRank(): bool
    {
        return strtoupper($this->rank_type ?? 'PERMANENT') === 'PERMANENT';
    }

    /**
     * Check if account has expired trial rank.
     */
    public function isRankExpired(): bool
    {
        if ($this->isPermanentRank()) {
            return false;
        }

        return $this->rank_expires_at && $this->rank_expires_at->isPast();
    }

    /**
     * Alias for isRankExpired.
     */
    public function isTrialExpired(): bool
    {
        return $this->isRankExpired();
    }

    /**
     * Check if account is verified.
     */
    public function isVerified(): bool
    {
        return $this->verified_at !== null;
    }

    /**
     * Accessor for edition: auto-detects Bedrock if username prefix or UUID indicates Bedrock.
     */
    public function getEditionAttribute($value): string
    {
        if ($this->isBedrock()) {
            return 'BEDROCK';
        }
        return $value ? strtoupper($value) : 'JAVA';
    }

    /**
     * Accessor for auth_mode: auto-detects Bedrock Floodgate if applicable.
     */
    public function getAuthModeAttribute($value): string
    {
        if ($this->isBedrock()) {
            return 'BEDROCK_FLOODGATE';
        }
        return $value ? strtoupper($value) : 'JAVA_ONLINE';
    }

    /**
     * Check if this is a Bedrock Floodgate account.
     */
    public function isBedrock(): bool
    {
        $rawEdition = strtoupper($this->attributes['edition'] ?? '');
        $rawAuthMode = strtoupper($this->attributes['auth_mode'] ?? '');
        $username = (string) ($this->minecraft_username ?? '');
        $uuid = (string) ($this->minecraft_uuid ?? '');

        return $rawEdition === 'BEDROCK'
            || $rawAuthMode === 'BEDROCK_FLOODGATE'
            || !empty($this->attributes['floodgate_uuid'])
            || str_starts_with($username, '.')
            || str_starts_with($username, '*')
            || str_starts_with($uuid, '00000000-0000-0000-');
    }
}
