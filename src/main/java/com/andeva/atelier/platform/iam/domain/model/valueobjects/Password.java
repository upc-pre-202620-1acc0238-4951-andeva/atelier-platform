package com.andeva.atelier.platform.iam.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Universal immutable value object encapsulating a cryptographic BCrypt password hash.
 * Guarantees that raw plaintext passwords never leak into the domain layer lifecycle.
 *
 * @author Joel Huamani Estefanero
 */
public record Password(String hashedValue) implements Serializable {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    public Password {
        Objects.requireNonNull(hashedValue, "Hashed password cannot be null");
        String trimmed = hashedValue.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be empty");
        }
        if (!BCRYPT_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid BCrypt password hash format");
        }
        hashedValue = trimmed;
    }

    public static Password of(String hashedValue) {
        return new Password(hashedValue);
    }

    public static Password fromHash(String hashedValue) {
        return of(hashedValue);
    }

    @Override
    public String toString() {
        return "[PROTECTED_CREDENTIAL]";
    }
}
