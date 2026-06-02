package io.cutalab.javacup.core.report;

import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.metrics.ExternalVmUptime;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import io.cutalab.javacup.core.session.MonitoringSession;

import java.time.Instant;
import java.util.List;

public record ExternalMonitoringReport(
        ExternalMonitoringReportMetadata metadata,
        Instant generatedAt,
        MonitoringSession session,
        ExternalHeapInfo latestHeapInfo,
        ExternalVmUptime latestVmUptime,
        ExternalMetricSampleSummary sampleSummary,
        List<DiagnosticWarning> diagnostics,
        List<ExternalMetricSample> recentSamples
) {
}
