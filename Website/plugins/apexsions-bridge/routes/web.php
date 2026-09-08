<?php

use Azuriom\Plugin\ApexsionsBridge\Controllers\AccountLinkController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AuditLogController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PlayerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\LeaderboardController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\ProfileManagementController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\PublicProfileController;
use Illuminate\Support\Facades\Route;

// Public Routes
Route::get('/leaderboard', [LeaderboardController::class, 'index'])->name('leaderboard');
Route::get('/player/{identifier}', [PublicProfileController::class, 'show'])->name('player.show');

// Authenticated User Routes
Route::middleware('auth')->group(function () {
    Route::get('/link', [AccountLinkController::class, 'index'])->name('link.index');
    Route::post('/link/pin', [AccountLinkController::class, 'generatePin'])->name('link.pin');
    Route::delete('/link/{account}', [AccountLinkController::class, 'unlink'])->name('link.unlink');

    // Self-service Minecraft account actions from web profile
    Route::prefix('profile/minecraft')->name('profile.minecraft.')->group(function () {
        Route::post('/reset-password', [ProfileManagementController::class, 'resetPassword'])->name('reset-password');
        Route::post('/change-title', [ProfileManagementController::class, 'changeTitle'])->name('change-title');
        Route::post('/unlink', [ProfileManagementController::class, 'unlink'])->name('unlink');
        Route::post('/claim-reward', [ProfileManagementController::class, 'claimReward'])->name('claim-reward');
    });
});

// Admin Realm Routes (Guarded by admin-access and web middleware)
Route::middleware(['web', 'admin-access'])->prefix('admin')->name('admin.')->group(function () {
    Route::post('/apexsions/broadcast', [LinkVerificationController::class, 'broadcast'])->name('broadcast');

    // Player Management Foundation
    Route::prefix('players')->name('players.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [PlayerAdminController::class, 'index'])->name('index');
        Route::get('/{identifier}', [PlayerAdminController::class, 'show'])->name('show');
        Route::post('/{identifier}/action', [PlayerAdminController::class, 'executeAction'])->name('action');
    });

    // Unified Audit Logs
    Route::prefix('audit-logs')->name('audit-logs.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [AuditLogController::class, 'index'])->name('index');
        Route::get('/{id}', [AuditLogController::class, 'show'])->name('show');
    });
});
