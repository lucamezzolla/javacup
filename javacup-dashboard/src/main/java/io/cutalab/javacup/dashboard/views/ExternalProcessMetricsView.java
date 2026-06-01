package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
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
import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.core.process.ProcessProbeResult;
import io.cutalab.javacup.core.report.ExternalMonitoringReport;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import io.cutalab.javacup.core.session.MonitoringSession;
import io.cutalab.javacup.dashboard.ExternalHeapDiagnosticsService;
import io.cutalab.javacup.dashboard.ExternalHeapInfoService;
import io.cutalab.javacup.dashboard.ExternalMetricSampleService;
import io.cutalab.javacup.dashboard.ExternalMetricSampleSummaryService;
import io.cutalab.javacup.dashboard.ExternalMonitoringReportService;
import io.cutalab.javacup.dashboard.ExternalProcessProbeService;
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
    private final ExternalHeapInfoService heapInfoService;
    private final ExternalHeapDiagnosticsService diagnosticsService;
    private final ExternalSampleDiagnosticsService sampleDiagnosticsService;
    private final MonitoringSessionService monitoringSessionService;
    private final ExternalMetricSampleService sampleService;
    private final ExternalMetricSampleSummaryService sampleSummaryService;
    private final ExternalMonitoringReportService reportService;

    private final ObjectMapper reportObjectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final H1 title = new H1("External process metrics");
    private final Span pid = new Span();
    private final Span application = new Span();
    private final Span type = new Span();

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
    private final Grid<ExternalMetricSample> samplesGrid = new Grid<>(ExternalMetricSample.class, false);

    private final Div rawHeapInfo = new Div();
    private final Div uptimeInfo = new Div();
    private final Div reportPreview = new Div();
    private final Anchor downloadReportLink = new Anchor();

    private Long selectedPid;
    private MonitoringSession currentSession;
    private ExternalHeapInfo latestHeapInfo;
    private ExternalMetricSampleSummary latestSampleSummary;
    private List<DiagnosticWarning> latestDiagnostics = List.of();
    private List<ExternalMetricSample> latestSamples = List.of();
    private boolean sessionStopped;
    private Registration pollRegistration;

    public ExternalProcessMetricsView(
            LocalJavaProcessService processService,
            ExternalProcessProbeService probeService,
            ExternalHeapInfoService heapInfoService,
            ExternalHeapDiagnosticsService diagnosticsService,
            ExternalSampleDiagnosticsService sampleDiagnosticsService,
            MonitoringSessionService monitoringSessionService,
            ExternalMetricSampleService sampleService,
            ExternalMetricSampleSummaryService sampleSummaryService,
            ExternalMonitoringReportService reportService
    ) {
        this.processService = processService;
        this.probeService = probeService;
        this.heapInfoService = heapInfoService;
        this.diagnosticsService = diagnosticsService;
        this.sampleDiagnosticsService = sampleDiagnosticsService;
        this.monitoringSessionService = monitoringSessionService;
        this.sampleService = sampleService;
        this.sampleSummaryService = sampleSummaryService;
        this.reportService = reportService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("padding-bottom", "var(--lumo-space-xl)");

        Button backButton = new Button("Back to processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button refreshButton = new Button("Refresh metrics", event -> refreshMetrics(true));
        Button stopButton = new Button("Stop session", event -> stopSession());
        Button previewReportButton = new Button("Preview report", event -> previewReport());

        configureDiagnosticsGrid();
        configureSamplesGrid();

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
                new HorizontalLayout(backButton, refreshButton, stopButton, previewReportButton, downloadReportLink),
                section("Selected process", pid, application, type, autoRefreshStatus, lastRefresh),
                section("Monitoring session", sessionId, sessionStatus, sessionStartedAt, sessionLastUpdatedAt),
                section("Structured heap summary", heapType, heapUsed, heapTotal, heapReserved),
                section("Structured metaspace summary", metaspaceUsed, metaspaceCommitted, metaspaceReserved),
                section("Structured compressed class space summary", classSpaceUsed, classSpaceCommitted, classSpaceReserved),
                section("Diagnostics", new Paragraph("Rules: HEAP_NEAR_MAX and HEAP_SESSION_GROWING. More trend-based diagnostics will be added later."), diagnosticsGrid),
                section("Session trend summary", sampleCount, firstHeapUsed, latestHeapUsed, minHeapUsed, maxHeapUsed, heapGrowth),
                section("Recent external samples", new Paragraph("In-memory samples collected while this page is open. Oldest samples are discarded when the session buffer is full."), samplesRetained, samplesGrid),
                section("Raw heap information", new Paragraph("Source: jcmd <pid> GC.heap_info"), rawHeapInfo),
                section("VM uptime", new Paragraph("Source: jcmd <pid> VM.uptime"), uptimeInfo),
                section("Report preview", new Paragraph("Readable preview of the report data."), reportPreview),
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
        ProcessProbeResult uptimeResult = probeService.probeVmUptime(selectedPid);

        updateStructuredHeapInfo(latestHeapInfo);

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
        samplesRetained.setText("Samples retained: " + latestSamples.size() + " / " + sampleService.maxSamplesPerSession());
        samplesGrid.setItems(latestSamples.reversed());

        rawHeapInfo.setText(latestHeapInfo.rawOutput());
        uptimeInfo.setText(uptimeResult.displayText());
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
                latestSampleSummary,
                latestDiagnostics,
                latestSamples
        );
    }


    private void previewReport() {
        ExternalMonitoringReport report = createCurrentReport();

        if (report == null) {
            Notification.show("No report data available yet.");
            return;
        }

        reportPreview.setText(reportService.createReadablePreview(report));
        Notification.show("Report preview updated.");
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

    private void updateSampleSummary(ExternalMetricSampleSummary summary) {
        sampleCount.setText("Samples collected: " + summary.sampleCount());
        firstHeapUsed.setText("First heap used: " + formatNullableMb(summary.firstHeapUsedMb()));
        latestHeapUsed.setText("Latest heap used: " + formatNullableMb(summary.latestHeapUsedMb()));
        minHeapUsed.setText("Min heap used: " + formatNullableMb(summary.minHeapUsedMb()));
        maxHeapUsed.setText("Max heap used: " + formatNullableMb(summary.maxHeapUsedMb()));
        heapGrowth.setText("Heap growth: " + formatSignedMb(summary.growthMb()));
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
