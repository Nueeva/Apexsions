<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class AutomationPolicy extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_automation_policies';

    protected $fillable = [
        'policy_id',
        'name',
        'trigger_type',
        'condition_schema',
        'action_type',
        'action_payload',
        'severity',
        'cooldown_minutes',
        'approval_required',
        'is_enabled',
    ];

    protected $casts = [
        'condition_schema' => 'array',
        'action_payload' => 'array',
        'cooldown_minutes' => 'integer',
        'approval_required' => 'boolean',
        'is_enabled' => 'boolean',
    ];

    public function executions(): HasMany
    {
        return $this->hasMany(AutomationExecution::class, 'policy_id', 'policy_id');
    }

    public function scopeEnabled(Builder $query): Builder
    {
        return $query->where('is_enabled', true);
    }
}
