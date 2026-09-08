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
        // 1. Unified Normalized Event Ledger
        if (!Schema::hasTable('apexsions_events')) {
            Schema::create('apexsions_events', function (Blueprint $table) {
                $table->id();
                $table->string('event_id', 64)->unique();
                $table->string('event_type', 64)->index();
                $table->string('source', 32)->default('SYSTEM')->index(); // WEB, MINECRAFT, PLUGIN, SYSTEM, BRIDGE
                $table->string('entity_type', 32)->default('PLAYER')->index(); // PLAYER, TRANSACTION, AUCTION, SERVER, PLUGIN, REPORT
                $table->string('entity_id', 64)->index();
                $table->string('actor_type', 32)->default('SYSTEM');
                $table->string('actor_id', 64)->nullable()->index();
                $table->string('actor_name', 64)->nullable();
                $table->string('target_type', 32)->nullable();
                $table->string('target_id', 64)->nullable()->index();
                $table->string('target_name', 64)->nullable();
                $table->string('severity', 16)->default('INFO')->index(); // INFO, LOW, MEDIUM, HIGH, CRITICAL
                $table->string('correlation_id', 64)->nullable()->index();
                $table->string('action_id', 64)->nullable()->index();
                $table->string('incident_id', 64)->nullable()->index();
                $table->json('metadata')->nullable();
                $table->timestamp('occurred_at')->index();
                $table->timestamp('received_at')->nullable();
                $table->timestamps();
            });
        }

        // 2. Incident Case Tracking
        if (!Schema::hasTable('apexsions_incidents')) {
            Schema::create('apexsions_incidents', function (Blueprint $table) {
                $table->id();
                $table->string('incident_id', 64)->unique();
                $table->string('title', 128);
                $table->string('type', 32)->default('SECURITY')->index(); // ECONOMY, PLAYER, MODERATION, SERVER, PLUGIN, BRIDGE, SECURITY
                $table->string('severity', 16)->default('MEDIUM')->index(); // LOW, MEDIUM, HIGH, CRITICAL
                $table->string('status', 16)->default('OPEN')->index(); // OPEN, INVESTIGATING, MITIGATED, RESOLVED, CLOSED
                $table->string('source', 32)->default('RULE_ENGINE')->index(); // RULE_ENGINE, SERVER_ALERT, STAFF_REPORT, MANUAL
                $table->timestamp('detected_at')->index();
                $table->timestamp('resolved_at')->nullable();
                $table->string('assigned_to', 64)->nullable();
                $table->timestamp('assigned_at')->nullable();
                $table->string('root_entity_type', 32)->default('PLAYER');
                $table->string('root_entity_id', 64)->index();
                $table->string('root_entity_name', 128)->nullable();
                $table->text('correlation_summary')->nullable();
                $table->integer('occurrence_count')->default(1);
                $table->timestamp('last_occurred_at')->nullable();
                $table->json('metadata')->nullable();
                $table->timestamps();
            });
        }

        // 3. Incident Investigation Notes
        if (!Schema::hasTable('apexsions_incident_notes')) {
            Schema::create('apexsions_incident_notes', function (Blueprint $table) {
                $table->id();
                $table->string('incident_id', 64)->index();
                $table->string('staff_id', 64);
                $table->string('staff_name', 64);
                $table->text('content');
                $table->string('related_event_id', 64)->nullable();
                $table->timestamps();
            });
        }

        // 4. Explicit Intelligence Rules
        if (!Schema::hasTable('apexsions_intelligence_rules')) {
            Schema::create('apexsions_intelligence_rules', function (Blueprint $table) {
                $table->id();
                $table->string('rule_id', 64)->unique();
                $table->string('name', 128);
                $table->string('category', 32)->default('SECURITY')->index(); // ECONOMY, SERVER, PLUGIN, MODERATION, SECURITY
                $table->text('description')->nullable();
                $table->string('severity', 16)->default('MEDIUM');
                $table->json('condition_schema')->nullable();
                $table->integer('cooldown_minutes')->default(15);
                $table->boolean('is_enabled')->default(true)->index();
                $table->timestamps();
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_intelligence_rules');
        Schema::dropIfExists('apexsions_incident_notes');
        Schema::dropIfExists('apexsions_incidents');
        Schema::dropIfExists('apexsions_events');
    }
};
