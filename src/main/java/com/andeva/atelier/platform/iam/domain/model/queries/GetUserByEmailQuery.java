package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

import java.util.Objects;

/**
 * Domain query to retrieve a user account by its unique email address.
 *
 * @author Joel Huamani Estefanero
 */
public record GetUserByEmailQuery(
        EmailAddress email
) {
    public GetUserByEmailQuery {
        Objects.requireNonNull(email, "Email address cannot be null");
    }
}
