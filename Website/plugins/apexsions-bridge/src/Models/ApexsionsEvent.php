<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ApexsionsEvent extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_events';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'event_id',
        'event_type',
        'source',
        'entity_type',
        'entity_id',
        'actor_type',
        'actor_id',
        'actor_name',
        'target_type',
        'target_id',
        'target_name',
        'severity',
        'correlation_id',
        'action_id',
        'incident_id',
        'metadata',
        'occurred_at',
        'received_at',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'metadata' => 'array',
        'occurred_at' => 'datetime',
        'received_at' => 'datetime',
    ];

    /**
     * Associated incident if correlated.
     */
    public function incident(): BelongsTo
    {
        return $this->belongsTo(ApexsionsIncident::class, 'incident_id', 'incident_id');
    }

    /**
     * Scope query to recent events.
     */
    public function scopeRecent(Builder $query, int $hours = 24): Builder
    {
        return $query->where('occurred_at', '>=', Carbon::now()->subHours($hours))
                     ->orderBy('occurred_at', 'desc');
    }

    /**
     * Scope filter by event type.
     */
    public function scopeByType(Builder $query, string $type): Builder
    {
        return $query->where('event_type', strtoupper($type));
    }

    /**
     * Scope filter by severity.
     */
    public function scopeBySeverity(Builder $query, string $severity): Builder
    {
        return $query->where('severity', strtoupper($severity));
    }

    /**
     * Scope filter by correlation identifier.
     */
    public function scopeByCorrelation(Builder $query, string $correlationId): Builder
    {
        return $query->where('correlation_id', $correlationId)
                     ->orWhere('action_id', $correlationId);
    }

    /**
     * Badge CSS class for event severity.
     */
    public function getSeverityBadgeAttribute(): string
    {
        return match ($this->severity) {
            'CRITICAL' => 'bg-danger text-white',
            'HIGH' => 'bg-warning text-dark',
            'MEDIUM' => 'bg-primary text-white',
            'LOW' => 'bg-info text-white',
            default => 'bg-secondary text-white',
        };
    }

    /**
     * Badge CSS class for event source tier.
     */
    public function getSourceBadgeAttribute(): string
    {
        return match ($this->source) {
            'MINECRAFT' => 'badge-success bg-success text-white',
            'PLUGIN' => 'badge-info bg-info text-white',
            'WEB' => 'badge-primary bg-primary text-white',
            'BRIDGE' => 'badge-warning bg-warning text-dark',
            default => 'badge-secondary bg-secondary text-white',
        };
    }
}
