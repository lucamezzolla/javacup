package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.dashboard.LocalJavaProcessService;

import java.util.ArrayList;
import java.util.List;

@Route(value = "processes", layout = MainLayout.class)
public class ProcessesView extends VerticalLayout {

    private final LocalJavaProcessService processService;
    private final Grid<JavaProcessInfo> grid = new Grid<>(JavaProcessInfo.class, false);
    private final TextField filterField = new TextField();

    private List<JavaProcessInfo> allProcesses = new ArrayList<>();

    public ProcessesView(LocalJavaProcessService processService) {
        this.processService = processService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Java processes");
        Paragraph description = new Paragraph(
                "This page lists local Java processes detected through the Java ProcessHandle API. Use the filter to search by PID, application name, command or arguments."
        );

        filterField.setPlaceholder("Filter by PID, jar name, command or arguments");
        filterField.setClearButtonVisible(true);
        filterField.setWidthFull();
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(event -> applyFilter());

        Button refreshButton = new Button("Refresh", event -> refresh());

        HorizontalLayout actions = new HorizontalLayout(refreshButton);
        actions.setWidthFull();

        configureGrid();

        add(title, description, filterField, actions, grid);
        expand(grid);

        refresh();
    }

    private void configureGrid() {
        grid.addColumn(JavaProcessInfo::pid)
                .setHeader("PID")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(JavaProcessInfo::applicationName)
                .setHeader("Application")
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
                        Notification.show("Monitoring process " + process.pid() + " will be added in a later step.")))
                .setHeader("Action")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private void refresh() {
        allProcesses = processService.findJavaProcesses();
        applyFilter();

        if (allProcesses.isEmpty()) {
            Notification.show("No local Java processes detected.");
        } else {
            Notification.show(allProcesses.size() + " Java process(es) detected.");
        }
    }

    private void applyFilter() {
        String filterText = filterField.getValue();

        List<JavaProcessInfo> filtered = allProcesses.stream()
                .filter(process -> process.matches(filterText))
                .toList();

        grid.setItems(filtered);
    }
}
