<?php

use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\PlayerSyncController;
use Illuminate\Support\Facades\Route;

Route::post('/verify', [LinkVerificationController::class, 'verify'])->name('verify');
Route::post('/sync-player', [PlayerSyncController::class, 'sync'])->name('sync-player');
Route::get('/deliveries/pending', [LinkVerificationController::class, 'getPendingDeliveries'])->name('deliveries.pending');
Route::post('/deliveries/{id}/status', [LinkVerificationController::class, 'updateDeliveryStatus'])->name('deliveries.status');
Route::post('/heartbeat', [LinkVerificationController::class, 'heartbeat'])->name('heartbeat');
Route::get('/status', [LinkVerificationController::class, 'status'])->name('status');
