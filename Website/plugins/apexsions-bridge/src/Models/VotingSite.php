<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\Traits\HasTablePrefix;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class VotingSite extends Model
{
    use HasTablePrefix;

    protected $table = 'apexsions_voting_sites';

    protected $fillable = [
        'name',
        'slug',
        'vote_url',
        'server_id',
        'api_key',
        'cooldown_hours',
        'is_active',
    ];

    protected $casts = [
        'cooldown_hours' => 'integer',
        'is_active' => 'boolean',
    ];

    public function transactions(): HasMany
    {
        return $this->hasMany(VoteTransaction::class, 'site_id');
    }
}
