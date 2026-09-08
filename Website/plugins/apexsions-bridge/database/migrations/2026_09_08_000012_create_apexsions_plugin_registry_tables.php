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
        // 1. Custom Plugins Registry Table
        if (!Schema::hasTable('apexsions_plugins')) {
            Schema::create('apexsions_plugins', function (Blueprint $table) {
                $table->id();
                $table->string('plugin_id', 64)->unique();
                $table->string('name', 64)->index();
                $table->string('version', 32)->default('1.0.0');
                $table->string('type', 32)->default('CORE')->index(); // CORE, CHAT, ECONOMY, GAMEPLAY, UTILITY, COSMETIC, COMBAT
                $table->string('status', 16)->default('ENABLED')->index(); // ENABLED, DISABLED, ERROR, UNKNOWN
                $table->string('integration_status', 32)->default('WEB_READY')->index(); // WEB_READY, PARTIAL, MINECRAFT_ONLY, NOT_INTEGRATED
                $table->string('health_status', 16)->default('HEALTHY')->index(); // HEALTHY, DEGRADED, ERROR, UNKNOWN
                $table->text('description')->nullable();
                $table->json('dependencies')->nullable();
                $table->json('metadata')->nullable();
                $table->timestamp('last_heartbeat_at')->nullable();
                $table->timestamps();
            });
        }

        // 2. Plugin Capabilities Registry Table
        if (!Schema::hasTable('apexsions_plugin_capabilities')) {
            Schema::create('apexsions_plugin_capabilities', function (Blueprint $table) {
                $table->id();
                $table->string('plugin_id', 64)->index();
                $table->string('capability_id', 64)->index();
                $table->string('name', 128);
                $table->string('type', 16)->default('READ')->index(); // READ, WRITE, ACTION, EVENT, METRIC
                $table->string('access', 32)->default('ADMIN')->index(); // PUBLIC_INTERNAL, ADMIN, SYSTEM
                $table->string('status', 16)->default('AVAILABLE')->index(); // AVAILABLE, DEGRADED, DISABLED
                $table->text('description')->nullable();
                $table->boolean('requires_reason')->default(false);
                $table->boolean('requires_confirmation')->default(false);
                $table->json('input_schema')->nullable();
                $table->timestamps();

                $table->unique(['plugin_id', 'capability_id'], 'plugin_cap_unique');
            });
        }

        // 3. Plugin Health History Snapshots
        if (!Schema::hasTable('apexsions_plugin_health_history')) {
            Schema::create('apexsions_plugin_health_history', function (Blueprint $table) {
                $table->id();
                $table->string('plugin_id', 64)->index();
                $table->string('status', 16)->default('ENABLED');
                $table->string('health_status', 16)->default('HEALTHY')->index();
                $table->json('metrics')->nullable();
                $table->string('error_category', 32)->nullable(); // NONE, INTEGRATION, DATABASE, DEPENDENCY, CONFIGURATION, RUNTIME, UNKNOWN
                $table->string('error_summary', 255)->nullable();
                $table->timestamp('created_at')->index();
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_plugin_health_history');
        Schema::dropIfExists('apexsions_plugin_capabilities');
        Schema::dropIfExists('apexsions_plugins');
    }
};
