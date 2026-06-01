package io.cutalab.javacup.core.process;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record JavaProcessInfo(
        long pid,
        String command,
        List<String> arguments,
        boolean currentProcess
) {

    public String processType() {
        return currentProcess ? "Self" : "Java application";
    }

    public String displayName() {
        if (command == null || command.isBlank()) {
            return "Unknown Java process";
        }

        int lastSeparator = Math.max(command.lastIndexOf('/'), command.lastIndexOf('\\'));
        return lastSeparator >= 0 ? command.substring(lastSeparator + 1) : command;
    }

    public String applicationName() {
        Optional<String> jar = findJarArgument();

        if (jar.isPresent()) {
            return fileName(jar.get());
        }

        Optional<String> classpathMain = findMainClassOrModule();

        return classpathMain.orElse(displayName());
    }

    public String argumentsAsText() {
        if (arguments == null || arguments.isEmpty()) {
            return "";
        }

        return String.join(" ", arguments);
    }

    public String commandLine() {
        String args = argumentsAsText();

        if (args.isBlank()) {
            return command == null ? "" : command;
        }

        return (command == null ? "" : command) + " " + args;
    }

    public boolean matches(String filterText) {
        if (filterText == null || filterText.isBlank()) {
            return true;
        }

        String normalizedFilter = filterText.toLowerCase(Locale.ROOT);

        return String.valueOf(pid).contains(normalizedFilter)
                || safeLower(command).contains(normalizedFilter)
                || safeLower(argumentsAsText()).contains(normalizedFilter)
                || safeLower(applicationName()).contains(normalizedFilter)
                || safeLower(processType()).contains(normalizedFilter);
    }

    private Optional<String> findJarArgument() {
        if (arguments == null || arguments.isEmpty()) {
            return Optional.empty();
        }

        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);

            if ("-jar".equals(argument) && i + 1 < arguments.size()) {
                return Optional.of(arguments.get(i + 1));
            }

            if (argument != null && argument.toLowerCase(Locale.ROOT).endsWith(".jar")) {
                return Optional.of(argument);
            }
        }

        return Optional.empty();
    }

    private Optional<String> findMainClassOrModule() {
        if (arguments == null || arguments.isEmpty()) {
            return Optional.empty();
        }

        boolean skipNext = false;

        for (String argument : arguments) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            if (argument == null || argument.isBlank()) {
                continue;
            }

            if ("-jar".equals(argument) || "-cp".equals(argument) || "-classpath".equals(argument) || "--class-path".equals(argument)) {
                skipNext = true;
                continue;
            }

            if (argument.startsWith("-")) {
                continue;
            }

            return Optional.of(argument);
        }

        return Optional.empty();
    }

    private String fileName(String path) {
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSeparator >= 0 ? path.substring(lastSeparator + 1) : path;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
