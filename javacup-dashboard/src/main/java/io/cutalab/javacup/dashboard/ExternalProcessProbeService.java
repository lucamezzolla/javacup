package io.cutalab.javacup.dashboard;

import io.cutalab.javacup.core.process.ProcessProbeResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ExternalProcessProbeService {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public ProcessProbeResult probeVmVersion(long pid) {
        String jcmd = findJcmdExecutable();
        List<String> command = List.of(jcmd, String.valueOf(pid), "VM.version");

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false);

        try {
            Process process = builder.start();

            boolean completed = process.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            if (!completed) {
                process.destroyForcibly();
                return ProcessProbeResult.failure(pid, commandAsText(command), "jcmd timed out after " + TIMEOUT.toSeconds() + " seconds.");
            }

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

            if (process.exitValue() == 0) {
                return ProcessProbeResult.success(pid, commandAsText(command), output);
            }

            return ProcessProbeResult.failure(pid, commandAsText(command), error.isBlank() ? output : error);
        } catch (IOException exception) {
            return ProcessProbeResult.failure(pid, commandAsText(command), exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ProcessProbeResult.failure(pid, commandAsText(command), "Probe interrupted.");
        }
    }

    private String findJcmdExecutable() {
        String javaHome = System.getProperty("java.home");
        String executableName = isWindows() ? "jcmd.exe" : "jcmd";

        return Path.of(javaHome, "bin", executableName).toString();
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private String commandAsText(List<String> command) {
        return String.join(" ", command);
    }
}
