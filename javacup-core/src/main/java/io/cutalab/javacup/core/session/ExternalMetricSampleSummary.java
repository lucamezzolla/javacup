package io.cutalab.javacup.core.session;

public record ExternalMetricSampleSummary(
        int sampleCount,
        Long firstHeapUsedMb,
        Long latestHeapUsedMb,
        Long minHeapUsedMb,
        Long maxHeapUsedMb,
        Long growthMb
) {

    public boolean hasHeapData() {
        return firstHeapUsedMb != null
                && latestHeapUsedMb != null
                && minHeapUsedMb != null
                && maxHeapUsedMb != null;
    }
}
