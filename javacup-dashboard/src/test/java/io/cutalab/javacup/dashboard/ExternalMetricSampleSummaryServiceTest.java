package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExternalMetricSampleSummaryServiceTest {

    private final ExternalMetricSampleSummaryService service = new ExternalMetricSampleSummaryService();

    @Test
    void summarizesHeapValues() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 50),
                sample(sessionId, 70),
                sample(sessionId, 65),
                sample(sessionId, 95)
        );

        ExternalMetricSampleSummary summary = service.summarize(samples);

        assertEquals(4, summary.sampleCount());
        assertEquals(50L, summary.firstHeapUsedMb());
        assertEquals(95L, summary.latestHeapUsedMb());
        assertEquals(50L, summary.minHeapUsedMb());
        assertEquals(95L, summary.maxHeapUsedMb());
        assertEquals(45L, summary.heapGrowthMb());
    }


    @Test
    void summarizesMetaspaceValues() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                sample(sessionId, 50, 20),
                sample(sessionId, 70, 22),
                sample(sessionId, 65, 21),
                sample(sessionId, 95, 30)
        );

        ExternalMetricSampleSummary summary = service.summarize(samples);

        assertEquals(20L, summary.firstMetaspaceUsedMb());
        assertEquals(30L, summary.latestMetaspaceUsedMb());
        assertEquals(20L, summary.minMetaspaceUsedMb());
        assertEquals(30L, summary.maxMetaspaceUsedMb());
        assertEquals(10L, summary.metaspaceGrowthMb());
    }

    @Test
    void handlesEmptySamples() {
        ExternalMetricSampleSummary summary = service.summarize(List.of());

        assertEquals(0, summary.sampleCount());
        assertFalse(summary.hasHeapData());
    }

    @Test
    void ignoresSamplesWithoutHeapValue() {
        UUID sessionId = UUID.randomUUID();

        List<ExternalMetricSample> samples = List.of(
                new ExternalMetricSample(sessionId, 1234L, Instant.now(), null, null, null, null)
        );

        ExternalMetricSampleSummary summary = service.summarize(samples);

        assertEquals(1, summary.sampleCount());
        assertFalse(summary.hasHeapData());
    }

    private ExternalMetricSample sample(UUID sessionId, long heapUsedMb) {
        return sample(sessionId, heapUsedMb, 10L);
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
