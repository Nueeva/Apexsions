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
        // 1. Add rank status columns to minecraft_accounts if not present
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            if (!Schema::hasColumn('minecraft_accounts', 'rank_type')) {
                $table->string('rank_type', 16)->default('PERMANENT')->after('rank_display');
            }
            if (!Schema::hasColumn('minecraft_accounts', 'rank_expires_at')) {
                $table->timestamp('rank_expires_at')->nullable()->after('rank_type');
            }
        });

        // 2. Create apexsions_rank_purchases table
        if (!Schema::hasTable('apexsions_rank_purchases')) {
            Schema::create('apexsions_rank_purchases', function (Blueprint $table) {
                $table->id();
                $table->unsignedInteger('user_id')->nullable()->index();
                $table->unsignedBigInteger('minecraft_account_id')->nullable()->index();
                $table->string('minecraft_uuid', 36)->nullable()->index();
                $table->string('minecraft_username', 64)->index();
                $table->string('rank', 32)->index();
                $table->string('rank_type', 16)->default('PERMANENT'); // 'TRIAL', 'PERMANENT'
                $table->integer('duration_days')->nullable(); // 30, 90, null
                $table->double('price_paid')->default(0);
                $table->string('status', 32)->default('ACTIVE')->index(); // 'ACTIVE', 'EXPIRED', 'UPGRADED', 'REVOKED'
                $table->timestamp('started_at')->useCurrent();
                $table->timestamp('expires_at')->nullable()->index();
                $table->string('source', 32)->default('WEBSTORE');
                $table->text('notes')->nullable();
                $table->timestamps();
            });
        }

        // 3. Create apexsions_rank_rewards_claimed table to strictly prevent money reward duplication
        if (!Schema::hasTable('apexsions_rank_rewards_claimed')) {
            Schema::create('apexsions_rank_rewards_claimed', function (Blueprint $table) {
                $table->id();
                $table->string('minecraft_uuid', 36)->index();
                $table->string('minecraft_username', 64)->index();
                $table->string('rank', 32)->index();
                $table->string('reward_type', 32)->default('MONEY_ONETIME');
                $table->double('amount')->default(0);
                $table->string('transaction_reference', 128)->nullable();
                $table->timestamp('claimed_at')->useCurrent();
                $table->timestamps();

                $table->unique(['minecraft_uuid', 'rank', 'reward_type'], 'rank_reward_unique_claim');
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_rank_rewards_claimed');
        Schema::dropIfExists('apexsions_rank_purchases');

        Schema::table('minecraft_accounts', function (Blueprint $table) {
            if (Schema::hasColumn('minecraft_accounts', 'rank_type')) {
                $table->dropColumn(['rank_type', 'rank_expires_at']);
            }
        });
    }
};
