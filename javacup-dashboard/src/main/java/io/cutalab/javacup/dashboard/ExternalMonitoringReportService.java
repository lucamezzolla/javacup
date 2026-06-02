package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.metrics.ExternalVmUptime;
import io.cutalab.javacup.core.report.ExternalMonitoringReport;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import io.cutalab.javacup.core.session.MonitoringSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ExternalMonitoringReportService {

    public ExternalMonitoringReport createReport(
            MonitoringSession session,
            ExternalHeapInfo latestHeapInfo,
            ExternalVmUptime latestVmUptime,
            ExternalMetricSampleSummary sampleSummary,
            List<DiagnosticWarning> diagnostics,
            List<ExternalMetricSample> recentSamples
    ) {
        return new ExternalMonitoringReport(
                Instant.now(),
                session,
                latestHeapInfo,
                latestVmUptime,
                sampleSummary,
                List.copyOf(diagnostics),
                List.copyOf(recentSamples)
        );
    }

    public String createReadablePreview(ExternalMonitoringReport report) {
        StringBuilder builder = new StringBuilder();

        builder.append("Generated at: ").append(report.generatedAt()).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        builder.append("Session").append(System.lineSeparator());
        builder.append("- ID: ").append(report.session().id()).append(System.lineSeparator());
        builder.append("- PID: ").append(report.session().pid()).append(System.lineSeparator());
        builder.append("- Application: ").append(report.session().applicationName()).append(System.lineSeparator());
        builder.append("- Status: ").append(report.session().status()).append(System.lineSeparator());
        builder.append("- Started at: ").append(report.session().startedAt()).append(System.lineSeparator());
        builder.append("- Last updated at: ").append(report.session().lastUpdatedAt()).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        builder.append("VM uptime").append(System.lineSeparator());
        builder.append("- Uptime: ").append(report.latestVmUptime() == null ? "unavailable" : report.latestVmUptime().displayValue()).append(System.lineSeparator());
        builder.append("- Uptime seconds: ").append(report.latestVmUptime() == null || report.latestVmUptime().uptimeSeconds() == null ? "unavailable" : report.latestVmUptime().uptimeSeconds()).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        builder.append("Heap summary").append(System.lineSeparator());
        builder.append("- Heap type: ").append(valueOrUnavailable(report.latestHeapInfo().collectorOrHeapType())).append(System.lineSeparator());
        builder.append("- Heap used: ").append(formatMb(report.latestHeapInfo().heapUsedMb().orElse(null))).append(System.lineSeparator());
        builder.append("- Heap total/committed: ").append(formatMb(report.latestHeapInfo().heapTotalMb().orElse(null))).append(System.lineSeparator());
        builder.append("- Metaspace used: ").append(formatMb(report.latestHeapInfo().metaspaceUsedMb().orElse(null))).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        builder.append("Trend summary").append(System.lineSeparator());
        builder.append("- Samples: ").append(report.sampleSummary().sampleCount()).append(System.lineSeparator());
        builder.append("- First heap used: ").append(formatMb(report.sampleSummary().firstHeapUsedMb())).append(System.lineSeparator());
        builder.append("- Latest heap used: ").append(formatMb(report.sampleSummary().latestHeapUsedMb())).append(System.lineSeparator());
        builder.append("- Min heap used: ").append(formatMb(report.sampleSummary().minHeapUsedMb())).append(System.lineSeparator());
        builder.append("- Max heap used: ").append(formatMb(report.sampleSummary().maxHeapUsedMb())).append(System.lineSeparator());
        builder.append("- Heap growth: ").append(formatSignedMb(report.sampleSummary().heapGrowthMb())).append(System.lineSeparator());
        builder.append("- First Metaspace used: ").append(formatMb(report.sampleSummary().firstMetaspaceUsedMb())).append(System.lineSeparator());
        builder.append("- Latest Metaspace used: ").append(formatMb(report.sampleSummary().latestMetaspaceUsedMb())).append(System.lineSeparator());
        builder.append("- Min Metaspace used: ").append(formatMb(report.sampleSummary().minMetaspaceUsedMb())).append(System.lineSeparator());
        builder.append("- Max Metaspace used: ").append(formatMb(report.sampleSummary().maxMetaspaceUsedMb())).append(System.lineSeparator());
        builder.append("- Metaspace growth: ").append(formatSignedMb(report.sampleSummary().metaspaceGrowthMb())).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        builder.append("Diagnostics").append(System.lineSeparator());
        for (DiagnosticWarning diagnostic : report.diagnostics()) {
            builder.append("- ")
                    .append(diagnostic.code())
                    .append(" [")
                    .append(diagnostic.severity())
                    .append("] ")
                    .append(diagnostic.title())
                    .append(System.lineSeparator())
                    .append("  Evidence: ")
                    .append(diagnostic.evidence())
                    .append(System.lineSeparator())
                    .append("  Recommendation: ")
                    .append(diagnostic.recommendation())
                    .append(System.lineSeparator());
        }

        builder.append(System.lineSeparator());
        builder.append("Recent samples retained: ").append(report.recentSamples().size()).append(System.lineSeparator());

        return builder.toString();
    }

    private String valueOrUnavailable(String value) {
        return value == null || value.isBlank() ? "unavailable" : value;
    }

    private String formatMb(Long value) {
        return value == null ? "unavailable" : value + " MB";
    }

    private String formatSignedMb(Long value) {
        if (value == null) {
            return "unavailable";
        }

        if (value > 0) {
            return "+" + value + " MB";
        }

        return value + " MB";
    }
}
