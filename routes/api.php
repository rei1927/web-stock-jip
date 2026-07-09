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
    Route::post('/users/update', [AuthController::class, 'updateUser']); // For Android sync role update
    Route::post('/users/change-password', [AuthController::class, 'changePassword']); // For Android change password
    Route::get('/users', [AuthController::class, 'index']); // Get all users

    // FCM
    Route::post('/fcm-token', [FCMController::class, 'storeToken']);
    Route::post('/broadcast-notification', [FCMController::class, 'broadcast']);

    // Master Data
    Route::get('/units', [UnitController::class, 'index']);
    Route::post('/units/update-status', [UnitController::class, 'updateStatus']);
    Route::post('/units/submit-sold', [UnitController::class, 'submitSold']);
    Route::post('/units/upload-utj', [\App\Http\Controllers\Api\UploadController::class, 'uploadImage']);

    // Clusters API
    Route::get('/clusters', [ClusterController::class, 'index']);

    // Transactions API
    Route::get('/transactions', [TransactionController::class, 'index']);
    Route::post('/transactions/hold', [TransactionController::class, 'hold']);
    Route::post('/transactions/book', [TransactionController::class, 'book']);

    // Attendance API
    Route::post('/attendance/upload-photo', [\App\Http\Controllers\Api\UploadController::class, 'uploadImage']);
    Route::post('/attendance', [\App\Http\Controllers\Api\AttendanceController::class, 'submit']);
    Route::get('/attendance/status', [\App\Http\Controllers\Api\AttendanceController::class, 'status']);
    Route::get('/attendance/status/{email}', [\App\Http\Controllers\Api\AttendanceController::class, 'status']);
    Route::get('/attendance/all', [\App\Http\Controllers\Api\AttendanceController::class, 'all']);
});
