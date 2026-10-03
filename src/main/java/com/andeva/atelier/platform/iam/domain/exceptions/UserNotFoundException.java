package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

/**
 * Thrown when a User account cannot be located by its identifier or email address.
 *
 * @author Joel Huamani Estefanero
 */
public class UserNotFoundException extends IamDomainException {

    public UserNotFoundException(UserId userId) {
        super("USER_NOT_FOUND", "User account not found with identifier: " + (userId != null ? userId.value() : "null"));
    }

    public UserNotFoundException(EmailAddress email) {
        super("USER_NOT_FOUND", "User account not found with email: " + (email != null ? email.value() : "null"));
    }

    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }
}
