package com.andeva.atelier.platform.crm.domain.model.valueobjects;

import com.andeva.atelier.platform.crm.domain.exceptions.InvalidVinException;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable value object representing a Vehicle Identification Number (VIN)
 * compliant with ISO 3779 (17 alphanumeric characters, excluding letters I, O, Q).
 *
 * @author Adiel Sanchez Santin
 */
public record Vin(String value) implements Serializable {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    public Vin {
        if (value != null) {
            String normalized = value.trim().toUpperCase();
            if (!VIN_PATTERN.matcher(normalized).matches()) {
                throw new InvalidVinException(value);
            }
            value = normalized;
        }
    }

    public static Vin of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new Vin(value);
    }
}
