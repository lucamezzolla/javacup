package io.cutalab.javacup.core.session;

import java.time.Instant;
import java.util.UUID;

public record MonitoringSession(
        UUID id,
        long pid,
        String applicationName,
        Instant startedAt,
        Instant lastUpdatedAt,
        MonitoringSessionStatus status
) {

    public MonitoringSession withStatus(MonitoringSessionStatus newStatus, Instant updatedAt) {
        return new MonitoringSession(id, pid, applicationName, startedAt, updatedAt, newStatus);
    }

    public MonitoringSession withApplicationAndStatus(String newApplicationName, MonitoringSessionStatus newStatus, Instant updatedAt) {
        return new MonitoringSession(id, pid, newApplicationName, startedAt, updatedAt, newStatus);
    }

    public MonitoringSession touch(Instant updatedAt) {
        return new MonitoringSession(id, pid, applicationName, startedAt, updatedAt, status);
    }
}
