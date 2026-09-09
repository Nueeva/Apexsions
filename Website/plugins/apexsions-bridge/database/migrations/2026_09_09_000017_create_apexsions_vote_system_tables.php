<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // 1. Voting Sites Directory
        Schema::create('apexsions_voting_sites', function (Blueprint $table) {
            $table->id();
            $table->string('name', 64);
            $table->string('slug', 64)->unique();
            $table->string('vote_url', 255);
            $table->string('api_key', 255)->nullable();
            $table->integer('cooldown_hours')->default(24);
            $table->boolean('is_active')->default(true)->index();
            $table->timestamps();
        });

        // Seed Default Official Voting Platforms
        DB::table('apexsions_voting_sites')->insert([
            [
                'name' => 'Minecraft-MP',
                'slug' => 'minecraft-mp',
                'vote_url' => 'https://minecraft-mp.com/server/338274/vote/',
                'api_key' => null,
                'cooldown_hours' => 24,
                'is_active' => true,
                'created_at' => now(),
                'updated_at' => now(),
            ],
            [
                'name' => 'TopG Global',
                'slug' => 'topg',
                'vote_url' => 'https://topg.org/minecraft-servers/server-678910',
                'api_key' => null,
                'cooldown_hours' => 12,
                'is_active' => true,
                'created_at' => now(),
                'updated_at' => now(),
            ],
            [
                'name' => 'PlanetMinecraft',
                'slug' => 'planetminecraft',
                'vote_url' => 'https://www.planetminecraft.com/server/apexsions/vote/',
                'api_key' => null,
                'cooldown_hours' => 24,
                'is_active' => true,
                'created_at' => now(),
                'updated_at' => now(),
            ],
        ]);

        // 2. Vote Transactions & Rewards Ledger
        Schema::create('apexsions_vote_transactions', function (Blueprint $table) {
            $table->id();
            $table->string('vote_uuid', 36)->unique();
            $table->unsignedBigInteger('site_id')->nullable()->index();
            $table->string('site_slug', 64)->index();
            $table->string('player_uuid', 36)->nullable()->index();
            $table->string('player_username', 64)->index();
            $table->string('ip_address', 45)->nullable();
            $table->string('idempotency_hash', 64)->unique()->index();
            $table->timestamp('voted_at')->index();
            $table->string('vote_status', 16)->default('VALID')->index(); // VALID, DUPLICATE, INVALID
            $table->string('reward_status', 32)->default('PENDING')->index(); // PENDING, KEYS_DELIVERED, REWARDED, FAILED
            $table->integer('keys_amount')->default(3);
            $table->double('money_amount', 18, 2)->default(1000.00);
            $table->unsignedBigInteger('keys_delivery_id')->nullable()->index();
            $table->unsignedBigInteger('money_delivery_id')->nullable()->index();
            $table->timestamp('rewarded_at')->nullable()->index();
            $table->text('failure_reason')->nullable();
            $table->integer('retry_count')->default(0);
            $table->timestamps();

            $table->index(['player_username', 'site_slug', 'voted_at'], 'apx_vote_usr_site_idx');
            $table->index(['reward_status', 'created_at'], 'apx_vote_rew_stat_idx');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_vote_transactions');
        Schema::dropIfExists('apexsions_voting_sites');
    }
};
