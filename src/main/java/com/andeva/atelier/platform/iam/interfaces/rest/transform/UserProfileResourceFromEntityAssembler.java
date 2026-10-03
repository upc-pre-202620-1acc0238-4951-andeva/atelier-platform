package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.entities.Profile;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.UserProfileResource;

import java.util.Objects;
import java.util.UUID;

/**
 * Assembler projecting domain {@link Profile} entities and {@link User} aggregates into REST {@link UserProfileResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
public final class UserProfileResourceFromEntityAssembler {

    private UserProfileResourceFromEntityAssembler() {
    }

    /**
     * Converts a {@link User} aggregate into a {@link UserProfileResource}.
     *
     * @param user Domain user aggregate
     * @return REST user profile response projection
     */
    public static UserProfileResource toResourceFromUser(User user) {
        Objects.requireNonNull(user, "User aggregate cannot be null");
        Profile profile = user.profile();
        UUID profileId = profile != null ? profile.userId().value() : user.id().value();
        String firstName = (profile != null && profile.name() != null) ? profile.name().firstName() : "";
        String lastName = (profile != null && profile.name() != null) ? profile.name().lastName() : "";
        String phone = (profile != null && profile.phone() != null) ? profile.phone().value() : null;
        String avatarUrl = null;

        return new UserProfileResource(
                profileId,
                user.email().value(),
                firstName,
                lastName,
                phone,
                avatarUrl
        );
    }
}
