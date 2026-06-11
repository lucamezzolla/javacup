package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "local-lab", layout = MainLayout.class)
public class LocalLabView extends VerticalLayout {

    public LocalLabView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Local Lab");

        Paragraph intro = new Paragraph(
                "Local Lab is a quick workflow page for testing Javacup locally with the bundled demo JVMs."
        );

        H2 quickStartTitle = new H2("Quick start");
        UnorderedList quickStart = new UnorderedList(
                new ListItem("Open one terminal and start the dashboard."),
                new ListItem("Open another terminal and start a demo JVM."),
                new ListItem("Go to Processes, select the demo JVM and open External Metrics."),
                new ListItem("Collect samples, generate a report and compare archived reports.")
        );

        H2 dashboardTitle = new H2("Start the dashboard");
        Pre dashboardCommand = command("./scripts/run-dashboard.sh");

        H2 demoTitle = new H2("Start a demo JVM");
        Paragraph demoIntro = new Paragraph(
                "Use normal for a baseline process, heap for heap growth, and metaspace for class-loading/metaspace behavior."
        );
        Pre demoCommands = command("""
                ./scripts/run-demo.sh normal
                ./scripts/run-demo.sh heap
                ./scripts/run-demo.sh metaspace
                """);

        H2 workflowTitle = new H2("Suggested local workflow");
        UnorderedList workflow = new UnorderedList(
                new ListItem("Start the dashboard."),
                new ListItem("Start one demo JVM."),
                new ListItem("Open Processes and select the demo process."),
                new ListItem("Open External Metrics."),
                new ListItem("Collect at least four samples for trend diagnostics."),
                new ListItem("Generate a report preview and check Report verdict, Probe summary, Trend interpretation, Diagnostic summary and Recommended next actions."),
                new ListItem("Save/archive reports and compare them from Archived reports.")
        );

        H2 troubleshootingTitle = new H2("Troubleshooting");
        UnorderedList troubleshooting = new UnorderedList(
                new ListItem("If the demo process does not appear, refresh Processes and confirm the demo terminal is still running."),
                new ListItem("If jcmd fails, run Javacup with a full JDK and the same operating-system user as the target JVM."),
                new ListItem("If trend diagnostics look weak, collect more samples and check for partial sample data diagnostics.")
        );

        add(
                title,
                intro,
                quickStartTitle,
                quickStart,
                dashboardTitle,
                dashboardCommand,
                demoTitle,
                demoIntro,
                demoCommands,
                workflowTitle,
                workflow,
                troubleshootingTitle,
                troubleshooting
        );
    }

    private Pre command(String value) {
        Pre pre = new Pre(value.strip());
        pre.getStyle()
                .set("white-space", "pre-wrap")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("overflow-x", "auto");
        return pre;
    }
}
