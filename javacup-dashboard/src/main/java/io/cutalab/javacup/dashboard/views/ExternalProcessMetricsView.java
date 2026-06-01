package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
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
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.core.process.ProcessProbeResult;
import io.cutalab.javacup.dashboard.ExternalHeapInfoService;
import io.cutalab.javacup.dashboard.ExternalProcessProbeService;
import io.cutalab.javacup.dashboard.LocalJavaProcessService;

@Route(value = "metrics/external", layout = MainLayout.class)
public class ExternalProcessMetricsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final LocalJavaProcessService processService;
    private final ExternalProcessProbeService probeService;
    private final ExternalHeapInfoService heapInfoService;

    private final H1 title = new H1("External process metrics");
    private final Span pid = new Span();
    private final Span application = new Span();
    private final Span type = new Span();

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

    private final Div rawHeapInfo = new Div();
    private final Div uptimeInfo = new Div();

    private JavaProcessInfo selectedProcess;

    public ExternalProcessMetricsView(
            LocalJavaProcessService processService,
            ExternalProcessProbeService probeService,
            ExternalHeapInfoService heapInfoService
    ) {
        this.processService = processService;
        this.probeService = probeService;
        this.heapInfoService = heapInfoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backButton = new Button("Back to processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button refreshButton = new Button("Refresh metrics", event -> refreshMetrics());

        styleTechnicalBlock(rawHeapInfo);
        styleTechnicalBlock(uptimeInfo);

        add(
                title,
                new Paragraph("This page reads external JVM information from a selected Java process using local JDK diagnostic commands."),
                new HorizontalLayout(backButton, refreshButton),
                section("Selected process", pid, application, type),
                section("Structured heap summary", heapType, heapUsed, heapTotal, heapReserved),
                section("Structured metaspace summary", metaspaceUsed, metaspaceCommitted, metaspaceReserved),
                section("Structured compressed class space summary", classSpaceUsed, classSpaceCommitted, classSpaceReserved),
                section("Raw heap information", new Paragraph("Source: jcmd <pid> GC.heap_info"), rawHeapInfo),
                section("VM uptime", new Paragraph("Source: jcmd <pid> VM.uptime"), uptimeInfo)
        );
    }

    @Override
    public void setParameter(BeforeEvent event, Long processId) {
        processService.findJavaProcessByPid(processId)
                .ifPresentOrElse(this::showProcess, () -> showMissingProcess(processId));
    }

    private void showProcess(JavaProcessInfo process) {
        selectedProcess = process;

        title.setText("External metrics for process " + process.pid());
        pid.setText("PID: " + process.pid());
        application.setText("Application: " + process.applicationName());
        type.setText("Type: " + process.processType());

        refreshMetrics();
    }

    private void showMissingProcess(Long processId) {
        selectedProcess = null;

        title.setText("External process not available");
        pid.setText("PID: " + processId);
        application.setText("Application: unavailable");
        type.setText("Type: unavailable");

        clearStructuredValues();
        rawHeapInfo.setText("Process not found or not recognized as a Java process.");
        uptimeInfo.setText("Process not found or not recognized as a Java process.");

        Notification.show("Process " + processId + " is not available.");
    }

    private void refreshMetrics() {
        if (selectedProcess == null) {
            Notification.show("No process selected.");
            return;
        }

        ExternalHeapInfo heapInfo = heapInfoService.readHeapInfo(selectedProcess.pid());
        ProcessProbeResult uptimeResult = probeService.probeVmUptime(selectedProcess.pid());

        updateStructuredHeapInfo(heapInfo);

        rawHeapInfo.setText(heapInfo.rawOutput());
        uptimeInfo.setText(uptimeResult.displayText());

        if (heapInfo.hasStructuredValues()) {
            Notification.show("External metrics refreshed.");
        } else {
            Notification.show("External heap info refreshed, but structured parsing is incomplete.");
        }
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

    private void clearStructuredValues() {
        heapType.setText("Heap type: unavailable");
        heapUsed.setText("Heap used: unavailable");
        heapTotal.setText("Heap total/committed: unavailable");
        heapReserved.setText("Heap reserved: unavailable");

        metaspaceUsed.setText("Metaspace used: unavailable");
        metaspaceCommitted.setText("Metaspace committed: unavailable");
        metaspaceReserved.setText("Metaspace reserved: unavailable");

        classSpaceUsed.setText("Compressed class space used: unavailable");
        classSpaceCommitted.setText("Compressed class space committed: unavailable");
        classSpaceReserved.setText("Compressed class space reserved: unavailable");
    }

    private String formatMb(java.util.Optional<Long> value) {
        return value.map(number -> number + " MB").orElse("unavailable");
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
