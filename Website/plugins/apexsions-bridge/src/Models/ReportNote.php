<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Azuriom\Models\User;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\SoftDeletes;

class ReportNote extends Model
{
    use SoftDeletes;

    /**
     * The table associated with the model.
     */
    protected $table = 'apexsions_report_notes';

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'report_id',
        'author_id',
        'author_name',
        'note',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'report_id' => 'integer',
        'author_id' => 'integer',
    ];

    /**
     * Report to which this note belongs.
     */
    public function report(): BelongsTo
    {
        return $this->belongsTo(Report::class, 'report_id');
    }

    /**
     * User who authored this note.
     */
    public function author(): BelongsTo
    {
        return $this->belongsTo(User::class, 'author_id');
    }
}
