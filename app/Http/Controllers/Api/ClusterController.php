<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Cluster;
use Illuminate\Http\Request;

class ClusterController extends Controller
{
    public function index()
    {
        $clusters = Cluster::orderBy('name')->get();

        return response()->json([
            'message' => 'Berhasil mengambil data cluster',
            'data' => $clusters
        ]);
    }
}
