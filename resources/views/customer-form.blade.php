@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <h6>{{ isset($customer) ? 'Edit Customer' : 'Add New Customer' }}</h6>
            </div>
            <div class="card-body">
              <form role="form" method="POST" action="{{ isset($customer) ? route('customers.update', $customer->id) : route('customers.store') }}">
                @csrf
                @if(isset($customer))
                  @method('PUT')
                @endif
                <div class="mb-3">
                  <label>Name</label>
                  <input type="text" class="form-control" name="name" placeholder="Full Name" value="{{ old('name', $customer->name ?? '') }}" required>
                  @error('name')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label>NIK</label>
                    <input type="text" class="form-control" name="nik" placeholder="NIK" value="{{ old('nik', $customer->nik ?? '') }}" required>
                    @error('nik')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>NPWP</label>
                    <input type="text" class="form-control" name="npwp" placeholder="NPWP" value="{{ old('npwp', $customer->npwp ?? '') }}">
                    @error('npwp')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label>Phone</label>
                    <input type="text" class="form-control" name="phone" placeholder="Phone Number" value="{{ old('phone', $customer->phone ?? '') }}" required>
                    @error('phone')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>Email</label>
                    <input type="email" class="form-control" name="email" placeholder="Email Address" value="{{ old('email', $customer->email ?? '') }}">
                    @error('email')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                </div>
                <div class="mb-3">
                  <label>Address</label>
                  <textarea class="form-control" name="address" rows="3" placeholder="Full Address">{{ old('address', $customer->address ?? '') }}</textarea>
                  @error('address')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="text-end">
                  <a href="{{ route('customers.index') }}" class="btn btn-secondary mt-4 mb-0">Cancel</a>
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
