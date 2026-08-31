# Deployment Guide

Finance Manager is a Java Swing desktop application. It is distributed as a Windows installer through GitHub Releases rather than hosted as a browser application.

## What happens after you commit

Every push to `main` or `master` runs the Java CI workflow. It builds the Maven project and runs the included smoke and integration tests.

A version tag such as `v2.0.0` starts the Windows Release workflow. GitHub Actions then:

1. Builds the Java 21 application.
2. Runs the smoke and integration tests.
3. Installs WiX Toolset on the Windows runner.
4. Uses `jpackage` to build a Windows `.exe` installer with a bundled Java runtime.
5. Publishes the installer and runnable JAR to a GitHub Release.

## First deployment

The current application version in `pom.xml` is `2.0.0`, so the matching release tag is `v2.0.0`.

```powershell
git add .
git commit -m "chore: add automated Windows deployment"
git push origin main

git tag v2.0.0
git push origin v2.0.0
```

If your default branch is `master`, replace `main` with `master` in the push command.

Open the repository on GitHub and select **Actions** to watch `Windows Release`. When it succeeds, open **Releases** to download the generated installer.

## Future releases

For a new release, update the `<version>` in `pom.xml`, commit the change, and push a matching `v` tag.

Example for `2.1.0`:

```xml
<version>2.1.0</version>
```

Then:

```powershell
git add pom.xml
git commit -m "chore: prepare release 2.1.0"
git push origin main
git tag v2.1.0
git push origin v2.1.0
```

The workflow intentionally rejects a tag when its version does not match `pom.xml`.

## Build locally on Windows

Requirements:

- JDK 21 or newer
- WiX Toolset 3 for installer creation

Build and test:

```powershell
.\scripts\build.ps1
```

Run:

```powershell
.\scripts\run.ps1
```

Install WiX with Chocolatey if needed:

```powershell
choco install wixtoolset -y
```

Create the Windows installer:

```powershell
.\scripts\package-windows.ps1
```

Generated files are placed in `release/` and are ignored by Git.

## Important

Do not deploy the Swing desktop UI to Vercel. Vercel serves web applications and HTTP services; a native Swing window does not render in a browser. GitHub Releases is the deployment channel configured for this repository.
