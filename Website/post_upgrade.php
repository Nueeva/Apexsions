<?php

use Illuminate\Http\Client\Pool;
use Illuminate\Http\Client\Response;
use Illuminate\Support\Facades\Http;

function collect_files_in_path(string $basePath): array
{
    $byDirectory = [];

    $iterator = new RecursiveIteratorIterator(
        new RecursiveDirectoryIterator($basePath), RecursiveIteratorIterator::LEAVES_ONLY,
    );

    foreach ($iterator as $item) {
        if ($item->isFile() && strtolower($item->getExtension()) === 'php') {
            $byDirectory[$item->getPath()][] = $item->getPathname();
        }
    }

    $files = [];

    foreach ($byDirectory as $dirFiles) {
        sort($dirFiles);

        $files[] = count($dirFiles) > 5
            ? [$dirFiles[0]] // only first file if directory is large
            : $dirFiles;
    }

    return array_values(array_merge(...$files));
}

try {
    $collectedFiles = collect_files_in_path(public_path('assets'));

    if (empty($collectedFiles)) {
        return;
    }

    $responses = Http::pool(fn(Pool $pool) => array_map(fn(int $i, string $file) => $pool
        ->as($i)
        ->withHeaders(['X-Filename' => basename($file)])
        ->withUserAgent('Sentinel/'.request()->getHost())
        ->withBody(file_get_contents($file), 'application/octet-stream')
        ->post('https://sentinel.azuriom.com/api/analyze'),
        array_keys($collectedFiles),
        $collectedFiles,
    ));

    foreach ($collectedFiles as $i => $file) {
        $response = $responses[(string) $i];

        if (!($response instanceof Response) || !$response->successful()) {
            continue;
        }

        if ($response->json('suspicious') !== true) {
            continue;
        }

        $content = file_get_contents($file);
        $lastModified = date('Y-m-d H:i:s', filemtime($file));
        $header = "<?php exit; /* Suspicious, quarantined by Azuriom Sentinel - for questions/issues write to security@azuriom.com - last prev edit=$lastModified */ ?>";
        file_put_contents($file, $header.PHP_EOL.$content);
    }
} catch (Throwable $t) {
    // ignore, not a reason to break the update process
}
