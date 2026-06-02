package io.cutalab.javacup.core.metrics;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalHeapInfoParser {

    private static final Pattern HEAP_TOTAL_USED = Pattern.compile(".*heap.*total\\s+([0-9]+)([KMG]?),\\s*used\\s+([0-9]+)([KMG]?).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEAP_USED_TOTAL = Pattern.compile(".*heap.*used\\s+([0-9]+)([KMG]?).*total\\s+([0-9]+)([KMG]?).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPACE_TOTAL_USED = Pattern.compile(".*\\btotal\\s+([0-9]+)([KMG]?),\\s*used\\s+([0-9]+)([KMG]?).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPACE_USED_TOTAL = Pattern.compile(".*\\bused\\s+([0-9]+)([KMG]?).*\\btotal\\s+([0-9]+)([KMG]?).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESERVED = Pattern.compile(".*reserved\\s+([0-9]+)([KMG]?).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern METASPACE = Pattern.compile(".*Metaspace\\s+used\\s+([0-9]+)([KMG]?),\\s*committed\\s+([0-9]+)([KMG]?),\\s*reserved\\s+([0-9]+)([KMG]?).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLASS_SPACE = Pattern.compile(".*class space\\s+used\\s+([0-9]+)([KMG]?),\\s*committed\\s+([0-9]+)([KMG]?),\\s*reserved\\s+([0-9]+)([KMG]?).*", Pattern.CASE_INSENSITIVE);

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

        long generationUsedKb = 0L;
        long generationTotalKb = 0L;
        boolean generationValuesFound = false;

        String[] lines = safeOutput.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();

            if (collectorOrHeapType.isBlank() && looksLikeHeapTypeLine(trimmed)) {
                collectorOrHeapType = trimmed;
            }

            Matcher heapTotalUsed = HEAP_TOTAL_USED.matcher(trimmed);
            if (heapTotalUsed.matches()) {
                heapTotalKb = parseMemoryToKb(heapTotalUsed.group(1), heapTotalUsed.group(2));
                heapUsedKb = parseMemoryToKb(heapTotalUsed.group(3), heapTotalUsed.group(4));

                if (collectorOrHeapType.isBlank()) {
                    collectorOrHeapType = extractHeapType(trimmed);
                }
            }

            Matcher heapUsedTotal = HEAP_USED_TOTAL.matcher(trimmed);
            if (heapUsedTotal.matches()) {
                heapUsedKb = parseMemoryToKb(heapUsedTotal.group(1), heapUsedTotal.group(2));
                heapTotalKb = parseMemoryToKb(heapUsedTotal.group(3), heapUsedTotal.group(4));

                if (collectorOrHeapType.isBlank()) {
                    collectorOrHeapType = extractHeapType(trimmed);
                }
            }

            if (looksLikeGenerationOrSpaceLine(trimmed)) {
                SpaceUsage usage = parseSpaceUsage(trimmed);
                if (usage != null) {
                    generationUsedKb += usage.usedKb();
                    generationTotalKb += usage.totalKb();
                    generationValuesFound = true;

                    if (collectorOrHeapType.isBlank()) {
                        collectorOrHeapType = extractSpaceName(trimmed);
                    }
                }
            }

            if (trimmed.toLowerCase(Locale.ROOT).contains("heap")
                    && trimmed.toLowerCase(Locale.ROOT).contains("reserved")) {
                Matcher reserved = RESERVED.matcher(trimmed);
                if (reserved.matches()) {
                    heapReservedKb = parseMemoryToKb(reserved.group(1), reserved.group(2));
                }
            }

            Matcher metaspace = METASPACE.matcher(trimmed);
            if (metaspace.matches()) {
                metaspaceUsedKb = parseMemoryToKb(metaspace.group(1), metaspace.group(2));
                metaspaceCommittedKb = parseMemoryToKb(metaspace.group(3), metaspace.group(4));
                metaspaceReservedKb = parseMemoryToKb(metaspace.group(5), metaspace.group(6));
            }

            Matcher classSpace = CLASS_SPACE.matcher(trimmed);
            if (classSpace.matches()) {
                classSpaceUsedKb = parseMemoryToKb(classSpace.group(1), classSpace.group(2));
                classSpaceCommittedKb = parseMemoryToKb(classSpace.group(3), classSpace.group(4));
                classSpaceReservedKb = parseMemoryToKb(classSpace.group(5), classSpace.group(6));
            }
        }

        if ((heapUsedKb == null || heapTotalKb == null) && generationValuesFound) {
            heapUsedKb = generationUsedKb;
            heapTotalKb = generationTotalKb;
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

    private SpaceUsage parseSpaceUsage(String line) {
        Matcher totalUsed = SPACE_TOTAL_USED.matcher(line);
        if (totalUsed.matches()) {
            Long totalKb = parseMemoryToKb(totalUsed.group(1), totalUsed.group(2));
            Long usedKb = parseMemoryToKb(totalUsed.group(3), totalUsed.group(4));

            if (totalKb != null && usedKb != null) {
                return new SpaceUsage(usedKb, totalKb);
            }
        }

        Matcher usedTotal = SPACE_USED_TOTAL.matcher(line);
        if (usedTotal.matches()) {
            Long usedKb = parseMemoryToKb(usedTotal.group(1), usedTotal.group(2));
            Long totalKb = parseMemoryToKb(usedTotal.group(3), usedTotal.group(4));

            if (totalKb != null && usedKb != null) {
                return new SpaceUsage(usedKb, totalKb);
            }
        }

        return null;
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
                && !lower.startsWith("status:")
                && !lower.startsWith("failure kind:");
    }

    private boolean looksLikeGenerationOrSpaceLine(String line) {
        String lower = line.toLowerCase(Locale.ROOT);

        if (lower.contains("metaspace") || lower.contains("class space")) {
            return false;
        }

        if (!(lower.contains("total") && lower.contains("used"))) {
            return false;
        }

        return lower.contains("generation")
                || lower.contains("gen")
                || lower.contains("eden")
                || lower.contains("survivor")
                || lower.contains("old")
                || lower.contains("tenured")
                || lower.contains("parold")
                || lower.contains("psyoung");
    }

    private String extractHeapType(String line) {
        int totalIndex = line.toLowerCase(Locale.ROOT).indexOf("total");

        if (totalIndex <= 0) {
            return line;
        }

        return line.substring(0, totalIndex).trim();
    }

    private String extractSpaceName(String line) {
        int totalIndex = line.toLowerCase(Locale.ROOT).indexOf("total");

        if (totalIndex <= 0) {
            return line;
        }

        return line.substring(0, totalIndex).trim();
    }

    private Long parseMemoryToKb(String value, String unit) {
        try {
            long parsed = Long.parseLong(value);
            String normalizedUnit = unit == null || unit.isBlank() ? "K" : unit.toUpperCase(Locale.ROOT);

            return switch (normalizedUnit) {
                case "G" -> parsed * 1024L * 1024L;
                case "M" -> parsed * 1024L;
                default -> parsed;
            };
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record SpaceUsage(long usedKb, long totalKb) {
    }
}
