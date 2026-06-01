package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.metrics.JvmMetricSample;
import io.cutalab.javacup.dashboard.JvmMetricSampleService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Route(value = "metrics/samples", layout = MainLayout.class)
public class MetricSamplesView extends VerticalLayout {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final JvmMetricSampleService sampleService;
    private final Grid<JvmMetricSample> grid = new Grid<>(JvmMetricSample.class, false);

    public MetricSamplesView(JvmMetricSampleService sampleService) {
        this.sampleService = sampleService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("padding-bottom", "var(--lumo-space-xl)");
        getStyle().set("padding-bottom", "var(--lumo-space-xl)");

        H1 title = new H1("Metric samples");
        Paragraph description = new Paragraph(
                "This page shows recent lightweight samples collected every 2 seconds from the Javacup JVM. Data is currently kept in memory only."
        );

        Button refreshButton = new Button("Refresh", event -> refresh());

        configureGrid();

        add(title, description, new HorizontalLayout(refreshButton), grid);
        expand(grid);

        refresh();
    }

    private void configureGrid() {
        grid.addColumn(sample -> TIMESTAMP_FORMATTER.format(sample.timestamp()))
                .setHeader("Time")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(JvmMetricSample::heapUsedMb)
                .setHeader("Heap used MB")
                .setAutoWidth(true);

        grid.addColumn(JvmMetricSample::heapCommittedMb)
                .setHeader("Heap committed MB")
                .setAutoWidth(true);

        grid.addColumn(sample -> formatMax(sample.heapMaxMb()))
                .setHeader("Heap max MB")
                .setAutoWidth(true);

        grid.addColumn(sample -> String.format("%.2f%%", sample.heapUsagePercentage()))
                .setHeader("Heap usage")
                .setAutoWidth(true);

        grid.addColumn(JvmMetricSample::nonHeapUsedMb)
                .setHeader("Non-heap used MB")
                .setAutoWidth(true);

        grid.addColumn(JvmMetricSample::totalGcCount)
                .setHeader("GC count")
                .setAutoWidth(true);

        grid.addColumn(JvmMetricSample::totalGcTimeMillis)
                .setHeader("GC time ms")
                .setAutoWidth(true);

        grid.addColumn(JvmMetricSample::threadCount)
                .setHeader("Threads")
                .setAutoWidth(true);
    }

    private void refresh() {
        grid.setItems(sampleService.findRecentSamples());
    }

    private String formatMax(long value) {
        if (value < 0) {
            return "undefined";
        }

        return String.valueOf(value);
    }
}
