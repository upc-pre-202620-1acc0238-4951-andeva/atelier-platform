package com.andeva.atelier.platform.iam.application.queryservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByEmailQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;

import java.util.Optional;

/**
 * Public query service interface for reading platform User account state.
 *
 * @author Joel Huamani Estefanero
 */
public interface UserQueryService {

    Optional<User> handle(GetUserByIdQuery query);

    Optional<User> handle(GetUserByEmailQuery query);
}
