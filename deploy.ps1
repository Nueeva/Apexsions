# ==============================================================================
#           APEXSIONS SFTP DEPLOYMENT WRAPPER
# ==============================================================================
param(
    [Parameter(Position = 0)]
    [string]$Plugin = "",

    [Parameter()]
    [switch]$All = $false,

    [Parameter()]
    [switch]$Test = $false,

    [Parameter()]
    [string]$File = ""
)

$scriptDir = $PSScriptRoot
$pyScript = Join-Path $scriptDir "scripts\deploy_sftp.py"

if (-not (Test-Path $pyScript)) {
    Write-Host "❌ Script not found: $pyScript" -ForegroundColor Red
    exit 1
}

$argsList = @()
if ($Test) {
    $argsList += "--test"
} elseif ($File) {
    $argsList += "--file"
    $argsList += $File
} elseif ($All) {
    $argsList += "--all"
} elseif ($Plugin) {
    $argsList += $Plugin
}

python $pyScript @argsList
