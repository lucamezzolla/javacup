package io.cutalab.javacup.core.metrics;

import java.time.Instant;

public record JvmMetricSample(
        Instant timestamp,
        long heapUsedBytes,
        long heapCommittedBytes,
        long heapMaxBytes,
        long nonHeapUsedBytes,
        long totalGcCount,
        long totalGcTimeMillis,
        int threadCount
) {

    public long heapUsedMb() {
        return toMb(heapUsedBytes);
    }

    public long heapCommittedMb() {
        return toMb(heapCommittedBytes);
    }

    public long heapMaxMb() {
        return heapMaxBytes < 0 ? -1 : toMb(heapMaxBytes);
    }

    public long nonHeapUsedMb() {
        return toMb(nonHeapUsedBytes);
    }

    public double heapUsagePercentage() {
        if (heapMaxBytes <= 0) {
            return 0.0;
        }

        return (heapUsedBytes * 100.0) / heapMaxBytes;
    }

    private long toMb(long bytes) {
        return bytes / 1024 / 1024;
    }
}
