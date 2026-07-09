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
        
        // Simpan ke MinIO via disk 's3' dengan visibility public
        // menggunakan path 'uploads'
        $path = Storage::disk('s3')->putFileAs('uploads', $file, $filename, 'public');

        if (!$path) {
            return response()->json([
                'status' => 'error',
                'message' => 'Failed to upload image'
            ], 500);
        }

        // Dapatkan Public URL
        $url = Storage::disk('s3')->url($path);

        return response()->json([
            'status' => 'success',
            'url' => $url
        ]);
    }
}
