package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.cutalab.javacup.dashboard.LocalArchivedReport;
import io.cutalab.javacup.dashboard.LocalReportArchiveService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "reports/archived", layout = MainLayout.class)
public class ArchivedReportsView extends VerticalLayout {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final LocalReportArchiveService archiveService;
    private final Grid<LocalArchivedReport> reportsGrid = new Grid<>(LocalArchivedReport.class, false);
    private final Paragraph summary = new Paragraph();

    public ArchivedReportsView(LocalReportArchiveService archiveService) {
        this.archiveService = archiveService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Archived reports");

        Paragraph description = new Paragraph(
                "Local JSON reports archived from external JVM monitoring sessions. Files are stored under "
                        + archiveService.archiveDirectory().toAbsolutePath()
                        + "."
        );

        Button refreshButton = new Button("Refresh", event -> refreshReports());

        configureGrid();

        add(title, description, refreshButton, summary, reportsGrid);

        refreshReports();
    }

    private void configureGrid() {
        reportsGrid.setWidthFull();
        reportsGrid.setAllRowsVisible(true);

        reportsGrid.addColumn(LocalArchivedReport::fileName)
                .setHeader("File")
                .setAutoWidth(true)
                .setFlexGrow(1);

        reportsGrid.addColumn(report -> formatSize(report.sizeBytes()))
                .setHeader("Size")
                .setAutoWidth(true);

        reportsGrid.addColumn(report -> DATE_TIME_FORMATTER.format(report.lastModifiedAt()))
                .setHeader("Last modified")
                .setAutoWidth(true);

        reportsGrid.addComponentColumn(this::createPreviewButton)
                .setHeader("Preview")
                .setAutoWidth(true);

        reportsGrid.addComponentColumn(this::createPathDownloadLink)
                .setHeader("Path")
                .setAutoWidth(false)
                .setFlexGrow(2);
    }

    private Button createPreviewButton(LocalArchivedReport report) {
        return new Button("Preview", event -> showReportPreview(report));
    }

    private void showReportPreview(LocalArchivedReport report) {
        try {
            String reportText = archiveService.readReportText(report);

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Archived report preview");
            dialog.setWidth("min(900px, 95vw)");
            dialog.setMaxHeight("90vh");

            H2 fileName = new H2(report.fileName());
            fileName.getStyle()
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("margin", "0");

            Pre content = new Pre(reportText);
            content.getStyle()
                    .set("max-height", "65vh")
                    .set("overflow", "auto")
                    .set("white-space", "pre-wrap")
                    .set("word-break", "break-word")
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("font-size", "var(--lumo-font-size-s)");

            Button closeButton = new Button("Close", event -> dialog.close());
            HorizontalLayout footer = new HorizontalLayout(closeButton);
            footer.setWidthFull();
            footer.setJustifyContentMode(JustifyContentMode.END);

            dialog.add(fileName, content, footer);
            dialog.open();
        } catch (RuntimeException exception) {
            Notification.show("Unable to preview report: " + exception.getMessage());
        }
    }
    private Anchor createPathDownloadLink(LocalArchivedReport report) {
        Anchor link = new Anchor(
                new StreamResource(report.fileName(), () -> {
                    try {
                        return Files.newInputStream(Path.of(report.absolutePath()));
                    } catch (IOException exception) {
                        throw new IllegalStateException("Unable to open archived report", exception);
                    }
                }),
                report.absolutePath()
        );

        link.getElement().setAttribute("download", true);
        link.getElement().setAttribute("title", report.absolutePath());
        link.getStyle()
                .set("display", "block")
                .set("max-width", "100%")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap")
                .set("cursor", "pointer");

        return link;
    }
    private void refreshReports() {
        try {
            List<LocalArchivedReport> reports = archiveService.listReports();

            reportsGrid.setItems(reports);
            summary.setText("Archived reports: " + reports.size());
        } catch (IllegalStateException exception) {
            reportsGrid.setItems(List.of());
            summary.setText("Archived reports: unavailable");
            Notification.show("Unable to load archived reports: " + exception.getMessage());
        }
    }

    private String formatSize(long sizeBytes) {
        if (sizeBytes < 0) {
            return "unavailable";
        }

        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }

        long sizeKb = sizeBytes / 1024;

        if (sizeKb < 1024) {
            return sizeKb + " KB";
        }

        long sizeMb = sizeKb / 1024;

        return sizeMb + " MB";
    }
}
