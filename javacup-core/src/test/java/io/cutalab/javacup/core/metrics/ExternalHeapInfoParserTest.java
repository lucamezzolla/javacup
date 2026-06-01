package io.cutalab.javacup.core.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalHeapInfoParserTest {

    private final ExternalHeapInfoParser parser = new ExternalHeapInfoParser();

    @Test
    void parsesG1HeapInfoFromJcmdOutput() {
        String output = """
                12529:
                 garbage-first heap   total 129024K, used 5087K [0x0000000085a00000, 0x0000000100000000)
                  region size 1024K, 4 young (4096K), 0 survivors (0K)
                 Metaspace       used 1138K, committed 1344K, reserved 1114112K
                  class space    used 101K, committed 192K, reserved 1048576K
                """;

        ExternalHeapInfo heapInfo = parser.parse(output);

        assertTrue(heapInfo.hasStructuredValues());
        assertEquals("garbage-first heap", heapInfo.collectorOrHeapType());
        assertEquals(5087L, heapInfo.heapUsedKb());
        assertEquals(129024L, heapInfo.heapTotalKb());
        assertEquals(1138L, heapInfo.metaspaceUsedKb());
        assertEquals(1344L, heapInfo.metaspaceCommittedKb());
        assertEquals(1114112L, heapInfo.metaspaceReservedKb());
        assertEquals(101L, heapInfo.classSpaceUsedKb());
        assertEquals(192L, heapInfo.classSpaceCommittedKb());
        assertEquals(1048576L, heapInfo.classSpaceReservedKb());
    }

    @Test
    void keepsRawOutputWhenValuesAreNotParseable() {
        String output = "unexpected heap output";

        ExternalHeapInfo heapInfo = parser.parse(output);

        assertEquals(output, heapInfo.rawOutput());
        assertTrue(heapInfo.heapUsedMb().isEmpty());
    }
}
