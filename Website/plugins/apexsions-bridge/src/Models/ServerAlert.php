<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class ServerAlert extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_server_alerts';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'type',
        'severity',
        'message',
        'status',
        'acknowledged_by',
        'acknowledged_at',
        'resolved_at',
        'metadata',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'metadata' => 'array',
        'acknowledged_at' => 'datetime',
        'resolved_at' => 'datetime',
    ];

    /**
     * Scope for active alerts.
     */
    public function scopeActive(Builder $query): Builder
    {
        return $query->where('status', 'ACTIVE');
    }

    /**
     * Scope for acknowledged alerts.
     */
    public function scopeAcknowledged(Builder $query): Builder
    {
        return $query->where('status', 'ACKNOWLEDGED');
    }

    /**
     * Scope for unresolved alerts (active or acknowledged).
     */
    public function scopeUnresolved(Builder $query): Builder
    {
        return $query->whereIn('status', ['ACTIVE', 'ACKNOWLEDGED']);
    }

    /**
     * Acknowledge this alert.
     */
    public function acknowledge(string $staffName): void
    {
        $this->update([
            'status' => 'ACKNOWLEDGED',
            'acknowledged_by' => $staffName,
            'acknowledged_at' => Carbon::now(),
        ]);
    }

    /**
     * Resolve this alert.
     */
    public function resolve(string $staffName): void
    {
        $this->update([
            'status' => 'RESOLVED',
            'acknowledged_by' => $this->acknowledged_by ?? $staffName,
            'resolved_at' => Carbon::now(),
        ]);
    }
}
