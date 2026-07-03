<?php

namespace App\Http\Controllers;

use App\Models\Transaction;
use App\Models\Unit;
use App\Models\Customer;
use App\Models\User;
use App\Exports\TransactionExport;
use Maatwebsite\Excel\Facades\Excel;
use Illuminate\Http\Request;

class TransactionController extends Controller
{
    public function index(Request $request)
    {
        $query = Transaction::with(['unit.cluster', 'customer']);

        if ($request->has('search') && $request->search != '') {
            $search = $request->search;
            $query->whereHas('customer', function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('nik', 'like', "%{$search}%");
            })->orWhereHas('unit', function($q) use ($search) {
                $q->where('block', 'like', "%{$search}%");
            });
        }

        if ($request->has('start_date') && $request->start_date != '') {
            $query->whereDate('created_at', '>=', $request->start_date);
        }

        if ($request->has('end_date') && $request->end_date != '') {
            $query->whereDate('created_at', '<=', $request->end_date);
        }

        $transactions = $query->latest()->paginate($request->get('per_page', 10));

        return view('transactions', compact('transactions'));
    }

    public function bulkAction(Request $request)
    {
        $request->validate([
            'ids' => 'required|array',
            'action' => 'required|in:delete,export'
        ]);

        $ids = $request->ids;
        $action = $request->action;

        if ($action === 'delete') {
            Transaction::whereIn('id', $ids)->delete();
            return redirect()->back()->with('success', count($ids) . ' transactions deleted successfully.');
        } elseif ($action === 'export') {
            return Excel::download(new TransactionExport($ids), 'transactions_export_' . date('Ymd_His') . '.xlsx');
        }

        return redirect()->back();
    }

    public function create()
    {
        $units = Unit::all();
        $admins = User::all();
        return view('transaction-form', compact('units', 'admins'));
    }

    public function store(Request $request)
    {
        $request->validate([
            'unit_id' => 'required|exists:units,id',
            'admin_id' => 'required|exists:users,id',
            'status' => 'required',
            'nik' => 'required|string',
            'name' => 'required|string',
        ]);

        $customer = Customer::updateOrCreate(
            ['nik' => $request->nik],
            $request->only(['name', 'npwp', 'phone', 'email', 'address', 'alamat_surat', 'no_telepon_rumah', 'no_kk'])
        );

        $details = $request->except([
            '_token', '_method', 'unit_id', 'admin_id', 'status', 'notes', 
            'nik', 'name', 'npwp', 'phone', 'email', 'address', 'alamat_surat', 'no_telepon_rumah', 'no_kk'
        ]);

        Transaction::create([
            'unit_id' => $request->unit_id,
            'customer_id' => $customer->id,
            'admin_id' => $request->admin_id,
            'sales_id' => auth()->id(),
            'status' => $request->status,
            'notes' => $request->notes,
            'details' => $details,
        ]);

        return redirect()->route('transactions.index')->with('success', 'Transaction created successfully.');
    }

    public function show(Transaction $transaction)
    {
        return view('transaction-show', compact('transaction'));
    }

    public function edit(Transaction $transaction)
    {
        $units = Unit::all();
        $admins = User::all();
        return view('transaction-form', compact('transaction', 'units', 'admins'));
    }

    public function update(Request $request, Transaction $transaction)
    {
        $request->validate([
            'unit_id' => 'required|exists:units,id',
            'admin_id' => 'required|exists:users,id',
            'status' => 'required',
            'nik' => 'required|string',
            'name' => 'required|string',
        ]);

        $customer = Customer::updateOrCreate(
            ['nik' => $request->nik],
            $request->only(['name', 'npwp', 'phone', 'email', 'address', 'alamat_surat', 'no_telepon_rumah', 'no_kk'])
        );

        $details = $request->except([
            '_token', '_method', 'unit_id', 'admin_id', 'status', 'notes', 
            'nik', 'name', 'npwp', 'phone', 'email', 'address', 'alamat_surat', 'no_telepon_rumah', 'no_kk'
        ]);

        $transaction->update([
            'unit_id' => $request->unit_id,
            'customer_id' => $customer->id,
            'admin_id' => $request->admin_id,
            'status' => $request->status,
            'notes' => $request->notes,
            'details' => $details,
        ]);

        return redirect()->route('transactions.index')->with('success', 'Transaction updated successfully.');
    }

    public function destroy(Transaction $transaction)
    {
        $transaction->delete();

        return redirect()->route('transactions.index')->with('success', 'Transaction deleted successfully.');
    }
}
