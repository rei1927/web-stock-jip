<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Transaction;

class DashboardWebController extends Controller
{
    public function index(Request $request)
    {
        $period = $request->query('period', 'daily'); // daily, weekly, monthly
        $now = \Carbon\Carbon::now();

        if ($period === 'monthly') {
            $startDate = $now->copy()->startOfMonth();
            $previousStartDate = $now->copy()->subMonth()->startOfMonth();
            $previousEndDate = $now->copy()->subMonth()->endOfMonth();
        } elseif ($period === 'weekly') {
            $startDate = $now->copy()->startOfWeek();
            $previousStartDate = $now->copy()->subWeek()->startOfWeek();
            $previousEndDate = $now->copy()->subWeek()->endOfWeek();
        } else {
            $startDate = $now->copy()->startOfDay();
            $previousStartDate = $now->copy()->subDay()->startOfDay();
            $previousEndDate = $now->copy()->subDay()->endOfDay();
        }

        // Current Omzet
        $currentOmzet = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
            ->where('transactions.created_at', '>=', $startDate)
            ->sum('units.selling_price');

        // Previous Omzet
        $previousOmzet = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
            ->whereBetween('transactions.created_at', [$previousStartDate, $previousEndDate])
            ->sum('units.selling_price');

        $omzetChange = 0;
        if ($previousOmzet > 0) {
            $omzetChange = (($currentOmzet - $previousOmzet) / $previousOmzet) * 100;
        } else if ($currentOmzet > 0) {
            $omzetChange = 100;
        }

        // General Metrics
        $totalUsers = \App\Models\User::count();
        $totalTransactions = \App\Models\Transaction::count();

        // Chart Data
        $chartLabels = [];
        $chartData = [];

        if ($period === 'monthly') {
            for ($i = 5; $i >= 0; $i--) {
                $mStart = $now->copy()->subMonths($i)->startOfMonth();
                $mEnd = $now->copy()->subMonths($i)->endOfMonth();
                $val = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
                    ->whereBetween('transactions.created_at', [$mStart, $mEnd])
                    ->sum('units.selling_price');
                $chartLabels[] = $mStart->format('M Y');
                $chartData[] = $val;
            }
        } elseif ($period === 'weekly') {
            for ($i = 3; $i >= 0; $i--) {
                $wStart = $now->copy()->subWeeks($i)->startOfWeek();
                $wEnd = $now->copy()->subWeeks($i)->endOfWeek();
                $val = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
                    ->whereBetween('transactions.created_at', [$wStart, $wEnd])
                    ->sum('units.selling_price');
                $chartLabels[] = $wStart->format('d M');
                $chartData[] = $val;
            }
        } else {
            for ($i = 6; $i >= 0; $i--) {
                $dStart = $now->copy()->subDays($i)->startOfDay();
                $dEnd = $now->copy()->subDays($i)->endOfDay();
                $val = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
                    ->whereBetween('transactions.created_at', [$dStart, $dEnd])
                    ->sum('units.selling_price');
                $chartLabels[] = $dStart->format('d M');
                $chartData[] = $val;
            }
        }

        return view('dashboard', compact(
            'period',
            'currentOmzet',
            'omzetChange',
            'totalUsers',
            'totalTransactions',
            'chartLabels',
            'chartData'
        ));
    }
}
