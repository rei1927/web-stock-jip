<?php

namespace App\Http\Controllers;

use App\Models\Transaction;
use App\Models\Unit;
use App\Models\Customer;
use App\Models\User;
use Illuminate\Http\Request;

class TransactionController extends Controller
{
    public function index()
    {
        $transactions = Transaction::with(['unit', 'customer'])->get();
        return view('transactions', compact('transactions'));
    }

    public function create()
    {
        $units = Unit::all();
        $customers = Customer::all();
        $admins = User::all();
        return view('transaction-form', compact('units', 'customers', 'admins'));
    }

    public function store(Request $request)
    {
        $request->validate([
            'unit_id' => 'required|exists:units,id',
            'customer_id' => 'required|exists:customers,id',
            'admin_id' => 'required|exists:users,id',
            'status' => 'required|in:pending,completed,canceled',
            'notes' => 'nullable|string',
        ]);

        Transaction::create($request->all());

        return redirect()->route('transactions.index')->with('success', 'Transaction created successfully.');
    }

    public function show(Transaction $transaction)
    {
        //
    }

    public function edit(Transaction $transaction)
    {
        $units = Unit::all();
        $customers = Customer::all();
        $admins = User::all();
        return view('transaction-form', compact('transaction', 'units', 'customers', 'admins'));
    }

    public function update(Request $request, Transaction $transaction)
    {
        $request->validate([
            'unit_id' => 'required|exists:units,id',
            'customer_id' => 'required|exists:customers,id',
            'admin_id' => 'required|exists:users,id',
            'status' => 'required|in:pending,completed,canceled',
            'notes' => 'nullable|string',
        ]);

        $transaction->update($request->all());

        return redirect()->route('transactions.index')->with('success', 'Transaction updated successfully.');
    }

    public function destroy(Transaction $transaction)
    {
        $transaction->delete();

        return redirect()->route('transactions.index')->with('success', 'Transaction deleted successfully.');
    }
}
