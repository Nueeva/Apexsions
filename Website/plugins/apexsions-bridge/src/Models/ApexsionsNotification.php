<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class ApexsionsNotification extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_notifications';

    protected $fillable = [
        'notification_id',
        'dedup_key',
        'type',
        'severity',
        'title',
        'message',
        'source',
        'entity_type',
        'entity_id',
        'event_id',
        'incident_id',
        'action_id',
        'correlation_id',
        'occurrence_count',
        'last_occurred_at',
        'status',
        'acknowledged_by',
        'acknowledged_at',
        'metadata',
    ];

    protected $casts = [
        'metadata' => 'array',
        'last_occurred_at' => 'datetime',
        'acknowledged_at' => 'datetime',
        'occurrence_count' => 'integer',
    ];

    public function deliveries(): HasMany
    {
        return $this->hasMany(NotificationDelivery::class, 'notification_id', 'notification_id');
    }

    public function incident(): BelongsTo
    {
        return $this->belongsTo(ApexsionsIncident::class, 'incident_id', 'incident_id');
    }

    public function event(): BelongsTo
    {
        return $this->belongsTo(ApexsionsEvent::class, 'event_id', 'event_id');
    }

    public function scopeUnacknowledged(Builder $query): Builder
    {
        return $query->where('status', 'UNACKNOWLEDGED');
    }

    public function scopeBySeverity(Builder $query, string $severity): Builder
    {
        return $query->where('severity', strtoupper($severity));
    }
}
