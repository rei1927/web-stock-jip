@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <h6>{{ isset($user) ? 'Edit User' : 'Add New User' }}</h6>
            </div>
            <div class="card-body">
              <form role="form" method="POST" action="{{ isset($user) ? route('users.update', $user->id) : route('users.store') }}">
                @csrf
                @if(isset($user))
                  @method('PUT')
                @endif
                
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label>Name</label>
                    <input type="text" class="form-control" name="name" value="{{ old('name', $user->name ?? '') }}" required>
                    @error('name')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>Email</label>
                    <input type="email" class="form-control" name="email" value="{{ old('email', $user->email ?? '') }}" required>
                    @error('email')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>Password {{ isset($user) ? '(Biarkan kosong jika tidak ingin mengubah)' : '' }}</label>
                    <input type="password" class="form-control" name="password" {{ isset($user) ? '' : 'required' }}>
                    @error('password')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>Role</label>
                    <select class="form-control" name="role" required>
                      <option value="">- Pilih Role -</option>
                      <option value="super_admin" {{ old('role', $user->role ?? '') == 'super_admin' ? 'selected' : '' }}>Super Admin</option>
                      <option value="admin" {{ old('role', $user->role ?? '') == 'admin' ? 'selected' : '' }}>Admin</option>
                      <option value="sales_manager" {{ old('role', $user->role ?? '') == 'sales_manager' ? 'selected' : '' }}>Sales Manager</option>
                      <option value="sales" {{ old('role', $user->role ?? '') == 'sales' ? 'selected' : '' }}>Sales</option>
                    </select>
                    @error('role')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                </div>

                <div class="text-end mt-4">
                  <a href="{{ route('users.index') }}" class="btn btn-secondary mb-0">Cancel</a>
                  <button type="submit" class="btn bg-gradient-info mb-0">Save</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
@endsection
