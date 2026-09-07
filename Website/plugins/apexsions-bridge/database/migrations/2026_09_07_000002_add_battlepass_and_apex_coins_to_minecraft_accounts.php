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
            $table->integer('battlepass_tier')->default(1)->after('balance_diamond');
            $table->integer('battlepass_xp')->default(0)->after('battlepass_tier');
            $table->integer('battlepass_required_xp')->default(100)->after('battlepass_xp');
            $table->boolean('battlepass_has_premium')->default(false)->after('battlepass_required_xp');
            $table->bigInteger('apex_coins')->default(0)->after('battlepass_has_premium');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            $table->dropColumn([
                'battlepass_tier',
                'battlepass_xp',
                'battlepass_required_xp',
                'battlepass_has_premium',
                'apex_coins',
            ]);
        });
    }
};
