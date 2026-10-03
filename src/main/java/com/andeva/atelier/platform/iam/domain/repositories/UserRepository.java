package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Optional;

/**
 * Domain repository port for managing User account aggregates.
 *
 * @author Joel Huamani Estefanero
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(EmailAddress email);

    boolean existsByEmail(EmailAddress email);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByVerificationToken(String token);
}
