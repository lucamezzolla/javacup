package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.diagnostics.ExternalHeapDiagnostics;
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalHeapDiagnosticsService {

    private final ExternalHeapDiagnostics diagnostics = new ExternalHeapDiagnostics();

    public List<DiagnosticWarning> analyze(ExternalHeapInfo heapInfo) {
        return diagnostics.analyze(heapInfo);
    }
}
