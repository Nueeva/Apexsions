<?php

use Azuriom\Plugin\ApexsionsBridge\Controllers\AccountLinkController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AuditLogController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AuctionAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\EconomyAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AutomationAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\IncidentAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\IntelligenceAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ModerationAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\NotificationAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PlayerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PluginAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ReportAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ServerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\TransactionAdminController;
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

    // Reports Center
    Route::prefix('reports')->name('reports.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [ReportAdminController::class, 'index'])->name('index');
        Route::get('/{id}', [ReportAdminController::class, 'show'])->name('show');
        Route::post('/{id}/claim', [ReportAdminController::class, 'claim'])->name('claim');
        Route::post('/{id}/assign', [ReportAdminController::class, 'assign'])->name('assign');
        Route::post('/{id}/status', [ReportAdminController::class, 'updateStatus'])->name('status');
        Route::post('/{id}/notes', [ReportAdminController::class, 'addNote'])->name('notes.store');
    });

    // Moderation Center
    Route::prefix('moderation')->name('moderation.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [ModerationAdminController::class, 'index'])->name('index');
        Route::post('/store', [ModerationAdminController::class, 'storeAction'])->name('store');
        Route::post('/{id}/pardon', [ModerationAdminController::class, 'pardon'])->name('pardon');
    });

    // Economy Operations & Market Control
    Route::prefix('economy')->name('economy.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [EconomyAdminController::class, 'index'])->name('index');
        Route::post('/adjust', [EconomyAdminController::class, 'adjustBalance'])->name('adjust');

        // Transactions Explorer
        Route::prefix('transactions')->name('transactions.')->group(function () {
            Route::get('/', [TransactionAdminController::class, 'index'])->name('index');
            Route::get('/{id}', [TransactionAdminController::class, 'show'])->name('show');
        });

        // Auction Inspector
        Route::prefix('auctions')->name('auctions.')->group(function () {
            Route::get('/', [AuctionAdminController::class, 'index'])->name('index');
            Route::get('/{id}', [AuctionAdminController::class, 'show'])->name('show');
            Route::post('/{id}/quarantine', [AuctionAdminController::class, 'quarantine'])->name('quarantine');
            Route::post('/{id}/cancel', [AuctionAdminController::class, 'cancel'])->name('cancel');
        });
    });

    // Server Operations & Safe Control
    Route::prefix('server')->name('server.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [ServerAdminController::class, 'index'])->name('index');
        Route::get('/actions', [ServerAdminController::class, 'actions'])->name('actions');
        Route::get('/plugins', [ServerAdminController::class, 'plugins'])->name('plugins');
        Route::get('/metrics', [ServerAdminController::class, 'metrics'])->name('metrics');
        Route::post('/actions/execute', [ServerAdminController::class, 'executeAction'])->name('actions.execute');
        Route::post('/maintenance', [ServerAdminController::class, 'toggleMaintenance'])->name('maintenance.toggle');
        Route::post('/alerts/{id}/acknowledge', [ServerAdminController::class, 'acknowledgeAlert'])->name('alerts.acknowledge');
        Route::post('/alerts/{id}/resolve', [ServerAdminController::class, 'resolveAlert'])->name('alerts.resolve');
    });

    // Custom Plugin Control & Capability System
    Route::prefix('custom-plugins')->name('plugins.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [PluginAdminController::class, 'index'])->name('index');
        Route::get('/{plugin_id}', [PluginAdminController::class, 'show'])->name('show');
        Route::post('/{plugin_id}/action', [PluginAdminController::class, 'executeAction'])->name('action');
    });

    // Unified Audit Logs
    Route::prefix('audit-logs')->name('audit-logs.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [AuditLogController::class, 'index'])->name('index');
        Route::get('/{id}', [AuditLogController::class, 'show'])->name('show');
    });

    // Intelligence & Incident Management System (Phase 6)
    Route::prefix('intelligence')->name('intelligence.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [IntelligenceAdminController::class, 'index'])->name('index');
    });

    Route::prefix('incidents')->name('incidents.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [IncidentAdminController::class, 'index'])->name('index');
        Route::get('/{id}', [IncidentAdminController::class, 'show'])->name('show');
        Route::post('/{id}/assign', [IncidentAdminController::class, 'assign'])->name('assign');
        Route::post('/{id}/status', [IncidentAdminController::class, 'status'])->name('status');
        Route::post('/{id}/notes', [IncidentAdminController::class, 'note'])->name('notes.store');
    });

    // Notifications & Alert Orchestration (Phase 8)
    Route::prefix('notifications')->name('notifications.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [NotificationAdminController::class, 'index'])->name('index');
        Route::get('/{id}', [NotificationAdminController::class, 'show'])->name('show');
        Route::post('/{id}/acknowledge', [NotificationAdminController::class, 'acknowledge'])->name('acknowledge');
    });

    // Safe Automation Framework (Phase 8)
    Route::prefix('automation')->name('automation.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [AutomationAdminController::class, 'index'])->name('index');
        Route::post('/{id}/toggle', [AutomationAdminController::class, 'toggle'])->name('toggle');
        Route::post('/executions/{id}/approve', [AutomationAdminController::class, 'approve'])->name('executions.approve');
        Route::post('/executions/{id}/reject', [AutomationAdminController::class, 'reject'])->name('executions.reject');
    });
});
