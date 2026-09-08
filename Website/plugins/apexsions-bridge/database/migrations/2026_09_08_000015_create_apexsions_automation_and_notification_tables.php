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
        // 1. Admin Notifications Table
        if (!Schema::hasTable('apexsions_notifications')) {
            Schema::create('apexsions_notifications', function (Blueprint $table) {
                $table->id();
                $table->string('notification_id', 64)->unique();
                $table->string('dedup_key', 128)->index();
                $table->string('type', 64)->index(); // INCIDENT_ALERT, SERVER_ALERT, PLUGIN_ALERT, SECURITY_ALERT, ECONOMY_ALERT
                $table->string('severity', 16)->default('MEDIUM')->index(); // INFO, LOW, MEDIUM, HIGH, CRITICAL
                $table->string('title', 191);
                $table->text('message');
                $table->string('source', 32)->default('SYSTEM')->index(); // SYSTEM, INCIDENT, RULE_ENGINE, AUTOMATION, SERVER, PLUGIN, BRIDGE
                $table->string('entity_type', 32)->default('PLAYER')->index();
                $table->string('entity_id', 64)->index();
                $table->string('event_id', 64)->nullable()->index();
                $table->string('incident_id', 64)->nullable()->index();
                $table->string('action_id', 64)->nullable()->index();
                $table->string('correlation_id', 64)->nullable()->index();
                $table->integer('occurrence_count')->default(1);
                $table->timestamp('last_occurred_at')->index();
                $table->string('status', 32)->default('UNACKNOWLEDGED')->index(); // UNACKNOWLEDGED, ACKNOWLEDGED, SUPPRESSED
                $table->string('acknowledged_by', 64)->nullable();
                $table->timestamp('acknowledged_at')->nullable();
                $table->json('metadata')->nullable();
                $table->timestamps();
            });
        }

        // 2. Notification Delivery Logs
        if (!Schema::hasTable('apexsions_notification_deliveries')) {
            Schema::create('apexsions_notification_deliveries', function (Blueprint $table) {
                $table->id();
                $table->string('delivery_id', 64)->unique();
                $table->string('notification_id', 64)->index();
                $table->string('channel', 32)->index(); // IN_APP, DISCORD_WEBHOOK
                $table->string('status', 32)->default('PENDING')->index(); // PENDING, QUEUED, DELIVERED, FAILED, RETRYING, SUPPRESSED
                $table->integer('attempt_count')->default(0);
                $table->integer('max_attempts')->default(3);
                $table->timestamp('last_attempt_at')->nullable();
                $table->timestamp('next_retry_at')->nullable();
                $table->text('error_summary')->nullable();
                $table->json('payload')->nullable();
                $table->timestamps();
            });
        }

        // 3. Automation Policies
        if (!Schema::hasTable('apexsions_automation_policies')) {
            Schema::create('apexsions_automation_policies', function (Blueprint $table) {
                $table->id();
                $table->string('policy_id', 64)->unique();
                $table->string('name', 128);
                $table->string('trigger_type', 64)->index(); // EVENT_SEVERITY, INCIDENT_CREATED, PLUGIN_STATUS_CHANGE, SERVER_OFFLINE
                $table->json('condition_schema')->nullable();
                $table->string('action_type', 64)->index(); // NOTIFY, CREATE_INCIDENT, UPDATE_INCIDENT, CREATE_ALERT, SAFE_ACTION, REQUIRES_APPROVAL
                $table->json('action_payload')->nullable();
                $table->string('severity', 16)->default('MEDIUM')->index();
                $table->integer('cooldown_minutes')->default(15);
                $table->boolean('approval_required')->default(false);
                $table->boolean('is_enabled')->default(true)->index();
                $table->timestamps();
            });
        }

        // 4. Automation Execution Ledger
        if (!Schema::hasTable('apexsions_automation_executions')) {
            Schema::create('apexsions_automation_executions', function (Blueprint $table) {
                $table->id();
                $table->string('execution_id', 64)->unique();
                $table->string('policy_id', 64)->index();
                $table->string('trigger_event_id', 64)->nullable()->index();
                $table->string('trigger_incident_id', 64)->nullable()->index();
                $table->string('action_type', 64)->index();
                $table->string('status', 32)->default('PROPOSED')->index(); // PROPOSED, PENDING_APPROVAL, APPROVED, REJECTED, EXECUTED, FAILED, SUPPRESSED
                $table->string('approved_by', 64)->nullable();
                $table->timestamp('approved_at')->nullable();
                $table->text('rejection_reason')->nullable();
                $table->text('result_summary')->nullable();
                $table->integer('execution_depth')->default(1);
                $table->json('metadata')->nullable();
                $table->timestamps();
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_automation_executions');
        Schema::dropIfExists('apexsions_automation_policies');
        Schema::dropIfExists('apexsions_notification_deliveries');
        Schema::dropIfExists('apexsions_notifications');
    }
};
