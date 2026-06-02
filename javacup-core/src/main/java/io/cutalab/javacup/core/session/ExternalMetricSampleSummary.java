package io.cutalab.javacup.core.session;

public record ExternalMetricSampleSummary(
        int sampleCount,
        Long firstHeapUsedMb,
        Long latestHeapUsedMb,
        Long minHeapUsedMb,
        Long maxHeapUsedMb,
        Long heapGrowthMb,
        Long firstMetaspaceUsedMb,
        Long latestMetaspaceUsedMb,
        Long minMetaspaceUsedMb,
        Long maxMetaspaceUsedMb,
        Long metaspaceGrowthMb
) {

    public boolean hasHeapData() {
        return firstHeapUsedMb != null
                && latestHeapUsedMb != null
                && minHeapUsedMb != null
                && maxHeapUsedMb != null;
    }

    public boolean hasMetaspaceData() {
        return firstMetaspaceUsedMb != null
                && latestMetaspaceUsedMb != null
                && minMetaspaceUsedMb != null
                && maxMetaspaceUsedMb != null;
    }

    public Long growthMb() {
        return heapGrowthMb;
    }
}
