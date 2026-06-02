package io.cutalab.javacup.core.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalHeapInfoParserOpenJdkG1Test {

    private final ExternalHeapInfoParser parser = new ExternalHeapInfoParser();

    @Test
    void parsesRealOpenJdkG1HeapInfoOutput() {
        String output = """
                27251:
                 garbage-first heap   total 129024K, used 4063K [0x0000000085a00000, 0x0000000100000000)
                  region size 1024K, 3 young (3072K), 0 survivors (0K)
                 Metaspace       used 1052K, committed 1216K, reserved 1114112K
                  class space    used 111K, committed 192K, reserved 1048576K
                """;

        ExternalHeapInfo heapInfo = parser.parse(output);

        assertEquals("garbage-first heap", heapInfo.collectorOrHeapType());
        assertEquals(4063L, heapInfo.heapUsedKb());
        assertEquals(129024L, heapInfo.heapTotalKb());
        assertEquals(1052L, heapInfo.metaspaceUsedKb());
        assertEquals(1216L, heapInfo.metaspaceCommittedKb());
        assertEquals(1114112L, heapInfo.metaspaceReservedKb());
        assertEquals(111L, heapInfo.classSpaceUsedKb());
        assertEquals(192L, heapInfo.classSpaceCommittedKb());
        assertEquals(1048576L, heapInfo.classSpaceReservedKb());
        assertTrue(heapInfo.hasStructuredValues());
    }
}
