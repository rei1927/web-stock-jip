<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Cluster;
use App\Models\Unit;
use App\Models\Customer;
use App\Models\Transaction;

class DataWebController extends Controller
{
    public function clusters()
    {
        $clusters = Cluster::all();
        return view('clusters', compact('clusters'));
    }

    public function units()
    {
        $units = Unit::with('cluster')->get();
        return view('units', compact('units'));
    }

    public function customers()
    {
        $customers = Customer::all();
        return view('customers', compact('customers'));
    }

    public function transactions()
    {
        $transactions = Transaction::with(['unit', 'customer'])->get();
        return view('transactions', compact('transactions'));
    }
}
