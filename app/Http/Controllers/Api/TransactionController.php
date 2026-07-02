<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Customer;
use App\Models\Transaction;
use App\Models\Unit;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;
use App\Services\FCMService;

class TransactionController extends Controller
{
    public function index(Request $request)
    {
        $transactions = Transaction::with(['unit.cluster', 'customer', 'admin'])
            ->where('sales_id', $request->user()->id)
            ->orderBy('created_at', 'desc')
            ->paginate(20);

        return response()->json([
            'message' => 'Berhasil mengambil riwayat transaksi',
            'data' => $transactions
        ]);
    }

    public function hold(Request $request)
    {
        $request->validate([
            'unit_id' => 'required|exists:units,id',
            'notes' => 'nullable|string'
        ]);

        $unit = Unit::findOrFail($request->unit_id);

        if ($unit->status !== 'available') {
            throw ValidationException::withMessages([
                'unit_id' => ['Unit ini sudah tidak tersedia (status: ' . $unit->status . ').'],
            ]);
        }

        DB::beginTransaction();
        try {
            // Update unit status
            $unit->update(['status' => 'hold']);

            // Create transaction
            $transaction = Transaction::create([
                'unit_id' => $unit->id,
                'sales_id' => $request->user()->id,
                'status' => 'hold',
                'notes' => $request->notes,
            ]);

            DB::commit();

            // Broadcast notification for Hold
            FCMService::sendToAll(
                "Unit {$unit->block} di-Hold",
                "Unit {$unit->block} baru saja di-hold oleh sales.",
                ['unit_id' => $unit->id, 'status' => 'hold']
            );

            return response()->json([
                'message' => 'Berhasil melakukan hold unit',
                'data' => $transaction->load('unit')
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['message' => 'Terjadi kesalahan sistem'], 500);
        }
    }

    public function book(Request $request)
    {
        $request->validate([
            'unit_id' => 'required|exists:units,id',
            'customer_nik' => 'required|string',
            'customer_name' => 'required|string',
            'customer_phone' => 'required|string',
            'customer_address' => 'required|string',
            'notes' => 'nullable|string'
        ]);

        $unit = Unit::findOrFail($request->unit_id);

        if ($unit->status !== 'available' && $unit->status !== 'hold') {
            throw ValidationException::withMessages([
                'unit_id' => ['Unit ini sudah tidak dapat di-booking (status: ' . $unit->status . ').'],
            ]);
        }

        DB::beginTransaction();
        try {
            // Create or Update Customer
            $customer = Customer::updateOrCreate(
                ['nik' => $request->customer_nik],
                [
                    'name' => $request->customer_name,
                    'phone_number' => $request->customer_phone,
                    'address' => $request->customer_address,
                ]
            );

            // Update unit status
            $unit->update(['status' => 'request_booking']);

            // Create transaction
            $transaction = Transaction::create([
                'unit_id' => $unit->id,
                'sales_id' => $request->user()->id,
                'customer_id' => $customer->id,
                'status' => 'request_booking',
                'notes' => $request->notes,
            ]);

            DB::commit();

            // Broadcast notification for Request Booking
            FCMService::sendToAll(
                "Booking Request: Unit {$unit->block}",
                "Terdapat request booking baru untuk unit {$unit->block}.",
                ['unit_id' => $unit->id, 'status' => 'request_booking']
            );

            return response()->json([
                'message' => 'Berhasil mengajukan request booking',
                'data' => $transaction->load(['unit', 'customer'])
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['message' => 'Terjadi kesalahan sistem'], 500);
        }
    }
}
