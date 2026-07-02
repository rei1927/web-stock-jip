@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <h6>{{ isset($transaction) ? 'Edit Transaction' : 'Add New Transaction' }}</h6>
            </div>
            <div class="card-body">
              <form role="form" method="POST" action="{{ isset($transaction) ? route('transactions.update', $transaction->id) : route('transactions.store') }}">
                @csrf
                @if(isset($transaction))
                  @method('PUT')
                @endif
                <div class="row">
                  <div class="col-md-4 mb-3">
                    <label>Unit</label>
                    <select class="form-control" name="unit_id" required>
                      <option value="">Select Unit</option>
                      @foreach($units as $unit)
                        <option value="{{ $unit->id }}" {{ old('unit_id', $transaction->unit_id ?? '') == $unit->id ? 'selected' : '' }}>{{ $unit->name }} (Block {{ $unit->block }})</option>
                      @endforeach
                    </select>
                    @error('unit_id')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Customer</label>
                    <select class="form-control" name="customer_id" required>
                      <option value="">Select Customer</option>
                      @foreach($customers as $customer)
                        <option value="{{ $customer->id }}" {{ old('customer_id', $transaction->customer_id ?? '') == $customer->id ? 'selected' : '' }}>{{ $customer->name }}</option>
                      @endforeach
                    </select>
                    @error('customer_id')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Admin</label>
                    <select class="form-control" name="admin_id" required>
                      <option value="">Select Admin</option>
                      @foreach($admins as $admin)
                        <option value="{{ $admin->id }}" {{ old('admin_id', $transaction->admin_id ?? '') == $admin->id ? 'selected' : '' }}>{{ $admin->name }}</option>
                      @endforeach
                    </select>
                    @error('admin_id')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                </div>
                <div class="mb-3">
                  <label>Status</label>
                  <select class="form-control" name="status" required>
                    <option value="pending" {{ old('status', $transaction->status ?? '') == 'pending' ? 'selected' : '' }}>Pending</option>
                    <option value="completed" {{ old('status', $transaction->status ?? '') == 'completed' ? 'selected' : '' }}>Completed</option>
                    <option value="canceled" {{ old('status', $transaction->status ?? '') == 'canceled' ? 'selected' : '' }}>Canceled</option>
                  </select>
                  @error('status')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="mb-3">
                  <label>Notes</label>
                  <textarea class="form-control" name="notes" rows="3" placeholder="Additional notes">{{ old('notes', $transaction->notes ?? '') }}</textarea>
                  @error('notes')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="text-end">
                  <a href="{{ route('transactions.index') }}" class="btn btn-secondary mt-4 mb-0">Cancel</a>
                  <button type="submit" class="btn bg-gradient-info mt-4 mb-0">Save</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
@endsection
