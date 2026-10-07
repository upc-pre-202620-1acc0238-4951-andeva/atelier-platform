package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDtcCodeException;
import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Standardized Diagnostic Trouble Code (DTC) compliant with SAE J2012 / ISO 15031-6.
 * The code format consists of 5 characters:
 * - 1st character: System Category (P = Powertrain, C = Chassis, B = Body, U = Network).
 * - 2nd character: Standard Type (0 or 2 = SAE Generic, 1 or 3 = Manufacturer Specific).
 * - 3rd character: Subsystem ID.
 * - 4th & 5th characters: Specific fault designator (Hexadecimal digits 0-9, A-F).
 *
 * @author Joel Huamani Estefanero
 */
public record DtcCode(String value) implements Serializable {

    private static final Pattern DTC_PATTERN = Pattern.compile("^[PCBU][0-9A-Fa-f]{4}$");

    public DtcCode {
        Objects.requireNonNull(value, "DTC code cannot be null");
        String trimmed = value.trim();
        if (!DTC_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidDtcCodeException(trimmed);
        }
        value = trimmed.toUpperCase(Locale.ROOT);
    }

    public static DtcCode of(String value) {
        return new DtcCode(value);
    }

    public char prefix() {
        return value.charAt(0);
    }

    public DtcCategory category() {
        return DtcCategory.fromPrefix(prefix());
    }

    public boolean isGeneric() {
        char standardChar = value.charAt(1);
        return standardChar == '0' || standardChar == '2';
    }

    public boolean isManufacturerSpecific() {
        char standardChar = value.charAt(1);
        return standardChar == '1' || standardChar == '3';
    }

    @Override
    public String toString() {
        return value;
    }
}
