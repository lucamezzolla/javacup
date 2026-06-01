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
        Button checkAccessButton = new Button("Check local access", event -> checkLocalAccess());

        styleTechnicalBlock(arguments);
        styleTechnicalBlock(probeOutput);

        add(
                title,
                new Paragraph("This page prepares the process selection flow. External process monitoring will be added in a later step."),
                new HorizontalLayout(backButton, checkAccessButton),
                section("Identity", pid, application, type),
                section("Command", command),
                section("Arguments", arguments),
                section("Local access probe", new Paragraph("The access probe uses the local JDK `jcmd` tool to check whether this JVM can be queried."), probeOutput),
                section("Next step", new Paragraph("The next technical milestone will read lightweight heap information from the selected process when local access is available."))
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

    private void checkLocalAccess() {
        if (currentProcess == null) {
            Notification.show("No process selected.");
            return;
        }

        ProcessProbeResult result = probeService.probeVmVersion(currentProcess.pid());
        probeOutput.setText(result.displayText());

        if (result.successful()) {
            Notification.show("Local access probe succeeded.");
        } else {
            Notification.show("Local access probe failed.");
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
