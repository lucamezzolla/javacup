# Changelog

All notable changes to Javacup will be documented in this file.

The format follows the spirit of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project aims to use semantic versioning once public releases become stable.

---

## [Unreleased]

## [0.2.0-alpha.2-SNAPSHOT]

### Planned

- Internal quick-use guide in the dashboard.

## [0.2.0-alpha.1] - 2026-06-02

First 0.2.x alpha checkpoint focused on structured JVM probe diagnostics.

### Planned

- Start the next roadmap phase after the 0.1.x local MVP line.
- Improve structured JVM diagnostics and make runtime evidence less dependent on raw `jcmd` output.



## [0.1.x alpha line]

### Closed by

- `v0.1.0-alpha.4` closes the first local MVP alpha line.

### Included

- Local Vaadin dashboard.
- Java process discovery.
- External JVM monitoring through local `jcmd` probes.
- Demo applications for normal, burst, leak and Metaspace scenarios.
- JSON report generation and download.
- Local archived report storage.
- Archived report search by file name and date range.
- Archived report preview and download.
- Archived report comparison with diagnostics severity, interpretation, memory unit selection, sample summary comparison, `jcmd` status notes and report health score.

## [0.1.0-alpha.4] - 2026-06-02

### Changed

- Closing checkpoint for the 0.1.x alpha line before moving to the next roadmap phase.
- Keeps the advanced archived report comparison work from alpha.3 as the current stable baseline.

### Added

- Archived report comparison uses structured probe status when available.
- VM uptime probe failure kind converted into diagnostics.
- Probe failure kind converted into diagnostics.
- Probe status UI based on structured failure kind.
- Report health score for archived report comparison.
- jcmd status notes for archived report comparison.
- Sample summary comparison for archived reports.
- Memory risk notes for archived report comparison.
- Diagnostic change summary for archived report comparison.
- Overall verdict for archived report comparison.
- Old and new diagnostics sections in archived report comparison.
- Comparison context section for archived report comparison.
- Selection feedback for archived report comparison.
- Diagnostics severity comparison for archived reports.
- Human-readable memory values in archived report comparisons.
- Memory unit selector for archived report comparisons.
- Deterministic interpretation for archived report comparisons.
- Delta values in archived report comparison.
- Basic two-report comparison from the archived reports dashboard.
- Archived reports filters by file/path and local-time date range.
- Structured archived report details dialog.

### Planned

- More advanced heap charts for external monitoring sessions.
- More structured external JVM metrics.
- Improved report export options.
- Better diagnostic explanations.
- GitHub Release packaging.

---

## [0.1.0-alpha.2] - 2026-06-02

First alpha checkpoint for Javacup.

This release is intended for local testing, early feedback and controlled development validation.
It is not production-ready.

### Added

- Archived report preview directly from the dashboard.
- Archived reports view for local JSON report history.
- Local archive for generated external monitoring JSON reports.
- Shared project URL exposed through `AppInfo` for reports.
- Report metadata with application name, version, project URL and generation timestamp.
- Demo Metaspace mode for testing Metaspace trend diagnostics.
- Lightweight Metaspace usage trend chart for external samples.
- Metaspace trend summary for external samples.
- Donation page with PayPal support link.
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

- Archived reports grid now shows the full path in the File column and a dedicated download icon action.
- Downloadable archived report links with truncated path display.
- Dashboard theme CSS loaded through Vaadin AppShell configuration.
- Dashboard theme CSS loaded through Vaadin AppShell configuration.
- Improved layout spacing across dashboard views.
- Moved JSON report download action to the top of the external metrics view.
- Improved process table layout to keep actions visible.
- Improved generated Vaadin frontend handling through `.gitignore` and `.gitattributes`.

### Fixed

- Archived report comparison now supports KB-based report memory fields.
- Archived report comparison now reads current report JSON field names.
- Fixed heap type parsing when `GC.heap_info` reports type and values on the same line.
- Fixed GitHub Actions failures caused by incomplete test dependencies.
- Removed generated Vaadin frontend files from Git language statistics.
- Fixed GitHub build issue caused by ignored `diagnostics` package.
- Fixed overly generic `.gitignore` rule for `diagnostics/`.
- Fixed parameterized route usage in the sidebar.
- Fixed external metrics lookup to probe by PID even when `ProcessHandle` metadata is unavailable.
- Fixed external metrics auto-refresh lifecycle.
- Removed accidental root file named `cd`.\n\n### Added\n\n- Guide page now includes quick navigation and a suggested diagnostic workflow.\n- Guide page explains common diagnostic codes in a quick-reference section.\n- Light/dark theme toggle in the application header.\n- Internal quick-use guide with links for deeper JVM topics.\n- Archived report comparison explains unsupported heap parser formats in human-readable language.\n- Dedicated diagnostic for unsupported heap parser formats after successful probes.\n- Parser regression test based on real OpenJDK G1 `GC.heap_info` output.\n- Broader `GC.heap_info` parsing for generation-based heap outputs.\n- Unit tests for probe failure diagnostics.\n- Unit tests for structured probe failure classification.\n