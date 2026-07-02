<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Transaction;

class DashboardWebController extends Controller
{
    public function index()
    {
        $transactionsPerDay = Transaction::selectRaw('DATE(created_at) as date, count(*) as total')
                                ->groupBy('date')
                                ->orderBy('date', 'ASC')
                                ->take(7)
                                ->get();

        $chartLabels = $transactionsPerDay->pluck('date');
        $chartData = $transactionsPerDay->pluck('total');

        return view('dashboard', compact('chartLabels', 'chartData'));
    }
}
