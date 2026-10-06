package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.PayrollPaymentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollPaymentPersistenceRepository extends JpaRepository<PayrollPaymentPersistenceEntity, UUID> {

    Optional<PayrollPaymentPersistenceEntity> findByTenantIdAndMembershipIdAndPeriodStartAndPeriodEnd(
            UUID tenantId, UUID membershipId, LocalDate periodStart, LocalDate periodEnd
    );

    @Query("SELECT p FROM PayrollPaymentPersistenceEntity p WHERE p.tenantId = :tenantId AND p.periodStart >= :start AND p.periodEnd <= :end ORDER BY p.createdAt DESC")
    List<PayrollPaymentPersistenceEntity> findAllByTenantIdAndPeriod(
            @Param("tenantId") UUID tenantId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<PayrollPaymentPersistenceEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<PayrollPaymentPersistenceEntity> findAllByTenantIdAndMembershipIdOrderByPeriodStartDesc(
            UUID tenantId, UUID membershipId
    );
}
