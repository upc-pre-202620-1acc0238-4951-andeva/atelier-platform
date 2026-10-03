package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link UserPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface UserPersistenceRepository extends JpaRepository<UserPersistenceEntity, UUID> {

    Optional<UserPersistenceEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<UserPersistenceEntity> findByGoogleId(String googleId);

    @Query("SELECT u FROM UserPersistenceEntity u JOIN u.verificationTokens vt WHERE vt.token = :token")
    Optional<UserPersistenceEntity> findByVerificationToken(@Param("token") String token);
}
