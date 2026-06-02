package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.metrics.ExternalVmUptime;
import io.cutalab.javacup.core.metrics.ExternalVmUptimeParser;
import io.cutalab.javacup.core.process.ProcessProbeResult;
import org.springframework.stereotype.Service;

@Service
public class ExternalVmUptimeService {

    private final ExternalProcessProbeService probeService;
    private final ExternalVmUptimeParser parser = new ExternalVmUptimeParser();

    public ExternalVmUptimeService(ExternalProcessProbeService probeService) {
        this.probeService = probeService;
    }

    public ExternalVmUptime readUptime(long pid) {
        ProcessProbeResult result = probeService.probeVmUptime(pid);
        ExternalVmUptime uptime = parser.parse(result.successful() ? result.output() : result.displayText());
        return uptime.withProbeMetadata(result.probeStatus().name(), result.failureKind().name());
    }
}
