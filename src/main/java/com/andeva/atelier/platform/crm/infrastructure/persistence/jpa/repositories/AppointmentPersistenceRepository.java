package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentPersistenceRepository extends JpaRepository<AppointmentPersistenceEntity, UUID> {

    Optional<AppointmentPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT a FROM AppointmentPersistenceEntity a " +
           "WHERE a.tenantId = :tenantId " +
           "AND a.branchId = :branchId " +
           "AND a.scheduledAt >= :startOfDay " +
           "AND a.scheduledAt <= :endOfDay " +
           "ORDER BY a.scheduledAt ASC")
    List<AppointmentPersistenceEntity> findAllByTenantIdAndBranchIdAndDate(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay
    );

    @Query("SELECT a FROM AppointmentPersistenceEntity a " +
           "WHERE a.tenantId = :tenantId " +
           "AND a.branchId = :branchId " +
           "AND a.status IN ('pending', 'confirmed') " +
           "AND a.scheduledAt >= :windowStart " +
           "AND a.scheduledAt <= :windowEnd")
    List<AppointmentPersistenceEntity> findActiveByBranchInWindow(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("windowStart") Instant windowStart,
            @Param("windowEnd") Instant windowEnd
    );

    @Query("SELECT COUNT(a) > 0 FROM AppointmentPersistenceEntity a " +
           "WHERE a.customerId = :customerId " +
           "AND a.status IN ('pending', 'confirmed')")
    boolean existsActiveAppointmentsByCustomerId(@Param("customerId") UUID customerId);

    List<AppointmentPersistenceEntity> findByCustomerIdOrderByScheduledAtDesc(UUID customerId);

    List<AppointmentPersistenceEntity> findByVehicleIdOrderByScheduledAtDesc(UUID vehicleId);
}
