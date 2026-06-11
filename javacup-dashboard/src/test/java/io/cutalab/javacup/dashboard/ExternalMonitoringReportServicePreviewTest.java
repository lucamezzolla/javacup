package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.diagnostics.DiagnosticSeverity;
import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.metrics.ExternalVmUptime;
import io.cutalab.javacup.core.report.ExternalMonitoringReport;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import io.cutalab.javacup.core.session.MonitoringSession;
import io.cutalab.javacup.core.session.MonitoringSessionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalMonitoringReportServicePreviewTest {

    private final ExternalMonitoringReportService service = new ExternalMonitoringReportService();

    @Test
    void readablePreviewContainsHighLevelSections() {
        ExternalMonitoringReport report = reportWithDiagnostics(List.of(
                new DiagnosticWarning(
                        "HEAP_SESSION_GROWING",
                        DiagnosticSeverity.WARNING,
                        "Heap usage is growing during this monitoring session",
                        "The selected Java process shows increasing heap usage.",
                        "Heap grew from 64 MB to 128 MB.",
                        "Keep observing heap after workload stabilization."
                )
        ));

        String preview = service.createReadablePreview(report);

        assertTrue(preview.contains("Report verdict"));
        assertTrue(preview.contains("Review recommended"));
        assertTrue(preview.contains("Probe summary"));
        assertTrue(preview.contains("Heap probe status: OK"));
        assertTrue(preview.contains("Uptime probe status: OK"));
        assertTrue(preview.contains("Trend interpretation"));
        assertTrue(preview.contains("Heap trend:"));
        assertTrue(preview.contains("Metaspace trend:"));
        assertTrue(preview.contains("Diagnostic summary"));
        assertTrue(preview.contains("- WARNING: 1"));
        assertTrue(preview.contains("- Codes: HEAP_SESSION_GROWING"));
        assertTrue(preview.contains("Recommended next actions"));
        assertTrue(preview.contains("retained collections"));
    }

    @Test
    void readablePreviewExplainsInsufficientSamplesAction() {
        ExternalMonitoringReport report = reportWithDiagnostics(List.of(
                new DiagnosticWarning(
                        "INSUFFICIENT_SAMPLES_FOR_TREND",
                        DiagnosticSeverity.INFO,
                        "Not enough samples for reliable trend diagnostics",
                        "Javacup needs more samples before trend diagnostics are reliable.",
                        "Collected 2 sample(s). Trend diagnostics require at least 4 samples.",
                        "Keep the monitoring session running a little longer."
                )
        ));

        String preview = service.createReadablePreview(report);

        assertTrue(preview.contains("Informational"));
        assertTrue(preview.contains("not enough samples for reliable trend interpretation"));
        assertTrue(preview.contains("Collect more samples before relying on trend diagnostics."));
    }

    private ExternalMonitoringReport reportWithDiagnostics(List<DiagnosticWarning> diagnostics) {
        MonitoringSession session = new MonitoringSession(
                UUID.randomUUID(),
                1234L,
                "Test JVM",
                Instant.parse("2026-06-02T10:00:00Z"),
                Instant.parse("2026-06-02T10:05:00Z"),
                MonitoringSessionStatus.ACTIVE
        );

        ExternalHeapInfo heapInfo = new ExternalHeapInfo(
                "garbage-first heap",
                131072L,
                262144L,
                1048576L,
                32768L,
                40960L,
                262144L,
                8192L,
                10240L,
                131072L,
                "raw heap output"
        ).withProbeMetadata("OK", "NONE");

        ExternalVmUptime uptime = new ExternalVmUptime(
                300L,
                "300.000 s"
        ).withProbeMetadata("OK", "NONE");

        ExternalMetricSampleSummary summary = new ExternalMetricSampleSummary(
                4,
                64L,
                128L,
                64L,
                128L,
                64L,
                16L,
                24L,
                16L,
                24L,
                8L
        );

        List<ExternalMetricSample> recentSamples = List.of(
                sample(session.id(), 64L, 16L),
                sample(session.id(), 80L, 18L),
                sample(session.id(), 96L, 20L),
                sample(session.id(), 128L, 24L)
        );

        return service.createReport(
                session,
                heapInfo,
                uptime,
                summary,
                diagnostics,
                recentSamples
        );
    }

    private ExternalMetricSample sample(UUID sessionId, long heapUsedMb, long metaspaceUsedMb) {
        return new ExternalMetricSample(
                sessionId,
                1234L,
                Instant.now(),
                heapUsedMb,
                256L,
                metaspaceUsedMb,
                8L
        );
    }
}
