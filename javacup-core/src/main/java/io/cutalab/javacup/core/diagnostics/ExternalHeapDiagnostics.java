package io.cutalab.javacup.core.diagnostics;

import io.cutalab.javacup.core.metrics.ExternalHeapInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExternalHeapDiagnostics {

    private static final double HEAP_NEAR_MAX_WARNING_THRESHOLD = 85.0;
    private static final double HEAP_NEAR_MAX_CRITICAL_THRESHOLD = 95.0;

    public List<DiagnosticWarning> analyze(ExternalHeapInfo heapInfo) {
        List<DiagnosticWarning> warnings = new ArrayList<>();

        evaluateProbeFailure(heapInfo).ifPresent(warnings::add);

        if (!warnings.isEmpty()) {
            return warnings;
        }

        evaluateHeapNearMax(heapInfo).ifPresent(warnings::add);

        if (warnings.isEmpty() && heapInfo.hasStructuredValues()) {
            warnings.add(new DiagnosticWarning(
                    "HEAP_STRUCTURED_INFO_AVAILABLE",
                    DiagnosticSeverity.INFO,
                    "Structured heap information available",
                    "Javacup was able to parse heap information from the selected process.",
                    "The external heap probe returned parseable heap data.",
                    "Continue observing the process over time. Trend-based diagnostics will be added in later milestones."
            ));
        }

        if (warnings.isEmpty()) {
            warnings.add(createUnstructuredHeapDiagnostic(heapInfo));
        }

        return warnings;
    }

    private DiagnosticWarning createUnstructuredHeapDiagnostic(ExternalHeapInfo heapInfo) {
        if (heapInfo != null && "OK".equals(heapInfo.probeStatus())) {
            return new DiagnosticWarning(
                    "HEAP_PARSER_UNSUPPORTED_FORMAT",
                    DiagnosticSeverity.INFO,
                    "Heap probe output format is not supported yet",
                    "The external JVM probe completed successfully, but Javacup could not extract structured heap values from the returned format.",
                    "Probe status: OK. Raw heap information is still available in the page and in archived reports.",
                    "Keep the raw output for debugging. Parser support will be improved as more JVM/GC formats are tested."
            );
        }

        return new DiagnosticWarning(
                "HEAP_INFO_NOT_STRUCTURED",
                DiagnosticSeverity.INFO,
                "Heap information is not fully structured yet",
                "The selected JVM returned heap information, but Javacup could not extract enough structured values from it yet.",
                "Raw heap information is still available in the page.",
                "Keep the raw output for debugging. Parser support will be improved as more JVM/GC formats are tested."
        );
    }

    private Optional<DiagnosticWarning> evaluateProbeFailure(ExternalHeapInfo heapInfo) {
        if (heapInfo == null) {
            return Optional.empty();
        }

        String failureKind = heapInfo.probeFailureKind();

        if (failureKind == null || failureKind.isBlank() || "NONE".equals(failureKind) || "UNKNOWN".equals(failureKind)) {
            return Optional.empty();
        }

        return switch (failureKind) {
            case "PROCESS_NOT_FOUND" -> Optional.of(new DiagnosticWarning(
                    "PROBE_PROCESS_NOT_FOUND",
                    DiagnosticSeverity.WARNING,
                    "Selected JVM process is no longer available",
                    "The external JVM probe failed because the selected process could not be found.",
                    "Probe failure kind: PROCESS_NOT_FOUND.",
                    "Go back to Processes and select a currently running Java process. If this happens often, the target application may be short-lived."
            ));
            case "ATTACH_FAILED" -> Optional.of(new DiagnosticWarning(
                    "PROBE_ATTACH_FAILED",
                    DiagnosticSeverity.WARNING,
                    "Javacup could not attach to the selected JVM",
                    "The local jcmd probe could not attach to the selected JVM with the current user or environment.",
                    "Probe failure kind: ATTACH_FAILED.",
                    "Run Javacup with the same user as the target JVM and check operating-system attach permissions."
            ));
            case "JCMD_UNAVAILABLE" -> Optional.of(new DiagnosticWarning(
                    "PROBE_JCMD_UNAVAILABLE",
                    DiagnosticSeverity.WARNING,
                    "jcmd is unavailable",
                    "The local probe could not run the jcmd executable.",
                    "Probe failure kind: JCMD_UNAVAILABLE.",
                    "Make sure Javacup is running with a full JDK, not only a JRE, and that java.home/bin contains jcmd."
            ));
            case "TIMEOUT" -> Optional.of(new DiagnosticWarning(
                    "PROBE_TIMEOUT",
                    DiagnosticSeverity.WARNING,
                    "jcmd probe timed out",
                    "The local jcmd probe did not complete within the configured timeout.",
                    "Probe failure kind: TIMEOUT.",
                    "Retry the probe. If the issue persists, the target JVM or the host may be overloaded."
            ));
            case "INTERRUPTED" -> Optional.of(new DiagnosticWarning(
                    "PROBE_INTERRUPTED",
                    DiagnosticSeverity.INFO,
                    "JVM probe was interrupted",
                    "The local jcmd probe was interrupted before completion.",
                    "Probe failure kind: INTERRUPTED.",
                    "Retry the probe if the target JVM is still running."
            ));
            default -> Optional.of(new DiagnosticWarning(
                    "PROBE_FAILED",
                    DiagnosticSeverity.INFO,
                    "JVM probe failed",
                    "The local jcmd probe failed with a structured failure kind that does not have a dedicated diagnostic yet.",
                    "Probe failure kind: " + failureKind + ".",
                    "Check the raw probe output for more details."
            ));
        };
    }

    private Optional<DiagnosticWarning> evaluateHeapNearMax(ExternalHeapInfo heapInfo) {
        Optional<Long> usedMb = heapInfo.heapUsedMb();
        Optional<Long> totalMb = heapInfo.heapTotalMb();

        if (usedMb.isEmpty() || totalMb.isEmpty() || totalMb.get() <= 0) {
            return Optional.empty();
        }

        double percentage = usedMb.get() * 100.0 / totalMb.get();

        if (percentage < HEAP_NEAR_MAX_WARNING_THRESHOLD) {
            return Optional.empty();
        }

        DiagnosticSeverity severity = percentage >= HEAP_NEAR_MAX_CRITICAL_THRESHOLD
                ? DiagnosticSeverity.CRITICAL
                : DiagnosticSeverity.WARNING;

        return Optional.of(new DiagnosticWarning(
                "HEAP_NEAR_MAX",
                severity,
                "Heap usage is close to the available heap",
                "The selected Java process is using a high percentage of the heap reported by the external probe.",
                String.format("Heap used is %d MB over %d MB, approximately %.2f%%.", usedMb.get(), totalMb.get(), percentage),
                "Observe the trend over time. If this value stays high or keeps growing, check memory-intensive operations, retained collections, caches and large buffers."
        ));
    }
}
