package com.andeva.atelier.platform.iam.domain.exceptions;

/**
 * Thrown when user authentication fails due to incorrect password, unrecognized provider, or invalid token.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidCredentialsException extends IamDomainException {

    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid authentication credentials provided");
    }

    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message);
    }
}
