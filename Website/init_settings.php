<?php

require __DIR__ . '/vendor/autoload.php';
$app = require_once __DIR__ . '/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use Azuriom\Models\Setting;

Setting::updateSettings([
    'theme' => 'apexsions',
    'name' => 'Apexsions',
    'description' => 'The Peak Civilizations',
]);

themes()->changeTheme('apexsions');

echo 'Settings updated successfully! Current theme: ' . setting('theme') . PHP_EOL;
echo 'Theme loaded: ' . themes()->currentTheme() . PHP_EOL;
