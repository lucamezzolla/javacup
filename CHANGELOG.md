# Changelog

All notable changes to Javacup will be documented in this file.

The format follows the spirit of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project aims to use semantic versioning once public releases become stable.

---

## [Unreleased]

### Added

- Apache License 2.0.
- Local Vaadin dashboard running on `127.0.0.1:8787`.
- Branded Javacup header with coffee icon.
- Sidebar navigation.
- Local Java process discovery using the Java `ProcessHandle` API.
- Process filtering by PID, application name, type, command and arguments.
- Compact process table with shortened arguments.
- Native browser hint for full process arguments.
- Process detail page.
- Wrapped process arguments in the process detail page.
- Demo application module with:
  - normal mode;
  - burst allocation mode;
  - intentional leak mode.
- Local `jcmd` probes for selected Java processes:
  - `VM.version`;
  - `GC.heap_info`;
  - `VM.uptime`.
- External process metrics page.
- External heap summary parsed from `jcmd GC.heap_info`.
- External monitoring session model.
- Auto-refreshing external metrics view.
- Last refresh indicator.
- In-memory external metric samples.
- External sample buffer limited to 100 samples per monitoring session.
- Bounded external samples grid to avoid page growth during long sessions.
- Session trend summary for external heap samples.
- Basic external diagnostics:
  - `HEAP_NEAR_MAX`;
  - `HEAP_SESSION_GROWING`.
- External monitoring report model.
- Readable report preview.
- Direct JSON report download.
- Current JVM metrics for the Javacup process.
- In-memory metric sampling for the Javacup process.
- Recent metric samples table.
- Professional README with:
  - installation instructions;
  - run instructions;
  - current features;
  - roadmap;
  - PayPal support badge;
  - security and privacy notes.

### Changed

- Improved layout spacing across dashboard views.
- Moved JSON report download action to the top of the external metrics view.
- Improved process table layout to keep actions visible.
- Improved generated Vaadin frontend handling through `.gitignore` and `.gitattributes`.

### Fixed

- Removed generated Vaadin frontend files from Git language statistics.
- Fixed GitHub build issue caused by ignored `diagnostics` package.
- Fixed overly generic `.gitignore` rule for `diagnostics/`.
- Fixed parameterized route usage in the sidebar.
- Fixed external metrics lookup to probe by PID even when `ProcessHandle` metadata is unavailable.
- Fixed external metrics auto-refresh lifecycle.
- Removed accidental root file named `cd`.

---

## [0.1.0-alpha.1] - Planned

This will be the first alpha checkpoint for Javacup.

Expected scope:

- Local JVM dashboard.
- Java process discovery.
- External process metrics through local `jcmd`.
- Basic heap parsing.
- External monitoring session.
- In-memory samples.
- Basic diagnostics.
- JSON report export.
- Demo applications.
- Documentation suitable for early testers.

This release will not be production-ready.
It will be intended for local testing, feedback and controlled development validation.
