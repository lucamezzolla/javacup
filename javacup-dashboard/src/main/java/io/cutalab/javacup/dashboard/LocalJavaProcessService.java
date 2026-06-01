package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.process.JavaProcessInfo;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LocalJavaProcessService {

    public List<JavaProcessInfo> findJavaProcesses() {
        return ProcessHandle.allProcesses()
                .map(ProcessHandle::info)
                .flatMap(info -> toJavaProcessInfo(info).stream())
                .sorted(Comparator.comparingLong(JavaProcessInfo::pid))
                .toList();
    }

    private Optional<JavaProcessInfo> toJavaProcessInfo(ProcessHandle.Info info) {
        Optional<String> command = info.command();

        if (command.isEmpty() || !looksLikeJavaCommand(command.get())) {
            return Optional.empty();
        }

        return Optional.of(new JavaProcessInfo(
                findPid(info),
                command.orElse(""),
                info.arguments().map(List::of).orElseGet(List::of)
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
                || normalized.contains("\\java-");
    }

    private long findPid(ProcessHandle.Info info) {
        return ProcessHandle.allProcesses()
                .filter(process -> process.info().equals(info))
                .findFirst()
                .map(ProcessHandle::pid)
                .orElse(-1L);
    }
}
