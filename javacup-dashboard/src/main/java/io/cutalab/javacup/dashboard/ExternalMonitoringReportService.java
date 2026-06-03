package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.AppInfo;
import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.diagnostics.DiagnosticSeverity;
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.metrics.ExternalVmUptime;
import io.cutalab.javacup.core.report.ExternalMonitoringReport;
import io.cutalab.javacup.core.report.ExternalMonitoringReportMetadata;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import io.cutalab.javacup.core.session.MonitoringSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Instant generatedAt = Instant.now();
        List<DiagnosticWarning> enrichedDiagnostics = new ArrayList<>(diagnostics);
        evaluateUptimeProbeFailure(latestVmUptime).ifPresent(diagnostic -> addIfMissing(enrichedDiagnostics, diagnostic));

        return new ExternalMonitoringReport(
                new ExternalMonitoringReportMetadata(
                        AppInfo.NAME,
                        AppInfo.VERSION,
                        AppInfo.PROJECT_URL,
                        generatedAt
                ),
                Instant.now(),
                session,
                latestHeapInfo,
                latestVmUptime,
                sampleSummary,
                List.copyOf(enrichedDiagnostics),
                List.copyOf(recentSamples)
        );
    }


    private Optional<DiagnosticWarning> evaluateUptimeProbeFailure(ExternalVmUptime uptime) {
        if (uptime == null) {
            return Optional.empty();
        }

        String failureKind = uptime.probeFailureKind();

        if (failureKind == null || failureKind.isBlank() || "NONE".equals(failureKind) || "UNKNOWN".equals(failureKind)) {
            return Optional.empty();
        }

        return switch (failureKind) {
            case "PROCESS_NOT_FOUND" -> Optional.of(new DiagnosticWarning(
                    "UPTIME_PROBE_PROCESS_NOT_FOUND",
                    DiagnosticSeverity.WARNING,
                    "VM uptime probe target is no longer available",
                    "The VM.uptime probe failed because the selected process could not be found.",
                    "Uptime probe failure kind: PROCESS_NOT_FOUND.",
                    "Go back to Processes and select a currently running Java process."
            ));
            case "ATTACH_FAILED" -> Optional.of(new DiagnosticWarning(
                    "UPTIME_PROBE_ATTACH_FAILED",
                    DiagnosticSeverity.WARNING,
                    "VM uptime probe could not attach to the selected JVM",
                    "The VM.uptime jcmd probe could not attach to the selected JVM with the current user or environment.",
                    "Uptime probe failure kind: ATTACH_FAILED.",
                    "Run Javacup with the same user as the target JVM and check operating-system attach permissions."
            ));
            case "JCMD_UNAVAILABLE" -> Optional.of(new DiagnosticWarning(
                    "UPTIME_PROBE_JCMD_UNAVAILABLE",
                    DiagnosticSeverity.WARNING,
                    "jcmd is unavailable for VM uptime",
                    "The VM.uptime probe could not run the jcmd executable.",
                    "Uptime probe failure kind: JCMD_UNAVAILABLE.",
                    "Make sure Javacup is running with a full JDK, not only a JRE, and that java.home/bin contains jcmd."
            ));
            case "TIMEOUT" -> Optional.of(new DiagnosticWarning(
                    "UPTIME_PROBE_TIMEOUT",
                    DiagnosticSeverity.WARNING,
                    "VM uptime probe timed out",
                    "The VM.uptime jcmd probe did not complete within the configured timeout.",
                    "Uptime probe failure kind: TIMEOUT.",
                    "Retry the probe. If the issue persists, the target JVM or the host may be overloaded."
            ));
            case "INTERRUPTED" -> Optional.of(new DiagnosticWarning(
                    "UPTIME_PROBE_INTERRUPTED",
                    DiagnosticSeverity.INFO,
                    "VM uptime probe was interrupted",
                    "The VM.uptime jcmd probe was interrupted before completion.",
                    "Uptime probe failure kind: INTERRUPTED.",
                    "Retry the probe if the target JVM is still running."
            ));
            default -> Optional.of(new DiagnosticWarning(
                    "UPTIME_PROBE_FAILED",
                    DiagnosticSeverity.INFO,
                    "VM uptime probe failed",
                    "The VM.uptime jcmd probe failed with a structured failure kind that does not have a dedicated diagnostic yet.",
                    "Uptime probe failure kind: " + failureKind + ".",
                    "Check the raw probe output for more details."
            ));
        };
    }

    private void addIfMissing(List<DiagnosticWarning> diagnostics, DiagnosticWarning diagnostic) {
        boolean alreadyPresent = diagnostics.stream()
                .anyMatch(existing -> existing.code().equals(diagnostic.code()));

        if (!alreadyPresent) {
            diagnostics.add(diagnostic);
        }
    }

    public String createReadablePreview(ExternalMonitoringReport report) {
        StringBuilder builder = new StringBuilder();

        builder.append("Generated at: ").append(report.generatedAt()).append(System.lineSeparator());
        builder.append("Application: ").append(report.metadata().applicationName()).append(System.lineSeparator());
        builder.append("Version: ").append(report.metadata().applicationVersion()).append(System.lineSeparator());
        builder.append("Project: ").append(report.metadata().projectUrl()).append(System.lineSeparator());
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
        builder.append("- Probe status: ").append(report.latestVmUptime() == null ? "unavailable" : valueOrUnavailable(report.latestVmUptime().probeStatus())).append(System.lineSeparator());
        builder.append("- Probe failure kind: ").append(report.latestVmUptime() == null ? "unavailable" : valueOrUnavailable(report.latestVmUptime().probeFailureKind())).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        builder.append("Heap summary").append(System.lineSeparator());
        builder.append("- Heap type: ").append(valueOrUnavailable(report.latestHeapInfo().collectorOrHeapType())).append(System.lineSeparator());
        builder.append("- Heap used: ").append(formatMb(report.latestHeapInfo().heapUsedMb().orElse(null))).append(System.lineSeparator());
        builder.append("- Heap total/committed: ").append(formatMb(report.latestHeapInfo().heapTotalMb().orElse(null))).append(System.lineSeparator());
        builder.append("- Metaspace used: ").append(formatMb(report.latestHeapInfo().metaspaceUsedMb().orElse(null))).append(System.lineSeparator());
        builder.append("- Probe status: ").append(valueOrUnavailable(report.latestHeapInfo().probeStatus())).append(System.lineSeparator());
        builder.append("- Probe failure kind: ").append(valueOrUnavailable(report.latestHeapInfo().probeFailureKind())).append(System.lineSeparator());
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

        appendDiagnosticSummary(builder, report.diagnostics());

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

    private void appendDiagnosticSummary(StringBuilder builder, List<DiagnosticWarning> diagnostics) {
        List<DiagnosticWarning> safeDiagnostics = diagnostics == null ? List.of() : diagnostics;

        builder.append("Diagnostic summary").append(System.lineSeparator());
        builder.append("- Total diagnostics: ").append(safeDiagnostics.size()).append(System.lineSeparator());
        builder.append("- INFO: ").append(countDiagnosticsWithSeverity(safeDiagnostics, "INFO")).append(System.lineSeparator());
        builder.append("- WARNING: ").append(countDiagnosticsWithSeverity(safeDiagnostics, "WARNING")).append(System.lineSeparator());
        builder.append("- CRITICAL: ").append(countDiagnosticsWithSeverity(safeDiagnostics, "CRITICAL")).append(System.lineSeparator());
        builder.append("- Codes: ").append(formatDiagnosticCodes(safeDiagnostics)).append(System.lineSeparator());
        builder.append(System.lineSeparator());
    }

    private long countDiagnosticsWithSeverity(List<DiagnosticWarning> diagnostics, String severityName) {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() != null)
                .filter(diagnostic -> severityName.equals(diagnostic.severity().name()))
                .count();
    }

    private String formatDiagnosticCodes(List<DiagnosticWarning> diagnostics) {
        if (diagnostics.isEmpty()) {
            return "none";
        }

        StringBuilder builder = new StringBuilder();

        for (DiagnosticWarning diagnostic : diagnostics) {
            if (diagnostic.code() == null || diagnostic.code().isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(", ");
            }

            builder.append(diagnostic.code());
        }

        return builder.isEmpty() ? "none" : builder.toString();
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
