package io.cutalab.javacup.core.report;

import java.time.Instant;

public record ExternalMonitoringReportMetadata(
        String applicationName,
        String applicationVersion,
        String projectUrl,
        Instant generatedAt
) {
}
