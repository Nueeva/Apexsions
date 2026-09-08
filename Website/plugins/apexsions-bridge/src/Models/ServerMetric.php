<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class ServerMetric extends Model
{
    /**
     * Disable updated_at column since metric snapshots are immutable.
     */
    const UPDATED_AT = null;

    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_server_metrics';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'tps',
        'mspt',
        'cpu_usage',
        'ram_used_mb',
        'ram_max_mb',
        'disk_used_gb',
        'disk_total_gb',
        'online_players',
        'max_players',
        'loaded_chunks',
        'entities',
        'server_status',
        'created_at',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'tps' => 'float',
        'mspt' => 'float',
        'cpu_usage' => 'float',
        'disk_used_gb' => 'float',
        'disk_total_gb' => 'float',
        'created_at' => 'datetime',
    ];

    /**
     * Scope metrics to the last given hours.
     */
    public function scopeRecent(Builder $query, int $hours = 24): Builder
    {
        return $query->where('created_at', '>=', Carbon::now()->subHours($hours))
            ->orderBy('created_at', 'asc');
    }

    /**
     * Scope to the latest recorded snapshot.
     */
    public function scopeLatestSnapshot(Builder $query): Builder
    {
        return $query->orderBy('created_at', 'desc');
    }
}
