# Changelog

All notable changes to Javacup will be documented in this file.

The format follows the spirit of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project aims to use semantic versioning once public releases become stable.

---

## [Unreleased]

### Planned

- More advanced heap charts for external monitoring sessions.
- More structured external JVM metrics.
- Improved report export options.
- Better diagnostic explanations.
- GitHub Release packaging.

---

## [0.1.0-alpha.1] - 2026-06-02

First alpha checkpoint for Javacup.

This release is intended for local testing, early feedback and controlled development validation.
It is not production-ready.

### Added

- Tests for Metaspace trend diagnostics.
- Metaspace trend diagnostic `METASPACE_SESSION_GROWING`.
- Tests for external sample buffer retention.
- Tests for current external heap diagnostics.
- Probe status explanation for external JVM metrics.
- JUnit 5 test setup for core and dashboard modules.
- Report preview dialog for external monitoring reports.
- Tests for external heap trend diagnostics and sample summaries.
- Parser tests for external heap and VM uptime parsing.
- Structured external VM uptime parsed from `jcmd VM.uptime`.
- Heap trend chart labels for external samples.
- Lightweight heap usage trend chart for external samples.
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
  - release notes;
  - PayPal support badge;
  - security and privacy notes.

### Changed

- Improved layout spacing across dashboard views.
- Moved JSON report download action to the top of the external metrics view.
- Improved process table layout to keep actions visible.
- Improved generated Vaadin frontend handling through `.gitignore` and `.gitattributes`.

### Fixed

- Fixed heap type parsing when `GC.heap_info` reports type and values on the same line.
- Fixed GitHub Actions failures caused by incomplete test dependencies.
- Removed generated Vaadin frontend files from Git language statistics.
- Fixed GitHub build issue caused by ignored `diagnostics` package.
- Fixed overly generic `.gitignore` rule for `diagnostics/`.
- Fixed parameterized route usage in the sidebar.
- Fixed external metrics lookup to probe by PID even when `ProcessHandle` metadata is unavailable.
- Fixed external metrics auto-refresh lifecycle.
- Removed accidental root file named `cd`.
