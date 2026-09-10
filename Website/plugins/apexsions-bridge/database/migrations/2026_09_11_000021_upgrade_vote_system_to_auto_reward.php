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
        Schema::table('apexsions_vote_transactions', function (Blueprint $table) {
            if (!Schema::hasColumn('apexsions_vote_transactions', 'external_vote_id')) {
                $table->string('external_vote_id', 128)->nullable()->after('vote_uuid')->index();
            }
            if (!Schema::hasColumn('apexsions_vote_transactions', 'external_status')) {
                $table->string('external_status', 32)->nullable()->after('vote_status')->index();
            }
            if (!Schema::hasColumn('apexsions_vote_transactions', 'verified_at')) {
                $table->timestamp('verified_at')->nullable()->after('voted_at')->index();
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('apexsions_vote_transactions', function (Blueprint $table) {
            if (Schema::hasColumn('apexsions_vote_transactions', 'external_vote_id')) {
                $table->dropColumn('external_vote_id');
            }
            if (Schema::hasColumn('apexsions_vote_transactions', 'external_status')) {
                $table->dropColumn('external_status');
            }
            if (Schema::hasColumn('apexsions_vote_transactions', 'verified_at')) {
                $table->dropColumn('verified_at');
            }
        });
    }
};
