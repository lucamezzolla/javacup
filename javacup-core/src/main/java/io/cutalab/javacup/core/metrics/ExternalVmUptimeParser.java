package io.cutalab.javacup.core.metrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalVmUptimeParser {

    private static final Pattern UPTIME_SECONDS = Pattern.compile(".*?(\\d+)(?:[\\.,](\\d+))?\\s*s\\s*$", Pattern.CASE_INSENSITIVE);

    public ExternalVmUptime parse(String output) {
        String safeOutput = output == null ? "" : output;
        Long uptimeSeconds = null;

        String[] lines = safeOutput.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isBlank() || trimmed.endsWith(":")) {
                continue;
            }

            Matcher matcher = UPTIME_SECONDS.matcher(trimmed);

            if (matcher.matches()) {
                uptimeSeconds = parseLong(matcher.group(1));
            }
        }

        return new ExternalVmUptime(uptimeSeconds, safeOutput);
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
