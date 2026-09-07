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
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            $table->timestamp('last_daily_reward_at')->nullable()->after('last_seen_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            $table->dropColumn('last_daily_reward_at');
        });
    }
};
