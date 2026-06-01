package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
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
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.MonitoringSession;
import io.cutalab.javacup.dashboard.ExternalHeapDiagnosticsService;
import io.cutalab.javacup.dashboard.ExternalHeapInfoService;
import io.cutalab.javacup.dashboard.ExternalMetricSampleService;
import io.cutalab.javacup.dashboard.ExternalProcessProbeService;
import io.cutalab.javacup.dashboard.LocalJavaProcessService;
import io.cutalab.javacup.dashboard.MonitoringSessionService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
    private final MonitoringSessionService monitoringSessionService;
    private final ExternalMetricSampleService sampleService;

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
    private final Grid<ExternalMetricSample> samplesGrid = new Grid<>(ExternalMetricSample.class, false);

    private final Div rawHeapInfo = new Div();
    private final Div uptimeInfo = new Div();

    private Long selectedPid;
    private MonitoringSession currentSession;
    private boolean sessionStopped;
    private Registration pollRegistration;

    public ExternalProcessMetricsView(
            LocalJavaProcessService processService,
            ExternalProcessProbeService probeService,
            ExternalHeapInfoService heapInfoService,
            ExternalHeapDiagnosticsService diagnosticsService,
            MonitoringSessionService monitoringSessionService,
            ExternalMetricSampleService sampleService
    ) {
        this.processService = processService;
        this.probeService = probeService;
        this.heapInfoService = heapInfoService;
        this.diagnosticsService = diagnosticsService;
        this.monitoringSessionService = monitoringSessionService;
        this.sampleService = sampleService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backButton = new Button("Back to processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button refreshButton = new Button("Refresh metrics", event -> refreshMetrics(true));
        Button stopButton = new Button("Stop session", event -> stopSession());

        configureDiagnosticsGrid();
        configureSamplesGrid();

        styleTechnicalBlock(rawHeapInfo);
        styleTechnicalBlock(uptimeInfo);

        add(
                title,
                new Paragraph("This page reads external JVM information from a selected Java process using local JDK diagnostic commands."),
                new HorizontalLayout(backButton, refreshButton, stopButton),
                section("Selected process", pid, application, type, autoRefreshStatus, lastRefresh),
                section("Monitoring session", sessionId, sessionStatus, sessionStartedAt, sessionLastUpdatedAt),
                section("Structured heap summary", heapType, heapUsed, heapTotal, heapReserved),
                section("Structured metaspace summary", metaspaceUsed, metaspaceCommitted, metaspaceReserved),
                section("Structured compressed class space summary", classSpaceUsed, classSpaceCommitted, classSpaceReserved),
                section("Diagnostics", new Paragraph("First rule: HEAP_NEAR_MAX. Trend-based diagnostics will be added later."), diagnosticsGrid),
                section("Recent external samples", new Paragraph("In-memory samples collected while this page is open."), samplesGrid),
                section("Raw heap information", new Paragraph("Source: jcmd <pid> GC.heap_info"), rawHeapInfo),
                section("VM uptime", new Paragraph("Source: jcmd <pid> VM.uptime"), uptimeInfo)
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

        samplesGrid.setAllRowsVisible(true);
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

        ExternalHeapInfo heapInfo = heapInfoService.readHeapInfo(selectedPid);
        ProcessProbeResult uptimeResult = probeService.probeVmUptime(selectedPid);

        updateStructuredHeapInfo(heapInfo);
        diagnosticsGrid.setItems(diagnosticsService.analyze(heapInfo));

        if (heapInfo.hasStructuredValues()) {
            sampleService.addSample(currentSession, heapInfo);
        }

        samplesGrid.setItems(sampleService.findSamples(currentSession.id()));

        rawHeapInfo.setText(heapInfo.rawOutput());
        uptimeInfo.setText(uptimeResult.displayText());
        lastRefresh.setText("Last refresh: " + LocalDateTime.now().format(REFRESH_TIME_FORMATTER));

        if (showNotification) {
            if (heapInfo.hasStructuredValues()) {
                Notification.show("External metrics refreshed.");
            } else {
                Notification.show("External heap info refreshed, but structured parsing is incomplete.");
            }
        }
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
