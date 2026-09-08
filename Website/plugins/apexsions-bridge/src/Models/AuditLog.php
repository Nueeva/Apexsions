<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class AuditLog extends Model
{
    use HasTablePrefix;

    /**
     * The table associated with the model.
     */
    protected $table = 'apexsions_audit_logs';

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'actor_type',
        'actor_id',
        'actor_name',
        'action',
        'target_type',
        'target_id',
        'target_name',
        'old_value',
        'new_value',
        'reason',
        'source',
        'status',
        'metadata',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'metadata' => 'array',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
    ];

    /**
     * Scope query to search across actor, target, reason, or action.
     */
    public function scopeSearch(Builder $query, ?string $search): Builder
    {
        if (empty($search)) {
            return $query;
        }

        $term = '%' . trim($search) . '%';
        return $query->where(function (Builder $q) use ($term) {
            $q->where('actor_name', 'LIKE', $term)
              ->orWhere('target_name', 'LIKE', $term)
              ->orWhere('target_id', 'LIKE', $term)
              ->orWhere('action', 'LIKE', $term)
              ->orWhere('reason', 'LIKE', $term);
        });
    }

    /**
     * Scope query by action category or exact action.
     */
    public function scopeFilterAction(Builder $query, ?string $action): Builder
    {
        if (!empty($action) && $action !== 'all') {
            return $query->where('action', $action);
        }
        return $query;
    }

    /**
     * Scope query by source (WEB, API, INGAME, SYSTEM).
     */
    public function scopeFilterSource(Builder $query, ?string $source): Builder
    {
        if (!empty($source) && $source !== 'all') {
            return $query->where('source', strtoupper($source));
        }
        return $query;
    }

    /**
     * Scope query by status (PENDING, SUCCESS, FAILED).
     */
    public function scopeFilterStatus(Builder $query, ?string $status): Builder
    {
        if (!empty($status) && $status !== 'all') {
            return $query->where('status', strtoupper($status));
        }
        return $query;
    }

    /**
     * Scope query by date range.
     */
    public function scopeFilterDate(Builder $query, ?string $from, ?string $to): Builder
    {
        if (!empty($from)) {
            $query->whereDate('created_at', '>=', $from);
        }
        if (!empty($to)) {
            $query->whereDate('created_at', '<=', $to);
        }
        return $query;
    }

    /**
     * Get action_id from metadata if present.
     */
    public function getActionIdAttribute(): ?string
    {
        return $this->metadata['action_id'] ?? null;
    }
}
