@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12">
          <div class="card mb-4">
            <div class="card-header pb-0 d-flex justify-content-between align-items-center">
              <h6>Detail Transaksi: {{ $transaction->unit ? $transaction->unit->name : '-' }}</h6>
              <div>
                  <a href="{{ route('transactions.edit', $transaction->id) }}" class="btn btn-sm bg-gradient-info mb-0">Edit Transaksi</a>
                  <a href="{{ route('transactions.index') }}" class="btn btn-sm btn-secondary mb-0">Kembali</a>
              </div>
            </div>
            <div class="card-body">
              
              <h6 class="text-uppercase text-body text-xs font-weight-bolder mt-4 mb-3">DATA CUSTOMER (PEMBELI)</h6>
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">Nama Lengkap (Sesuai KTP)</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->name : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">No. KTP Konsumen</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->nik : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">Alamat KTP</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->address : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">No. NPWP</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->npwp : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">Alamat Surat</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->alamat_surat : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">No. KK</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->no_kk : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">No. Telepon Rumah</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->no_telepon_rumah : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">No. Telepon Seluler</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->phone : '-' }}</p>
                </div>
                <div class="col-md-6 mb-3">
                  <label class="text-xs font-weight-bold">E-mail</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->customer ? $transaction->customer->email : '-' }}</p>
                </div>
              </div>

              <hr class="horizontal dark mt-4 mb-4">
              
              <h6 class="text-uppercase text-body text-xs font-weight-bolder mb-3">DATA UNIT & TRANSAKSI</h6>
              <div class="row">
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Type / Blok</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->unit ? $transaction->unit->name . ' / ' . $transaction->unit->block : '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Cluster</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->unit && $transaction->unit->cluster ? $transaction->unit->cluster->name : '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Status Transaksi</label>
                  <p class="text-sm font-weight-bold mb-0 badge badge-sm bg-gradient-{{ $transaction->status == 'completed' ? 'success' : 'info' }}">{{ ucfirst($transaction->status) }}</p>
                </div>

                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Tujuan Pembelian</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->details['tujuanPembelian'] ?? '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Sumber Dana</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->details['sumberDana'] ?? '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Sistem Pembayaran</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->details['sistemPembayaran'] ?? '-' }}</p>
                </div>

                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Harga Jual Inc. PPN</label>
                  <p class="text-sm font-weight-bold mb-0">{{ isset($transaction->details['hargaJualIncPpn']) ? 'Rp ' . number_format((float)$transaction->details['hargaJualIncPpn'], 0, ',', '.') : '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Rencana Plafond KPR</label>
                  <p class="text-sm font-weight-bold mb-0">{{ isset($transaction->details['rencanaPlafondKpr']) ? 'Rp ' . number_format((float)$transaction->details['rencanaPlafondKpr'], 0, ',', '.') : '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Tanda Jadi</label>
                  <p class="text-sm font-weight-bold mb-0">{{ isset($transaction->details['tandaJadi']) ? 'Rp ' . number_format((float)$transaction->details['tandaJadi'], 0, ',', '.') : '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">U.Muka (Transaksi KPR)</label>
                  <p class="text-sm font-weight-bold mb-0">{{ isset($transaction->details['uMukaTransaksiKpr']) ? 'Rp ' . number_format((float)$transaction->details['uMukaTransaksiKpr'], 0, ',', '.') : '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">U.Muka Pertama (Transaksi KPR)</label>
                  <p class="text-sm font-weight-bold mb-0">{{ isset($transaction->details['uMukaPertamaTransaksiKpr']) ? 'Rp ' . number_format((float)$transaction->details['uMukaPertamaTransaksiKpr'], 0, ',', '.') : '-' }}</p>
                </div>
                <div class="col-md-4 mb-3">
                  <label class="text-xs font-weight-bold">Angsuran Pertama (Transaksi Cash)</label>
                  <p class="text-sm font-weight-bold mb-0">{{ isset($transaction->details['angsuranPertamaTransaksiCash']) ? 'Rp ' . number_format((float)$transaction->details['angsuranPertamaTransaksiCash'], 0, ',', '.') : '-' }}</p>
                </div>
              </div>
              
              @if(!empty($transaction->notes))
              <div class="row mt-3">
                <div class="col-12">
                  <label class="text-xs font-weight-bold">Notes</label>
                  <p class="text-sm font-weight-bold mb-0">{{ $transaction->notes }}</p>
                </div>
              </div>
              @endif

            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
@endsection
