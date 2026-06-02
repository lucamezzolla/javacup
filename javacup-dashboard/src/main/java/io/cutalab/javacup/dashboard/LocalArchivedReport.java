package io.cutalab.javacup.dashboard;

import java.time.Instant;

public record LocalArchivedReport(
        String fileName,
        String absolutePath,
        long sizeBytes,
        Instant lastModifiedAt
) {
}
