<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Model;

class Delivery extends Model
{
    use HasTablePrefix;
    /**
     * The table associated with the model.
     */
    protected $table = 'deliveries';

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'order_item_id',
        'server_id',
        'player_uuid',
        'player_username',
        'command',
        'status',
        'executed_at',
        'error_message',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'executed_at' => 'datetime',
    ];
}
