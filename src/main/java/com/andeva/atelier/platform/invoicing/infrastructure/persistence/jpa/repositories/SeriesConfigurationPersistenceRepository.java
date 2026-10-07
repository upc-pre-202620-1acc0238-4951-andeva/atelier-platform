package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.SeriesConfigurationPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for {@link SeriesConfigurationPersistenceEntity} with pessimistic write locking.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface SeriesConfigurationPersistenceRepository extends JpaRepository<SeriesConfigurationPersistenceEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeriesConfigurationPersistenceEntity s WHERE s.tenantId = :tenantId AND s.branchId = :branchId AND s.voucherType = :voucherType AND s.isActive = true")
    Optional<SeriesConfigurationPersistenceEntity> findActiveForUpdate(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("voucherType") String voucherType
    );

    @Query("SELECT s FROM SeriesConfigurationPersistenceEntity s WHERE s.tenantId = :tenantId AND s.branchId = :branchId AND s.voucherType = :voucherType AND s.isActive = true")
    Optional<SeriesConfigurationPersistenceEntity> findByTenantIdAndBranchIdAndVoucherTypeAndActive(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("voucherType") String voucherType
    );

    Optional<SeriesConfigurationPersistenceEntity> findByTenantIdAndBranchIdAndVoucherTypeAndSerie(
            UUID tenantId,
            UUID branchId,
            String voucherType,
            String serie
    );

    List<SeriesConfigurationPersistenceEntity> findAllByTenantIdAndBranchId(UUID tenantId, UUID branchId);

    boolean existsByTenantIdAndBranchIdAndSerie(UUID tenantId, UUID branchId, String serie);
}
