package io.cutalab.javacup.core.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessProbeResultTest {

    @Test
    void successHasOkStatusAndNoFailureKind() {
        ProcessProbeResult result = ProcessProbeResult.success(1234L, "jcmd 1234 VM.uptime", "10.5 s");

        assertTrue(result.successful());
        assertEquals(ProbeStatus.OK, result.probeStatus());
        assertEquals(ProbeFailureKind.NONE, result.failureKind());
    }

    @Test
    void classifiesTimeoutFailures() {
        ProcessProbeResult result = ProcessProbeResult.failure(1234L, "jcmd 1234 VM.uptime", "jcmd timed out after 5 seconds.");

        assertFalse(result.successful());
        assertEquals(ProbeStatus.FAILED, result.probeStatus());
        assertEquals(ProbeFailureKind.TIMEOUT, result.failureKind());
    }

    @Test
    void classifiesProcessNotFoundFailures() {
        ProcessProbeResult result = ProcessProbeResult.failure(1234L, "jcmd 1234 GC.heap_info", "java.io.IOException: Nessun processo corrisponde");

        assertFalse(result.successful());
        assertEquals(ProbeStatus.FAILED, result.probeStatus());
        assertEquals(ProbeFailureKind.PROCESS_NOT_FOUND, result.failureKind());
    }

    @Test
    void classifiesJcmdUnavailableFailures() {
        ProcessProbeResult result = ProcessProbeResult.failure(1234L, "jcmd 1234 GC.heap_info", "Cannot run program \"jcmd\": error=2, No such file or directory");

        assertFalse(result.successful());
        assertEquals(ProbeStatus.FAILED, result.probeStatus());
        assertEquals(ProbeFailureKind.JCMD_UNAVAILABLE, result.failureKind());
    }

    @Test
    void classifiesAttachFailures() {
        ProcessProbeResult result = ProcessProbeResult.failure(1234L, "jcmd 1234 GC.heap_info", "AttachNotSupportedException: operation not permitted");

        assertFalse(result.successful());
        assertEquals(ProbeStatus.FAILED, result.probeStatus());
        assertEquals(ProbeFailureKind.ATTACH_FAILED, result.failureKind());
    }

    @Test
    void displayTextIncludesFailureKind() {
        ProcessProbeResult result = ProcessProbeResult.failure(1234L, "jcmd 1234 GC.heap_info", "process not found");

        assertTrue(result.displayText().contains("Failure kind: PROCESS_NOT_FOUND"));
    }
}
