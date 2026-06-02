package io.cutalab.javacup.dashboard.views;

import com.fasterxml.jackson.databind.JsonNode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.cutalab.javacup.dashboard.LocalArchivedReport;
import io.cutalab.javacup.dashboard.LocalReportArchiveService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

@Route(value = "reports/archived", layout = MainLayout.class)
public class ArchivedReportsView extends VerticalLayout {

    private enum MemoryDisplayUnit {
        AUTO("Auto"),
        BYTES("Bytes"),
        KB("KB"),
        MB("MB"),
        GB("GB");

        private final String label;

        MemoryDisplayUnit(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final ZoneId LOCAL_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(LOCAL_ZONE);

    private final LocalReportArchiveService archiveService;
    private final Grid<LocalArchivedReport> reportsGrid = new Grid<>(LocalArchivedReport.class, false);
    private final Paragraph summary = new Paragraph();
    private final TextField fileSearchField = new TextField("File or path contains");
    private final DateTimePicker fromDateTimePicker = new DateTimePicker("From");
    private final DateTimePicker toDateTimePicker = new DateTimePicker("To");
    private final Button compareSelectedButton = new Button("Compare selected");
    private final Button clearSelectionButton = new Button("Clear selection");
    private final Paragraph compareSelectionSummary = new Paragraph("Select exactly two reports to compare.");
    private final Select<MemoryDisplayUnit> memoryUnitSelect = new Select<>();

    public ArchivedReportsView(LocalReportArchiveService archiveService) {
        this.archiveService = archiveService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Archived reports");

        Paragraph description = new Paragraph(
                "Local JSON reports archived from external JVM monitoring sessions. Files are stored under "
                        + archiveService.archiveDirectory().toAbsolutePath()
                        + ". Date filters use the local system timezone: "
                        + LOCAL_ZONE
                        + ". Dates are displayed in an ISO-like format with offset."
        );

        Button refreshButton = new Button("Refresh", event -> refreshReports());
        Button applyFiltersButton = new Button("Apply filters", event -> refreshReports());
        Button clearFiltersButton = new Button("Clear filters", event -> clearFilters());
        compareSelectedButton.addClickListener(event -> compareSelectedReports());
        clearSelectionButton.addClickListener(event -> clearReportSelection());

        configureFilters();
        configureMemoryUnitSelect();
        configureGrid();

        HorizontalLayout filters = new HorizontalLayout(
                fileSearchField,
                fromDateTimePicker,
                toDateTimePicker,
                applyFiltersButton,
                clearFiltersButton,
                refreshButton,
                compareSelectedButton,
                clearSelectionButton
,
                memoryUnitSelect        );
        filters.setWidthFull();
        filters.setAlignItems(Alignment.END);
        filters.getStyle().set("flex-wrap", "wrap");

        add(title, description, filters, compareSelectionSummary, summary, reportsGrid);

        refreshReports();
    }

    private void configureFilters() {
        fileSearchField.setWidth("320px");
        fileSearchField.setPlaceholder("Example: pid-1234 or /reports/");
        fileSearchField.setClearButtonVisible(true);
        fileSearchField.addValueChangeListener(event -> refreshReports());

        fromDateTimePicker.setWidth("260px");
        fromDateTimePicker.setStep(java.time.Duration.ofSeconds(1));

        toDateTimePicker.setWidth("260px");
        toDateTimePicker.setStep(java.time.Duration.ofSeconds(1));
    }
    private void configureMemoryUnitSelect() {
        memoryUnitSelect.setLabel("Memory unit");
        memoryUnitSelect.setItems(MemoryDisplayUnit.values());
        memoryUnitSelect.setValue(MemoryDisplayUnit.AUTO);
        memoryUnitSelect.setWidth("160px");
    }
    private void configureGrid() {
        reportsGrid.setWidthFull();
        reportsGrid.setAllRowsVisible(true);
        reportsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        reportsGrid.addSelectionListener(event -> updateCompareSelectionSummary());
        reportsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        reportsGrid.addComponentColumn(this::createPathText)
                .setHeader("File")
                .setAutoWidth(false)
                .setFlexGrow(3);

        reportsGrid.addColumn(report -> formatSize(report.sizeBytes()))
                .setHeader("Size")
                .setAutoWidth(true);

        reportsGrid.addColumn(report -> DATE_TIME_FORMATTER.format(report.lastModifiedAt()))
                .setHeader("Last modified")
                .setAutoWidth(true);

        reportsGrid.addComponentColumn(this::createPreviewButton)
                .setHeader("Preview")
                .setAutoWidth(true);
        reportsGrid.addComponentColumn(this::createDetailsButton)
                .setHeader("Details")
                .setAutoWidth(true);

        reportsGrid.addComponentColumn(this::createDownloadLink)
                .setHeader("Download")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private com.vaadin.flow.component.html.Span createPathText(LocalArchivedReport report) {
        com.vaadin.flow.component.html.Span path = new com.vaadin.flow.component.html.Span(report.absolutePath());
        path.getElement().setAttribute("title", report.absolutePath());
        path.getStyle()
                .set("display", "block")
                .set("max-width", "100%")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        return path;
    }
    private Button createDetailsButton(LocalArchivedReport report) {
        return new Button("Details", event -> showReportDetails(report));
    }

    private void showReportDetails(LocalArchivedReport report) {
        try {
            JsonNode root = archiveService.readReportJson(report);

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Archived report details");
            dialog.setWidth("min(1000px, 95vw)");
            dialog.setMaxHeight("90vh");

            VerticalLayout content = new VerticalLayout();
            content.setPadding(false);
            content.setSpacing(true);
            content.setWidthFull();

            H2 fileName = new H2(report.fileName());
            fileName.getStyle()
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("margin", "0");

            content.add(fileName);
            content.add(createJsonSection("Metadata", root.path("metadata")));
            content.add(createJsonSection("Session", root.path("session")));
            content.add(createJsonSection("Heap / Metaspace", firstExistingNode(root, "latestHeapInfo", "heapInfo", "heap")));
            content.add(createJsonSection("Uptime", firstExistingNode(root, "latestVmUptime", "uptime")));
            content.add(createJsonSection("Summary", firstExistingNode(root, "sampleSummary", "summary")));
            content.add(createJsonSection("Diagnostics", firstExistingNode(root, "diagnostics", "warnings")));
            content.add(createJsonSection("Samples", firstExistingNode(root, "recentSamples", "samples")));

            Scroller scroller = new Scroller(content);
            scroller.setWidthFull();
            scroller.setMaxHeight("70vh");

            Button closeButton = new Button("Close", event -> dialog.close());
            HorizontalLayout footer = new HorizontalLayout(closeButton);
            footer.setWidthFull();
            footer.setJustifyContentMode(JustifyContentMode.END);

            dialog.add(scroller, footer);
            dialog.open();
        } catch (RuntimeException exception) {
            Notification.show("Unable to load report details: " + exception.getMessage());
        }
    }

    private VerticalLayout createJsonSection(String title, JsonNode node) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();

        H3 heading = new H3(title);
        heading.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("margin-bottom", "var(--lumo-space-xs)");

        Pre value = new Pre(formatJsonNode(node));
        value.getStyle()
                .set("width", "100%")
                .set("max-height", "260px")
                .set("overflow", "auto")
                .set("white-space", "pre-wrap")
                .set("word-break", "break-word")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");

        section.add(heading, value);

        return section;
    }

    private String formatJsonNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "Unavailable";
        }

        return node.toPrettyString();
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
    private Anchor createDownloadLink(LocalArchivedReport report) {
        StreamResource resource = new StreamResource(report.fileName(), () -> {
            try {
                return Files.newInputStream(Path.of(report.absolutePath()));
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to open archived report", exception);
            }
        });

        Anchor link = new Anchor(resource, "");
        Icon downloadIcon = new Icon(VaadinIcon.DOWNLOAD);
        downloadIcon.setSize("18px");
        link.add(downloadIcon);

        link.getElement().setAttribute("download", true);
        link.getElement().setAttribute("title", "Download " + report.fileName());
        link.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("cursor", "pointer")
                .set("text-decoration", "none")
                .set("color", "var(--lumo-primary-text-color)");

        return link;
    }

    private void clearFilters() {
        fileSearchField.clear();
        fromDateTimePicker.clear();
        toDateTimePicker.clear();
        refreshReports();
    }

    private List<LocalArchivedReport> applyFilters(List<LocalArchivedReport> reports) {
        String query = fileSearchField.getValue();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Instant from = toInstant(fromDateTimePicker.getValue());
        Instant to = toInstant(toDateTimePicker.getValue());

        return reports.stream()
                .filter(report -> matchesQuery(report, normalizedQuery))
                .filter(report -> from == null || !report.lastModifiedAt().isBefore(from))
                .filter(report -> to == null || !report.lastModifiedAt().isAfter(to))
                .toList();
    }

    private boolean matchesQuery(LocalArchivedReport report, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }

        return report.fileName().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || report.absolutePath().toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return localDateTime.atZone(LOCAL_ZONE).toInstant();
    }
    private void clearReportSelection() {
        reportsGrid.deselectAll();
        updateCompareSelectionSummary();
    }

    private void updateCompareSelectionSummary() {
        int selectedCount = reportsGrid.getSelectedItems().size();
        if (selectedCount == 0) {
            compareSelectionSummary.setText("Select exactly two reports to compare.");
        } else if (selectedCount == 1) {
            compareSelectionSummary.setText("One report selected. Select one more report to compare.");
        } else if (selectedCount == 2) {
            compareSelectionSummary.setText("Two reports selected. Ready to compare.");
        } else {
            compareSelectionSummary.setText(selectedCount + " reports selected. Keep exactly two selected to compare.");
        }
    }
    private void compareSelectedReports() {
        List<LocalArchivedReport> selectedReports = new ArrayList<>(reportsGrid.getSelectedItems());
        selectedReports.sort(Comparator.comparing(LocalArchivedReport::lastModifiedAt));

        if (selectedReports.size() != 2) {
            Notification.show("Select exactly two archived reports to compare.");
            return;
        }

        showReportComparison(selectedReports.get(0), selectedReports.get(1));
    }

    private void showReportComparison(LocalArchivedReport olderReport, LocalArchivedReport newerReport) {
        try {
            JsonNode older = archiveService.readReportJson(olderReport);
            JsonNode newer = archiveService.readReportJson(newerReport);

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Archived report comparison");
            dialog.setWidth("min(1000px, 95vw)");
            dialog.setMaxHeight("90vh");

            VerticalLayout content = new VerticalLayout();
            content.setPadding(false);
            content.setSpacing(true);
            content.setWidthFull();

            content.add(new Span("Older: " + olderReport.absolutePath()));
            content.add(new Span("Newer: " + newerReport.absolutePath()));
            content.add(createComparisonContextSection(olderReport, newerReport, older, newer));
            content.add(createInterpretationSection(older, newer));
            content.add(createComparisonSection("Generated at", textValue(older, "generatedAt"), textValue(newer, "generatedAt")));
            content.add(createComparisonSection("PID", textValue(older.path("session"), "pid"), textValue(newer.path("session"), "pid")));
            content.add(createComparisonSection("Application", firstTextValue(firstExistingNode(older, "session"), "applicationName", "displayName", "command"), firstTextValue(firstExistingNode(newer, "session"), "applicationName", "displayName", "command")));
            content.add(createMemoryComparisonSection("Heap used", firstMemoryValueAsBytes(firstExistingNode(older, "latestHeapInfo", "heapInfo", "heap"), "heapUsedKb", "usedKb", "usedBytes", "heapUsedBytes", "heapUsed", "used"), firstMemoryValueAsBytes(firstExistingNode(newer, "latestHeapInfo", "heapInfo", "heap"), "heapUsedKb", "usedKb", "usedBytes", "heapUsedBytes", "heapUsed", "used")));
            content.add(createMemoryComparisonSection("Heap total", firstMemoryValueAsBytes(firstExistingNode(older, "latestHeapInfo", "heapInfo", "heap"), "heapTotalKb", "totalKb", "totalBytes", "committedBytes", "heapCommittedBytes", "heapCommitted", "committed"), firstMemoryValueAsBytes(firstExistingNode(newer, "latestHeapInfo", "heapInfo", "heap"), "heapTotalKb", "totalKb", "totalBytes", "committedBytes", "heapCommittedBytes", "heapCommitted", "committed")));
            content.add(createMemoryComparisonSection("Metaspace used", firstMemoryValueAsBytes(firstExistingNode(older, "latestHeapInfo", "heapInfo", "heap"), "metaspaceUsedKb", "metaspaceUsedBytes", "metaspaceUsed", "usedMetaspaceBytes"), firstMemoryValueAsBytes(firstExistingNode(newer, "latestHeapInfo", "heapInfo", "heap"), "metaspaceUsedKb", "metaspaceUsedBytes", "metaspaceUsed", "usedMetaspaceBytes")));
            content.add(createNumericComparisonSection("Uptime", firstAvailable(firstExistingNode(older, "latestVmUptime", "uptime"), "uptimeSeconds", "seconds", "displayValue"), firstAvailable(firstExistingNode(newer, "latestVmUptime", "uptime"), "uptimeSeconds", "seconds", "displayValue")));
            content.add(createNumericComparisonSection("Diagnostics", String.valueOf(arraySize(firstExistingNode(older, "diagnostics", "warnings"))), String.valueOf(arraySize(firstExistingNode(newer, "diagnostics", "warnings")))));
            content.add(createNumericComparisonSection("Diagnostics INFO", String.valueOf(countDiagnosticsBySeverity(firstExistingNode(older, "diagnostics", "warnings"), "INFO")), String.valueOf(countDiagnosticsBySeverity(firstExistingNode(newer, "diagnostics", "warnings"), "INFO"))));
            content.add(createNumericComparisonSection("Diagnostics WARNING", String.valueOf(countDiagnosticsBySeverity(firstExistingNode(older, "diagnostics", "warnings"), "WARNING")), String.valueOf(countDiagnosticsBySeverity(firstExistingNode(newer, "diagnostics", "warnings"), "WARNING"))));
            content.add(createNumericComparisonSection("Diagnostics ERROR", String.valueOf(countDiagnosticsBySeverity(firstExistingNode(older, "diagnostics", "warnings"), "ERROR")), String.valueOf(countDiagnosticsBySeverity(firstExistingNode(newer, "diagnostics", "warnings"), "ERROR"))));
            content.add(createNumericComparisonSection("Samples", String.valueOf(arraySize(firstExistingNode(older, "recentSamples", "samples"))), String.valueOf(arraySize(firstExistingNode(newer, "recentSamples", "samples")))));
            content.add(createJsonSection("Older diagnostics", firstExistingNode(older, "diagnostics", "warnings")));
            content.add(createJsonSection("Newer diagnostics", firstExistingNode(newer, "diagnostics", "warnings")));

            Scroller scroller = new Scroller(content);
            scroller.setWidthFull();
            scroller.setMaxHeight("70vh");

            Button closeButton = new Button("Close", event -> dialog.close());
            HorizontalLayout footer = new HorizontalLayout(closeButton);
            footer.setWidthFull();
            footer.setJustifyContentMode(JustifyContentMode.END);

            dialog.add(scroller, footer);
            dialog.open();
        } catch (RuntimeException exception) {
            Notification.show("Unable to compare reports: " + exception.getMessage());
        }
    }

    private VerticalLayout createComparisonContextSection(
            LocalArchivedReport olderReport,
            LocalArchivedReport newerReport,
            JsonNode older,
            JsonNode newer
    ) {
        String context = "Memory unit: " + memoryUnitSelect.getValue()
                + System.lineSeparator()
                + "Older file: " + olderReport.fileName()
                + System.lineSeparator()
                + "Newer file: " + newerReport.fileName()
                + System.lineSeparator()
                + "Older generated at: " + firstTextValue(firstExistingNode(older, "metadata"), "generatedAt")
                + System.lineSeparator()
                + "Newer generated at: " + firstTextValue(firstExistingNode(newer, "metadata"), "generatedAt")
                + System.lineSeparator()
                + "Older application: " + firstTextValue(firstExistingNode(older, "session"), "applicationName")
                + System.lineSeparator()
                + "Newer application: " + firstTextValue(firstExistingNode(newer, "session"), "applicationName");

        return createTextSection("Comparison context", context);
    }

    private VerticalLayout createTextSection(String title, String contentText) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();

        H3 heading = new H3(title);
        heading.getStyle().set("font-size", "var(--lumo-font-size-m)").set("margin-bottom", "var(--lumo-space-xs)");

        Pre value = new Pre(contentText);
        value.getStyle()
                .set("width", "100%")
                .set("overflow", "auto")
                .set("white-space", "pre-wrap")
                .set("word-break", "break-word")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");

        section.add(heading, value);
        return section;
    }
    private VerticalLayout createInterpretationSection(JsonNode older, JsonNode newer) {
        StringBuilder interpretation = new StringBuilder();
        interpretation.append(interpretMemoryDelta("Heap used", firstMemoryValueAsBytes(firstExistingNode(older, "latestHeapInfo", "heapInfo", "heap"), "heapUsedKb", "usedKb", "usedBytes", "heapUsedBytes", "heapUsed", "used"), firstMemoryValueAsBytes(firstExistingNode(newer, "latestHeapInfo", "heapInfo", "heap"), "heapUsedKb", "usedKb", "usedBytes", "heapUsedBytes", "heapUsed", "used"))).append(System.lineSeparator());
        interpretation.append(interpretMemoryDelta("Heap total", firstMemoryValueAsBytes(firstExistingNode(older, "latestHeapInfo", "heapInfo", "heap"), "heapTotalKb", "totalKb", "totalBytes", "committedBytes", "heapCommittedBytes", "heapCommitted", "committed"), firstMemoryValueAsBytes(firstExistingNode(newer, "latestHeapInfo", "heapInfo", "heap"), "heapTotalKb", "totalKb", "totalBytes", "committedBytes", "heapCommittedBytes", "heapCommitted", "committed"))).append(System.lineSeparator());
        interpretation.append(interpretMemoryDelta("Metaspace used", firstMemoryValueAsBytes(firstExistingNode(older, "latestHeapInfo", "heapInfo", "heap"), "metaspaceUsedKb", "metaspaceUsedBytes", "metaspaceUsed", "usedMetaspaceBytes"), firstMemoryValueAsBytes(firstExistingNode(newer, "latestHeapInfo", "heapInfo", "heap"), "metaspaceUsedKb", "metaspaceUsedBytes", "metaspaceUsed", "usedMetaspaceBytes"))).append(System.lineSeparator());
        interpretation.append(interpretNumericDelta("Diagnostics", String.valueOf(arraySize(firstExistingNode(older, "diagnostics", "warnings"))), String.valueOf(arraySize(firstExistingNode(newer, "diagnostics", "warnings"))))).append(System.lineSeparator());
        interpretation.append(interpretNumericDelta("Samples", String.valueOf(arraySize(firstExistingNode(older, "recentSamples", "samples"))), String.valueOf(arraySize(firstExistingNode(newer, "recentSamples", "samples")))));

        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();

        H3 heading = new H3("Interpretation");
        heading.getStyle().set("font-size", "var(--lumo-font-size-m)").set("margin-bottom", "var(--lumo-space-xs)");

        Pre value = new Pre(interpretation.toString());
        value.getStyle()
                .set("width", "100%")
                .set("overflow", "auto")
                .set("white-space", "pre-wrap")
                .set("word-break", "break-word")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");

        section.add(heading, value);
        return section;
    }

    private String interpretNumericDelta(String label, String olderValue, String newerValue) {
        Long olderNumber = parseLongValue(olderValue);
        Long newerNumber = parseLongValue(newerValue);

        if (olderNumber == null || newerNumber == null) {
            return label + ": unavailable for comparison";
        }

        long delta = newerNumber - olderNumber;
        if (delta > 0) {
            return label + " increased by " + delta + ".";
        }
        if (delta < 0) {
            return label + " decreased by " + Math.abs(delta) + ".";
        }
        return label + " remained stable.";
    }
    private String firstMemoryValueAsBytes(JsonNode node, String... candidateFields) {
        for (String candidateField : candidateFields) {
            JsonNode value = node.path(candidateField);
            if (value == null || value.isMissingNode() || value.isNull()) {
                continue;
            }

            Long number = jsonNodeToLong(value);
            if (number == null) {
                continue;
            }

            if (candidateField.toLowerCase(java.util.Locale.ROOT).endsWith("kb")) {
                return String.valueOf(number * 1024L);
            }

            return String.valueOf(number);
        }

        return "Unavailable";
    }

    private Long jsonNodeToLong(JsonNode value) {
        if (value.isNumber()) {
            return value.asLong();
        }

        if (value.isTextual()) {
            try {
                return Long.parseLong(value.asText().replaceAll("[^0-9-]", ""));
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }

    private String interpretMemoryDelta(String label, String olderValue, String newerValue) {
        Long olderNumber = parseLongValue(olderValue);
        Long newerNumber = parseLongValue(newerValue);

        if (olderNumber == null || newerNumber == null) {
            return label + ": unavailable for comparison";
        }

        long delta = newerNumber - olderNumber;
        if (delta > 0) {
            return label + " increased by " + formatBytes(delta, memoryUnitSelect.getValue()) + ".";
        }
        if (delta < 0) {
            return label + " decreased by " + formatBytes(Math.abs(delta), memoryUnitSelect.getValue()) + ".";
        }
        return label + " remained stable.";
    }
    private VerticalLayout createMemoryComparisonSection(String label, String olderValue, String newerValue) {
        Long olderNumber = parseLongValue(olderValue);
        Long newerNumber = parseLongValue(newerValue);

        if (olderNumber == null || newerNumber == null) {
            return createNumericComparisonSection(label, olderValue, newerValue);
        }

        long delta = newerNumber - olderNumber;
        return createComparisonSection(
                label,
                formatBytes(olderNumber, memoryUnitSelect.getValue()),
                formatBytes(newerNumber, memoryUnitSelect.getValue()),
                formatSignedBytes(delta, memoryUnitSelect.getValue())
        );
    }

    private String formatSignedBytes(long bytes, MemoryDisplayUnit unit) {
        String sign = bytes >= 0 ? "+" : "";
        return sign + formatBytes(bytes, unit);
    }

    private String formatBytes(long bytes, MemoryDisplayUnit unit) {
        MemoryDisplayUnit selectedUnit = unit == null ? MemoryDisplayUnit.AUTO : unit;
        return switch (selectedUnit) {
            case AUTO -> formatBytesUsingAutoUnit(bytes);
            case BYTES -> bytes + " B";
            case KB -> formatDecimal(bytes / 1024.0) + " KB (" + bytes + " bytes)";
            case MB -> formatDecimal(bytes / (1024.0 * 1024.0)) + " MB (" + bytes + " bytes)";
            case GB -> formatDecimal(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB (" + bytes + " bytes)";
        };
    }

    private String formatBytesUsingAutoUnit(long bytes) {
        long absoluteBytes = Math.abs(bytes);

        if (absoluteBytes < 1024L) {
            return bytes + " B";
        }

        if (absoluteBytes < 1024L * 1024L) {
            return formatDecimal(bytes / 1024.0) + " KB (" + bytes + " bytes)";
        }

        if (absoluteBytes < 1024L * 1024L * 1024L) {
            return formatDecimal(bytes / (1024.0 * 1024.0)) + " MB (" + bytes + " bytes)";
        }

        return formatDecimal(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB (" + bytes + " bytes)";
    }

    private String formatDecimal(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.01) {
            return String.format(java.util.Locale.ROOT, "%.0f", value);
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
    private VerticalLayout createNumericComparisonSection(String label, String olderValue, String newerValue) {
        Long olderNumber = parseLongValue(olderValue);
        Long newerNumber = parseLongValue(newerValue);

        if (olderNumber == null || newerNumber == null) {
            return createComparisonSection(label, olderValue, newerValue);
        }

        long delta = newerNumber - olderNumber;
        String deltaText = delta >= 0 ? "+" + delta : String.valueOf(delta);
        return createComparisonSection(label, olderValue, newerValue, deltaText);
    }

    private Long parseLongValue(String value) {
        if (value == null || value.isBlank() || "Unavailable".equals(value)) {
            return null;
        }

        try {
            return Long.parseLong(value.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
    private VerticalLayout createComparisonSection(String label, String olderValue, String newerValue) {
        return createComparisonSection(label, olderValue, newerValue, "Unavailable");
    }

    private VerticalLayout createComparisonSection(String label, String olderValue, String newerValue, String deltaValue) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();

        H3 heading = new H3(label);
        heading.getStyle().set("font-size", "var(--lumo-font-size-m)").set("margin-bottom", "var(--lumo-space-xs)");

        Pre values = new Pre("Older: " + olderValue + System.lineSeparator() + "Newer: " + newerValue + System.lineSeparator() + "Delta: " + deltaValue);
        values.getStyle()
                .set("width", "100%")
                .set("overflow", "auto")
                .set("white-space", "pre-wrap")
                .set("word-break", "break-word")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");

        section.add(heading, values);
        return section;
    }

    private JsonNode firstExistingNode(JsonNode root, String... candidateFields) {
        for (String candidateField : candidateFields) {
            JsonNode value = root.path(candidateField);
            if (value != null && !value.isMissingNode() && !value.isNull()) {
                return value;
            }
        }

        return com.fasterxml.jackson.databind.node.MissingNode.getInstance();
    }

    private String firstAvailable(JsonNode node, String... candidateFields) {
        for (String candidateField : candidateFields) {
            String value = textValue(node, candidateField);
            if (!"Unavailable".equals(value)) {
                return value;
            }
        }

        return "Unavailable";
    }

    private String firstTextValue(JsonNode node, String... candidateFields) {
        return firstAvailable(node, candidateFields);
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return "Unavailable";
        }
        return value.isTextual() ? value.asText() : value.toString();
    }

    private int countDiagnosticsBySeverity(JsonNode diagnosticsNode, String severity) {
        if (diagnosticsNode == null || !diagnosticsNode.isArray()) {
            return 0;
        }

        int count = 0;
        for (JsonNode diagnostic : diagnosticsNode) {
            String diagnosticSeverity = firstTextValue(diagnostic, "severity");
            if (severity.equalsIgnoreCase(diagnosticSeverity)) {
                count++;
            }
        }

        return count;
    }
    private int arraySize(JsonNode node) {
        return node != null && node.isArray() ? node.size() : 0;
    }
    private void refreshReports() {
        try {
            List<LocalArchivedReport> reports = archiveService.listReports();
            List<LocalArchivedReport> filteredReports = applyFilters(reports);

            reportsGrid.setItems(filteredReports);
            reportsGrid.deselectAll();
            updateCompareSelectionSummary();
            summary.setText("Archived reports: " + filteredReports.size() + " of " + reports.size());
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
