# Javacup

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C?logo=paypal&logoColor=white)](https://www.paypal.com/paypalme/lucamezzolla82)

> If you find Javacup useful or want to support its development, you can make a small donation through PayPal.  
> Your support helps improve documentation, testing, safety checks, UI polish and controlled production-readiness.

---

**Javacup** is a local-first JVM memory diagnostic dashboard for Java developers.

Javacup helps inspect local Java processes, collect external JVM memory samples, explain diagnostic signals, generate readable reports, archive JSON reports and compare archived reports locally.

## Project status

Javacup `0.3.0` is complete as a **Local JVM Memory Diagnostic MVP**.

The first MVP scope is concluded: the application has a working local workflow from process discovery to external JVM inspection, diagnostics, report preview, JSON archive and archived report comparison.

The project remains open to future improvements, but future work should be treated as **post-MVP enhancement work**, not MVP closure work.

Current released version:

- `0.3.0`

Current release tag:

- `v0.3.0`

## What Javacup does

Javacup provides a local workflow for inspecting JVM memory behavior during development, testing and support sessions.

Core workflow:

1. Start the local dashboard.
2. Discover local Java processes.
3. Select a target JVM.
4. Read JVM information through local JDK diagnostic commands.
5. Collect external metric samples.
6. Review diagnostics and session health.
7. Generate a readable report preview.
8. Download or archive a JSON report.
9. Search, preview and compare archived reports.

## Current features

Javacup `0.3.0` includes:

- Local Vaadin dashboard.
- Local Java process discovery.
- External JVM probing through local JDK diagnostic commands.
- Structured heap information.
- Structured VM uptime information.
- Probe status and probe failure classification.
- Diagnostics for process-not-found, attach failure, unavailable `jcmd`, timeout and generic probe failures.
- Uptime probe diagnostics.
- Heap pressure diagnostics.
- Heap growth diagnostics.
- Metaspace growth diagnostics.
- Sample-quality diagnostics:
  - `INSUFFICIENT_SAMPLES_FOR_TREND`
  - `PARTIAL_SAMPLE_DATA`
- Unsupported heap parser format diagnostic.
- External Metrics page with Session health summary:
  - verdict
  - probe state
  - sample quality
  - main issue
  - recommended action
- Visual Session health verdict.
- External metric sample collection.
- Heap trend summary.
- Metaspace trend summary.
- Lightweight heap trend chart.
- Lightweight Metaspace trend chart.
- Raw `GC.heap_info` output view.
- Raw `VM.uptime` output view.
- Readable report preview with:
  - Report verdict
  - Probe summary
  - Trend interpretation
  - Diagnostic summary
  - Recommended next actions
- JSON report download.
- Local JSON report archive.
- Archived report search.
- Archived report preview.
- Archived report comparison.
- Human-readable comparison notes for:
  - structured probe status
  - unsupported heap parser formats
  - insufficient sample data
  - partial sample data
- Light/dark theme toggle.
- Internal quick-use guide.
- MVP verification script: `scripts/verify-mvp.sh`.
- Bundled demo JVM launcher, when available: `scripts/run-demo.sh`.

## Available pages

- **Dashboard**: Project entry page with quick access to the main local workflow.
- **Processes**: Lists local Java processes that Javacup can inspect.
- **External Metrics**: Shows external JVM metrics for a selected process, including probe status, session health, heap/metaspace summaries, samples, diagnostics and report actions.
- **Current JVM Metrics**: Shows metrics for the Javacup JVM itself.
- **Metric Samples**: Shows local metric samples collected by the application.
- **Archived reports**: Lists locally archived JSON reports, supports search, preview and report comparison.
- **Local Lab**: Optional local workflow helper page for running the dashboard and bundled demo JVMs.
- **Guide**: Internal quick-use guide for the local diagnostic workflow and diagnostic interpretation.
- **Donate**: Support page for the project.

## Requirements

- Linux, macOS or Windows with a supported Java environment.
- JDK 21 or newer.
- Maven.
- A full JDK is recommended because Javacup relies on local diagnostic commands such as `jcmd`.

`jcmd` must be available from the JDK used to run Javacup.

## Build and test

From the project root:

```bash
mvn -q test
mvn -q -DskipTests package
```

## Run the dashboard

```bash
./scripts/run-dashboard.sh
```

Then open the local dashboard in the browser using the URL printed by Spring Boot.

## Optional demo JVMs

If the demo launcher is available:

```bash
./scripts/run-demo.sh normal
./scripts/run-demo.sh heap
./scripts/run-demo.sh metaspace
```

Use these demo JVMs to test process discovery, external metrics, sample collection, diagnostics and report generation.

## MVP verification

Before publishing or checking a release, run:

```bash
./scripts/verify-mvp.sh
```

Expected final line:

```text
Javacup MVP verification completed successfully.
```

## Reports

Javacup reports are local JSON files.

The report workflow includes:

- readable preview
- JSON download
- local archive
- archived search
- archived preview
- archived comparison

Javacup is local-first: reports stay on the machine unless the user explicitly shares them.

## Release status

`v0.3.0` is the first official local MVP release.

This release closes the initial MVP scope. Future releases may add packaging, richer charts, additional diagnostics, improved exports, JFR-oriented analysis, or longer-term local persistence.

Those items are intentionally considered **post-MVP improvements**.

## Non-goals for v0.3.0

The following items are not part of the `v0.3.0` MVP:

- Installer/package distribution.
- Remote JVM monitoring.
- Production agent mode.
- JFR recording/import workflow.
- Long-term database-backed persistence.
- Advanced historical dashboards.
- Full leak detector automation.

## License

See [LICENSE](LICENSE).
