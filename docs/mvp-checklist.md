# Javacup MVP checklist

This checklist defines the local MVP closure for Javacup `0.3.0`.

## MVP scope

Javacup `0.3.0` is a local-first JVM memory diagnostic MVP.

The MVP workflow is:

1. Start the dashboard locally.
2. Discover local Java processes.
3. Select an external JVM.
4. Read local JVM data through JDK diagnostic commands.
5. Show structured probe, heap, uptime and sample information.
6. Collect external metric samples.
7. Explain diagnostics in the External Metrics page.
8. Generate a readable report preview.
9. Download/archive JSON reports.
10. Compare archived reports locally.

## Closure criteria

The MVP is considered closed when:

- `scripts/verify-mvp.sh` passes locally.
- GitHub Actions are green.
- The repository is tagged as `v0.3.0`.
- No known blocking workflow issue prevents local usage.

## Non-goals for this MVP

The following items are intentionally left for later versions:

- Installer/package distribution.
- Long-term persistent database storage.
- Advanced charts and timelines.
- JFR analysis.
- Production monitoring agent mode.
- Remote JVM monitoring.
- Large UI redesigns.
