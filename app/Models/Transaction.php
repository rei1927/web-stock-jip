<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Transaction extends Model
{
    protected $fillable = [
        'unit_id',
        'sales_id',
        'customer_id',
        'admin_id',
        'status',
        'notes',
        'details',
    ];

    protected $casts = [
        'details' => 'array',
    ];

    public function unit()
    {
        return $this->belongsTo(Unit::class);
    }

    public function sales()
    {
        return $this->belongsTo(User::class, 'sales_id');
    }

    public function customer()
    {
        return $this->belongsTo(Customer::class);
    }

    public function admin()
    {
        return $this->belongsTo(User::class, 'admin_id');
    }
}
