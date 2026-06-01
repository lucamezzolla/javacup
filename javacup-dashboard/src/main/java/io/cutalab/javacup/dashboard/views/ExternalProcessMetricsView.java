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
import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.core.process.ProcessProbeResult;
import io.cutalab.javacup.dashboard.ExternalProcessProbeService;
import io.cutalab.javacup.dashboard.LocalJavaProcessService;

@Route(value = "metrics/external", layout = MainLayout.class)
public class ExternalProcessMetricsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final LocalJavaProcessService processService;
    private final ExternalProcessProbeService probeService;

    private final H1 title = new H1("External process metrics");
    private final Span pid = new Span();
    private final Span application = new Span();
    private final Span type = new Span();
    private final Div heapInfo = new Div();
    private final Div uptimeInfo = new Div();

    private JavaProcessInfo selectedProcess;

    public ExternalProcessMetricsView(LocalJavaProcessService processService, ExternalProcessProbeService probeService) {
        this.processService = processService;
        this.probeService = probeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backButton = new Button("Back to processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button refreshButton = new Button("Refresh metrics", event -> refreshMetrics());

        styleTechnicalBlock(heapInfo);
        styleTechnicalBlock(uptimeInfo);

        add(
                title,
                new Paragraph("This page reads raw external JVM information from a selected Java process using local JDK diagnostic commands."),
                new HorizontalLayout(backButton, refreshButton),
                section("Selected process", pid, application, type),
                section("Heap information", new Paragraph("Source: jcmd <pid> GC.heap_info"), heapInfo),
                section("VM uptime", new Paragraph("Source: jcmd <pid> VM.uptime"), uptimeInfo),
                section("Note", new Paragraph("This is still raw probe output. A later step will parse it into structured metrics."))
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
        heapInfo.setText("Process not found or not recognized as a Java process.");
        uptimeInfo.setText("Process not found or not recognized as a Java process.");

        Notification.show("Process " + processId + " is not available.");
    }

    private void refreshMetrics() {
        if (selectedProcess == null) {
            Notification.show("No process selected.");
            return;
        }

        ProcessProbeResult heapResult = probeService.probeHeapInfo(selectedProcess.pid());
        ProcessProbeResult uptimeResult = probeService.probeVmUptime(selectedProcess.pid());

        heapInfo.setText(heapResult.displayText());
        uptimeInfo.setText(uptimeResult.displayText());

        if (heapResult.successful() && uptimeResult.successful()) {
            Notification.show("External metrics refreshed.");
        } else {
            Notification.show("Some external metrics could not be read.");
        }
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
