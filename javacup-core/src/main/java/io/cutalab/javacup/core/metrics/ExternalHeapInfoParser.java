package io.cutalab.javacup.core.metrics;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalHeapInfoParser {

    private static final Pattern HEAP_TOTAL_USED = Pattern.compile(".*heap.*total\\s+(\\d+)K,\\s*used\\s+(\\d+)K.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEAP_USED_TOTAL = Pattern.compile(".*heap.*used\\s+(\\d+)K.*total\\s+(\\d+)K.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESERVED = Pattern.compile(".*reserved\\s+(\\d+)K.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern METASPACE = Pattern.compile(".*Metaspace\\s+used\\s+(\\d+)K,\\s*committed\\s+(\\d+)K,\\s*reserved\\s+(\\d+)K.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLASS_SPACE = Pattern.compile(".*class space\\s+used\\s+(\\d+)K,\\s*committed\\s+(\\d+)K,\\s*reserved\\s+(\\d+)K.*", Pattern.CASE_INSENSITIVE);

    public ExternalHeapInfo parse(String output) {
        String safeOutput = output == null ? "" : output;

        String collectorOrHeapType = "";
        Long heapUsedKb = null;
        Long heapTotalKb = null;
        Long heapReservedKb = null;
        Long metaspaceUsedKb = null;
        Long metaspaceCommittedKb = null;
        Long metaspaceReservedKb = null;
        Long classSpaceUsedKb = null;
        Long classSpaceCommittedKb = null;
        Long classSpaceReservedKb = null;

        String[] lines = safeOutput.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();

            if (collectorOrHeapType.isBlank() && looksLikeHeapTypeLine(trimmed)) {
                collectorOrHeapType = trimmed;
            }

            Matcher heapTotalUsed = HEAP_TOTAL_USED.matcher(trimmed);
            if (heapTotalUsed.matches()) {
                heapTotalKb = parseLong(heapTotalUsed.group(1));
                heapUsedKb = parseLong(heapTotalUsed.group(2));
            }

            Matcher heapUsedTotal = HEAP_USED_TOTAL.matcher(trimmed);
            if (heapUsedTotal.matches()) {
                heapUsedKb = parseLong(heapUsedTotal.group(1));
                heapTotalKb = parseLong(heapUsedTotal.group(2));
            }

            if (trimmed.toLowerCase(Locale.ROOT).contains("heap")
                    && trimmed.toLowerCase(Locale.ROOT).contains("reserved")) {
                Matcher reserved = RESERVED.matcher(trimmed);
                if (reserved.matches()) {
                    heapReservedKb = parseLong(reserved.group(1));
                }
            }

            Matcher metaspace = METASPACE.matcher(trimmed);
            if (metaspace.matches()) {
                metaspaceUsedKb = parseLong(metaspace.group(1));
                metaspaceCommittedKb = parseLong(metaspace.group(2));
                metaspaceReservedKb = parseLong(metaspace.group(3));
            }

            Matcher classSpace = CLASS_SPACE.matcher(trimmed);
            if (classSpace.matches()) {
                classSpaceUsedKb = parseLong(classSpace.group(1));
                classSpaceCommittedKb = parseLong(classSpace.group(2));
                classSpaceReservedKb = parseLong(classSpace.group(3));
            }
        }

        return new ExternalHeapInfo(
                collectorOrHeapType,
                heapUsedKb,
                heapTotalKb,
                heapReservedKb,
                metaspaceUsedKb,
                metaspaceCommittedKb,
                metaspaceReservedKb,
                classSpaceUsedKb,
                classSpaceCommittedKb,
                classSpaceReservedKb,
                safeOutput
        );
    }

    private boolean looksLikeHeapTypeLine(String line) {
        if (line.isBlank()) {
            return false;
        }

        String lower = line.toLowerCase(Locale.ROOT);

        return lower.contains("heap")
                && !lower.contains("used")
                && !lower.contains("total")
                && !lower.contains("reserved")
                && !lower.startsWith("pid:")
                && !lower.startsWith("command:")
                && !lower.startsWith("status:");
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
