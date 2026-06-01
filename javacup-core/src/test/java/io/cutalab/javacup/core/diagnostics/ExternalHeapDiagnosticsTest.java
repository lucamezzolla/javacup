package io.cutalab.javacup.core.diagnostics;

import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalHeapDiagnosticsTest {

    private final ExternalHeapDiagnostics diagnostics = new ExternalHeapDiagnostics();

    @Test
    void returnsInfoWhenHeapIsStructuredAndNotNearMax() {
        ExternalHeapInfo heapInfo = heapInfo(50_000L, 100_000L);

        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfo);

        assertEquals(1, warnings.size());
        assertEquals("HEAP_STRUCTURED_INFO_AVAILABLE", warnings.getFirst().code());
        assertEquals(DiagnosticSeverity.INFO, warnings.getFirst().severity());
    }

    @Test
    void returnsWarningWhenHeapIsNearMax() {
        ExternalHeapInfo heapInfo = heapInfo(90_000L, 100_000L);

        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfo);

        assertEquals(1, warnings.size());
        assertEquals("HEAP_NEAR_MAX", warnings.getFirst().code());
        assertEquals(DiagnosticSeverity.WARNING, warnings.getFirst().severity());
    }

    @Test
    void returnsCriticalWhenHeapIsVeryNearMax() {
        ExternalHeapInfo heapInfo = heapInfo(96_000L, 100_000L);

        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfo);

        assertEquals(1, warnings.size());
        assertEquals("HEAP_NEAR_MAX", warnings.getFirst().code());
        assertEquals(DiagnosticSeverity.CRITICAL, warnings.getFirst().severity());
    }

    @Test
    void returnsInfoWhenHeapCannotBeParsed() {
        ExternalHeapInfo heapInfo = new ExternalHeapInfo(
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "unstructured output"
        );

        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfo);

        assertEquals(1, warnings.size());
        assertEquals("HEAP_INFO_NOT_STRUCTURED", warnings.getFirst().code());
        assertEquals(DiagnosticSeverity.INFO, warnings.getFirst().severity());
    }

    private ExternalHeapInfo heapInfo(Long usedKb, Long totalKb) {
        return new ExternalHeapInfo(
                "garbage-first heap",
                usedKb,
                totalKb,
                null,
                1_000L,
                1_200L,
                2_000L,
                100L,
                200L,
                1_000L,
                "raw heap output"
        );
    }
}
