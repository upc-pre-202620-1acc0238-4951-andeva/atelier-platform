package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.exceptions.BusinessRuleValidationException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a legally validated national tax identification document.
 * Implements SUNAT's weighted check-digit Modulo 11 verification algorithm for Peruvian RUCs.
 *
 * @author Joel Huamani Estefanero
 */
public record TaxId(String value, TaxIdType type) {

    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern RUC_PATTERN = Pattern.compile("^(10|15|17|20)\\d{9}$");
    private static final int[] RUC_WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    public TaxId {
        Objects.requireNonNull(value, "Tax document value cannot be null");
        Objects.requireNonNull(type, "Tax document type cannot be null");
        value = value.trim();

        if (type == TaxIdType.DNI) {
            if (!DNI_PATTERN.matcher(value).matches()) {
                throw new BusinessRuleValidationException(
                        "INVALID_DNI_FORMAT",
                        "National Identity Card (DNI) must have exactly 8 numeric digits: " + value);
            }
        } else if (type == TaxIdType.RUC) {
            if (!RUC_PATTERN.matcher(value).matches()) {
                throw new BusinessRuleValidationException(
                        "INVALID_RUC_FORMAT",
                        "Tax ID (RUC) must have 11 digits and start with 10, 15, 17, or 20: " + value);
            }
            if (!isValidRucChecksum(value)) {
                throw new BusinessRuleValidationException(
                        "INVALID_RUC_CHECKSUM",
                        "RUC verification check digit is not mathematically valid: " + value);
            }
        } else {
            if (value.isBlank()) {
                throw new BusinessRuleValidationException(
                        "EMPTY_TAX_ID",
                        "Tax document value cannot be empty");
            }
            if (value.length() > type.expectedLength()) {
                throw new BusinessRuleValidationException(
                        "INVALID_TAX_ID_LENGTH",
                        String.format("Tax document %s cannot exceed %d characters: %s", type, type.expectedLength(), value));
            }
        }
    }

    public static TaxId ruc(String ruc) {
        return new TaxId(ruc, TaxIdType.RUC);
    }

    public static TaxId dni(String dni) {
        return new TaxId(dni, TaxIdType.DNI);
    }

    /**
     * Validates official SUNAT verification check digit under Modulo 11 algorithm.
     */
    private static boolean isValidRucChecksum(String ruc) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(ruc.charAt(i)) * RUC_WEIGHTS[i];
        }
        int remainder = sum % 11;
        int checkDigit = 11 - remainder;
        if (checkDigit == 10) checkDigit = 0;
        else if (checkDigit == 11) checkDigit = 1;

        return checkDigit == Character.getNumericValue(ruc.charAt(10));
    }
}
