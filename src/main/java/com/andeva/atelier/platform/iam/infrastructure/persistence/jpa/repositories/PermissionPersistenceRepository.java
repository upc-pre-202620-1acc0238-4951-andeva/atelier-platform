package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PermissionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PermissionPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface PermissionPersistenceRepository extends JpaRepository<PermissionPersistenceEntity, UUID> {

    Optional<PermissionPersistenceEntity> findByName(String name);

    List<PermissionPersistenceEntity> findByIdIn(Collection<UUID> ids);
}
