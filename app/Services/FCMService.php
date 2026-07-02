<?php

namespace App\Services;

use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;
use App\Models\User;
use Illuminate\Support\Facades\Log;
use Throwable;

class FCMService
{
    /**
     * Send a multicast push notification to all users who have an FCM token.
     *
     * @param string $title
     * @param string $body
     * @param array $data Additional data payload
     * @return array
     */
    public static function sendToAll(string $title, string $body, array $data = []): array
    {
        // Retrieve all non-null FCM tokens
        $tokens = User::whereNotNull('fcm_token')
            ->where('fcm_token', '!=', '')
            ->pluck('fcm_token')
            ->toArray();

        if (empty($tokens)) {
            Log::info('FCM: No tokens found to broadcast.');
            return ['success' => 0, 'failure' => 0];
        }

        try {
            $messaging = app('firebase.messaging');
            
            $message = CloudMessage::new()
                ->withNotification(Notification::create($title, $body))
                ->withData($data);
                
            $report = $messaging->sendMulticast($message, $tokens);
            
            Log::info('FCM Broadcast Result:', [
                'success' => $report->successes()->count(),
                'failures' => $report->failures()->count()
            ]);

            return [
                'success' => $report->successes()->count(),
                'failure' => $report->failures()->count(),
            ];
        } catch (Throwable $e) {
            Log::error('FCM Broadcast Error: ' . $e->getMessage());
            return [
                'success' => 0,
                'failure' => 0,
                'error' => $e->getMessage()
            ];
        }
    }

    public static function sendToTokens(array $tokens, string $title, string $body, array $data = []): array
    {
        if (empty($tokens)) {
            Log::info('FCM: No tokens provided to sendToTokens.');
            return ['success' => 0, 'failure' => 0];
        }

        try {
            $messaging = app('firebase.messaging');
            $message = CloudMessage::new()
                ->withNotification(Notification::create($title, $body))
                ->withData($data);
                
            $report = $messaging->sendMulticast($message, $tokens);
            
            return [
                'success' => $report->successes()->count(),
                'failure' => $report->failures()->count(),
            ];
        } catch (Throwable $e) {
            Log::error('FCM Send Error: ' . $e->getMessage());
            return [
                'success' => 0,
                'failure' => 0,
                'error' => $e->getMessage()
            ];
        }
    }
}
