package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkBayPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkBayPersistenceRepository extends JpaRepository<WorkBayPersistenceEntity, UUID> {

    Optional<WorkBayPersistenceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<WorkBayPersistenceEntity> findAllByTenantIdAndBranchId(UUID tenantId, UUID branchId);

    @Query("SELECT b FROM WorkBayPersistenceEntity b " +
           "WHERE b.tenantId = :tenantId AND b.branchId = :branchId " +
           "AND b.type = :type AND b.status = com.andeva.atelier.platform.operations.domain.model.enums.BayStatus.AVAILABLE")
    List<WorkBayPersistenceEntity> findAvailableByBranchIdAndType(
        @Param("tenantId") UUID tenantId,
        @Param("branchId") UUID branchId,
        @Param("type") BayType type
    );

    boolean existsByTenantIdAndBranchIdAndName(UUID tenantId, UUID branchId, String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM WorkBayPersistenceEntity b WHERE b.id = :id AND b.tenantId = :tenantId")
    Optional<WorkBayPersistenceEntity> findByIdForUpdate(
        @Param("id") UUID id,
        @Param("tenantId") UUID tenantId
    );
}
