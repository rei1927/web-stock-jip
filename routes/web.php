<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\DashboardWebController;
use App\Http\Controllers\SessionsController;
use App\Http\Controllers\BroadcastController;

Route::get('/', [DashboardWebController::class, 'index'])->name('home')->middleware('auth');
Route::get('/dashboard', [DashboardWebController::class, 'index'])->name('dashboard')->middleware('auth');

Route::get('broadcast', [BroadcastController::class, 'index'])->name('broadcast.index')->middleware('auth');
Route::post('broadcast', [BroadcastController::class, 'send'])->name('broadcast.send')->middleware('auth');

Route::get('/login', [SessionsController::class, 'create'])->name('login');
Route::post('/session', [SessionsController::class, 'store']);
Route::post('/logout', [SessionsController::class, 'destroy'])->name('logout');

use App\Http\Controllers\ClusterController;
use App\Http\Controllers\UnitController;
use App\Http\Controllers\CustomerController;
use App\Http\Controllers\TransactionController;

Route::resource('clusters', ClusterController::class)->middleware('auth');
Route::get('units/template', [UnitController::class, 'downloadTemplate'])->name('units.template')->middleware('auth');
Route::post('units/import', [UnitController::class, 'import'])->name('units.import')->middleware('auth');
Route::post('units/bulk-action', [UnitController::class, 'bulkAction'])->name('units.bulkAction')->middleware('auth');
Route::resource('units', UnitController::class)->middleware('auth');
Route::resource('customers', CustomerController::class)->middleware('auth');
Route::resource('transactions', TransactionController::class)->middleware('auth');
Route::get('users', [App\Http\Controllers\UserController::class, 'index'])->name('users.index')->middleware('auth');
