package io.cutalab.javacup.core.metrics;

import java.time.Instant;
import java.util.List;

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
        long uptimeMillis,
        List<GarbageCollectorSnapshot> garbageCollectors
) {

    public long uptimeSeconds() {
        return uptimeMillis / 1000;
    }

    public long totalGarbageCollectionCount() {
        return garbageCollectors.stream()
                .mapToLong(GarbageCollectorSnapshot::collectionCount)
                .filter(value -> value >= 0)
                .sum();
    }

    public long totalGarbageCollectionTimeMillis() {
        return garbageCollectors.stream()
                .mapToLong(GarbageCollectorSnapshot::collectionTimeMillis)
                .filter(value -> value >= 0)
                .sum();
    }
}
