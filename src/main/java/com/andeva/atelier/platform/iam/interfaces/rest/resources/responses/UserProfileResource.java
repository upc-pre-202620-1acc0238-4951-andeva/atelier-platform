package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * Response projection representing the user profile demographic details.
 *
 * @param id        Universal unique identifier of the user profile
 * @param email     Registered account email address
 * @param firstName Given first name
 * @param lastName  Full surname
 * @param phone     Contact telephone number
 * @param avatarUrl Public URL of user avatar image in cloud storage
 * @author Joel Huamani Estefanero
 */
public record UserProfileResource(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl
) {
}
