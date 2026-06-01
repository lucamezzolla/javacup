package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.dashboard.LocalJavaProcessService;

@Route(value = "processes", layout = MainLayout.class)
public class ProcessDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final LocalJavaProcessService processService;

    private final H1 title = new H1("Process details");
    private final Span pid = new Span();
    private final Span application = new Span();
    private final Span type = new Span();
    private final Span command = new Span();
    private final Pre arguments = new Pre();

    public ProcessDetailView(LocalJavaProcessService processService) {
        this.processService = processService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backButton = new Button("Back to processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));

        add(
                title,
                new Paragraph("This page prepares the process selection flow. External process monitoring will be added in a later step."),
                new HorizontalLayout(backButton),
                section("Identity", pid, application, type),
                section("Command", command),
                section("Arguments", arguments),
                section("Next step", new Paragraph("The next technical milestone will investigate safe local attach/JMX access for this selected process."))
        );
    }

    @Override
    public void setParameter(BeforeEvent event, Long processId) {
        processService.findJavaProcessByPid(processId)
                .ifPresentOrElse(this::showProcess, () -> showMissingProcess(processId));
    }

    private void showProcess(JavaProcessInfo process) {
        title.setText("Process " + process.pid());

        pid.setText("PID: " + process.pid());
        application.setText("Application: " + process.applicationName());
        type.setText("Type: " + process.processType());
        command.setText("Command: " + process.command());
        arguments.setText(process.argumentsAsText().isBlank() ? "(no arguments)" : process.argumentsAsText());
    }

    private void showMissingProcess(Long processId) {
        title.setText("Process not available");

        pid.setText("PID: " + processId);
        application.setText("Application: unavailable");
        type.setText("Type: unavailable");
        command.setText("Command: unavailable");
        arguments.setText("(process not found or not recognized as a Java process)");

        Notification.show("Process " + processId + " is not available.");
    }

    private VerticalLayout section(String title, Component... rows) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        H2 heading = new H2(title);
        layout.add(heading);
        layout.add(rows);

        return layout;
    }
}
