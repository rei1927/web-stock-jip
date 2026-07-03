<?php

namespace App\Http\Controllers;

use App\Models\Unit;
use App\Models\Cluster;
use Illuminate\Http\Request;
use Maatwebsite\Excel\Facades\Excel;
use App\Imports\UnitsImport;
use App\Exports\UnitsTemplateExport;
use App\Services\FCMService;

class UnitController extends Controller
{
    public function index(Request $request)
    {
        $query = Unit::with('cluster');

        if ($request->filled('cluster_id')) {
            $query->where('cluster_id', $request->cluster_id);
        }

        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function ($q) use ($search) {
                $q->where('block', 'like', "%{$search}%")
                  ->orWhere('name', 'like', "%{$search}%")
                  ->orWhere('status', 'like', "%{$search}%");
            });
        }

        $perPage = $request->input('per_page', 10);
        $units = $query->paginate($perPage)->appends($request->query());
        $clusters = Cluster::all();

        return view('units', compact('units', 'clusters'));
    }

    public function create()
    {
        $clusters = Cluster::all();
        return view('unit-form', compact('clusters'));
    }

    public function store(Request $request)
    {
        $request->validate([
            'cluster_id' => 'required|exists:clusters,id',
            'name' => 'required|string|max:255',
            'block' => 'required|string|max:255',
            'selling_price' => 'required|numeric',
            'status' => 'required|in:available,sold,hold',
            'type' => 'nullable|string|max:255',
            'landArea' => 'nullable|numeric',
            'buildingArea' => 'nullable|numeric',
            'cash_1x' => 'nullable|numeric',
            'cash_2x' => 'nullable|numeric',
            'cash_3x' => 'nullable|numeric',
            'kpr_1x' => 'nullable|numeric',
            'kpr_2x' => 'nullable|numeric',
            'kpr_3x' => 'nullable|numeric',
            'description' => 'nullable|string',
        ]);

        $details = [
            'type' => $request->input('type'),
            'landArea' => $request->input('landArea'),
            'buildingArea' => $request->input('buildingArea'),
            'cash_1x' => $request->input('cash_1x'),
            'cash_2x' => $request->input('cash_2x'),
            'cash_3x' => $request->input('cash_3x'),
            'kpr_1x' => $request->input('kpr_1x'),
            'kpr_2x' => $request->input('kpr_2x'),
            'kpr_3x' => $request->input('kpr_3x'),
            'description' => $request->input('description'),
        ];

        $data = $request->only(['cluster_id', 'name', 'block', 'selling_price', 'status']);
        $data['unit_details'] = $details;

        Unit::create($data);

        return redirect()->route('units.index')->with('success', 'Unit created successfully.');
    }

    public function show(Unit $unit)
    {
        //
    }

    public function edit(Unit $unit)
    {
        $clusters = Cluster::all();
        return view('unit-form', compact('unit', 'clusters'));
    }

    public function update(Request $request, Unit $unit)
    {
        $request->validate([
            'cluster_id' => 'required|exists:clusters,id',
            'name' => 'required|string|max:255',
            'block' => 'required|string|max:255',
            'selling_price' => 'required|numeric',
            'status' => 'required|in:available,sold,hold',
            'type' => 'nullable|string|max:255',
            'landArea' => 'nullable|numeric',
            'buildingArea' => 'nullable|numeric',
            'cash_1x' => 'nullable|numeric',
            'cash_2x' => 'nullable|numeric',
            'cash_3x' => 'nullable|numeric',
            'kpr_1x' => 'nullable|numeric',
            'kpr_2x' => 'nullable|numeric',
            'kpr_3x' => 'nullable|numeric',
            'description' => 'nullable|string',
        ]);

        $details = [
            'type' => $request->input('type'),
            'landArea' => $request->input('landArea'),
            'buildingArea' => $request->input('buildingArea'),
            'cash_1x' => $request->input('cash_1x'),
            'cash_2x' => $request->input('cash_2x'),
            'cash_3x' => $request->input('cash_3x'),
            'kpr_1x' => $request->input('kpr_1x'),
            'kpr_2x' => $request->input('kpr_2x'),
            'kpr_3x' => $request->input('kpr_3x'),
            'description' => $request->input('description'),
        ];

        $data = $request->only(['cluster_id', 'name', 'block', 'selling_price', 'status']);
        $data['unit_details'] = array_merge($unit->unit_details ?? [], $details);

        // Check if status changed to hold or sold
        $oldStatus = $unit->status;
        $unit->update($data);

        if ($oldStatus !== $unit->status && in_array($unit->status, ['hold', 'sold'])) {
            $statusName = ucfirst($unit->status);
            FCMService::sendToAll(
                "Unit {$unit->block} menjadi {$statusName}",
                "Status unit {$unit->block} telah dirubah menjadi {$statusName} oleh Admin.",
                ['unit_id' => $unit->id, 'status' => $unit->status]
            );
        }

        return redirect()->route('units.index')->with('success', 'Unit updated successfully.');
    }

    public function destroy(Unit $unit)
    {
        $unit->delete();

        return redirect()->route('units.index')->with('success', 'Unit deleted successfully.');
    }

    public function downloadTemplate()
    {
        return Excel::download(new UnitsTemplateExport, 'units-template.xlsx');
    }

    public function import(Request $request)
    {
        $request->validate([
            'file' => 'required|mimes:xlsx,xls,csv|max:2048',
        ]);

        try {
            Excel::import(new UnitsImport, $request->file('file'));
            return redirect()->route('units.index')->with('success', 'Units imported successfully.');
        } catch (\Exception $e) {
            return redirect()->route('units.index')->with('error', 'Error importing units: ' . $e->getMessage());
        }
    }

    public function bulkAction(Request $request)
    {
        $request->validate([
            'unit_ids' => 'required|array',
            'unit_ids.*' => 'exists:units,id',
            'bulk_action' => 'required|string'
        ]);

        $action = $request->bulk_action;
        $ids = $request->unit_ids;

        if ($action === 'delete') {
            Unit::whereIn('id', $ids)->delete();
            return redirect()->route('units.index')->with('success', count($ids) . ' units deleted successfully.');
        } 
        
        if (in_array($action, ['available', 'hold', 'sold', 'request_booking'])) {
            Unit::whereIn('id', $ids)->update(['status' => $action]);

            if (in_array($action, ['hold', 'sold'])) {
                $statusName = ucfirst($action);
                $count = count($ids);
                FCMService::sendToAll(
                    "Pembaruan Status Unit",
                    "Status {$count} unit telah dirubah menjadi {$statusName} secara massal oleh Admin.",
                    ['status' => $action]
                );
            }

            return redirect()->route('units.index')->with('success', count($ids) . ' units status updated to ' . ucfirst($action) . '.');
        }

        return redirect()->route('units.index')->with('error', 'Invalid bulk action selected.');
    }
}
