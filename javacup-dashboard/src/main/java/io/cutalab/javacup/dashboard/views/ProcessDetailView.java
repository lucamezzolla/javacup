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

@Route(value = "processes", layout = MainLayout.class)
public class ProcessDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final LocalJavaProcessService processService;
    private final ExternalProcessProbeService probeService;

    private final H1 title = new H1("Process details");
    private final Span pid = new Span();
    private final Span application = new Span();
    private final Span type = new Span();
    private final Span command = new Span();
    private final Div arguments = new Div();
    private final Div probeOutput = new Div();

    private JavaProcessInfo currentProcess;

    public ProcessDetailView(LocalJavaProcessService processService, ExternalProcessProbeService probeService) {
        this.processService = processService;
        this.probeService = probeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backButton = new Button("Back to processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button checkAccessButton = new Button("Check local access", event -> runProbe(ProbeType.VM_VERSION));
        Button heapInfoButton = new Button("Read heap info", event -> runProbe(ProbeType.HEAP_INFO));
        Button uptimeButton = new Button("Read VM uptime", event -> runProbe(ProbeType.VM_UPTIME));

        styleTechnicalBlock(arguments);
        styleTechnicalBlock(probeOutput);

        add(
                title,
                new Paragraph("This page prepares the process selection flow. External process monitoring will be added progressively."),
                new HorizontalLayout(backButton, checkAccessButton, heapInfoButton, uptimeButton),
                section("Identity", pid, application, type),
                section("Command", command),
                section("Arguments", arguments),
                section("External JVM probe", new Paragraph("These probes use the local JDK `jcmd` tool to check and read basic information from the selected JVM."), probeOutput),
                section("Next step", new Paragraph("The next milestone will convert command output into structured metrics instead of displaying raw text."))
        );
    }

    @Override
    public void setParameter(BeforeEvent event, Long processId) {
        processService.findJavaProcessByPid(processId)
                .ifPresentOrElse(this::showProcess, () -> showMissingProcess(processId));
    }

    private void showProcess(JavaProcessInfo process) {
        currentProcess = process;

        title.setText("Process " + process.pid());

        pid.setText("PID: " + process.pid());
        application.setText("Application: " + process.applicationName());
        type.setText("Type: " + process.processType());
        command.setText("Command: " + process.command());
        arguments.setText(process.argumentsAsText().isBlank() ? "(no arguments)" : process.argumentsAsText());
        probeOutput.setText("No probe executed yet.");
    }

    private void showMissingProcess(Long processId) {
        currentProcess = null;

        title.setText("Process not available");

        pid.setText("PID: " + processId);
        application.setText("Application: unavailable");
        type.setText("Type: unavailable");
        command.setText("Command: unavailable");
        arguments.setText("(process not found or not recognized as a Java process)");
        probeOutput.setText("No probe available.");

        Notification.show("Process " + processId + " is not available.");
    }

    private void runProbe(ProbeType probeType) {
        if (currentProcess == null) {
            Notification.show("No process selected.");
            return;
        }

        ProcessProbeResult result = switch (probeType) {
            case VM_VERSION -> probeService.probeVmVersion(currentProcess.pid());
            case HEAP_INFO -> probeService.probeHeapInfo(currentProcess.pid());
            case VM_UPTIME -> probeService.probeVmUptime(currentProcess.pid());
        };

        probeOutput.setText(result.displayText());

        if (result.successful()) {
            Notification.show(probeType.successMessage());
        } else {
            Notification.show(probeType.failureMessage());
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

    private enum ProbeType {
        VM_VERSION("Local access probe succeeded.", "Local access probe failed."),
        HEAP_INFO("Heap info probe succeeded.", "Heap info probe failed."),
        VM_UPTIME("VM uptime probe succeeded.", "VM uptime probe failed.");

        private final String successMessage;
        private final String failureMessage;

        ProbeType(String successMessage, String failureMessage) {
            this.successMessage = successMessage;
            this.failureMessage = failureMessage;
        }

        public String successMessage() {
            return successMessage;
        }

        public String failureMessage() {
            return failureMessage;
        }
    }
}
