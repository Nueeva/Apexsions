# ==============================================================================
#           APEXSIONS PLUGIN SUITE — AUTOMATED MULTI-COMPILER
# ==============================================================================
param(
    [Parameter(Position = 0)]
    [string]$Plugin = ""
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$outLibs = "build/libs"
if (!(Test-Path $outLibs)) { New-Item -ItemType Directory -Path $outLibs -Force | Out-Null }

# Locate JDK
$localJdk = "$scriptDir/plugins/ApexsionsCore/jdk-21"
if (Test-Path "$localJdk/bin/javac.exe") {
    $env:JAVA_HOME = (Get-Item $localJdk).FullName
    $env:Path = "$($env:JAVA_HOME)/bin;$env:Path"
} elseif (!(Test-Path "$env:JAVA_HOME/bin/javac.exe")) {
    $foundJavac = Get-ChildItem "C:\Program Files\Eclipse Adoptium\*\bin\javac.exe", "C:\Program Files\Java\*\bin\javac.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($foundJavac) {
        $env:JAVA_HOME = $foundJavac.Directory.Parent.FullName
        $env:Path = "$($env:JAVA_HOME)/bin;$env:Path"
    }
}

# Check Maven
$hasMaven = $false
$mvn = "$scriptDir/plugins/ApexsionsCore/apache-maven-3.9.9/bin/mvn.cmd"
if (Test-Path $mvn) {
    $hasMaven = $true
} elseif (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    $mvn = "mvn"
    $hasMaven = $true
}

$allPlugins = @(
    @{ Name = "ApexsionsCore";       Path = "plugins/ApexsionsCore" },
    @{ Name = "ApexsionsChat";       Path = "plugins/ApexsionsChat" },
    @{ Name = "ApexsionsEconomy";    Path = "plugins/ApexsionsEconomy" },
    @{ Name = "ApexsionsBattlepass"; Path = "plugins/ApexsionsBattlepass" },
    @{ Name = "ApexsionsShop";       Path = "plugins/ApexsionsShop" },
    @{ Name = "ApexsionsMedia";      Path = "plugins/ApexsionsMedia" }
)

$targetPlugins = @()
if ([string]::IsNullOrWhiteSpace($Plugin) -or $Plugin.ToLower() -eq "all") {
    $targetPlugins = $allPlugins
} else {
    $search = $Plugin.ToLower().Replace("apexsions", "")
    foreach ($p in $allPlugins) {
        if ($p.Name.ToLower().Contains($search)) {
            $targetPlugins += $p
        }
    }
    if ($targetPlugins.Count -eq 0) {
        Write-Host "Plugin '$Plugin' not found! Available options: ApexsionsCore, ApexsionsChat, ApexsionsEconomy, ApexsionsBattlepass, ApexsionsShop, ApexsionsMedia, all" -ForegroundColor Red
        exit 1
    }
}

Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "         APEXSIONS PLUGIN SUITE MULTI-COMPILER            " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "Target: $($targetPlugins.Name -join ', ')" -ForegroundColor Cyan

if ($hasMaven) {
    $index = 1
    $total = $targetPlugins.Count
    foreach ($p in $targetPlugins) {
        $pName = $p.Name
        $pDir = $p.Path
        Write-Host ""
        Write-Host "[$index/$total] Building $pName with Maven..." -ForegroundColor Green
        & $mvn -f "$pDir/pom.xml" clean package -DskipTests=true
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Compilation failed for $pName!" -ForegroundColor Red
            exit 1
        }

        $jarPattern = "$pDir/target/$pName-*.jar"
        $builtJar = Get-ChildItem -Path $jarPattern -Exclude "*shaded*", "*original*" | Select-Object -First 1
        if ($builtJar) {
            $kb = [math]::Round($builtJar.Length / 1024, 2)
            Copy-Item -Force $builtJar.FullName "$outLibs/$pName-1.0.0.jar"
            Copy-Item -Force $builtJar.FullName "$pDir/$pName-1.0.0.jar"
            Write-Host "  -> [OK] $pName-1.0.0.jar ($kb KB)" -ForegroundColor Cyan
        }
        $index++
    }
} else {
    Write-Host "Standalone Maven not in PATH. Using native JDK compiler pipeline..." -ForegroundColor Cyan
    $javac = "$env:JAVA_HOME/bin/javac.exe"
    $jarExe = "$env:JAVA_HOME/bin/jar.exe"

    $jars = (Get-ChildItem -Recurse "C:\Users\Rafriel\.gradle\caches\modules-2\files-2.1" -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch "sources" -and $_.Name -notmatch "javadoc" } | Select-Object -ExpandProperty FullName)
    $jars += (Get-ChildItem "$outLibs/*.jar" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName)
    $cp = $jars -join ";"

    $index = 1
    $total = $targetPlugins.Count
    foreach ($p in $targetPlugins) {
        $pName = $p.Name
        $pDir = $p.Path
        $srcDir = "$pDir/src/main/java"
        $resDir = "$pDir/src/main/resources"
        $classesDir = "build/$pName-classes"
        $stagingDir = "build/$pName-staging"

        if (Test-Path $srcDir) {
            Write-Host ""
            Write-Host "[$index/$total] Compiling $pName..." -ForegroundColor Green
            if (!(Test-Path $classesDir)) { New-Item -ItemType Directory -Path $classesDir -Force | Out-Null } else { Remove-Item -Recurse -Force "$classesDir/*" }
            if (!(Test-Path $stagingDir)) { New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null } else { Remove-Item -Recurse -Force "$stagingDir/*" }

            $sources = Get-ChildItem -Recurse "$srcDir/*.java" | Select-Object -ExpandProperty FullName
            & $javac -cp $cp -d $classesDir --release 21 -encoding UTF-8 $sources
            if ($LASTEXITCODE -ne 0) {
                Write-Host "Compilation failed for $pName!" -ForegroundColor Red
                exit 1
            }

            Copy-Item -Recurse "$classesDir/*" $stagingDir
            if (Test-Path $resDir) {
                Copy-Item -Recurse "$resDir/*" $stagingDir
            }

            $target1 = "$pDir/$pName-1.0.0.jar"
            $target2 = "$outLibs/$pName-1.0.0.jar"

            Push-Location $stagingDir
            & $jarExe -cf $target1 *
            & $jarExe -cf $target2 *
            Pop-Location

            $kb = [math]::Round((Get-Item $target2).Length / 1024, 2)
            Write-Host "  -> [OK] $pName-1.0.0.jar ($kb KB)" -ForegroundColor Cyan
        }
        $index++
    }
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "         COMPILATION COMPLETED SUCCESSFULLY!              " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

foreach ($p in $targetPlugins) {
    $outJar = "$outLibs/$($p.Name)-1.0.0.jar"
    if (Test-Path $outJar) {
        $kb = [math]::Round((Get-Item $outJar).Length / 1024, 2)
        Write-Host "  [OK] $($p.Name)-1.0.0.jar ($kb KB)" -ForegroundColor Yellow
    }
}
