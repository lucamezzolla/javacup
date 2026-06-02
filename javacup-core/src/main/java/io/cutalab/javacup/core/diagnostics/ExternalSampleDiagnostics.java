package io.cutalab.javacup.core.diagnostics;

import io.cutalab.javacup.core.session.ExternalMetricSample;

import java.util.List;
import java.util.Optional;

public class ExternalSampleDiagnostics {

    private static final int MIN_SAMPLES_FOR_TREND = 4;

    private static final long MIN_HEAP_GROWTH_MB = 16;
    private static final double MIN_HEAP_GROWTH_PERCENTAGE = 20.0;

    private static final long MIN_METASPACE_GROWTH_MB = 8;
    private static final double MIN_METASPACE_GROWTH_PERCENTAGE = 20.0;

    public Optional<DiagnosticWarning> analyzeInsufficientSamples(List<ExternalMetricSample> samples) {
        int sampleCount = samples == null ? 0 : samples.size();

        if (sampleCount >= MIN_SAMPLES_FOR_TREND) {
            return Optional.empty();
        }

        return Optional.of(new DiagnosticWarning(
                "INSUFFICIENT_SAMPLES_FOR_TREND",
                DiagnosticSeverity.INFO,
                "Not enough samples for reliable trend diagnostics",
                "Javacup needs more samples before heap and Metaspace trend diagnostics can be considered reliable.",
                String.format("Collected %d sample(s). Trend diagnostics require at least %d samples.",
                        sampleCount,
                        MIN_SAMPLES_FOR_TREND),
                "Keep the monitoring session running a little longer, then collect more samples before interpreting growth diagnostics."
        ));
    }

    public Optional<DiagnosticWarning> analyzeHeapGrowth(List<ExternalMetricSample> samples) {
        TrendValues trend = trendValues(samples, MetricKind.HEAP);

        if (!trend.enoughSamples()) {
            return Optional.empty();
        }

        if (trend.growthMb() < MIN_HEAP_GROWTH_MB) {
            return Optional.empty();
        }

        if (trend.growthPercentage() < MIN_HEAP_GROWTH_PERCENTAGE) {
            return Optional.empty();
        }

        return Optional.of(new DiagnosticWarning(
                "HEAP_SESSION_GROWING",
                DiagnosticSeverity.WARNING,
                "Heap usage is growing during this monitoring session",
                "The selected Java process shows increasing heap usage across the samples collected in this Javacup session.",
                String.format("Heap grew from %d MB to %d MB, a growth of %d MB, approximately %.2f%%.",
                        trend.firstMb(),
                        trend.latestMb(),
                        trend.growthMb(),
                        trend.growthPercentage()),
                "Keep observing the process. If the trend continues after garbage collection, inspect retained collections, caches, sessions, buffers and long-lived references."
        ));
    }

    public Optional<DiagnosticWarning> analyzeMetaspaceGrowth(List<ExternalMetricSample> samples) {
        TrendValues trend = trendValues(samples, MetricKind.METASPACE);

        if (!trend.enoughSamples()) {
            return Optional.empty();
        }

        if (trend.growthMb() < MIN_METASPACE_GROWTH_MB) {
            return Optional.empty();
        }

        if (trend.growthPercentage() < MIN_METASPACE_GROWTH_PERCENTAGE) {
            return Optional.empty();
        }

        return Optional.of(new DiagnosticWarning(
                "METASPACE_SESSION_GROWING",
                DiagnosticSeverity.WARNING,
                "Metaspace usage is growing during this monitoring session",
                "The selected Java process shows increasing Metaspace usage across the samples collected in this Javacup session.",
                String.format("Metaspace grew from %d MB to %d MB, a growth of %d MB, approximately %.2f%%.",
                        trend.firstMb(),
                        trend.latestMb(),
                        trend.growthMb(),
                        trend.growthPercentage()),
                "If this trend continues, inspect class loading behavior, dynamic proxies, generated classes, hot redeploy cycles and custom class loaders."
        ));
    }

    private TrendValues trendValues(List<ExternalMetricSample> samples, MetricKind metricKind) {
        if (samples == null || samples.size() < MIN_SAMPLES_FOR_TREND) {
            return TrendValues.notEnoughSamples();
        }

        ExternalMetricSample first = firstSampleWithMetric(samples, metricKind);
        ExternalMetricSample latest = latestSampleWithMetric(samples, metricKind);

        if (first == null || latest == null) {
            return TrendValues.notEnoughSamples();
        }

        Long firstMb = metricValue(first, metricKind);
        Long latestMb = metricValue(latest, metricKind);

        if (firstMb == null || latestMb == null || firstMb <= 0) {
            return TrendValues.notEnoughSamples();
        }

        long growthMb = latestMb - firstMb;
        double growthPercentage = growthMb * 100.0 / firstMb;

        return new TrendValues(true, firstMb, latestMb, growthMb, growthPercentage);
    }

    private ExternalMetricSample firstSampleWithMetric(List<ExternalMetricSample> samples, MetricKind metricKind) {
        return samples.stream()
                .filter(sample -> metricValue(sample, metricKind) != null)
                .findFirst()
                .orElse(null);
    }

    private ExternalMetricSample latestSampleWithMetric(List<ExternalMetricSample> samples, MetricKind metricKind) {
        for (int index = samples.size() - 1; index >= 0; index--) {
            ExternalMetricSample sample = samples.get(index);

            if (metricValue(sample, metricKind) != null) {
                return sample;
            }
        }

        return null;
    }

    private Long metricValue(ExternalMetricSample sample, MetricKind metricKind) {
        return switch (metricKind) {
            case HEAP -> sample.heapUsedMb();
            case METASPACE -> sample.metaspaceUsedMb();
        };
    }

    private enum MetricKind {
        HEAP,
        METASPACE
    }

    private record TrendValues(
            boolean enoughSamples,
            long firstMb,
            long latestMb,
            long growthMb,
            double growthPercentage
    ) {

        private static TrendValues notEnoughSamples() {
            return new TrendValues(false, 0, 0, 0, 0.0);
        }
    }
}
