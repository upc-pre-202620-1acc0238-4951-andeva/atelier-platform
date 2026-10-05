package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.ServicePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePersistenceRepository extends JpaRepository<ServicePersistenceEntity, UUID> {

    List<ServicePersistenceEntity> findAllByTenantId(UUID tenantId);

    Optional<ServicePersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<ServicePersistenceEntity> findByTenantIdAndName(UUID tenantId, String name);

    @Query("SELECT s FROM ServicePersistenceEntity s " +
           "WHERE s.tenantId = :tenantId " +
           "AND LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ServicePersistenceEntity> searchByName(
        @Param("tenantId") UUID tenantId,
        @Param("searchTerm") String searchTerm
    );
}
