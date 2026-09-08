<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class IntelligenceRule extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_intelligence_rules';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'rule_id',
        'name',
        'category',
        'description',
        'severity',
        'condition_schema',
        'cooldown_minutes',
        'is_enabled',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'condition_schema' => 'array',
        'cooldown_minutes' => 'integer',
        'is_enabled' => 'boolean',
    ];

    /**
     * Scope query to active enabled rules.
     */
    public function scopeEnabled(Builder $query): Builder
    {
        return $query->where('is_enabled', true);
    }

    /**
     * Scope query by category.
     */
    public function scopeByCategory(Builder $query, string $category): Builder
    {
        return $query->where('category', strtoupper($category));
    }

    /**
     * Badge CSS class for rule severity.
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
}
