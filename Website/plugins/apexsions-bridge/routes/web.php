<?php

use Azuriom\Plugin\ApexsionsBridge\Controllers\AccountLinkController;
use Illuminate\Support\Facades\Route;

Route::middleware('auth')->group(function () {
    Route::get('/link', [AccountLinkController::class, 'index'])->name('link.index');
    Route::post('/link/pin', [AccountLinkController::class, 'generatePin'])->name('link.pin');
    Route::delete('/link/{account}', [AccountLinkController::class, 'unlink'])->name('link.unlink');
});
