package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link RolePersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface RolePersistenceRepository extends JpaRepository<RolePersistenceEntity, UUID> {

    Optional<RolePersistenceEntity> findByTenant_IdAndName(UUID tenantId, String name);

    Optional<RolePersistenceEntity> findByTenant_IdAndCode(UUID tenantId, String code);

    List<RolePersistenceEntity> findByTenant_Id(UUID tenantId);

    boolean existsByTenant_IdAndName(UUID tenantId, String name);

    List<RolePersistenceEntity> findByTenant_IdOrSystemRoleTrue(UUID tenantId);

    List<RolePersistenceEntity> findBySystemRoleTrue();
}
