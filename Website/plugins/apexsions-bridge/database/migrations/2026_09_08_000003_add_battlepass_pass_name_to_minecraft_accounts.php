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
            $table->string('battlepass_pass_name', 64)->nullable()->after('battlepass_has_premium');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            $table->dropColumn('battlepass_pass_name');
        });
    }
};
