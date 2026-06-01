# Javacup ☕

**A friendly JVM memory doctor for Java developers.**

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C?logo=paypal&logoColor=white)](https://www.paypal.com/paypalme/lucamezzolla82)

> If you find Javacup useful or want to support its development, you can make a small donation through PayPal.  
> Your support helps improve documentation, testing, safety checks, UI polish and controlled production-readiness.

Javacup is a local-first JVM diagnostic tool designed to help Java developers understand memory behavior, garbage collection trends and suspicious growth patterns through a clean Vaadin dashboard and readable reports.

It does not try to replace advanced profilers such as VisualVM, JDK Mission Control or commercial APM tools. Instead, Javacup focuses on practical diagnostics: it highlights suspicious JVM memory patterns, explains the evidence and helps developers decide what to inspect next.

> Understand your JVM memory before it spills over.

---

## Project status

Javacup is **under active construction**.

This repository currently contains an early development preview. The application already runs locally and includes useful building blocks, but it is not production-ready yet.

Current focus:

- stable local dashboard;
- Java process discovery;
- safe external process probing;
- controlled demo applications;
- clean UI foundations;
- readable documentation.

External JVM monitoring is being introduced progressively. Javacup can currently run basic local `jcmd` probes against a selected Java process, including VM version, heap information and VM uptime.

---

## Current features

- Local Vaadin dashboard running on `127.0.0.1:8787`
- Shared application layout with sidebar navigation and branded header styling
- Compact Javacup header with coffee icon
- Local Java process discovery through the Java `ProcessHandle` API
- Process filtering by PID, application name, type, command or arguments
- Compact process table with shortened arguments and native browser hint details
- Self-process detection
- Process detail page at `/processes/{pid}`
- Wrapped process arguments in the process detail page
- Local access probe for selected Java processes using `jcmd`
- Raw external heap information probe through `jcmd GC.heap_info`
- Raw external VM uptime probe through `jcmd VM.uptime`
- External process metrics page based on raw `jcmd` probes
- Structured external heap summary parsed from `jcmd GC.heap_info`
- First external heap diagnostic rule: `HEAP_NEAR_MAX`
- First trend-based external heap diagnostic: `HEAP_SESSION_GROWING`
- Auto-refreshing external process metrics every 5 seconds
- External metrics page can still probe a PID when ProcessHandle metadata is unavailable
- External metrics page shows last refresh time for auto-refresh verification
- In-memory monitoring session for external process metrics
- Recent in-memory samples for external monitored processes
- Bounded external samples grid to avoid page growth during long sessions
- Current JVM memory metrics for the Javacup process
- Current JVM thread metrics
- Current JVM class loading metrics
- Current JVM garbage collection metrics
- In-memory JVM metric sampling every 2 seconds
- Recent metric samples table
- Demo memory application with normal, burst and intentional leak modes

---

## Available pages

| Page | Description |
| --- | --- |
| `/` | Dashboard home |
| `/processes` | Local Java process discovery and filtering |
| `/processes/{pid}` | Selected process detail and local `jcmd` probes |
| `/metrics/current` | Current JVM metrics for the Javacup process |
| `/metrics/samples` | Recent in-memory JVM metric samples |
| `/metrics/external/{pid}` | Raw external JVM heap and uptime information for a selected process |

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

## Demo applications

Javacup includes a small demo application that can be used to test process discovery and future memory monitoring features.

Run a normal Java process:

```bash
./scripts/run-demo-normal.sh
```

Run a process with temporary allocation bursts:

```bash
./scripts/run-demo-burst.sh
```

Run a process with intentional retained memory growth:

```bash
./scripts/run-demo-leak.sh
```

The leak demo runs with `-Xmx256m` and intentionally retains memory chunks.
It is only meant for local testing.

After starting one of these scripts, open:

```text
http://127.0.0.1:8787/processes
```

You should see the demo JAR as a separate Java process.

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
- `jcmd <pid> VM.version`
- `jcmd <pid> GC.heap_info`
- `jcmd <pid> VM.uptime`

Reading structured metrics from an external Java process will require a later milestone based on one or more of:

- local JMX
- Attach API
- `jcmd` output parsing
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
- External process access probes
- First diagnostic warnings, including HEAP_NEAR_MAX
- HTML/JSON report export
- Demo applications with controlled leaks

### 0.2.x — External process monitoring

- Safe local attach/JMX investigation
- Connect to a selected Java process
- Read memory, GC and thread metrics from the selected process
- Parse selected `jcmd` outputs into structured metrics
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

## Support the project

If Javacup helps you or you want to support its development, you can make a small donation through PayPal:

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C?logo=paypal&logoColor=white)](https://www.paypal.com/paypalme/lucamezzolla82)

Every contribution helps improve documentation, testing, safety checks, UI polish and controlled production-readiness.

---

## License

License to be defined.

---

## Author

Created by [Luca Mezzolla](https://github.com/lucamezzolla).
