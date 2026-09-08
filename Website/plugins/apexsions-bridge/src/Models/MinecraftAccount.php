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
     * Check if account is verified.
     */
    public function isVerified(): bool
    {
        return $this->verified_at !== null;
    }

    /**
     * Check if this is a Bedrock Floodgate account.
     */
    public function isBedrock(): bool
    {
        return $this->edition === 'BEDROCK' || $this->auth_mode === 'BEDROCK_FLOODGATE';
    }
}
