package io.cutalab.javacup.core.metrics;

import java.time.Instant;

public record CurrentJvmMetrics(
        Instant timestamp,
        MemoryUsageSnapshot heap,
        MemoryUsageSnapshot nonHeap,
        int threadCount,
        int daemonThreadCount,
        int peakThreadCount,
        long totalStartedThreadCount,
        int loadedClassCount,
        long totalLoadedClassCount,
        long unloadedClassCount,
        long uptimeMillis
) {

    public long uptimeSeconds() {
        return uptimeMillis / 1000;
    }
}
