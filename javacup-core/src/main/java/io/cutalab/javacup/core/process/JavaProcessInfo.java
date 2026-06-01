package io.cutalab.javacup.core.process;

import java.util.List;

public record JavaProcessInfo(
        long pid,
        String command,
        List<String> arguments
) {

    public String displayName() {
        if (command == null || command.isBlank()) {
            return "Unknown Java process";
        }

        int lastSeparator = Math.max(command.lastIndexOf('/'), command.lastIndexOf('\\'));
        return lastSeparator >= 0 ? command.substring(lastSeparator + 1) : command;
    }

    public String argumentsAsText() {
        if (arguments == null || arguments.isEmpty()) {
            return "";
        }

        return String.join(" ", arguments);
    }
}
