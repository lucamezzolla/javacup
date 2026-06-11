package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.metrics.ExternalVmUptime;
import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.core.report.ExternalMonitoringReport;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import io.cutalab.javacup.core.session.MonitoringSession;
import io.cutalab.javacup.dashboard.ExternalHeapDiagnosticsService;
import io.cutalab.javacup.dashboard.ExternalHeapInfoService;
import io.cutalab.javacup.dashboard.ExternalMetricSampleService;
import io.cutalab.javacup.dashboard.ExternalMetricSampleSummaryService;
import io.cutalab.javacup.dashboard.ExternalMonitoringReportService;
import io.cutalab.javacup.dashboard.LocalReportArchiveService;
import io.cutalab.javacup.dashboard.ExternalProcessProbeService;
import io.cutalab.javacup.dashboard.ExternalVmUptimeService;
import io.cutalab.javacup.dashboard.ExternalSampleDiagnosticsService;
import io.cutalab.javacup.dashboard.LocalJavaProcessService;
import io.cutalab.javacup.dashboard.MonitoringSessionService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

@Route(value = "metrics/external", layout = MainLayout.class)
public class ExternalProcessMetricsView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final int AUTO_REFRESH_INTERVAL_MS = 5_000;
    private static final DateTimeFormatter REFRESH_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SESSION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter SAMPLE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final LocalJavaProcessService processService;
    private final ExternalProcessProbeService probeService;
    private final ExternalVmUptimeService uptimeService;
    private final ExternalHeapInfoService heapInfoService;
    private final ExternalHeapDiagnosticsService diagnosticsService;
    private final ExternalSampleDiagnosticsService sampleDiagnosticsService;
    private final MonitoringSessionService monitoringSessionService;
    private final ExternalMetricSampleService sampleService;
    private final ExternalMetricSampleSummaryService sampleSummaryService;
    private final ExternalMonitoringReportService reportService;
    private final LocalReportArchiveService reportArchiveService;

    private final ObjectMapper reportObjectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final H1 title = new H1("External process metrics");
    private final Span pid = new Span();
    private final Span application = new Span();
    private final Span type = new Span();
    private final Span vmUptime = new Span("VM uptime: unavailable");
    private final Span vmUptimeSeconds = new Span("VM uptime seconds: unavailable");
    private final Span processAvailability = new Span("Process availability: unknown");
    private final Span probeAvailability = new Span("Probe availability: unknown");
    private final Span probeHint = new Span("Probe hint: waiting for first refresh");
    private final Span sessionHealthVerdict = new Span("Session health: waiting for first refresh");
    private final Span sessionHealthProbe = new Span("Probe: waiting for first refresh");
    private final Span sessionHealthSamples = new Span("Samples: waiting for first refresh");
    private final Span sessionHealthMainIssue = new Span("Main issue: waiting for first refresh");
    private final Span sessionHealthAction = new Span("Recommended action: waiting for first refresh");

    private final Span autoRefreshStatus = new Span("Auto-refresh: waiting for page attach");
    private final Span lastRefresh = new Span("Last refresh: never");

    private final Span sessionId = new Span("Session ID: unavailable");
    private final Span sessionStatus = new Span("Status: unavailable");
    private final Span sessionStartedAt = new Span("Started at: unavailable");
    private final Span sessionLastUpdatedAt = new Span("Last updated at: unavailable");

    private final Span heapType = new Span();
    private final Span heapUsed = new Span();
    private final Span heapTotal = new Span();
    private final Span heapReserved = new Span();

    private final Span metaspaceUsed = new Span();
    private final Span metaspaceCommitted = new Span();
    private final Span metaspaceReserved = new Span();

    private final Span classSpaceUsed = new Span();
    private final Span classSpaceCommitted = new Span();
    private final Span classSpaceReserved = new Span();

    private final Grid<DiagnosticWarning> diagnosticsGrid = new Grid<>(DiagnosticWarning.class, false);

    private final Span samplesRetained = new Span("Samples retained: 0 / 100");
    private final Span sampleCount = new Span("Samples collected: 0");
    private final Span firstHeapUsed = new Span("First heap used: unavailable");
    private final Span latestHeapUsed = new Span("Latest heap used: unavailable");
    private final Span minHeapUsed = new Span("Min heap used: unavailable");
    private final Span maxHeapUsed = new Span("Max heap used: unavailable");
    private final Span heapGrowth = new Span("Heap growth: unavailable");
    private final Span firstMetaspaceUsed = new Span("First Metaspace used: unavailable");
    private final Span latestMetaspaceUsed = new Span("Latest Metaspace used: unavailable");
    private final Span minMetaspaceUsedSummary = new Span("Min Metaspace used: unavailable");
    private final Span maxMetaspaceUsedSummary = new Span("Max Metaspace used: unavailable");
    private final Span metaspaceGrowth = new Span("Metaspace growth: unavailable");
    private final Div heapTrendChart = new Div();
    private final Div metaspaceTrendChart = new Div();
    private final Grid<ExternalMetricSample> samplesGrid = new Grid<>(ExternalMetricSample.class, false);

    private final Div rawHeapInfo = new Div();
    private final Div uptimeInfo = new Div();
    private final Div reportPreview = new Div();
    private final Dialog reportPreviewDialog = new Dialog();
    private final Div reportPreviewDialogContent = new Div();
    private final Anchor downloadReportLink = new Anchor();

    private Long selectedPid;
    private MonitoringSession currentSession;
    private ExternalHeapInfo latestHeapInfo;
    private ExternalVmUptime latestVmUptime;
    private ExternalMetricSampleSummary latestSampleSummary;
    private List<DiagnosticWarning> latestDiagnostics = List.of();
    private List<ExternalMetricSample> latestSamples = List.of();
    private boolean sessionStopped;
    private Registration pollRegistration;

    public ExternalProcessMetricsView(
            LocalJavaProcessService processService,
            ExternalProcessProbeService probeService,
            ExternalVmUptimeService uptimeService,
            ExternalHeapInfoService heapInfoService,
            ExternalHeapDiagnosticsService diagnosticsService,
            ExternalSampleDiagnosticsService sampleDiagnosticsService,
            MonitoringSessionService monitoringSessionService,
            ExternalMetricSampleService sampleService,
            ExternalMetricSampleSummaryService sampleSummaryService,
            ExternalMonitoringReportService reportService,
            LocalReportArchiveService reportArchiveService) {
        this.processService = processService;
        this.probeService = probeService;
        this.uptimeService = uptimeService;
        this.heapInfoService = heapInfoService;
        this.diagnosticsService = diagnosticsService;
        this.sampleDiagnosticsService = sampleDiagnosticsService;
        this.monitoringSessionService = monitoringSessionService;
        this.sampleService = sampleService;
        this.sampleSummaryService = sampleSummaryService;
        this.reportService = reportService;
        this.reportArchiveService = reportArchiveService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backButton = new Button("Back to processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button refreshButton = new Button("Refresh metrics", event -> refreshMetrics(true));
        Button stopButton = new Button("Stop session", event -> stopSession());
        Button previewReportButton = new Button("Preview report", event -> previewReport());
        Button archiveReportButton = new Button("Archive JSON report", event -> archiveReport());

        configureDiagnosticsGrid();
        configureSamplesGrid();
        configureReportPreviewDialog();
        styleHeapTrendChart();
        styleMetaspaceTrendChart();

        styleTechnicalBlock(rawHeapInfo);
        styleTechnicalBlock(uptimeInfo);
        styleTechnicalBlock(reportPreview);

        StreamResource reportResource = new StreamResource("javacup-external-report.json", this::openJsonReportStream);
        reportResource.setContentType("application/json");

        downloadReportLink.setText("Download JSON report");
        downloadReportLink.setHref(reportResource);
        downloadReportLink.getElement().setAttribute("download", true);
        downloadReportLink.setVisible(true);
        downloadReportLink.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("text-decoration", "none")
                .set("font-weight", "600");

        add(
                title,
                new Paragraph("This page reads external JVM information from a selected Java process using local JDK diagnostic commands."),
                new HorizontalLayout(backButton, refreshButton, stopButton, previewReportButton, archiveReportButton, downloadReportLink),
                section("Selected process", pid, application, type, autoRefreshStatus, lastRefresh),
                section("Monitoring session", sessionId, sessionStatus, sessionStartedAt, sessionLastUpdatedAt),
                section("Probe status", processAvailability, probeAvailability, probeHint),
                section("Session health",
                        new Paragraph("Operational summary for the selected monitoring session."),
                        sessionHealthVerdict,
                        sessionHealthProbe,
                        sessionHealthSamples,
                        sessionHealthMainIssue,
                        sessionHealthAction),
                section("Structured VM uptime", vmUptime, vmUptimeSeconds),
                section("Structured heap summary", heapType, heapUsed, heapTotal, heapReserved),
                section("Structured metaspace summary", metaspaceUsed, metaspaceCommitted, metaspaceReserved),
                section("Structured compressed class space summary", classSpaceUsed, classSpaceCommitted, classSpaceReserved),
                section("Diagnostics", new Paragraph("Rules include probe status, heap pressure, sample quality, heap growth and Metaspace growth diagnostics."), diagnosticsGrid),
                section("Heap trend summary", sampleCount, firstHeapUsed, latestHeapUsed, minHeapUsed, maxHeapUsed, heapGrowth),
                section("Metaspace trend summary", firstMetaspaceUsed, latestMetaspaceUsed, minMetaspaceUsedSummary, maxMetaspaceUsedSummary, metaspaceGrowth),
                section("Heap usage trend", new Paragraph("Lightweight chart based on the latest retained external samples."), heapTrendChart),
                section("Metaspace usage trend", new Paragraph("Lightweight chart based on the latest retained external samples."), metaspaceTrendChart),
                section("Recent external samples", new Paragraph("In-memory samples collected while this page is open. Oldest samples are discarded when the session buffer is full."), samplesRetained, samplesGrid),
                section("Raw heap information", new Paragraph("Source: jcmd <pid> GC.heap_info"), rawHeapInfo),
                section("VM uptime", new Paragraph("Source: jcmd <pid> VM.uptime"), uptimeInfo),
                bottomSpacer()
        );
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        attachEvent.getUI().setPollInterval(AUTO_REFRESH_INTERVAL_MS);

        if (pollRegistration != null) {
            pollRegistration.remove();
        }

        pollRegistration = attachEvent.getUI().addPollListener(event -> refreshMetrics(false));
        autoRefreshStatus.setText("Auto-refresh: every 5 seconds");

        refreshMetrics(false);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (pollRegistration != null) {
            pollRegistration.remove();
            pollRegistration = null;
        }

        detachEvent.getUI().setPollInterval(-1);
        super.onDetach(detachEvent);
    }

    @Override
    public void setParameter(BeforeEvent event, Long processId) {
        selectedPid = processId;
        sessionStopped = false;
        showProcess(processId);
    }


    private void configureReportPreviewDialog() {
        reportPreviewDialog.setHeaderTitle("External monitoring report preview");
        reportPreviewDialog.setWidth("900px");
        reportPreviewDialog.setMaxWidth("95vw");

        reportPreviewDialogContent.setWidthFull();
        reportPreviewDialogContent.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere")
                .set("word-break", "break-word")
                .set("max-height", "70vh")
                .set("overflow", "auto")
                .set("padding", "var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("font-family", "monospace");

        Button closeButton = new Button("Close", event -> reportPreviewDialog.close());

        reportPreviewDialog.add(reportPreviewDialogContent);
        reportPreviewDialog.getFooter().add(closeButton);
    }

    private void configureDiagnosticsGrid() {
        diagnosticsGrid.addColumn(DiagnosticWarning::code).setHeader("Code").setAutoWidth(true).setFlexGrow(0);
        diagnosticsGrid.addColumn(DiagnosticWarning::severity).setHeader("Severity").setAutoWidth(true).setFlexGrow(0);
        diagnosticsGrid.addColumn(DiagnosticWarning::title).setHeader("Title").setAutoWidth(true).setFlexGrow(0);
        diagnosticsGrid.addColumn(DiagnosticWarning::evidence).setHeader("Evidence").setFlexGrow(1);
        diagnosticsGrid.addColumn(DiagnosticWarning::recommendation).setHeader("Recommendation").setFlexGrow(1);
        diagnosticsGrid.setAllRowsVisible(true);
    }

    private void configureSamplesGrid() {
        samplesGrid.addColumn(sample -> SAMPLE_TIME_FORMATTER.format(sample.timestamp()))
                .setHeader("Time")
                .setAutoWidth(true)
                .setFlexGrow(0);

        samplesGrid.addColumn(sample -> formatNullableMb(sample.heapUsedMb()))
                .setHeader("Heap used")
                .setAutoWidth(true);

        samplesGrid.addColumn(sample -> formatNullableMb(sample.heapTotalMb()))
                .setHeader("Heap total")
                .setAutoWidth(true);

        samplesGrid.addColumn(sample -> formatNullableMb(sample.metaspaceUsedMb()))
                .setHeader("Metaspace used")
                .setAutoWidth(true);

        samplesGrid.addColumn(sample -> formatNullableMb(sample.classSpaceUsedMb()))
                .setHeader("Class space used")
                .setAutoWidth(true);

        samplesGrid.setAllRowsVisible(false);
        samplesGrid.setHeight("260px");
        samplesGrid.setWidthFull();
    }

    private void showProcess(Long processId) {
        Optional<JavaProcessInfo> processInfo = processService.findJavaProcessByPid(processId);

        title.setText("External metrics for process " + processId);
        pid.setText("PID: " + processId);

        if (processInfo.isPresent()) {
            JavaProcessInfo process = processInfo.get();
            application.setText("Application: " + process.applicationName());
            type.setText("Type: " + process.processType());
        } else {
            application.setText("Application: unavailable from ProcessHandle");
            type.setText("Type: unknown, jcmd probe will still be attempted");
        }

        currentSession = monitoringSessionService.startOrResume(processId);
        updateSessionInfo(currentSession);
        refreshMetrics(false);
    }

    private void refreshMetrics(boolean showNotification) {
        if (selectedPid == null) {
            if (showNotification) {
                Notification.show("No process selected.");
            }
            return;
        }

        if (sessionStopped) {
            if (showNotification) {
                Notification.show("Monitoring session is stopped.");
            }
            return;
        }

        currentSession = monitoringSessionService.refresh(selectedPid);
        updateSessionInfo(currentSession);

        latestHeapInfo = heapInfoService.readHeapInfo(selectedPid);
        latestVmUptime = uptimeService.readUptime(selectedPid);

        updateStructuredHeapInfo(latestHeapInfo);
        updateProbeStatus();
        updateSessionHealthSummary();

        if (latestHeapInfo.hasStructuredValues()) {
            sampleService.addSample(currentSession, latestHeapInfo);
        }

        latestSamples = sampleService.findSamples(currentSession.id());

        List<DiagnosticWarning> warnings = new ArrayList<>(diagnosticsService.analyze(latestHeapInfo));
        warnings.addAll(sampleDiagnosticsService.analyze(latestSamples));
        latestDiagnostics = List.copyOf(warnings);
        diagnosticsGrid.setItems(warnings);

        latestSampleSummary = sampleSummaryService.summarize(latestSamples);
        updateSampleSummary(latestSampleSummary);
        renderHeapTrendChart(latestSamples);
        renderMetaspaceTrendChart(latestSamples);
        samplesRetained.setText("Samples retained: " + latestSamples.size() + " / " + sampleService.maxSamplesPerSession());
        samplesGrid.setItems(latestSamples.reversed());

        rawHeapInfo.setText(latestHeapInfo.rawOutput());
        updateStructuredVmUptime(latestVmUptime);
        uptimeInfo.setText(latestVmUptime.rawOutput());
        lastRefresh.setText("Last refresh: " + LocalDateTime.now().format(REFRESH_TIME_FORMATTER));

        if (showNotification) {
            if (latestHeapInfo.hasStructuredValues()) {
                Notification.show("External metrics refreshed.");
            } else {
                Notification.show("External heap info refreshed, but structured parsing is incomplete.");
            }
        }
    }


    private ByteArrayInputStream openJsonReportStream() {
        ExternalMonitoringReport report = createCurrentReport();

        try {
            String json;

            if (report == null) {
                json = "{\\n  \\\"error\\\" : \\\"No report data available yet\\\"\\n}\\n";
            } else {
                json = reportObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
            }

            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException exception) {
            String safeMessage = exception.getMessage() == null ? "unknown" : exception.getMessage().replace("\\\"", "\\\\\\\"");
            String json = "{\\n  \\\"error\\\" : \\\"" + safeMessage + "\\\"\\n}\\n";
            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private ExternalMonitoringReport createCurrentReport() {
        if (currentSession == null || latestHeapInfo == null || latestSampleSummary == null) {
            return null;
        }

        return reportService.createReport(
                currentSession,
                latestHeapInfo,
                latestVmUptime,
                latestSampleSummary,
                latestDiagnostics,
                latestSamples
        );
    }


    private void archiveReport() {
        ExternalMonitoringReport report = createCurrentReport();

        if (report == null) {
            Notification.show("No report data available yet.");
            return;
        }

        try {
            java.nio.file.Path archivedPath = reportArchiveService.archive(report);
            Notification.show("Report archived locally: " + archivedPath);
        } catch (IllegalStateException exception) {
            Notification.show("Unable to archive report: " + exception.getMessage());
        }
    }
    private void previewReport() {
        ExternalMonitoringReport report = createCurrentReport();

        if (report == null) {
            Notification.show("No report data available yet.");
            return;
        }

        String preview = reportService.createReadablePreview(report);
        reportPreview.setText(preview);
        reportPreviewDialogContent.setText(preview);
        reportPreviewDialog.open();
    }

    private void stopSession() {
        if (selectedPid == null) {
            Notification.show("No process selected.");
            return;
        }

        sessionStopped = true;
        currentSession = monitoringSessionService.stop(selectedPid);
        updateSessionInfo(currentSession);
        Notification.show("Monitoring session stopped.");
    }

    private void updateSessionInfo(MonitoringSession session) {
        sessionId.setText("Session ID: " + session.id());
        sessionStatus.setText("Status: " + session.status());
        sessionStartedAt.setText("Started at: " + SESSION_TIME_FORMATTER.format(session.startedAt()));
        sessionLastUpdatedAt.setText("Last updated at: " + SESSION_TIME_FORMATTER.format(session.lastUpdatedAt()));
    }


    private void renderHeapTrendChart(List<ExternalMetricSample> samples) {
        heapTrendChart.removeAll();

        List<ExternalMetricSample> visibleSamples = samples.stream()
                .filter(sample -> sample.heapUsedMb() != null)
                .skip(Math.max(0, samples.size() - 40))
                .toList();

        if (visibleSamples.isEmpty()) {
            heapTrendChart.setText("No heap samples available yet.");
            return;
        }

        long minHeapUsed = visibleSamples.stream()
                .map(ExternalMetricSample::heapUsedMb)
                .filter(value -> value != null)
                .min(Long::compareTo)
                .orElse(0L);

        long maxHeapUsed = visibleSamples.stream()
                .map(ExternalMetricSample::heapUsedMb)
                .filter(value -> value != null)
                .max(Long::compareTo)
                .orElse(1L);

        long latestHeapUsed = visibleSamples.get(visibleSamples.size() - 1).heapUsedMb();

        Span yAxisLabel = new Span("Y: Heap used (MB)");
        yAxisLabel.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("margin-bottom", "var(--lumo-space-xs)");

        HorizontalLayout chartRow = new HorizontalLayout();
        chartRow.setPadding(false);
        chartRow.setSpacing(false);
        chartRow.setWidthFull();
        chartRow.setAlignItems(Alignment.STRETCH);
        chartRow.getStyle().set("gap", "var(--lumo-space-s)");

        VerticalLayout yScale = new VerticalLayout();
        yScale.setPadding(false);
        yScale.setSpacing(false);
        yScale.setWidth("80px");
        yScale.setHeight("140px");
        yScale.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        Span maxLabel = new Span(maxHeapUsed + " MB");
        Span middleLabel = new Span(((maxHeapUsed + minHeapUsed) / 2) + " MB");
        Span minLabel = new Span(minHeapUsed + " MB");

        yScale.add(maxLabel, new Span(""), middleLabel, new Span(""), minLabel);
        yScale.expand(yScale.getComponentAt(1), yScale.getComponentAt(3));

        HorizontalLayout bars = new HorizontalLayout();
        bars.setPadding(false);
        bars.setSpacing(false);
        bars.setWidthFull();
        bars.setHeight("140px");
        bars.setAlignItems(Alignment.END);
        bars.getStyle()
                .set("gap", "3px")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-contrast-5pct)");

        long visibleRange = Math.max(1L, maxHeapUsed - minHeapUsed);

        for (ExternalMetricSample sample : visibleSamples) {
            long heapUsed = sample.heapUsedMb();
            int heightPercentage = maxHeapUsed == minHeapUsed
                    ? 50
                    : Math.max(4, (int) Math.round((heapUsed - minHeapUsed) * 100.0 / visibleRange));

            Div bar = new Div();
            bar.getStyle()
                    .set("height", heightPercentage + "%")
                    .set("min-width", "6px")
                    .set("flex", "1")
                    .set("border-radius", "var(--lumo-border-radius-s)")
                    .set("background", "var(--lumo-primary-color-50pct)");

            bar.getElement().setAttribute("title", heapUsed + " MB at " + SAMPLE_TIME_FORMATTER.format(sample.timestamp()));

            bars.add(bar);
        }

        chartRow.add(yScale, bars);
        chartRow.expand(bars);

        Span xAxisLabel = new Span("X: recent samples, oldest → newest");
        xAxisLabel.getStyle()
                .set("display", "block")
                .set("margin-top", "var(--lumo-space-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        Span caption = new Span(
                "Showing latest " + visibleSamples.size()
                        + " samples. Min: " + minHeapUsed
                        + " MB, Max: " + maxHeapUsed
                        + " MB, Latest: " + latestHeapUsed
                        + " MB."
        );
        caption.getStyle()
                .set("display", "block")
                .set("margin-top", "var(--lumo-space-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        heapTrendChart.add(yAxisLabel, chartRow, xAxisLabel, caption);
    }


    private void styleHeapTrendChart() {
        heapTrendChart.setWidthFull();
        heapTrendChart.getStyle()
                .set("max-width", "100%")
                .set("padding-bottom", "var(--lumo-space-s)");
    }




    private void updateSessionHealthSummary() {
        List<DiagnosticWarning> diagnostics = latestDiagnostics == null ? List.of() : latestDiagnostics;

        String verdict = sessionHealthVerdict(diagnostics);
        String probeSummary = sessionHealthProbeSummary();
        String sampleSummary = sessionHealthSampleSummary(diagnostics);
        DiagnosticWarning mainDiagnostic = mainDiagnostic(diagnostics);

        sessionHealthVerdict.setText("Session health: " + verdict);
        sessionHealthProbe.setText("Probe: " + probeSummary);
        sessionHealthSamples.setText("Samples: " + sampleSummary);
        sessionHealthMainIssue.setText("Main issue: " + (mainDiagnostic == null ? "none" : mainDiagnostic.code()));
        sessionHealthAction.setText("Recommended action: " + recommendedSessionAction(mainDiagnostic));
    }

    private String sessionHealthVerdict(List<DiagnosticWarning> diagnostics) {
        if (diagnostics.isEmpty()) {
            return "OK";
        }

        if (hasSeverity(diagnostics, "CRITICAL")) {
            return "Needs attention";
        }

        if (hasSeverity(diagnostics, "WARNING")) {
            return "Review recommended";
        }

        return "Informational";
    }

    private boolean hasSeverity(List<DiagnosticWarning> diagnostics, String severityName) {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() != null)
                .anyMatch(diagnostic -> severityName.equals(diagnostic.severity().name()));
    }

    private String sessionHealthProbeSummary() {
        boolean heapOk = latestHeapInfo != null && isProbeOk(latestHeapInfo.probeStatus(), latestHeapInfo.probeFailureKind());
        boolean uptimeOk = latestVmUptime != null && isProbeOk(latestVmUptime.probeStatus(), latestVmUptime.probeFailureKind());

        if (heapOk && uptimeOk) {
            return "OK";
        }

        if (latestHeapInfo == null && latestVmUptime == null) {
            return "waiting for probe data";
        }

        return "issue detected";
    }

    private boolean isProbeOk(String probeStatus, String failureKind) {
        String status = probeStatus == null ? "" : probeStatus;
        String failure = failureKind == null ? "" : failureKind;

        return "OK".equals(status) && ("NONE".equals(failure) || failure.isBlank());
    }

    private String sessionHealthSampleSummary(List<DiagnosticWarning> diagnostics) {
        if (hasDiagnosticCode(diagnostics, "INSUFFICIENT_SAMPLES_FOR_TREND")) {
            return "insufficient for trend diagnostics";
        }

        if (hasDiagnosticCode(diagnostics, "PARTIAL_SAMPLE_DATA")) {
            return "partial sample data";
        }

        if (latestSampleSummary == null) {
            return "unavailable";
        }

        return latestSampleSummary.sampleCount() + " collected";
    }

    private boolean hasDiagnosticCode(List<DiagnosticWarning> diagnostics, String code) {
        return diagnostics.stream()
                .anyMatch(diagnostic -> code.equals(diagnostic.code()));
    }

    private DiagnosticWarning mainDiagnostic(List<DiagnosticWarning> diagnostics) {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() != null)
                .filter(diagnostic -> "CRITICAL".equals(diagnostic.severity().name()))
                .findFirst()
                .or(() -> diagnostics.stream()
                        .filter(diagnostic -> diagnostic.severity() != null)
                        .filter(diagnostic -> "WARNING".equals(diagnostic.severity().name()))
                        .findFirst())
                .or(() -> diagnostics.stream().findFirst())
                .orElse(null);
    }

    private String recommendedSessionAction(DiagnosticWarning diagnostic) {
        if (diagnostic == null) {
            return "continue observing or generate a report when needed";
        }

        String code = diagnostic.code();

        if ("INSUFFICIENT_SAMPLES_FOR_TREND".equals(code)) {
            return "collect at least four samples before interpreting trends";
        }

        if ("PARTIAL_SAMPLE_DATA".equals(code)) {
            return "check probe availability and raw output";
        }

        if (code != null && (code.startsWith("PROBE_") || code.startsWith("UPTIME_PROBE_"))) {
            return "verify process availability, permissions and local jcmd";
        }

        if ("HEAP_SESSION_GROWING".equals(code) || "HEAP_NEAR_MAX".equals(code)) {
            return "observe after workload stabilization and inspect retained memory if growth continues";
        }

        if ("METASPACE_SESSION_GROWING".equals(code)) {
            return "inspect class loading, generated classes and class loaders";
        }

        if ("HEAP_PARSER_UNSUPPORTED_FORMAT".equals(code)) {
            return "keep raw GC.heap_info output for parser support";
        }

        return diagnostic.recommendation() == null || diagnostic.recommendation().isBlank()
                ? "review diagnostic details"
                : diagnostic.recommendation();
    }

    private void updateProbeStatus() {
        if (selectedPid == null) {
            processAvailability.setText("Process availability: unknown");
            probeAvailability.setText("Probe availability: unknown");
            probeHint.setText("Probe hint: no process selected");
            return;
        }

        boolean processAlive = ProcessHandle.of(selectedPid)
                .map(ProcessHandle::isAlive)
                .orElse(false);

        processAvailability.setText("Process availability: " + (processAlive ? "alive" : "not visible or terminated"));

        boolean heapStructured = latestHeapInfo != null && latestHeapInfo.hasStructuredValues();
        boolean uptimeStructured = latestVmUptime != null && latestVmUptime.hasStructuredValue();

        if (heapStructured || uptimeStructured) {
            probeAvailability.setText("Probe availability: jcmd data available");
            probeHint.setText("Probe hint: external JVM data is being collected successfully");
            return;
        }

        String structuredFailureKind = firstStructuredProbeFailureKind();
        if (structuredFailureKind != null) {
            updateProbeStatusFromFailureKind(structuredFailureKind);
            return;
        }

        String raw = collectRawProbeText();

        if (!processAlive) {
            probeAvailability.setText("Probe availability: unavailable");
            probeHint.setText("Probe hint: the selected process may have terminated or may not be visible to this user");
            return;
        }

        if (containsAny(raw, "permission", "operation not permitted", "access denied", "attachnot-supported")) {
            probeAvailability.setText("Probe availability: permission issue");
            probeHint.setText("Probe hint: jcmd may not be allowed to attach to this process with the current user");
            return;
        }

        if (containsAny(raw, "no such process", "not found", "process not found")) {
            probeAvailability.setText("Probe availability: process not found");
            probeHint.setText("Probe hint: go back to Processes and select a currently running Java process");
            return;
        }

        if (containsAny(raw, "jcmd", "cannot run program", "no such file", "error=2")) {
            probeAvailability.setText("Probe availability: jcmd may be unavailable");
            probeHint.setText("Probe hint: make sure Javacup is running with a JDK, not only a JRE, and that jcmd is available");
            return;
        }

        probeAvailability.setText("Probe availability: raw output only");
        probeHint.setText("Probe hint: jcmd returned data, but Javacup could not parse it into structured values yet");
    }


    private String firstStructuredProbeFailureKind() {
        String heapFailureKind = latestHeapInfo == null ? null : latestHeapInfo.probeFailureKind();
        if (isUsefulFailureKind(heapFailureKind)) {
            return heapFailureKind;
        }

        String uptimeFailureKind = latestVmUptime == null ? null : latestVmUptime.probeFailureKind();
        if (isUsefulFailureKind(uptimeFailureKind)) {
            return uptimeFailureKind;
        }

        return null;
    }

    private boolean isUsefulFailureKind(String failureKind) {
        return failureKind != null
                && !failureKind.isBlank()
                && !"NONE".equals(failureKind)
                && !"UNKNOWN".equals(failureKind);
    }

    private void updateProbeStatusFromFailureKind(String failureKind) {
        switch (failureKind) {
            case "PROCESS_NOT_FOUND" -> {
                probeAvailability.setText("Probe availability: process not found");
                probeHint.setText("Probe hint: the selected process ended or is no longer visible; go back to Processes and select a running JVM");
            }
            case "ATTACH_FAILED" -> {
                probeAvailability.setText("Probe availability: permission issue");
                probeHint.setText("Probe hint: jcmd could not attach to the selected JVM with the current user");
            }
            case "JCMD_UNAVAILABLE" -> {
                probeAvailability.setText("Probe availability: jcmd unavailable");
                probeHint.setText("Probe hint: make sure Javacup is running with a JDK and that jcmd exists in java.home/bin");
            }
            case "TIMEOUT" -> {
                probeAvailability.setText("Probe availability: timeout");
                probeHint.setText("Probe hint: jcmd did not complete within the configured timeout");
            }
            case "INTERRUPTED" -> {
                probeAvailability.setText("Probe availability: interrupted");
                probeHint.setText("Probe hint: the local probe was interrupted before completion");
            }
            default -> {
                probeAvailability.setText("Probe availability: failed");
                probeHint.setText("Probe hint: jcmd failed; raw output may contain more details");
            }
        }
    }

    private String collectRawProbeText() {
        StringBuilder builder = new StringBuilder();

        if (latestHeapInfo != null && latestHeapInfo.rawOutput() != null) {
            builder.append(latestHeapInfo.rawOutput()).append('\n');
        }

        if (latestVmUptime != null && latestVmUptime.rawOutput() != null) {
            builder.append(latestVmUptime.rawOutput()).append('\n');
        }

        return builder.toString().toLowerCase();
    }

    private boolean containsAny(String text, String... needles) {
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String needle : needles) {
            if (text.contains(needle.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private void updateStructuredVmUptime(ExternalVmUptime uptime) {
        vmUptime.setText("VM uptime: " + uptime.displayValue());
        vmUptimeSeconds.setText("VM uptime seconds: " + (uptime.uptimeSeconds() == null ? "unavailable" : uptime.uptimeSeconds()));
    }

    private void renderMetaspaceTrendChart(List<ExternalMetricSample> samples) {
        metaspaceTrendChart.removeAll();

        List<ExternalMetricSample> visibleSamples = samples.stream()
                .filter(sample -> sample.metaspaceUsedMb() != null)
                .skip(Math.max(0, samples.size() - 40))
                .toList();

        if (visibleSamples.isEmpty()) {
            metaspaceTrendChart.setText("No Metaspace samples available yet.");
            return;
        }

        renderMetricTrendChart(
                metaspaceTrendChart,
                visibleSamples,
                "Y: Metaspace used (MB)",
                "Metaspace",
                ExternalMetricSample::metaspaceUsedMb
        );
    }

    private void renderMetricTrendChart(
            Div target,
            List<ExternalMetricSample> visibleSamples,
            String yAxisTitle,
            String metricLabel,
            Function<ExternalMetricSample, Long> valueExtractor
    ) {
        target.removeAll();

        long minValue = visibleSamples.stream()
                .map(valueExtractor)
                .filter(value -> value != null)
                .min(Long::compareTo)
                .orElse(0L);

        long maxValue = visibleSamples.stream()
                .map(valueExtractor)
                .filter(value -> value != null)
                .max(Long::compareTo)
                .orElse(1L);

        long latestValue = valueExtractor.apply(visibleSamples.getLast());

        Span yAxisLabel = new Span(yAxisTitle);
        yAxisLabel.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("margin-bottom", "var(--lumo-space-xs)");

        HorizontalLayout chartRow = new HorizontalLayout();
        chartRow.setPadding(false);
        chartRow.setSpacing(false);
        chartRow.setWidthFull();
        chartRow.setAlignItems(Alignment.STRETCH);
        chartRow.getStyle().set("gap", "var(--lumo-space-s)");

        VerticalLayout yScale = new VerticalLayout();
        yScale.setPadding(false);
        yScale.setSpacing(false);
        yScale.setWidth("80px");
        yScale.setHeight("140px");
        yScale.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        Span maxLabel = new Span(maxValue + " MB");
        Span middleLabel = new Span(((maxValue + minValue) / 2) + " MB");
        Span minLabel = new Span(minValue + " MB");

        yScale.add(maxLabel, new Span(""), middleLabel, new Span(""), minLabel);
        yScale.expand(yScale.getComponentAt(1), yScale.getComponentAt(3));

        HorizontalLayout bars = new HorizontalLayout();
        bars.setPadding(false);
        bars.setSpacing(false);
        bars.setWidthFull();
        bars.setHeight("140px");
        bars.setAlignItems(Alignment.END);
        bars.getStyle()
                .set("gap", "3px")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-contrast-5pct)");

        long visibleRange = Math.max(1L, maxValue - minValue);

        for (ExternalMetricSample sample : visibleSamples) {
            long value = valueExtractor.apply(sample);
            int heightPercentage = maxValue == minValue
                    ? 50
                    : Math.max(4, (int) Math.round((value - minValue) * 100.0 / visibleRange));

            Div bar = new Div();
            bar.getStyle()
                    .set("height", heightPercentage + "%")
                    .set("min-width", "6px")
                    .set("flex", "1")
                    .set("border-radius", "var(--lumo-border-radius-s)")
                    .set("background", "var(--lumo-primary-color-50pct)");

            bar.getElement().setAttribute("title", metricLabel + ": " + value + " MB at " + SAMPLE_TIME_FORMATTER.format(sample.timestamp()));

            bars.add(bar);
        }

        chartRow.add(yScale, bars);
        chartRow.expand(bars);

        Span xAxisLabel = new Span("X: recent samples, oldest → newest");
        xAxisLabel.getStyle()
                .set("display", "block")
                .set("margin-top", "var(--lumo-space-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        Span caption = new Span(
                "Showing latest " + visibleSamples.size()
                        + " samples. Min: " + minValue
                        + " MB, Max: " + maxValue
                        + " MB, Latest: " + latestValue
                        + " MB."
        );
        caption.getStyle()
                .set("display", "block")
                .set("margin-top", "var(--lumo-space-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        target.add(yAxisLabel, chartRow, xAxisLabel, caption);
    }

    private void styleMetaspaceTrendChart() {
        metaspaceTrendChart.setWidthFull();
        metaspaceTrendChart.getStyle()
                .set("max-width", "100%")
                .set("padding-bottom", "var(--lumo-space-s)");
    }
    private void updateSampleSummary(ExternalMetricSampleSummary summary) {
        sampleCount.setText("Samples collected: " + summary.sampleCount());

        firstHeapUsed.setText("First heap used: " + formatNullableMb(summary.firstHeapUsedMb()));
        latestHeapUsed.setText("Latest heap used: " + formatNullableMb(summary.latestHeapUsedMb()));
        minHeapUsed.setText("Min heap used: " + formatNullableMb(summary.minHeapUsedMb()));
        maxHeapUsed.setText("Max heap used: " + formatNullableMb(summary.maxHeapUsedMb()));
        heapGrowth.setText("Heap growth: " + formatSignedMb(summary.heapGrowthMb()));

        firstMetaspaceUsed.setText("First Metaspace used: " + formatNullableMb(summary.firstMetaspaceUsedMb()));
        latestMetaspaceUsed.setText("Latest Metaspace used: " + formatNullableMb(summary.latestMetaspaceUsedMb()));
        minMetaspaceUsedSummary.setText("Min Metaspace used: " + formatNullableMb(summary.minMetaspaceUsedMb()));
        maxMetaspaceUsedSummary.setText("Max Metaspace used: " + formatNullableMb(summary.maxMetaspaceUsedMb()));
        metaspaceGrowth.setText("Metaspace growth: " + formatSignedMb(summary.metaspaceGrowthMb()));
    }

    private void updateStructuredHeapInfo(ExternalHeapInfo heapInfo) {
        heapType.setText("Heap type: " + emptyFallback(heapInfo.collectorOrHeapType()));
        heapUsed.setText("Heap used: " + formatMb(heapInfo.heapUsedMb()));
        heapTotal.setText("Heap total/committed: " + formatMb(heapInfo.heapTotalMb()));
        heapReserved.setText("Heap reserved: " + formatMb(heapInfo.heapReservedMb()));

        metaspaceUsed.setText("Metaspace used: " + formatMb(heapInfo.metaspaceUsedMb()));
        metaspaceCommitted.setText("Metaspace committed: " + formatMb(heapInfo.metaspaceCommittedMb()));
        metaspaceReserved.setText("Metaspace reserved: " + formatMb(heapInfo.metaspaceReservedMb()));

        classSpaceUsed.setText("Compressed class space used: " + formatMb(heapInfo.classSpaceUsedMb()));
        classSpaceCommitted.setText("Compressed class space committed: " + formatMb(heapInfo.classSpaceCommittedMb()));
        classSpaceReserved.setText("Compressed class space reserved: " + formatMb(heapInfo.classSpaceReservedMb()));
    }

    private String formatMb(java.util.Optional<Long> value) {
        return value.map(number -> number + " MB").orElse("unavailable");
    }

    private String formatNullableMb(Long value) {
        return value == null ? "unavailable" : value + " MB";
    }

    private String formatSignedMb(Long value) {
        if (value == null) {
            return "unavailable";
        }

        if (value > 0) {
            return "+" + value + " MB";
        }

        return value + " MB";
    }

    private String emptyFallback(String value) {
        return value == null || value.isBlank() ? "unavailable" : value;
    }

    private VerticalLayout section(String title, Component... rows) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();

        H2 heading = new H2(title);
        layout.add(heading);
        layout.add(rows);

        return layout;
    }


    private Component bottomSpacer() {
        Hr spacer = new Hr();
        spacer.getStyle()
                .set("opacity", "0")
                .set("margin-top", "var(--lumo-space-l)")
                .set("margin-bottom", "var(--lumo-space-xl)");
        return spacer;
    }

    private void styleTechnicalBlock(Div block) {
        block.setWidthFull();
        block.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere")
                .set("word-break", "break-word")
                .set("max-width", "100%")
                .set("padding", "var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-contrast-5pct)");
    }
}
