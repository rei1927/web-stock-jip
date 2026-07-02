@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h6>Units Data</h6>
                <div>
                  <button type="button" class="btn btn-sm bg-gradient-success mb-0 me-2" data-bs-toggle="modal" data-bs-target="#importModal">Bulk Add (Excel)</button>
                  <a href="{{ route('units.create') }}" class="btn btn-sm bg-gradient-info mb-0">Add New</a>
                </div>
              </div>
              <div class="d-flex justify-content-between align-items-center mt-3">
                <form method="GET" action="{{ route('units.index') }}" class="d-flex mb-0 align-items-center gap-2">
                  <select name="cluster_id" class="form-select form-select-sm" style="width: auto;">
                    <option value="">All Clusters</option>
                    @foreach($clusters as $cluster)
                      <option value="{{ $cluster->id }}" {{ request('cluster_id') == $cluster->id ? 'selected' : '' }}>{{ $cluster->name }}</option>
                    @endforeach
                  </select>
                  <select name="per_page" class="form-select form-select-sm" style="width: auto;" onchange="this.form.submit()">
                    <option value="10" {{ request('per_page', 10) == 10 ? 'selected' : '' }}>10 / Page</option>
                    <option value="20" {{ request('per_page') == 20 ? 'selected' : '' }}>20 / Page</option>
                    <option value="50" {{ request('per_page') == 50 ? 'selected' : '' }}>50 / Page</option>
                    <option value="100" {{ request('per_page') == 100 ? 'selected' : '' }}>100 / Page</option>
                  </select>
                  <input type="text" name="search" class="form-control form-control-sm" style="width: 200px;" placeholder="Search Block or Status..." value="{{ request('search') }}">
                  <button type="submit" class="btn btn-sm bg-gradient-secondary mb-0">Filter</button>
                  @if(request('cluster_id') || request('search'))
                    <a href="{{ route('units.index') }}" class="btn btn-sm btn-outline-secondary mb-0">Clear</a>
                  @endif
                </form>

                <div class="d-flex align-items-center gap-2">
                  <select id="bulk_action_select" class="form-select form-select-sm" style="width: auto;">
                    <option value="">-- Bulk Action --</option>
                    <option value="available">Set as Available</option>
                    <option value="reserved">Set as Reserved</option>
                    <option value="sold">Set as Sold</option>
                    <option value="delete">Delete Selected</option>
                  </select>
                  <button type="button" class="btn btn-sm bg-gradient-dark mb-0" onclick="submitBulkAction()">Apply</button>
                </div>
              </div>
            </div>
            <div class="card-body px-0 pt-0 pb-2">
              <div class="table-responsive p-0">
                <table class="table align-items-center mb-0">
                  <thead class="text-center">
                    <tr>
                      <th rowspan="2" class="text-secondary text-xxs font-weight-bolder opacity-7 align-middle border-end" style="width: 40px;">
                        <input type="checkbox" id="checkAll">
                      </th>
                      <th rowspan="2" class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 align-middle border-end">NO</th>
                      <th rowspan="2" class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2 align-middle border-end">CLUSTER</th>
                      <th rowspan="2" class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2 align-middle border-end">BLOK</th>
                      <th rowspan="2" class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2 align-middle border-end">TYPE</th>
                      <th colspan="2" class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-bottom border-end">LUAS</th>
                      <th colspan="3" class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-bottom border-end">CASH</th>
                      <th colspan="3" class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-bottom border-end">KPR</th>
                      <th rowspan="2" class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2 align-middle border-end">HARGA NORMAL</th>
                      <th rowspan="2" class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 align-middle border-end">STATUS</th>
                      <th rowspan="2" class="text-secondary opacity-7 align-middle"></th>
                    </tr>
                    <tr>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">TNH</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">BGN</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">1X</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">2X</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">3X</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">1X</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">2X</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 border-end">3X</th>
                    </tr>
                  </thead>
                  <tbody>
                    @foreach($units as $index => $unit)
                    <tr class="border-bottom">
                      <td class="align-middle text-center border-end">
                        <input type="checkbox" name="unit_ids[]" value="{{ $unit->id }}" class="unit-checkbox">
                      </td>
                      <td class="align-middle text-center border-end">
                        <span class="text-secondary text-xs font-weight-bold">{{ ($units->firstItem() ?? 1) + $index }}</span>
                      </td>
                      <td class="align-middle border-end">
                        <p class="text-xs font-weight-bold mb-0 ps-2">{{ $unit->cluster ? $unit->cluster->name : '-' }}</p>
                      </td>
                      <td class="align-middle border-end">
                        <p class="text-xs font-weight-bold mb-0 ps-2" 
                           @if(!empty($unit->unit_details['description'])) 
                           data-bs-toggle="tooltip" data-bs-placement="top" title="{{ $unit->unit_details['description'] }}"
                           style="text-decoration: underline dotted;"
                           @endif>
                           {{ $unit->block }}
                        </p>
                      </td>
                      <td class="align-middle border-end">
                        <p class="text-xs font-weight-bold mb-0 ps-2">{{ $unit->unit_details['type'] ?? '-' }}</p>
                      </td>
                      <td class="align-middle text-center border-end">
                        <span class="text-secondary text-xs font-weight-bold">{{ $unit->unit_details['landArea'] ?? '-' }}</span>
                      </td>
                      <td class="align-middle text-center border-end">
                        <span class="text-secondary text-xs font-weight-bold">{{ $unit->unit_details['buildingArea'] ?? '-' }}</span>
                      </td>
                      
                      <!-- CASH -->
                      <td class="align-middle text-end border-end pe-2">
                        <span class="text-secondary text-xs font-weight-bold">{{ isset($unit->unit_details['cash_1x']) ? number_format($unit->unit_details['cash_1x'], 0, ',', '.') : '-' }}</span>
                      </td>
                      <td class="align-middle text-end border-end pe-2">
                        <span class="text-secondary text-xs font-weight-bold">{{ isset($unit->unit_details['cash_2x']) ? number_format($unit->unit_details['cash_2x'], 0, ',', '.') : '-' }}</span>
                      </td>
                      <td class="align-middle text-end border-end pe-2">
                        <span class="text-secondary text-xs font-weight-bold">{{ isset($unit->unit_details['cash_3x']) ? number_format($unit->unit_details['cash_3x'], 0, ',', '.') : '-' }}</span>
                      </td>

                      <!-- KPR -->
                      <td class="align-middle text-end border-end pe-2">
                        <span class="text-secondary text-xs font-weight-bold">{{ isset($unit->unit_details['kpr_1x']) ? number_format($unit->unit_details['kpr_1x'], 0, ',', '.') : '-' }}</span>
                      </td>
                      <td class="align-middle text-end border-end pe-2">
                        <span class="text-secondary text-xs font-weight-bold">{{ isset($unit->unit_details['kpr_2x']) ? number_format($unit->unit_details['kpr_2x'], 0, ',', '.') : '-' }}</span>
                      </td>
                      <td class="align-middle text-end border-end pe-2">
                        <span class="text-secondary text-xs font-weight-bold">{{ isset($unit->unit_details['kpr_3x']) ? number_format($unit->unit_details['kpr_3x'], 0, ',', '.') : '-' }}</span>
                      </td>

                      <td class="align-middle text-end border-end pe-2">
                        <p class="text-xs font-weight-bold mb-0 text-dark">{{ number_format($unit->selling_price, 0, ',', '.') }}</p>
                      </td>
                      
                      <td class="align-middle text-center text-sm border-end">
                        <span class="badge badge-sm bg-gradient-{{ $unit->status == 'available' ? 'success' : ($unit->status == 'sold' ? 'danger' : 'warning') }}">{{ ucfirst($unit->status) }}</span>
                        @if($unit->status == 'hold' && isset($unit->unit_details['actionUserLabel']))
                          <br><small class="text-xs text-muted">by {{ $unit->unit_details['actionUserLabel'] }}</small>
                        @endif
                      </td>
                      <td class="align-middle">
                        <a href="{{ route('units.edit', $unit->id) }}" class="text-secondary font-weight-bold text-xs me-3" data-toggle="tooltip" data-original-title="Edit unit">
                          Edit
                        </a>
                        <form action="{{ route('units.destroy', $unit->id) }}" method="POST" class="d-inline">
                          @csrf
                          @method('DELETE')
                          <button type="submit" class="text-danger font-weight-bold text-xs bg-transparent border-0 p-0" onclick="return confirm('Are you sure you want to delete this unit?')" data-toggle="tooltip" data-original-title="Delete unit">
                            Delete
                          </button>
                        </form>
                      </td>
                    </tr>
                    @endforeach
                  </tbody>
                </table>
              </div>
              
              <div class="px-4 py-3 border-top d-flex justify-content-end">
                {{ $units->links('pagination::bootstrap-5') }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>

  <!-- Import Modal -->
  <div class="modal fade" id="importModal" tabindex="-1" role="dialog" aria-labelledby="importModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="importModalLabel">Bulk Add Units (Excel)</h5>
          <button type="button" class="btn-close text-dark" data-bs-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <p class="text-sm mb-3">1. Silakan unduh template kosong di bawah ini dan isi data unit Anda sesuai format kolom yang disediakan.</p>
          <a href="{{ route('units.template') }}" class="btn btn-sm btn-outline-info w-100 mb-4">Download Sample Template</a>
          
          <hr>
          
          <p class="text-sm mb-2 mt-4">2. Setelah data diisi, unggah file tersebut di sini.</p>
          <form action="{{ route('units.import') }}" method="POST" enctype="multipart/form-data">
            @csrf
            <div class="form-group mb-3">
              <input type="file" name="file" class="form-control" accept=".xlsx, .xls, .csv" required>
            </div>
            <button type="submit" class="btn btn-sm bg-gradient-success w-100 mb-0">Upload & Simpan</button>
          </form>
        </div>
      </div>
    </div>
  </div>

  <form id="bulkActionForm" method="POST" action="{{ route('units.bulkAction') }}" style="display: none;">
      @csrf
      <input type="hidden" name="bulk_action" id="hidden_bulk_action">
  </form>

  <script>
    document.getElementById('checkAll').addEventListener('change', function() {
        const checkboxes = document.querySelectorAll('.unit-checkbox');
        for (let checkbox of checkboxes) {
            checkbox.checked = this.checked;
        }
    });

    function submitBulkAction() {
        const action = document.getElementById('bulk_action_select').value;
        const checkboxes = document.querySelectorAll('.unit-checkbox:checked');
        
        if (!action) {
            alert('Please select a bulk action first.');
            return;
        }

        if (checkboxes.length === 0) {
            alert('Please select at least one unit.');
            return;
        }

        if (confirm('Are you sure you want to apply this action to ' + checkboxes.length + ' selected units?')) {
            const form = document.getElementById('bulkActionForm');
            document.getElementById('hidden_bulk_action').value = action;
            
            form.querySelectorAll('.hidden-unit-id').forEach(e => e.remove());

            checkboxes.forEach(cb => {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'unit_ids[]';
                input.value = cb.value;
                input.className = 'hidden-unit-id';
                form.appendChild(input);
            });

            form.submit();
        }
    }
  </script>
@endsection
