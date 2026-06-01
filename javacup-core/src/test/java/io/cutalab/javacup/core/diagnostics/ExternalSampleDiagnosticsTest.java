package io.cutalab.javacup.core.diagnostics;

import io.cutalab.javacup.core.session.ExternalMetricSample;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalSampleDiagnosticsTest {

    private final ExternalSampleDiagnostics diagnostics = new ExternalSampleDiagnostics();

    @Test
    void doesNotWarnWhenThereAreTooFewSamples() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100),
                sample(sessionId, 110),
                sample(sessionId, 120)
        );

        assertTrue(diagnostics.analyzeHeapGrowth(samples).isEmpty());
    }

    @Test
    void doesNotWarnWhenGrowthIsTooSmall() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100),
                sample(sessionId, 103),
                sample(sessionId, 107),
                sample(sessionId, 111)
        );

        assertTrue(diagnostics.analyzeHeapGrowth(samples).isEmpty());
    }

    @Test
    void warnsWhenHeapGrowsEnoughDuringSession() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100),
                sample(sessionId, 115),
                sample(sessionId, 132),
                sample(sessionId, 140)
        );

        var warning = diagnostics.analyzeHeapGrowth(samples);

        assertTrue(warning.isPresent());
        assertEquals("HEAP_SESSION_GROWING", warning.get().code());
        assertEquals(DiagnosticSeverity.WARNING, warning.get().severity());
    }

    private ExternalMetricSample sample(UUID sessionId, long heapUsedMb) {
        return new ExternalMetricSample(
                sessionId,
                1234L,
                Instant.now(),
                heapUsedMb,
                256L,
                10L,
                1L
        );
    }
}
