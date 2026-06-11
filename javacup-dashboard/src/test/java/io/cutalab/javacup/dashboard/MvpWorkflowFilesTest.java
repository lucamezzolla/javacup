package io.cutalab.javacup.dashboard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MvpWorkflowFilesTest {

    @Test
    void mvpWorkflowScriptsExist() {
        assertTrue(existsFromModuleOrRoot("scripts/run-dashboard.sh"));
        assertTrue(existsFromModuleOrRoot("scripts/verify-mvp.sh"));
    }

    @Test
    void externalMetricsContainsCoreMvpActions() throws Exception {
        Path sourcePath = firstExisting(
                "src/main/java/io/cutalab/javacup/dashboard/views/ExternalProcessMetricsView.java",
                "javacup-dashboard/src/main/java/io/cutalab/javacup/dashboard/views/ExternalProcessMetricsView.java"
        );

        String source = Files.readString(sourcePath);

        assertTrue(source.contains("Refresh metrics"));
        assertTrue(source.contains("Preview report"));
        assertTrue(source.contains("Archive JSON report"));
        assertTrue(source.contains("Open archived reports"));
        assertTrue(source.contains("Session health"));
    }

    private boolean existsFromModuleOrRoot(String relativePath) {
        return Files.exists(Path.of(relativePath)) || Files.exists(Path.of("..", relativePath));
    }

    private Path firstExisting(String modulePath, String rootPath) {
        Path first = Path.of(modulePath);

        if (Files.exists(first)) {
            return first;
        }

        Path second = Path.of(rootPath);

        if (Files.exists(second)) {
            return second;
        }

        throw new IllegalStateException("Could not find " + modulePath + " or " + rootPath);
    }
}
