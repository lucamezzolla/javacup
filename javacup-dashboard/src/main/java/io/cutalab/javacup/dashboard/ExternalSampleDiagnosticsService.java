package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.diagnostics.ExternalSampleDiagnostics;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalSampleDiagnosticsService {

    private final ExternalSampleDiagnostics diagnostics = new ExternalSampleDiagnostics();

    public List<DiagnosticWarning> analyze(List<ExternalMetricSample> samples) {
        return diagnostics.analyzeHeapGrowth(samples)
                .map(List::of)
                .orElseGet(List::of);
    }
}
