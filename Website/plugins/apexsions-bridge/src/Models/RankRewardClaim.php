<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Model;

class RankRewardClaim extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_rank_rewards_claimed';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'minecraft_uuid',
        'minecraft_username',
        'rank',
        'reward_type',
        'amount',
        'transaction_reference',
        'claimed_at',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'amount' => 'double',
        'claimed_at' => 'datetime',
    ];
}
