package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.metrics.CurrentJvmMetrics;
import io.cutalab.javacup.core.metrics.JvmMetricSample;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JvmMetricSampleService {

    private static final int MAX_SAMPLES = 300;

    private final CurrentJvmMetricsService metricsService;
    private final List<JvmMetricSample> samples = new ArrayList<>();

    public JvmMetricSampleService(CurrentJvmMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Scheduled(fixedDelay = 2000)
    public void collectSample() {
        CurrentJvmMetrics metrics = metricsService.readCurrentMetrics();

        JvmMetricSample sample = new JvmMetricSample(
                metrics.timestamp(),
                metrics.heap().usedBytes(),
                metrics.heap().committedBytes(),
                metrics.heap().maxBytes(),
                metrics.nonHeap().usedBytes(),
                metrics.totalGarbageCollectionCount(),
                metrics.totalGarbageCollectionTimeMillis(),
                metrics.threadCount()
        );

        synchronized (samples) {
            samples.add(sample);

            if (samples.size() > MAX_SAMPLES) {
                samples.remove(0);
            }
        }
    }

    public List<JvmMetricSample> findRecentSamples() {
        synchronized (samples) {
            return List.copyOf(samples);
        }
    }

    public int maxSamples() {
        return MAX_SAMPLES;
    }
}
