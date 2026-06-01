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
            warnings.add(new DiagnosticWarning(
                    "HEAP_INFO_NOT_STRUCTURED",
                    DiagnosticSeverity.INFO,
                    "Heap information is not fully structured yet",
                    "The selected JVM returned heap information, but Javacup could not extract enough structured values from it yet.",
                    "Raw heap information is still available in the page.",
                    "Keep the raw output for debugging. Parser support will be improved as more JVM/GC formats are tested."
            ));
        }

        return warnings;
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
