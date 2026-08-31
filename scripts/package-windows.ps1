$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage was not found. Install JDK 21+ and ensure its bin directory is on PATH."
}

if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
    throw "WiX Toolset 3 is required for a Windows EXE installer. Install it with: choco install wixtoolset -y"
}

& (Join-Path $PSScriptRoot "build.ps1")

$Pom = Get-Content (Join-Path $ProjectRoot "pom.xml") -Raw
$VersionMatch = [regex]::Match($Pom, '(?s)<artifactId>finance-manager-java</artifactId>\s*<version>([^<]+)</version>')
if (-not $VersionMatch.Success) {
    throw "Could not determine the application version from pom.xml."
}
$Version = $VersionMatch.Groups[1].Value.Trim()

$InputDir = Join-Path $ProjectRoot "package-input"
$ReleaseDir = Join-Path $ProjectRoot "release"
$SourceJar = Join-Path $ProjectRoot "dist\finance-manager-java.jar"
$PackageJar = Join-Path $InputDir "finance-manager-java.jar"

Remove-Item $InputDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $ReleaseDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $InputDir, $ReleaseDir | Out-Null
Copy-Item $SourceJar $PackageJar

Write-Host "Creating Finance Manager $Version Windows installer..."
& jpackage `
    --type exe `
    --name "Finance Manager" `
    --input $InputDir `
    --main-jar "finance-manager-java.jar" `
    --main-class "com.financemanager.FinanceManagerApplication" `
    --app-version $Version `
    --vendor "Nkotolane Pitso" `
    --description "Personal finance management desktop application" `
    --win-menu `
    --win-menu-group "Finance Manager" `
    --win-shortcut `
    --win-dir-chooser `
    --win-per-user-install `
    --dest $ReleaseDir

if ($LASTEXITCODE -ne 0) { throw "jpackage failed." }

Copy-Item $SourceJar (Join-Path $ReleaseDir "finance-manager-java-$Version.jar")

Write-Host ""
Write-Host "Packaging complete. Release files:"
Get-ChildItem $ReleaseDir -File | ForEach-Object { Write-Host " - $($_.FullName)" }
