# ==============================================================================
#           APEXSIONS PLUGIN SUITE — SMART TURBO MULTI-COMPILER
# ==============================================================================
param(
    [Parameter(Position = 0)]
    [string]$Plugin = "",

    [Parameter()]
    [switch]$All = $false,

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

$rootLibs = Join-Path (Split-Path -Parent $scriptDir) 'build\libs'
if (-not (Test-Path $rootLibs)) {
    New-Item -ItemType Directory -Path $rootLibs -Force | Out-Null
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
} else {
    $foundMvn = Get-ChildItem 'C:\Program Files\*\bin\mvn.cmd', 'C:\Program Files\*\maven\bin\mvn.cmd', 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($foundMvn) {
        $mvn = $foundMvn.FullName
        $hasMaven = $true
    }
}
if (-not $hasMaven) {
    Write-Host '❌ Maven not found! Please install Maven or add it to PATH.' -ForegroundColor Red
    exit 1
}

$allPlugins = @(
    @{ Name = 'ApexsionsCore';           Path = 'plugins\ApexsionsCore' },
    @{ Name = 'ApexsionsChat';           Path = 'plugins\ApexsionsChat' },
    @{ Name = 'ApexsionsEconomy';    Path = 'plugins\ApexsionsEconomy' },
    @{ Name = 'ApexsionsBattlepass'; Path = 'plugins\ApexsionsBattlepass' },
    @{ Name = 'ApexsionsShop';       Path = 'plugins\ApexsionsShop' },
    @{ Name = 'ApexsionsMedia';      Path = 'plugins\ApexsionsMedia' },
    @{ Name = 'ApexsionsCustomEnchants'; Path = 'plugins\ApexsionsCustomEnchants' }
)

function Test-PluginModified {
    param(
        [string]$Name,
        [string]$RelPath,
        [string]$RootDirectory
    )

    $pluginDir = Join-Path $RootDirectory $RelPath
    $builtJar = Join-Path $pluginDir ($Name + '-1.0.0.jar')
    $outJar = Join-Path (Join-Path $RootDirectory 'build\libs') ($Name + '-1.0.0.jar')

    if ((-not (Test-Path $builtJar)) -or (-not (Test-Path $outJar))) {
        return $true
    }

    $jarTime = (Get-Item $builtJar).LastWriteTimeUtc

    # Check pom.xml
    $pomFile = Join-Path $pluginDir 'pom.xml'
    if (Test-Path $pomFile) {
        if ((Get-Item $pomFile).LastWriteTimeUtc -gt $jarTime) {
            return $true
        }
    }

    # Check src
    $srcDir = Join-Path $pluginDir 'src'
    if (Test-Path $srcDir) {
        $newestSrc = Get-ChildItem -Path $srcDir -Recurse -File -ErrorAction SilentlyContinue | 
            Measure-Object -Property LastWriteTimeUtc -Maximum
        if ($newestSrc.Maximum -and ($newestSrc.Maximum -gt $jarTime)) {
            return $true
        }
    }

    return $false
}

$targetPlugins = @()

if ($All -or ($Plugin.ToLower() -eq 'all')) {
    $targetPlugins = $allPlugins
} elseif (-not [string]::IsNullOrWhiteSpace($Plugin)) {
    $search = $Plugin.ToLower().Replace('apexsions', '')
    if ($search -eq 'bp') { $search = 'battlepass' }
    if ($search -eq 'eco') { $search = 'economy' }
    if ($search -eq 'ace' -or $search -eq 'ce' -or $search -eq 'enchants' -or $search -eq 'enchant') { $search = 'customenchants' }
    foreach ($p in $allPlugins) {
        if ($p.Name.ToLower().Contains($search)) {
            $targetPlugins += $p
        }
    }
    if ($targetPlugins.Count -eq 0) {
        Write-Host "Plugin '$Plugin' not found! Available options: Core, Chat, Economy (eco), Battlepass (bp), Shop, Media, CustomEnchants (ace/ce), all" -ForegroundColor Red
        exit 1
    }
} else {
    foreach ($p in $allPlugins) {
        if (Test-PluginModified -Name $p.Name -RelPath $p.Path -RootDirectory $scriptDir) {
            $targetPlugins += $p
        }
    }

    if ($targetPlugins.Count -eq 0) {
        $stopwatch.Stop()
        $quickSec = [math]::Round($stopwatch.Elapsed.TotalSeconds, 2)
        Write-Host '==========================================================' -ForegroundColor Yellow
        Write-Host '     ⚡ APEXSIONS PLUGIN SUITE — SMART TURBO COMPILER    ' -ForegroundColor Yellow
        Write-Host '==========================================================' -ForegroundColor Yellow
        Write-Host ('  [✓] All 6 plugins are UP TO DATE! (Checked in {0}s)' -f $quickSec) -ForegroundColor Green
        Write-Host '      No changes detected in source files. Compilation skipped.' -ForegroundColor Gray
        Write-Host ''
        Write-Host "  💡 Tip: Gunakan '.\build.ps1 all' jika ingin memaksakan kompilasi seluruh plugin." -ForegroundColor DarkGray
        exit 0
    }
}

Write-Host '==========================================================' -ForegroundColor Yellow
Write-Host '     ⚡ APEXSIONS PLUGIN SUITE — SMART TURBO COMPILER    ' -ForegroundColor Yellow
Write-Host '==========================================================' -ForegroundColor Yellow
$targetNames = ($targetPlugins | ForEach-Object { $_.Name }) -join ', '
$infoMsg = 'Target: {0} ({1} of {2} plugins)' -f $targetNames, $targetPlugins.Count, $allPlugins.Count
Write-Host $infoMsg -ForegroundColor Cyan
if ($Clean) { 
    Write-Host 'Mode  : Clean Recompile (-Clean)' -ForegroundColor DarkYellow 
} else { 
    Write-Host 'Mode  : Smart Incremental Build' -ForegroundColor Green 
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
    $quotedPom = '"{0}"' -f $pomFile
    $fullArgs = @('-f', $quotedPom) + $Goals + $ExtraArgs

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
            if (Test-Path $rootLibs) {
                Copy-Item -Force $builtJar.FullName (Join-Path $rootLibs ($Name + '-1.0.0.jar'))
            }
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
        Write-Host ''
        Write-Host ('[{0}/{1}] ⚡ Building {2}...' -f $idx, $total, $pName) -ForegroundColor Green
        $res = Build-Plugin-Fast -Name $pName -RelPath $pRel -MvnCmd $mvn -Goals $mvnGoals -ExtraArgs $mvnArgs -OutDirectory $outLibs -RootDirectory $scriptDir
        
        if (-not $res.Success) {
            Write-Host ''
            Write-Host ('❌ Compilation FAILED for {0} (ExitCode: {1})' -f $pName, $res.ExitCode) -ForegroundColor Red
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
Write-Host ('      ⚡ ALL TARGETS BUILT SUCCESSFULLY IN {0}s!  ' -f $totalSec) -ForegroundColor Green
Write-Host '==========================================================' -ForegroundColor Yellow
foreach ($p in $targetPlugins) {
    $pName = $p.Name
    $destJar = Join-Path $outLibs ($pName + '-1.0.0.jar')
    if (Test-Path $destJar) {
        $kb = [math]::Round((Get-Item $destJar).Length / 1024, 2)
        Write-Host ('  [OK] {0}-1.0.0.jar ({1} KB)' -f $pName, $kb) -ForegroundColor Cyan
    }
}
