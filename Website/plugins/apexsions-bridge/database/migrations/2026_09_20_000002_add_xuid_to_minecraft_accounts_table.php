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
            if (!Schema::hasColumn('minecraft_accounts', 'xuid')) {
                $table->string('xuid', 32)->nullable()->after('floodgate_uuid');
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            if (Schema::hasColumn('minecraft_accounts', 'xuid')) {
                $table->dropColumn('xuid');
            }
        });
    }
};
