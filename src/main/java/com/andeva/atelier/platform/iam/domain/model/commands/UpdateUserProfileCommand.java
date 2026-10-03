package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;

/**
 * Command carrying user profile demographic modifications.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateUserProfileCommand(
        UserId userId,
        PersonName name,
        PhoneNumber phone
) {
    public UpdateUserProfileCommand {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(name, "Person name cannot be null");
    }
}
