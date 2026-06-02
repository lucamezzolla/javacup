package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
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

        reportsGrid.addComponentColumn(this::createPathDownloadLink)
                .setHeader("Path")
                .setAutoWidth(false)
                .setFlexGrow(2);
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
