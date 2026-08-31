$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$JarFile = Join-Path $ProjectRoot "dist\finance-manager-java.jar"

if (-not (Test-Path $JarFile)) {
    Write-Host "Runnable JAR not found. Building first..."
    & (Join-Path $PSScriptRoot "build.ps1")
}

Set-Location $ProjectRoot
& java -jar $JarFile
