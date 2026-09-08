<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Model;

class MaintenanceState extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_maintenance_state';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'is_enabled',
        'message',
        'reason',
        'enabled_by',
        'enabled_at',
        'allow_staff',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'is_enabled' => 'boolean',
        'allow_staff' => 'boolean',
        'enabled_at' => 'datetime',
    ];

    /**
     * Get the single authoritative maintenance state record.
     */
    public static function current(): self
    {
        return static::firstOrCreate(
            ['id' => 1],
            [
                'is_enabled' => false,
                'message' => 'Server sedang dalam pemeliharaan berkala. Silakan kembali beberapa saat lagi.',
                'allow_staff' => true,
            ]
        );
    }

    /**
     * Determine if maintenance mode is currently active.
     */
    public function isEnabled(): bool
    {
        return (bool) $this->is_enabled;
    }

    /**
     * Determine if staff with bypass permissions can enter.
     */
    public function canStaffBypass(): bool
    {
        return (bool) $this->allow_staff;
    }
}
