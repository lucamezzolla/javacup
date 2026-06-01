package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.dashboard.LocalJavaProcessService;

@Route("processes")
public class ProcessesView extends VerticalLayout {

    private final LocalJavaProcessService processService;
    private final Grid<JavaProcessInfo> grid = new Grid<>(JavaProcessInfo.class, false);

    public ProcessesView(LocalJavaProcessService processService) {
        this.processService = processService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Java processes");
        Paragraph description = new Paragraph(
                "This page lists local Java processes detected through the Java ProcessHandle API. Monitoring is not enabled yet."
        );

        Button refreshButton = new Button("Refresh", event -> refresh());
        Button backButton = new Button("Back to dashboard", event -> getUI().ifPresent(ui -> ui.navigate("")));

        HorizontalLayout actions = new HorizontalLayout(refreshButton, backButton);

        configureGrid();

        add(title, description, actions, grid);
        expand(grid);

        refresh();
    }

    private void configureGrid() {
        grid.addColumn(JavaProcessInfo::pid)
                .setHeader("PID")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(JavaProcessInfo::displayName)
                .setHeader("Command")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(JavaProcessInfo::argumentsAsText)
                .setHeader("Arguments")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(process -> new Button("Monitor later", event ->
                        Notification.show("Monitoring will be added in a later step.")))
                .setHeader("Action")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private void refresh() {
        var processes = processService.findJavaProcesses();
        grid.setItems(processes);

        if (processes.isEmpty()) {
            Notification.show("No local Java processes detected.");
        } else {
            Notification.show(processes.size() + " Java process(es) detected.");
        }
    }
}
