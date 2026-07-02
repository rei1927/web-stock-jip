@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0 d-flex justify-content-between align-items-center">
              <h6>Clusters Data</h6>
              <a href="{{ route('clusters.create') }}" class="btn btn-sm bg-gradient-info mb-0">Add New</a>
            </div>
            <div class="card-body px-0 pt-0 pb-2">
              <div class="table-responsive p-0">
                <table class="table align-items-center mb-0">
                  <thead>
                    <tr>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">ID</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Name</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Description</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Created At</th>
                      <th class="text-secondary opacity-7"></th>
                    </tr>
                  </thead>
                  <tbody>
                    @foreach($clusters as $cluster)
                    <tr>
                      <td>
                        <div class="d-flex px-3 py-1">
                          <div class="d-flex flex-column justify-content-center">
                            <h6 class="mb-0 text-sm">{{ $cluster->id }}</h6>
                          </div>
                        </div>
                      </td>
                      <td>
                        <p class="text-sm font-weight-bold mb-0">{{ $cluster->name }}</p>
                      </td>
                      <td>
                        <p class="text-xs font-weight-bold mb-0">{{ Str::limit($cluster->description, 50) }}</p>
                      </td>
                      <td class="align-middle text-center text-sm">
                        <span class="text-secondary text-xs font-weight-bold">{{ $cluster->created_at ? $cluster->created_at->format('d/m/y') : '-' }}</span>
                      </td>
                      <td class="align-middle">
                        <a href="{{ route('clusters.edit', $cluster->id) }}" class="text-secondary font-weight-bold text-xs me-3" data-toggle="tooltip" data-original-title="Edit cluster">
                          Edit
                        </a>
                        <form action="{{ route('clusters.destroy', $cluster->id) }}" method="POST" class="d-inline">
                          @csrf
                          @method('DELETE')
                          <button type="submit" class="text-danger font-weight-bold text-xs bg-transparent border-0 p-0" onclick="return confirm('Are you sure you want to delete this cluster?')" data-toggle="tooltip" data-original-title="Delete cluster">
                            Delete
                          </button>
                        </form>
                      </td>
                    </tr>
                    @endforeach
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
@endsection
