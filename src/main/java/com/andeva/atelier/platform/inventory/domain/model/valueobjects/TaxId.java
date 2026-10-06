package com.andeva.atelier.platform.inventory.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public record TaxId(String value) implements Serializable {

    private static final Pattern TAX_ID_PATTERN = Pattern.compile("^\\d{8,20}$");

    public TaxId {
        Objects.requireNonNull(value, "TaxId value cannot be null");
        String trimmed = value.trim();
        if (!TAX_ID_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid TaxId format: " + value + ". Must be between 8 and 20 digits.");
        }
        value = trimmed;
    }

    public static TaxId of(String value) {
        return new TaxId(value);
    }
}
