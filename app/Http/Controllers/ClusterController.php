<?php

namespace App\Http\Controllers;

use App\Models\Cluster;
use Illuminate\Http\Request;

class ClusterController extends Controller
{
    public function index()
    {
        $clusters = Cluster::all();
        return view('clusters', compact('clusters'));
    }

    public function create()
    {
        return view('cluster-form');
    }

    public function store(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'description' => 'nullable|string',
        ]);

        Cluster::create($request->all());

        return redirect()->route('clusters.index')->with('success', 'Cluster created successfully.');
    }

    public function show(Cluster $cluster)
    {
        //
    }

    public function edit(Cluster $cluster)
    {
        return view('cluster-form', compact('cluster'));
    }

    public function update(Request $request, Cluster $cluster)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'description' => 'nullable|string',
        ]);

        $cluster->update($request->all());

        return redirect()->route('clusters.index')->with('success', 'Cluster updated successfully.');
    }

    public function destroy(Cluster $cluster)
    {
        $cluster->delete();

        return redirect()->route('clusters.index')->with('success', 'Cluster deleted successfully.');
    }
}
