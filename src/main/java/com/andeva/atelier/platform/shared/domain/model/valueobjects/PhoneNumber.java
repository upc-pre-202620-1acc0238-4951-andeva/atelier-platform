package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.exceptions.BusinessRuleValidationException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an international phone number formatted under the E.164 standard.
 *
 * @author Joel Huamani Estefanero
 */
public record PhoneNumber(String value) {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");

    public PhoneNumber {
        Objects.requireNonNull(value, "Phone number cannot be null");
        value = value.trim().replaceAll("\\s+", "");
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new BusinessRuleValidationException(
                    "INVALID_PHONE_FORMAT",
                    "Phone number outside E.164 standard: " + value);
        }
    }

    public static PhoneNumber of(String phone) {
        return new PhoneNumber(phone);
    }
}
