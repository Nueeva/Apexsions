# Apexsions Plugin Suite Clean Multi-Plugin Build Script
$ErrorActionPreference = "Stop"

$buildDir = "build"
$libsDir = "$buildDir/dependencies"
$outLibs = "$buildDir/libs"

if (!(Test-Path $outLibs)) { New-Item -ItemType Directory -Path $outLibs -Force | Out-Null }
if (!(Test-Path $libsDir)) { New-Item -ItemType Directory -Path $libsDir -Force | Out-Null }

Write-Host "=== APEXSIONS PLUGIN SUITE BUILD ===" -ForegroundColor Yellow

# 1. Ensure Dependencies
$paperJar = "$libsDir/paper-api-1.21.4.jar"
if (!(Test-Path $paperJar)) {
    Write-Host "Fetching Paper API..." -ForegroundColor Cyan
    $paperJar = "$libsDir/paper-api.jar"
}

$papiJar = "$libsDir/placeholderapi.jar"
if (!(Test-Path $papiJar)) {
    Invoke-WebRequest -Uri "https://repo.extendedclip.com/content/repositories/placeholderapi/me/clip/placeholderapi/2.11.5/placeholderapi-2.11.5.jar" -OutFile $papiJar
}

$vaultJar = "$libsDir/vault.jar"
if (!(Test-Path $vaultJar)) {
    Invoke-WebRequest -Uri "https://jitpack.io/com/github/MilkBowl/VaultAPI/1.7/VaultAPI-1.7.jar" -OutFile $vaultJar
}

$advJar = "$libsDir/adventure-api.jar"
if (!(Test-Path $advJar)) {
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/net/kyori/adventure-api/4.17.0/adventure-api-4.17.0.jar" -OutFile $advJar
}

$advKeyJar = "$libsDir/adventure-key.jar"
if (!(Test-Path $advKeyJar)) {
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/net/kyori/adventure-key/4.17.0/adventure-key-4.17.0.jar" -OutFile $advKeyJar
}

$miniMsgJar = "$libsDir/adventure-text-minimessage.jar"
$plainJar = "$libsDir/adventure-text-serializer-plain.jar"
$stubsJar = "$libsDir/stubs-api.jar"

$bungeeJar = "$libsDir/bungeecord-chat.jar"
if (!(Test-Path $bungeeJar)) {
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/net/md-5/bungeecord-chat/1.20-R0.2/bungeecord-chat-1.20-R0.2.jar" -OutFile $bungeeJar
}

$annoJar = "$libsDir/annotations.jar"
if (!(Test-Path $annoJar)) {
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/jetbrains/annotations/24.1.0/annotations-24.1.0.jar" -OutFile $annoJar
}

$examJar = "$libsDir/examination-api.jar"
if (!(Test-Path $examJar)) {
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/net/kyori/examination-api/1.3.0/examination-api-1.3.0.jar" -OutFile $examJar
}

$guavaJar = "$libsDir/guava.jar"
if (!(Test-Path $guavaJar)) {
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/google/guava/guava/32.1.2-jre/guava-32.1.2-jre.jar" -OutFile $guavaJar
}

$luckpermsJar = "$libsDir/luckperms-api-5.4.jar"
$hikariJar = "$libsDir/hikaricp-6.2.1.jar"
$postgresJar = "$libsDir/postgresql-42.7.5.jar"
$caffeineJar = "$libsDir/caffeine-3.2.0.jar"
$flywayJar = "$libsDir/flyway-core-11.3.4.jar"

# ========================================================
# 1. BUILD APEXSIONS CORE
# ========================================================
Write-Host "`n[1/4] Building ApexsionsCore..." -ForegroundColor Green
$coreClasses = "$buildDir/core-classes"
if (Test-Path $coreClasses) { Remove-Item -Recurse -Force $coreClasses }
New-Item -ItemType Directory -Path $coreClasses -Force | Out-Null

$coreSources = Get-ChildItem -Recurse -Filter "*.java" "plugins/ApexsionsCore/src/main/java" | Select-Object -ExpandProperty FullName
$coreSourceList = [System.IO.Path]::GetFullPath("$buildDir/core-sources.txt")
[System.IO.File]::WriteAllLines($coreSourceList, ($coreSources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }))

$baseCoreJar = "plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar"
$coreClasspath = "$paperJar;$papiJar;$vaultJar;$advJar;$advKeyJar;$miniMsgJar;$plainJar;$bungeeJar;$annoJar;$examJar;$guavaJar;$luckpermsJar;$hikariJar;$postgresJar;$caffeineJar;$flywayJar;$stubsJar"

javac --release 21 -encoding UTF-8 -cp $coreClasspath -d $coreClasses "@$coreSourceList"
if ($LASTEXITCODE -ne 0) { throw "ApexsionsCore compilation failed" }

# Extract shaded dependencies (HikariCP, SQLite-JDBC, PostgreSQL, H2, Flyway, Caffeine) into staging
$coreStaging = "$buildDir/core-staging"
if (Test-Path $coreStaging) { Remove-Item -Recurse -Force $coreStaging }
New-Item -ItemType Directory -Path $coreStaging -Force | Out-Null

$shadedJars = @(
    "$libsDir/hikaricp-6.2.1.jar",
    "$libsDir/sqlite-jdbc-3.49.1.0.jar",
    "$libsDir/postgresql-42.7.5.jar",
    "$libsDir/h2-2.3.232.jar",
    "$libsDir/caffeine-3.2.0.jar",
    "$libsDir/flyway-core-11.3.4.jar"
)

foreach ($jarFile in $shadedJars) {
    if (Test-Path $jarFile) {
        Set-Location $coreStaging
        jar -xf (Resolve-Path "$PSScriptRoot/$jarFile").Path
        Set-Location $PSScriptRoot
    }
}

# Overlay newly compiled classes
Copy-Item -Path "$coreClasses/*" -Destination $coreStaging -Recurse -Force
# Overlay resources
Copy-Item -Path "plugins/ApexsionsCore/src/main/resources/*" -Destination $coreStaging -Recurse -Force

$coreOutputJar = "$outLibs/ApexsionsCore-1.0.0.jar"
jar -cf $coreOutputJar -C $coreStaging .
Copy-Item -Force $coreOutputJar "plugins/ApexsionsCore/ApexsionsCore-1.0.0.jar"
Write-Host "  -> ApexsionsCore-1.0.0.jar packaged successfully." -ForegroundColor Cyan


# ========================================================
# 2. BUILD APEXSIONS BATTLEPASS & ECONOMY
# ========================================================
Write-Host "`n[2/4 & 3/4] Building BattlePass & Economy..." -ForegroundColor Green
$commonClasses = "$buildDir/common-classes"
if (Test-Path $commonClasses) { Remove-Item -Recurse -Force $commonClasses }
New-Item -ItemType Directory -Path $commonClasses -Force | Out-Null

$bpEcoSources = Get-ChildItem -Recurse -Filter "*.java" "src/main/java" | Select-Object -ExpandProperty FullName
$bpEcoSourceList = [System.IO.Path]::GetFullPath("$buildDir/bpeco-sources.txt")
[System.IO.File]::WriteAllLines($bpEcoSourceList, ($bpEcoSources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }))

$bpEcoClasspath = "$paperJar;$papiJar;$vaultJar;$advJar;$advKeyJar;$examJar;$miniMsgJar;$plainJar;$bungeeJar;$annoJar;$guavaJar;$coreOutputJar;$luckpermsJar;$hikariJar"
javac --release 21 -encoding UTF-8 -cp $bpEcoClasspath -d $commonClasses "@$bpEcoSourceList"
if ($LASTEXITCODE -ne 0) { throw "Battlepass & Economy compilation failed" }

# BattlePass Packaging
$bpStaging = "$buildDir/bp-staging"
if (Test-Path $bpStaging) { Remove-Item -Recurse -Force $bpStaging }
New-Item -ItemType Directory -Path $bpStaging -Force | Out-Null
Copy-Item -Path "$commonClasses/*" -Destination $bpStaging -Recurse -Force
Copy-Item -Path "src/main/resources/*" -Destination $bpStaging -Recurse -Force
if (Test-Path "$bpStaging/economy_plugin.yml") { Remove-Item -Force "$bpStaging/economy_plugin.yml" }
if (Test-Path "$bpStaging/economy_config.yml") { Remove-Item -Force "$bpStaging/economy_config.yml" }

$bpOutputJar = "$outLibs/ApexsionsBattlepass-1.0.0.jar"
jar -cf $bpOutputJar -C $bpStaging .
Copy-Item -Force $bpOutputJar "plugins/ApexsionsBattlepass/ApexsionsBattlepass-1.0.0.jar"
Write-Host "  -> ApexsionsBattlepass-1.0.0.jar packaged successfully." -ForegroundColor Cyan

# Economy Packaging
$ecoStaging = "$buildDir/eco-staging"
if (Test-Path $ecoStaging) { Remove-Item -Recurse -Force $ecoStaging }
New-Item -ItemType Directory -Path $ecoStaging -Force | Out-Null
Copy-Item -Path "$commonClasses/*" -Destination $ecoStaging -Recurse -Force
Copy-Item -Path "src/main/resources/*" -Destination $ecoStaging -Recurse -Force

if (Test-Path "$ecoStaging/economy_plugin.yml") {
    Copy-Item -Path "$ecoStaging/economy_plugin.yml" -Destination "$ecoStaging/plugin.yml" -Force
    Remove-Item -Force "$ecoStaging/economy_plugin.yml"
}
if (Test-Path "$ecoStaging/economy_config.yml") {
    Copy-Item -Path "$ecoStaging/economy_config.yml" -Destination "$ecoStaging/config.yml" -Force
    Remove-Item -Force "$ecoStaging/economy_config.yml"
}
Remove-Item -Recurse -Force "$ecoStaging/quests", "$ecoStaging/passes", "$ecoStaging/shop", "$ecoStaging/exp-shop", "$ecoStaging/passes.yml", "$ecoStaging/rewards.yml", "$ecoStaging/seasons.yml" -ErrorAction SilentlyContinue

$ecoOutputJar = "$outLibs/ApexsionsEconomy-1.0.0.jar"
jar -cf $ecoOutputJar -C $ecoStaging .
Copy-Item -Force $ecoOutputJar "plugins/ApexsionsEconomy/ApexsionsEconomy-1.0.0.jar"
Write-Host "  -> ApexsionsEconomy-1.0.0.jar packaged successfully." -ForegroundColor Cyan


# ========================================================
# 3. BUILD APEXSIONS CHAT
# ========================================================
Write-Host "`n[4/5] Assembling ApexsionsChat..." -ForegroundColor Green
Copy-Item -Force "plugins/ApexsionsChat/ApexsionsChat-1.0.0.jar" "$outLibs/ApexsionsChat-1.0.0.jar"
Write-Host "  -> ApexsionsChat-1.0.0.jar ready." -ForegroundColor Cyan


# ========================================================
# 4. BUILD APEXSIONS SHOP
# ========================================================
Write-Host "`n[5/5] Building ApexsionsShop..." -ForegroundColor Green
$shopClasses = "$buildDir/shop-classes"
if (Test-Path $shopClasses) { Remove-Item -Recurse -Force $shopClasses }
New-Item -ItemType Directory -Path $shopClasses -Force | Out-Null

$shopSources = Get-ChildItem -Recurse -Filter "*.java" "plugins/ApexsionsShop/src/main/java" | Select-Object -ExpandProperty FullName
$shopSourceList = [System.IO.Path]::GetFullPath("$buildDir/shop-sources.txt")
[System.IO.File]::WriteAllLines($shopSourceList, ($shopSources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' }))

$shopClasspath = "$paperJar;$papiJar;$vaultJar;$advJar;$advKeyJar;$examJar;$miniMsgJar;$plainJar;$bungeeJar;$annoJar;$guavaJar;$coreOutputJar;$ecoOutputJar"
javac --release 21 -encoding UTF-8 -cp $shopClasspath -d $shopClasses "@$shopSourceList"
if ($LASTEXITCODE -ne 0) { throw "ApexsionsShop compilation failed" }

$shopStaging = "$buildDir/shop-staging"
if (Test-Path $shopStaging) { Remove-Item -Recurse -Force $shopStaging }
New-Item -ItemType Directory -Path $shopStaging -Force | Out-Null
Copy-Item -Path "$shopClasses/*" -Destination $shopStaging -Recurse -Force
Copy-Item -Path "plugins/ApexsionsShop/src/main/resources/*" -Destination $shopStaging -Recurse -Force

$shopOutputJar = "$outLibs/ApexsionsShop-1.0.0.jar"
jar -cf $shopOutputJar -C $shopStaging .
Copy-Item -Force $shopOutputJar "plugins/ApexsionsShop/ApexsionsShop-1.0.0.jar"
Write-Host "  -> ApexsionsShop-1.0.0.jar packaged successfully." -ForegroundColor Cyan

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  ALL 5 APEXSIONS PLUGINS BUILT & PACKAGED SUCCESSFULLY!" -ForegroundColor Green
Get-ChildItem "$outLibs/*.jar" | ForEach-Object {
    Write-Host "  [OK] $($_.Name) ($([math]::Round($_.Length / 1KB, 2)) KB)" -ForegroundColor Yellow
}
Write-Host "==========================================================" -ForegroundColor Green
