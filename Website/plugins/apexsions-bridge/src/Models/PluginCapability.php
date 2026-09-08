<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PluginCapability extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_plugin_capabilities';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'plugin_id',
        'capability_id',
        'name',
        'type',
        'access',
        'status',
        'description',
        'requires_reason',
        'requires_confirmation',
        'input_schema',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'requires_reason' => 'boolean',
        'requires_confirmation' => 'boolean',
        'input_schema' => 'array',
    ];

    /**
     * Parent plugin owning this capability.
     */
    public function plugin(): BelongsTo
    {
        return $this->belongsTo(ApexsionsPlugin::class, 'plugin_id', 'plugin_id');
    }

    /**
     * Scope to available capabilities.
     */
    public function scopeAvailable(Builder $query): Builder
    {
        return $query->where('status', 'AVAILABLE');
    }

    /**
     * Scope filter by type (READ, WRITE, ACTION, EVENT, METRIC).
     */
    public function scopeByType(Builder $query, string $type): Builder
    {
        return $query->where('type', strtoupper($type));
    }

    /**
     * Scope filter by access tier (PUBLIC_INTERNAL, ADMIN, SYSTEM).
     */
    public function scopeByAccess(Builder $query, string $access): Builder
    {
        return $query->where('access', strtoupper($access));
    }

    /**
     * Check if capability is currently available.
     */
    public function isAvailable(): bool
    {
        return $this->status === 'AVAILABLE';
    }

    /**
     * Badge CSS class for capability type.
     */
    public function getTypeBadgeAttribute(): string
    {
        return match ($this->type) {
            'READ' => 'bg-info text-white',
            'WRITE' => 'bg-warning text-dark',
            'ACTION' => 'bg-danger text-white',
            'EVENT' => 'bg-primary text-white',
            'METRIC' => 'bg-success text-white',
            default => 'bg-secondary text-white',
        };
    }

    /**
     * Badge CSS class for access level.
     */
    public function getAccessBadgeAttribute(): string
    {
        return match ($this->access) {
            'ADMIN' => 'border border-danger text-danger',
            'SYSTEM' => 'border border-warning text-warning',
            'PUBLIC_INTERNAL' => 'border border-info text-info',
            default => 'border border-secondary text-secondary',
        };
    }
}
