package com.andeva.atelier.platform.iot.domain.model.enums;

import java.util.Locale;

/**
 * Standard SAE J2012 / ISO 15031-6 Diagnostic Trouble Code (DTC) system categories.
 *
 * @author Joel Huamani Estefanero
 */
public enum DtcCategory {
    POWERTRAIN_P,
    CHASSIS_C,
    BODY_B,
    NETWORK_U;

    /**
     * Resolves the DTC category from the first character prefix of a standard DTC code.
     *
     * @param prefix the first character (P, C, B, U)
     * @return the corresponding DtcCategory
     */
    public static DtcCategory fromPrefix(char prefix) {
        return switch (Character.toUpperCase(prefix)) {
            case 'P' -> POWERTRAIN_P;
            case 'C' -> CHASSIS_C;
            case 'B' -> BODY_B;
            case 'U' -> NETWORK_U;
            default -> throw new IllegalArgumentException("Unknown DTC prefix character: " + prefix);
        };
    }

    /**
     * Permissive parser accepting either the full enum name (e.g. POWERTRAIN_P) or short prefix name (e.g. POWERTRAIN).
     */
    public static DtcCategory fromString(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("DTC category cannot be null or blank");
        }
        String normalized = category.trim().toUpperCase(Locale.ROOT);
        for (DtcCategory cat : values()) {
            if (cat.name().equals(normalized) || cat.name().startsWith(normalized)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unknown DTC category: " + category);
    }
}
