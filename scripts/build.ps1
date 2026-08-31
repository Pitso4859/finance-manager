$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw "JDK 21+ is required. Install a JDK and ensure javac is on PATH."
}

$JavaVersionOutput = (& javac -version 2>&1 | Out-String).Trim()
Write-Host "Using $JavaVersionOutput"

$BuildDir = Join-Path $ProjectRoot "build"
$MainClasses = Join-Path $BuildDir "classes"
$TestClasses = Join-Path $BuildDir "test-classes"
$DistDir = Join-Path $ProjectRoot "dist"
$JarFile = Join-Path $DistDir "finance-manager-java.jar"

Remove-Item $BuildDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $MainClasses, $TestClasses, $DistDir | Out-Null

$MainSources = @(Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object FullName)
if ($MainSources.Count -eq 0) {
    throw "No Java source files were found under src/main/java."
}

Write-Host "Compiling application..."
& javac --release 21 -d $MainClasses $MainSources
if ($LASTEXITCODE -ne 0) { throw "Application compilation failed." }

Write-Host "Creating runnable JAR..."
& jar --create --file $JarFile --main-class com.financemanager.FinanceManagerApplication -C $MainClasses .
if ($LASTEXITCODE -ne 0) { throw "JAR creation failed." }

$TestSources = @(Get-ChildItem -Path "src\test\java" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue | ForEach-Object FullName)
if ($TestSources.Count -gt 0) {
    Write-Host "Compiling tests..."
    & javac --release 21 -cp $MainClasses -d $TestClasses $TestSources
    if ($LASTEXITCODE -ne 0) { throw "Test compilation failed." }

    $TestClassPath = "$MainClasses;$TestClasses"
    Write-Host "Running CoreSmokeTest..."
    & java -ea -cp $TestClassPath com.financemanager.CoreSmokeTest
    if ($LASTEXITCODE -ne 0) { throw "CoreSmokeTest failed." }

    Write-Host "Running ServiceIntegrationTest..."
    & java -ea -cp $TestClassPath com.financemanager.ServiceIntegrationTest
    if ($LASTEXITCODE -ne 0) { throw "ServiceIntegrationTest failed." }
}

Write-Host ""
Write-Host "Build successful: $JarFile"
