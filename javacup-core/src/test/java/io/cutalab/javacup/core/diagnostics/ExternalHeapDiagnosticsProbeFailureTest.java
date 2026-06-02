package io.cutalab.javacup.core.diagnostics;

import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalHeapDiagnosticsProbeFailureTest {

    private final ExternalHeapDiagnostics diagnostics = new ExternalHeapDiagnostics();

    @Test
    void reportsProcessNotFoundProbeFailure() {
        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfoWithFailure("PROCESS_NOT_FOUND"));

        assertEquals(1, warnings.size());
        assertEquals("PROBE_PROCESS_NOT_FOUND", warnings.get(0).code());
        assertEquals(DiagnosticSeverity.WARNING, warnings.get(0).severity());
    }

    @Test
    void reportsAttachProbeFailure() {
        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfoWithFailure("ATTACH_FAILED"));

        assertEquals(1, warnings.size());
        assertEquals("PROBE_ATTACH_FAILED", warnings.get(0).code());
        assertEquals(DiagnosticSeverity.WARNING, warnings.get(0).severity());
    }

    @Test
    void reportsJcmdUnavailableProbeFailure() {
        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfoWithFailure("JCMD_UNAVAILABLE"));

        assertEquals(1, warnings.size());
        assertEquals("PROBE_JCMD_UNAVAILABLE", warnings.get(0).code());
        assertEquals(DiagnosticSeverity.WARNING, warnings.get(0).severity());
    }

    @Test
    void reportsTimeoutProbeFailure() {
        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfoWithFailure("TIMEOUT"));

        assertEquals(1, warnings.size());
        assertEquals("PROBE_TIMEOUT", warnings.get(0).code());
        assertEquals(DiagnosticSeverity.WARNING, warnings.get(0).severity());
    }

    @Test
    void unknownFailureFallsBackToGenericProbeFailure() {
        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfoWithFailure("SOMETHING_NEW"));

        assertEquals(1, warnings.size());
        assertEquals("PROBE_FAILED", warnings.get(0).code());
        assertEquals(DiagnosticSeverity.INFO, warnings.get(0).severity());
    }

    private ExternalHeapInfo heapInfoWithFailure(String failureKind) {
        return new ExternalHeapInfo(
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
                "FAILED",
                failureKind,
                "synthetic test raw output"
        );
    }
}
