<?php

namespace App\Http\Controllers;

use App\Models\Attendance;
use Illuminate\Http\Request;

class AttendanceWebController extends Controller
{
    public function index(Request $request)
    {
        $query = Attendance::with('user');

        if ($request->filled('start_date')) {
            $query->whereDate('timestamp', '>=', $request->start_date);
        }
        
        if ($request->filled('end_date')) {
            $query->whereDate('timestamp', '<=', $request->end_date);
        }
        
        if ($request->filled('sales_name')) {
            $query->whereHas('user', function($q) use ($request) {
                $q->where('name', 'like', '%' . $request->sales_name . '%');
            });
        }

        $attendances = $query->orderBy('timestamp', 'desc')->paginate(20);

        return view('attendances', compact('attendances'));
    }

    public function export(Request $request)
    {
        $query = Attendance::with('user');

        if ($request->filled('start_date')) {
            $query->whereDate('timestamp', '>=', $request->start_date);
        }
        
        if ($request->filled('end_date')) {
            $query->whereDate('timestamp', '<=', $request->end_date);
        }
        
        if ($request->filled('sales_name')) {
            $query->whereHas('user', function($q) use ($request) {
                $q->where('name', 'like', '%' . $request->sales_name . '%');
            });
        }

        $attendances = $query->orderBy('timestamp', 'desc')->get();
        $csv = "Nama Sales,Tipe,Waktu,Latitude,Longitude,Alamat,URL Foto\n";

        foreach ($attendances as $record) {
            $name = str_replace(',', ' ', $record->user->name);
            $address = str_replace(["\n", "\r", ","], ' ', $record->address);
            $csv .= "{$name},{$record->type},{$record->timestamp},{$record->lat},{$record->long},{$address},{$record->photo_url}\n";
        }

        return response()->streamDownload(function () use ($csv) {
            echo $csv;
        }, 'laporan-absensi.csv');
    }
}
