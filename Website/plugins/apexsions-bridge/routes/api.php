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

// In-game Audit Log Ingestion Endpoint
Route::post('/audit/log', [LinkVerificationController::class, 'ingestAuditLog'])->name('audit.log');

// In-game Reports and Punishments Ingestion Endpoints
Route::post('/reports/sync', [LinkVerificationController::class, 'syncReport'])->name('reports.sync');
Route::post('/punishments/sync', [LinkVerificationController::class, 'syncPunishment'])->name('punishments.sync');

// In-game Economy Ingestion Endpoints
Route::post('/economy/transactions/sync', [LinkVerificationController::class, 'syncTransaction'])->name('economy.transactions.sync');
Route::post('/economy/auctions/sync', [LinkVerificationController::class, 'syncAuction'])->name('economy.auctions.sync');
Route::post('/economy/treasury/sync', [LinkVerificationController::class, 'syncKingdomTreasury'])->name('economy.treasury.sync');

// Custom Plugin Control & Capability Endpoints
Route::get('/plugins', [LinkVerificationController::class, 'getPlugins'])->name('plugins');
Route::post('/plugins/handshake', [LinkVerificationController::class, 'pluginHandshake'])->name('plugins.handshake');


