package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.CustomerPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerPersistenceRepository extends JpaRepository<CustomerPersistenceEntity, UUID> {

    boolean existsByTenantIdAndTaxId(UUID tenantId, String taxId);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    Optional<CustomerPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<CustomerPersistenceEntity> findByTenantIdAndTaxId(UUID tenantId, String taxId);

    @Query("SELECT c FROM CustomerPersistenceEntity c " +
           "WHERE c.tenantId = :tenantId " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     c.taxId LIKE CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.createdAt DESC")
    List<CustomerPersistenceEntity> searchCustomers(
            @Param("tenantId") UUID tenantId,
            @Param("type") CustomerType type,
            @Param("searchTerm") String searchTerm,
            @Param("status") String status
    );
}
