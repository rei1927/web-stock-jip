@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <h6>{{ isset($cluster) ? 'Edit Cluster' : 'Add New Cluster' }}</h6>
            </div>
            <div class="card-body">
              <form role="form" method="POST" action="{{ isset($cluster) ? route('clusters.update', $cluster->id) : route('clusters.store') }}">
                @csrf
                @if(isset($cluster))
                  @method('PUT')
                @endif
                <div class="mb-3">
                  <label>Name</label>
                  <input type="text" class="form-control" name="name" id="name" placeholder="Cluster Name" value="{{ old('name', $cluster->name ?? '') }}" required>
                  @error('name')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="mb-3">
                  <label>Description</label>
                  <textarea class="form-control" name="description" id="description" rows="3" placeholder="Description">{{ old('description', $cluster->description ?? '') }}</textarea>
                  @error('description')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="text-end">
                  <a href="{{ route('clusters.index') }}" class="btn btn-secondary mt-4 mb-0">Cancel</a>
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
