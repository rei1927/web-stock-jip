@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <h6>{{ isset($unit) ? 'Edit Unit' : 'Add New Unit' }}</h6>
            </div>
            <div class="card-body">
              <form role="form" method="POST" action="{{ isset($unit) ? route('units.update', $unit->id) : route('units.store') }}">
                @csrf
                @if(isset($unit))
                  @method('PUT')
                @endif
                <div class="mb-3">
                  <label>Cluster</label>
                  <select class="form-control" name="cluster_id" required>
                    <option value="">Select Cluster</option>
                    @foreach($clusters as $cluster)
                      <option value="{{ $cluster->id }}" {{ old('cluster_id', $unit->cluster_id ?? '') == $cluster->id ? 'selected' : '' }}>{{ $cluster->name }}</option>
                    @endforeach
                  </select>
                  @error('cluster_id')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="mb-3">
                  <label>Unit Name</label>
                  <input type="text" class="form-control" name="name" placeholder="Unit Name" value="{{ old('name', $unit->name ?? '') }}" required>
                  @error('name')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="mb-3">
                  <label>Block</label>
                  <input type="text" class="form-control" name="block" placeholder="Block" value="{{ old('block', $unit->block ?? '') }}" required>
                  @error('block')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                
                <div class="row">
                  <div class="col-md-4 mb-3">
                    <label>Type</label>
                    <input type="text" class="form-control" name="type" placeholder="Type (e.g. RUBY)" value="{{ old('type', $unit->unit_details['type'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Luas Tanah (m2)</label>
                    <input type="number" class="form-control" name="landArea" placeholder="Luas Tanah" value="{{ old('landArea', $unit->unit_details['landArea'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Luas Bangunan (m2)</label>
                    <input type="number" class="form-control" name="buildingArea" placeholder="Luas Bangunan" value="{{ old('buildingArea', $unit->unit_details['buildingArea'] ?? '') }}">
                  </div>
                </div>

                <h6 class="mt-4">Harga & Skema Pembayaran</h6>
                
                <div class="row">
                  <div class="col-md-4 mb-3">
                    <label>Harga Normal</label>
                    <input type="number" class="form-control" name="selling_price" placeholder="Harga Normal" value="{{ old('selling_price', $unit->selling_price ?? '') }}" required>
                    @error('selling_price')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>
                </div>

                <div class="row mt-3">
                  <div class="col-md-4 mb-3">
                    <label class="text-success text-xs font-weight-bold">CASH 1X</label>
                    <input type="number" class="form-control" name="cash_1x" placeholder="Harga Cash 1X" value="{{ old('cash_1x', $unit->unit_details['cash_1x'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label class="text-success text-xs font-weight-bold">CASH 2X</label>
                    <input type="number" class="form-control" name="cash_2x" placeholder="Harga Cash 2X" value="{{ old('cash_2x', $unit->unit_details['cash_2x'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label class="text-success text-xs font-weight-bold">CASH 3X</label>
                    <input type="number" class="form-control" name="cash_3x" placeholder="Harga Cash 3X" value="{{ old('cash_3x', $unit->unit_details['cash_3x'] ?? '') }}">
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-4 mb-3">
                    <label class="text-info text-xs font-weight-bold">KPR 1X</label>
                    <input type="number" class="form-control" name="kpr_1x" placeholder="Harga KPR 1X" value="{{ old('kpr_1x', $unit->unit_details['kpr_1x'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label class="text-info text-xs font-weight-bold">KPR 2X</label>
                    <input type="number" class="form-control" name="kpr_2x" placeholder="Harga KPR 2X" value="{{ old('kpr_2x', $unit->unit_details['kpr_2x'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label class="text-info text-xs font-weight-bold">KPR 3X</label>
                    <input type="number" class="form-control" name="kpr_3x" placeholder="Harga KPR 3X" value="{{ old('kpr_3x', $unit->unit_details['kpr_3x'] ?? '') }}">
                  </div>
                </div>
                <div class="mb-3">
                  <label>Keterangan (Description)</label>
                  <textarea class="form-control" name="description" rows="3" placeholder="Tambahkan keterangan unit jika ada...">{{ old('description', $unit->unit_details['description'] ?? '') }}</textarea>
                </div>
                
                <div class="mb-3">
                  <label>Status</label>
                  <select class="form-control" name="status" required>
                    <option value="available" {{ old('status', $unit->status ?? '') == 'available' ? 'selected' : '' }}>Available</option>
                    <option value="reserved" {{ old('status', $unit->status ?? '') == 'reserved' ? 'selected' : '' }}>Reserved</option>
                    <option value="sold" {{ old('status', $unit->status ?? '') == 'sold' ? 'selected' : '' }}>Sold</option>
                  </select>
                  @error('status')
                    <p class="text-danger text-xs mt-2">{{ $message }}</p>
                  @enderror
                </div>
                <div class="text-end">
                  <a href="{{ route('units.index') }}" class="btn btn-secondary mt-4 mb-0">Cancel</a>
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
