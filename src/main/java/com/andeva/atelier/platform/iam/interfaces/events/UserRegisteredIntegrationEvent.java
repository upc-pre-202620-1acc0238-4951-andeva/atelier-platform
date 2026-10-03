package com.andeva.atelier.platform.iam.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a new global user account is registered.
 *
 * @param userId     Universal unique identifier of the user
 * @param email      Verified or primary email address
 * @param fullName   Concatenated full legal name
 * @param occurredOn Timestamp of registration
 * @author Joel Huamani Estefanero
 */
public record UserRegisteredIntegrationEvent(
        UUID userId,
        String email,
        String fullName,
        Instant occurredOn
) {
    public UserRegisteredIntegrationEvent {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(email, "email cannot be null");
        Objects.requireNonNull(fullName, "fullName cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
