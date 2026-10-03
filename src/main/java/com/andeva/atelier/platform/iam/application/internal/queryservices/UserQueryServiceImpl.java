package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByEmailQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Transactional read-only query service implementation for platform User accounts.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        Objects.requireNonNull(query, "GetUserByIdQuery cannot be null");
        return userRepository.findById(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        Objects.requireNonNull(query, "GetUserByEmailQuery cannot be null");
        return userRepository.findByEmail(query.email());
    }
}
