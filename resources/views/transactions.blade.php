@extends('layouts.user_type.auth')

@section('content')
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h6>Transactions Data</h6>
                <a href="{{ route('transactions.create') }}" class="btn btn-sm bg-gradient-info mb-0">Add New</a>
              </div>
              
              <form method="GET" action="{{ route('transactions.index') }}" class="d-flex justify-content-between align-items-center mt-3 flex-wrap" id="filterForm">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <select name="per_page" class="form-select form-select-sm" style="width: auto;" onchange="document.getElementById('filterForm').submit()">
                    <option value="10" {{ request('per_page', 10) == 10 ? 'selected' : '' }}>10 / Page</option>
                    <option value="20" {{ request('per_page') == 20 ? 'selected' : '' }}>20 / Page</option>
                    <option value="50" {{ request('per_page') == 50 ? 'selected' : '' }}>50 / Page</option>
                    <option value="100" {{ request('per_page') == 100 ? 'selected' : '' }}>100 / Page</option>
                  </select>
                  <input type="text" name="search" class="form-control form-control-sm" style="width: 200px;" placeholder="Search Name, KTP, or Block..." value="{{ request('search') }}">
                </div>
                
                <div class="d-flex align-items-center gap-2 mb-2">
                  <input type="text" id="dateRange" class="form-control form-control-sm text-center" style="width: 220px;" placeholder="Select Date Range" value="{{ request('start_date') ? request('start_date').' to '.request('end_date') : '' }}">
                  <input type="hidden" name="start_date" id="start_date" value="{{ request('start_date') }}">
                  <input type="hidden" name="end_date" id="end_date" value="{{ request('end_date') }}">
                  
                  <button type="submit" class="btn btn-sm bg-gradient-secondary mb-0">Filter</button>
                  @if(request('search') || request('start_date'))
                    <a href="{{ route('transactions.index') }}" class="btn btn-sm btn-outline-secondary mb-0">Clear</a>
                  @endif
                </div>
              </form>
              
              <form method="POST" action="{{ route('transactions.bulkAction') }}" id="bulkActionForm" class="d-flex align-items-center gap-2 mt-2 mb-3">
                @csrf
                <select name="action" class="form-select form-select-sm" style="width: auto;" required>
                  <option value="">- Bulk Action -</option>
                  <option value="export">Download .xlsx</option>
                  <option value="delete">Delete</option>
                </select>
                <button type="button" class="btn btn-sm bg-gradient-primary mb-0" onclick="submitBulkAction()">Apply</button>
              
            </div>
            
            <div class="card-body px-0 pt-0 pb-2">
              <div class="table-responsive p-0">
                <table class="table align-items-center mb-0">
                  <thead>
                    <tr>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7" style="width: 40px;">
                        <input type="checkbox" id="selectAll">
                      </th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Nama Customer</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Cluster</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Blok</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Tanggal Transaksi</th>
                      <th class="text-secondary opacity-7"></th>
                    </tr>
                  </thead>
                  <tbody>
                    @forelse($transactions as $transaction)
                    <tr>
                      <td class="align-middle text-center">
                        <input type="checkbox" name="ids[]" value="{{ $transaction->id }}" class="row-checkbox">
                      </td>
                      <td>
                        <div class="d-flex px-3 py-1">
                          <div class="d-flex flex-column justify-content-center">
                            <h6 class="mb-0 text-sm">{{ $transaction->customer ? $transaction->customer->name : '-' }}</h6>
                          </div>
                        </div>
                      </td>
                      <td>
                        <p class="text-sm font-weight-bold mb-0">{{ $transaction->unit && $transaction->unit->cluster ? $transaction->unit->cluster->name : '-' }}</p>
                      </td>
                      <td>
                        <p class="text-sm font-weight-bold mb-0">{{ $transaction->unit ? $transaction->unit->block : '-' }}</p>
                      </td>
                      <td class="align-middle text-center text-sm">
                        <span class="text-secondary text-xs font-weight-bold">{{ $transaction->created_at ? $transaction->created_at->format('d/m/y') : '-' }}</span>
                      </td>
                      <td class="align-middle">
                        <a href="{{ route('transactions.show', $transaction->id) }}" class="text-info font-weight-bold text-xs me-3" data-toggle="tooltip" data-original-title="View details">
                          Detail
                        </a>
                        <!-- Single delete form is removed to prevent form nesting inside bulkActionForm. Users will use Bulk Action to delete. -->
                      </td>
                    </tr>
                    @empty
                    <tr>
                      <td colspan="6" class="text-center py-4">
                        <p class="text-xs font-weight-bold mb-0">No transactions found.</p>
                      </td>
                    </tr>
                    @endforelse
                  </tbody>
                </table>
              </form> <!-- End bulk action form -->
              </div>
              
              <div class="d-flex justify-content-end px-4 py-3">
                {{ $transactions->appends(request()->query())->links('pagination::bootstrap-5') }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
  
  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
  <script>
    document.addEventListener("DOMContentLoaded", function() {
      flatpickr("#dateRange", {
        mode: "range",
        dateFormat: "Y-m-d",
        onChange: function(selectedDates, dateStr, instance) {
          if (selectedDates.length === 2) {
            document.getElementById('start_date').value = instance.formatDate(selectedDates[0], "Y-m-d");
            document.getElementById('end_date').value = instance.formatDate(selectedDates[1], "Y-m-d");
          } else {
            document.getElementById('start_date').value = "";
            document.getElementById('end_date').value = "";
          }
        }
      });

      document.getElementById('selectAll').addEventListener('change', function() {
        let checkboxes = document.querySelectorAll('.row-checkbox');
        for(let cb of checkboxes) {
          cb.checked = this.checked;
        }
      });
    });

    function submitBulkAction() {
      const action = document.querySelector('select[name="action"]').value;
      if (!action) {
        alert("Please select an action.");
        return;
      }
      
      const checked = document.querySelectorAll('.row-checkbox:checked');
      if (checked.length === 0) {
        alert("Please select at least one transaction.");
        return;
      }
      
      if (action === 'delete') {
        if (!confirm("Are you sure you want to delete selected transactions?")) {
          return;
        }
      }
      
      document.getElementById('bulkActionForm').submit();
    }
  </script>
@endsection
