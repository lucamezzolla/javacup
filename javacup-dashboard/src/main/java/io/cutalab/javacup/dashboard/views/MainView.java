package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
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
                "Local-first JVM memory diagnostics. The current milestone focuses on discovering local Java processes."
        );

        H2 nextStep = new H2("Step 03");
        Paragraph stepDescription = new Paragraph("Open the process discovery page and check which local Java processes are visible.");
        Button processesButton = new Button("Open processes", event -> getUI().ifPresent(ui -> ui.navigate("processes")));

        add(title, tagline, description, nextStep, stepDescription, processesButton);
    }
}
