package io.cutalab.javacup.core.session;

import java.time.Instant;
import java.util.UUID;

public record ExternalMetricSample(
        UUID sessionId,
        long pid,
        Instant timestamp,
        Long heapUsedMb,
        Long heapTotalMb,
        Long metaspaceUsedMb,
        Long classSpaceUsedMb
) {
}
