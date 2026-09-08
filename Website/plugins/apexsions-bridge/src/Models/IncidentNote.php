<?php

namespace Azuriom\Plugin\ApexsionsBridge\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class IncidentNote extends Model
{
    /**
     * The table associated with the model.
     *
     * @var string
     */
    protected $table = 'apexsions_incident_notes';

    /**
     * The attributes that are mass assignable.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'incident_id',
        'staff_id',
        'staff_name',
        'content',
        'related_event_id',
    ];

    /**
     * Parent incident owning this note.
     */
    public function incident(): BelongsTo
    {
        return $this->belongsTo(ApexsionsIncident::class, 'incident_id', 'incident_id');
    }
}
