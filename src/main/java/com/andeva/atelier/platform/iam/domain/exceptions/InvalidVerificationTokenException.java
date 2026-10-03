package com.andeva.atelier.platform.iam.domain.exceptions;

/**
 * Thrown when a verification token (OTP, password reset) is invalid, expired, or already consumed.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidVerificationTokenException extends IamDomainException {

    public InvalidVerificationTokenException() {
        super("INVALID_VERIFICATION_TOKEN", "The provided verification token is invalid, expired, or already used");
    }

    public InvalidVerificationTokenException(String message) {
        super("INVALID_VERIFICATION_TOKEN", message);
    }
}
