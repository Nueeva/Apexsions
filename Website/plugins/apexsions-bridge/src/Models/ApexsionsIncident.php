<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class ApexsionsIncident extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_incidents';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'incident_id',
        'title',
        'type',
        'severity',
        'status',
        'source',
        'detected_at',
        'resolved_at',
        'assigned_to',
        'assigned_at',
        'root_entity_type',
        'root_entity_id',
        'root_entity_name',
        'correlation_summary',
        'occurrence_count',
        'last_occurred_at',
        'metadata',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'metadata' => 'array',
        'detected_at' => 'datetime',
        'resolved_at' => 'datetime',
        'assigned_at' => 'datetime',
        'last_occurred_at' => 'datetime',
    ];

    /**
     * Correlated timeline events associated with this incident.
     */
    public function events(): HasMany
    {
        return $this->hasMany(ApexsionsEvent::class, 'incident_id', 'incident_id')->orderBy('occurred_at', 'asc');
    }

    /**
     * Staff internal investigation notes.
     */
    public function notes(): HasMany
    {
        return $this->hasMany(IncidentNote::class, 'incident_id', 'incident_id')->orderBy('created_at', 'desc');
    }

    /**
     * Scope query to active unresolved incidents.
     */
    public function scopeOpen(Builder $query): Builder
    {
        return $query->whereIn('status', ['OPEN', 'INVESTIGATING', 'MITIGATED']);
    }

    /**
     * Scope query to resolved incidents.
     */
    public function scopeResolved(Builder $query): Builder
    {
        return $query->whereIn('status', ['RESOLVED', 'CLOSED']);
    }

    /**
     * Check if incident is currently open.
     */
    public function isOpen(): bool
    {
        return in_array($this->status, ['OPEN', 'INVESTIGATING', 'MITIGATED']);
    }

    /**
     * Badge CSS class for incident severity.
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
     * Badge CSS class for incident lifecycle status.
     */
    public function getStatusBadgeAttribute(): string
    {
        return match ($this->status) {
            'OPEN' => 'bg-danger text-white',
            'INVESTIGATING' => 'bg-warning text-dark',
            'MITIGATED' => 'bg-info text-white',
            'RESOLVED' => 'bg-success text-white',
            'CLOSED' => 'bg-secondary text-white',
            default => 'bg-secondary text-white',
        };
    }
}
