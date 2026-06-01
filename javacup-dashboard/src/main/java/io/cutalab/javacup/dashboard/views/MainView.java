package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.html.H1;
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

        add(
                new H1(AppInfo.NAME),
                new Paragraph(AppInfo.TAGLINE),
                new Paragraph("Local-first JVM memory diagnostics. Step 01 is running.")
        );
    }
}
