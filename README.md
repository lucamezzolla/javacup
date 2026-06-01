# Javacup

**A friendly JVM memory doctor for Java developers.**

Javacup is a local-first JVM diagnostic tool.
It will help Java developers monitor heap, non-heap memory, metaspace, garbage collection and thread trends from a clean local Vaadin dashboard.

Javacup does not claim to automatically find every memory leak.
It highlights suspicious JVM memory behavior, explains the evidence and helps developers understand where to look.

## Current status

Early development.

## Run the dashboard

```bash
mvn -pl javacup-dashboard spring-boot:run
```

Then open:

```text
http://127.0.0.1:8787
```

## Requirements

- Java 21
- Maven 3.8+
# javacup
