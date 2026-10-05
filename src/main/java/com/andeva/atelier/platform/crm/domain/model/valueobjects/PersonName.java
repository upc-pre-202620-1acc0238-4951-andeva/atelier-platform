package com.andeva.atelier.platform.crm.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable value object encapsulating an individual's given first name and family surname.
 *
 * @author Adiel Sanchez Santin
 */
public record PersonName(String firstName, String lastName) implements Serializable {

    public PersonName {
        Objects.requireNonNull(firstName, "First name cannot be null");
        Objects.requireNonNull(lastName, "Last name cannot be null");

        firstName = firstName.trim();
        lastName = lastName.trim();

        if (firstName.length() < 2) {
            throw new IllegalArgumentException("First name must contain at least 2 characters");
        }
        if (lastName.length() < 2) {
            throw new IllegalArgumentException("Last name must contain at least 2 characters");
        }
    }

    public static PersonName of(String firstName, String lastName) {
        return new PersonName(firstName, lastName);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
