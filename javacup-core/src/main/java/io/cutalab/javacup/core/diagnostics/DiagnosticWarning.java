package io.cutalab.javacup.core.diagnostics;

public record DiagnosticWarning(
        String code,
        DiagnosticSeverity severity,
        String title,
        String explanation,
        String evidence,
        String recommendation
) {
}
