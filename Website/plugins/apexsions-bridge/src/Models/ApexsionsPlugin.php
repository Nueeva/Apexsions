<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class ApexsionsPlugin extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_plugins';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'plugin_id',
        'name',
        'version',
        'type',
        'status',
        'integration_status',
        'health_status',
        'description',
        'dependencies',
        'metadata',
        'last_heartbeat_at',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'dependencies' => 'array',
        'metadata' => 'array',
        'last_heartbeat_at' => 'datetime',
    ];

    /**
     * Get all declared capabilities for this plugin.
     */
    public function capabilities(): HasMany
    {
        return $this->hasMany(PluginCapability::class, 'plugin_id', 'plugin_id');
    }

    /**
     * Get recent health history snapshots for this plugin.
     */
    public function healthHistory(): HasMany
    {
        return $this->hasMany(PluginHealthRecord::class, 'plugin_id', 'plugin_id')->orderBy('created_at', 'desc');
    }

    /**
     * Scope to active/enabled plugins.
     */
    public function scopeEnabled(Builder $query): Builder
    {
        return $query->where('status', 'ENABLED');
    }

    /**
     * Scope filter by type.
     */
    public function scopeByType(Builder $query, string $type): Builder
    {
        return $query->where('type', strtoupper($type));
    }

    /**
     * Scope filter by integration status.
     */
    public function scopeByIntegration(Builder $query, string $status): Builder
    {
        return $query->where('integration_status', strtoupper($status));
    }

    /**
     * Check if the plugin is currently operational and healthy.
     */
    public function isHealthy(): bool
    {
        return $this->status === 'ENABLED' && $this->health_status === 'HEALTHY';
    }

    /**
     * Badge CSS class for operational health.
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

    /**
     * Badge CSS class for integration tier.
     */
    public function getIntegrationBadgeAttribute(): string
    {
        return match ($this->integration_status) {
            'WEB_READY' => 'badge-primary bg-primary text-white',
            'PARTIAL' => 'badge-info bg-info text-white',
            'MINECRAFT_ONLY' => 'badge-secondary bg-secondary text-white',
            default => 'badge-dark bg-dark text-white',
        };
    }

    /**
     * Badge CSS class for architectural domain type.
     */
    public function getTypeBadgeAttribute(): string
    {
        return match ($this->type) {
            'CORE' => 'bg-danger text-white',
            'ECONOMY' => 'bg-warning text-dark',
            'CHAT' => 'bg-info text-white',
            'GAMEPLAY' => 'bg-success text-white',
            'COMBAT' => 'bg-danger text-white',
            'COSMETIC' => 'bg-purple text-white',
            default => 'bg-secondary text-white',
        };
    }
}
