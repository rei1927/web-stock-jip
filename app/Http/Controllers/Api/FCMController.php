<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Services\FCMService;

class FCMController extends Controller
{
    /**
     * Store or update the FCM token for the authenticated user.
     */
    public function storeToken(Request $request)
    {
        $request->validate([
            'fcm_token' => 'required|string',
        ]);

        $user = $request->user();
        $user->fcm_token = $request->fcm_token;
        $user->save();

        return response()->json([
            'status' => 'success',
            'message' => 'FCM Token saved successfully',
        ]);
    }

    /**
     * Broadcast a notification to all users.
     * Only accessible by admin role.
     */
    public function broadcast(Request $request)
    {
        $user = $request->user();
        
        // Ensure only admin can broadcast
        if ($user->role !== 'admin') {
            return response()->json([
                'status' => 'error',
                'message' => 'Unauthorized. Only admins can broadcast notifications.',
            ], 403);
        }

        $request->validate([
            'title' => 'required|string|max:255',
            'body' => 'required|string',
            'data' => 'nullable|array',
        ]);

        $result = FCMService::sendToAll(
            $request->title,
            $request->body,
            $request->data ?? []
        );

        return response()->json([
            'status' => 'success',
            'message' => 'Broadcast notification triggered successfully',
            'result' => $result
        ]);
    }
}
