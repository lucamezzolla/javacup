package io.cutalab.javacup.dashboard.views;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProcessMetricsViewSessionHealthSourceTest {

    @Test
    void externalMetricsViewContainsSessionHealthSummary() throws Exception {
        Path sourcePath = Path.of("src/main/java/io/cutalab/javacup/dashboard/views/ExternalProcessMetricsView.java");

        if (!Files.exists(sourcePath)) {
            sourcePath = Path.of("javacup-dashboard/src/main/java/io/cutalab/javacup/dashboard/views/ExternalProcessMetricsView.java");
        }

        String source = Files.readString(sourcePath);

        assertTrue(source.contains("section(\"Session health\""));
        assertTrue(source.contains("sessionHealthVerdict"));
        assertTrue(source.contains("sessionHealthProbe"));
        assertTrue(source.contains("sessionHealthSamples"));
        assertTrue(source.contains("sessionHealthMainIssue"));
        assertTrue(source.contains("sessionHealthAction"));
        assertTrue(source.contains("updateSessionHealthSummary"));
        assertTrue(source.contains("styleSessionHealthVerdict"));
    }
}
