package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkOrderPersistenceRepository extends JpaRepository<WorkOrderPersistenceEntity, UUID> {

    Optional<WorkOrderPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<WorkOrderPersistenceEntity> findByTenantIdAndInternalNumber(UUID tenantId, Integer internalNumber);

    List<WorkOrderPersistenceEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Page<WorkOrderPersistenceEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    List<WorkOrderPersistenceEntity> findByTenantIdAndVehicleId(UUID tenantId, UUID vehicleId);

    List<WorkOrderPersistenceEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);

    List<WorkOrderPersistenceEntity> findByCustomerId(UUID customerId);

    Optional<WorkOrderPersistenceEntity> findByCurrentBayId(UUID currentBayId);

    @Query("SELECT w FROM WorkOrderPersistenceEntity w " +
           "WHERE w.tenantId = :tenantId AND w.vehicleId = :vehicleId " +
           "AND w.status IN (com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus.DRAFT, com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus.IN_PROGRESS)")
    Optional<WorkOrderPersistenceEntity> findActiveByTenantIdAndVehicleId(
        @Param("tenantId") UUID tenantId,
        @Param("vehicleId") UUID vehicleId
    );

    Optional<WorkOrderPersistenceEntity> findByTenantIdAndCurrentBayId(UUID tenantId, UUID currentBayId);

    @Query("SELECT COALESCE(MAX(w.internalNumber), 0) + 1 FROM WorkOrderPersistenceEntity w " +
           "WHERE w.tenantId = :tenantId")
    Integer findNextInternalNumber(@Param("tenantId") UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WorkOrderPersistenceEntity w WHERE w.id = :id AND w.tenantId = :tenantId")
    Optional<WorkOrderPersistenceEntity> findByIdForUpdate(
        @Param("id") UUID id,
        @Param("tenantId") UUID tenantId
    );

    @Query("SELECT w FROM WorkOrderPersistenceEntity w JOIN w.tasks t WHERE t.id = :taskId")
    Optional<WorkOrderPersistenceEntity> findByTaskId(@Param("taskId") UUID taskId);
}
