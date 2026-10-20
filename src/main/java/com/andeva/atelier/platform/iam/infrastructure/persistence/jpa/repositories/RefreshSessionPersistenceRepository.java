package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RefreshSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshSessionPersistenceRepository extends JpaRepository<RefreshSessionPersistenceEntity, UUID> {

    Optional<RefreshSessionPersistenceEntity> findByTokenHash(String tokenHash);
}
