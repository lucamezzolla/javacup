package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.MonitoringSession;
import io.cutalab.javacup.core.session.MonitoringSessionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalMetricSampleServiceTest {

    private final ExternalMetricSampleService service = new ExternalMetricSampleService();

    @Test
    void keepsOnlyTheLatestSamplesWhenBufferLimitIsReached() {
        MonitoringSession session = new MonitoringSession(
                UUID.randomUUID(),
                1234L,
                "demo-app",
                Instant.now(),
                Instant.now(),
                MonitoringSessionStatus.ACTIVE
        );

        int maxSamples = service.maxSamplesPerSession();

        for (int index = 1; index <= maxSamples + 10; index++) {
            service.addSample(session, heapInfo(index));
        }

        List<ExternalMetricSample> samples = service.findSamples(session.id());

        assertEquals(maxSamples, samples.size());
        assertEquals(11L, samples.getFirst().heapUsedMb());
        assertEquals(maxSamples + 10L, samples.getLast().heapUsedMb());
    }

    @Test
    void returnsEmptyListForUnknownSession() {
        assertEquals(List.of(), service.findSamples(UUID.randomUUID()));
    }

    private ExternalHeapInfo heapInfo(long heapUsedMb) {
        long heapUsedKb = heapUsedMb * 1024L;

        return new ExternalHeapInfo(
                "garbage-first heap",
                heapUsedKb,
                256L * 1024L,
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
