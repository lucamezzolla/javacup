package io.cutalab.javacup.core.metrics;

import java.util.Optional;

public record ExternalHeapInfo(
        String collectorOrHeapType,
        Long heapUsedKb,
        Long heapTotalKb,
        Long heapReservedKb,
        Long metaspaceUsedKb,
        Long metaspaceCommittedKb,
        Long metaspaceReservedKb,
        Long classSpaceUsedKb,
        Long classSpaceCommittedKb,
        Long classSpaceReservedKb,
        String rawOutput
) {

    public Optional<Long> heapUsedMb() {
        return toMb(heapUsedKb);
    }

    public Optional<Long> heapTotalMb() {
        return toMb(heapTotalKb);
    }

    public Optional<Long> heapReservedMb() {
        return toMb(heapReservedKb);
    }

    public Optional<Long> metaspaceUsedMb() {
        return toMb(metaspaceUsedKb);
    }

    public Optional<Long> metaspaceCommittedMb() {
        return toMb(metaspaceCommittedKb);
    }

    public Optional<Long> metaspaceReservedMb() {
        return toMb(metaspaceReservedKb);
    }

    public Optional<Long> classSpaceUsedMb() {
        return toMb(classSpaceUsedKb);
    }

    public Optional<Long> classSpaceCommittedMb() {
        return toMb(classSpaceCommittedKb);
    }

    public Optional<Long> classSpaceReservedMb() {
        return toMb(classSpaceReservedKb);
    }

    public boolean hasStructuredValues() {
        return heapUsedKb != null
                || heapTotalKb != null
                || heapReservedKb != null
                || metaspaceUsedKb != null
                || classSpaceUsedKb != null;
    }

    private Optional<Long> toMb(Long valueKb) {
        if (valueKb == null) {
            return Optional.empty();
        }

        return Optional.of(valueKb / 1024);
    }
}
