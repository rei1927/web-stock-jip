<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Unit extends Model
{
    protected $fillable = [
        'cluster_id',
        'name',
        'block',
        'selling_price',
        'unit_details',
        'status',
    ];

    protected $casts = [
        'unit_details' => 'array',
    ];

    public function cluster()
    {
        return $this->belongsTo(Cluster::class);
    }

    public function transactions()
    {
        return $this->hasMany(Transaction::class);
    }
}
