<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Model;
use Azuriom\Models\User;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class MinecraftAccount extends Model
{
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
        'floodgate_uuid',
        'verification_code',
        'verification_expires_at',
        'verified_at',
        'last_seen_at',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'verification_expires_at' => 'datetime',
        'verified_at' => 'datetime',
        'last_seen_at' => 'datetime',
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
