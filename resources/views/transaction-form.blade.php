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
                
                <h6 class="text-uppercase text-body text-xs font-weight-bolder mt-2 mb-3">DATA CUSTOMER (PEMBELI)</h6>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label>Nama Lengkap (Sesuai KTP)</label>
                    <input type="text" class="form-control" name="name" value="{{ old('name', $transaction->customer->name ?? '') }}" required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>No. KTP Konsumen</label>
                    <input type="text" class="form-control" name="nik" value="{{ old('nik', $transaction->customer->nik ?? '') }}" required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>Alamat KTP</label>
                    <textarea class="form-control" name="address" rows="2">{{ old('address', $transaction->customer->address ?? '') }}</textarea>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>Alamat Surat</label>
                    <textarea class="form-control" name="alamat_surat" rows="2">{{ old('alamat_surat', $transaction->customer->alamat_surat ?? '') }}</textarea>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>No. NPWP</label>
                    <input type="text" class="form-control" name="npwp" value="{{ old('npwp', $transaction->customer->npwp ?? '') }}">
                  </div>
                  <div class="col-md-6 mb-3">
                    <label>No. KK</label>
                    <input type="text" class="form-control" name="no_kk" value="{{ old('no_kk', $transaction->customer->no_kk ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>No. Telepon Rumah</label>
                    <input type="text" class="form-control" name="no_telepon_rumah" value="{{ old('no_telepon_rumah', $transaction->customer->no_telepon_rumah ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>No. Telepon Seluler</label>
                    <input type="text" class="form-control" name="phone" value="{{ old('phone', $transaction->customer->phone ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>E-mail</label>
                    <input type="email" class="form-control" name="email" value="{{ old('email', $transaction->customer->email ?? '') }}">
                  </div>
                </div>

                <hr class="horizontal dark mt-3 mb-4">
                
                <h6 class="text-uppercase text-body text-xs font-weight-bolder mb-3">DATA UNIT & TRANSAKSI</h6>
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
                  <div class="col-md-4 mb-3">
                    <label>Status Transaksi</label>
                    <select class="form-control" name="status" required>
                      <option value="pending" {{ old('status', $transaction->status ?? '') == 'pending' ? 'selected' : '' }}>Pending</option>
                      <option value="completed" {{ old('status', $transaction->status ?? '') == 'completed' ? 'selected' : '' }}>Completed</option>
                      <option value="canceled" {{ old('status', $transaction->status ?? '') == 'canceled' ? 'selected' : '' }}>Canceled</option>
                    </select>
                    @error('status')
                      <p class="text-danger text-xs mt-2">{{ $message }}</p>
                    @enderror
                  </div>

                  <div class="col-md-4 mb-3">
                    <label>Tujuan Pembelian</label>
                    <select class="form-control" name="tujuanPembelian">
                      <option value="">- Pilih -</option>
                      <option value="Investasi" {{ old('tujuanPembelian', $transaction->details['tujuanPembelian'] ?? '') == 'Investasi' ? 'selected' : '' }}>Investasi</option>
                      <option value="Tempat Tinggal" {{ old('tujuanPembelian', $transaction->details['tujuanPembelian'] ?? '') == 'Tempat Tinggal' ? 'selected' : '' }}>Tempat Tinggal</option>
                      <option value="Lain-lain" {{ old('tujuanPembelian', $transaction->details['tujuanPembelian'] ?? '') == 'Lain-lain' ? 'selected' : '' }}>Lain-lain</option>
                    </select>
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Sumber Dana</label>
                    <select class="form-control" name="sumberDana">
                      <option value="">- Pilih -</option>
                      <option value="Gaji / Upah" {{ old('sumberDana', $transaction->details['sumberDana'] ?? '') == 'Gaji / Upah' ? 'selected' : '' }}>Gaji / Upah</option>
                      <option value="Pemberian Orang Tua" {{ old('sumberDana', $transaction->details['sumberDana'] ?? '') == 'Pemberian Orang Tua' ? 'selected' : '' }}>Pemberian Orang Tua</option>
                      <option value="Warisan" {{ old('sumberDana', $transaction->details['sumberDana'] ?? '') == 'Warisan' ? 'selected' : '' }}>Warisan</option>
                      <option value="Hasil/Laba Usaha" {{ old('sumberDana', $transaction->details['sumberDana'] ?? '') == 'Hasil/Laba Usaha' ? 'selected' : '' }}>Hasil/Laba Usaha</option>
                    </select>
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Sistem Pembayaran</label>
                    <select class="form-control" name="sistemPembayaran">
                      <option value="">- Pilih -</option>
                      <option value="Tunai" {{ old('sistemPembayaran', $transaction->details['sistemPembayaran'] ?? '') == 'Tunai' ? 'selected' : '' }}>Tunai</option>
                      <option value="KPR" {{ old('sistemPembayaran', $transaction->details['sistemPembayaran'] ?? '') == 'KPR' ? 'selected' : '' }}>KPR</option>
                    </select>
                  </div>

                  <div class="col-md-4 mb-3">
                    <label>Harga Jual Inc. PPN</label>
                    <input type="number" class="form-control" name="hargaJualIncPpn" value="{{ old('hargaJualIncPpn', $transaction->details['hargaJualIncPpn'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Rencana Plafond KPR</label>
                    <input type="number" class="form-control" name="rencanaPlafondKpr" value="{{ old('rencanaPlafondKpr', $transaction->details['rencanaPlafondKpr'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Tanda Jadi</label>
                    <input type="number" class="form-control" name="tandaJadi" value="{{ old('tandaJadi', $transaction->details['tandaJadi'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>U.Muka (Transaksi KPR)</label>
                    <input type="number" class="form-control" name="uMukaTransaksiKpr" value="{{ old('uMukaTransaksiKpr', $transaction->details['uMukaTransaksiKpr'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>U.Muka Pertama (Transaksi KPR)</label>
                    <input type="number" class="form-control" name="uMukaPertamaTransaksiKpr" value="{{ old('uMukaPertamaTransaksiKpr', $transaction->details['uMukaPertamaTransaksiKpr'] ?? '') }}">
                  </div>
                  <div class="col-md-4 mb-3">
                    <label>Angsuran Pertama (Transaksi Cash)</label>
                    <input type="number" class="form-control" name="angsuranPertamaTransaksiCash" value="{{ old('angsuranPertamaTransaksiCash', $transaction->details['angsuranPertamaTransaksiCash'] ?? '') }}">
                  </div>
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
