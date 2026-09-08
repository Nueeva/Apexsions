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
        Schema::table('deliveries', function (Blueprint $table) {
            $table->string('action_id', 64)->nullable()->unique()->after('id');
            $table->string('idempotency_key', 64)->nullable()->index()->after('action_id');
            $table->timestamp('locked_at')->nullable()->after('status');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('deliveries', function (Blueprint $table) {
            $table->dropColumn(['action_id', 'idempotency_key', 'locked_at']);
        });
    }
};
