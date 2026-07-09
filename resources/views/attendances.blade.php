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
                <h6>Attendance Data</h6>
              </div>
              
              <form method="GET" action="{{ route('attendances.index') }}" class="d-flex justify-content-between align-items-center mt-3 flex-wrap" id="filterForm">
                <div class="d-flex align-items-center gap-2 mb-2">
                  <input type="text" name="sales_name" class="form-control form-control-sm" style="width: 200px;" placeholder="Search Sales Name..." value="{{ request('sales_name') }}">
                </div>
                
                <div class="d-flex align-items-center gap-2 mb-2">
                  <input type="text" id="dateRange" class="form-control form-control-sm text-center" style="width: 220px;" placeholder="Select Date Range" value="{{ request('start_date') ? request('start_date').' to '.request('end_date') : '' }}">
                  <input type="hidden" name="start_date" id="start_date" value="{{ request('start_date') }}">
                  <input type="hidden" name="end_date" id="end_date" value="{{ request('end_date') }}">
                  
                  <button type="submit" class="btn btn-sm bg-gradient-secondary mb-0">Filter</button>
                  @if(request('sales_name') || request('start_date'))
                    <a href="{{ route('attendances.index') }}" class="btn btn-sm btn-outline-secondary mb-0">Clear</a>
                  @endif
                  
                  <button type="submit" formaction="{{ route('attendances.export') }}" class="btn btn-sm bg-gradient-success mb-0 ms-2">Export CSV</button>
                </div>
              </form>
              
            </div>
            
            <div class="card-body px-0 pt-0 pb-2 mt-3">
              <div class="table-responsive p-0">
                <table class="table align-items-center mb-0">
                  <thead>
                    <tr>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Nama Sales</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Tipe</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Waktu</th>
                      <th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Lokasi</th>
                      <th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Foto</th>
                    </tr>
                  </thead>
                  <tbody>
                    @forelse($attendances as $record)
                    <tr>
                      <td>
                        <div class="d-flex px-3 py-1">
                          <div class="d-flex flex-column justify-content-center">
                            <h6 class="mb-0 text-sm">{{ $record->user ? $record->user->name : '-' }}</h6>
                          </div>
                        </div>
                      </td>
                      <td>
                        <span class="badge badge-sm bg-gradient-{{ $record->type === 'Masuk' ? 'success' : 'danger' }}">{{ $record->type }}</span>
                      </td>
                      <td class="align-middle text-center text-sm">
                        <span class="text-secondary text-xs font-weight-bold">{{ $record->timestamp }}</span>
                      </td>
                      <td>
                        <p class="text-sm font-weight-bold mb-0" style="max-width: 250px; white-space: normal;">{{ $record->address ?? '-' }}</p>
                        @if($record->lat && $record->long)
                            <a href="https://maps.google.com/?q={{ $record->lat }},{{ $record->long }}" target="_blank" class="text-xs text-info">View Map</a>
                        @endif
                      </td>
                      <td class="align-middle text-center">
                        @if($record->photo_url)
                            <a href="javascript:;" onclick="document.getElementById('modalPhotoImage').style.display='block'; document.getElementById('modalPhotoError').style.display='none'; document.getElementById('modalPhotoImage').src='{{ $record->photo_url }}'; document.getElementById('modalPhotoUrlText').innerText='{{ $record->photo_url }}';" data-bs-toggle="modal" data-bs-target="#photoModal" class="text-secondary" title="Lihat Foto">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#17c1e8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
                            </a>
                        @else
                            -
                        @endif
                      </td>
                    </tr>
                    @empty
                    <tr>
                      <td colspan="5" class="text-center py-4">
                        <p class="text-xs font-weight-bold mb-0">No attendances found.</p>
                      </td>
                    </tr>
                    @endforelse
                  </tbody>
                </table>
              </div>
              
              <div class="d-flex justify-content-end px-4 py-3">
                {{ $attendances->appends(request()->query())->links('pagination::bootstrap-5') }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
  
  <!-- Photo Modal -->
  <div class="modal fade" id="photoModal" tabindex="-1" role="dialog" aria-labelledby="photoModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="photoModalLabel">Foto Absensi</h5>
          <button type="button" class="btn-close text-dark" data-bs-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body text-center p-0 pb-3">
          <img id="modalPhotoImage" src="" class="img-fluid" alt="Foto Absensi" style="width: 100%; border-bottom-left-radius: 0.5rem; border-bottom-right-radius: 0.5rem;" onerror="this.style.display='none'; document.getElementById('modalPhotoError').style.display='block';">
          <div id="modalPhotoError" class="p-4" style="display: none;">
            <i class="fas fa-broken-link text-danger text-4xl mb-3"></i>
            <h6 class="text-danger">Gambar Tidak Dapat Diakses</h6>
            <p class="text-xs text-secondary mb-1">URL gambar yang tersimpan adalah:</p>
            <code id="modalPhotoUrlText" class="text-xxs text-wrap" style="word-break: break-all;"></code>
            <p class="text-xs text-secondary mt-3">Jika URL di atas diawali dengan <b>http://localhost</b> atau IP internal, pastikan Anda mengubah <b>APP_URL</b> di file <b>.env</b> server Proxmox Anda menjadi IP/Domain yang benar, lalu coba absen ulang.</p>
          </div>
        </div>
      </div>
    </div>
  </div>

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
    });
  </script>
@endsection
