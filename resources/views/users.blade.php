@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h6>Users Data</h6>
              </div>
              <div class="d-flex justify-content-between align-items-center mt-3">
                <form method="GET" action="{{ route('users.index') }}" class="d-flex mb-0 align-items-center gap-2">
                  <select name="per_page" class="form-select form-select-sm" style="width: auto;" onchange="this.form.submit()">
                    <option value="10" {{ request('per_page', 10) == 10 ? 'selected' : '' }}>10 / Page</option>
                    <option value="20" {{ request('per_page') == 20 ? 'selected' : '' }}>20 / Page</option>
                    <option value="50" {{ request('per_page') == 50 ? 'selected' : '' }}>50 / Page</option>
                    <option value="100" {{ request('per_page') == 100 ? 'selected' : '' }}>100 / Page</option>
                  </select>
                  <input type="text" name="search" class="form-control form-control-sm" style="width: 200px;" placeholder="Search Name, Email, or Role..." value="{{ request('search') }}">
                  <button type="submit" class="btn btn-sm bg-gradient-secondary mb-0">Filter</button>
                  @if(request('search'))
                    <a href="{{ route('users.index') }}" class="btn btn-sm btn-outline-secondary mb-0">Clear</a>
                  @endif
                </form>
              </div>
            </div>
            <div class="card-body px-0 pt-0 pb-2">
              <div class="table-responsive p-0">
                <table class="table align-items-center mb-0">
                  <thead>
                    <tr>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Name</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2 border-start">Role</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-start">Registered Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    @forelse($users as $user)
                    <tr>
                      <td>
                        <div class="d-flex px-2 py-1">
                          <div class="d-flex flex-column justify-content-center">
                            <h6 class="mb-0 text-sm">{{ $user->name }}</h6>
                            <p class="text-xs text-secondary mb-0">{{ $user->email }}</p>
                          </div>
                        </div>
                      </td>
                      <td class="align-middle border-start">
                        <span class="badge badge-sm bg-gradient-{{ $user->role == 'super_admin' ? 'danger' : ($user->role == 'admin' ? 'info' : 'success') }}">{{ ucfirst($user->role ?? 'Sales') }}</span>
                      </td>
                      <td class="align-middle text-center border-start">
                        <span class="text-secondary text-xs font-weight-bold">{{ $user->created_at ? $user->created_at->format('d/m/Y') : '-' }}</span>
                      </td>
                    </tr>
                    @empty
                    <tr>
                      <td colspan="3" class="text-center py-4">
                        <p class="text-xs font-weight-bold mb-0">No users found.</p>
                      </td>
                    </tr>
                    @endforelse
                  </tbody>
                </table>
              </div>
              <div class="px-4 py-3 d-flex justify-content-end">
                {{ $users->withQueryString()->links('pagination::bootstrap-5') }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
@endsection
