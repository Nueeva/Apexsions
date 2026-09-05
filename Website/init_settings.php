<?php

require __DIR__ . '/vendor/autoload.php';
$app = require_once __DIR__ . '/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Models\Setting;

Setting::updateSettings([
    'theme' => 'apexsions',
    'name' => 'Apexsions Network',
    'description' => 'Official Apexsions Minecraft Server Network Portal',
]);

echo 'Settings updated successfully! Current theme: ' . setting('theme') . PHP_EOL;
