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
            ->whereIn('transactions.status', ['success', 'approved'])
            ->sum('units.selling_price');

        // Previous Omzet
        $previousOmzet = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
            ->whereBetween('transactions.created_at', [$previousStartDate, $previousEndDate])
            ->whereIn('transactions.status', ['success', 'approved'])
            ->sum('units.selling_price');

        $omzetChange = 0;
        if ($previousOmzet > 0) {
            $omzetChange = (($currentOmzet - $previousOmzet) / $previousOmzet) * 100;
        } else if ($currentOmzet > 0) {
            $omzetChange = 100;
        }

        // General Metrics
        $totalUsers = \App\Models\User::count();
        $totalTransactions = \App\Models\Transaction::whereIn('status', ['success', 'approved'])->count();

        // Chart Data (Filtered by start_date and end_date)
        $startDateChartStr = $request->query('start_date');
        $endDateChartStr = $request->query('end_date');
        
        $endDateChart = $endDateChartStr ? \Carbon\Carbon::parse($endDateChartStr)->endOfDay() : $now->copy()->endOfDay();
        $startDateChart = $startDateChartStr ? \Carbon\Carbon::parse($startDateChartStr)->startOfDay() : $now->copy()->subDays(6)->startOfDay(); // Default 7 days
        
        $diffInDays = $startDateChart->diffInDays($endDateChart);
        if ($diffInDays < 0) $diffInDays = 0;
        
        $previousEndDateChart = $startDateChart->copy()->subDay()->endOfDay();
        $previousStartDateChart = $previousEndDateChart->copy()->subDays($diffInDays)->startOfDay();

        // Optimized Queries using grouping
        $currentTransactions = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
            ->whereBetween('transactions.created_at', [$startDateChart, $endDateChart])
            ->whereIn('transactions.status', ['success', 'approved'])
            ->selectRaw('DATE(transactions.created_at) as date, SUM(units.selling_price) as total')
            ->groupBy('date')
            ->pluck('total', 'date');

        $previousTransactions = \App\Models\Transaction::join('units', 'transactions.unit_id', '=', 'units.id')
            ->whereBetween('transactions.created_at', [$previousStartDateChart, $previousEndDateChart])
            ->whereIn('transactions.status', ['success', 'approved'])
            ->selectRaw('DATE(transactions.created_at) as date, SUM(units.selling_price) as total')
            ->groupBy('date')
            ->pluck('total', 'date');

        $chartLabels = [];
        $chartData = [];
        $previousChartData = [];

        for ($i = 0; $i <= $diffInDays; $i++) {
            $currentDate = $startDateChart->copy()->addDays($i);
            $previousDate = $previousStartDateChart->copy()->addDays($i);
            
            $chartLabels[] = $currentDate->format('d M');
            $chartData[] = $currentTransactions->get($currentDate->format('Y-m-d'), 0);
            $previousChartData[] = $previousTransactions->get($previousDate->format('Y-m-d'), 0);
        }
        
        $chartStartDate = $startDateChart->format('Y-m-d');
        $chartEndDate = $endDateChart->format('Y-m-d');

        return view('dashboard', compact(
            'period',
            'currentOmzet',
            'omzetChange',
            'totalUsers',
            'totalTransactions',
            'chartLabels',
            'chartData',
            'previousChartData',
            'chartStartDate',
            'chartEndDate'
        ));
    }
}
