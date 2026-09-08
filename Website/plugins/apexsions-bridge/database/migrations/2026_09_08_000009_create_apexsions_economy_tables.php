<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // 1. Transactions Ledger Table
        Schema::create('apexsions_transactions', function (Blueprint $table) {
            $table->id();
            $table->string('transaction_id', 36)->unique();
            $table->string('type', 32)->index(); // TRANSFER, AUCTION_BUY, ADMIN_ADJUST, TAX_TREASURY, MARKET_BUY, MARKET_SELL
            $table->string('sender_uuid', 36)->nullable()->index();
            $table->string('sender_name', 64)->nullable()->index();
            $table->string('receiver_uuid', 36)->nullable()->index();
            $table->string('receiver_name', 64)->nullable()->index();
            $table->string('currency', 16)->default('rupiah')->index();
            $table->double('amount', 18, 2);
            $table->double('tax_amount', 18, 2)->default(0);
            $table->double('net_amount', 18, 2);
            $table->string('reason', 255)->nullable();
            $table->string('status', 16)->default('COMPLETED')->index(); // COMPLETED, PENDING, FAILED, CANCELLED
            $table->string('source', 32)->default('IN_GAME'); // IN_GAME, WEB_DASHBOARD, API
            $table->string('action_id', 36)->nullable()->index();
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['currency', 'created_at']);
            $table->index(['type', 'status']);
        });

        // 2. Auctions Table
        Schema::create('apexsions_auctions', function (Blueprint $table) {
            $table->id();
            $table->string('auction_id', 36)->unique();
            $table->string('seller_uuid', 36)->index();
            $table->string('seller_name', 64)->index();
            $table->string('currency', 16)->default('rupiah')->index();
            $table->double('price', 18, 2);
            $table->string('item_name', 128);
            $table->text('item_data')->nullable(); // Serialized Bukkit item or Base64
            $table->json('item_lore')->nullable();
            $table->string('status', 16)->default('ACTIVE')->index(); // ACTIVE, SOLD, EXPIRED, CANCELLED, QUARANTINED
            $table->string('buyer_uuid', 36)->nullable()->index();
            $table->string('buyer_name', 64)->nullable();
            $table->string('quarantined_by', 64)->nullable();
            $table->text('quarantine_reason')->nullable();
            $table->timestamp('quarantined_at')->nullable();
            $table->timestamp('expires_at')->nullable()->index();
            $table->timestamps();

            $table->index(['seller_uuid', 'status']);
            $table->index(['status', 'created_at']);
        });

        // 3. Kingdom Treasury Accumulator Table
        Schema::create('apexsions_kingdom_treasury', function (Blueprint $table) {
            $table->id();
            $table->string('kingdom_key', 32)->index(); // SOLTERRA, ZENITHAR, SYLVAMOOR, GENERAL
            $table->string('kingdom_name', 64);
            $table->string('currency', 16)->default('rupiah');
            $table->double('balance', 18, 2)->default(0);
            $table->double('total_tax_collected', 18, 2)->default(0);
            $table->timestamp('last_tax_collected_at')->nullable();
            $table->timestamps();

            $table->unique(['kingdom_key', 'currency']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_kingdom_treasury');
        Schema::dropIfExists('apexsions_auctions');
        Schema::dropIfExists('apexsions_transactions');
    }
};
