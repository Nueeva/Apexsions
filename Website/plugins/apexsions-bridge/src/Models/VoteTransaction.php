<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class VoteTransaction extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_vote_transactions';

    protected $fillable = [
        'vote_uuid',
        'external_vote_id',
        'site_id',
        'site_slug',
        'player_uuid',
        'player_username',
        'ip_address',
        'idempotency_hash',
        'voted_at',
        'verified_at',
        'vote_status',
        'external_status',
        'reward_status',
        'keys_amount',
        'money_amount',
        'keys_delivery_id',
        'money_delivery_id',
        'rewarded_at',
        'failure_reason',
        'retry_count',
    ];

    protected $casts = [
        'voted_at' => 'datetime',
        'verified_at' => 'datetime',
        'rewarded_at' => 'datetime',
        'keys_amount' => 'integer',
        'money_amount' => 'double',
        'retry_count' => 'integer',
    ];

    public function site(): BelongsTo
    {
        return $this->belongsTo(VotingSite::class, 'site_id');
    }

    public function keysDelivery(): BelongsTo
    {
        return $this->belongsTo(Delivery::class, 'keys_delivery_id');
    }

    public function moneyDelivery(): BelongsTo
    {
        return $this->belongsTo(Delivery::class, 'money_delivery_id');
    }
}
