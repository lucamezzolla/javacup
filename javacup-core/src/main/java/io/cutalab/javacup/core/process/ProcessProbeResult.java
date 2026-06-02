package io.cutalab.javacup.core.process;

import java.util.Locale;

public record ProcessProbeResult(
        long pid,
        boolean successful,
        ProbeStatus probeStatus,
        ProbeFailureKind failureKind,
        String command,
        String output,
        String error) {

    public static ProcessProbeResult success(long pid, String command, String output) {
        return new ProcessProbeResult(pid, true, ProbeStatus.OK, ProbeFailureKind.NONE, command, output, "");
    }

    public static ProcessProbeResult failure(long pid, String command, String error) {
        return failure(pid, command, classifyFailure(error), error);
    }

    public static ProcessProbeResult failure(long pid, String command, ProbeFailureKind failureKind, String error) {
        return new ProcessProbeResult(pid, false, ProbeStatus.FAILED, failureKind, command, "", error);
    }

    public String displayText() {
        StringBuilder builder = new StringBuilder();
        builder.append("PID: ").append(pid).append(System.lineSeparator());
        builder.append("Command: ").append(command).append(System.lineSeparator());
        builder.append("Status: ").append(successful ? "OK" : "FAILED").append(System.lineSeparator());

        if (!successful) {
            builder.append("Failure kind: ").append(failureKind).append(System.lineSeparator());
        }

        builder.append(System.lineSeparator());

        if (successful) {
            builder.append(output == null || output.isBlank() ? "(no output)" : output);
        } else {
            builder.append(error == null || error.isBlank() ? "(no error details)" : error);
        }

        return builder.toString();
    }

    private static ProbeFailureKind classifyFailure(String error) {
        String normalized = error == null ? "" : error.toLowerCase(Locale.ROOT);

        if (normalized.contains("timed out") || normalized.contains("timeout")) {
            return ProbeFailureKind.TIMEOUT;
        }

        if (normalized.contains("no such process")
                || normalized.contains("process not found")
                || normalized.contains("nessun processo corrisponde")) {
            return ProbeFailureKind.PROCESS_NOT_FOUND;
        }

        if (normalized.contains("cannot run program")
                || normalized.contains("no such file")
                || normalized.contains("error=2")) {
            return ProbeFailureKind.JCMD_UNAVAILABLE;
        }

        if (normalized.contains("permission")
                || normalized.contains("operation not permitted")
                || normalized.contains("access denied")
                || normalized.contains("attachnotsupported")
                || normalized.contains("attach not supported")) {
            return ProbeFailureKind.ATTACH_FAILED;
        }

        if (normalized.contains("interrupted")) {
            return ProbeFailureKind.INTERRUPTED;
        }

        return ProbeFailureKind.UNKNOWN;
    }
}
