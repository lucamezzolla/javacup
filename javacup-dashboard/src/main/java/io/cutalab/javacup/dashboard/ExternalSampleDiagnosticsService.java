package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.diagnostics.ExternalSampleDiagnostics;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalSampleDiagnosticsService {

    private final ExternalSampleDiagnostics diagnostics = new ExternalSampleDiagnostics();

    public List<DiagnosticWarning> analyze(List<ExternalMetricSample> samples) {
        List<DiagnosticWarning> warnings = new ArrayList<>();

        diagnostics.analyzeInsufficientSamples(samples).ifPresent(warnings::add);
        diagnostics.analyzeHeapGrowth(samples).ifPresent(warnings::add);
        diagnostics.analyzeMetaspaceGrowth(samples).ifPresent(warnings::add);

        return warnings;
    }
}
