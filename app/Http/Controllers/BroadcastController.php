<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Services\FCMService;

class BroadcastController extends Controller
{
    public function index()
    {
        return view('broadcast');
    }

    public function send(Request $request)
    {
        $request->validate([
            'title' => 'required|string|max:255',
            'body' => 'required|string',
        ]);

        $result = FCMService::sendToAll($request->title, $request->body);

        if (isset($result['error'])) {
            return back()->with('error', 'Gagal mengirim broadcast: ' . $result['error']);
        }

        return back()->with('success', "Broadcast berhasil dikirim ke {$result['success']} perangkat. (Gagal: {$result['failure']})");
    }
}
