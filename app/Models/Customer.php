<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Customer extends Model
{
    protected $fillable = [
        'nik',
        'npwp',
        'name',
        'phone',
        'email',
        'address',
        'alamat_surat',
        'no_telepon_rumah',
        'no_kk',
    ];

    public function transactions()
    {
        return $this->hasMany(Transaction::class);
    }
}
