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
        // 1. Compound Index for Incident Deduplication
        if (Schema::hasTable('apexsions_incidents')) {
            Schema::table('apexsions_incidents', function (Blueprint $table) {
                $table->index(['root_entity_id', 'type', 'status'], 'idx_incidents_dedup');
            });
        }

        // 2. Compound Indexes for Anomaly Window Evaluation
        if (Schema::hasTable('apexsions_events')) {
            Schema::table('apexsions_events', function (Blueprint $table) {
                $table->index(['event_type', 'actor_id', 'occurred_at'], 'idx_events_actor_window');
                $table->index(['event_type', 'target_id', 'occurred_at'], 'idx_events_target_window');
            });
        }

        // 3. Compound Index for Investigation Timeline Lookup
        if (Schema::hasTable('apexsions_audit_logs')) {
            Schema::table('apexsions_audit_logs', function (Blueprint $table) {
                $table->index(['target_id', 'action'], 'idx_audit_target_action');
            });
        }

        // 4. Compound Index for Player Balance Mutations
        if (Schema::hasTable('apexsions_transactions')) {
            Schema::table('apexsions_transactions', function (Blueprint $table) {
                $table->index(['sender_uuid', 'currency', 'created_at'], 'idx_tx_sender_curr');
                $table->index(['receiver_uuid', 'currency', 'created_at'], 'idx_tx_receiver_curr');
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        if (Schema::hasTable('apexsions_transactions')) {
            Schema::table('apexsions_transactions', function (Blueprint $table) {
                $table->dropIndex('idx_tx_sender_curr');
                $table->dropIndex('idx_tx_receiver_curr');
            });
        }

        if (Schema::hasTable('apexsions_audit_logs')) {
            Schema::table('apexsions_audit_logs', function (Blueprint $table) {
                $table->dropIndex('idx_audit_target_action');
            });
        }

        if (Schema::hasTable('apexsions_events')) {
            Schema::table('apexsions_events', function (Blueprint $table) {
                $table->dropIndex('idx_events_actor_window');
                $table->dropIndex('idx_events_target_window');
            });
        }

        if (Schema::hasTable('apexsions_incidents')) {
            Schema::table('apexsions_incidents', function (Blueprint $table) {
                $table->dropIndex('idx_incidents_dedup');
            });
        }
    }
};
