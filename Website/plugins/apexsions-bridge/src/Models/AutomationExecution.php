<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class AutomationExecution extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_automation_executions';

    protected $fillable = [
        'execution_id',
        'policy_id',
        'trigger_event_id',
        'trigger_incident_id',
        'action_type',
        'status',
        'approved_by',
        'approved_at',
        'rejection_reason',
        'result_summary',
        'execution_depth',
        'metadata',
    ];

    protected $casts = [
        'metadata' => 'array',
        'approved_at' => 'datetime',
        'execution_depth' => 'integer',
    ];

    public function policy(): BelongsTo
    {
        return $this->belongsTo(AutomationPolicy::class, 'policy_id', 'policy_id');
    }
}
