# Release Process Guide

This document outlines the step-by-step process for preparing and publishing a new release of TaskManager.

---

## Release Checklist

1. **Verify Build and Tests Pass**:
   Run all automated checks locally to verify code correctness:
   ```bash
   mvn clean test
   ```
2. **Update Version Information**:
   - Bump the version inside `pom.xml`:
     ```xml
     <version>1.0.0</version>
     ```
   - Update version strings in `about_dialog.fxml` and `README.md` if necessary.
3. **Update the Changelog**:
   - Open `CHANGELOG.md`.
   - Ensure all changes since the last release are categorized under `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, or `Security`.
   - Update the release date and tag URL.
4. **Commit and Push Changes**:
   Commit the version bump and changelog update to the `main` branch:
   ```bash
   git add pom.xml CHANGELOG.md
   git commit -m "chore: release v1.0.0"
   git push origin main
   ```
5. **Create and Push a Git Tag**:
   Create a semantic version tag (prefixed with `v`):
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```
6. **Monitor CI/CD Build Pipeline**:
   - Go to the GitHub Actions tab.
   - Confirm that the `Build & Release` workflow has started.
   - The workflow compiles the project and packages installers on macOS, Windows, and Ubuntu runners in parallel.
7. **Publish Draft Release**:
   - Once the build jobs complete, a draft release will be created under Releases.
   - Verify that all native installer packages are successfully attached as assets:
     - `TaskManager-1.0.0.dmg` (macOS)
     - `TaskManager-1.0.0.msi` / `TaskManager.exe` (Windows)
     - `taskmanager_1.0.0_amd64.deb` / `taskmanager-1.0.0.x86_64.rpm` (Linux)
   - Copy the latest notes from `CHANGELOG.md` into the release description.
   - Publish the Release!
