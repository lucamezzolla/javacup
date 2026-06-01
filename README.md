# Javacup

**A friendly JVM memory doctor for Java developers.**

Javacup is a local-first JVM diagnostic tool designed to help Java developers understand memory behavior, garbage collection trends and suspicious growth patterns through a clean Vaadin dashboard and readable reports.

It does not try to replace advanced profilers such as VisualVM, JDK Mission Control or commercial APM tools. Instead, Javacup focuses on practical diagnostics: it highlights suspicious JVM memory patterns, explains the evidence and helps developers decide what to inspect next.

> Understand your JVM memory before it spills over.

---

## Project status

Javacup is currently in early development.

The first goal is to build a small, reliable local MVP:

- run locally from a single Java command;
- expose a Vaadin dashboard on localhost;
- discover local Java processes;
- monitor heap, non-heap, metaspace, garbage collection and thread metrics;
- detect basic suspicious memory trends;
- generate readable diagnostic reports.

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

## Current architecture

The project is organized as a Maven multi-module application.

```text
javacup/
  javacup-core/
  javacup-dashboard/
  docs/
  scripts/
```

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

## Requirements

- Java 21
- Maven 3.8+

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

By default, Javacup is intended to run locally.
The dashboard should not be exposed publicly unless explicitly configured and secured.

---

## Roadmap

### 0.1.x — Local Memory MVP

- Local Vaadin dashboard
- Java process discovery
- Basic JVM memory metrics
- GC and thread metrics
- First diagnostic warnings
- HTML/JSON report export
- Demo applications with controlled leaks

### 0.2.x — Diagnostic engine

- Post-GC baseline analysis
- GC pressure detection
- Thread growth detection
- Metaspace growth detection
- Severity scoring
- Better report explanations

### 0.3.x — JFR support

- Start and stop JFR recordings
- Import `.jfr` files
- Summarize GC and allocation events
- Export JFR-based diagnostic reports

---

## License

License to be defined.

---

## Author

Created by [Luca Mezzolla](https://github.com/lucamezzolla).
