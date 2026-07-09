<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Attendance;
use Illuminate\Http\Request;

class AttendanceController extends Controller
{
    public function submit(Request $request)
    {
        $request->validate([
            'type' => 'required|string',
            'location' => 'nullable|string',
            'photo_url' => 'nullable|string',
        ]);

        $attendance = Attendance::create([
            'user_id' => $request->user()->id,
            'type' => $request->type,
            'location' => $request->location,
            'photo_url' => $request->photo_url,
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Attendance submitted successfully',
            'data' => $attendance
        ], 201);
    }

    public function all(Request $request)
    {
        // For Admin / Super Admin
        if (!in_array($request->user()->role, ['admin', 'super_admin'])) {
            return response()->json(['message' => 'Unauthorized'], 403);
        }

        $attendances = Attendance::with('user')->orderBy('created_at', 'desc')->get();

        return response()->json([
            'status' => 'success',
            'data' => $attendances
        ]);
    }
}
