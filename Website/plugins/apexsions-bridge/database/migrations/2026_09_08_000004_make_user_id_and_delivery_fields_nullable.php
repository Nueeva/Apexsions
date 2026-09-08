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
        // Allow minecraft_accounts to store unlinked in-game players
        if (Schema::hasTable('minecraft_accounts')) {
            try {
                DB::statement("ALTER TABLE minecraft_accounts MODIFY user_id INT(10) UNSIGNED NULL");
            } catch (\Throwable $e) {
                Schema::table('minecraft_accounts', function (Blueprint $table) {
                    $table->unsignedInteger('user_id')->nullable()->change();
                });
            }
        }

        // Allow global or server deliveries without specific player UUIDs
        if (Schema::hasTable('deliveries')) {
            try {
                DB::statement("ALTER TABLE deliveries MODIFY player_uuid VARCHAR(36) NULL, MODIFY player_username VARCHAR(32) NULL");
            } catch (\Throwable $e) {
                Schema::table('deliveries', function (Blueprint $table) {
                    $table->string('player_uuid', 36)->nullable()->change();
                    $table->string('player_username', 32)->nullable()->change();
                });
            }
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // No destructive reversal needed
    }
};
