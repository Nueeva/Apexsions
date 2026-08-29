# ==============================================================================
#           APEXSIONS PLUGIN SUITE — TURBO MULTI-COMPILER
# ==============================================================================
param(
    [Parameter(Position = 0)]
    [string]$Plugin = "",

    [Parameter()]
    [switch]$Clean = $false,

    [Parameter()]
    [switch]$Offline = $false
)

$ErrorActionPreference = 'Stop'
$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$outLibs = Join-Path $scriptDir 'build\libs'
if (-not (Test-Path $outLibs)) {
    New-Item -ItemType Directory -Path $outLibs -Force | Out-Null
}

# Locate JDK 21
$localJdk = Join-Path $scriptDir 'plugins\ApexsionsCore\jdk-21'
$localJavac = Join-Path $localJdk 'bin\javac.exe'
if (Test-Path $localJavac) {
    $env:JAVA_HOME = (Get-Item $localJdk).FullName
    $env:Path = $env:JAVA_HOME + '\bin;' + $env:Path
} elseif (-not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $foundJavac = Get-ChildItem 'C:\Program Files\Eclipse Adoptium\*\bin\javac.exe', 'C:\Program Files\Java\*\bin\javac.exe' -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($foundJavac) {
        $env:JAVA_HOME = $foundJavac.Directory.Parent.FullName
        $env:Path = $env:JAVA_HOME + '\bin;' + $env:Path
    }
}

# Locate Maven
$hasMaven = $false
$mvn = Join-Path $scriptDir 'plugins\ApexsionsCore\apache-maven-3.9.9\bin\mvn.cmd'
if (Test-Path $mvn) {
    $hasMaven = $true
} elseif (Get-Command 'mvn' -ErrorAction SilentlyContinue) {
    $mvn = 'mvn'
    $hasMaven = $true
}

$allPlugins = @(
    @{ Name = 'ApexsionsCore';       Path = 'plugins\ApexsionsCore' },
    @{ Name = 'ApexsionsChat';       Path = 'plugins\ApexsionsChat' },
    @{ Name = 'ApexsionsEconomy';    Path = 'plugins\ApexsionsEconomy' },
    @{ Name = 'ApexsionsBattlepass'; Path = 'plugins\ApexsionsBattlepass' },
    @{ Name = 'ApexsionsShop';       Path = 'plugins\ApexsionsShop' },
    @{ Name = 'ApexsionsMedia';      Path = 'plugins\ApexsionsMedia' }
)

$targetPlugins = @()
if ([string]::IsNullOrWhiteSpace($Plugin) -or $Plugin.ToLower() -eq 'all') {
    $targetPlugins = $allPlugins
} else {
    $search = $Plugin.ToLower().Replace('apexsions', '')
    if ($search -eq 'bp') { $search = 'battlepass' }
    if ($search -eq 'eco') { $search = 'economy' }
    foreach ($p in $allPlugins) {
        if ($p.Name.ToLower().Contains($search)) {
            $targetPlugins += $p
        }
    }
    if ($targetPlugins.Count -eq 0) {
        Write-Host "Plugin '$Plugin' not found! Available options: Core, Chat, Economy (eco), Battlepass (bp), Shop, Media, all" -ForegroundColor Red
        exit 1
    }
}

Write-Host '==========================================================' -ForegroundColor Yellow
Write-Host '     ⚡ APEXSIONS PLUGIN SUITE — TURBO MULTI-COMPILER     ' -ForegroundColor Yellow
Write-Host '==========================================================' -ForegroundColor Yellow
$targetNames = ($targetPlugins | ForEach-Object { $_.Name }) -join ', '
Write-Host "Target: $targetNames" -ForegroundColor Cyan
if ($Clean) { 
    Write-Host 'Mode  : Clean Recompile (-Clean)' -ForegroundColor DarkYellow 
} else { 
    Write-Host 'Mode  : Ultra-Fast Incremental (No Clean)' -ForegroundColor Green 
}

# Construct Maven arguments
$mvnGoals = @()
if ($Clean) { $mvnGoals += 'clean' }
$mvnGoals += 'package'

$mvnArgs = @(
    '-DskipTests=true',
    '-T', '1C',
    '-nsu',
    '--no-transfer-progress',
    '-Dmaven.wagon.http.ssl.insecure=true',
    '-Dmaven.artifact.threads=8',
    '-Dmaven.javadoc.skip=true',
    '-Dsource.skip=true'
)
if ($Offline) { $mvnArgs += '-o' }

function Build-Plugin-Fast {
    param(
        [string]$Name,
        [string]$RelPath,
        [string]$MvnCmd,
        [string[]]$Goals,
        [string[]]$ExtraArgs,
        [string]$OutDirectory,
        [string]$RootDirectory
    )

    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $pluginDir = Join-Path $RootDirectory $RelPath
    $pomFile = Join-Path $pluginDir 'pom.xml'
    $fullArgs = @('-f', "`"$pomFile`"") + $Goals + $ExtraArgs

    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $MvnCmd
    $pinfo.Arguments = ($fullArgs -join ' ')
    $pinfo.WorkingDirectory = $pluginDir
    $pinfo.RedirectStandardOutput = $true
    $pinfo.RedirectStandardError = $true
    $pinfo.UseShellExecute = $false
    $pinfo.CreateNoWindow = $true

    $p = [System.Diagnostics.Process]::Start($pinfo)
    $stdout = $p.StandardOutput.ReadToEnd()
    $stderr = $p.StandardError.ReadToEnd()
    $p.WaitForExit()
    $sw.Stop()

    $ok = ($p.ExitCode -eq 0)
    $size = 0

    if ($ok) {
        $targetDir = Join-Path $pluginDir 'target'
        $builtJar = Get-ChildItem -Path (Join-Path $targetDir ($Name + '-*.jar')) -Exclude '*shaded*', '*original*' -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($builtJar) {
            $size = [math]::Round($builtJar.Length / 1024, 2)
            Copy-Item -Force $builtJar.FullName (Join-Path $OutDirectory ($Name + '-1.0.0.jar'))
            Copy-Item -Force $builtJar.FullName (Join-Path $pluginDir ($Name + '-1.0.0.jar'))
        }
    }

    $dur = [math]::Round($sw.Elapsed.TotalSeconds, 2)

    return [PSCustomObject]@{
        Name     = $Name
        Success  = $ok
        ExitCode = $p.ExitCode
        Duration = $dur
        SizeKB   = $size
        StdOut   = $stdout
        StdErr   = $stderr
    }
}

if ($hasMaven) {
    $idx = 1
    $total = $targetPlugins.Count
    foreach ($p in $targetPlugins) {
        $pName = $p.Name
        $pRel = $p.Path
        Write-Host ""
        Write-Host "[$idx/$total] ⚡ Building $pName..." -ForegroundColor Green
        $res = Build-Plugin-Fast -Name $pName -RelPath $pRel -MvnCmd $mvn -Goals $mvnGoals -ExtraArgs $mvnArgs -OutDirectory $outLibs -RootDirectory $scriptDir
        
        if (-not $res.Success) {
            Write-Host ""
            Write-Host "❌ Compilation FAILED for $pName (ExitCode: $($res.ExitCode))" -ForegroundColor Red
            Write-Host $res.StdOut
            Write-Host $res.StdErr -ForegroundColor Red
            exit 1
        } else {
            $line = '  -> [OK] {0}-1.0.0.jar [{1} KB] in {2}s' -f $pName, $res.SizeKB, $res.Duration
            Write-Host $line -ForegroundColor Cyan
        }
        $idx++
    }
}

$stopwatch.Stop()
$totalSec = [math]::Round($stopwatch.Elapsed.TotalSeconds, 2)

Write-Host ''
Write-Host '==========================================================' -ForegroundColor Yellow
Write-Host "      ⚡ ALL TARGETS BUILT SUCCESSFULLY IN ${totalSec}s!  " -ForegroundColor Green
Write-Host '==========================================================' -ForegroundColor Yellow
foreach ($p in $targetPlugins) {
    $pName = $p.Name
    $destJar = Join-Path $outLibs ($pName + '-1.0.0.jar')
    if (Test-Path $destJar) {
        $kb = [math]::Round((Get-Item $destJar).Length / 1024, 2)
        Write-Host "  [OK] $pName-1.0.0.jar ($kb KB)" -ForegroundColor Cyan
    }
}
