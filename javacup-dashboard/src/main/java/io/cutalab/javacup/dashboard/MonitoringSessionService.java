package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.process.JavaProcessInfo;
import io.cutalab.javacup.core.session.MonitoringSession;
import io.cutalab.javacup.core.session.MonitoringSessionStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitoringSessionService {

    private final LocalJavaProcessService processService;
    private final Map<Long, MonitoringSession> sessionsByPid = new ConcurrentHashMap<>();

    public MonitoringSessionService(LocalJavaProcessService processService) {
        this.processService = processService;
    }

    public MonitoringSession startOrResume(long pid) {
        Instant now = Instant.now();

        return sessionsByPid.compute(pid, (key, existing) -> {
            Optional<JavaProcessInfo> processInfo = processService.findJavaProcessByPid(pid);

            String applicationName = processInfo
                    .map(JavaProcessInfo::applicationName)
                    .orElse("PID " + pid);

            MonitoringSessionStatus status = processInfo.isPresent()
                    ? MonitoringSessionStatus.ACTIVE
                    : MonitoringSessionStatus.PROCESS_UNAVAILABLE;

            if (existing == null) {
                return new MonitoringSession(UUID.randomUUID(), pid, applicationName, now, now, status);
            }

            return existing.withApplicationAndStatus(applicationName, status, now);
        });
    }

    public MonitoringSession refresh(long pid) {
        MonitoringSession session = startOrResume(pid);

        if (!ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false)) {
            return updateStatus(pid, MonitoringSessionStatus.PROCESS_UNAVAILABLE);
        }

        return session.touch(Instant.now());
    }

    public MonitoringSession stop(long pid) {
        return updateStatus(pid, MonitoringSessionStatus.STOPPED);
    }

    private MonitoringSession updateStatus(long pid, MonitoringSessionStatus status) {
        Instant now = Instant.now();

        return sessionsByPid.compute(pid, (key, existing) -> {
            if (existing == null) {
                return new MonitoringSession(UUID.randomUUID(), pid, "PID " + pid, now, now, status);
            }

            return existing.withStatus(status, now);
        });
    }
}
