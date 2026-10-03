package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantMembershipPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link TenantMembershipPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface TenantMembershipPersistenceRepository extends JpaRepository<TenantMembershipPersistenceEntity, UUID> {

    Optional<TenantMembershipPersistenceEntity> findByTenant_IdAndUser_Id(UUID tenantId, UUID userId);

    List<TenantMembershipPersistenceEntity> findByTenant_Id(UUID tenantId);

    List<TenantMembershipPersistenceEntity> findByUser_Id(UUID userId);

    long countByTenant_Id(UUID tenantId);

    long countByAssignedRoles_Id(UUID roleId);
}
