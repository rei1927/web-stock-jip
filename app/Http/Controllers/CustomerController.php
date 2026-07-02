<?php

namespace App\Http\Controllers;

use App\Models\Customer;
use Illuminate\Http\Request;

class CustomerController extends Controller
{
    public function index()
    {
        $customers = Customer::all();
        return view('customers', compact('customers'));
    }

    public function create()
    {
        return view('customer-form');
    }

    public function store(Request $request)
    {
        $request->validate([
            'nik' => 'required|string|max:255',
            'npwp' => 'nullable|string|max:255',
            'name' => 'required|string|max:255',
            'phone' => 'required|string|max:255',
            'email' => 'nullable|email|max:255',
            'address' => 'nullable|string',
        ]);

        Customer::create($request->all());

        return redirect()->route('customers.index')->with('success', 'Customer created successfully.');
    }

    public function show(Customer $customer)
    {
        //
    }

    public function edit(Customer $customer)
    {
        return view('customer-form', compact('customer'));
    }

    public function update(Request $request, Customer $customer)
    {
        $request->validate([
            'nik' => 'required|string|max:255',
            'npwp' => 'nullable|string|max:255',
            'name' => 'required|string|max:255',
            'phone' => 'required|string|max:255',
            'email' => 'nullable|email|max:255',
            'address' => 'nullable|string',
        ]);

        $customer->update($request->all());

        return redirect()->route('customers.index')->with('success', 'Customer updated successfully.');
    }

    public function destroy(Customer $customer)
    {
        $customer->delete();

        return redirect()->route('customers.index')->with('success', 'Customer deleted successfully.');
    }
}
