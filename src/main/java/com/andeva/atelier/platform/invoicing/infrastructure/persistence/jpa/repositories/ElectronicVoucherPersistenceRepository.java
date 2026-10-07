package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.ElectronicVoucherPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for {@link ElectronicVoucherPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface ElectronicVoucherPersistenceRepository extends JpaRepository<ElectronicVoucherPersistenceEntity, UUID> {

    Optional<ElectronicVoucherPersistenceEntity> findByTenantIdAndSerieAndNumber(UUID tenantId, String serie, int number);

    List<ElectronicVoucherPersistenceEntity> findAllByWorkOrderId(UUID workOrderId);

    @Query("SELECT v FROM ElectronicVoucherPersistenceEntity v WHERE v.tenantId = :tenantId AND v.createdAt >= :from AND v.createdAt <= :to ORDER BY v.createdAt DESC")
    List<ElectronicVoucherPersistenceEntity> findAllByTenantAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    boolean existsByTenantIdAndSerieAndNumber(UUID tenantId, String serie, int number);
}
