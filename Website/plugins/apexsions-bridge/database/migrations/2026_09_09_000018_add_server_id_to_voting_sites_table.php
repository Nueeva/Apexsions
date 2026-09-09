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
        Schema::table('apexsions_voting_sites', function (Blueprint $table) {
            if (!Schema::hasColumn('apexsions_voting_sites', 'server_id')) {
                $table->string('server_id', 64)->nullable()->after('vote_url');
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('apexsions_voting_sites', function (Blueprint $table) {
            if (Schema::hasColumn('apexsions_voting_sites', 'server_id')) {
                $table->dropColumn('server_id');
            }
        });
    }
};
