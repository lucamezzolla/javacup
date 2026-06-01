package io.cutalab.javacup.core.process;

public record ProcessProbeResult(
        long pid,
        boolean successful,
        String command,
        String output,
        String error
) {

    public static ProcessProbeResult success(long pid, String command, String output) {
        return new ProcessProbeResult(pid, true, command, output, "");
    }

    public static ProcessProbeResult failure(long pid, String command, String error) {
        return new ProcessProbeResult(pid, false, command, "", error);
    }

    public String displayText() {
        StringBuilder builder = new StringBuilder();

        builder.append("PID: ").append(pid).append(System.lineSeparator());
        builder.append("Command: ").append(command).append(System.lineSeparator());
        builder.append("Status: ").append(successful ? "OK" : "FAILED").append(System.lineSeparator());
        builder.append(System.lineSeparator());

        if (successful) {
            builder.append(output == null || output.isBlank() ? "(no output)" : output);
        } else {
            builder.append(error == null || error.isBlank() ? "(no error details)" : error);
        }

        return builder.toString();
    }
}
