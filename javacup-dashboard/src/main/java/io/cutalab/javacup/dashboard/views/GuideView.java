package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.cutalab.javacup.core.AppInfo;

@Route(value = "guide", layout = MainLayout.class)
public class GuideView extends VerticalLayout {

    public GuideView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Quick guide");

        Paragraph intro = new Paragraph(
                "Javacup is a local-first JVM memory diagnostic tool. Use this page as a quick map: read the short sections, then follow the links when you want deeper details."
        );

        H2 navigationTitle = sectionTitle("Quick navigation", "quick-navigation");
        UnorderedList navigation = new UnorderedList(
                internalLinkItem("First steps", "first-steps"),
                internalLinkItem("Suggested workflow", "suggested-workflow"),
                internalLinkItem("How to read diagnostics", "diagnostics"),
                internalLinkItem("Which diagnostic am I seeing?", "diagnostic-codes"),
                internalLinkItem("Common problems", "common-problems"),
                internalLinkItem("Reports and comparison", "reports"),
                internalLinkItem("Learn more", "learn-more"),
                internalLinkItem("Privacy", "privacy"),
                internalLinkItem("About Javacup", "about")
        );

        H2 firstStepsTitle = sectionTitle("First steps", "first-steps");
        UnorderedList firstSteps = new UnorderedList(
                new ListItem("Open Processes and select a running Java process."),
                new ListItem("Open external metrics to read heap, Metaspace and uptime through local JDK tools."),
                new ListItem("Collect samples during a short observation window."),
                new ListItem("Generate and archive a JSON report when you want a snapshot."),
                new ListItem("Compare archived reports to explain what changed between two snapshots.")
        );

        H2 workflowTitle = sectionTitle("Suggested workflow", "suggested-workflow");
        UnorderedList workflow = new UnorderedList(
                new ListItem("Start with Processes and choose the JVM you want to observe."),
                new ListItem("Read external metrics once to confirm that probes work."),
                new ListItem("Collect samples while reproducing the behavior you want to investigate."),
                new ListItem("Generate a report when the interesting state is visible."),
                new ListItem("Generate a second report later if you want to compare growth or diagnostic changes."),
                new ListItem("Use Archived reports to preview, download and compare local JSON snapshots.")
        );

        Paragraph whenToReport = new Paragraph(
                "Save a report before and after an important action, after a memory spike, when a diagnostic appears, or before sharing evidence with another developer."
        );

        H2 diagnosticsTitle = sectionTitle("How to read diagnostics", "diagnostics");
        UnorderedList diagnostics = new UnorderedList(
                new ListItem("PROBE_* means Javacup could not read data from the selected JVM."),
                new ListItem("UPTIME_PROBE_* means the VM.uptime probe failed."),
                new ListItem("HEAP_PARSER_UNSUPPORTED_FORMAT means the probe succeeded, but Javacup does not support that GC.heap_info format yet."),
                new ListItem("HEAP_NEAR_MAX means heap usage is close to the committed or available heap reported by the probe."),
                new ListItem("Growth diagnostics become more useful after collecting multiple samples over time."),
                new ListItem("INSUFFICIENT_SAMPLES_FOR_TREND means Javacup needs more samples before trend diagnostics are reliable.")
        );

        H2 diagnosticCodesTitle = sectionTitle("Which diagnostic am I seeing?", "diagnostic-codes");
        UnorderedList diagnosticCodes = new UnorderedList(
                new ListItem("PROBE_PROCESS_NOT_FOUND: the selected JVM ended or cannot be found anymore."),
                new ListItem("PROBE_ATTACH_FAILED: Javacup could not attach to the target JVM with the current user/environment."),
                new ListItem("PROBE_JCMD_UNAVAILABLE: the local JDK diagnostic command tool is missing or cannot be launched."),
                new ListItem("PROBE_TIMEOUT: the jcmd probe did not complete in time."),
                new ListItem("UPTIME_PROBE_*: the same kind of probe issue happened while reading VM uptime."),
                new ListItem("HEAP_PARSER_UNSUPPORTED_FORMAT: jcmd returned data, but Javacup does not support that heap output format yet."),
                new ListItem("HEAP_NEAR_MAX: heap usage is high compared with the heap value reported by the JVM."),
                new ListItem("HEAP_STRUCTURED_INFO_AVAILABLE: the heap probe returned parseable structured values."),
                new ListItem("INSUFFICIENT_SAMPLES_FOR_TREND: collect more samples before trusting trend diagnostics.")
        );

        H2 commonProblemsTitle = sectionTitle("Common problems", "common-problems");
        UnorderedList commonProblems = new UnorderedList(
                new ListItem("PROCESS_NOT_FOUND: the target process ended or is no longer visible. Refresh Processes and select a running JVM."),
                new ListItem("ATTACH_FAILED: run Javacup with the same operating-system user as the target JVM and check attach permissions."),
                new ListItem("JCMD_UNAVAILABLE: run Javacup with a full JDK, not only a JRE, and make sure jcmd is available."),
                new ListItem("Unsupported parser format: keep the raw output; it can be used to improve parser support.")
        );

        H2 reportsTitle = sectionTitle("Reports and comparison", "reports");
        Paragraph reports = new Paragraph(
                "Reports are local JSON snapshots. They keep metadata, probe outputs, parsed values, diagnostics and recent samples. Use archived report comparison to understand whether memory usage, warnings or probe state changed."
        );

        H2 learnMoreTitle = sectionTitle("Learn more", "learn-more");
        UnorderedList learnMore = new UnorderedList(
                externalLinkItem("jcmd diagnostic command tool", "https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html"),
                externalLinkItem("Java troubleshooting tools", "https://docs.oracle.com/en/java/javase/21/troubleshoot/diagnostic-tools.html"),
                externalLinkItem("Java Flight Recorder", "https://docs.oracle.com/en/java/javase/21/jfapi/flight-recorder.html"),
                externalLinkItem("Garbage collection tuning guide", "https://docs.oracle.com/en/java/javase/21/gctuning/"),
                externalLinkItem("JSON format overview", "https://www.json.org/json-en.html")
        );

        H2 privacyTitle = sectionTitle("Privacy", "privacy");
        Paragraph privacy = new Paragraph(
                "Javacup is designed for local use. It observes local JVMs and writes local reports. Review report contents before sharing them outside your machine."
        );

        H2 aboutTitle = sectionTitle("About Javacup", "about");
        Paragraph about = new Paragraph(
                AppInfo.NAME + " " + AppInfo.VERSION + " — " + AppInfo.TAGLINE
        );
        Paragraph aboutScope = new Paragraph(
                "Javacup is developed as a local-first Java memory diagnostic assistant. It is intended for controlled local analysis, testing and support workflows."
        );
        Anchor projectLink = new Anchor(AppInfo.PROJECT_URL, "Project page");
        projectLink.setTarget("_blank");
        projectLink.getElement().setAttribute("rel", "noopener noreferrer");

        add(
                title,
                intro,
                navigationTitle,
                navigation,
                firstStepsTitle,
                firstSteps,
                workflowTitle,
                workflow,
                whenToReport,
                diagnosticsTitle,
                diagnostics,
                diagnosticCodesTitle,
                diagnosticCodes,
                commonProblemsTitle,
                commonProblems,
                reportsTitle,
                reports,
                learnMoreTitle,
                learnMore,
                privacyTitle,
                privacy,
                aboutTitle,
                about,
                aboutScope,
                projectLink
        );
    }

    private H2 sectionTitle(String text, String id) {
        H2 title = new H2(text);
        title.getElement().setAttribute("id", id);

        return title;
    }

    private ListItem internalLinkItem(String text, String sectionId) {
        return new ListItem(new Anchor("guide#" + sectionId, text));
    }

    private ListItem externalLinkItem(String text, String url) {
        Anchor anchor = new Anchor(url, text);
        anchor.setTarget("_blank");
        anchor.getElement().setAttribute("rel", "noopener noreferrer");

        return new ListItem(anchor);
    }
}
