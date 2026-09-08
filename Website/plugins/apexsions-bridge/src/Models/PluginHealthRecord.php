<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PluginHealthRecord extends Model
{
    /**
     * Disable updated_at column since snapshots are immutable logs.
     */
    const UPDATED_AT = null;

    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_plugin_health_history';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'plugin_id',
        'status',
        'health_status',
        'metrics',
        'error_category',
        'error_summary',
        'created_at',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'metrics' => 'array',
        'created_at' => 'datetime',
    ];

    /**
     * Parent plugin owning this record.
     */
    public function plugin(): BelongsTo
    {
        return $this->belongsTo(ApexsionsPlugin::class, 'plugin_id', 'plugin_id');
    }

    /**
     * Badge CSS class for recorded health status.
     */
    public function getHealthBadgeAttribute(): string
    {
        return match ($this->health_status) {
            'HEALTHY' => 'bg-success text-white',
            'DEGRADED' => 'bg-warning text-dark',
            'ERROR' => 'bg-danger text-white',
            default => 'bg-secondary text-white',
        };
    }
}
