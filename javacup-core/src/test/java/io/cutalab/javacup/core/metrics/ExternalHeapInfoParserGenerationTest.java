package io.cutalab.javacup.core.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalHeapInfoParserGenerationTest {

    private final ExternalHeapInfoParser parser = new ExternalHeapInfoParser();

    @Test
    void sumsParallelGenerationHeapLinesWhenNoGlobalHeapLineIsAvailable() {
        String output = """
                PSYoungGen      total 38400K, used 9933K [0x00000000d5580000, 0x00000000d8000000, 0x0000000100000000)
                ParOldGen       total 87552K, used 20480K [0x0000000080000000, 0x0000000085580000, 0x00000000d5580000)
                Metaspace       used 1138K, committed 1344K, reserved 1114112K
                class space     used 128K, committed 256K, reserved 1048576K
                """;

        ExternalHeapInfo heapInfo = parser.parse(output);

        assertEquals(9933L + 20480L, heapInfo.heapUsedKb());
        assertEquals(38400L + 87552L, heapInfo.heapTotalKb());
        assertEquals(1138L, heapInfo.metaspaceUsedKb());
        assertEquals(1344L, heapInfo.metaspaceCommittedKb());
        assertEquals(128L, heapInfo.classSpaceUsedKb());
    }

    @Test
    void parsesMegabyteUnitsFromGenerationLines() {
        String output = """
                young generation total 64M, used 16M
                old generation total 256M, used 128M
                Metaspace       used 2M, committed 4M, reserved 1G
                """;

        ExternalHeapInfo heapInfo = parser.parse(output);

        assertEquals((16L + 128L) * 1024L, heapInfo.heapUsedKb());
        assertEquals((64L + 256L) * 1024L, heapInfo.heapTotalKb());
        assertEquals(2L * 1024L, heapInfo.metaspaceUsedKb());
        assertEquals(4L * 1024L, heapInfo.metaspaceCommittedKb());
        assertEquals(1024L * 1024L, heapInfo.metaspaceReservedKb());
    }

    @Test
    void keepsGlobalHeapLineWhenBothGlobalAndGenerationLinesExist() {
        String output = """
                garbage-first heap   total 129024K, used 5087K [0x0000000085a00000, 0x0000000100000000)
                PSYoungGen      total 38400K, used 9933K
                ParOldGen       total 87552K, used 20480K
                """;

        ExternalHeapInfo heapInfo = parser.parse(output);

        assertEquals(5087L, heapInfo.heapUsedKb());
        assertEquals(129024L, heapInfo.heapTotalKb());
    }
}
