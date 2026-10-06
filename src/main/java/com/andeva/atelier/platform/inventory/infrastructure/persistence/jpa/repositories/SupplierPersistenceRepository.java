package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.SupplierPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierPersistenceRepository extends JpaRepository<SupplierPersistenceEntity, UUID> {

    Optional<SupplierPersistenceEntity> findByTenantIdAndTaxId(UUID tenantId, String taxId);

    boolean existsByTenantIdAndTaxId(UUID tenantId, String taxId);

    List<SupplierPersistenceEntity> findByTenantId(UUID tenantId);

    List<SupplierPersistenceEntity> findByTenantIdAndActive(UUID tenantId, boolean active);

    @Query("SELECT s FROM SupplierPersistenceEntity s WHERE s.tenantId = :tenantId " +
            "AND (:search IS NULL OR LOWER(s.businessName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.taxId) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:active IS NULL OR s.active = :active) " +
            "AND s.deletedAt IS NULL")
    List<SupplierPersistenceEntity> searchSuppliers(@Param("tenantId") UUID tenantId,
                                                   @Param("search") String search,
                                                   @Param("active") Boolean active);
}
