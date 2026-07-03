<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Unit;
use Illuminate\Http\Request;

class UnitController extends Controller
{
    public function index(Request $request)
    {
        $query = Unit::with('cluster');

        if ($request->has('cluster') && $request->cluster) {
            // Filter by cluster name (as expected by Android PANDUAN_INTEGRASI_API.md)
            $query->whereHas('cluster', function ($q) use ($request) {
                $q->where('name', 'like', '%' . $request->cluster . '%');
            });
        }

        $units = $query->get();

        $mappedUnits = $units->map(function ($unit) {
            $details = is_string($unit->unit_details) ? json_decode($unit->unit_details, true) : ($unit->unit_details ?? []);
            
            // Status mapping to Android App UI
            $mappedStatus = 'Tersedia';
            if ($unit->status === 'hold') $mappedStatus = 'Hold';
            elseif ($unit->status === 'request_booking') $mappedStatus = 'Pending Sold';
            elseif ($unit->status === 'sold') $mappedStatus = 'Terjual';

            return [
                'clusterName' => $unit->cluster ? $unit->cluster->name : 'Unknown',
                'block' => $unit->block,
                'typeName' => $details['type'] ?? $unit->name,
                'price' => (float) $unit->selling_price,
                'isSold' => in_array($unit->status, ['sold']),
                'buildingArea' => (int) ($details['buildingArea'] ?? 0),
                'landArea' => (int) ($details['landArea'] ?? 0),
                'bedrooms' => (int) ($details['bedrooms'] ?? 0),
                'bathrooms' => (int) ($details['bathrooms'] ?? 0),
                'notes' => $details['notes'] ?? '',
                'status' => $mappedStatus,
                'actionByUser' => null, // Will be populated dynamically in Android App or via Transaction relations later
                'actionUserLabel' => null,
                'holdTimestamp' => null,
            ];
        });

        // Return flat JSON array as requested by the Android App DTO
        return response()->json($mappedUnits);
    }

    public function updateStatus(Request $request)
    {
        $request->validate([
            'clusterName' => 'required|string',
            'block' => 'required|string',
            'status' => 'required|string',
            'actionByUser' => 'nullable|string',
            'actionUserLabel' => 'nullable|string',
        ]);

        $unit = Unit::whereHas('cluster', function ($q) use ($request) {
            $q->where('name', $request->clusterName);
        })->where('block', $request->block)->first();

        if (!$unit) {
            return response()->json([
                'status' => 'error',
                'message' => 'Unit not found'
            ], 404);
        }

        // Map Android Status to Database Status
        $mappedStatus = 'available';
        $androidStatus = strtolower($request->status);
        if ($androidStatus === 'hold') {
            $mappedStatus = 'hold';
        } elseif ($androidStatus === 'pending sold' || $androidStatus === 'pending_sold' || $androidStatus === 'request_booking') {
            $mappedStatus = 'request_booking';
        } elseif ($androidStatus === 'terjual' || $androidStatus === 'sold') {
            $mappedStatus = 'sold';
        }

        $unit->status = $mappedStatus;

        $details = is_string($unit->unit_details) ? json_decode($unit->unit_details, true) : ($unit->unit_details ?? []);
        $details['actionByUser'] = $request->actionByUser;
        $details['actionUserLabel'] = $request->actionUserLabel;
        if ($mappedStatus === 'hold') {
            $details['holdTimestamp'] = time() * 1000;
        } else {
            unset($details['holdTimestamp']);
        }
        $unit->unit_details = $details;
        $unit->save();

        if ($mappedStatus === 'hold') {
            // Notify Manager/Admin
            $adminTokens = \App\Models\User::whereIn('role', ['admin', 'super_admin'])
                ->whereNotNull('fcm_token')
                ->where('fcm_token', '!=', '')
                ->pluck('fcm_token')
                ->toArray();
                
            if (!empty($adminTokens)) {
                $salesName = $request->actionUserLabel ?? $request->actionByUser ?? 'Unknown Sales';
                \App\Services\FCMService::sendToTokens(
                    $adminTokens,
                    "Unit di-Hold",
                    "Sales {$salesName} telah melakukan HOLD pada {$unit->block}."
                );
            }
        }

        return response()->json([
            'status' => 'success',
            'message' => 'Unit status updated successfully'
        ]);
    }

    public function submitSold(Request $request)
    {
        $request->validate([
            'unitId' => 'required|exists:units,id',
            'namaLengkap' => 'required|string',
            'noKtpConsumer' => 'required|string',
            // other fields are optional/nullable in schema
        ]);

        try {
            \Illuminate\Support\Facades\DB::beginTransaction();

            $unit = Unit::findOrFail($request->unitId);

            // Update or Create Customer
            $customer = \App\Models\Customer::updateOrCreate(
                ['nik' => $request->noKtpConsumer],
                [
                    'npwp' => $request->noNpwp,
                    'name' => $request->namaLengkap,
                    'phone' => $request->noTelpSeluler ?? '-',
                    'email' => $request->email,
                    'address' => $request->alamatKtp ?? '-',
                ]
            );

            // Find Sales ID if available
            $details = is_string($unit->unit_details) ? json_decode($unit->unit_details, true) : ($unit->unit_details ?? []);
            $salesUsername = $details['actionByUser'] ?? null;
            $sales = null;
            if ($salesUsername) {
                // assume username is the part before @ in email for now, or just search by name
                // we seeded siska@example.com, and actionByUser="siska"
                $sales = \App\Models\User::where('email', 'like', $salesUsername . '@%')
                                         ->orWhere('name', 'like', '%' . $salesUsername . '%')
                                         ->first();
            }

            // transaction details
            $trxDetails = $request->except(['unitId', 'namaLengkap', 'noKtpConsumer', 'alamatKtp', 'noTelpSeluler', 'noNpwp', 'email']);

            $transaction = \App\Models\Transaction::create([
                'unit_id' => $unit->id,
                'sales_id' => $sales ? $sales->id : auth()->id(),
                'customer_id' => $customer->id,
                'admin_id' => auth()->id(),
                'status' => 'approved',
                'details' => $trxDetails,
            ]);

            // update unit status
            $unit->status = 'sold';
            $unit->save();

            \Illuminate\Support\Facades\DB::commit();

            // Notify Sales if found
            if ($sales && $sales->fcm_token) {
                \App\Services\FCMService::sendToTokens(
                    [$sales->fcm_token],
                    "Penjualan Disetujui!",
                    "Unit {$unit->block} telah resmi SOLD. Selamat!"
                );
            }

            return response()->json([
                'status' => 'success',
                'message' => 'Transaction submitted successfully',
                'data' => $transaction
            ]);
        } catch (\Throwable $e) {
            \Illuminate\Support\Facades\DB::rollBack();
            return response()->json([
                'status' => 'error',
                'message' => $e->getMessage()
            ], 500);
        }
    }
}
