package com.andeva.atelier.platform.iam.domain.model.commands;

import java.util.Objects;

/**
 * Domain command to redeem a staff invitation and establish local user credentials.
 *
 * @author Joel Huamani Estefanero
 */
public record AcceptInvitationCommand(
        String token,
        String rawPassword,
        String firstName,
        String lastName,
        String phone
) {
    public AcceptInvitationCommand {
        Objects.requireNonNull(token, "Invitation token cannot be null");
        Objects.requireNonNull(rawPassword, "Password cannot be null");
        Objects.requireNonNull(firstName, "First name cannot be null");
        Objects.requireNonNull(lastName, "Last name cannot be null");
    }
}
