<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\ClusterController;
use App\Http\Controllers\Api\UnitController;
use App\Http\Controllers\Api\TransactionController;
use App\Http\Controllers\Api\FCMController;

Route::post('/login', [AuthController::class, 'login']);

// Public routes for Android App Sync
Route::get('/units', [UnitController::class, 'index']);

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/profile', [AuthController::class, 'profile']);
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::post('/users/register', [AuthController::class, 'register']); // For Android sync
    Route::get('/users', [AuthController::class, 'index']); // Get all users

    // FCM
    Route::post('/fcm-token', [FCMController::class, 'storeToken']);
    Route::post('/broadcast-notification', [FCMController::class, 'broadcast']);

    // Master Data
    Route::get('/clusters', [ClusterController::class, 'index']);
    Route::post('/units/update-status', [UnitController::class, 'updateStatus']);
    Route::post('/units/submit-sold', [UnitController::class, 'submitSold']);

    // Transactions
    Route::get('/transactions', [TransactionController::class, 'index']);
    Route::post('/transactions/hold', [TransactionController::class, 'hold']);
    Route::post('/transactions/book', [TransactionController::class, 'book']);
});
