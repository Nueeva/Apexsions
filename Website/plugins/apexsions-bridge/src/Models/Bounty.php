<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

/**
 * Web snapshot of an active in-game player bounty.
 *
 * This is a read-model mirror of the Minecraft server's apexsions_bounties
 * aggregate (one row per hunted target), refreshed by the game via the
 * /bounties/sync-all endpoint.
 *
 * @property int $id
 * @property string $target_uuid
 * @property string $target_name
 * @property float $total_amount
 * @property int $contributor_count
 * @property array|null $top_contributors
 * @property \Carbon\Carbon|null $last_synced_at
 */
class Bounty extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_bounties';

    protected $fillable = [
        'target_uuid',
        'target_name',
        'total_amount',
        'contributor_count',
        'top_contributors',
        'last_synced_at',
    ];

    protected $casts = [
        'total_amount' => 'float',
        'contributor_count' => 'integer',
        'top_contributors' => 'array',
        'last_synced_at' => 'datetime',
    ];

    public function scopeTop(Builder $query, int $limit = 50): Builder
    {
        return $query->orderByDesc('total_amount')->limit($limit);
    }

    /**
     * Timestamp of the most recent synchronisation across all bounties.
     */
    public static function lastSyncedAt(): ?\Carbon\Carbon
    {
        $latest = static::query()->max('last_synced_at');

        return $latest ? \Carbon\Carbon::parse($latest) : null;
    }
}
