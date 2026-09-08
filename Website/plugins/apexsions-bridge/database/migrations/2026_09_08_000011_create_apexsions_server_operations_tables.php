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
        // 1. Maintenance Mode State (Authoritative Singleton State)
        if (!Schema::hasTable('apexsions_maintenance_state')) {
            Schema::create('apexsions_maintenance_state', function (Blueprint $table) {
                $table->id();
                $table->boolean('is_enabled')->default(false)->index();
                $table->string('message', 255)->default('Server sedang dalam pemeliharaan berkala. Silakan kembali beberapa saat lagi.');
                $table->string('reason', 255)->nullable();
                $table->string('enabled_by', 64)->nullable();
                $table->timestamp('enabled_at')->nullable();
                $table->boolean('allow_staff')->default(true);
                $table->timestamps();
            });
        }

        // 2. Telemetry Historical Snapshots
        if (!Schema::hasTable('apexsions_server_metrics')) {
            Schema::create('apexsions_server_metrics', function (Blueprint $table) {
                $table->id();
                $table->float('tps')->default(20.0);
                $table->float('mspt')->default(15.0);
                $table->float('cpu_usage')->nullable();
                $table->integer('ram_used_mb')->default(0);
                $table->integer('ram_max_mb')->default(0);
                $table->float('disk_used_gb')->nullable();
                $table->float('disk_total_gb')->nullable();
                $table->integer('online_players')->default(0);
                $table->integer('max_players')->default(500);
                $table->integer('loaded_chunks')->default(0);
                $table->integer('entities')->default(0);
                $table->string('server_status', 16)->default('ONLINE')->index(); // ONLINE, DEGRADED, OFFLINE, MAINTENANCE
                $table->timestamp('created_at')->index();
            });
        }

        // 3. Server Alerts Lifecycle
        if (!Schema::hasTable('apexsions_server_alerts')) {
            Schema::create('apexsions_server_alerts', function (Blueprint $table) {
                $table->id();
                $table->string('type', 32)->index(); // LOW_TPS, HIGH_MSPT, HIGH_MEMORY, SERVER_OFFLINE, BRIDGE_DELAYED, PLUGIN_DEGRADED
                $table->string('severity', 16)->default('WARNING')->index(); // INFO, WARNING, CRITICAL
                $table->string('message', 255);
                $table->string('status', 16)->default('ACTIVE')->index(); // ACTIVE, ACKNOWLEDGED, RESOLVED
                $table->string('acknowledged_by', 64)->nullable();
                $table->timestamp('acknowledged_at')->nullable();
                $table->timestamp('resolved_at')->nullable();
                $table->json('metadata')->nullable();
                $table->timestamps();

                $table->index(['type', 'status']);
                $table->index('created_at');
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('apexsions_server_alerts');
        Schema::dropIfExists('apexsions_server_metrics');
        Schema::dropIfExists('apexsions_maintenance_state');
    }
};
