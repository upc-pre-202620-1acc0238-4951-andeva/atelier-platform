package com.andeva.atelier.platform.crm.domain.model.valueobjects;

import com.andeva.atelier.platform.crm.domain.exceptions.InvalidLicensePlateException;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable value object representing a national official vehicle license plate.
 * Normalizes removing hyphens and whitespaces, stored in uppercase.
 *
 * @author Adiel Sanchez Santin
 */
public record LicensePlate(String value) implements Serializable {

    private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z0-9]{5,7}$");

    public LicensePlate {
        Objects.requireNonNull(value, "License plate cannot be null");
        String normalized = value.trim().replaceAll("[-\\s]", "").toUpperCase();
        if (!PLATE_PATTERN.matcher(normalized).matches()) {
            throw new InvalidLicensePlateException(value);
        }
        value = normalized;
    }

    public static LicensePlate of(String value) {
        return new LicensePlate(value);
    }

    public String getFormatted() {
        if (value.length() == 6) {
            return value.substring(0, 3) + "-" + value.substring(3);
        }
        return value;
    }
}
