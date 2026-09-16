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
        Schema::table('apexsions_claims', function (Blueprint $table) {
            $table->decimal('bank_balance', 14, 2)->default(0.00)->after('trusted_count');
            $table->decimal('daily_upkeep', 14, 2)->default(100.00)->after('bank_balance');
            $table->string('status', 32)->default('ACTIVE')->index()->after('daily_upkeep');
            $table->timestamp('grace_period_until')->nullable()->after('status');
            $table->timestamp('last_tax_collected_at')->nullable()->after('grace_period_until');
            $table->string('kingdom_id', 64)->nullable()->index()->after('last_tax_collected_at');
            $table->text('flags')->nullable()->after('kingdom_id');
            $table->text('roles')->nullable()->after('flags');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('apexsions_claims', function (Blueprint $table) {
            $table->dropColumn([
                'bank_balance',
                'daily_upkeep',
                'status',
                'grace_period_until',
                'last_tax_collected_at',
                'kingdom_id',
                'flags',
                'roles',
            ]);
        });
    }
};
