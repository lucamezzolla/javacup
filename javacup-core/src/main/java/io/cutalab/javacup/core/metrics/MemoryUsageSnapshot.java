package io.cutalab.javacup.core.metrics;

public record MemoryUsageSnapshot(
        long usedBytes,
        long committedBytes,
        long maxBytes
) {

    public long usedMb() {
        return toMb(usedBytes);
    }

    public long committedMb() {
        return toMb(committedBytes);
    }

    public long maxMb() {
        return maxBytes < 0 ? -1 : toMb(maxBytes);
    }

    public double usedPercentage() {
        if (maxBytes <= 0) {
            return 0.0;
        }

        return (usedBytes * 100.0) / maxBytes;
    }

    private long toMb(long bytes) {
        return bytes / 1024 / 1024;
    }
}
