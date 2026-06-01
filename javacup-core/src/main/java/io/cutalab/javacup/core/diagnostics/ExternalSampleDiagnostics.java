package io.cutalab.javacup.core.diagnostics;

import io.cutalab.javacup.core.session.ExternalMetricSample;

import java.util.List;
import java.util.Optional;

public class ExternalSampleDiagnostics {

    private static final int MIN_SAMPLES_FOR_TREND = 4;
    private static final long MIN_GROWTH_MB = 16;
    private static final double MIN_GROWTH_PERCENTAGE = 20.0;

    public Optional<DiagnosticWarning> analyzeHeapGrowth(List<ExternalMetricSample> samples) {
        if (samples == null || samples.size() < MIN_SAMPLES_FOR_TREND) {
            return Optional.empty();
        }

        ExternalMetricSample first = firstSampleWithHeap(samples);
        ExternalMetricSample latest = latestSampleWithHeap(samples);

        if (first == null || latest == null) {
            return Optional.empty();
        }

        Long firstHeapMb = first.heapUsedMb();
        Long latestHeapMb = latest.heapUsedMb();

        if (firstHeapMb == null || latestHeapMb == null || firstHeapMb <= 0) {
            return Optional.empty();
        }

        long growthMb = latestHeapMb - firstHeapMb;

        if (growthMb < MIN_GROWTH_MB) {
            return Optional.empty();
        }

        double growthPercentage = growthMb * 100.0 / firstHeapMb;

        if (growthPercentage < MIN_GROWTH_PERCENTAGE) {
            return Optional.empty();
        }

        return Optional.of(new DiagnosticWarning(
                "HEAP_SESSION_GROWING",
                DiagnosticSeverity.WARNING,
                "Heap usage is growing during this monitoring session",
                "The selected Java process shows increasing heap usage across the samples collected in this Javacup session.",
                String.format("Heap grew from %d MB to %d MB, a growth of %d MB, approximately %.2f%%.",
                        firstHeapMb,
                        latestHeapMb,
                        growthMb,
                        growthPercentage),
                "Keep observing the process. If the trend continues after garbage collection, inspect retained collections, caches, sessions, buffers and long-lived references."
        ));
    }

    private ExternalMetricSample firstSampleWithHeap(List<ExternalMetricSample> samples) {
        return samples.stream()
                .filter(sample -> sample.heapUsedMb() != null)
                .findFirst()
                .orElse(null);
    }

    private ExternalMetricSample latestSampleWithHeap(List<ExternalMetricSample> samples) {
        for (int index = samples.size() - 1; index >= 0; index--) {
            ExternalMetricSample sample = samples.get(index);

            if (sample.heapUsedMb() != null) {
                return sample;
            }
        }

        return null;
    }
}
