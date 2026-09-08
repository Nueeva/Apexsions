<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class NotificationDelivery extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_notification_deliveries';

    protected $fillable = [
        'delivery_id',
        'notification_id',
        'channel',
        'status',
        'attempt_count',
        'max_attempts',
        'last_attempt_at',
        'next_retry_at',
        'error_summary',
        'payload',
    ];

    protected $casts = [
        'payload' => 'array',
        'last_attempt_at' => 'datetime',
        'next_retry_at' => 'datetime',
        'attempt_count' => 'integer',
        'max_attempts' => 'integer',
    ];

    public function notification(): BelongsTo
    {
        return $this->belongsTo(ApexsionsNotification::class, 'notification_id', 'notification_id');
    }
}
