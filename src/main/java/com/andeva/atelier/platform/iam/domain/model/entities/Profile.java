package com.andeva.atelier.platform.iam.domain.model.entities;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;

/**
 * Biographical and contact entity tied 1:1 to a User account.
 * Decouples personal information from security and cryptographic credentials.
 *
 * @author Joel Huamani Estefanero
 */
public class Profile {

    private final UserId userId;
    private PersonName name;
    private PhoneNumber phone;

    public Profile(UserId userId, PersonName name, PhoneNumber phone) {
        this.userId = Objects.requireNonNull(userId, "User identifier cannot be null");
        this.name = Objects.requireNonNull(name, "Person name cannot be null");
        this.phone = phone; // optional for federated users until completed
    }

    public static Profile create(UserId userId, PersonName name, PhoneNumber phone) {
        return new Profile(userId, name, phone);
    }

    public void update(PersonName name, PhoneNumber phone) {
        this.name = Objects.requireNonNull(name, "Person name cannot be null");
        this.phone = phone;
    }

    public String getFullName() {
        return name.getFullName();
    }

    public String fullName() {
        return getFullName();
    }

    public UserId userId() {
        return userId;
    }

    public PersonName name() {
        return name;
    }

    public PhoneNumber phone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile profile)) return false;
        return Objects.equals(userId, profile.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
