@extends('layouts.user_type.auth')

@section('content')
  <main class="main-content position-relative max-height-vh-100 h-100 mt-1 border-radius-lg ">
    <div class="container-fluid py-4">
      <div class="row">
        <div class="col-12 col-md-8 mx-auto">
          <div class="card mb-4">
            <div class="card-header pb-0">
              <h6>Kirim Notifikasi Broadcast</h6>
            </div>
            <div class="card-body">
              @if(session('success'))
                <div class="alert alert-success text-white" role="alert">
                  {{ session('success') }}
                </div>
              @endif
              
              @if(session('error'))
                <div class="alert alert-danger text-white" role="alert">
                  {{ session('error') }}
                </div>
              @endif

              <form role="form" method="POST" action="{{ route('broadcast.send') }}">
                @csrf
                <div class="mb-3">
                  <label>Judul Notifikasi</label>
                  <input type="text" class="form-control @error('title') is-invalid @enderror" name="title" placeholder="Contoh: Info Penting" value="{{ old('title') }}" required>
                  @error('title')
                    <div class="invalid-feedback">{{ $message }}</div>
                  @enderror
                </div>
                <div class="mb-3">
                  <label>Isi Pesan</label>
                  <textarea class="form-control @error('body') is-invalid @enderror" name="body" rows="5" placeholder="Ketik pesan Anda di sini..." required>{{ old('body') }}</textarea>
                  @error('body')
                    <div class="invalid-feedback">{{ $message }}</div>
                  @enderror
                </div>
                
                <div class="text-end">
                  <button type="submit" class="btn bg-gradient-primary mt-4 mb-0">Kirim Broadcast</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
@endsection
