package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ExternalMetricSampleSummaryService {

    public ExternalMetricSampleSummary summarize(List<ExternalMetricSample> samples) {
        if (samples == null || samples.isEmpty()) {
            return new ExternalMetricSampleSummary(0, null, null, null, null, null);
        }

        List<Long> heapValues = samples.stream()
                .map(ExternalMetricSample::heapUsedMb)
                .filter(Objects::nonNull)
                .toList();

        if (heapValues.isEmpty()) {
            return new ExternalMetricSampleSummary(samples.size(), null, null, null, null, null);
        }

        Long first = heapValues.get(0);
        Long latest = heapValues.get(heapValues.size() - 1);
        Long min = heapValues.stream().min(Long::compareTo).orElse(null);
        Long max = heapValues.stream().max(Long::compareTo).orElse(null);
        Long growth = latest - first;

        return new ExternalMetricSampleSummary(samples.size(), first, latest, min, max, growth);
    }
}
