<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class SuperAdminSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $user = User::where('email', 'reizarachmattullah@gmail.com')->first();
        
        if ($user) {
            $user->update([
                'role' => 'super_admin',
                'password' => Hash::make('Alamatgue123')
            ]);
            $this->command->info('Akun reizarachmattullah@gmail.com telah diupdate menjadi super_admin dan password berhasil di-reset.');
        } else {
            User::create([
                'name' => 'Reiza Rachmattullah',
                'email' => 'reizarachmattullah@gmail.com',
                'password' => Hash::make('Alamatgue123'), // Default password if not exists
                'role' => 'super_admin',
            ]);
            $this->command->info('Akun reizarachmattullah@gmail.com berhasil dibuat dengan role super_admin. Password default: password123');
        }
    }
}
