<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class Transaction extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_transactions';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'transaction_id',
        'type',
        'sender_uuid',
        'sender_name',
        'receiver_uuid',
        'receiver_name',
        'currency',
        'amount',
        'tax_amount',
        'net_amount',
        'reason',
        'status',
        'source',
        'action_id',
        'metadata',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'amount' => 'double',
        'tax_amount' => 'double',
        'net_amount' => 'double',
        'metadata' => 'array',
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
    ];

    /**
     * Scope a query to search transactions across identifiers and text.
     */
    public function scopeSearch(Builder $query, string $search): Builder
    {
        $term = trim($search);
        if (empty($term)) {
            return $query;
        }

        return $query->where(function (Builder $q) use ($term) {
            $q->where('transaction_id', 'like', "%{$term}%")
              ->orWhere('sender_name', 'like', "%{$term}%")
              ->orWhere('receiver_name', 'like', "%{$term}%")
              ->orWhere('sender_uuid', 'like', "%{$term}%")
              ->orWhere('receiver_uuid', 'like', "%{$term}%")
              ->orWhere('reason', 'like', "%{$term}%")
              ->orWhere('action_id', 'like', "%{$term}%");
        });
    }

    /**
     * Scope a query to filter by transaction type.
     */
    public function scopeFilterType(Builder $query, string $type): Builder
    {
        return $type !== 'all' ? $query->where('type', strtoupper($type)) : $query;
    }

    /**
     * Scope a query to filter by currency.
     */
    public function scopeFilterCurrency(Builder $query, string $currency): Builder
    {
        return $currency !== 'all' ? $query->where('currency', strtolower($currency)) : $query;
    }

    /**
     * Scope a query to filter by status.
     */
    public function scopeFilterStatus(Builder $query, string $status): Builder
    {
        return $status !== 'all' ? $query->where('status', strtoupper($status)) : $query;
    }

    /**
     * Scope a query to filter by date range.
     */
    public function scopeDateRange(Builder $query, ?string $from, ?string $to): Builder
    {
        if (!empty($from)) {
            $query->where('created_at', '>=', $from . ' 00:00:00');
        }
        if (!empty($to)) {
            $query->where('created_at', '<=', $to . ' 23:59:59');
        }
        return $query;
    }

    /**
     * Scope a query to filter by amount range.
     */
    public function scopeAmountRange(Builder $query, ?float $min, ?float $max): Builder
    {
        if ($min !== null && $min >= 0) {
            $query->where('amount', '>=', $min);
        }
        if ($max !== null && $max > 0) {
            $query->where('amount', '<=', $max);
        }
        return $query;
    }

    /**
     * Link to sender Minecraft Account if available.
     */
    public function senderAccount(): HasOne
    {
        return $this->hasOne(MinecraftAccount::class, 'minecraft_uuid', 'sender_uuid');
    }

    /**
     * Link to receiver Minecraft Account if available.
     */
    public function receiverAccount(): HasOne
    {
        return $this->hasOne(MinecraftAccount::class, 'minecraft_uuid', 'receiver_uuid');
    }

    /**
     * Link to associated administrative audit log if action was admin-initiated.
     */
    public function auditLog(): HasOne
    {
        return $this->hasOne(AuditLog::class, 'action_id', 'action_id');
    }
}
