package io.cutalab.javacup.core.process;

public enum ProbeFailureKind {
    NONE,
    TIMEOUT,
    PROCESS_NOT_FOUND,
    ATTACH_FAILED,
    JCMD_UNAVAILABLE,
    INTERRUPTED,
    UNKNOWN
}
