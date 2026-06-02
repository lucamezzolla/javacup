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
    void reportsInsufficientSamplesForTrendDiagnostics() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 10),
                sample(sessionId, 110, 10),
                sample(sessionId, 120, 10)
        );

        var warning = diagnostics.analyzeInsufficientSamples(samples);

        assertTrue(warning.isPresent());
        assertEquals("INSUFFICIENT_SAMPLES_FOR_TREND", warning.get().code());
        assertEquals(DiagnosticSeverity.INFO, warning.get().severity());
    }

    @Test
    void doesNotReportInsufficientSamplesWhenTrendSampleThresholdIsReached() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 10),
                sample(sessionId, 110, 10),
                sample(sessionId, 120, 10),
                sample(sessionId, 130, 10)
        );

        assertTrue(diagnostics.analyzeInsufficientSamples(samples).isEmpty());
    }

    @Test
    void reportsInsufficientSamplesForNullSampleList() {
        var warning = diagnostics.analyzeInsufficientSamples(null);

        assertTrue(warning.isPresent());
        assertEquals("INSUFFICIENT_SAMPLES_FOR_TREND", warning.get().code());
    }

    @Test
    void doesNotWarnWhenThereAreTooFewHeapSamples() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 10),
                sample(sessionId, 110, 10),
                sample(sessionId, 120, 10)
        );

        assertTrue(diagnostics.analyzeHeapGrowth(samples).isEmpty());
    }

    @Test
    void doesNotWarnWhenHeapGrowthIsTooSmall() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 10),
                sample(sessionId, 103, 10),
                sample(sessionId, 107, 10),
                sample(sessionId, 111, 10)
        );

        assertTrue(diagnostics.analyzeHeapGrowth(samples).isEmpty());
    }

    @Test
    void warnsWhenHeapGrowsEnoughDuringSession() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 10),
                sample(sessionId, 115, 10),
                sample(sessionId, 132, 10),
                sample(sessionId, 140, 10)
        );

        var warning = diagnostics.analyzeHeapGrowth(samples);

        assertTrue(warning.isPresent());
        assertEquals("HEAP_SESSION_GROWING", warning.get().code());
        assertEquals(DiagnosticSeverity.WARNING, warning.get().severity());
    }

    @Test
    void doesNotWarnWhenMetaspaceGrowthIsTooSmall() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 20),
                sample(sessionId, 100, 21),
                sample(sessionId, 100, 22),
                sample(sessionId, 100, 23)
        );

        assertTrue(diagnostics.analyzeMetaspaceGrowth(samples).isEmpty());
    }

    @Test
    void warnsWhenMetaspaceGrowsEnoughDuringSession() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 100, 20),
                sample(sessionId, 100, 24),
                sample(sessionId, 100, 27),
                sample(sessionId, 100, 30)
        );

        var warning = diagnostics.analyzeMetaspaceGrowth(samples);

        assertTrue(warning.isPresent());
        assertEquals("METASPACE_SESSION_GROWING", warning.get().code());
        assertEquals(DiagnosticSeverity.WARNING, warning.get().severity());
    }

    private ExternalMetricSample sample(UUID sessionId, long heapUsedMb, long metaspaceUsedMb) {
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
