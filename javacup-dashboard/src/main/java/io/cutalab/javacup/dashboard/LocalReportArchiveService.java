package io.cutalab.javacup.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.cutalab.javacup.core.report.ExternalMonitoringReport;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class LocalReportArchiveService {

    private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ObjectMapper objectMapper;
    private final Path archiveDirectory;

    public LocalReportArchiveService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.archiveDirectory = Path.of(System.getProperty("user.home"), ".javacup", "reports");
    }

    public Path archive(ExternalMonitoringReport report) {
        try {
            Files.createDirectories(archiveDirectory);

            String timestamp = FILE_TIMESTAMP_FORMATTER.withZone(ZoneId.systemDefault())
                    .format(report.generatedAt());

            String fileName = "external-report-pid-" + report.session().pid() + "-" + timestamp + ".json";
            Path target = archiveDirectory.resolve(fileName);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), report);

            return target;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to archive external monitoring report", exception);
        }
    }

    public List<LocalArchivedReport> listReports() {
        if (!Files.exists(archiveDirectory)) {
            return List.of();
        }

        try (var paths = Files.list(archiveDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(this::toArchivedReport)
                    .sorted(Comparator.comparing(LocalArchivedReport::lastModifiedAt).reversed())
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to list archived reports", exception);
        }
    }

    public String readReportText(LocalArchivedReport report) {
        try {
            Path path = Path.of(report.absolutePath());

            if (!path.normalize().startsWith(archiveDirectory.normalize())) {
                throw new IllegalArgumentException("Report path is outside the archive directory");
            }

            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read archived report", exception);
        }
    }
    public Path archiveDirectory() {
        return archiveDirectory;
    }

    private LocalArchivedReport toArchivedReport(Path path) {
        try {
            return new LocalArchivedReport(
                    path.getFileName().toString(),
                    path.toAbsolutePath().toString(),
                    Files.size(path),
                    Files.getLastModifiedTime(path).toInstant()
            );
        } catch (IOException exception) {
            return new LocalArchivedReport(
                    path.getFileName().toString(),
                    path.toAbsolutePath().toString(),
                    -1L,
                    Instant.EPOCH
            );
        }
    }
}
