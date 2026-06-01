package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.AppInfo;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1(AppInfo.NAME);
        Paragraph tagline = new Paragraph(AppInfo.TAGLINE);
        Paragraph description = new Paragraph(
                "Local-first JVM memory diagnostics. The current milestone introduces basic JVM metrics."
        );

        H2 currentMilestone = new H2("Current milestone");
        Paragraph stepDescription = new Paragraph(
                "Discover local Java processes and inspect the JVM metrics of Javacup itself."
        );

        Button processesButton = new Button("Open processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button metricsButton = new Button("Open current JVM metrics", event -> getUI().ifPresent(ui -> ui.navigate("metrics/current")));

        HorizontalLayout actions = new HorizontalLayout(processesButton, metricsButton);

        add(title, tagline, description, currentMilestone, stepDescription, actions);
    }
}
