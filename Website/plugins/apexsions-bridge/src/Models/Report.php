<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\User;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Report extends Model
{
    /**
     * The table associated with the model.
     */
    protected $table = 'apexsions_reports';

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'in_game_report_id',
        'reporter_uuid',
        'reporter_name',
        'reported_uuid',
        'reported_name',
        'reason',
        'description',
        'server',
        'world',
        'status',
        'priority',
        'assigned_staff_id',
        'assigned_staff_name',
        'assigned_at',
        'resolution',
        'resolved_at',
        'metadata',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'in_game_report_id' => 'integer',
        'assigned_staff_id' => 'integer',
        'assigned_at' => 'datetime',
        'resolved_at' => 'datetime',
        'metadata' => 'array',
    ];

    /**
     * Notes associated with this report.
     */
    public function notes(): HasMany
    {
        return $this->hasMany(ReportNote::class, 'report_id')->orderBy('created_at', 'desc');
    }

    /**
     * Staff member assigned to this report.
     */
    public function assignedStaff(): BelongsTo
    {
        return $this->belongsTo(User::class, 'assigned_staff_id');
    }

    /**
     * Reported player's linked MinecraftAccount record.
     */
    public function reportedAccount(): BelongsTo
    {
        return $this->belongsTo(MinecraftAccount::class, 'reported_uuid', 'minecraft_uuid');
    }

    /**
     * Scope query to search by player name, UUID, or reason.
     */
    public function scopeSearch(Builder $query, ?string $search): Builder
    {
        if (empty($search)) {
            return $query;
        }

        $term = '%' . $search . '%';

        return $query->where(function ($q) use ($term) {
            $q->where('reported_name', 'LIKE', $term)
              ->orWhere('reporter_name', 'LIKE', $term)
              ->orWhere('reported_uuid', 'LIKE', $term)
              ->orWhere('reporter_uuid', 'LIKE', $term)
              ->orWhere('reason', 'LIKE', $term)
              ->orWhere('id', is_numeric(trim($term, '%')) ? (int) trim($term, '%') : 0);
        });
    }

    /**
     * Scope query by report status.
     */
    public function scopeFilterStatus(Builder $query, ?string $status): Builder
    {
        if (!empty($status) && $status !== 'all') {
            return $query->where('status', strtoupper($status));
        }

        return $query;
    }

    /**
     * Scope query by report priority.
     */
    public function scopeFilterPriority(Builder $query, ?string $priority): Builder
    {
        if (!empty($priority) && $priority !== 'all') {
            return $query->where('priority', strtoupper($priority));
        }

        return $query;
    }

    /**
     * Check if the report is already claimed by any staff.
     */
    public function isClaimed(): bool
    {
        return !empty($this->assigned_staff_id) || in_array($this->status, ['CLAIMED', 'INVESTIGATING'], true);
    }
}
