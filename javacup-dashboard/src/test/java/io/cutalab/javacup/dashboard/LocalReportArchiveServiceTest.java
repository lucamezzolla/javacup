package io.cutalab.javacup.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cutalab.javacup.core.diagnostics.DiagnosticWarning;
import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.metrics.ExternalVmUptime;
import io.cutalab.javacup.core.report.ExternalMonitoringReport;
import io.cutalab.javacup.core.report.ExternalMonitoringReportMetadata;
import io.cutalab.javacup.core.session.ExternalMetricSample;
import io.cutalab.javacup.core.session.ExternalMetricSampleSummary;
import io.cutalab.javacup.core.session.MonitoringSession;
import io.cutalab.javacup.core.session.MonitoringSessionStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalReportArchiveServiceTest {

    @Test
    void archivesReportAsJsonFile() {
        String originalUserHome = System.getProperty("user.home");

        try {
            Path tempHome = Files.createTempDirectory("javacup-report-archive-test");
            System.setProperty("user.home", tempHome.toString());

            LocalReportArchiveService service = new LocalReportArchiveService(new ObjectMapper());
            Path archived = service.archive(report());

            assertTrue(Files.exists(archived));
            assertTrue(archived.getFileName().toString().startsWith("external-report-pid-1234-"));
            assertTrue(Files.readString(archived).contains("\"metadata\""));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }


    @Test
    void listsArchivedReportsNewestFirst() {
        String originalUserHome = System.getProperty("user.home");

        try {
            Path tempHome = Files.createTempDirectory("javacup-report-list-test");
            System.setProperty("user.home", tempHome.toString());

            LocalReportArchiveService service = new LocalReportArchiveService(new ObjectMapper());

            Path first = service.archive(report());
            Thread.sleep(1100L);
            Path second = service.archive(report());

            List<LocalArchivedReport> reports = service.listReports();

            assertTrue(reports.size() >= 2);
            assertTrue(reports.get(0).lastModifiedAt().compareTo(reports.get(1).lastModifiedAt()) >= 0);
            assertTrue(reports.stream().anyMatch(report -> report.absolutePath().equals(first.toAbsolutePath().toString())));
            assertTrue(reports.stream().anyMatch(report -> report.absolutePath().equals(second.toAbsolutePath().toString())));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void readsArchivedReportText() {
        String originalUserHome = System.getProperty("user.home");

        try {
            Path tempHome = Files.createTempDirectory("javacup-report-read-test");
            System.setProperty("user.home", tempHome.toString());

            LocalReportArchiveService service = new LocalReportArchiveService(new ObjectMapper());
            Path archived = service.archive(report());

            LocalArchivedReport archivedReport = service.listReports().stream()
                    .filter(report -> report.absolutePath().equals(archived.toAbsolutePath().toString()))
                    .findFirst()
                    .orElseThrow();

            String content = service.readReportText(archivedReport);

            assertTrue(content.contains("\"metadata\""));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        } finally {
            System.setProperty("user.home", originalUserHome);
        }
    }
    private ExternalMonitoringReport report() {
        MonitoringSession session = new MonitoringSession(
                UUID.randomUUID(),
                1234L,
                "demo",
                Instant.now(),
                Instant.now(),
                MonitoringSessionStatus.ACTIVE
        );

        ExternalHeapInfo heapInfo = new ExternalHeapInfo(
                "garbage-first heap",
                1024L,
                2048L,
                null,
                512L,
                1024L,
                2048L,
                128L,
                256L,
                512L,
                "raw"
        );

        ExternalVmUptime uptime = new ExternalVmUptime(1L, "raw");

        ExternalMetricSampleSummary summary = new ExternalMetricSampleSummary(
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        return new ExternalMonitoringReport(
                new ExternalMonitoringReportMetadata(
                        "Javacup",
                        "test",
                        "https://github.com/lucamezzolla/javacup",
                        Instant.now()
                ),
                Instant.now(),
                session,
                heapInfo,
                uptime,
                summary,
                List.<DiagnosticWarning>of(),
                List.<ExternalMetricSample>of()
        );
    }
}
