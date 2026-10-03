package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

/**
 * Thrown when attempting to register a User with an email address that is already registered.
 *
 * @author Joel Huamani Estefanero
 */
public class UserAlreadyExistsException extends IamDomainException {

    public UserAlreadyExistsException(EmailAddress email) {
        super("USER_ALREADY_EXISTS", "A user account with email " + (email != null ? email.value() : "null") + " already exists");
    }

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message);
    }
}
