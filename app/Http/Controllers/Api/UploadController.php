<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;

class UploadController extends Controller
{
    public function uploadImage(Request $request)
    {
        $request->validate([
            'file' => 'required|image|mimes:jpeg,png,jpg,gif|max:10240', // max 10MB
        ]);

        $file = $request->file('file');
        
        // Generate random hash for filename to prevent guessing
        $filename = Str::random(40) . '.' . $file->getClientOriginalExtension();
        
        // Gunakan disk dari env, default ke 'public'
        $disk = env('FILESYSTEM_DISK', 'public');
        
        try {
            $path = Storage::disk($disk)->putFileAs('uploads', $file, $filename, 'public');

            if (!$path) {
                return response()->json([
                    'status' => 'error',
                    'message' => "Failed to upload image to disk: $disk. Please check .env configuration and storage permissions."
                ], 500);
            }

            // Dapatkan Public URL
            $url = Storage::disk($disk)->url($path);
        } catch (\Exception $e) {
            return response()->json([
                'status' => 'error',
                'message' => "Exception on disk '$disk': " . $e->getMessage()
            ], 500);
        }

        return response()->json([
            'status' => 'success',
            'url' => $url
        ]);
    }
}
