package io.cutalab.javacup.core.metrics;

public record GarbageCollectorSnapshot(
        String name,
        long collectionCount,
        long collectionTimeMillis
) {

    public boolean collectionCountAvailable() {
        return collectionCount >= 0;
    }

    public boolean collectionTimeAvailable() {
        return collectionTimeMillis >= 0;
    }
}
