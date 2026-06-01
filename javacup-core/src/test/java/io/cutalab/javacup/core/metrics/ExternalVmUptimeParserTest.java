package io.cutalab.javacup.core.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalVmUptimeParserTest {

    private final ExternalVmUptimeParser parser = new ExternalVmUptimeParser();

    @Test
    void parsesIntegerSecondsFromJcmdOutput() {
        ExternalVmUptime uptime = parser.parse("""
                12529:
                163 s
                """);

        assertTrue(uptime.hasStructuredValue());
        assertEquals(163L, uptime.uptimeSeconds());
        assertEquals("2m 43s", uptime.displayValue());
    }

    @Test
    void parsesDecimalSecondsFromJcmdOutputUsingComma() {
        ExternalVmUptime uptime = parser.parse("""
                12529:
                163,289 s
                """);

        assertTrue(uptime.hasStructuredValue());
        assertEquals(163L, uptime.uptimeSeconds());
    }

    @Test
    void parsesDecimalSecondsFromJcmdOutputUsingDot() {
        ExternalVmUptime uptime = parser.parse("""
                12529:
                163.289 s
                """);

        assertTrue(uptime.hasStructuredValue());
        assertEquals(163L, uptime.uptimeSeconds());
    }

    @Test
    void keepsRawOutputWhenValueIsNotParseable() {
        String output = "unexpected uptime output";

        ExternalVmUptime uptime = parser.parse(output);

        assertFalse(uptime.hasStructuredValue());
        assertEquals(output, uptime.rawOutput());
        assertEquals("unavailable", uptime.displayValue());
    }
}
