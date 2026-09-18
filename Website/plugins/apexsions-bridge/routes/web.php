<?php

use Azuriom\Plugin\ApexsionsBridge\Controllers\AccountLinkController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\GlobalSearchController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AuditLogController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AuctionAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\BattlepassAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\BroadcastAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\CrateAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\EconomyAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\AutomationAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\IncidentAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\KingdomAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\MarketAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\IntelligenceAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ModerationAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\NotificationAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PlayerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\PluginAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\RankAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ReportAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ServerAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\TransactionAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Api\LinkVerificationController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\VoteAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\WebstoreAdminController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\LeaderboardController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\ProfileManagementController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\PublicProfileController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\ServerMapController;
use Azuriom\Plugin\ApexsionsBridge\Controllers\VoteController;
use Illuminate\Support\Facades\Route;

// Public Routes
Route::get('/leaderboard', [LeaderboardController::class, 'index'])->name('leaderboard');
Route::get('/player/{identifier}', [PublicProfileController::class, 'show'])->name('player.show');
Route::get('/server-map', [ServerMapController::class, 'index'])->name('server-map');
Route::get('/vote', [VoteController::class, 'index'])->name('vote');
Route::post('/vote/check-status', [VoteController::class, 'checkStatus'])->name('vote.check-status');
Route::post('/vote/verify/{siteSlug}', [VoteController::class, 'checkStatus'])->name('vote.verify');

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
    Route::get('/global-search', [GlobalSearchController::class, 'search'])->name('global-search');
    Route::post('/apexsions/broadcast', [LinkVerificationController::class, 'broadcast'])->name('broadcast');

    // Player Management Foundation
    Route::prefix('players')->name('players.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [PlayerAdminController::class, 'index'])->name('index');
        Route::get('/{identifier}', [PlayerAdminController::class, 'show'])->name('show');
        Route::post('/{identifier}/action', [PlayerAdminController::class, 'executeAction'])->name('action');
    });

    // Rank Management & Hierarchy
    Route::prefix('ranks')->name('ranks.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [RankAdminController::class, 'index'])->name('index');
        Route::post('/assign', [RankAdminController::class, 'assign'])->name('assign');
        Route::post('/expire-trials', [RankAdminController::class, 'expireTrials'])->name('expire-trials');
        Route::get('/settings', [RankAdminController::class, 'settings'])->name('settings');
        Route::post('/settings', [RankAdminController::class, 'updateSettings'])->name('settings.update');
        Route::get('/purchases', [RankAdminController::class, 'purchases'])->name('purchases');
        Route::get('/{rank_key}/edit', [RankAdminController::class, 'edit'])->name('edit');
        Route::put('/{rank_key}', [RankAdminController::class, 'update'])->name('update');
        Route::get('/{rank_key}', [RankAdminController::class, 'show'])->name('show');
    });

    // Webstore Manager (Centralized Package & Banner Administration)
    Route::prefix('webstore')->name('webstore.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [WebstoreAdminController::class, 'index'])->name('index');
        Route::get('/{id}/edit', [WebstoreAdminController::class, 'edit'])->name('edit');
        Route::put('/{id}', [WebstoreAdminController::class, 'update'])->name('update');
        Route::post('/{id}/toggle-status', [WebstoreAdminController::class, 'toggleStatus'])->name('toggle-status');
        Route::post('/{id}/toggle-featured', [WebstoreAdminController::class, 'toggleFeatured'])->name('toggle-featured');
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

    // Land Claims & Anti-Grief Territory Management
    Route::prefix('claims')->name('claims.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [\Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ClaimAdminController::class, 'index'])->name('index');
        Route::post('/{id}/unclaim', [\Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ClaimAdminController::class, 'unclaim'])->name('unclaim');
        Route::post('/{id}/deposit', [\Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ClaimAdminController::class, 'deposit'])->name('deposit');
        Route::post('/collect-tax', [\Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ClaimAdminController::class, 'collectTax'])->name('collect-tax');
        Route::post('/sync', [\Azuriom\Plugin\ApexsionsBridge\Controllers\Admin\ClaimAdminController::class, 'sync'])->name('sync');
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

    // Kingdom Market & Dynamic Shop Administration
    Route::prefix('market')->name('market.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [MarketAdminController::class, 'index'])->name('index');
        Route::post('/kingdoms', [MarketAdminController::class, 'updateKingdoms'])->name('kingdoms.update');
        Route::post('/dynamics', [MarketAdminController::class, 'updateDynamics'])->name('dynamics.update');
        Route::post('/items', [MarketAdminController::class, 'updateItem'])->name('items.update');
        Route::post('/items/batch', [MarketAdminController::class, 'batchUpdateItems'])->name('items.batch');
        Route::post('/sync-now', [MarketAdminController::class, 'syncNow'])->name('sync-now');
        Route::post('/reset-defaults', [MarketAdminController::class, 'resetDefaults'])->name('reset-defaults');
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
        Route::post('/map-settings', [ServerAdminController::class, 'updateMapSettings'])->name('map.settings');
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

    // Vote Management & Civic Verification
    Route::prefix('votes')->name('votes.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [VoteAdminController::class, 'index'])->name('index');
        Route::get('/{id}', [VoteAdminController::class, 'show'])->name('show');
        Route::post('/{id}/retry', [VoteAdminController::class, 'retryReward'])->name('retry');
        Route::post('/{id}/retry-key', [VoteAdminController::class, 'retryKey'])->name('retry.key');
        Route::post('/{id}/retry-money', [VoteAdminController::class, 'retryMoney'])->name('retry.money');
        Route::post('/poll', [VoteAdminController::class, 'triggerPoll'])->name('poll');
        Route::post('/sites/{id}/toggle', [VoteAdminController::class, 'toggleSite'])->name('sites.toggle');
        Route::post('/sites/{id}/update', [VoteAdminController::class, 'updateSite'])->name('sites.update');
    });

    // Kingdoms & Territory War Operations
    Route::prefix('kingdoms')->name('kingdoms.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [KingdomAdminController::class, 'index'])->name('index');
        Route::post('/war/start', [KingdomAdminController::class, 'startWar'])->name('start-war');
        Route::post('/war/stop', [KingdomAdminController::class, 'stopWar'])->name('stop-war');
        Route::post('/monarch/set', [KingdomAdminController::class, 'setKing'])->name('set-king');
        Route::post('/monarch/unset', [KingdomAdminController::class, 'unsetKing'])->name('unset-king');
        Route::post('/treasury', [KingdomAdminController::class, 'adjustTreasury'])->name('adjust-treasury');
    });

    // BattlePass Season & Pass Desk
    Route::prefix('battlepass')->name('battlepass.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [BattlepassAdminController::class, 'index'])->name('index');
        Route::post('/give-pass', [BattlepassAdminController::class, 'givePass'])->name('give-pass');
        Route::post('/adjust', [BattlepassAdminController::class, 'adjustProgress'])->name('adjust-progress');
        Route::post('/reload', [BattlepassAdminController::class, 'reload'])->name('reload');
    });

    // Crates & Key Dispenser
    Route::prefix('crates')->name('crates.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [CrateAdminController::class, 'index'])->name('index');
        Route::post('/manage-key', [CrateAdminController::class, 'manageKey'])->name('manage-key');
        Route::post('/reload', [CrateAdminController::class, 'reload'])->name('reload');
    });

    // Live Server Broadcast & Lockdown Hub
    Route::prefix('broadcast')->name('broadcast.')->middleware('can:admin.users')->group(function () {
        Route::get('/', [BroadcastAdminController::class, 'index'])->name('index');
        Route::post('/send', [BroadcastAdminController::class, 'dispatchBroadcast'])->name('send');
        Route::post('/lockdown', [BroadcastAdminController::class, 'toggleLockdown'])->name('toggle-lockdown');
    });
});
