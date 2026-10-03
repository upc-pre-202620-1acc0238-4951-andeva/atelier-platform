package com.andeva.atelier.platform.shared.domain.model.valueobjects;

/**
 * National and civil tax identification document typologies recognized by the platform.
 *
 * @author Joel Huamani Estefanero
 */
public enum TaxIdType {
    RUC("Unique Taxpayer Registry", 11),
    DNI("National Identity Document", 8),
    CE("Foreigner Identity Card", 12),
    PASSPORT("International Passport", 12);

    private final String description;
    private final int expectedLength;

    TaxIdType(String description, int expectedLength) {
        this.description = description;
        this.expectedLength = expectedLength;
    }

    public String description() {
        return description;
    }

    public int expectedLength() {
        return expectedLength;
    }
}
