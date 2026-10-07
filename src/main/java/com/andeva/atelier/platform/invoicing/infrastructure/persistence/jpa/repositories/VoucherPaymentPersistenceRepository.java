package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.VoucherPaymentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for {@link VoucherPaymentPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface VoucherPaymentPersistenceRepository extends JpaRepository<VoucherPaymentPersistenceEntity, UUID> {

    @Query("SELECT p FROM VoucherPaymentPersistenceEntity p WHERE p.voucher.id = :voucherId ORDER BY p.paidAt ASC")
    List<VoucherPaymentPersistenceEntity> findAllByVoucherId(@Param("voucherId") UUID voucherId);

    @Query("SELECT p FROM VoucherPaymentPersistenceEntity p WHERE p.branchId = :branchId AND p.paidAt >= :dayStart AND p.paidAt <= :dayEnd ORDER BY p.paidAt DESC")
    List<VoucherPaymentPersistenceEntity> findAllByBranchAndDate(
            @Param("branchId") UUID branchId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd
    );

    @Query("SELECT p FROM VoucherPaymentPersistenceEntity p WHERE p.tenantId = :tenantId AND p.paidAt >= :from AND p.paidAt <= :to ORDER BY p.paidAt ASC")
    List<VoucherPaymentPersistenceEntity> findByTenantAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
