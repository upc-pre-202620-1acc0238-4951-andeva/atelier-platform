package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;

import java.util.Objects;

/**
 * Domain command to register a new local User account with biographical details.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterUserCommand(
        EmailAddress email,
        Password password,
        PersonName name,
        PhoneNumber phone
) {
    public RegisterUserCommand {
        Objects.requireNonNull(email, "Email address cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(name, "Person name cannot be null");
    }
}
