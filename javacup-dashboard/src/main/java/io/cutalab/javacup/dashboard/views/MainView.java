package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.AppInfo;

@Route(value = "", layout = MainLayout.class)
public class MainView extends VerticalLayout {

    public MainView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1(AppInfo.NAME);
        Paragraph tagline = new Paragraph(AppInfo.TAGLINE);
        Paragraph description = new Paragraph(
                "Javacup is a local-first JVM diagnostic tool. The current milestone focuses on process discovery and lightweight JVM metrics."
        );

        H2 projectStatusTitle = new H2("Project status");
        Paragraph projectStatus = new Paragraph(
                "Current development version: " + AppInfo.VERSION + ". The current roadmap focus is to stabilize Javacup as a practical local JVM memory diagnostic MVP."
        );
        Paragraph projectFocus = new Paragraph(
                "Near-term work focuses on report readability, actionable interpretations, local-first workflows and controlled release polish."
        );

        H2 currentMilestone = new H2("Current milestone");
        Paragraph stepDescription = new Paragraph(
                "Discover local Java processes and inspect the JVM metrics of Javacup itself."
        );

        Button processesButton = new Button("Open processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));
        Button metricsButton = new Button("Open current JVM metrics", event -> getUI().ifPresent(ui -> ui.navigate("metrics/current")));
        Button samplesButton = new Button("Open metric samples", event -> getUI().ifPresent(ui -> ui.navigate("metrics/samples")));
        Button guideButton = new Button("Open guide", event -> getUI().ifPresent(ui -> ui.navigate("guide")));

        HorizontalLayout actions = new HorizontalLayout(processesButton, metricsButton, samplesButton, guideButton);

        add(
                title,
                tagline,
                description,
                projectStatusTitle,
                projectStatus,
                projectFocus,
                currentMilestone,
                stepDescription,
                actions
        );
    }
}
