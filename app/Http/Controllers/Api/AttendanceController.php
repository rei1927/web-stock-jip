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
            'type' => 'required|string|in:Masuk,Keluar',
            'lat' => 'nullable|string',
            'long' => 'nullable|string',
            'address' => 'nullable|string',
            'photo' => 'nullable|string',
            'timestamp' => 'nullable|date',
        ]);

        $user = $request->user();

        // Cek status terakhir hari ini
        $lastAttendance = Attendance::where('user_id', $user->id)
            ->whereDate('created_at', now()->toDateString())
            ->orderBy('created_at', 'desc')
            ->first();

        $lastType = $lastAttendance ? $lastAttendance->type : null;

        // Aturan Stateful (Jika Masuk, harus Keluar)
        if ($request->type === 'Masuk' && $lastType === 'Masuk') {
            return response()->json([
                'status' => 'error',
                'message' => 'Anda sudah melakukan Absen Masuk hari ini. Silakan Absen Keluar.'
            ], 400);
        }

        if ($request->type === 'Keluar' && $lastType !== 'Masuk') {
            return response()->json([
                'status' => 'error',
                'message' => 'Anda harus Absen Masuk terlebih dahulu sebelum Absen Keluar.'
            ], 400);
        }

        $attendance = Attendance::create([
            'user_id' => $user->id,
            'type' => $request->type,
            'lat' => $request->lat,
            'long' => $request->long,
            'address' => $request->address,
            'photo_url' => $request->photo ?? $request->photo_url,
            'timestamp' => $request->timestamp ?? now(),
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Attendance submitted successfully',
            'data' => $attendance
        ], 201);
    }

    public function status(Request $request, $email = null)
    {
        // Get user from token if email is not provided or if we prefer token security
        $user = $request->user();
        
        // Cek status terakhir hari ini
        $lastAttendance = Attendance::where('user_id', $user->id)
            ->whereDate('created_at', now()->toDateString())
            ->orderBy('created_at', 'desc')
            ->first();

        $status = $lastAttendance ? $lastAttendance->type : 'Belum Absen';

        return response()->json([
            'status' => 'success',
            'last_status' => $status
        ]);
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
