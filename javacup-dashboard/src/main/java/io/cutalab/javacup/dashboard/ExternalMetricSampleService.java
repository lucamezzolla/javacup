package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.MonitoringSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ExternalMetricSampleService {

    private static final int MAX_SAMPLES_PER_SESSION = 300;

    private final ConcurrentMap<UUID, List<ExternalMetricSample>> samplesBySession = new ConcurrentHashMap<>();

    public ExternalMetricSample addSample(MonitoringSession session, ExternalHeapInfo heapInfo) {
        ExternalMetricSample sample = new ExternalMetricSample(
                session.id(),
                session.pid(),
                Instant.now(),
                heapInfo.heapUsedMb().orElse(null),
                heapInfo.heapTotalMb().orElse(null),
                heapInfo.metaspaceUsedMb().orElse(null),
                heapInfo.classSpaceUsedMb().orElse(null)
        );

        List<ExternalMetricSample> samples = samplesBySession.computeIfAbsent(session.id(), key -> new ArrayList<>());

        synchronized (samples) {
            samples.add(sample);

            if (samples.size() > MAX_SAMPLES_PER_SESSION) {
                samples.remove(0);
            }
        }

        return sample;
    }

    public List<ExternalMetricSample> findSamples(UUID sessionId) {
        List<ExternalMetricSample> samples = samplesBySession.get(sessionId);

        if (samples == null) {
            return List.of();
        }

        synchronized (samples) {
            return List.copyOf(samples);
        }
    }

    public Optional<ExternalMetricSample> findLatestSample(UUID sessionId) {
        List<ExternalMetricSample> samples = samplesBySession.get(sessionId);

        if (samples == null) {
            return Optional.empty();
        }

        synchronized (samples) {
            if (samples.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(samples.get(samples.size() - 1));
        }
    }
}
