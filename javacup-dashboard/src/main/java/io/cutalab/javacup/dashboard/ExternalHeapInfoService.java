package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.metrics.ExternalHeapInfo;
import io.cutalab.javacup.core.metrics.ExternalHeapInfoParser;
import io.cutalab.javacup.core.process.ProcessProbeResult;
import org.springframework.stereotype.Service;

@Service
public class ExternalHeapInfoService {

    private final ExternalProcessProbeService probeService;
    private final ExternalHeapInfoParser parser = new ExternalHeapInfoParser();

    public ExternalHeapInfoService(ExternalProcessProbeService probeService) {
        this.probeService = probeService;
    }

    public ExternalHeapInfo readHeapInfo(long pid) {
        ProcessProbeResult result = probeService.probeHeapInfo(pid);

        if (!result.successful()) {
            return parser.parse(result.displayText());
        }

        return parser.parse(result.output());
    }
}
