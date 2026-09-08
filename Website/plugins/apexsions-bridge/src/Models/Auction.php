<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class Auction extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_auctions';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'auction_id',
        'seller_uuid',
        'seller_name',
        'currency',
        'price',
        'item_name',
        'item_data',
        'item_lore',
        'status',
        'buyer_uuid',
        'buyer_name',
        'quarantined_by',
        'quarantine_reason',
        'quarantined_at',
        'expires_at',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'price' => 'double',
        'item_lore' => 'array',
        'quarantined_at' => 'datetime',
        'expires_at' => 'datetime',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
    ];

    /**
     * Scope search query across item name, seller name, or auction ID.
     */
    public function scopeSearch(Builder $query, string $search): Builder
    {
        $term = trim($search);
        if (empty($term)) {
            return $query;
        }

        return $query->where(function (Builder $q) use ($term) {
            $q->where('auction_id', 'like', "%{$term}%")
              ->orWhere('item_name', 'like', "%{$term}%")
              ->orWhere('seller_name', 'like', "%{$term}%")
              ->orWhere('seller_uuid', 'like', "%{$term}%")
              ->orWhere('buyer_name', 'like', "%{$term}%");
        });
    }

    /**
     * Scope filter status.
     */
    public function scopeFilterStatus(Builder $query, string $status): Builder
    {
        return $status !== 'all' ? $query->where('status', strtoupper($status)) : $query;
    }

    /**
     * Scope filter currency.
     */
    public function scopeFilterCurrency(Builder $query, string $currency): Builder
    {
        return $currency !== 'all' ? $query->where('currency', strtolower($currency)) : $query;
    }

    public function isActive(): bool
    {
        return $this->status === 'ACTIVE' && ($this->expires_at === null || $this->expires_at->isFuture());
    }

    public function isQuarantined(): bool
    {
        return $this->status === 'QUARANTINED';
    }

    public function isExpired(): bool
    {
        return $this->status === 'EXPIRED' || ($this->expires_at !== null && $this->expires_at->isPast() && $this->status === 'ACTIVE');
    }

    /**
     * Link to seller account.
     */
    public function sellerAccount(): HasOne
    {
        return $this->hasOne(MinecraftAccount::class, 'minecraft_uuid', 'seller_uuid');
    }

    /**
     * Link to buyer account.
     */
    public function buyerAccount(): HasOne
    {
        return $this->hasOne(MinecraftAccount::class, 'minecraft_uuid', 'buyer_uuid');
    }
}
