# Javacup

**A friendly JVM memory doctor for Java developers.**

Javacup is a local-first JVM diagnostic tool designed to help Java developers understand memory behavior, garbage collection trends and suspicious growth patterns through a clean Vaadin dashboard and readable reports.

It does not try to replace advanced profilers such as VisualVM, JDK Mission Control or commercial APM tools. Instead, Javacup focuses on practical diagnostics: it highlights suspicious JVM memory patterns, explains the evidence and helps developers decide what to inspect next.

> Understand your JVM memory before it spills over.

---

## Project status

Javacup is currently in early development.

The current version is a local development preview running on a Vaadin dashboard.
It already provides process discovery, current JVM metrics and in-memory metric sampling for the Javacup process itself.

External JVM monitoring is not enabled yet. The next technical milestone is to investigate safe local attach/JMX access for a selected Java process.

---

## Current features

- Local Vaadin dashboard running on `127.0.0.1:8787`
- Shared application layout with sidebar navigation
- Local Java process discovery through the Java `ProcessHandle` API
- Process filtering by PID, application name, type, command or arguments
- Compact process table with shortened arguments and tooltip details
- Self-process detection
- Process detail page placeholder at `/processes/{pid}`
- Local access probe for selected Java processes using `jcmd`
- Current JVM memory metrics for the Javacup process
- Current JVM thread metrics
- Current JVM class loading metrics
- Current JVM garbage collection metrics
- In-memory JVM metric sampling every 2 seconds
- Recent metric samples table

---

## Available pages

| Page | Description |
| --- | --- |
| `/` | Dashboard home |
| `/processes` | Local Java process discovery and filtering |
| `/processes/{pid}` | Selected process detail placeholder |
| `/metrics/current` | Current JVM metrics for the Javacup process |
| `/metrics/samples` | Recent in-memory JVM metric samples |

---

## Why Javacup?

Java memory issues are often difficult to understand from raw numbers alone.

Javacup aims to answer questions such as:

- Is my Java application really growing in memory over time?
- Does heap usage decrease after garbage collection?
- Is the post-GC heap baseline increasing?
- Is the JVM spending too much time in garbage collection?
- Are thread counts or metaspace usage growing unexpectedly?
- What should I inspect next?

The goal is not to magically find every memory leak.
The goal is to provide a clear, local and developer-friendly diagnostic assistant.

---

## Requirements

For development:

- Java 21
- Maven 3.8+
- Git

Recommended:

- VSCodium or VS Code with Java support
- A modern browser

---

## Installation for development

Clone the repository:

```bash
git clone git@github.com:lucamezzolla/javacup.git
cd javacup
```

Switch to the development branch:

```bash
git checkout development
```

Build the project:

```bash
mvn -q -DskipTests package
```

---

## Run locally

From the project root:

```bash
./scripts/run-dashboard.sh
```

Then open:

```text
http://127.0.0.1:8787
```

The dashboard binds to localhost by default.

```properties
server.address=127.0.0.1
server.port=8787
```

This is intentional: Javacup is designed to be local-first and should not be exposed publicly unless explicitly configured and secured.

---

## Development workflow

The project currently uses the `development` branch as the active working branch.

Recommended workflow:

```bash
git status
mvn -q -DskipTests package
./scripts/run-dashboard.sh
git add .
git commit -m "Describe the change"
git push
```

Release tags will be created only when the project reaches meaningful milestones.

---

## Current architecture

The project is organized as a Maven multi-module application.

```text
javacup/
  javacup-core/
  javacup-dashboard/
  javacup-demo-apps/
  docs/
  scripts/
```

Current modules:

| Module | Purpose |
| --- | --- |
| `javacup-core` | Shared models and core data structures |
| `javacup-dashboard` | Spring Boot and Vaadin local dashboard |
| `javacup-demo-apps` | Small demo application for process discovery and memory behavior experiments |

Planned modules:

```text
javacup-collector
javacup-analyzer
javacup-report
javacup-storage
javacup-agent
javacup-demo-apps
```

---

## Technical notes

Current metrics are collected from the Javacup JVM itself using standard Java MXBeans:

- `MemoryMXBean`
- `GarbageCollectorMXBean`
- `ThreadMXBean`
- `ClassLoadingMXBean`
- `RuntimeMXBean`

Local process discovery currently uses:

- `ProcessHandle`
- `ProcessHandle.Info`

Selected process access can currently be probed with:

- local JDK `jcmd` access probe

Reading metrics from an external Java process will require a later milestone based on one or more of:

- local JMX
- Attach API
- `jcmd` fallback
- optional Java Agent

---

## Roadmap

### 0.1.x — Local Memory MVP

- Local Vaadin dashboard
- Java process discovery
- Selected process detail page
- Basic JVM memory metrics
- GC and thread metrics
- In-memory metric sampling
- First diagnostic warnings
- HTML/JSON report export
- Demo applications with controlled leaks

### 0.2.x — External process monitoring

- Safe local attach/JMX investigation
- Connect to a selected Java process
- Read memory, GC and thread metrics from the selected process
- Handle permission and compatibility errors clearly
- Add monitoring session concept

### 0.3.x — Diagnostic engine

- Post-GC baseline analysis
- GC pressure detection
- Thread growth detection
- Metaspace growth detection
- Severity scoring
- Better report explanations

### 0.4.x — JFR support

- Start and stop JFR recordings
- Import `.jfr` files
- Summarize GC and allocation events
- Export JFR-based diagnostic reports

---

## Security and privacy

Javacup is local-first.

By default:

- the dashboard runs only on `127.0.0.1`;
- no data is uploaded automatically;
- no telemetry is sent;
- diagnostic data stays on the local machine.

Future export and upload features should include clear user consent and report sanitization options.

---

## License

License to be defined.

---

## Author

Created by [Luca Mezzolla](https://github.com/lucamezzolla).
