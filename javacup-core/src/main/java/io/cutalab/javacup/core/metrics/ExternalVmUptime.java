package io.cutalab.javacup.core.metrics;

public record ExternalVmUptime(
        Long uptimeSeconds,
        String probeStatus,
        String probeFailureKind,
        String rawOutput
) {
    public ExternalVmUptime(Long uptimeSeconds, String rawOutput) {
        this(uptimeSeconds, "UNKNOWN", "UNKNOWN", rawOutput);
    }

    public ExternalVmUptime withProbeMetadata(String probeStatus, String probeFailureKind) {
        return new ExternalVmUptime(uptimeSeconds, probeStatus, probeFailureKind, rawOutput);
    }



    public boolean hasStructuredValue() {
        return uptimeSeconds != null;
    }

    public String displayValue() {
        if (uptimeSeconds == null) {
            return "unavailable";
        }

        long seconds = uptimeSeconds;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + remainingSeconds + "s";
        }

        if (minutes > 0) {
            return minutes + "m " + remainingSeconds + "s";
        }

        return remainingSeconds + "s";
    }
}
