<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

/**
 * Sovereign land claim model representing protected territory chunks in Minecraft.
 *
 * @property int $id
 * @property string $claim_id
 * @property string $owner_uuid
 * @property string $owner_name
 * @property string $world
 * @property int $chunk_x
 * @property int $chunk_z
 * @property int $trusted_count
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
        'in_game_created_at',
    ];

    protected $casts = [
        'chunk_x' => 'integer',
        'chunk_z' => 'integer',
        'trusted_count' => 'integer',
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

    public function getCenterBlockXAttribute(): int
    {
        return ($this->chunk_x * 16) + 8;
    }

    public function getCenterBlockZAttribute(): int
    {
        return ($this->chunk_z * 16) + 8;
    }

    public function getBlueMapUrlAttribute(): string
    {
        return "/map/#{$this->world}:{$this->center_block_x}:64:{$this->center_block_z}:500:0:0:0:0:perspective";
    }
}
