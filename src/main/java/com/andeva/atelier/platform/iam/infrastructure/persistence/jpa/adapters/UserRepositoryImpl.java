package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Repository adapter implementing {@link UserRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {

    private final UserPersistenceRepository persistenceRepository;

    public UserRepositoryImpl(UserPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        UserPersistenceEntity entity = UserPersistenceAssembler.toEntity(user);
        UserPersistenceEntity saved = persistenceRepository.save(entity);
        return UserPersistenceAssembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        if (id == null) {
            return Optional.empty();
        }
        return persistenceRepository.findById(id.value())
                .map(UserPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(EmailAddress email) {
        if (email == null) {
            return Optional.empty();
        }
        return persistenceRepository.findByEmail(email.value())
                .map(UserPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(EmailAddress email) {
        if (email == null) {
            return false;
        }
        return persistenceRepository.existsByEmail(email.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByGoogleId(String googleId) {
        if (googleId == null || googleId.isBlank()) {
            return Optional.empty();
        }
        return persistenceRepository.findByGoogleId(googleId)
                .map(UserPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByVerificationToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return persistenceRepository.findByVerificationToken(token)
                .map(UserPersistenceAssembler::toDomain);
    }
}
