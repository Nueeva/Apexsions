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
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            $table->string('rank', 32)->default('wanderer')->after('minecraft_username');
            $table->string('rank_display', 64)->default('Wanderer')->after('rank');
            $table->string('kingdom', 32)->default('NONE')->after('rank_display');
            $table->string('kingdom_display', 64)->default('Belum Memilih')->after('kingdom');
            $table->integer('level')->default(1)->after('kingdom_display');
            $table->bigInteger('xp')->default(0)->after('level');
            $table->bigInteger('required_xp')->default(100)->after('xp');
            $table->string('level_title', 64)->nullable()->after('required_xp');
            $table->string('active_title', 128)->nullable()->after('level_title');
            $table->double('balance_rupiah')->default(0)->after('active_title');
            $table->double('balance_diamond')->default(0)->after('balance_rupiah');
            $table->json('unlocked_titles')->nullable()->after('balance_diamond');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('minecraft_accounts', function (Blueprint $table) {
            $table->dropColumn([
                'rank',
                'rank_display',
                'kingdom',
                'kingdom_display',
                'level',
                'xp',
                'required_xp',
                'level_title',
                'active_title',
                'balance_rupiah',
                'balance_diamond',
                'unlocked_titles',
            ]);
        });
    }
};
