package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;

/**
 * Domain query to retrieve a user account by its universal identifier.
 *
 * @author Joel Huamani Estefanero
 */
public record GetUserByIdQuery(
        UserId userId
) {
    public GetUserByIdQuery {
        Objects.requireNonNull(userId, "User identifier cannot be null");
    }
}
