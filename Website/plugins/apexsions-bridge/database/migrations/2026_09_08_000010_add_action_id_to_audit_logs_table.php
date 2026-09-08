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
        if (Schema::hasTable('apexsions_audit_logs') && !Schema::hasColumn('apexsions_audit_logs', 'action_id')) {
            Schema::table('apexsions_audit_logs', function (Blueprint $table) {
                $table->string('action_id', 36)->nullable()->after('id')->index();
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        if (Schema::hasTable('apexsions_audit_logs') && Schema::hasColumn('apexsions_audit_logs', 'action_id')) {
            Schema::table('apexsions_audit_logs', function (Blueprint $table) {
                $table->dropColumn('action_id');
            });
        }
    }
};
