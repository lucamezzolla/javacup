package io.cutalab.javacup.core.diagnostics;

import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalHeapDiagnosticsParserUnsupportedTest {

    private final ExternalHeapDiagnostics diagnostics = new ExternalHeapDiagnostics();

    @Test
    void reportsUnsupportedParserFormatWhenProbeSucceededButNoStructuredValuesExist() {
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
                "OK",
                "NONE",
                "synthetic unsupported but successful raw output"
        );

        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfo);

        assertEquals(1, warnings.size());
        assertEquals("HEAP_PARSER_UNSUPPORTED_FORMAT", warnings.get(0).code());
        assertEquals(DiagnosticSeverity.INFO, warnings.get(0).severity());
    }

    @Test
    void keepsLegacyUnstructuredDiagnosticWhenProbeStatusIsUnknown() {
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
                "UNKNOWN",
                "UNKNOWN",
                "synthetic legacy raw output"
        );

        List<DiagnosticWarning> warnings = diagnostics.analyze(heapInfo);

        assertEquals(1, warnings.size());
        assertEquals("HEAP_INFO_NOT_STRUCTURED", warnings.get(0).code());
    }
}
