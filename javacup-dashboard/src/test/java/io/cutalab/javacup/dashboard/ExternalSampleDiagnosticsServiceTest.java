package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalSampleDiagnosticsServiceTest {

    private final ExternalSampleDiagnosticsService service = new ExternalSampleDiagnosticsService();

    @Test
    void includesInsufficientSamplesDiagnosticBeforeGrowthDiagnostics() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 10),
                sample(sessionId, 110, 10)
        );

        List<DiagnosticWarning> warnings = service.analyze(samples);

        assertEquals(1, warnings.size());
        assertEquals("INSUFFICIENT_SAMPLES_FOR_TREND", warnings.get(0).code());
    }

    @Test
    void includesPartialSampleDataDiagnostic() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100L, 10L),
                sample(sessionId, null, 11L),
                sample(sessionId, null, 12L),
                sample(sessionId, 130L, null)
        );

        List<DiagnosticWarning> warnings = service.analyze(samples);

        assertEquals(1, warnings.size());
        assertEquals("PARTIAL_SAMPLE_DATA", warnings.get(0).code());
    }

    private ExternalMetricSample sample(UUID sessionId, long heapUsedMb, long metaspaceUsedMb) {
        return sample(sessionId, Long.valueOf(heapUsedMb), Long.valueOf(metaspaceUsedMb));
    }

    private ExternalMetricSample sample(UUID sessionId, Long heapUsedMb, Long metaspaceUsedMb) {
        return new ExternalMetricSample(
                sessionId,
                1234L,
                Instant.now(),
                heapUsedMb,
                256L,
                metaspaceUsedMb,
                1L
        );
    }
}
