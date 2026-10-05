package com.andeva.atelier.platform.operations.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

public record DiagnosticSummary(String value) implements Serializable {

    public DiagnosticSummary {
        Objects.requireNonNull(value, "DiagnosticSummary value cannot be null");
        String trimmed = value.trim();
        if (trimmed.length() > 2000) {
            throw new IllegalArgumentException("DiagnosticSummary cannot exceed 2000 characters");
        }
    }

    public static DiagnosticSummary of(String value) {
        return new DiagnosticSummary(value != null ? value.trim() : "");
    }

    public static DiagnosticSummary empty() {
        return new DiagnosticSummary("");
    }
}
