<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\DashboardWebController;
use App\Http\Controllers\SessionsController;
use App\Http\Controllers\BroadcastController;

Route::get('/', [DashboardWebController::class, 'index'])->name('home')->middleware('auth');
Route::get('/dashboard', [DashboardWebController::class, 'index'])->name('dashboard')->middleware('auth');

Route::get('broadcast', [BroadcastController::class, 'index'])->name('broadcast.index')->middleware('auth');
Route::post('broadcast', [BroadcastController::class, 'send'])->name('broadcast.send')->middleware('auth');

// Fallback route for serving images directly (fixes Docker symlink issues)
// Using a different prefix than /storage/ so Nginx doesn't intercept it and return 404 before hitting Laravel
Route::get('/attendance-photo/{filename}', function ($filename) {
    $basename = basename($filename);
    
    $possiblePaths = [
        storage_path('app/public/uploads/' . $basename),
        storage_path('app/private/uploads/' . $basename),
        storage_path('app/uploads/' . $basename),
    ];
    
    $foundPath = null;
    foreach ($possiblePaths as $path) {
        if (file_exists($path)) {
            $foundPath = $path;
            break;
        }
    }
    
    if (!$foundPath) {
        abort(404);
    }
    
    $mimeType = mime_content_type($foundPath);
    return response()->file($foundPath, ['Content-Type' => $mimeType]);
})->where('filename', '.*')->name('attendance.photo');

Route::get('/login', [SessionsController::class, 'create'])->name('login');
Route::post('/session', [SessionsController::class, 'store']);
Route::post('/logout', [SessionsController::class, 'destroy'])->name('logout');

use App\Http\Controllers\ClusterController;
use App\Http\Controllers\UnitController;
use App\Http\Controllers\CustomerController;
use App\Http\Controllers\TransactionController;
use App\Http\Controllers\AttendanceWebController;

Route::resource('clusters', ClusterController::class)->middleware('auth');
Route::get('units/template', [UnitController::class, 'downloadTemplate'])->name('units.template')->middleware('auth');
Route::post('units/import', [UnitController::class, 'import'])->name('units.import')->middleware('auth');
Route::post('units/bulk-action', [UnitController::class, 'bulkAction'])->name('units.bulkAction')->middleware('auth');
Route::resource('units', UnitController::class)->middleware('auth');
Route::resource('customers', CustomerController::class)->middleware('auth');
Route::post('transactions/bulk-action', [TransactionController::class, 'bulkAction'])->name('transactions.bulkAction')->middleware('auth');
Route::resource('transactions', TransactionController::class)->middleware('auth');
Route::resource('users', App\Http\Controllers\UserController::class)->middleware('auth');

Route::get('attendances', [AttendanceWebController::class, 'index'])->name('attendances.index')->middleware('auth');
Route::get('attendances/export', [AttendanceWebController::class, 'export'])->name('attendances.export')->middleware('auth');
