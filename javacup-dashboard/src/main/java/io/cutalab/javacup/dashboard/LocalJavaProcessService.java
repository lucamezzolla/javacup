package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.process.JavaProcessInfo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LocalJavaProcessService {

    private final long currentPid = ProcessHandle.current().pid();

    public List<JavaProcessInfo> findJavaProcesses() {
        return ProcessHandle.allProcesses()
                .flatMap(process -> toJavaProcessInfo(process).stream())
                .sorted(Comparator
                        .comparing(JavaProcessInfo::currentProcess).reversed()
                        .thenComparing(JavaProcessInfo::applicationName)
                        .thenComparingLong(JavaProcessInfo::pid))
                .toList();
    }

    public Optional<JavaProcessInfo> findJavaProcessByPid(long pid) {
        return ProcessHandle.of(pid)
                .flatMap(this::toJavaProcessInfo);
    }

    private Optional<JavaProcessInfo> toJavaProcessInfo(ProcessHandle process) {
        ProcessHandle.Info info = process.info();
        Optional<String> command = info.command();

        if (command.isEmpty() || !looksLikeJavaCommand(command.get())) {
            return Optional.empty();
        }

        return Optional.of(new JavaProcessInfo(
                process.pid(),
                command.orElse(""),
                info.arguments().map(List::of).orElseGet(List::of),
                process.pid() == currentPid
        ));
    }

    private boolean looksLikeJavaCommand(String command) {
        String normalized = command.toLowerCase();
        return normalized.endsWith("/java")
                || normalized.endsWith("\\java.exe")
                || normalized.endsWith("/java.exe")
                || normalized.equals("java")
                || normalized.equals("java.exe")
                || normalized.contains("/java-")
                || normalized.contains("\\java-")
                || normalized.endsWith("/javaw")
                || normalized.endsWith("\\javaw.exe");
    }
}
