package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class ExternalMetricSampleSummaryService {

    public ExternalMetricSampleSummary summarize(List<ExternalMetricSample> samples) {
        if (samples == null || samples.isEmpty()) {
            return emptySummary(0);
        }

        MetricSummary heapSummary = summarizeMetric(samples, ExternalMetricSample::heapUsedMb);
        MetricSummary metaspaceSummary = summarizeMetric(samples, ExternalMetricSample::metaspaceUsedMb);

        return new ExternalMetricSampleSummary(
                samples.size(),
                heapSummary.first(),
                heapSummary.latest(),
                heapSummary.min(),
                heapSummary.max(),
                heapSummary.growth(),
                metaspaceSummary.first(),
                metaspaceSummary.latest(),
                metaspaceSummary.min(),
                metaspaceSummary.max(),
                metaspaceSummary.growth()
        );
    }

    private ExternalMetricSampleSummary emptySummary(int sampleCount) {
        return new ExternalMetricSampleSummary(
                sampleCount,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private MetricSummary summarizeMetric(
            List<ExternalMetricSample> samples,
            Function<ExternalMetricSample, Long> valueExtractor
    ) {
        List<Long> values = samples.stream()
                .map(valueExtractor)
                .filter(Objects::nonNull)
                .toList();

        if (values.isEmpty()) {
            return new MetricSummary(null, null, null, null, null);
        }

        Long first = values.getFirst();
        Long latest = values.getLast();
        Long min = values.stream().min(Long::compareTo).orElse(null);
        Long max = values.stream().max(Long::compareTo).orElse(null);
        Long growth = latest - first;

        return new MetricSummary(first, latest, min, max, growth);
    }

    private record MetricSummary(
            Long first,
            Long latest,
            Long min,
            Long max,
            Long growth
    ) {
    }
}
