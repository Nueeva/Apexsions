# ==============================================================================
#           APEXSIONS PLUGIN SUITE — AUTOMATED 6-PLUGIN BUILD SCRIPT
# ==============================================================================
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$outLibs = "build/libs"
if (!(Test-Path $outLibs)) { New-Item -ItemType Directory -Path $outLibs -Force | Out-Null }

# Locate JDK 21
$javaHome = "$scriptDir/plugins/ApexsionsCore/jdk-21"
if (Test-Path $javaHome) {
    $env:JAVA_HOME = $javaHome
    $env:Path = "$javaHome/bin;$env:Path"
}

# Locate Maven
$mvn = "$scriptDir/plugins/ApexsionsCore/apache-maven-3.9.9/bin/mvn.cmd"
if (!(Test-Path $mvn)) {
    $mvn = "mvn"
}

Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "         APEXSIONS PLUGIN SUITE MULTI-COMPILER            " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow

$plugins = @(
    @{ Name = "ApexsionsCore";       Path = "plugins/ApexsionsCore/pom.xml" },
    @{ Name = "ApexsionsChat";       Path = "plugins/ApexsionsChat/pom.xml" },
    @{ Name = "ApexsionsEconomy";    Path = "plugins/ApexsionsEconomy/pom.xml" },
    @{ Name = "ApexsionsBattlepass"; Path = "plugins/ApexsionsBattlepass/pom.xml" },
    @{ Name = "ApexsionsShop";       Path = "plugins/ApexsionsShop/pom.xml" },
    @{ Name = "ApexsionsMedia";      Path = "plugins/ApexsionsMedia/pom.xml" }
)

$index = 1
foreach ($p in $plugins) {
    Write-Host "`n[$index/6] Building $($p.Name)..." -ForegroundColor Green
    & $mvn -f $p.Path clean package -DskipTests=true
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Compilation failed for $($p.Name)!" -ForegroundColor Red
        exit 1
    }

    $jarPattern = "plugins/$($p.Name)/target/$($p.Name)-*.jar"
    $builtJar = Get-ChildItem -Path $jarPattern -Exclude "*shaded*", "*original*" | Select-Object -First 1
    if ($builtJar) {
        $kb = [math]::Round($builtJar.Length / 1024, 2)
        Copy-Item -Force $builtJar.FullName "$outLibs/$($p.Name)-1.0.0.jar"
        Copy-Item -Force $builtJar.FullName "plugins/$($p.Name)/$($p.Name)-1.0.0.jar"
        Write-Host "  -> [OK] $($p.Name)-1.0.0.jar ($kb KB)" -ForegroundColor Cyan
    }
    $index++
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  ALL 6 APEXSIONS PLUGINS BUILT & PACKAGED SUCCESSFULLY!  " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

Get-ChildItem "$outLibs/*.jar" | ForEach-Object {
    $kb = [math]::Round($_.Length / 1024, 2)
    Write-Host "  [OK] $($_.Name) ($kb KB)" -ForegroundColor Yellow
}
