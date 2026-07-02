<?php

namespace App\Console\Commands;

use Illuminate\Console\Attributes\Description;
use Illuminate\Console\Attributes\Signature;
use Illuminate\Console\Command;
use App\Models\Unit;
use App\Models\User;
use App\Services\FCMService;
use Illuminate\Support\Facades\Log;

#[Signature('app:check-expired-holds')]
#[Description('Release unit holds that are older than 12 hours and notify sales.')]
class CheckExpiredHolds extends Command
{
    /**
     * Execute the console command.
     */
    public function handle()
    {
        $units = Unit::where('status', 'hold')->get();
        $expiredCount = 0;
        $now = time() * 1000; // current time in ms
        $twelveHoursInMs = 12 * 60 * 60 * 1000;

        foreach ($units as $unit) {
            $details = is_string($unit->unit_details) ? json_decode($unit->unit_details, true) : ($unit->unit_details ?? []);
            
            if (isset($details['holdTimestamp'])) {
                $holdTimestamp = (int) $details['holdTimestamp'];
                
                if ($now - $holdTimestamp > $twelveHoursInMs) {
                    $salesUsername = $details['actionByUser'] ?? null;
                    
                    // Revert to available
                    $unit->status = 'available';
                    unset($details['holdTimestamp']);
                    unset($details['actionByUser']);
                    unset($details['actionUserLabel']);
                    
                    $unit->unit_details = $details;
                    $unit->save();
                    
                    $expiredCount++;

                    // Notify Sales
                    if ($salesUsername) {
                        $sales = User::where('email', 'like', $salesUsername . '@%')
                                     ->orWhere('name', 'like', '%' . $salesUsername . '%')
                                     ->first();
                                     
                        if ($sales && $sales->fcm_token) {
                            FCMService::sendToTokens(
                                [$sales->fcm_token],
                                "Hold Expired",
                                "Unit {$unit->block} telah dilepas otomatis karena melewati batas waktu 12 Jam."
                            );
                        }
                    }
                }
            }
        }

        $this->info("Released {$expiredCount} expired holds.");
        Log::info("CheckExpiredHolds executed: Released {$expiredCount} expired holds.");
    }
}
