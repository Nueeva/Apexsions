<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

/**
 * Sovereign land claim model representing protected territory chunks in Minecraft,
 * including progressive tax upkeep, grace periods, citizen roles, and flags.
 *
 * @property int $id
 * @property string $claim_id
 * @property string $owner_uuid
 * @property string $owner_name
 * @property string $world
 * @property int $chunk_x
 * @property int $chunk_z
 * @property int $trusted_count
 * @property float $bank_balance
 * @property float $daily_upkeep
 * @property string $status
 * @property \Carbon\Carbon|null $grace_period_until
 * @property \Carbon\Carbon|null $last_tax_collected_at
 * @property string|null $kingdom_id
 * @property array|null $flags
 * @property array|null $roles
 * @property \Carbon\Carbon|null $in_game_created_at
 * @property \Carbon\Carbon $created_at
 * @property \Carbon\Carbon $updated_at
 */
class Claim extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_claims';

    protected $fillable = [
        'claim_id',
        'owner_uuid',
        'owner_name',
        'world',
        'chunk_x',
        'chunk_z',
        'trusted_count',
        'bank_balance',
        'daily_upkeep',
        'status',
        'grace_period_until',
        'last_tax_collected_at',
        'kingdom_id',
        'flags',
        'roles',
        'in_game_created_at',
    ];

    protected $casts = [
        'chunk_x' => 'integer',
        'chunk_z' => 'integer',
        'trusted_count' => 'integer',
        'bank_balance' => 'float',
        'daily_upkeep' => 'float',
        'grace_period_until' => 'datetime',
        'last_tax_collected_at' => 'datetime',
        'flags' => 'array',
        'roles' => 'array',
        'in_game_created_at' => 'datetime',
    ];

    public function account()
    {
        return $this->belongsTo(MinecraftAccount::class, 'owner_uuid', 'minecraft_uuid');
    }

    public function scopeForWorld(Builder $query, string $world): Builder
    {
        return $query->where('world', $world);
    }

    public function scopeForOwner(Builder $query, string $identifier): Builder
    {
        return $query->where('owner_uuid', $identifier)
                     ->orWhere('owner_name', $identifier);
    }

    public function scopeActive(Builder $query): Builder
    {
        return $query->where('status', 'ACTIVE');
    }

    public function scopeInGracePeriod(Builder $query): Builder
    {
        return $query->where('status', 'GRACE_PERIOD');
    }

    public function scopeByKingdom(Builder $query, string $kingdom): Builder
    {
        return $query->where('kingdom_id', $kingdom);
    }

    public function isInGracePeriod(): bool
    {
        return $this->status === 'GRACE_PERIOD' && ($this->grace_period_until === null || $this->grace_period_until->isFuture());
    }

    public function isGracePeriodExpired(): bool
    {
        return $this->status === 'GRACE_PERIOD' && $this->grace_period_until !== null && $this->grace_period_until->isPast();
    }

    public function getDaysRemainingAttribute(): float
    {
        if ($this->daily_upkeep <= 0) {
            return 999.0;
        }
        return round($this->bank_balance / $this->daily_upkeep, 1);
    }

    public function getStatusBadgeAttribute(): string
    {
        if ($this->isInGracePeriod()) {
            return '<span class="badge bg-danger"><i class="bi bi-exclamation-triangle me-1"></i>Menunggak Pajak</span>';
        }
        if ($this->status === 'EXPIRED' || $this->isGracePeriodExpired()) {
            return '<span class="badge bg-dark text-danger"><i class="bi bi-x-circle me-1"></i>Kedaluwarsa</span>';
        }
        return '<span class="badge bg-success"><i class="bi bi-shield-check me-1"></i>Lunas & Aktif</span>';
    }

    /**
     * Get center block X coordinate of chunk.
     */
    public function getBlockCenterX(): int
    {
        return ($this->chunk_x << 4) + 8;
    }

    /**
     * Get center block Z coordinate of chunk.
     */
    public function getBlockCenterZ(): int
    {
        return ($this->chunk_z << 4) + 8;
    }

    /**
     * Generate BlueMap live map URL for this claimed territory.
     */
    public function getBlueMapUrl(): string
    {
        $x = $this->getBlockCenterX();
        $z = $this->getBlockCenterZ();
        return "http://map.apexsions.my.id/#{$this->world}:{$x}:100:{$z}:500:0:0:0:0:perspective";
    }
}
