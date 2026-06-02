package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "donate", layout = MainLayout.class)
public class DonationsView extends VerticalLayout {

    private static final String PAYPAL_DONATION_URL = "https://www.paypal.com/paypalme/lucamezzolla82";
    private static final String PAYPAL_BADGE_URL = "https://img.shields.io/badge/Donate-PayPal-00457C?logo=paypal&logoColor=white";

    public DonationsView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("padding-bottom", "var(--lumo-space-xl)");

        H1 title = new H1("Support Javacup");

        Image paypalBadge = new Image(PAYPAL_BADGE_URL, "Donate with PayPal");
        paypalBadge.setHeight("28px");

        Anchor paypalLink = new Anchor(PAYPAL_DONATION_URL, paypalBadge);
        paypalLink.setTarget("_blank");
        paypalLink.getElement().setAttribute("rel", "noopener noreferrer");
        paypalLink.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("width", "fit-content");

        Paragraph intro = new Paragraph(
                "Javacup is an open-source, local-first JVM memory diagnostic tool designed to help Java developers understand, observe and explain memory behavior without sending application data to external services."
        );

        Paragraph support = new Paragraph(
                "If you find Javacup useful, or if you want to support its development, you can make a small donation through PayPal. Your support helps improve documentation, testing, diagnostics, UI polish, examples and controlled production-readiness."
        );

        Paragraph privacy = new Paragraph(
                "The project is built around a privacy-conscious workflow: Javacup runs locally, observes local JVM processes and focuses on explainable diagnostics for developers, maintainers and technical teams."
        );

        H2 currentScopeTitle = new H2("What Javacup already focuses on");

        UnorderedList currentScope = new UnorderedList(
                new ListItem("Discovering local Java processes."),
                new ListItem("Reading selected JVM information through local JDK diagnostic commands."),
                new ListItem("Parsing heap and VM uptime information from external Java processes."),
                new ListItem("Collecting bounded in-memory samples during a monitoring session."),
                new ListItem("Showing trend summaries and lightweight charts for external heap usage."),
                new ListItem("Providing early diagnostic warnings such as heap growth, heap near max and Metaspace growth."),
                new ListItem("Generating readable previews and JSON reports for local analysis.")
        );

        H2 roadmapTitle = new H2("Long-term roadmap");

        Paragraph roadmapIntro = new Paragraph(
                "The wider vision is to make Javacup a practical memory doctor for Java applications: a tool that can inspect, explain and document memory behavior while staying simple enough to run locally during development, testing and support sessions."
        );

        UnorderedList roadmap = new UnorderedList(
                new ListItem("Richer memory timelines and charts for heap, Metaspace, class loading, threads and garbage collection."),
                new ListItem("More diagnostic rules for leaks, GC pressure, retained memory patterns, Metaspace growth, thread growth and suspicious allocation trends."),
                new ListItem("Improved external process monitoring with clearer states for terminated processes, permission issues and unavailable JDK tools."),
                new ListItem("Better report exports for debugging sessions, including JSON first and richer human-readable formats later."),
                new ListItem("Optional local storage for comparing sessions over time."),
                new ListItem("JFR-oriented analysis for deeper JVM investigation."),
                new ListItem("Vaadin-specific memory inspection ideas for applications built with Vaadin."),
                new ListItem("Packaging and release artifacts suitable for early adopters and controlled local usage.")
        );

        H2 whyDonateTitle = new H2("Why donations matter");

        Paragraph whyDonate = new Paragraph(
                "Donations help keep the project moving carefully: adding tests before risky changes, documenting each milestone, polishing the user experience, validating behavior on real JVM outputs and preparing safe releases for developers who want a trustworthy diagnostic companion."
        );

        add(
                title,
                paypalLink,
                intro,
                support,
                privacy,
                currentScopeTitle,
                currentScope,
                roadmapTitle,
                roadmapIntro,
                roadmap,
                whyDonateTitle,
                whyDonate
        );
    }
}
