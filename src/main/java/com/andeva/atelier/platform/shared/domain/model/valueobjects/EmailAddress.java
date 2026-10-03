package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.exceptions.BusinessRuleValidationException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address validated under RFC 5322 specifications.
 *
 * @author Joel Huamani Estefanero
 */
public record EmailAddress(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public EmailAddress {
        Objects.requireNonNull(value, "Email address cannot be null");
        value = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BusinessRuleValidationException(
                    "INVALID_EMAIL_FORMAT",
                    "Invalid email address: " + value);
        }
    }

    public static EmailAddress of(String email) {
        return new EmailAddress(email);
    }
}
