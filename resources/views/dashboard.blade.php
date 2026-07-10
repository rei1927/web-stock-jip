@extends('layouts.user_type.auth')

@section('content')

  <div class="row">
    <div class="col-xl-4 col-sm-6 mb-xl-0 mb-4">
      <div class="card">
        <div class="card-body p-3">
          <div class="row">
            <div class="col-8">
              <div class="numbers">
                <p class="text-sm mb-0 text-capitalize font-weight-bold">
                  Omzet
                  <a href="#" class="ms-1 dropdown-toggle text-secondary" data-bs-toggle="dropdown" aria-expanded="false">
                    {{ ucfirst(request()->query('period', 'daily')) }}
                  </a>
                  <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="?period=daily">Daily (Harian)</a></li>
                    <li><a class="dropdown-item" href="?period=weekly">Weekly (Mingguan)</a></li>
                    <li><a class="dropdown-item" href="?period=monthly">Monthly (Bulanan)</a></li>
                  </ul>
                </p>
                <h5 class="font-weight-bolder mb-0">
                  Rp {{ number_format($currentOmzet, 0, ',', '.') }}
                  <span class="{{ $omzetChange >= 0 ? 'text-success' : 'text-danger' }} text-sm font-weight-bolder">
                    {{ $omzetChange >= 0 ? '+' : '' }}{{ number_format($omzetChange, 1) }}%
                  </span>
                </h5>
              </div>
            </div>
            <div class="col-4 text-end">
              <div class="icon icon-shape bg-gradient-primary shadow text-center border-radius-md">
                <i class="ni ni-money-coins text-lg opacity-10" aria-hidden="true"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-xl-4 col-sm-6 mb-xl-0 mb-4">
      <div class="card">
        <div class="card-body p-3">
          <div class="row">
            <div class="col-8">
              <div class="numbers">
                <p class="text-sm mb-0 text-capitalize font-weight-bold">Users</p>
                <h5 class="font-weight-bolder mb-0">
                  {{ number_format($totalUsers) }}
                </h5>
              </div>
            </div>
            <div class="col-4 text-end">
              <div class="icon icon-shape bg-gradient-primary shadow text-center border-radius-md">
                <i class="ni ni-world text-lg opacity-10" aria-hidden="true"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-xl-4 col-sm-6 mb-xl-0 mb-4">
      <div class="card">
        <div class="card-body p-3">
          <div class="row">
            <div class="col-8">
              <div class="numbers">
                <p class="text-sm mb-0 text-capitalize font-weight-bold">Total unit terjual</p>
                <h5 class="font-weight-bolder mb-0">
                  {{ number_format($totalTransactions) }}
                </h5>
              </div>
            </div>
            <div class="col-4 text-end">
              <div class="icon icon-shape bg-gradient-primary shadow text-center border-radius-md">
                <i class="ni ni-cart text-lg opacity-10" aria-hidden="true"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="row mt-4">
    <div class="col-lg-12">
      <div class="card z-index-2">
        <div class="card-header pb-0">
          <h6>Grafik Omzet ({{ ucfirst(request()->query('period', 'daily')) }})</h6>
          <p class="text-sm">
            <i class="fa fa-arrow-{{ $omzetChange >= 0 ? 'up text-success' : 'down text-danger' }}"></i>
            <span class="font-weight-bold">{{ number_format(abs($omzetChange), 1) }}% {{ $omzetChange >= 0 ? 'lebih tinggi' : 'lebih rendah' }}</span> dari sebelumnya
          </p>
        </div>
        <div class="card-body p-3">
          <div class="chart">
            <canvas id="chart-line" class="chart-canvas" height="300"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>


@endsection
@push('dashboard')
  <script>
    window.onload = function() {
      var ctx2 = document.getElementById("chart-line").getContext("2d");

      var gradientStroke1 = ctx2.createLinearGradient(0, 230, 0, 50);

      gradientStroke1.addColorStop(1, 'rgba(203,12,159,0.2)');
      gradientStroke1.addColorStop(0.2, 'rgba(72,72,176,0.0)');
      gradientStroke1.addColorStop(0, 'rgba(203,12,159,0)'); //purple colors

      var gradientStroke2 = ctx2.createLinearGradient(0, 230, 0, 50);

      gradientStroke2.addColorStop(1, 'rgba(20,23,39,0.2)');
      gradientStroke2.addColorStop(0.2, 'rgba(72,72,176,0.0)');
      gradientStroke2.addColorStop(0, 'rgba(20,23,39,0)'); //purple colors

      new Chart(ctx2, {
        type: "line",
        data: {
          labels: @json($chartLabels),
          datasets: [{
              label: "Omzet (Rp)",
              tension: 0.4,
              borderWidth: 0,
              pointRadius: 0,
              borderColor: "#cb0c9f",
              borderWidth: 3,
              backgroundColor: gradientStroke1,
              fill: true,
              data: @json($chartData),
              maxBarThickness: 6

            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false,
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return ' Rp ' + context.parsed.y.toLocaleString('id-ID');
                }
              }
            }
          },
          interaction: {
            intersect: false,
            mode: 'index',
          },
          scales: {
            y: {
              grid: {
                drawBorder: false,
                display: true,
                drawOnChartArea: true,
                drawTicks: false,
                borderDash: [5, 5]
              },
              ticks: {
                display: true,
                padding: 10,
                color: '#b2b9bf',
                font: {
                  size: 11,
                  family: "Open Sans",
                  style: 'normal',
                  lineHeight: 2
                },
                callback: function(value, index, values) {
                  return 'Rp ' + value.toLocaleString('id-ID');
                }
              }
            },
            x: {
              grid: {
                drawBorder: false,
                display: false,
                drawOnChartArea: false,
                drawTicks: false,
                borderDash: [5, 5]
              },
              ticks: {
                display: true,
                color: '#b2b9bf',
                padding: 20,
                font: {
                  size: 11,
                  family: "Open Sans",
                  style: 'normal',
                  lineHeight: 2
                },
              }
            },
          },
        },
      });
    }
  </script>
@endpush

