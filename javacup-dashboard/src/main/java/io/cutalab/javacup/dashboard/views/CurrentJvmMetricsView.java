package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.metrics.CurrentJvmMetrics;
import io.cutalab.javacup.core.metrics.GarbageCollectorSnapshot;
import io.cutalab.javacup.core.metrics.MemoryUsageSnapshot;
import io.cutalab.javacup.dashboard.CurrentJvmMetricsService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Route(value = "metrics/current", layout = MainLayout.class)
public class CurrentJvmMetricsView extends VerticalLayout {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final CurrentJvmMetricsService metricsService;

    private final Span timestamp = new Span();
    private final Span uptime = new Span();

    private final Span heapUsed = new Span();
    private final Span heapCommitted = new Span();
    private final Span heapMax = new Span();
    private final Span heapUsage = new Span();

    private final Span nonHeapUsed = new Span();
    private final Span nonHeapCommitted = new Span();
    private final Span nonHeapMax = new Span();

    private final Span threadCount = new Span();
    private final Span daemonThreadCount = new Span();
    private final Span peakThreadCount = new Span();
    private final Span totalStartedThreadCount = new Span();

    private final Span loadedClassCount = new Span();
    private final Span totalLoadedClassCount = new Span();
    private final Span unloadedClassCount = new Span();

    private final Span totalGcCount = new Span();
    private final Span totalGcTime = new Span();
    private final Grid<GarbageCollectorSnapshot> garbageCollectorGrid = new Grid<>(GarbageCollectorSnapshot.class, false);

    public CurrentJvmMetricsView(CurrentJvmMetricsService metricsService) {
        this.metricsService = metricsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("padding-bottom", "var(--lumo-space-xl)");
        getStyle().set("padding-bottom", "var(--lumo-space-xl)");

        H1 title = new H1("Current JVM metrics");
        Paragraph description = new Paragraph(
                "This page reads lightweight JVM metrics from the Javacup process itself using standard MXBeans."
        );

        Button refreshButton = new Button("Refresh", event -> refresh());

        configureGarbageCollectorGrid();

        add(
                title,
                description,
                new HorizontalLayout(refreshButton),
                section("Runtime", timestamp, uptime),
                section("Heap memory", heapUsed, heapCommitted, heapMax, heapUsage),
                section("Non-heap memory", nonHeapUsed, nonHeapCommitted, nonHeapMax),
                section("Garbage collection", totalGcCount, totalGcTime),
                garbageCollectorGrid,
                section("Threads", threadCount, daemonThreadCount, peakThreadCount, totalStartedThreadCount),
                section("Class loading", loadedClassCount, totalLoadedClassCount, unloadedClassCount)
        );

        refresh();
    }

    private void configureGarbageCollectorGrid() {
        garbageCollectorGrid.addColumn(GarbageCollectorSnapshot::name)
                .setHeader("Collector")
                .setAutoWidth(true);

        garbageCollectorGrid.addColumn(this::formatCollectionCount)
                .setHeader("Collections")
                .setAutoWidth(true);

        garbageCollectorGrid.addColumn(this::formatCollectionTime)
                .setHeader("Collection time")
                .setAutoWidth(true);

        garbageCollectorGrid.setAllRowsVisible(true);
    }

    private VerticalLayout section(String title, Span... rows) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        H2 heading = new H2(title);
        layout.add(heading);
        layout.add(rows);

        return layout;
    }

    private void refresh() {
        CurrentJvmMetrics metrics = metricsService.readCurrentMetrics();

        timestamp.setText("Timestamp: " + TIMESTAMP_FORMATTER.format(metrics.timestamp()));
        uptime.setText("Uptime: " + metrics.uptimeSeconds() + " seconds");

        updateMemory("Heap", metrics.heap(), heapUsed, heapCommitted, heapMax, heapUsage);
        updateMemory("Non-heap", metrics.nonHeap(), nonHeapUsed, nonHeapCommitted, nonHeapMax, null);

        totalGcCount.setText("Total collections: " + metrics.totalGarbageCollectionCount());
        totalGcTime.setText("Total collection time: " + metrics.totalGarbageCollectionTimeMillis() + " ms");
        garbageCollectorGrid.setItems(metrics.garbageCollectors());

        threadCount.setText("Current threads: " + metrics.threadCount());
        daemonThreadCount.setText("Daemon threads: " + metrics.daemonThreadCount());
        peakThreadCount.setText("Peak threads: " + metrics.peakThreadCount());
        totalStartedThreadCount.setText("Total started threads: " + metrics.totalStartedThreadCount());

        loadedClassCount.setText("Loaded classes: " + metrics.loadedClassCount());
        totalLoadedClassCount.setText("Total loaded classes: " + metrics.totalLoadedClassCount());
        unloadedClassCount.setText("Unloaded classes: " + metrics.unloadedClassCount());
    }

    private void updateMemory(
            String label,
            MemoryUsageSnapshot memory,
            Span used,
            Span committed,
            Span max,
            Span usage
    ) {
        used.setText(label + " used: " + memory.usedMb() + " MB");
        committed.setText(label + " committed: " + memory.committedMb() + " MB");
        max.setText(label + " max: " + formatMax(memory.maxMb()));

        if (usage != null) {
            usage.setText(label + " usage: " + String.format("%.2f", memory.usedPercentage()) + "%");
        }
    }

    private String formatMax(long maxMb) {
        if (maxMb < 0) {
            return "undefined";
        }

        return maxMb + " MB";
    }

    private String formatCollectionCount(GarbageCollectorSnapshot snapshot) {
        if (!snapshot.collectionCountAvailable()) {
            return "unavailable";
        }

        return String.valueOf(snapshot.collectionCount());
    }

    private String formatCollectionTime(GarbageCollectorSnapshot snapshot) {
        if (!snapshot.collectionTimeAvailable()) {
            return "unavailable";
        }

        return snapshot.collectionTimeMillis() + " ms";
    }
}
